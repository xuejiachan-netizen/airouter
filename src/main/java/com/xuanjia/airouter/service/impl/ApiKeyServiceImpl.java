package com.xuanjia.airouter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.mapper.ApiKeyMapper;
import com.xuanjia.airouter.model.entity.ApiKey;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.model.vo.ApiKeyVO;
import com.xuanjia.airouter.service.ApiKeyService;
import jodd.util.StringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiKeyServiceImpl extends ServiceImpl<ApiKeyMapper, ApiKey> implements ApiKeyService {


    /**
     * 创建 API key
     */
    @Override
    public ApiKey createApiKey(String keyName, User loginUser) {

        String keyValue = "sk-" + IdUtil.simpleUUID();

        ApiKey apiKey = ApiKey.builder()
                .keyName(keyName)
                .keyValue(keyValue)
                .totalTokens(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status("active")
                .isDelete(0)
                .userId(loginUser.getId())
                .build();

        this.save(apiKey);


        return apiKey;
    }

    /**
     * 获取用户的 API key 列表
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<ApiKey> listUserApiKey(Long userId, int pageNum, int pageSize) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("isDelete", 0)
                .orderBy("createTime", false);
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    @Override
    public Page<ApiKeyVO> listMyApiKeyVO(Page<ApiKey> apiKeyPage) {
        Page<ApiKeyVO> page = new Page<>(apiKeyPage.getPageNumber(),apiKeyPage.getPageSize(),apiKeyPage.getTotalRow());
        List<ApiKeyVO> apiKeyVOS = apiKeyPage.getRecords().stream()
                .map(this::convertApiKeyMask)
                .toList();

        page.setRecords(apiKeyVOS);
        return page;
    }


    /**
     * 撤销 API key
     * @param id
     * @param userId
     * @return
     */
    @Override
    public boolean revokeKey(Long id, Long userId) {
        QueryWrapper revokeWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("keyValue", id);

        ApiKey apiKey = this.getOne(revokeWrapper);

        if (apiKey == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"账户信息不存在！");
        }

        apiKey.setStatus("revoked");
        apiKey.setUpdateTime(LocalDateTime.now());

        return this.updateById(apiKey);
    }


    /**
     * 根据 Key 查询 API key
     * @param keyValue
     * @return
     */
    @Override
    public ApiKey getByKeyValue(String keyValue) {

        if (StringUtil.isEmpty(keyValue)){
            return null;
        }

        QueryWrapper apiWrapper = this.query()
                .eq("keyValue", keyValue)
                .eq("status", "active");

        ApiKey apiKey = this.getOne(apiWrapper);
        return apiKey;
    }

    /**
     * 更新使用状态
     * @param apiKeyId
     * @param tokens
     */
    @Override
    public void updateUsageStats(Long apiKeyId, Integer tokens) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(apiKeyId);
        apiKey.setTotalTokens((long)tokens);
        this.updateById(apiKey);
    }


    private ApiKeyVO convertApiKeyMask(ApiKey apiKey){

        String keyValue = apiKey.getKeyValue();
        apiKey.setKeyValue(keyValue.substring(0,8) + "****" + keyValue.substring(keyValue.length() - 4));

        ApiKeyVO apiKeyVO = new ApiKeyVO();
        BeanUtil.copyProperties(apiKey, apiKeyVO);

        return apiKeyVO;
    }
}
