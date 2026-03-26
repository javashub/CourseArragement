package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.SchedulingFailureCode;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 统一汇总失败原因。
 */
public class SchedulingFailureReporter {

    public List<SchedulingConstraintSummary> summarize(List<UnscheduledTaskDetail> unscheduledTasks) {
        if (unscheduledTasks == null || unscheduledTasks.isEmpty()) {
            return List.of();
        }
        return unscheduledTasks.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(UnscheduledTaskDetail::getReasonCode, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new SchedulingConstraintSummary(
                        entry.getKey(),
                        resolveReasonLabel(entry.getKey()),
                        entry.getValue().intValue()
                ))
                .sorted(Comparator.comparing(SchedulingConstraintSummary::getReasonCode))
                .toList();
    }

    public UnscheduledTaskDetail buildFailureDetail(com.lyk.coursearrange.schedule.engine.model.SchedulingTask task,
                                                    Map<SchedulingFailureCode, Integer> failureCounter) {
        SchedulingFailureCode reasonCode = resolvePrimaryReason(failureCounter);
        return UnscheduledTaskDetail.builder()
                .taskId(task.getTaskId())
                .taskCode(task.getTaskCode())
                .classNo(task.getClassNo())
                .courseNo(task.getCourseNo())
                .teacherNo(task.getTeacherNo())
                .reasonCode(reasonCode.name())
                .reasonMessage(buildReasonMessage(task, reasonCode))
                .build();
    }

    private SchedulingFailureCode resolvePrimaryReason(Map<SchedulingFailureCode, Integer> failureCounter) {
        if (failureCounter == null || failureCounter.isEmpty()) {
            return SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT;
        }
        List<SchedulingFailureCode> priority = List.of(
                SchedulingFailureCode.FIXED_TIME_UNSATISFIED,
                SchedulingFailureCode.CONTINUOUS_SLOT_UNAVAILABLE,
                SchedulingFailureCode.SPECIAL_ROOM_UNAVAILABLE,
                SchedulingFailureCode.TEACHER_FORBIDDEN_TIME,
                SchedulingFailureCode.CLASS_FORBIDDEN_TIME,
                SchedulingFailureCode.TEACHER_DAY_HOUR_LIMIT,
                SchedulingFailureCode.TEACHER_CONFLICT,
                SchedulingFailureCode.CLASS_CONFLICT,
                SchedulingFailureCode.CLASSROOM_CONFLICT,
                SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT
        );
        return priority.stream()
                .filter(failureCounter::containsKey)
                .findFirst()
                .orElse(SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
    }

    private String buildReasonMessage(com.lyk.coursearrange.schedule.engine.model.SchedulingTask task,
                                      SchedulingFailureCode reasonCode) {
        return switch (reasonCode) {
            case FIXED_TIME_UNSATISFIED -> String.format("任务 %s 的固定时间无法满足当前约束", task.getTaskCode());
            case CONTINUOUS_SLOT_UNAVAILABLE -> String.format("任务 %s 找不到满足连堂要求的连续时间片", task.getTaskCode());
            case SPECIAL_ROOM_UNAVAILABLE -> String.format("任务 %s 缺少满足要求的特殊教室", task.getTaskCode());
            case TEACHER_FORBIDDEN_TIME -> String.format("教师 %s 的禁排时间覆盖了当前可用时间片", task.getTeacherNo());
            case CLASS_FORBIDDEN_TIME -> String.format("班级 %s 的禁排时间覆盖了当前可用时间片", task.getClassNo());
            case TEACHER_DAY_HOUR_LIMIT -> String.format("教师 %s 已达到当日课时上限", task.getTeacherNo());
            case TEACHER_CONFLICT -> String.format("教师 %s 在候选时间片存在冲突", task.getTeacherNo());
            case CLASS_CONFLICT -> String.format("班级 %s 在候选时间片存在冲突", task.getClassNo());
            case CLASSROOM_CONFLICT -> String.format("任务 %s 的候选教室在时间片内已被占用", task.getTaskCode());
            case NO_AVAILABLE_TIME_SLOT -> String.format("任务 %s 没有可用时间片", task.getTaskCode());
        };
    }

    private String resolveReasonLabel(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT.getDefaultMessage();
        }
        return SchedulingFailureCode.valueOf(reasonCode).getDefaultMessage();
    }
}
