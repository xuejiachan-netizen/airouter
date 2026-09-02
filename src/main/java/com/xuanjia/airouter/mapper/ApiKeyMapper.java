package com.xuanjia.airouter.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xuanjia.airouter.model.entity.ApiKey;
import com.xuanjia.airouter.model.entity.RequestLog;
import org.apache.ibatis.annotations.Mapper;

/**
* @author chenxuanjia
* @description 针对表【ApiKeyMapper】的数据库操作Mapper
* @createDate
*/
@Mapper
public interface ApiKeyMapper extends BaseMapper<ApiKey> {

}
