package com.lyk.coursearrange.system.rbac.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色权限分配请求。
 */
@Data
public class RolePermissionAssignRequest {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotEmpty(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}
