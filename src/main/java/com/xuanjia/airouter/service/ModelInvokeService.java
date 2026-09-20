package com.xuanjia.airouter.service;

import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.StreamChunk;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.model.entity.ModelProvider;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public interface ModelInvokeService {

    /**
     * 调用指定的模型 （非流式）
     * @param model 模型信息
     * @param provider 提供者信息
     * @param request 聊天请求
     * @return 返回的聊天响应
     */
    ChatResponse invoke(Model model, ModelProvider provider, ChatRequest request);

    /**
     * 流式调用模型
     * @param model
     * @param provider
     * @param request
     * @return
     */
    Flux<ChatResponse> invokeStream(Model model, ModelProvider provider, ChatRequest request);

    /**
     * 流式调用模型并返回统一的格式响应块
     * @param model
     * @param provider
     * @param request
     * @return
     */
    Flux<StreamChunk> streamChunk(Model model, ModelProvider provider, ChatRequest request);
}
