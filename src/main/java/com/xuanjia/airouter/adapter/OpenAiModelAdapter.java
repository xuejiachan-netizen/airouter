package com.xuanjia.airouter.adapter;

import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.StreamChunk;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.model.entity.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OpenAiModelAdapter implements ModelAdapter{

    private static final Set<String> SUPPORT_PROVIDERS = Set.of("gpt","openai");

    @Override
    public ChatResponse invoke(Model model, ModelProvider provider, ChatRequest request) {
        try {
            Prompt prompt = this.buildPrompt(request, model.getModelKey());
            OpenAiChatModel chatModel = this.createChatModel(provider, model);
            return chatModel.call(prompt);
        }catch (Exception e){
            log.error("openAi 适配器调用失败{},请检查!",model.getModelKey(),e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

    }

    @Override
    public Flux<ChatResponse> invokeStream(Model model, ModelProvider modelProvider, ChatRequest chatRequest) {
        try {
            Prompt prompt = this.buildPrompt(chatRequest, modelProvider.getApiKey());
            OpenAiChatModel chatModel = this.createChatModel(modelProvider, model);
            return chatModel.stream(prompt);
        }catch (Exception e){
            log.error("openAi 适配器调用失败{},请检查!",model.getModelKey(),e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

    }

    @Override
    public Flux<StreamChunk> invokeStreamChunk(Model model, ModelProvider provider, ChatRequest chatRequest) {
        Flux<ChatResponse> chatResponseFlux = this.invokeStream(model, provider, chatRequest);
        return chatResponseFlux.map(this::convertToStreamChunk)
                .filter(streamChunk -> !streamChunk.isEmpty());
    }

    @Override
    public boolean supports(String provierName) {
        return provierName != null && SUPPORT_PROVIDERS.contains(provierName);
    }


    private Prompt buildPrompt(ChatRequest request, String modelKey){
        List<Message> messages = request.getMessages().stream()
                .map(msg -> switch (msg.getRole()) {
                    case "system" -> new SystemMessage(msg.getContent());
                    case "user" -> new UserMessage(msg.getContent());
                    case "assistant" -> new AssistantMessage(msg.getContent());
                    default -> throw new IllegalStateException("Unexpected role: " + msg.getRole());
                }).collect(Collectors.toList());
        return Prompt.builder()
                .messages(messages)
                .chatOptions(ChatOptions.builder()
                        .model(modelKey)
                        .temperature(request.getTemperature() >= 0 ? request.getTemperature() : 0)
                        .maxTokens(request.getMaxTokens() >=0 ? request.getMaxTokens() : 30)
                        .build())
                .build();
    }

    private OpenAiChatModel createChatModel(ModelProvider provider, Model model){
          return OpenAiChatModel.builder()
                .options(
                        OpenAiChatOptions.builder()
                                .apiKey(provider.getApiKey())
                                .baseUrl(provider.getBaseUrl())
                                .model(model.getModelKey())
                                .build()
                )
                .build();
    }

    private StreamChunk convertToStreamChunk(ChatResponse response){
        String text = response.getResult().getOutput().getText();
        Integer promptTokens = response.getMetadata().getUsage().getPromptTokens();
        Integer completionTokens = response.getMetadata().getUsage().getCompletionTokens();

        return StreamChunk.builder()
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .text(text)
                .build();
    }
}
