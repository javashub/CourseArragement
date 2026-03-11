package com.lyk.coursearrange.system.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.system.rbac.entity.SysRole;

/**
 * 角色服务。
 */
public interface SysRoleService extends IService<SysRole> {

    java.util.List<String> listRoleCodesByUserId(Long userId);
}
