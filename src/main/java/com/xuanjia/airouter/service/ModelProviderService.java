package com.xuanjia.airouter.service;

import com.mybatisflex.core.service.IService;
import com.xuanjia.airouter.model.entity.ModelProvider;

import java.math.BigDecimal;
import java.util.List;

public interface ModelProviderService extends IService<ModelProvider> {
     void updateHealthStatus(Long providerId, String status, long latency, BigDecimal healthRate);


    List<ModelProvider> listHealthModel();
}
