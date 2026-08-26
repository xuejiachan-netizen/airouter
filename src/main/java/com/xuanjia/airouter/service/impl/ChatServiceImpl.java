package com.xuanjia.airouter.service.impl;

import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.ChatResponse;
import com.xuanjia.airouter.service.ChatService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService {


    @Override
    public ChatResponse chat(ChatRequest chatRequest, Long userId, Long apiKeyId, String clientIp, String userAgent) {
        //构造 prompt

        //调 chatClient

        //转换响应格式 chatResponse

        //记录请求日志

        return null;
    }

    @Override
    public Flux<String> chatStream(ChatRequest chatRequest, Long userId, Long apiKeyId, String clientIp, String userAgent) {

        return null;
    }
}
