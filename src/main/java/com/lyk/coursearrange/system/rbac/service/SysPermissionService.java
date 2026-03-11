package com.lyk.coursearrange.system.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.system.rbac.entity.SysPermission;

/**
 * 权限服务。
 */
public interface SysPermissionService extends IService<SysPermission> {

    java.util.List<String> listPermissionCodesByUserId(Long userId);
}
