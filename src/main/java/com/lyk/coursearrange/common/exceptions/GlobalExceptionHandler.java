package com.lyk.coursearrange.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public ServerResponse handleBusinessException(BusinessException exception) {
        log.warn("业务异常: {}", exception.getMessage());
        return ServerResponse.ofError(exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ServerResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ServerResponse.ofError(ResultCode.BAD_REQUEST.getCode(), message, null);
    }

    @ResponseBody
    @ExceptionHandler(BindException.class)
    public ServerResponse handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ServerResponse.ofError(ResultCode.BAD_REQUEST.getCode(), message, null);
    }

    @ResponseBody
    @ExceptionHandler(NotLoginException.class)
    public ServerResponse handleNotLoginException(NotLoginException exception) {
        log.warn("登录校验失败: {}", exception.getMessage());
        return ServerResponse.ofError(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), null);
    }

    @ResponseBody
    @ExceptionHandler(NotPermissionException.class)
    public ServerResponse handleNotPermissionException(NotPermissionException exception) {
        log.warn("权限校验失败: {}", exception.getMessage());
        return ServerResponse.ofError(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), null);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ServerResponse handleException(Exception exception) {
        log.error("系统异常", exception);
        return ServerResponse.ofError(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage(), null);
    }
}
