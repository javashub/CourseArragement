package com.lyk.coursearrange.auth.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户模型。
 * 步骤说明：
 * 1. 统一承载登录后的身份信息。
 * 2. 后续可直接放入登录态或返回前端。
 * 3. 便于后续接入 RBAC3 菜单和权限集合。
 */
@Data
@Builder
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String realName;
    private String displayName;
    private String userType;
    private List<String> roles;
}
