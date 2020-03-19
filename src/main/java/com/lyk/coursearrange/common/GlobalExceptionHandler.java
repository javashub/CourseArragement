package com.lyk.coursearrange.common;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: 15760
 * @Date: 2020/3/17
 * @Descripe: 统一异常处理
 */

@ControllerAdvice
public class GlobalExceptionHandler {

    // 对所有的异常进行相同的处理
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ServerResponse error(Exception e) {
        e.printStackTrace();
        return ServerResponse.ofError("服务器出现异常");
    }

    // 对特定异常进行处理,更改@ExceptionHandler()中异常的类型即可
    // 如@ExceptionHandler(IOException.class)

}
