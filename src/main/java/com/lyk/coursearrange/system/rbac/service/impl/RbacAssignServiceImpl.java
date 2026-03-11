package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysMenu;
import com.lyk.coursearrange.system.rbac.entity.SysPermission;
import com.lyk.coursearrange.system.rbac.entity.SysRole;
import com.lyk.coursearrange.system.rbac.entity.SysRoleMenu;
import com.lyk.coursearrange.system.rbac.entity.SysRolePermission;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.entity.SysUserRole;
import com.lyk.coursearrange.system.rbac.request.RoleMenuAssignRequest;
import com.lyk.coursearrange.system.rbac.request.RolePermissionAssignRequest;
import com.lyk.coursearrange.system.rbac.request.UserRoleAssignRequest;
import com.lyk.coursearrange.system.rbac.service.RbacAssignService;
import com.lyk.coursearrange.system.rbac.service.SysMenuService;
import com.lyk.coursearrange.system.rbac.service.SysPermissionService;
import com.lyk.coursearrange.system.rbac.service.SysRoleMenuService;
import com.lyk.coursearrange.system.rbac.service.SysRolePermissionService;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import com.lyk.coursearrange.system.rbac.service.SysUserRoleService;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * RBAC 授权写服务实现。
 */
@Slf4j
@Service
public class RbacAssignServiceImpl implements RbacAssignService {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;
    private final SysPermissionService sysPermissionService;
    private final SysUserRoleService sysUserRoleService;
    private final SysRoleMenuService sysRoleMenuService;
    private final SysRolePermissionService sysRolePermissionService;

    public RbacAssignServiceImpl(SysUserService sysUserService,
                                 SysRoleService sysRoleService,
                                 SysMenuService sysMenuService,
                                 SysPermissionService sysPermissionService,
                                 SysUserRoleService sysUserRoleService,
                                 SysRoleMenuService sysRoleMenuService,
                                 SysRolePermissionService sysRolePermissionService) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.sysMenuService = sysMenuService;
        this.sysPermissionService = sysPermissionService;
        this.sysUserRoleService = sysUserRoleService;
        this.sysRoleMenuService = sysRoleMenuService;
        this.sysRolePermissionService = sysRolePermissionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(UserRoleAssignRequest request) {
        SysUser user = getUserOrThrow(request.getUserId());
        validateRoles(request.getRoleIds());
        LambdaQueryWrapper<SysUserRole> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(SysUserRole::getUserId, request.getUserId());
        sysUserRoleService.remove(removeWrapper);
        List<SysUserRole> entities = new ArrayList<>();
        for (Long roleId : request.getRoleIds()) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(request.getUserId());
            relation.setRoleId(roleId);
            entities.add(relation);
        }
        sysUserRoleService.saveBatch(entities);
        log.info("分配用户角色成功，userId={}, username={}, roleCount={}", user.getId(), user.getUsername(), entities.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenusToRole(RoleMenuAssignRequest request) {
        SysRole role = getRoleOrThrow(request.getRoleId());
        validateMenus(request.getMenuIds());
        LambdaQueryWrapper<SysRoleMenu> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(SysRoleMenu::getRoleId, request.getRoleId());
        sysRoleMenuService.remove(removeWrapper);
        List<SysRoleMenu> entities = new ArrayList<>();
        for (Long menuId : request.getMenuIds()) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(request.getRoleId());
            relation.setMenuId(menuId);
            entities.add(relation);
        }
        sysRoleMenuService.saveBatch(entities);
        log.info("分配角色菜单成功，roleId={}, roleCode={}, menuCount={}", role.getId(), role.getRoleCode(), entities.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissionsToRole(RolePermissionAssignRequest request) {
        SysRole role = getRoleOrThrow(request.getRoleId());
        validatePermissions(request.getPermissionIds());
        LambdaQueryWrapper<SysRolePermission> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(SysRolePermission::getRoleId, request.getRoleId());
        sysRolePermissionService.remove(removeWrapper);
        List<SysRolePermission> entities = new ArrayList<>();
        for (Long permissionId : request.getPermissionIds()) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(request.getRoleId());
            relation.setPermissionId(permissionId);
            entities.add(relation);
        }
        sysRolePermissionService.saveBatch(entities);
        log.info("分配角色权限成功，roleId={}, roleCode={}, permissionCount={}",
                role.getId(), role.getRoleCode(), entities.size());
    }

    @Override
    public List<Long> getAssignedRoleIds(Long userId) {
        getUserOrThrow(userId);
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getDeleted, 0)
                .orderByAsc(SysUserRole::getId);
        return sysUserRoleService.list(wrapper).stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Override
    public List<Long> getAssignedMenuIds(Long roleId) {
        getRoleOrThrow(roleId);
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId)
                .eq(SysRoleMenu::getDeleted, 0)
                .orderByAsc(SysRoleMenu::getId);
        return sysRoleMenuService.list(wrapper).stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    @Override
    public List<Long> getAssignedPermissionIds(Long roleId) {
        getRoleOrThrow(roleId);
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId)
                .eq(SysRolePermission::getDeleted, 0)
                .orderByAsc(SysRolePermission::getId);
        return sysRolePermissionService.list(wrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
    }

    private SysUser getUserOrThrow(Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private SysRole getRoleOrThrow(Long roleId) {
        SysRole role = sysRoleService.getById(roleId);
        if (role == null || role.getDeleted() != null && role.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private void validateRoles(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            getRoleOrThrow(roleId);
        }
    }

    private void validateMenus(List<Long> menuIds) {
        for (Long menuId : menuIds) {
            SysMenu menu = sysMenuService.getById(menuId);
            if (menu == null || menu.getDeleted() != null && menu.getDeleted() == 1) {
                throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
            }
        }
    }

    private void validatePermissions(List<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            SysPermission permission = sysPermissionService.getById(permissionId);
            if (permission == null || permission.getDeleted() != null && permission.getDeleted() == 1) {
                throw new BusinessException(ResultCode.NOT_FOUND, "权限不存在");
            }
        }
    }
}
