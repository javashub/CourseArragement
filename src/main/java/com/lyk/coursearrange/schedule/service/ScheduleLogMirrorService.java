package com.lyk.coursearrange.schedule.service;

import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;

/**
 * 排课日志镜像服务。
 */
public interface ScheduleLogMirrorService {

    void mirrorExecuteLog(ScheduleExecuteLog legacyLog);

    void mirrorAdjustLog(CoursePlanAdjustLog legacyLog);
}
