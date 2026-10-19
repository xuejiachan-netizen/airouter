package com.xuanjia.airouter.service;


import cn.hutool.ai.core.Message;
import com.xuanjia.airouter.model.dto.reqeustLog.RequestLogDto;
import com.xuanjia.airouter.model.entity.RequestLog;

import java.util.List;

public interface RequestLogService {

    /**
     * 记录请求日志

     */
    void logRequest(RequestLogDto logDTO);


    /**
     * 查询用户的日志
     */
    List<RequestLog> listUserLogs(Long userId, Integer limit);

    /**
     * 统计用户的 token 消耗
     */
    List countUserTokens(Long userId);

}
