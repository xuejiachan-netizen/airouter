package com.xuanjia.airouter.strategy.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xuanjia.airouter.enums.HealthStatusEnum;
import com.xuanjia.airouter.enums.ModelStatusEnum;
import com.xuanjia.airouter.enums.RoutingStrategyEnum;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.service.ModelService;
import com.xuanjia.airouter.strategy.RoutingStrategyInterface;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AutoRoutingStrategyInterface implements RoutingStrategyInterface  {

    @Resource
    private ModelService modelService;

    //选取最优模型
    @Override
    public Model selectModel(String modelType, String requestModel) {
        QueryWrapper wrapper = this.getWrapper(modelType);

        wrapper.limit(1);
        wrapper.orderBy("score",true);
        return modelService.getOne(wrapper);
    }


    //获取除了主模型以外的备选模型
    @Override
    public List<Model> getFallbackModel(String modelType, String requestModel) {
        QueryWrapper wrapper = this.getWrapper(modelType);

        wrapper.orderBy("score", true);
        List<Model> list = modelService.list(wrapper);
        return list.stream().skip(1).toList();
    }


    //返回策略类型表示标识
    @Override
    public String getStrategyType() {
        return RoutingStrategyEnum.AUTO.getValue();
    }

    private QueryWrapper getWrapper(String modelType){
        return QueryWrapper.create()
                .eq("status", ModelStatusEnum.ACTIVE)
                .in("healthStatus", HealthStatusEnum.DEGRADED, HealthStatusEnum.UNKNOWN, HealthStatusEnum.HEALTHY)
                .eq("modelType", modelType, modelType != null);
    }
}
