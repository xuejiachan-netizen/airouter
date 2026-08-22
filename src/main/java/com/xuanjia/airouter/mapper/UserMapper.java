package com.xuanjia.airouter.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xuanjia.airouter.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author chenxuanjia
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-08-22 10:44:25
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
