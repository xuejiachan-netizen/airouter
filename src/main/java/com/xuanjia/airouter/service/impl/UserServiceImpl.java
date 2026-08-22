package com.xuanjia.airouter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuanjia.airouter.constant.UserConstant;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.mapper.UserMapper;
import com.xuanjia.airouter.model.vo.LoginUserVO;
import com.xuanjia.airouter.model.vo.UserVO;
import com.xuanjia.airouter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
* @author chenxuanjia
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-08-22 10:44:25
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        if (StringUtil.isEmpty(userAccount) || StringUtil.isEmpty(userPassword) || StringUtil.isEmpty(checkPassword)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "登录注册的内容有误！");
        }

        QueryWrapper account = this.query().eq("userAccount", userAccount);
        long l = this.mapper.selectCountByQuery(account);
        if (l > 0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账户已存在，请重新创建！");
        }

        String encryptPassword = this.getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserAccount(checkPassword);
        boolean save = this.save(user);
        if ( !save){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建账户失败！");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtil.isEmpty(userAccount) || StringUtil.isEmpty(userPassword)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号或密码错误！");
        }

        String encryptPassword = this.getEncryptPassword(userPassword);

        QueryWrapper wr = new QueryWrapper();
        wr = this.query().eq("userAccount", userAccount)
                .eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(wr);
        if (user == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户信息不存在！");
        }
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        LoginUserVO userVO = new LoginUserVO();
        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user =  (User) userObj;
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户信息不存在！");
        }
        User user1 = this.getById(user.getId());
        return user1;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        LoginUserVO userVO = new LoginUserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"获取用户信息失败！");
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public boolean isAdmin(User user) {
        return StringUtils.equals(user.getUserAccount(), "admin");
    }


    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "xuanjia";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }
}
