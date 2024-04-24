package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/3/14
 * @Descripe: 封装用户(管理员，讲师)登录请求体
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -5508597805308310771L;
    private String username;

    private String password;

    /**
     * 1管理员，2讲师
     */
    private Integer type;

}
