package com.xuanjia.airouter.service.impl;

import com.xuanjia.airouter.enums.RoutingStrategyEnum;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.service.RoutingService;
import com.xuanjia.airouter.strategy.RoutingStrategyInterface;
import jakarta.annotation.Resource;

import java.util.List;

public class RoutingServiceImpl implements RoutingService {

    @Resource
    List<RoutingStrategyInterface> routingStrategys;

    @Override
    public Model selectModel(String strategyType,  String modelType,String requestModel) {
        RoutingStrategyInterface routingStrategy = this.gettingStrategy(strategyType);
        if (routingStrategy == null){
            routingStrategy = this.gettingStrategy(RoutingStrategyEnum.AUTO.getValue());
        }
        return routingStrategy.selectModel(modelType, requestModel);
    }

    @Override
    public List<Model> fallbackInvokeModel(String strategyType, String modelType,String requestModel) {
        RoutingStrategyInterface routingStrategy = this.gettingStrategy(strategyType);
        if (routingStrategy == null){
            routingStrategy = this.gettingStrategy(RoutingStrategyEnum.AUTO.getValue());
        }
        return routingStrategy.getFallbackModel(modelType, requestModel);
    }


    private RoutingStrategyInterface gettingStrategy(String strategyType){

        return routingStrategys.stream()
                .filter(routingStrategyInterface -> routingStrategyInterface.getStrategyType().equals(strategyType))
                .findFirst()
                .orElse(null);
    }
}