package com.lyk.coursearrange.schedule.service;

import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;

import java.util.List;

/**
 * 排课日志镜像服务。
 */
public interface ScheduleLogMirrorService {

    void replaceScheduleResults(String semester, List<SchedulingTaskInput> schedulingTasks, List<CoursePlan> coursePlans);

    void syncAdjustedPlan(CoursePlan legacyPlan, String beforeClassTime);

    void mirrorExecuteLog(ScheduleExecuteLog legacyLog);

    void mirrorAdjustLog(CoursePlanAdjustLog legacyLog);
}
