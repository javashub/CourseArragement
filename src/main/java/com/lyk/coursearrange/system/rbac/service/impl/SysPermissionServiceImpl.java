package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.rbac.entity.SysPermission;
import com.lyk.coursearrange.system.rbac.mapper.SysPermissionMapper;
import com.lyk.coursearrange.system.rbac.service.SysPermissionService;
import org.springframework.stereotype.Service;

/**
 * 权限服务实现。
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public java.util.List<String> listPermissionCodesByUserId(Long userId) {
        return baseMapper.selectPermissionCodesByUserId(userId);
    }
}
