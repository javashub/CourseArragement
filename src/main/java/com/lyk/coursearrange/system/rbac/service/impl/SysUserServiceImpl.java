package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.mapper.SysUserMapper;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 系统用户服务实现。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser getBySource(String sourceType, Long sourceId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getSourceType, sourceType)
                .eq(SysUser::getSourceId, sourceId)
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getStatus, SystemConstants.Status.ENABLED)
                .last("limit 1");
        return getOne(wrapper, false);
    }

    @Override
    public SysUser getByUsernameAndUserType(String username, String userType) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username)
                .eq(SysUser::getUserType, userType)
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getStatus, SystemConstants.Status.ENABLED)
                .last("limit 1");
        return getOne(wrapper, false);
    }
}
