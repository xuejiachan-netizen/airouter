package com.xuanjia.airouter.controller;

import com.xuanjia.airouter.common.BaseResponse;
import com.xuanjia.airouter.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/test")
    public BaseResponse<String> healthTest(){
        return ResultUtils.success("hello!");
    }
}