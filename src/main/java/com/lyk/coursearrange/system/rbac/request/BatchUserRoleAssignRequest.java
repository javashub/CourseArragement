package com.lyk.coursearrange.system.rbac.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量为用户分配角色请求。
 */
@Data
public class BatchUserRoleAssignRequest {

    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
