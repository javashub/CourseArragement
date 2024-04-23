package com.lyk.coursearrange.common.enums;

public enum ErrorCodeEnum {

    SUCCESS(0, "成功"),

    ERROR(9999, "失败"),

    VALIDATE_FAILED(404, "参数校验失败"),

    UNAUTHORIZED(401, "暂未登录或token已经过期"),

    FORBIDDEN(403, "没有相关权限");

    private int code;
    private String desc;

    ErrorCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
