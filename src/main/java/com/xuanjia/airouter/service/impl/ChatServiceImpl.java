package com.xuanjia.airouter.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.dto.chat.ChatMessage;
import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.ChatResponse;
import com.xuanjia.airouter.service.ChatService;
import com.xuanjia.airouter.service.RequestLogService;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import javax.security.auth.login.CredentialNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private RequestLogService logService;

    @Override
    public ChatResponse chat(ChatRequest chatRequest, Long userId, Long apiKeyId) {
        String modelName = chatRequest.getModel();
        long startTime = System.currentTimeMillis();

        try {
            //构建 prompt
            Prompt prompt = this.buildPrompt(chatRequest);

            //调用模型
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .build();
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt(prompt)
                    .call()
                    .chatResponse();

            //转换响应的格式
            ChatResponse chatResponse = this.convertChatResponse(modelName, aiResponse);

            long currentTime = System.currentTimeMillis();
            long duration = currentTime - startTime;
            logService.logReuqest(userId,apiKeyId,modelName,
                    chatResponse.getUsage().getPromptTokens(),chatResponse.getUsage().getCompletionTokens(), chatResponse.getUsage().getTotalTokens(),
                    (int) duration, "success", null);

            return chatResponse;

        }catch (Exception e){
            log.error("模型调用失败！");
            long currentTime = System.currentTimeMillis();
            long duration = currentTime - startTime;
            logService.logReuqest(userId,apiKeyId,modelName,
                    0,0,0,
                    (int) duration, "failed", e.getMessage());

            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"调用模型失败!!!" + e.getMessage());
        }

    }



    @Override
    public Flux<String> chatStream(ChatRequest chatRequest, Long userId, Long apiKeyId) {
        final int[] promptTokens = {0};
        final int[] completionTokens = {0};

        long startTime = System.currentTimeMillis();
        String modelName = chatRequest.getModel();
        //1、构建 Prompt
        Prompt prompt = this.buildPrompt(chatRequest);

        //2、调用 ChatClient 模型
        Flux<org.springframework.ai.chat.model.ChatResponse> chatResponseFlux = ChatClient.builder(chatModel)
                .build()
                .prompt(prompt)
                .stream()
                .chatResponse();

        //3、流式响应
        return chatResponseFlux.flatMap(chatResponse -> {
            // 3、1 判断 usage 是否存在 并且对 token 进行赋值
            if (chatResponse.getMetadata().getUsage() != null) {
                Integer promptToken = chatResponse.getMetadata().getUsage().getPromptTokens();
                Integer completionToken = chatResponse.getMetadata().getUsage().getCompletionTokens();

                if (promptToken > 0) promptTokens[0] = promptToken;
                if (completionToken > 0) completionTokens[0] = completionToken;
            }

            String text = chatResponse.getResult().getOutput().getText();
            AssistantMessage output = chatResponse.getResult().getOutput();
            if (StringUtil.isEmpty(text) && ObjectUtil.isEmpty(output)) {
                return Flux.empty();
            }

            //修改换行符， 避免和 SSE 冲突
            String escapedText = text.replaceAll("/n", "//n");
            return Flux.just(escapedText + "/n/n");
        }).doOnComplete(() -> {
            long duration = System.currentTimeMillis() - startTime;
            long totalTokens = promptTokens[0] + completionTokens[0];
            logService.logReuqest(userId, apiKeyId, modelName,
                    promptTokens[0], completionTokens[0], (int) totalTokens,
                    (int) duration, "success", null);
        }).doOnError(error -> {
            long duration = System.currentTimeMillis() - startTime;
            logService.logReuqest(userId, apiKeyId, modelName,
                    0, 0, 0,
                    (int) duration, "success", error.getMessage());
        });
    }

    /**
     *  根据不同的角色构建不同的prompt
     *  判断 maxToken 和 temperature 是否需要参与 build
     * @param chatRequest
     * @return
     */
    private Prompt buildPrompt(ChatRequest chatRequest) {
        List<Message> messages = chatRequest.getMessages().stream()
                .map(message -> switch (message.getRole()) {
                    case "assistant" -> new AssistantMessage(message.getContent());
                    case "user" -> new UserMessage(message.getContent());
                    case "system" -> new SystemMessage(message.getContent());
                    default -> throw new IllegalStateException("Unexpected role: " + message.getRole());
                })
                .collect(Collectors.toList());
        OpenAiChatOptions.Builder chatOptions = OpenAiChatOptions.builder()
                .model(chatRequest.getModel())
                .streamUsage(true);

        if (chatRequest.getMaxTokens() != null){
            chatOptions.maxTokens(chatRequest.getMaxTokens());
        }

        if (chatRequest.getTemperature() != null){
            chatOptions.temperature(chatRequest.getTemperature());
        }

        return new Prompt(messages, chatOptions.build());
    }


    /**
     * 回复时根据 chatResponse 的信息进行回复，并且只回复 assistant 的内容
     * @param modelName
     * @param chatResponse
     * @return
     */
    private ChatResponse convertChatResponse(String modelName, org.springframework.ai.chat.model.ChatResponse chatResponse) {
        String message = chatResponse.getResult().getOutput().getText();


        Integer completionTokens = chatResponse.getMetadata().getUsage().getCompletionTokens();
        Integer promptTokens = chatResponse.getMetadata().getUsage().getPromptTokens();
        Integer totalTokens = chatResponse.getMetadata().getUsage().getTotalTokens();

        ChatResponse.Usage usage = ChatResponse.Usage.builder()
                .totalTokens(completionTokens)
                .promptTokens(promptTokens)
                .totalTokens(totalTokens)
                .build();
        ChatResponse.Choice choice = ChatResponse.Choice.builder()
                .index(0)
                .message(new ChatMessage("assistant", message))
                .finishReason(chatResponse.getResult().getMetadata().getFinishReason())
                .build();

        return ChatResponse.builder()
                .id(IdUtil.simpleUUID())
                .object("chat.completion")
                .created(DateUtil.currentSeconds() / 1000)
                .model(modelName)
                .usage(usage)
                .choices(List.of(choice))
                .build();
    }
}
