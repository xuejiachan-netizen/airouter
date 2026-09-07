package com.xuanjia.airouter.controller;


import com.xuanjia.airouter.common.BaseResponse;
import com.xuanjia.airouter.common.ResultUtils;
import com.xuanjia.airouter.exception.BusinessException;
import com.xuanjia.airouter.exception.ErrorCode;
import com.xuanjia.airouter.model.dto.chat.ChatRequest;
import com.xuanjia.airouter.model.entity.User;
import com.xuanjia.airouter.service.ChatService;
import com.xuanjia.airouter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.awt.*;

@RestController
@RequestMapping("/internal/chat")
public class InternalChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    /**
     * todo 调试时检查 requestParam 是否会影响 apikeyid 传值
     * @param chatRequest
     * @param apiKeyId
     * @param httpRequest
     * @return
     */
    @PostMapping(value = "/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public BaseResponse<Object> completions(@RequestBody ChatRequest chatRequest,
                                             Long apiKeyId,
                                            HttpServletRequest httpRequest){
        if (chatRequest == null || apiKeyId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"传递的数据异常！");
        }

        Boolean stream = chatRequest.getStream();
        User loginUser = userService.getLoginUser(httpRequest);

        if (stream){
            return ResultUtils.success(chatService.chatStream(chatRequest, loginUser.getId(), apiKeyId));
        }{
            return ResultUtils.success(chatService.chat(chatRequest, loginUser.getId(), apiKeyId));
        }

    }
}
