package com.lyk.coursearrange.system.rbac.query;

import com.lyk.coursearrange.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPageQuery extends PageQuery {

    private String keyword;

    private String userType;

    private Integer status;
}
