package com.xuanjia.airouter.service;

import com.xuanjia.airouter.model.entity.Model;

import java.util.List;

public interface RoutingService {
    Model selectModel(String strategyType, String modelType, String requestModel);

    List<Model> fallbackInvokeModel(String strategyType, String modelType, String requestModel);
}
