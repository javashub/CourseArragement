package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/14
 * @Descripe: 封装管理员登录请求体
 */
@Data
public class AdminLoginRequest {

    private Integer id;

    private String username;

    private String password;

    /**
     * 1普通管理员，0超级管理员
     */
    private Integer power;

}
