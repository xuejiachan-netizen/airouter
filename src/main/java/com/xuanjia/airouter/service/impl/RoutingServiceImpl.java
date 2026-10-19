package com.xuanjia.airouter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xuanjia.airouter.enums.RoutingStrategyEnum;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.service.RoutingService;
import com.xuanjia.airouter.strategy.RoutingStrategyInterface;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 路由服务实现
 *
 * @author <a href="https://codefather.cn">编程导航学习圈</a>
 */
@Service
public class RoutingServiceImpl implements RoutingService {

    @Resource
    private List<RoutingStrategyInterface> routingStrategies;

    @Override
    public Model selectModel(String strategyType, String modelType, String requestedModel) {
        RoutingStrategyInterface strategy = getStrategy(strategyType);
        if (strategy == null) {
            // 默认使用自动路由策略
            strategy = getStrategy(RoutingStrategyEnum.AUTO.getValue());
        }
        return strategy.selectModel(modelType, requestedModel);
    }

    @Override
    public List<Model> getFallbackModels(String strategyType, String modelType, String requestedModel) {
        RoutingStrategyInterface strategy = getStrategy(strategyType);
        if (strategy == null) {
            strategy = getStrategy(RoutingStrategyEnum.AUTO.getValue());
        }
        if (strategy == null) {
            return new ArrayList<>();
        }
        return strategy.getFallbackModel(modelType, requestedModel);
    }

    /**
     * 根据策略类型获取策略实现
     */
    private RoutingStrategyInterface getStrategy(String strategyType) {
        if (StrUtil.isBlank(strategyType)) {
            return null;
        }
        return routingStrategies.stream()
                .filter(strategy -> strategy.getStrategyType().equals(strategyType))
                .findFirst()
                .orElse(null);
    }
}
