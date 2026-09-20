package com.xuanjia.airouter.service;

import com.xuanjia.airouter.model.entity.ModelProvider;

public interface HealthCheckService {

    /**
     * 单提供者健康检查
     * @param modelProvider
     * @return
     */
    boolean checkProviderHealth(ModelProvider modelProvider);

    /**
     * 全量提供者健康检查
     */
    void checkAllProviders();

    /**
     * 延迟测量
     * @param modelProvider
     */
    Integer measureLatency(ModelProvider modelProvider);

    /**
     * 从请求日志统各模型的指标，并更新到 model 和 modelProvider 表
     * 包括平均调用时间、成功率、健康状态、综合得分
     */
    void syncModelMetricsFromRequestLog();
}
