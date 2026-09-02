package com.xuanjia.airouter.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.xuanjia.airouter.common.BaseResponse;
import com.xuanjia.airouter.common.ResultUtils;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.dto.apikey.ApiKeyCreatedRequest;
import com.xuanjia.airouter.model.entity.ApiKey;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.model.vo.ApiKeyVO;
import com.xuanjia.airouter.service.ApiKeyService;
import com.xuanjia.airouter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping( "/api/key")
public class ApiKeyController {

    @Resource
    private ApiKeyService apiKeyService;

    @Resource
    private UserService userService;

    /**
     * 创建 apikey 并返回给前端
     * @param createdRequest
     * @param request
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<ApiKeyVO> createApiKey(@RequestBody ApiKeyCreatedRequest createdRequest, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        ApiKey apiKey = apiKeyService.createApiKey(createdRequest.getKeyName(), loginUser);

        ApiKeyVO apiKeyVO = new ApiKeyVO();
        BeanUtil.copyProperties(apiKey,apiKeyVO);

        return ResultUtils.success(apiKeyVO);
    }


    @GetMapping("/list/my")
    public BaseResponse<Page<ApiKeyVO>> listMyApiKey(@RequestParam(defaultValue = "10") int pageNum,
                                                     @RequestParam(defaultValue = "1") int pageSize,
                                                     HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);

        Page<ApiKey> apiKeyPage = apiKeyService.listUserApiKey(loginUser.getId(), pageNum, pageSize);

        Page<ApiKeyVO> apiKeyVOPage = apiKeyService.listMyApiKeyVO(apiKeyPage);

        return ResultUtils.success(apiKeyVOPage);
    }


    @PostMapping("/revoke")
    public BaseResponse<Boolean> revokeApiKey(@RequestParam("id") long id, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        boolean b = apiKeyService.revokeKey(id, loginUser.getId());
        return ResultUtils.success(b);
    }

}
