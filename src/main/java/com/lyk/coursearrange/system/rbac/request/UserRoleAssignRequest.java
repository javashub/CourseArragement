package com.lyk.coursearrange.system.rbac.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户角色分配请求。
 */
@Data
public class UserRoleAssignRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
