package com.lyk.coursearrange.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.resource.entity.ResStudent;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResourceAccountSyncService;
import com.lyk.coursearrange.system.rbac.entity.SysRole;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.entity.SysUserRole;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import com.lyk.coursearrange.system.rbac.service.SysUserRoleService;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资源账号同步服务实现。
 */
@Slf4j
@Service
public class ResourceAccountSyncServiceImpl implements ResourceAccountSyncService {

    private final SysUserService sysUserService;
    private final SysRoleService roleService;
    private final SysUserRoleService userRoleService;
    private final PasswordService passwordService;

    public ResourceAccountSyncServiceImpl(SysUserService sysUserService,
                                          SysRoleService roleService,
                                          SysUserRoleService userRoleService,
                                          PasswordService passwordService) {
        this.sysUserService = sysUserService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.passwordService = passwordService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser syncTeacherAccount(ResTeacher teacher) {
        SysUser user = getOrCreateBySource(SystemConstants.SourceType.TEACHER, teacher.getId());
        user.setUserCode("RT_" + teacher.getTeacherCode());
        user.setUsername(teacher.getTeacherCode());
        if (user.getId() == null) {
            user.setPasswordHash(passwordService.encode("123456"));
        }
        user.setRealName(teacher.getTeacherName());
        user.setDisplayName(teacher.getTeacherName());
        user.setMobile(teacher.getMobile());
        user.setEmail(teacher.getEmail());
        user.setUserType(SystemConstants.UserType.TEACHER);
        user.setSourceType(SystemConstants.SourceType.TEACHER);
        user.setSourceId(teacher.getId());
        user.setStatus(teacher.getStatus());
        sysUserService.saveOrUpdate(user);
        assignRole(user.getId(), SystemConstants.RoleCode.TEACHER);
        log.info("同步资源教师账号成功，teacherId={}, sysUserId={}", teacher.getId(), user.getId());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser syncStudentAccount(ResStudent student) {
        SysUser user = getOrCreateBySource(SystemConstants.SourceType.STUDENT, student.getId());
        user.setUserCode("RS_" + student.getStudentCode());
        user.setUsername(student.getStudentCode());
        if (user.getId() == null) {
            user.setPasswordHash(passwordService.encode("123456"));
        }
        user.setRealName(student.getStudentName());
        user.setDisplayName(student.getStudentName());
        user.setMobile(student.getMobile());
        user.setEmail(student.getEmail());
        user.setUserType(SystemConstants.UserType.STUDENT);
        user.setSourceType(SystemConstants.SourceType.STUDENT);
        user.setSourceId(student.getId());
        user.setStatus(student.getStatus());
        sysUserService.saveOrUpdate(user);
        assignRole(user.getId(), SystemConstants.RoleCode.STUDENT);
        log.info("同步资源学生账号成功，studentId={}, sysUserId={}", student.getId(), user.getId());
        return user;
    }

    @Override
    public void disableBySource(String sourceType, Long sourceId) {
        SysUser user = findBySource(sourceType, sourceId);
        if (user == null) {
            return;
        }
        user.setStatus(SystemConstants.Status.DISABLED);
        sysUserService.updateById(user);
        log.info("停用资源账号成功，sourceType={}, sourceId={}, sysUserId={}", sourceType, sourceId, user.getId());
    }

    private SysUser getOrCreateBySource(String sourceType, Long sourceId) {
        SysUser user = findBySource(sourceType, sourceId);
        if (user != null) {
            return user;
        }
        SysUser entity = new SysUser();
        entity.setDeleted(0);
        entity.setStatus(SystemConstants.Status.ENABLED);
        return entity;
    }

    private SysUser findBySource(String sourceType, Long sourceId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getSourceType, sourceType)
                .eq(SysUser::getSourceId, sourceId)
                .eq(SysUser::getDeleted, 0)
                .last("limit 1");
        return sysUserService.getOne(wrapper, false);
    }

    private void assignRole(Long userId, String roleCode) {
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDeleted, 0)
                .last("limit 1");
        SysRole role = roleService.getOne(roleWrapper, false);
        if (role == null) {
            return;
        }
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, role.getId())
                .last("limit 1");
        if (userRoleService.getOne(userRoleWrapper, false) != null) {
            return;
        }
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(role.getId());
        userRoleService.save(relation);
    }
}
