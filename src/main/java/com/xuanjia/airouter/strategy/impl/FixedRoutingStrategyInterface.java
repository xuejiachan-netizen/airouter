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
public class FixedRoutingStrategyInterface implements RoutingStrategyInterface {
    @Resource
    private ModelService modelService;

    @Override
    public Model selectModel(String modelType, String requestModel) {

        if (requestModel != null){
            return null;
        }

        QueryWrapper queryWrapper = this.pubQueryWrapper(modelType);

        queryWrapper.eq("modelKey", requestModel)
                .limit(1);
        return modelService.getOne(queryWrapper);
    }

    @Override
    public List<Model> getFallbackModel(String modelType, String requestModel) {
        QueryWrapper queryWrapper = this.pubQueryWrapper(modelType);
        queryWrapper.ne("modelKey", requestModel)
                .orderBy("priority", false);

        return modelService.list(queryWrapper);
    }

    @Override
    public String getStrategyType() {
        return RoutingStrategyEnum.FIXED.getValue();
    }

    private QueryWrapper pubQueryWrapper(String modelType){

        return QueryWrapper.create()
                .eq("status", ModelStatusEnum.ACTIVE)
                .in("healthStatus", HealthStatusEnum.HEALTHY, HealthStatusEnum.UNKNOWN,
                        HealthStatusEnum.UNKNOWN)
                .eq("modelType", modelType, modelType != null);
    }
}
