package com.lyk.coursearrange.system.rbac.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 权限点保存请求对象。
 */
@Data
public class SysPermissionSaveRequest {

    private Long id;

    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    private String resourcePath;

    private String httpMethod;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
