package com.xuanjia.airouter.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuanjia.airouter.mapper.RequestLogMapper;
import com.xuanjia.airouter.model.entity.RequestLog;
import com.xuanjia.airouter.service.RequestLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.logging.LogRecord;

@Service
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {

    @Resource
    private RequestLogMapper mapper;

    @Override
    public void logReuqest(Long userId, Long apiKeyId, String modelName, Integer promptTokens, Integer completionTokens, Integer totalTokens, Integer duration, String status, String errorMessagers) {
        RequestLog requestLog = RequestLog.builder()
                .apiKeyId(apiKeyId)
                .completionTokens(completionTokens)
                .duration(duration)
                .errorMessage(errorMessagers)
                .modelName(modelName)
                .promptTokens(promptTokens)
                .status(status)
                .build();
        this.save(requestLog);

        if ("success".equals(status) && totalTokens != null && apiKeyId != null && modelName != null){

        }
    }

    @Override
    public List<RequestLog> listUserLogs(Long userId, Integer limit) {
        QueryWrapper logQuery = QueryWrapper.create()
                .eq("userId", userId)
                .limit(limit == null ? 100 : limit);
        return this.list(logQuery);
    }

    @Override
    public List countUserTokens(Long userId) {
        QueryWrapper wr = QueryWrapper.create()
                .select("sum(totalTokens)")
                .eq("userId", userId)
                .eq("success", "status");
        return mapper.selectObjectListByQuery(query());
    }
}
