package com.lyk.coursearrange.schedule.service;

import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;

import java.util.List;

/**
 * 排课日志镜像服务。
 */
public interface ScheduleLogMirrorService {

    void mirrorTask(ClassTask legacyTask);

    void removeTaskMirror(ClassTask legacyTask);

    void mirrorScheduleResults(String semester, List<ClassTask> legacyTasks, List<CoursePlan> legacyPlans);

    void mirrorExecuteLog(ScheduleExecuteLog legacyLog);

    void mirrorAdjustLog(CoursePlanAdjustLog legacyLog);
}
