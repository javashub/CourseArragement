package com.lyk.coursearrange.system.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.system.rbac.entity.SysUser;

/**
 * 系统用户服务。
 */
public interface SysUserService extends IService<SysUser> {

    SysUser getBySource(String sourceType, Long sourceId);

    SysUser getByUsernameAndUserType(String username, String userType);
}
