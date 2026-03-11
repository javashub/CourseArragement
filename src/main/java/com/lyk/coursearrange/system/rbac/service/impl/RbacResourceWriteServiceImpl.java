package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysMenu;
import com.lyk.coursearrange.system.rbac.entity.SysPermission;
import com.lyk.coursearrange.system.rbac.entity.SysRoleMenu;
import com.lyk.coursearrange.system.rbac.entity.SysRolePermission;
import com.lyk.coursearrange.system.rbac.request.SysMenuSaveRequest;
import com.lyk.coursearrange.system.rbac.request.SysPermissionSaveRequest;
import com.lyk.coursearrange.system.rbac.service.RbacResourceWriteService;
import com.lyk.coursearrange.system.rbac.service.SysMenuService;
import com.lyk.coursearrange.system.rbac.service.SysPermissionService;
import com.lyk.coursearrange.system.rbac.service.SysRoleMenuService;
import com.lyk.coursearrange.system.rbac.service.SysRolePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RBAC 资源写服务实现。
 * 步骤说明：
 * 1. 菜单与权限点都需要唯一编码校验。
 * 2. 菜单删除需先校验子节点，避免树结构断裂。
 * 3. 资源删除需要同步清理角色授权关系。
 */
@Slf4j
@Service
public class RbacResourceWriteServiceImpl implements RbacResourceWriteService {

    private final SysMenuService sysMenuService;
    private final SysPermissionService sysPermissionService;
    private final SysRoleMenuService sysRoleMenuService;
    private final SysRolePermissionService sysRolePermissionService;

    public RbacResourceWriteServiceImpl(SysMenuService sysMenuService,
                                        SysPermissionService sysPermissionService,
                                        SysRoleMenuService sysRoleMenuService,
                                        SysRolePermissionService sysRolePermissionService) {
        this.sysMenuService = sysMenuService;
        this.sysPermissionService = sysPermissionService;
        this.sysRoleMenuService = sysRoleMenuService;
        this.sysRolePermissionService = sysRolePermissionService;
    }

    @Override
    public SysMenu saveMenu(SysMenuSaveRequest request) {
        validateMenuCodeUnique(request.getId(), request.getMenuCode());
        validateParentMenu(request.getId(), request.getParentId());
        SysMenu entity = request.getId() == null ? new SysMenu() : getMenuOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        if (entity.getSortNo() == null) {
            entity.setSortNo(99);
        }
        sysMenuService.saveOrUpdate(entity);
        log.info("保存菜单成功，menuId={}, menuCode={}", entity.getId(), entity.getMenuCode());
        return entity;
    }

    @Override
    public void changeMenuStatus(Long menuId, Integer status) {
        SysMenu entity = getMenuOrThrow(menuId);
        entity.setStatus(status);
        sysMenuService.updateById(entity);
        log.info("修改菜单状态成功，menuId={}, status={}", menuId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        SysMenu entity = getMenuOrThrow(menuId);
        LambdaQueryWrapper<SysMenu> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysMenu::getParentId, menuId)
                .eq(SysMenu::getDeleted, 0);
        if (sysMenuService.count(childWrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "请先删除子菜单");
        }
        sysMenuService.removeById(menuId);

        LambdaQueryWrapper<SysRoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(SysRoleMenu::getMenuId, menuId);
        sysRoleMenuService.remove(roleMenuWrapper);
        log.info("删除菜单成功，menuId={}, menuCode={}", menuId, entity.getMenuCode());
    }

    @Override
    public SysPermission savePermission(SysPermissionSaveRequest request) {
        validatePermissionCodeUnique(request.getId(), request.getPermissionCode());
        SysPermission entity = request.getId() == null ? new SysPermission() : getPermissionOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        sysPermissionService.saveOrUpdate(entity);
        log.info("保存权限点成功，permissionId={}, permissionCode={}", entity.getId(), entity.getPermissionCode());
        return entity;
    }

    @Override
    public void changePermissionStatus(Long permissionId, Integer status) {
        SysPermission entity = getPermissionOrThrow(permissionId);
        entity.setStatus(status);
        sysPermissionService.updateById(entity);
        log.info("修改权限点状态成功，permissionId={}, status={}", permissionId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long permissionId) {
        SysPermission entity = getPermissionOrThrow(permissionId);
        sysPermissionService.removeById(permissionId);

        LambdaQueryWrapper<SysRolePermission> rolePermissionWrapper = new LambdaQueryWrapper<>();
        rolePermissionWrapper.eq(SysRolePermission::getPermissionId, permissionId);
        sysRolePermissionService.remove(rolePermissionWrapper);
        log.info("删除权限点成功，permissionId={}, permissionCode={}", permissionId, entity.getPermissionCode());
    }

    private void validateMenuCodeUnique(Long menuId, String menuCode) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getMenuCode, menuCode)
                .eq(SysMenu::getDeleted, 0)
                .ne(menuId != null, SysMenu::getId, menuId);
        if (sysMenuService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "菜单编码已存在");
        }
    }

    private void validatePermissionCodeUnique(Long permissionId, String permissionCode) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermissionCode, permissionCode)
                .eq(SysPermission::getDeleted, 0)
                .ne(permissionId != null, SysPermission::getId, permissionId);
        if (sysPermissionService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "权限编码已存在");
        }
    }

    private void validateParentMenu(Long menuId, Long parentId) {
        if (parentId == null || parentId <= 0) {
            return;
        }
        if (menuId != null && menuId.equals(parentId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "父级菜单不能是自己");
        }
        getMenuOrThrow(parentId);
    }

    private SysMenu getMenuOrThrow(Long menuId) {
        SysMenu entity = sysMenuService.getById(menuId);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        return entity;
    }

    private SysPermission getPermissionOrThrow(Long permissionId) {
        SysPermission entity = sysPermissionService.getById(permissionId);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "权限点不存在");
        }
        return entity;
    }
}
