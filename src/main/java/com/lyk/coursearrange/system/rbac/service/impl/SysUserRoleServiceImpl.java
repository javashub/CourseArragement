package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.rbac.entity.SysUserRole;
import com.lyk.coursearrange.system.rbac.mapper.SysUserRoleMapper;
import com.lyk.coursearrange.system.rbac.service.SysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户角色关联服务实现。
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
}
