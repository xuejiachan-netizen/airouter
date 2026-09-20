package com.xuanjia.airouter.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuanjia.airouter.enums.HealthStatusEnum;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.mapper.ModelProviderMapper;
import com.xuanjia.airouter.model.entity.ModelProvider;
import com.xuanjia.airouter.service.ModelProviderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ModelProviderServiceImpl extends ServiceImpl<ModelProviderMapper, ModelProvider> implements ModelProviderService {

    @Resource ModelProvider modelProvider;

    @Override
    public void updateHealthStatus(Long providerId, String status, long latency, BigDecimal healthRate) {
        ModelProvider model = ModelProvider
                .builder()
                .id(providerId)
                .status(status)
                .successRate(healthRate)
                .avgLatency((int) latency).build();

        boolean save = this.save(model);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存健康日志错误！");
        }
    }

    @Override
    public List<ModelProvider> listHealthModel() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", HealthStatusEnum.HEALTHY.getValue())
                .orderBy("priority", true);

        return this.list(queryWrapper);
    }


}
