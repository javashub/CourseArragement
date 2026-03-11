package com.lyk.coursearrange.system.rbac.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 系统用户保存请求对象。
 */
@Data
public class SysUserSaveRequest {

    private Long id;

    @NotBlank(message = "用户编码不能为空")
    private String userCode;

    @NotBlank(message = "登录账号不能为空")
    private String username;

    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String displayName;

    private String mobile;

    private String email;

    @NotBlank(message = "用户类型不能为空")
    private String userType;

    private String sourceType;

    private Long sourceId;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
