package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.entity.ClassTask;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
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
    public void mirrorTask(ClassTask legacyTask) {
        try {
            SchTask task = getOrCreateTask(legacyTask);
            task.setSchoolYearId(0L);
            task.setTermId(0L);
            task.setStageId(0L);
            task.setCourseId(0L);
            task.setTeacherId(0L);
            task.setStudentCount(legacyTask.getStudentNum());
            task.setWeekHours(legacyTask.getWeeksNumber());
            task.setTotalHours((legacyTask.getWeeksNumber() == null || legacyTask.getWeeksSum() == null)
                    ? 0
                    : legacyTask.getWeeksNumber() * legacyTask.getWeeksSum());
            task.setNeedContinuous(0);
            task.setContinuousSize(1);
            task.setNeedFixedRoom(0);
            task.setNeedFixedTime("1".equals(legacyTask.getIsFix()) ? 1 : 0);
            task.setFixedWeekdayNo(resolveWeekdayNo(legacyTask.getClassTime()));
            task.setFixedPeriodNo(resolvePeriodNo(legacyTask.getClassTime()));
            task.setPriorityLevel(5);
            task.setAllowConflict(0);
            task.setTaskStatus("PENDING");
            task.setSourceType("LEGACY_SYNC");
            task.setStatus(1);
            task.setRemark(buildTaskRemark(legacyTask));
            taskService.saveOrUpdate(task);
        } catch (Exception exception) {
            log.warn("镜像标准排课任务失败，将继续保留旧任务链路，legacyId={}", legacyTask.getId(), exception);
        }
    }

    @Override
    public void removeTaskMirror(ClassTask legacyTask) {
        try {
            SchTask task = findTask(legacyTask);
            if (task == null) {
                return;
            }
            taskService.removeById(task.getId());
        } catch (Exception exception) {
            log.warn("移除标准排课任务镜像失败，legacyId={}", legacyTask.getId(), exception);
        }
    }

    @Override
    public void mirrorScheduleResults(String semester, List<ClassTask> legacyTasks, List<CoursePlan> legacyPlans) {
        try {
            Map<String, Long> taskIdMap = new HashMap<>();
            for (ClassTask legacyTask : legacyTasks) {
                SchTask schTask = getOrCreateTask(legacyTask);
                taskIdMap.put(buildTaskKey(legacyTask.getClassNo(), legacyTask.getCourseNo(), legacyTask.getTeacherNo()), schTask.getId());
            }

            LambdaQueryWrapper<SchScheduleResult> removeWrapper = new LambdaQueryWrapper<>();
            removeWrapper.eq(SchScheduleResult::getRemark, semester);
            resultService.remove(removeWrapper);

            for (CoursePlan legacyPlan : legacyPlans) {
                SchScheduleResult result = new SchScheduleResult();
                result.setRunLogId(null);
                result.setTaskId(taskIdMap.getOrDefault(
                        buildTaskKey(legacyPlan.getClassNo(), legacyPlan.getCourseNo(), legacyPlan.getTeacherNo()), 0L));
                result.setSchoolYearId(0L);
                result.setTermId(0L);
                result.setStageId(0L);
                result.setCourseId(0L);
                result.setTeacherId(0L);
                result.setClassroomId(0L);
                result.setWeekdayNo(resolveWeekdayNo(legacyPlan.getClassTime()));
                result.setPeriodNo(resolvePeriodNo(legacyPlan.getClassTime()));
                result.setWeekRangeType("ALL");
                result.setIsLocked(0);
                result.setSourceType("AUTO");
                result.setConflictFlag(0);
                result.setStatus(1);
                result.setRemark(semester);
                resultService.save(result);
            }
        } catch (Exception exception) {
            log.warn("镜像标准课表结果失败，将继续保留旧课表链路，semester={}", semester, exception);
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
        Integer slot = parseClassTime(classTime);
        if (slot == null || slot < 1) {
            return null;
        }
        return ((slot - 1) / 5) + 1;
    }

    private Integer resolvePeriodNo(String classTime) {
        Integer slot = parseClassTime(classTime);
        if (slot == null || slot < 1) {
            return null;
        }
        return ((slot - 1) % 5) + 1;
    }

    private Integer parseClassTime(String classTime) {
        try {
            return classTime == null ? null : Integer.parseInt(classTime);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private SchTask getOrCreateTask(ClassTask legacyTask) {
        SchTask existing = findTask(legacyTask);
        if (existing != null) {
            return existing;
        }
        SchTask task = new SchTask();
        task.setTaskCode(buildTaskCode(legacyTask));
        return task;
    }

    private SchTask findTask(ClassTask legacyTask) {
        return findTaskByCode(buildTaskCode(legacyTask));
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

    private String buildTaskCode(ClassTask legacyTask) {
        String raw = buildTaskKey(legacyTask.getClassNo(), legacyTask.getCourseNo(), legacyTask.getTeacherNo())
                + "_" + safe(legacyTask.getSemester());
        String sanitized = raw.replaceAll("[^A-Za-z0-9_]", "_");
        if (sanitized.length() > 32) {
            return sanitized.substring(0, 32);
        }
        return sanitized;
    }

    private String buildTaskRemark(ClassTask legacyTask) {
        return "semester=" + safe(legacyTask.getSemester())
                + ",legacyId=" + (legacyTask.getId() == null ? "" : legacyTask.getId())
                + ",classNo=" + safe(legacyTask.getClassNo())
                + ",courseNo=" + safe(legacyTask.getCourseNo())
                + ",teacherNo=" + safe(legacyTask.getTeacherNo())
                + ",gradeNo=" + safe(legacyTask.getGradeNo())
                + ",courseName=" + safe(legacyTask.getCourseName())
                + ",courseAttr=" + safe(legacyTask.getCourseAttr())
                + ",teacherName=" + safe(legacyTask.getRealname());
    }

    private String buildTaskKey(String classNo, String courseNo, String teacherNo) {
        return safe(classNo) + "_" + safe(courseNo) + "_" + safe(teacherNo);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
