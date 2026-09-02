package com.xuanjia.airouter.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xuanjia.airouter.model.entity.RequestLog;
import com.xuanjia.airouter.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author chenxuanjia
* @description 针对表【RequestLog】的数据库操作Mapper
* @createDate
*/
@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLog> {

}
