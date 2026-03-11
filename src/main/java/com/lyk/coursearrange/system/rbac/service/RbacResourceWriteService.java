package com.lyk.coursearrange.system.rbac.service;

import com.lyk.coursearrange.system.rbac.entity.SysMenu;
import com.lyk.coursearrange.system.rbac.entity.SysPermission;
import com.lyk.coursearrange.system.rbac.request.SysMenuSaveRequest;
import com.lyk.coursearrange.system.rbac.request.SysPermissionSaveRequest;

/**
 * RBAC 资源写服务。
 */
public interface RbacResourceWriteService {

    SysMenu saveMenu(SysMenuSaveRequest request);

    void changeMenuStatus(Long menuId, Integer status);

    void deleteMenu(Long menuId);

    SysPermission savePermission(SysPermissionSaveRequest request);

    void changePermissionStatus(Long permissionId, Integer status);

    void deletePermission(Long permissionId);
}
