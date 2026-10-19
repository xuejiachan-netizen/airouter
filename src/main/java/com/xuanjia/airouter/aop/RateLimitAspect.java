package com.xuanjia.airouter.aop;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.xuanjia.airouter.annotation.RateLimit;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.service.RateLimitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private static final String RATE_LIMIT_APIKEY_PREFIX = "rate:limit:apikey:prefix:";

    private static final String RATE_LIMIT_IP_PREFIX = "rate:limit:ip:prefix:";

    @Resource
    private RateLimitService rateLimitService;


    @Around("@annotation(rateLimit)")
    public Object rateLimitAspect(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest requestAttributes = (HttpServletRequest) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null){
            return joinPoint.proceed();
        }

        String apiKey = this.getApiKey(rateLimit, requestAttributes);
        if (StringUtil.isNotEmpty(apiKey)){
            return joinPoint.proceed();
        }

        Duration duration = this.getDuration(rateLimit);

        boolean acquire = rateLimitService.tryAcquire(apiKey, rateLimit.limit(), duration);

        if (!acquire){
            log.info("当前访问请求过多，请稍后重试！");
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,"请求过于频繁！");
        }
        return joinPoint.proceed();
    }

    private Duration getDuration(RateLimit rateLimit) {
        TimeUnit timeUnit = rateLimit.timeUnit();
        int amount = rateLimit.window();
        return switch (timeUnit) {
            case NANOSECONDS -> Duration.ofNanos(amount);
            case MICROSECONDS -> Duration.ofNanos(amount * 1000L);
            case MILLISECONDS -> Duration.ofMillis(amount);
            case SECONDS -> Duration.ofSeconds(amount);
            case MINUTES -> Duration.ofMinutes(amount);
            case HOURS -> Duration.ofHours(amount);
            case DAYS -> Duration.ofDays(amount);
        };
    }


    private String getApiKey(RateLimit rateLimit, HttpServletRequest requestAttributes){
        switch (rateLimit.limitType()){
            case APIKEY :
                String header = requestAttributes.getHeader("Authorization");
                if (StringUtil.isNotEmpty(header) &&header.startsWith("Bearer ")){
                    return RATE_LIMIT_APIKEY_PREFIX + header.substring(7);
                }
            case IP:
                String clientIP = JakartaServletUtil.getClientIP(requestAttributes);
                return RATE_LIMIT_IP_PREFIX + clientIP;
        }
        return null;
    }

}
