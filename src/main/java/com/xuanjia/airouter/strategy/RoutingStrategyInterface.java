package com.xuanjia.airouter.strategy;

import com.mybatisflex.core.query.QueryWrapper;
import com.xuanjia.airouter.model.entity.Model;

import java.util.List;

/**
 * 路由策略入口
 */
public interface RoutingStrategyInterface {

    /**
     * 选择对应的策略模型
     * @param modelType
     * @param requestModel
     * @return
     */
    Model selectModel(String modelType, String requestModel);

    /**
     * 获取 fallback 模型列表
     * @param modelType
     * @param requestModel
     * @return
     */
    List<Model> getFallbackModel(String modelType, String requestModel);


    /**
     * 获取策略类型
     * @return
     */
    String getStrategyType();

}
