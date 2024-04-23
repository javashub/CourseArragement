package com.lyk.coursearrange.common.exceptions;

import com.lyk.coursearrange.common.ServerResponse;
import lombok.extern.slf4j.Slf4j;
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
    @ExceptionHandler(AbstractCourseArrangeException.class)
    @ResponseBody
    public ServerResponse error(Exception e) {
        log.error(e.getMessage());
        return ServerResponse.ofError("服务器出现异常");
    }

}
