package com.xuanjia.airouter.strategy.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xuanjia.airouter.enums.HealthStatusEnum;
import com.xuanjia.airouter.enums.ModelStatusEnum;
import com.xuanjia.airouter.enums.RoutingStrategyEnum;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.service.ModelService;
import com.xuanjia.airouter.strategy.RoutingStrategyInterface;
import jakarta.annotation.Resource;

import java.util.List;

public class LatencyFirstRoutingStrategyInterface implements RoutingStrategyInterface {

    @Resource
    private ModelService modelService;

    @Override
    public Model selectModel(String modelType, String requestModel) {
        QueryWrapper queryWrapper = this.pubGetQueryWrapper(modelType);
        queryWrapper.orderBy("CASE WHEN avglatency = 0 then 9999 " +
                "else avglatency end", true)
                        .limit(1);

        return modelService.getOne(queryWrapper);
    }

    @Override
    public List<Model> getFallbackModel(String modelType, String requestModel) {
        QueryWrapper queryWrapper = this.pubGetQueryWrapper(modelType);
        queryWrapper.orderBy("CASE WHEN avglatency = 0 then 9999 " +
                        "else avglatency end", true);
        List<Model> models = modelService.list(queryWrapper);
        models.removeFirst();
        return models;
    }

    @Override
    public String getStrategyType() {
        return RoutingStrategyEnum.LATENCY_FIRST.getValue();
    }


    private QueryWrapper pubGetQueryWrapper(String modelType){
        return QueryWrapper.create()
                .eq("status", ModelStatusEnum.ACTIVE)
                .in("healthStatus", HealthStatusEnum.HEALTHY, HealthStatusEnum.DEGRADED,
                        HealthStatusEnum.UNKNOWN)
                .eq("modelType", modelType);
    }
}
