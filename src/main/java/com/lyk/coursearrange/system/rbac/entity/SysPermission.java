package com.lyk.coursearrange.system.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限点实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private String resourcePath;

    private String httpMethod;

    private Integer status;
}
