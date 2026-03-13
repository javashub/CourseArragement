package com.lyk.coursearrange.schedule.service.impl;

import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleAdjustLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunLog;
import com.lyk.coursearrange.schedule.service.SchScheduleAdjustLogService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunLogService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public ScheduleLogMirrorServiceImpl(SchScheduleRunLogService runLogService,
                                        SchScheduleAdjustLogService adjustLogService) {
        this.runLogService = runLogService;
        this.adjustLogService = adjustLogService;
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
}
