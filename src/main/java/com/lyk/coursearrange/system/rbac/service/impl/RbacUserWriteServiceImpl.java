package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.request.SysUserSaveRequest;
import com.lyk.coursearrange.system.rbac.service.RbacUserWriteService;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * RBAC 用户写服务实现。
 * 步骤说明：
 * 1. 校验用户编码、登录账号唯一性。
 * 2. 新增用户时密码必填，统一走 BCrypt 加密。
 * 3. 编辑用户时密码非必填，填写后才更新密码。
 */
@Slf4j
@Service
public class RbacUserWriteServiceImpl implements RbacUserWriteService {

    private final SysUserService sysUserService;
    private final PasswordService passwordService;

    public RbacUserWriteServiceImpl(SysUserService sysUserService, PasswordService passwordService) {
        this.sysUserService = sysUserService;
        this.passwordService = passwordService;
    }

    @Override
    public SysUser saveUser(SysUserSaveRequest request) {
        validateUserCodeUnique(request.getId(), request.getUserCode());
        validateUsernameUnique(request.getId(), request.getUsername(), request.getUserType());
        SysUser entity = request.getId() == null ? new SysUser() : getUserOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity, "password");
        if (StringUtils.isBlank(entity.getDisplayName())) {
            entity.setDisplayName(entity.getRealName());
        }
        if (request.getId() == null && StringUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新增用户时密码不能为空");
        }
        if (StringUtils.isNotBlank(request.getPassword())) {
            entity.setPasswordHash(passwordService.encode(request.getPassword()));
        }
        sysUserService.saveOrUpdate(entity);
        log.info("保存系统用户成功，userId={}, username={}", entity.getId(), entity.getUsername());
        return entity;
    }

    @Override
    public void changeUserStatus(Long userId, Integer status) {
        SysUser entity = getUserOrThrow(userId);
        entity.setStatus(status);
        sysUserService.updateById(entity);
        log.info("修改系统用户状态成功，userId={}, status={}", userId, status);
    }

    private void validateUserCodeUnique(Long userId, String userCode) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserCode, userCode)
                .eq(SysUser::getDeleted, 0)
                .ne(userId != null, SysUser::getId, userId);
        if (sysUserService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户编码已存在");
        }
    }

    private void validateUsernameUnique(Long userId, String username, String userType) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username)
                .eq(SysUser::getUserType, userType)
                .eq(SysUser::getDeleted, 0)
                .ne(userId != null, SysUser::getId, userId);
        if (sysUserService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "同类型登录账号已存在");
        }
    }

    private SysUser getUserOrThrow(Long userId) {
        SysUser entity = sysUserService.getById(userId);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统用户不存在");
        }
        return entity;
    }
}
