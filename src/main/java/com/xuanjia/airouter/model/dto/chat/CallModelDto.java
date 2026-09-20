package com.xuanjia.airouter.model.dto.chat;

import com.xuanjia.airouter.model.entity.Model;
import lombok.Data;

@Data
public class CallModelDto {

    private String traceId;

    private Long userId;

    private Long apiKeyId;

    private Long modelId;

    //实际请求调用的模型
    private String requestModel;

    //请求类型 chat/embedding/image
    private String requestType;

    //请求来源 web/api
    private String source;

    //输入 token 数据
    private Integer promptTokens;

    //输出 token 数据
    private Integer completionTokens;

    //总 token 数据
    private Integer totalTokens;

    //请求耗时（毫秒）
    private int duration;

    //状态 success / failed
    private String suceess;

    //使用的路由策略
    private String routingStrategy;

    //是否为 fallback 请求
    private boolean isFallback;

    //客户端 ip
    private String clientIp;

    //用户智能体
    private String userAgent;

    private Model model;

    private ChatRequest chatRequest;
}
