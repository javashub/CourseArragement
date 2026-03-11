package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.rbac.entity.SysRoleMenu;
import com.lyk.coursearrange.system.rbac.mapper.SysRoleMenuMapper;
import com.lyk.coursearrange.system.rbac.service.SysRoleMenuService;
import org.springframework.stereotype.Service;

/**
 * 角色菜单关联服务实现。
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {
}
