package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.auth.service.AuthAccountSyncService;
import com.lyk.coursearrange.auth.service.AuthLoginService;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.service.AdminService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherService;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 统一登录服务实现。
 * 步骤说明：
 * 1. 优先使用 sys_user 作为认证源。
 * 2. 对于尚未迁移完成的旧账号，允许从旧业务表登录并自动同步到 sys_user。
 */
@Slf4j
@Service
public class AuthLoginServiceImpl implements AuthLoginService {

    private final SysUserService sysUserService;
    private final PasswordService passwordService;
    private final AuthAccountSyncService authAccountSyncService;
    private final AdminService adminService;
    private final TeacherService teacherService;
    private final StudentService studentService;

    public AuthLoginServiceImpl(SysUserService sysUserService,
                                PasswordService passwordService,
                                AuthAccountSyncService authAccountSyncService,
                                AdminService adminService,
                                TeacherService teacherService,
                                StudentService studentService) {
        this.sysUserService = sysUserService;
        this.passwordService = passwordService;
        this.authAccountSyncService = authAccountSyncService;
        this.adminService = adminService;
        this.teacherService = teacherService;
        this.studentService = studentService;
    }

    @Override
    public SysUser loginAdmin(String username, String password) {
        SysUser sysUser = tryLoginSysUser(username, password, SystemConstants.UserType.ADMIN);
        if (sysUser != null) {
            return sysUser;
        }
        Admin admin = adminService.adminLogin(username, password);
        if (admin == null) {
            return null;
        }
        SysUser synced = authAccountSyncService.syncAdminAccount(admin);
        touchLoginSuccess(synced);
        return synced;
    }

    @Override
    public SysUser loginTeacher(String username, String password) {
        SysUser sysUser = tryLoginSysUser(username, password, SystemConstants.UserType.TEACHER);
        if (sysUser != null) {
            return sysUser;
        }
        Teacher teacher = teacherService.teacherLogin(username, password);
        if (teacher == null) {
            return null;
        }
        SysUser synced = authAccountSyncService.syncTeacherAccount(teacher);
        touchLoginSuccess(synced);
        return synced;
    }

    @Override
    public SysUser loginStudent(String username, String password) {
        SysUser sysUser = tryLoginSysUser(username, password, SystemConstants.UserType.STUDENT);
        if (sysUser != null) {
            return sysUser;
        }
        Student student = studentService.studentLogin(username, password);
        if (student == null) {
            return null;
        }
        SysUser synced = authAccountSyncService.syncStudentAccount(student);
        touchLoginSuccess(synced);
        return synced;
    }

    private SysUser tryLoginSysUser(String username, String password, String userType) {
        SysUser sysUser = sysUserService.getByUsernameAndUserType(username, userType);
        if (sysUser == null) {
            return null;
        }
        if (passwordService.isEncoded(sysUser.getPasswordHash())) {
            if (!passwordService.matches(password, sysUser.getPasswordHash())) {
                return null;
            }
        } else if (!password.equals(sysUser.getPasswordHash())) {
            return null;
        } else {
            sysUser.setPasswordHash(passwordService.encode(password));
            sysUserService.updateById(sysUser);
        }
        touchLoginSuccess(sysUser);
        log.info("sys_user 登录成功，sysUserId={}, username={}, userType={}", sysUser.getId(), sysUser.getUsername(), sysUser.getUserType());
        return sysUser;
    }

    private void touchLoginSuccess(SysUser sysUser) {
        sysUser.setLastLoginAt(LocalDateTime.now());
        sysUserService.updateById(sysUser);
    }
}
