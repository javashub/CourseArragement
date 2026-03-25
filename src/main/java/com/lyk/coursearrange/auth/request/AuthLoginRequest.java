package com.lyk.coursearrange.auth.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一登录请求。
 */
@Data
public class AuthLoginRequest implements Serializable {

    private static final long serialVersionUID = 2178650936605312856L;

    private String username;

    private String password;

    private String userType;
}
