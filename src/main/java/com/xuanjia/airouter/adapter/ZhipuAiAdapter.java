package com.xuanjia.airouter.adapter;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.dto.chat.ChatMessage;
import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.StreamChunk;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.model.entity.ModelProvider;
import io.netty.handler.stream.ChunkedStream;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ZhipuAiAdapter implements ModelAdapter{

    private final Set<String> SUPPORT_PROVIDERS = Set.of("zhipu", "zhipuai", "glm");

    @Override
    public ChatResponse invoke(Model model, ModelProvider provider, ChatRequest request) {
        try {
            ZhipuAiClient aiClient = this.createClient(provider);
            ChatCompletionCreateParams chatCompletionCreateParams = this.buildRequest(model, request, false);
            ChatCompletionResponse chatResponse = aiClient.chat().createChatCompletion(chatCompletionCreateParams);
            return convertChatResponse(chatResponse);
        }catch (Exception e){
            log.error("智谱 ai 调用异常！调用模型{}失败",model.getModelName(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"智谱ai调用异常！" + e.getMessage());
        }

    }

    @Override
    public Flux<ChatResponse> invokeStream(Model model, ModelProvider modelProvider, ChatRequest chatRequest) {
        try {
            ZhipuAiClient aiClient = this.createClient(modelProvider);
            ChatCompletionCreateParams chatCompletionCreateParams = this.buildRequest(model, chatRequest, true);
            Flowable<ModelData> flowable = aiClient.chat().createChatCompletion(chatCompletionCreateParams).getFlowable();
            return Flux.from(flowable).map(this::convertToChatResponse);

        }catch (Exception e){
            log.error("智谱 ai 调用异常！调用模型{}失败",model.getModelName(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"智谱ai调用异常！" + e.getMessage());
        }
    }

    @Override
    public Flux<StreamChunk> invokeStreamChunk(Model model, ModelProvider provider, ChatRequest chatRequest) {
        try {
            ZhipuAiClient aiClient = this.createClient(provider);
            ChatCompletionCreateParams chatCompletionCreateParams = this.buildRequest(model, chatRequest,true);
            Flowable<ModelData> flowable = aiClient.chat().createChatCompletion(chatCompletionCreateParams).getFlowable();
            return Flux.from(flowable.map(this::convertStreamChunk)
                    .filter(a -> !a.isEmpty()));
        }catch (Exception e){
            log.error("智谱 ai 调用异常！调用模型{}失败",model.getModelName(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"智谱ai调用异常！" + e.getMessage());
        }
    }


    @Override
    public boolean supports(String provierName) {
        return false;
    }

    private ZhipuAiClient createClient(ModelProvider provider) {
        return ZhipuAiClient.builder()
                .ofZHIPU()
                .apiKey(provider.getApiKey())
                .build();
    }


    private ChatResponse convertChatResponse(ChatCompletionResponse chatResponse) {
        ModelData data = chatResponse.getData();
        return this.convertToChatResponse(data);
    }

    private ChatResponse convertToChatResponse(ModelData data) {
        Choice choice = data.getChoices().getFirst();

        String content = choice.getDelta().getContent();
        String reasoningContent = choice.getMessage().getReasoningContent();

        AssistantMessage assistantMessage = new AssistantMessage(reasoningContent != null ? reasoningContent : content);
        Generation generation = new Generation(assistantMessage);

        Usage usage = data.getUsage();
        DefaultUsage defaultUsage = new DefaultUsage(usage.getPromptTokens(), usage.getCompletionTokens());
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .usage(defaultUsage)
                .build();

        return new ChatResponse(List.of(generation), metadata);
    }

    private ChatCompletionCreateParams buildRequest(Model model, ChatRequest request, Boolean stream) {
        List<ai.z.openapi.service.model.ChatMessage> messages = request.getMessages().stream()
                .map(this::convertMessages)
                .collect(Collectors.toList());

        return ChatCompletionCreateParams.builder()
                .temperature(request.getTemperature().floatValue())
                .maxTokens(request.getMaxTokens())
                .stream(stream)
                .messages(messages)
                .thinking(ChatThinking.builder()
                        .type(model.getModelType())
                        .build())
                .build();
    }

    private ai.z.openapi.service.model.ChatMessage convertMessages(ChatMessage chatMessage) {
        String role = chatMessage.getRole().toLowerCase();
        return ai.z.openapi.service.model.ChatMessage.builder()
                .role(role)
                .content(chatMessage.getContent()).build();
    }

    private StreamChunk convertStreamChunk(ModelData modelData) {
        Choice choice = modelData.getChoices().getFirst();

        String content = choice.getDelta().getContent();
        String reasoningContent = choice.getMessage().getReasoningContent();

        return StreamChunk.builder()
                .reasoningContent(reasoningContent)
                .text(content)
                .promptTokens(modelData.getUsage().getCompletionTokens())
                .completionTokens(modelData.getUsage().getCompletionTokens())
                .build();
    }

}
