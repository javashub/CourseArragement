package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.rbac.entity.SysRole;
import com.lyk.coursearrange.system.rbac.mapper.SysRoleMapper;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import org.springframework.stereotype.Service;

/**
 * 角色服务实现。
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public java.util.List<String> listRoleCodesByUserId(Long userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }
}
