package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleAdjustLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunLog;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchScheduleAdjustLogService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunLogService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * 排课日志镜像服务实现。
 * 说明：
 * 1. 当前阶段仍然保留旧 tb_* 日志表。
 * 2. 同时把日志镜像到 sch_*，为后续彻底删除旧表做准备。
 * 3. 如果标准表尚未落库，仅记录告警，不中断现有业务。
 */
@Slf4j
@Service
public class ScheduleLogMirrorServiceImpl implements ScheduleLogMirrorService {

    private final SchScheduleRunLogService runLogService;
    private final SchScheduleAdjustLogService adjustLogService;
    private final SchTaskService taskService;
    private final SchScheduleResultService resultService;

    public ScheduleLogMirrorServiceImpl(SchScheduleRunLogService runLogService,
                                        SchScheduleAdjustLogService adjustLogService,
                                        SchTaskService taskService,
                                        SchScheduleResultService resultService) {
        this.runLogService = runLogService;
        this.adjustLogService = adjustLogService;
        this.taskService = taskService;
        this.resultService = resultService;
    }

    @Override
    public void replaceScheduleResults(String semester, List<SchedulingTaskInput> schedulingTasks, List<CoursePlan> coursePlans) {
        Map<String, Long> taskIdMap = new HashMap<>();
        for (SchedulingTaskInput schedulingTask : schedulingTasks) {
            SchTask schTask = getOrCreateTask(schedulingTask);
            taskIdMap.put(buildTaskKey(schedulingTask.getClassNo(), schedulingTask.getCourseNo(), schedulingTask.getTeacherNo()), schTask.getId());
        }

        LambdaQueryWrapper<SchScheduleResult> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(SchScheduleResult::getRemark, semester);
        resultService.remove(removeWrapper);

        for (CoursePlan coursePlan : coursePlans) {
            SchScheduleResult result = new SchScheduleResult();
            result.setRunLogId(null);
            result.setTaskId(taskIdMap.getOrDefault(
                    buildTaskKey(coursePlan.getClassNo(), coursePlan.getCourseNo(), coursePlan.getTeacherNo()), 0L));
            result.setSchoolYearId(0L);
            result.setTermId(0L);
            result.setStageId(0L);
            result.setCourseId(0L);
            result.setTeacherId(0L);
            result.setClassroomId(0L);
            result.setWeekdayNo(resolveWeekdayNo(coursePlan.getClassTime()));
            result.setPeriodNo(resolvePeriodNo(coursePlan.getClassTime()));
            result.setWeekRangeType("ALL");
            result.setIsLocked(0);
            result.setSourceType("AUTO");
            result.setConflictFlag(0);
            result.setStatus(1);
            result.setRemark(semester);
            resultService.save(result);
        }
    }

    @Override
    public void syncAdjustedPlan(CoursePlan legacyPlan, String beforeClassTime) {
        try {
            SchTask task = findTaskByKey(legacyPlan.getClassNo(), legacyPlan.getCourseNo(), legacyPlan.getTeacherNo(), legacyPlan.getSemester());
            if (task == null) {
                return;
            }
            Integer beforeWeekdayNo = resolveWeekdayNo(beforeClassTime);
            Integer beforePeriodNo = resolvePeriodNo(beforeClassTime);
            Integer afterWeekdayNo = resolveWeekdayNo(legacyPlan.getClassTime());
            Integer afterPeriodNo = resolvePeriodNo(legacyPlan.getClassTime());

            LambdaQueryWrapper<SchScheduleResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SchScheduleResult::getTaskId, task.getId())
                    .eq(SchScheduleResult::getRemark, legacyPlan.getSemester())
                    .eq(beforeWeekdayNo != null, SchScheduleResult::getWeekdayNo, beforeWeekdayNo)
                    .eq(beforePeriodNo != null, SchScheduleResult::getPeriodNo, beforePeriodNo)
                    .last("limit 1");
            SchScheduleResult result = resultService.getOne(wrapper, false);
            if (result == null) {
                result = new SchScheduleResult();
                result.setTaskId(task.getId());
                result.setSchoolYearId(0L);
                result.setTermId(0L);
                result.setStageId(0L);
                result.setCourseId(0L);
                result.setTeacherId(0L);
                result.setClassroomId(0L);
                result.setWeekRangeType("ALL");
                result.setIsLocked(0);
                result.setConflictFlag(0);
                result.setStatus(1);
                result.setRemark(legacyPlan.getSemester());
            }
            result.setWeekdayNo(afterWeekdayNo);
            result.setPeriodNo(afterPeriodNo);
            result.setSourceType("DRAG");
            resultService.saveOrUpdate(result);
        } catch (Exception exception) {
            log.warn("同步标准课表调课结果失败，planId={}", legacyPlan.getId(), exception);
        }
    }

    @Override
    public void mirrorExecuteLog(ScheduleExecuteLog legacyLog) {
        try {
            SchScheduleRunLog logEntity = new SchScheduleRunLog();
            logEntity.setRunCode("RUN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
            logEntity.setTermId(0L);
            logEntity.setRunType("MANUAL");
            logEntity.setAlgorithmType("GENETIC");
            logEntity.setTaskTotal(legacyLog.getTaskCount());
            logEntity.setTaskSuccess(legacyLog.getStatus() != null && legacyLog.getStatus() == 1 ? legacyLog.getGeneratedPlanCount() : 0);
            logEntity.setTaskFailed(legacyLog.getStatus() != null && legacyLog.getStatus() == 1 ? 0 : legacyLog.getTaskCount());
            logEntity.setRunStatus(legacyLog.getStatus() != null && legacyLog.getStatus() == 1 ? "SUCCESS" : "FAILED");
            logEntity.setFailureReason(legacyLog.getStatus() != null && legacyLog.getStatus() == 1 ? null : legacyLog.getMessage());
            logEntity.setStartedAt(resolveStartedAt(legacyLog));
            logEntity.setFinishedAt(legacyLog.getCreateTime());
            logEntity.setOperatorUserId(legacyLog.getOperatorUserId());
            logEntity.setRemark(legacyLog.getSemester());
            runLogService.save(logEntity);
        } catch (Exception exception) {
            log.warn("镜像标准排课执行日志失败，将继续保留旧日志链路，legacyId={}", legacyLog.getId(), exception);
        }
    }

    @Override
    public void mirrorAdjustLog(CoursePlanAdjustLog legacyLog) {
        try {
            SchScheduleAdjustLog logEntity = new SchScheduleAdjustLog();
            logEntity.setAdjustCode("ADJ_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
            logEntity.setTermId(0L);
            logEntity.setSourceResultId(legacyLog.getCoursePlanId() == null ? 0L : legacyLog.getCoursePlanId().longValue());
            logEntity.setAdjustType("MOVE");
            logEntity.setBeforeWeekdayNo(resolveWeekdayNo(legacyLog.getBeforeClassTime()));
            logEntity.setBeforePeriodNo(resolvePeriodNo(legacyLog.getBeforeClassTime()));
            logEntity.setAfterWeekdayNo(resolveWeekdayNo(legacyLog.getAfterClassTime()));
            logEntity.setAfterPeriodNo(resolvePeriodNo(legacyLog.getAfterClassTime()));
            logEntity.setAdjustReason(legacyLog.getRemark());
            logEntity.setOperatorUserId(legacyLog.getOperatorUserId());
            logEntity.setStatus(1);
            logEntity.setRemark(legacyLog.getSemester());
            adjustLogService.save(logEntity);
        } catch (Exception exception) {
            log.warn("镜像标准调课日志失败，将继续保留旧日志链路，legacyId={}", legacyLog.getId(), exception);
        }
    }

    private LocalDateTime resolveStartedAt(ScheduleExecuteLog legacyLog) {
        if (legacyLog.getCreateTime() == null || legacyLog.getDurationMs() == null) {
            return legacyLog.getCreateTime();
        }
        return legacyLog.getCreateTime().minusNanos(legacyLog.getDurationMs() * 1_000_000);
    }

    private Integer resolveWeekdayNo(String classTime) {
        return ScheduleTaskMetaUtils.resolveWeekdayNo(classTime);
    }

    private Integer resolvePeriodNo(String classTime) {
        return ScheduleTaskMetaUtils.resolvePeriodNo(classTime);
    }

    private SchTask getOrCreateTask(SchedulingTaskInput schedulingTask) {
        SchTask existing = findTaskByCode(buildTaskCode(schedulingTask));
        if (existing != null) {
            return existing;
        }
        SchTask task = new SchTask();
        task.setTaskCode(buildTaskCode(schedulingTask));
        return task;
    }

    private SchTask findTaskByKey(String classNo, String courseNo, String teacherNo, String semester) {
        String raw = buildTaskKey(classNo, courseNo, teacherNo) + "_" + safe(semester);
        String sanitized = raw.replaceAll("[^A-Za-z0-9_]", "_");
        String taskCode = sanitized.length() > 32 ? sanitized.substring(0, 32) : sanitized;
        return findTaskByCode(taskCode);
    }

    private SchTask findTaskByCode(String taskCode) {
        LambdaQueryWrapper<SchTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchTask::getTaskCode, taskCode)
                .eq(SchTask::getDeleted, 0)
                .last("limit 1");
        return taskService.getOne(wrapper, false);
    }

    private String buildTaskCode(SchedulingTaskInput schedulingTask) {
        return ScheduleTaskMetaUtils.buildTaskCode(
                schedulingTask.getSemester(),
                schedulingTask.getClassNo(),
                schedulingTask.getCourseNo(),
                schedulingTask.getTeacherNo()
        );
    }

    private String buildTaskKey(String classNo, String courseNo, String teacherNo) {
        return safe(classNo) + "_" + safe(courseNo) + "_" + safe(teacherNo);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
