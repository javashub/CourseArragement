package com.lyk.coursearrange.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: 15760
 * @Date: 2020/3/13
 * @Descripe: 响应状态
 */

@AllArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS(0, "success"),
    ERROR(1, "error"),
    VALIDATE_FAILED(404, "参数校验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限");

    // 响应状态码
    private int code;

    // 提示信息
    private String desc;
}
