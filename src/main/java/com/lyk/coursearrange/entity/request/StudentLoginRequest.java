package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/3/14
 * @Descripe: 封装学生登录的请求体
 */
@Data
public class StudentLoginRequest implements Serializable {

    private static final long serialVersionUID = -4142979714468512398L;
    private String username;

    private String password;
}
