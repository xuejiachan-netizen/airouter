package com.xuanjia.airouter.service.impl;

import com.xuanjia.airouter.model.entity.ApiKey;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.service.ApiKeyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    @Override
    public ApiKey createApiKey(String keyName, User loginUser) {
        return null;
    }

    @Override
    public List<ApiKey> listUserApiKey(Long id, Long userId) {
        return List.of();
    }

    @Override
    public boolean revokeKey(Long id, Long userId) {
        return false;
    }

    @Override
    public ApiKey getByKeyValue(String keyValue) {
        return null;
    }

    @Override
    public void updateUsageStats(Long apiKeyId, Integer tokens) {

    }
}
