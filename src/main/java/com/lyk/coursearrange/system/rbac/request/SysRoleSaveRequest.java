package com.lyk.coursearrange.system.rbac.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 角色保存请求对象。
 */
@Data
public class SysRoleSaveRequest {

    private Long id;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色类型不能为空")
    private String roleType;

    @NotBlank(message = "数据范围不能为空")
    private String dataScopeType;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortNo;

    private String remark;
}
