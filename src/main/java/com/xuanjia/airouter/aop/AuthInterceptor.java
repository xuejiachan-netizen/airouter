package com.xuanjia.airouter.aop;

import com.xuanjia.airouter.annotation.AuthCheck;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.model.enums.UserRoleEnum;
import com.xuanjia.airouter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthInterceptor {

    private final UserService userService;

    @Around("@annotation(authCheck)")
    public Object authcheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        String userRole = loginUser.getUserRole();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(userRole);
        if (mustRoleEnum == null){
            return joinPoint.proceed();
        }

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);

        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限，禁止访问！");
        }

        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && ! UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限，禁止访问！");
        }
        return joinPoint.proceed();
    }

}
