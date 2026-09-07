package com.xuanjia.airouter.controller;


import com.xuanjia.airouter.common.ResultUtils;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.dto.chat.ChatResponse;
import com.xuanjia.airouter.model.entity.ApiKey;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.service.ApiKeyService;
import com.xuanjia.airouter.service.ChatService;
import com.xuanjia.airouter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/chat")
@Slf4j
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    @Resource
    private ApiKeyService apiKeyService;


    @PostMapping(value = "/completions",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object chatCompletions(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @RequestBody ChatRequest chatRequest,
                                  HttpServletRequest httpRequest
                                  ){
        if (StringUtil.isEmpty(authorization) || authorization.contains("Bearer")){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "认证码无效！");
        }

        if (chatRequest == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "传递的参数为空！");
        }

        User loginUser = userService.getLoginUser(httpRequest);

        ApiKey apiKey = apiKeyService.getByKeyValue(authorization);
        Long apiKeyId = apiKey.getId();

        if (apiKeyId == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"apiKeyId 不存在!");
        }

        Boolean stream = chatRequest.getStream();

        try {
            if (stream){
                Flux<String> stringFlux = chatService.chatStream(chatRequest, loginUser.getId(), apiKeyId);
                return ResultUtils.success(stringFlux);
            }else {
                ChatResponse chatResponse = chatService.chat(chatRequest, loginUser.getId(), apiKeyId);
                return ResultUtils.success(chatResponse);
            }

        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统发生错误!");
        }

    }
}
