package com.xuanjia.airouter.service;

import java.time.Duration;

public interface RateLimitService {

    //尝试获取令牌
    boolean tryAcquire(String key, int limit, Duration duration);

    //创建或者获取令牌
    long getAvaliable(String key);

    //根据 api key 检测是否被限流
    boolean checkApiKeyAcquire(String apikey, int limit);

    //根据 ip 检测是否被限流
    boolean checkIpAcquire(String ip, int limit);
}
