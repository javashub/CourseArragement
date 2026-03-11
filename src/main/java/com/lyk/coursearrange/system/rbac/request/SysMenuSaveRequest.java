package com.lyk.coursearrange.system.rbac.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 菜单保存请求对象。
 */
@Data
public class SysMenuSaveRequest {

    private Long id;

    @NotBlank(message = "菜单编码不能为空")
    private String menuCode;

    @NotNull(message = "父级菜单不能为空")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    private String routeName;

    private String routePath;

    private String componentPath;

    private String icon;

    private String permissionCode;

    @NotNull(message = "是否隐藏不能为空")
    private Integer isHidden;

    @NotNull(message = "是否缓存不能为空")
    private Integer isKeepAlive;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortNo;

    private String remark;
}
