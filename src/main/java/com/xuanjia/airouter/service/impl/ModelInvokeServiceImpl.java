package com.xuanjia.airouter.service.impl;

import com.xuanjia.airouter.adapter.ModelAdapter;
import com.xuanjia.airouter.adapter.ModelAdapterFactory;
import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.StreamChunk;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.model.entity.ModelProvider;
import com.xuanjia.airouter.service.ModelInvokeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class ModelInvokeServiceImpl implements ModelInvokeService {

    @Resource
    private ModelAdapterFactory modelAdapterFactory;

    @Override
    public ChatResponse invoke(Model model, ModelProvider provider, ChatRequest request) {
        log.info("调用模型:provider:{}, model:{},",provider.getProviderName(), model.getModelKey());
        ModelAdapter modelAdapter = modelAdapterFactory.getModel(provider.getProviderName());
        return modelAdapter.invoke(model, provider, request);

    }

    @Override
    public Flux<ChatResponse> invokeStream(Model model, ModelProvider provider, ChatRequest request) {
        log.info("调用模型:provider:{}, model:{},",provider.getProviderName(), model.getModelKey());
        ModelAdapter modelAdapter = modelAdapterFactory.getModel(provider.getProviderName());
        return modelAdapter.invokeStream(model, provider, request);
    }

    @Override
    public Flux<StreamChunk> streamChunk(Model model, ModelProvider provider, ChatRequest request) {
        log.info("调用模型:provider:{}, model:{},",provider.getProviderName(), model.getModelKey());
        ModelAdapter modelAdapter = modelAdapterFactory.getModel(provider.getProviderName());
        return modelAdapter.invokeStreamChunk(model, provider, request);
    }
}
