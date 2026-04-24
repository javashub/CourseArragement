package com.lyk.coursearrange.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultData<T> implements Serializable {

    private static final long serialVersionUID = -6424544841390528184L;

    /**
     * 成功标志
     */
    private static final String DEFAULT_SUCCESS_CODE = "0";
    /**
     * 默认失败返回码
     */
    private static final String DEFAULT_FAIL_CODE = "9999";

    private boolean success = true;

    /**
     * 返回处理消息
     */
    private String message = "success";

    /**
     * 返回代码
     */
    private String code = "0";

    /**
     * 返回数据对象 data
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    public ResultData() {

    }

    public ResultData(boolean ret) {
        this.success = ret;
        if (!success) {
            code = DEFAULT_FAIL_CODE;
        }
    }

    public static ResultData<Object> success(String message) {
        ResultData<Object> r = new ResultData<>();
        r.message = message;
        r.code = DEFAULT_SUCCESS_CODE;
        r.success = true;
        return r;
    }

    public static ResultData<Object> success() {
        ResultData<Object> r = new ResultData<>();
        r.setSuccess(true);
        r.setCode(DEFAULT_SUCCESS_CODE);
        r.setMessage("success");
        return r;
    }

    public static ResultData<Object> ok(String msg) {
        ResultData<Object> r = new ResultData<>();
        r.setSuccess(true);
        r.setCode(DEFAULT_SUCCESS_CODE);
        r.setMessage(msg);
        return r;
    }

    public static ResultData<Object> ok(Object data) {
        ResultData<Object> r = new ResultData<>();
        r.setSuccess(true);
        r.setCode(DEFAULT_SUCCESS_CODE);
        r.setData(data);
        return r;
    }

    public static ResultData<Object> ok() {
        ResultData<Object> r = new ResultData<>();
        r.setSuccess(true);
        r.setCode(DEFAULT_SUCCESS_CODE);
        return r;
    }

    public static ResultData<?> error() {
        ResultData<?> resultData = new ResultData<>(false);
        return resultData;
    }

    public static ResultData<Object> error(String msg) {
        return error(DEFAULT_FAIL_CODE, msg);
    }

    public static ResultData<Object> error(String code, String msg) {
        ResultData<Object> r = new ResultData<>();
        r.setCode(code);
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    public static ResultData<Object> error(String msg, Object data) {
        ResultData<Object> r = new ResultData<>(false);
        r.setMessage(msg);
        r.data = data;
        return r;
    }
}
