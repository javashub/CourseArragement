package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 行政班排课前置校验器。
 * 只处理“无论如何都排不成”的固定约束冲突，避免脏数据进入排课引擎后才报部分失败。
 */
@Component
public class AdministrativeClassSchedulingPreCheck {

    public List<PreCheckIssue> validate(List<SchedulingTask> tasks, List<String> availableSlotCodes) {
        List<PreCheckIssue> issues = new ArrayList<>();
        if (tasks == null || tasks.isEmpty()) {
            return issues;
        }

        Set<String> availableSlots = availableSlotCodes == null
                ? Set.of()
                : new LinkedHashSet<>(availableSlotCodes);
        Map<String, SchedulingTask> teacherSlotOwners = new LinkedHashMap<>();
        Map<String, SchedulingTask> classSlotOwners = new LinkedHashMap<>();
        Map<String, SchedulingTask> roomSlotOwners = new LinkedHashMap<>();

        for (SchedulingTask task : tasks) {
            if (task == null || !task.isFixedTime()) {
                continue;
            }
            if (task.getFixedTimeSlots() == null || task.getFixedTimeSlots().isEmpty()) {
                issues.add(new PreCheckIssue(task.getTaskId(), task.getTaskCode(),
                        "FIXED_TIME_UNSATISFIED", "固定时间不在当前可用时间片内"));
                continue;
            }
            for (String slotCode : task.getFixedTimeSlots()) {
                if (slotCode == null || slotCode.isBlank()) {
                    issues.add(new PreCheckIssue(task.getTaskId(), task.getTaskCode(),
                            "FIXED_TIME_UNSATISFIED", "固定时间不在当前可用时间片内"));
                    continue;
                }
                if (!availableSlots.contains(slotCode)) {
                    issues.add(new PreCheckIssue(task.getTaskId(), task.getTaskCode(),
                            "FIXED_TIME_UNSATISFIED", "固定时间不在当前可用时间片内"));
                    continue;
                }
                collectConflict(teacherSlotOwners, buildConflictKey(task.getTeacherNo(), slotCode),
                        task, "TEACHER_CONFLICT", "教师固定时间冲突", issues);
                collectConflict(classSlotOwners, buildConflictKey(task.getClassNo(), slotCode),
                        task, "CLASS_CONFLICT", "班级固定时间冲突", issues);
                if (task.getFixedRoomId() != null) {
                    collectConflict(roomSlotOwners, buildConflictKey(String.valueOf(task.getFixedRoomId()), slotCode),
                            task, "CLASSROOM_CONFLICT", "教室固定时间冲突", issues);
                }
            }
        }
        return issues;
    }

    private String buildConflictKey(String ownerCode, String slotCode) {
        if (ownerCode == null || ownerCode.isBlank() || slotCode == null || slotCode.isBlank()) {
            return null;
        }
        return ownerCode + "::" + slotCode;
    }

    private void collectConflict(Map<String, SchedulingTask> existedAssignments,
                                 String key,
                                 SchedulingTask currentTask,
                                 String reasonCode,
                                 String reasonMessage,
                                 List<PreCheckIssue> issues) {
        if (key == null || key.isBlank()) {
            return;
        }
        SchedulingTask existedTask = existedAssignments.putIfAbsent(key, currentTask);
        if (existedTask != null && !Objects.equals(existedTask.getTaskId(), currentTask.getTaskId())) {
            issues.add(new PreCheckIssue(currentTask.getTaskId(), currentTask.getTaskCode(), reasonCode, reasonMessage));
        }
    }

    public record PreCheckIssue(Long taskId, String taskCode, String reasonCode, String reasonMessage) {
    }
}
