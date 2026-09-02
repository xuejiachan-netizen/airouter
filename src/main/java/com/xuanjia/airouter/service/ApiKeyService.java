package com.xuanjia.airouter.service;

import com.xuanjia.airouter.model.entity.ApiKey;
import com.xuanjia.airouter.model.entity.User;

import java.util.List;

public interface ApiKeyService {

    /**
     * 创建 API key
     */
    ApiKey createApiKey(String keyName, User loginUser);

    /**
     * 获取用户的 API key 列表
     * @param id
     * @param userId
     * @return
     */
    List<ApiKey> listUserApiKey(Long id, Long userId);

    /**
     * 撤销 API key
     * @param id
     * @param userId
     * @return
     */
    boolean revokeKey(Long id, Long userId);

    /**
     * 根据 Key 查询 API key
     * @param keyValue
     * @return
     */
    ApiKey getByKeyValue(String keyValue);

    void updateUsageStats(Long apiKeyId, Integer tokens);
}
