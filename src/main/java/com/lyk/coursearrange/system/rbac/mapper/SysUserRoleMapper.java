package com.lyk.coursearrange.system.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.system.rbac.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
