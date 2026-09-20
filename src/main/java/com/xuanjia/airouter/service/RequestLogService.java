package com.xuanjia.airouter.service;


import cn.hutool.ai.core.Message;
import com.xuanjia.airouter.model.entity.RequestLog;

import java.util.List;

public interface RequestLogService {

    /**
     * 记录请求日志
     * @param userId
     * @param apiKeyId
     * @param modelName
     * @param promptTokens
     * @param completionTokens
     * @param totalTokens
     * @param duration
     * @param status
     * @param errorMessagers
     */
    void logReuqest(Long userId, Long apiKeyId, String modelName,
                    Integer promptTokens, Integer completionTokens, Integer totalTokens,
                    Integer duration, String status, String errorMessagers);


    /**
     * 查询用户的日志
     */
    List<RequestLog> listUserLogs(Long userId, Integer limit);

    /**
     * 统计用户的 token 消耗
     */
    List countUserTokens(Long userId);

}
