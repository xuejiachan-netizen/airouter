package com.xuanjia.airouter.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//创建限流器注解
public @interface RateLimit{

    //设置限制类型,默认 apikey
    LimitType limitType() default LimitType.APIKEY;

    //设置窗口
    int window() default 1;

    //设置限制次数
    int limit() default 10;

    //设置通行时间
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    enum LimitType{
        APIKEY,
        IP
    }
}
