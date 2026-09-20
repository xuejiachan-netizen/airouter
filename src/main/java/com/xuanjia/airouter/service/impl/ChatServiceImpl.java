package com.xuanjia.airouter.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.dto.chat.ChatMessage;
import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.ChatResponse;
import com.xuanjia.airouter.model.dto.chat.StreamChunk;
import com.xuanjia.airouter.model.dto.reqeustLog.RequestLogDto;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.model.entity.ModelProvider;
import com.xuanjia.airouter.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
//todo review 代码，难难难
public class ChatServiceImpl implements ChatService {

    private static final int MAX_FALLBACK_SIZE = 3;

    @Resource
    private ModelInvokeService modelInvokeService;

    @Resource
    private RequestLogService requestLogService;

    @Resource
    private ModelService modelService;

    @Resource
    private RoutingService routingService;
    @Autowired
    private ModelProviderService modelProviderService;

    @Override
    public ChatResponse chat(ChatRequest chatRequest, Long userId, Long apiKeyId, String clientIp, String userAgent) {
        List<Model> models = modelService.listHealthModel();
        String requestModel = chatRequest.getModel();
        String routingStrategy = chatRequest.getRoutingStrategy();
        String traceId = IdUtil.simpleUUID();
        long startTime = System.currentTimeMillis();

        //确认路由策略
        String strategyType = this.determineStrategyType(routingStrategy, requestModel);

        //获取相关的 routing 模型
        Model primaryModel = routingService.selectModel(strategyType,"chat", requestModel);

        //获取相关的 fallback 模型
        List<Model> fallbackModels = routingService.fallbackInvokeModel(strategyType, "chat",requestModel);

        boolean isFallback = false;

        return this.invokeFallback(primaryModel, fallbackModels,
                chatRequest, userId, apiKeyId, traceId, startTime,
                strategyType, isFallback, clientIp, userAgent);
    }



    @Override
    public Flux<String> chatStream(ChatRequest chatRequest, Long userId, Long apiKeyId, String clientIp, String userAgent) {
        String requestedModel = chatRequest.getModel();
        long startTime = System.currentTimeMillis();
        String traceId = IdUtil.simpleUUID();

        // Token 计数器
        final int[] promptTokens = {0};
        final int[] completionTokens = {0};

        // 深度思考状态追踪
        final boolean[] thinkingStarted = {false};
        final boolean[] thinkingEnded = {false};
        final boolean isReasoningEnabled = chatRequest.getEnableReasoning() != null && chatRequest.getEnableReasoning();

        try {
            // 确定路由策略：优先使用请求中指定的策略，否则根据是否指定模型决定
            String strategyType = determineStrategyType(chatRequest.getRoutingStrategy(), requestedModel);

            // 选择模型（策略内部查询数据库）
            Model selectedModel = routingService.selectModel(strategyType, "chat", requestedModel);

            if (selectedModel == null) {
                return Flux.error(new BusinessException(ErrorCode.PARAMS_ERROR, "没有可用的模型"));
            }

            // 获取提供者信息
            ModelProvider provider = modelProviderService.getById(selectedModel.getProviderId());
            if (provider == null) {
                return Flux.error(new BusinessException(ErrorCode.PARAMS_ERROR, "模型提供者不存在"));
            }

            // 调用流式模型，获取统一格式的响应块
            Flux<StreamChunk> chunkFlux = modelInvokeService.streamChunk(selectedModel, provider, chatRequest);

            // 将统一格式的响应块转换为输出字符串
            return chunkFlux.flatMap(chunk -> {
                // 更新 Token 统计
                if (chunk.getPromptTokens() != null && chunk.getPromptTokens() > 0) {
                    promptTokens[0] = chunk.getPromptTokens();
                }
                if (chunk.getCompletionTokens() != null && chunk.getCompletionTokens() > 0) {
                    completionTokens[0] = chunk.getCompletionTokens();
                }

                // 处理深度思考内容
                if (isReasoningEnabled && chunk.hasReasoningContent()) {
                    String reasoningContent = chunk.getReasoningContent();

                    // 思考内容存在且未开始
                    if (!thinkingStarted[0]) {
                        thinkingStarted[0] = true;
                        String thinkingText = "[THINKING]" + escapeNewlines(reasoningContent);
                        return Flux.just(thinkingText + "\n\n");
                    }

                    // 思考内容正在返回
                    if (!thinkingEnded[0]) {
                        return Flux.just(escapeNewlines(reasoningContent) + "\n\n");
                    }
                }

                // 思考内容结束，开始返回答案
                if (isReasoningEnabled && thinkingStarted[0] && !thinkingEnded[0] && !chunk.hasReasoningContent()) {
                    thinkingEnded[0] = true;
                    String endTag = "[/THINKING]\n\n";
                    if (chunk.hasText()) {
                        return Flux.just(endTag + escapeNewlines(chunk.getText()) + "\n\n");
                    }
                    return Flux.just(endTag);
                }

                // 返回普通文本内容
                if ((!isReasoningEnabled || thinkingEnded[0]) && chunk.hasText()) {
                    return Flux.just(escapeNewlines(chunk.getText()) + "\n\n");
                }

                return Flux.empty();
            }).doOnComplete(() -> {
                // 流结束时记录日志
                long duration = System.currentTimeMillis() - startTime;
                int totalTokens = promptTokens[0] + completionTokens[0];
                requestLogService.logRequest(RequestLogDto.builder()
                        .traceId(traceId)
                        .userId(userId)
                        .apiKeyId(apiKeyId)
                        .modelId(selectedModel.getId())
                        .requestModel(selectedModel.getModelKey())
                        .requestType("chat")
                        .source(apiKeyId != null ? "api" : "web")
                        .promptTokens(promptTokens[0])
                        .completionTokens(completionTokens[0])
                        .totalTokens(totalTokens)
                        .duration((int) duration)
                        .status("success")
                        .routingStrategy(strategyType)
                        .isFallback(false)
                        .clientIp(clientIp)
                        .userAgent(userAgent)
                        .build());
            }).doOnError(error -> {
                // 流错误时记录日志
                log.error("流式调用模型失败", error);
                long duration = System.currentTimeMillis() - startTime;
                requestLogService.logRequest(RequestLogDto.builder()
                        .traceId(traceId)
                        .userId(userId)
                        .apiKeyId(apiKeyId)
                        .modelId(selectedModel.getId())
                        .requestModel(selectedModel.getModelKey())
                        .requestType("chat")
                        .source(apiKeyId != null ? "api" : "web")
                        .duration((int) duration)
                        .status("failed")
                        .errorMessage(error.getMessage())
                        .errorCode("STREAM_ERROR")
                        .routingStrategy(strategyType)
                        .isFallback(false)
                        .clientIp(clientIp)
                        .userAgent(userAgent)
                        .build());
            });
        } catch (Exception e) {
            log.error("流式调用模型失败", e);
            return Flux.error(new BusinessException(ErrorCode.SYSTEM_ERROR, "流式调用模型失败: " + e.getMessage()));
        }
    }

    /**
     * 转义换行符
     */
    private String escapeNewlines(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\n", "\\n");
    }

    private String determineStrategyType(String routingStrategy, String requestModel) {
        if (routingStrategy != null && requestModel != null){
            return routingStrategy;
        }else if (routingStrategy == null && requestModel != null){
            return "fixed";
        }

        return "auto";
    }

    private ChatResponse invokeFallback(Model primaryModel, List<Model> fallbackModels,
                                        ChatRequest chatRequest, Long userId, Long apiKeyId,
                                        String traceId, long startTime, String strategyType, boolean isFallback,
                                        String clientIp, String userAgent){
        try {
            return this.callModel(primaryModel, chatRequest, userId, apiKeyId, traceId, startTime, strategyType,
                    isFallback, clientIp, userAgent);
        }catch (Exception e){
            int fallbackSize = Math.min(MAX_FALLBACK_SIZE, fallbackModels.size());
            for (int i = 0; i < fallbackSize; i++) {
                Model fallbackModel = fallbackModels.get(i);
                ChatResponse chatResponse = this.callModel(fallbackModel, chatRequest, userId, apiKeyId, traceId, startTime, strategyType,
                        true, clientIp, userAgent);
                return chatResponse;
            }
            log.error("fallback机制异常，没有找到合适的模型！{}",e.getMessage());
            throw e;
        }
    }


    //modelkey 对应模型标识
    private ChatResponse callModel(Model primaryModel,
                                   ChatRequest chatRequest, Long userId, Long apiKeyId,
                                   String traceId, long startTime, String strategyType, boolean isFallback,
                                   String clientIp, String userAgent){
        try {
            //第一步：调用模型
            Long providerId = primaryModel.getProviderId();
            ModelProvider modelProvider = modelProviderService.getById(providerId);
            org.springframework.ai.chat.model.ChatResponse aiResponse = modelInvokeService.invoke(primaryModel, modelProvider, chatRequest);

            //第二步：转换响应格式
            ChatResponse chatResponse = this.convertChatResonse(aiResponse, primaryModel.getModelKey());
            //第三步：记录相关日志
            requestLogService
                    .logRequest(
                            RequestLogDto.builder()
                                    .apiKeyId(apiKeyId)
                                    .clientIp(clientIp)
                                    .completionTokens(chatResponse.getUsage().getPromptTokens())
                                    .promptTokens(chatResponse.getUsage().getPromptTokens())
                                    .totalTokens(chatResponse.getUsage().getTotalTokens())
                                    .duration((int)(System.currentTimeMillis() - startTime))
                                    .isFallback(isFallback)
                                    .modelId(primaryModel.getId())
                                    .requestModel(primaryModel.getModelKey())
                                    .requestType("chat")
                                    .routingStrategy(strategyType)
                                    .source(apiKeyId != null ? "api" : "web")
                                    .status("success")
                                    .traceId(traceId)
                                    .userAgent(userAgent)
                                    .userId(userId)
                                    .build());
            return chatResponse;
        }catch (Exception e){
            //第三步：记录相关日志
            requestLogService
                    .logRequest(
                            RequestLogDto.builder()
                                    .apiKeyId(apiKeyId)
                                    .clientIp(clientIp)
                                    .duration((int)(System.currentTimeMillis() - startTime))
                                    .isFallback(isFallback)
                                    .modelId(primaryModel.getId())
                                    .requestModel(primaryModel.getModelKey())
                                    .requestType("chat")
                                    .routingStrategy(strategyType)
                                    .source(apiKeyId != null ? "api" : "web")
                                    .status("failure")
                                    .traceId(traceId)
                                    .userAgent(userAgent)
                                    .userId(userId)
                                    .errorMessage(e.getMessage())
                                    .errorCode(ErrorCode.OPERATION_ERROR.getMessage())
                                    .build());
            log.error("error,{}",e.getMessage());
            throw e;
        }
    }

    private ChatResponse convertChatResonse(org.springframework.ai.chat.model.ChatResponse chatResponse, String modelKey) {

        String content = chatResponse.getResult().getOutput().getText();
        String finishReason = chatResponse.getResult().getMetadata().getFinishReason();
        Integer promptTokens = chatResponse.getMetadata().getUsage().getPromptTokens();
        Integer completionTokens = chatResponse.getMetadata().getUsage().getCompletionTokens();
        Integer totalTokens = chatResponse.getMetadata().getUsage().getTotalTokens();

        ChatResponse.Usage usage = ChatResponse.Usage.builder()
                .completionTokens(completionTokens)
                .promptTokens(promptTokens)
                .totalTokens(totalTokens)
                .build();

        ChatResponse.Choice choice = ChatResponse.Choice.builder()
                .index(0)
                .finishReason(finishReason)
                .message(new ChatMessage("assistant", content))
                .build();
        return ChatResponse.builder()
                .choices(List.of(choice))
                .usage(usage)
                .model(modelKey)
                .build();
    }
}
