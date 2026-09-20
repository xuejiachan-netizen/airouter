package com.xuanjia.airouter.strategy.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xuanjia.airouter.enums.HealthStatusEnum;
import com.xuanjia.airouter.enums.ModelStatusEnum;
import com.xuanjia.airouter.enums.RoutingStrategyEnum;
import com.xuanjia.airouter.model.entity.Model;
import com.xuanjia.airouter.service.ModelService;
import com.xuanjia.airouter.strategy.RoutingStrategyInterface;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CostFirstStrategyInterface implements RoutingStrategyInterface {

    private ModelService modelService;

    @Override
    public Model selectModel(String modelType, String requestModel) {
        QueryWrapper queryWrapper = this.pubQueryWrapper(modelType);

        queryWrapper.orderBy("inputPrice + outPutPrice",true)
                        .limit(1);

        return modelService.getOne(queryWrapper);
    }

    @Override
    public List<Model> getFallbackModel(String modelType, String requestModel) {

        QueryWrapper queryWrapper = this.pubQueryWrapper(modelType);

        queryWrapper.orderBy("inputPrice + outPutPrice",true);

        List<Model> models = modelService.list(queryWrapper);
         models.removeFirst();
         return models;
    }

    @Override
    public String getStrategyType() {
        return RoutingStrategyEnum.COST_FIRST.getValue();
    }

    private QueryWrapper pubQueryWrapper(String modelType){
        return QueryWrapper.create()
                .eq("status", ModelStatusEnum.ACTIVE)
                .in("healthStatus", HealthStatusEnum.HEALTHY,
                        HealthStatusEnum.DEGRADED,
                        HealthStatusEnum.UNKNOWN)
                .eq("modelType", modelType, modelType != null);
    }
}
