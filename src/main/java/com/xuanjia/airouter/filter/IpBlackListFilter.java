package com.xuanjia.airouter.filter;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjia.airouter.common.BaseResponse;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.service.BlacklistService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class IpBlackListFilter extends OncePerRequestFilter {

    @Resource
    private BlacklistService blacklistService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String clientIP = JakartaServletUtil.getClientIP(request);

        if (blacklistService.isBlocked(clientIP)){
            this.filterBlackList(response, ErrorCode.FORBIDDEN_ERROR,"所属 ip 被封禁");
        }
    }

    private void filterBlackList(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        BaseResponse<Object> errorResponse = new BaseResponse<>(errorCode.getCode(), null, message);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
