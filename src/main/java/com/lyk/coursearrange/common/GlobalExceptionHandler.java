package com.lyk.coursearrange.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: 15760
 * @Date: 2020/3/17
 * @Descripe: 统一异常处理
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


    // 对所有的异常进行相同的处理
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ServerResponse error(Exception e) {
        log.error(e.getMessage());
        return ServerResponse.ofError("服务器出现异常");
    }

}
