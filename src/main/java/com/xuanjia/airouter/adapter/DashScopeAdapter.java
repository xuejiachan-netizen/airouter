package com.xuanjia.airouter.adapter;

import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.StreamChunk;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.model.entity.ModelProvider;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public class DashScopeAdapter implements ModelAdapter{
    @Override
    public ChatResponse invoke(Model model, ModelProvider provider, ChatRequest request) {
        return null;
    }

    @Override
    public Flux<ChatResponse> invokeStream(Model model, ModelProvider modelProvider, ChatRequest chatRequest) {
        return null;
    }

    @Override
    public Flux<StreamChunk> invokeStreamChunk(Model model, ModelProvider provider, ChatRequest chatRequest) {
        return null;
    }

    @Override
    public boolean supports(String provierName) {
        return false;
    }
}
