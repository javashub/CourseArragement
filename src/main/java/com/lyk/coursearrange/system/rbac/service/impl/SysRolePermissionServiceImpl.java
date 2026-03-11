package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.rbac.entity.SysRolePermission;
import com.lyk.coursearrange.system.rbac.mapper.SysRolePermissionMapper;
import com.lyk.coursearrange.system.rbac.service.SysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色权限关联服务实现。
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission>
        implements SysRolePermissionService {
}
