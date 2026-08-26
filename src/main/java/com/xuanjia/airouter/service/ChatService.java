package com.xuanjia.airouter.service;

import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 非流式
     * @param chatRequest 聊天请求
     * @param userId     用户id
     * @param apiKeyId
     * @param clientIp
     * @param userAgent
     * @return 非流式聊天响应
     */
    ChatResponse chat(ChatRequest chatRequest, Long userId, Long apiKeyId, String clientIp, String userAgent);

    /**
     * 流式
     * @param chatRequest 聊天请求
     * @param userId 用户 id
     * @param apiKeyId
     * @param clientIp
     * @param userAgent
     * @return 流式聊天请求
     */
    Flux<String> chatStream(ChatRequest chatRequest, Long userId, Long apiKeyId, String clientIp, String userAgent);
}
