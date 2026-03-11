package com.lyk.coursearrange.system.rbac.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 当前登录用户视图对象。
 */
@Data
@Builder
public class CurrentUserVO {

    private Long userId;

    private String username;

    private String realName;

    private String displayName;

    private String userType;

    private List<String> roles;
}
