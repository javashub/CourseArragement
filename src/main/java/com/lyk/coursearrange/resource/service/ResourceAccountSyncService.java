package com.lyk.coursearrange.resource.service;

import com.lyk.coursearrange.resource.entity.ResStudent;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.system.rbac.entity.SysUser;

/**
 * 资源账号同步服务。
 */
public interface ResourceAccountSyncService {

    SysUser syncTeacherAccount(ResTeacher teacher);

    SysUser syncStudentAccount(ResStudent student);

    void disableBySource(String sourceType, Long sourceId);
}
