package com.xuanjia.airouter.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xuanjia.airouter.model.entity.ApiKey;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.model.vo.ApiKeyVO;

public interface ApiKeyService extends IService<ApiKey> {

    /**
     * 创建 API key
     */
    ApiKey createApiKey(String keyName, User loginUser);

    /**
     * 获取用户的 API key 列表
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<ApiKey> listUserApiKey(Long userId, int pageNum, int pageSize);

    /**
     * 返回给前端的 apikey 信息
     * @return
     */
    Page<ApiKeyVO> listMyApiKeyVO(Page<ApiKey> apiKeyPage);

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
