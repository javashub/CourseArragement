package com.lyk.coursearrange.system.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.system.rbac.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper。
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
