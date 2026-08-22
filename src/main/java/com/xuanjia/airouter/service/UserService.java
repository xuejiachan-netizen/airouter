package com.xuanjia.airouter.service;

import com.mybatisflex.core.service.IService;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.model.vo.LoginUserVO;
import com.xuanjia.airouter.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author chenxuanjia
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-08-22 10:44:25
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 是否为管理员
     */
    boolean isAdmin(User user);
}
