package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.auth.service.AuthAccountSyncService;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证账号同步服务实现。
 */
@Slf4j
@Service
public class AuthAccountSyncServiceImpl implements AuthAccountSyncService {

    private final SysUserService sysUserService;

    public AuthAccountSyncServiceImpl(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public SysUser syncAdminAccount(Admin admin) {
        SysUser sysUser = getOrCreateBySource(SystemConstants.SourceType.ADMIN, admin.getId().longValue());
        sysUser.setUserCode("A_" + admin.getAdminNo());
        sysUser.setUsername(admin.getAdminNo());
        sysUser.setPasswordHash(admin.getPassword());
        sysUser.setRealName(admin.getRealname());
        sysUser.setDisplayName(admin.getRealname());
        sysUser.setMobile(admin.getTelephone());
        sysUser.setEmail(admin.getEmail());
        sysUser.setUserType(SystemConstants.UserType.ADMIN);
        sysUser.setSourceType(SystemConstants.SourceType.ADMIN);
        sysUser.setSourceId(admin.getId().longValue());
        sysUser.setStatus(admin.getStatus() != null && admin.getStatus() == 0
                ? SystemConstants.Status.ENABLED
                : SystemConstants.Status.DISABLED);
        sysUserService.saveOrUpdate(sysUser);
        log.info("同步管理员认证账号成功，adminId={}, sysUserId={}", admin.getId(), sysUser.getId());
        return sysUser;
    }

    @Override
    public SysUser syncTeacherAccount(Teacher teacher) {
        SysUser sysUser = getOrCreateBySource(SystemConstants.SourceType.TEACHER, teacher.getId().longValue());
        sysUser.setUserCode("T_" + teacher.getTeacherNo());
        sysUser.setUsername(teacher.getTeacherNo());
        sysUser.setPasswordHash(teacher.getPassword());
        sysUser.setRealName(teacher.getRealname());
        sysUser.setDisplayName(teacher.getRealname());
        sysUser.setMobile(teacher.getTelephone());
        sysUser.setEmail(teacher.getEmail());
        sysUser.setUserType(SystemConstants.UserType.TEACHER);
        sysUser.setSourceType(SystemConstants.SourceType.TEACHER);
        sysUser.setSourceId(teacher.getId().longValue());
        sysUser.setStatus(teacher.getStatus() != null && teacher.getStatus() == 0
                ? SystemConstants.Status.ENABLED
                : SystemConstants.Status.DISABLED);
        sysUserService.saveOrUpdate(sysUser);
        log.info("同步教师认证账号成功，teacherId={}, sysUserId={}", teacher.getId(), sysUser.getId());
        return sysUser;
    }

    @Override
    public SysUser syncStudentAccount(Student student) {
        SysUser sysUser = getOrCreateBySource(SystemConstants.SourceType.STUDENT, student.getId().longValue());
        sysUser.setUserCode("S_" + student.getStudentNo());
        sysUser.setUsername(student.getStudentNo());
        sysUser.setPasswordHash(student.getPassword());
        sysUser.setRealName(student.getRealname());
        sysUser.setDisplayName(student.getRealname());
        sysUser.setMobile(student.getTelephone());
        sysUser.setEmail(student.getEmail());
        sysUser.setUserType(SystemConstants.UserType.STUDENT);
        sysUser.setSourceType(SystemConstants.SourceType.STUDENT);
        sysUser.setSourceId(student.getId().longValue());
        sysUser.setStatus(student.getStatus() != null && student.getStatus() == 0
                ? SystemConstants.Status.ENABLED
                : SystemConstants.Status.DISABLED);
        sysUserService.saveOrUpdate(sysUser);
        log.info("同步学生认证账号成功，studentId={}, sysUserId={}", student.getId(), sysUser.getId());
        return sysUser;
    }

    @Override
    public void updatePasswordBySource(String sourceType, Long sourceId, String encodedPassword) {
        SysUser sysUser = sysUserService.getBySource(sourceType, sourceId);
        if (sysUser == null) {
            return;
        }
        sysUser.setPasswordHash(encodedPassword);
        sysUserService.updateById(sysUser);
        log.info("同步认证账号密码成功，sourceType={}, sourceId={}, sysUserId={}", sourceType, sourceId, sysUser.getId());
    }

    private SysUser getOrCreateBySource(String sourceType, Long sourceId) {
        SysUser sysUser = sysUserService.getBySource(sourceType, sourceId);
        if (sysUser != null) {
            return sysUser;
        }
        SysUser entity = new SysUser();
        entity.setStatus(SystemConstants.Status.ENABLED);
        entity.setDeleted(0);
        return entity;
    }
}
