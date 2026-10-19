package com.xuanjia.airouter.service.impl;

import com.xuanjia.airouter.service.RateLimitService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    private static final String RATE_LIMIT_APIKEY_PREFIX = "rate:limit:apikey:prefix:";

    private static final String RATE_LIMIT_IP_PREFIX = "rate:limit:ip:prefix:";

    @Resource
    private RedissonClient redissonClient;
    /**
     * 尝试获取并消耗一个令牌
     */
    @Override
    public boolean tryAcquire(String key, int limit, Duration duration) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        //建立规则
        rateLimiter.trySetRate(RateType.OVERALL, limit, duration);


        //尝试消耗一个令牌
        boolean acquire = rateLimiter.tryAcquire();

        if (!acquire){
            log.info("令牌无效或者已失效！");
        }

        return acquire;
    }

    /**
     * 尝试创建一个令牌
     */
    @Override
    public long getAvaliable(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        return rateLimiter.availablePermits();
    }


    /**
     * 检查 apikey 是否被限流
     *
     * @return
     */
    @Override
    public boolean checkApiKeyAcquire(String apikey, int limit) {
        String key = RATE_LIMIT_APIKEY_PREFIX + apikey;
        return tryAcquire(key, limit, Duration.ofSeconds(1L));
    }

    @Override
    public boolean checkIpAcquire(String ip, int limit) {
        String key = RATE_LIMIT_IP_PREFIX + ip;
        return tryAcquire(key, limit, Duration.ofSeconds(1L));
    }
}
