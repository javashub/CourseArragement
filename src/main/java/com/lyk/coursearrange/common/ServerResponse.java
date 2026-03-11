package com.lyk.coursearrange.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lyk.coursearrange.common.api.ApiResponse;
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
public class ServerResponse<T> {

    // 响应码
    private int code;

    // 信息
    private String message;

    // 数据
    private T data;



    private ServerResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @JsonIgnore
    public boolean isSuccess() {
        // 0成功，1失败
        return this.code == ResponseCode.SUCCESS.getCode();
    }


    public static <T> ServerResponse<T> ofSuccess() {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getDesc());
    }


    public static <T> ServerResponse<T> ofSuccess(T obj) {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getDesc(), obj);
    }


    public static <T> ServerResponse<T> ofSuccess(int code, String msg, T obj) {
        return new ServerResponse(code, msg, obj);
    }


    public static <T> ServerResponse<T> ofSuccess(String msg) {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), msg);
    }


    public static <T> ServerResponse<T> ofSuccess(String msg, T obj) {
        return new ServerResponse(ResponseCode.SUCCESS.getCode(), msg, obj);
    }


    public static <T> ServerResponse<T> ofError(int code, String msg, T obj) {
        return new ServerResponse(code, msg, obj);
    }


    public static <T> ServerResponse<T> ofError() {
        return new ServerResponse(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }


    public static <T> ServerResponse<T> ofError(String msg) {
        return new ServerResponse(ResponseCode.ERROR.getCode(), msg);
    }


    public static <T> ServerResponse<T> ofError(T obj) {
        return new ServerResponse(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc(), obj);
    }



    public static <T> ServerResponse<T> ofError(String msg, T obj) {
        return new ServerResponse(ResponseCode.ERROR.getCode(), msg, obj);
    }

    /**
     * 将旧响应结构平滑转换为新的统一响应结构。
     */
    public ApiResponse<T> toApiResponse() {
        return new ApiResponse<>(this.code, this.message, this.data);
    }
}
