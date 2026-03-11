package com.lyk.coursearrange.common.api;

import com.lyk.coursearrange.common.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> fail(ResultCode resultCode) {
        return ApiResponse.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> fail(ResultCode resultCode, String message) {
        return ApiResponse.<T>builder()
                .code(resultCode.getCode())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> fail(Integer code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
