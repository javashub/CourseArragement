package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.ScheduleResultWriteService;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 标准课表结果写服务实现。
 */
@Service
public class ScheduleResultWriteServiceImpl implements ScheduleResultWriteService {

    private final SchScheduleResultService resultService;
    private final ResClassroomService resClassroomService;

    public ScheduleResultWriteServiceImpl(SchScheduleResultService resultService,
                                          ResClassroomService resClassroomService) {
        this.resultService = resultService;
        this.resClassroomService = resClassroomService;
    }

    @Override
    public void replaceScheduleResults(String semester,
                                       Long runLogId,
                                       List<SchedulingTaskInput> schedulingTasks,
                                       List<SchedulingAssignment> assignments) {
        Map<String, Long> classroomIdMap = buildClassroomIdMap(assignments);

        resultService.remove(new LambdaQueryWrapper<SchScheduleResult>()
                .eq(SchScheduleResult::getRemark, semester));

        for (SchedulingAssignment assignment : assignments) {
            SchScheduleResult result = new SchScheduleResult();
            result.setRunLogId(runLogId);
            result.setTaskId(assignment.getTaskId());
            result.setSchoolYearId(0L);
            result.setTermId(0L);
            result.setStageId(0L);
            result.setCourseId(0L);
            result.setTeacherId(0L);
            result.setClassroomId(classroomIdMap.getOrDefault(assignment.getClassroomCode(), 0L));
            result.setWeekdayNo(assignment.getWeekdayNo());
            result.setPeriodNo(assignment.getPeriodNo());
            result.setWeekRangeType("ALL");
            result.setIsLocked(0);
            result.setSourceType("AUTO");
            result.setConflictFlag(0);
            result.setStatus(1);
            result.setRemark(semester);
            resultService.save(result);
        }
    }

    private Map<String, Long> buildClassroomIdMap(List<SchedulingAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return Map.of();
        }
        List<String> classroomCodes = assignments.stream()
                .map(SchedulingAssignment::getClassroomCode)
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
}
