package com.xuanjia.airouter.adapter;

import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ModelAdapterFactory {
    @Resource
    private List<ModelAdapter> modelAdapters;
    @Autowired
    private Model model;

    public ModelAdapter getModel(String providerName){
        for (ModelAdapter modelAdapter : modelAdapters) {
            if (modelAdapter.supports(providerName)){
                log.info("当前选择的模型适配器{}",providerName);
                return modelAdapter;
            }
        }

        return modelAdapters.stream()
                .filter(modelAdapter -> modelAdapter instanceof OpenAiModelAdapter)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, "没有找到相关的请求大模型!"));
    }

}