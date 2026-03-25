package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.service.ScheduleResultWriteService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标准课表结果写服务实现。
 */
@Service
public class ScheduleResultWriteServiceImpl implements ScheduleResultWriteService {

    private final SchTaskService taskService;
    private final SchScheduleResultService resultService;
    private final ResClassroomService resClassroomService;

    public ScheduleResultWriteServiceImpl(SchTaskService taskService,
                                          SchScheduleResultService resultService,
                                          ResClassroomService resClassroomService) {
        this.taskService = taskService;
        this.resultService = resultService;
        this.resClassroomService = resClassroomService;
    }

    @Override
    public void replaceScheduleResults(String semester, List<SchedulingTaskInput> schedulingTasks, List<CoursePlan> coursePlans) {
        Map<String, Long> taskIdMap = new HashMap<>();
        for (SchedulingTaskInput schedulingTask : schedulingTasks) {
            SchTask schTask = findTaskByCode(buildTaskCode(schedulingTask));
            if (schTask != null) {
                taskIdMap.put(buildTaskKey(schedulingTask.getClassNo(), schedulingTask.getCourseNo(), schedulingTask.getTeacherNo()), schTask.getId());
            }
        }
        Map<String, Long> classroomIdMap = buildClassroomIdMap(coursePlans);

        resultService.remove(new LambdaQueryWrapper<SchScheduleResult>()
                .eq(SchScheduleResult::getRemark, semester));

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
            result.setClassroomId(classroomIdMap.getOrDefault(coursePlan.getClassroomNo(), 0L));
            result.setWeekdayNo(ScheduleTaskMetaUtils.resolveWeekdayNo(coursePlan.getClassTime()));
            result.setPeriodNo(ScheduleTaskMetaUtils.resolvePeriodNo(coursePlan.getClassTime()));
            result.setWeekRangeType("ALL");
            result.setIsLocked(0);
            result.setSourceType("AUTO");
            result.setConflictFlag(0);
            result.setStatus(1);
            result.setRemark(semester);
            resultService.save(result);
        }
    }

    private Map<String, Long> buildClassroomIdMap(List<CoursePlan> coursePlans) {
        if (coursePlans == null || coursePlans.isEmpty()) {
            return Map.of();
        }
        List<String> classroomCodes = coursePlans.stream()
                .map(CoursePlan::getClassroomNo)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
        if (classroomCodes.isEmpty()) {
            return Map.of();
        }
        return resClassroomService.list(new LambdaQueryWrapper<ResClassroom>()
                        .eq(ResClassroom::getDeleted, 0)
                        .in(ResClassroom::getClassroomCode, classroomCodes))
                .stream()
                .collect(java.util.stream.Collectors.toMap(ResClassroom::getClassroomCode, ResClassroom::getId, (left, right) -> left));
    }

    private SchTask findTaskByCode(String taskCode) {
        return taskService.getOne(new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getTaskCode, taskCode)
                .eq(SchTask::getDeleted, 0)
                .last("limit 1"), false);
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
        return (classNo == null ? "" : classNo) + "_" + (courseNo == null ? "" : courseNo) + "_" + (teacherNo == null ? "" : teacherNo);
    }
}
