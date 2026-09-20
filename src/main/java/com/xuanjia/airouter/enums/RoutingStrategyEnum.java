package com.xuanjia.airouter.enums;

import lombok.Getter;

@Getter
public enum RoutingStrategyEnum {
    AUTO("auto", "自动路由"),
    COST_FIRST("cost_first", "成本优先"),
    LATENCY_FIRST("latency_first", "延迟优先"),
    ROUND_ROBIN("round_robin", "轮询"),
    FIXED("fixed", "固定模型");

    private final String value;
    private final String text;

    RoutingStrategyEnum(String value, String text){
        this.value = value;
        this.text = text;
    }

    public static RoutingStrategyEnum getRoutingStrategyEnum(String value){

        if (value.isEmpty()){
            return null;
        }

        for (RoutingStrategyEnum routingStrategyEnum : RoutingStrategyEnum.values()) {
            if (routingStrategyEnum.value.equals(value)){
                return routingStrategyEnum;
            }
        }
        return null;
    }
}
