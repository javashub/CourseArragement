package com.lyk.coursearrange.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author: 15760
 * @Date: 2020/3/13
 * @Descripe: 响应信息
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {

    // 响应码
    private int code;

    // 信息
    private String message;

    // 数据
    private Object data;



    private  ServerResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @JsonIgnore
    public boolean isSuccess() {
        // 0表示成功，1表示失败
        return this.code == ResponseCode.SUCCESS.getCode();
    }

    // 成功: 状态码、提示信息
    public static ServerResponse ofSuccess() {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getDesc());
    }

    // 成功: 状态码、提示信息、数据模型
    public static ServerResponse ofSuccess(Object obj) {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getDesc(), obj);
    }

    // 成功: 状态码、提示信息、数据模型
    public static ServerResponse ofSuccess(int code, String msg, Object obj) {
        return new ServerResponse(code, msg, obj);
    }

    // 成功: 提示信息
    public static ServerResponse ofSuccess(String msg) {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), msg);
    }

    // 成功: 提示信息、数据模型
    public static ServerResponse ofSuccess(String msg, Object obj) {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), msg, obj);
    }

    // 失败: 状态码、提示信息、数据模型
    public static ServerResponse ofError(int code, String msg, Object obj) {
        return new ServerResponse(code, msg, obj);
    }

    // 失败
    public static ServerResponse ofError() {
        return new ServerResponse(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    // 失败: 状态码、提示信息、数据模型
    public static ServerResponse ofError(String msg) {
        return new ServerResponse(ResponseCode.ERROR.getCode(), msg);
    }

    // 失败: 数据模型
    public static ServerResponse ofError(Object obj) {
        return new ServerResponse(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc(), obj);
    }

    // 失败: 提示信息、数据模型
    public static ServerResponse ofError(String msg, Object obj) {
        return new ServerResponse(ResponseCode.ERROR.getCode(), msg, obj);
    }
}
