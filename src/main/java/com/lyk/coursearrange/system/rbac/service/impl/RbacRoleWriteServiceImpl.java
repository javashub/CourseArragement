package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysRole;
import com.lyk.coursearrange.system.rbac.request.SysRoleSaveRequest;
import com.lyk.coursearrange.system.rbac.service.RbacRoleWriteService;
import com.lyk.coursearrange.system.rbac.service.SysRoleMenuService;
import com.lyk.coursearrange.system.rbac.service.SysRolePermissionService;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import com.lyk.coursearrange.system.rbac.service.SysUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RBAC 角色写服务实现。
 * 步骤说明：
 * 1. 角色保存需要校验编码唯一性。
 * 2. 角色删除需要同时清理关联关系，保证授权数据一致。
 * 3. 角色启停单独收口，方便后续接审计日志。
 */
@Slf4j
@Service
public class RbacRoleWriteServiceImpl implements RbacRoleWriteService {

    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final SysRoleMenuService sysRoleMenuService;
    private final SysRolePermissionService sysRolePermissionService;

    public RbacRoleWriteServiceImpl(SysRoleService sysRoleService,
                                    SysUserRoleService sysUserRoleService,
                                    SysRoleMenuService sysRoleMenuService,
                                    SysRolePermissionService sysRolePermissionService) {
        this.sysRoleService = sysRoleService;
        this.sysUserRoleService = sysUserRoleService;
        this.sysRoleMenuService = sysRoleMenuService;
        this.sysRolePermissionService = sysRolePermissionService;
    }

    @Override
    public SysRole saveRole(SysRoleSaveRequest request) {
        validateRoleCodeUnique(request.getId(), request.getRoleCode());
        SysRole entity = request.getId() == null ? new SysRole() : getRoleOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        if (entity.getSortNo() == null) {
            entity.setSortNo(99);
        }
        sysRoleService.saveOrUpdate(entity);
        log.info("保存角色成功，roleId={}, roleCode={}", entity.getId(), entity.getRoleCode());
        return entity;
    }

    @Override
    public void changeRoleStatus(Long roleId, Integer status) {
        SysRole entity = getRoleOrThrow(roleId);
        entity.setStatus(status);
        sysRoleService.updateById(entity);
        log.info("修改角色状态成功，roleId={}, status={}", roleId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole entity = getRoleOrThrow(roleId);
        sysRoleService.removeById(roleId);

        LambdaQueryWrapper<com.lyk.coursearrange.system.rbac.entity.SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(com.lyk.coursearrange.system.rbac.entity.SysUserRole::getRoleId, roleId);
        sysUserRoleService.remove(userRoleWrapper);

        LambdaQueryWrapper<com.lyk.coursearrange.system.rbac.entity.SysRoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(com.lyk.coursearrange.system.rbac.entity.SysRoleMenu::getRoleId, roleId);
        sysRoleMenuService.remove(roleMenuWrapper);

        LambdaQueryWrapper<com.lyk.coursearrange.system.rbac.entity.SysRolePermission> rolePermissionWrapper = new LambdaQueryWrapper<>();
        rolePermissionWrapper.eq(com.lyk.coursearrange.system.rbac.entity.SysRolePermission::getRoleId, roleId);
        sysRolePermissionService.remove(rolePermissionWrapper);

        log.info("删除角色成功，roleId={}, roleCode={}", roleId, entity.getRoleCode());
    }

    private void validateRoleCodeUnique(Long roleId, String roleCode) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDeleted, 0)
                .ne(roleId != null, SysRole::getId, roleId);
        if (sysRoleService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "角色编码已存在");
        }
    }

    private SysRole getRoleOrThrow(Long roleId) {
        SysRole entity = sysRoleService.getById(roleId);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return entity;
    }
}
