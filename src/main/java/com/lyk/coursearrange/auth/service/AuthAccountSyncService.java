package com.lyk.coursearrange.auth.service;

import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.system.rbac.entity.SysUser;

/**
 * 认证账号同步服务。
 * 场景说明：
 * 1. 当前系统处于旧业务表向 sys_user 迁移阶段。
 * 2. 这里统一负责把管理员、教师、学生资料同步成认证账号。
 */
public interface AuthAccountSyncService {

    SysUser syncAdminAccount(Admin admin);

    SysUser syncTeacherAccount(Teacher teacher);

    SysUser syncStudentAccount(Student student);

    void updatePasswordBySource(String sourceType, Long sourceId, String encodedPassword);
}
