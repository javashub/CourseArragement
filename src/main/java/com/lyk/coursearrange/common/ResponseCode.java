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
    ERROR(1, "error");

    // 响应状态码
    private int code;

    // 提示信息
    private String desc;
}
