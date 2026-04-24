package com.lyk.coursearrange.schedule.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunLog;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.service.impl.ClassTaskServiceImpl;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrativeClassSchedulingPreCheckTest {

    @Mock
    private com.lyk.coursearrange.schedule.service.SchTaskService schTaskService;
    @Mock
    private ScheduleConfigFacadeService scheduleConfigFacadeService;
    @Mock
    private com.lyk.coursearrange.schedule.service.SchScheduleRunLogService schScheduleRunLogService;
    @Mock
    private SchedulingEngine schedulingEngine;

    @Test
    void validate_shouldDetectFixedConstraintIssues() {
        AdministrativeClassSchedulingPreCheck preCheck = new AdministrativeClassSchedulingPreCheck();

        List<AdministrativeClassSchedulingPreCheck.PreCheckIssue> issues = preCheck.validate(
                List.of(
                        fixedTask(1L, "TK-INVALID", "AC-1", "T-01", 101L, "0199"),
                        fixedTask(2L, "TK-TEACHER-A", "AC-2", "T-02", null, "0101"),
                        fixedTask(3L, "TK-TEACHER-B", "AC-3", "T-02", null, "0101"),
                        fixedTask(4L, "TK-CLASS-A", "AC-4", "T-03", null, "0102"),
                        fixedTask(5L, "TK-CLASS-B", "AC-4", "T-04", null, "0102"),
                        fixedTask(6L, "TK-ROOM-A", "AC-5", "T-05", 201L, "0201"),
                        fixedTask(7L, "TK-ROOM-B", "AC-6", "T-06", 201L, "0201")
                ),
                List.of("0101", "0102", "0201")
        );

        assertEquals(4, issues.size());
        assertEquals("FIXED_TIME_UNSATISFIED", issues.get(0).reasonCode());
        assertEquals("TEACHER_CONFLICT", issues.get(1).reasonCode());
        assertEquals("CLASS_CONFLICT", issues.get(2).reasonCode());
        assertEquals("CLASSROOM_CONFLICT", issues.get(3).reasonCode());
    }

    @Test
    void validate_shouldReportUnsatisfiedWhenFixedTimeSlotIsBlank() {
        AdministrativeClassSchedulingPreCheck preCheck = new AdministrativeClassSchedulingPreCheck();

        SchedulingTask blankFixedSlotTask = SchedulingTask.builder()
                .taskId(8L)
                .taskCode("TK-BLANK-SLOT")
                .classNo("AC-7")
                .teacherNo("T-07")
                .fixedTime(true)
                .fixedTimeSlots(List.of("", "   "))
                .build();

        List<AdministrativeClassSchedulingPreCheck.PreCheckIssue> issues = preCheck.validate(
                List.of(blankFixedSlotTask),
                List.of("0101", "0102")
        );

        assertEquals(2, issues.size());
        assertEquals("FIXED_TIME_UNSATISFIED", issues.get(0).reasonCode());
        assertEquals("FIXED_TIME_UNSATISFIED", issues.get(1).reasonCode());
    }

    @Test
    void validate_shouldReportUnsatisfiedWhenFixedTimeSlotsIsNullOrEmpty() {
        AdministrativeClassSchedulingPreCheck preCheck = new AdministrativeClassSchedulingPreCheck();

        SchedulingTask nullFixedSlotsTask = SchedulingTask.builder()
                .taskId(9L)
                .taskCode("TK-NULL-SLOTS")
                .classNo("AC-8")
                .teacherNo("T-08")
                .fixedTime(true)
                .fixedTimeSlots(null)
                .build();
        SchedulingTask emptyFixedSlotsTask = SchedulingTask.builder()
                .taskId(10L)
                .taskCode("TK-EMPTY-SLOTS")
                .classNo("AC-9")
                .teacherNo("T-09")
                .fixedTime(true)
                .fixedTimeSlots(List.of())
                .build();

        List<AdministrativeClassSchedulingPreCheck.PreCheckIssue> issues = preCheck.validate(
                List.of(nullFixedSlotsTask, emptyFixedSlotsTask),
                List.of("0101", "0102")
        );

        assertEquals(2, issues.size());
        assertEquals("FIXED_TIME_UNSATISFIED", issues.get(0).reasonCode());
        assertEquals(9L, issues.get(0).taskId());
        assertEquals("FIXED_TIME_UNSATISFIED", issues.get(1).reasonCode());
        assertEquals(10L, issues.get(1).taskId());
    }

    @Test
    void classScheduling_shouldFailBeforeSchedulingEngineWhenPreCheckFindsTeacherConflict() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "scheduleConfigFacadeService", scheduleConfigFacadeService);
        ReflectionTestUtils.setField(service, "schScheduleRunLogService", schScheduleRunLogService);
        ReflectionTestUtils.setField(service, "schedulingEngine", schedulingEngine);
        ReflectionTestUtils.setField(service, "administrativeClassSchedulingPreCheck", new AdministrativeClassSchedulingPreCheck());

        SchTask first = buildFixedStandardTask(11L, "TK-11", "AC-1", "COURSE-1", "T-01", 1, 1);
        SchTask second = buildFixedStandardTask(12L, "TK-12", "AC-2", "COURSE-2", "T-01", 1, 1);

        when(schTaskService.list(org.mockito.ArgumentMatchers.<LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(first, second));
        when(scheduleConfigFacadeService.getScheduleConfig(any())).thenReturn(buildScheduleConfig("全局规则", "0101", "0102"));
        when(schScheduleRunLogService.save(any())).thenAnswer(invocation -> {
            SchScheduleRunLog runLog = invocation.getArgument(0);
            runLog.setId(3001L);
            return true;
        });

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.classScheduling("2026-2027-1"));

        assertTrue(exception.getMessage().contains("排课前校验失败"));
        verify(schedulingEngine, never()).execute(any());
    }

    private static SchedulingTask fixedTask(Long taskId,
                                            String taskCode,
                                            String classNo,
                                            String teacherNo,
                                            Long fixedRoomId,
                                            String slotCode) {
        return SchedulingTask.builder()
                .taskId(taskId)
                .taskCode(taskCode)
                .classNo(classNo)
                .teacherNo(teacherNo)
                .fixedTime(true)
                .fixedRoomId(fixedRoomId)
                .fixedTimeSlots(List.of(slotCode))
                .build();
    }

    private static SchTask buildFixedStandardTask(Long id,
                                                  String taskCode,
                                                  String classNo,
                                                  String courseNo,
                                                  String teacherNo,
                                                  int weekdayNo,
                                                  int periodNo) {
        SchTask task = new SchTask();
        task.setId(id);
        task.setTaskCode(taskCode);
        task.setWeekHours(2);
        task.setTotalHours(32);
        task.setPriorityLevel(8);
        task.setNeedFixedTime(1);
        task.setFixedWeekdayNo(weekdayNo);
        task.setFixedPeriodNo(periodNo);
        task.setRemark("semester=2026-2027-1,classNo=" + classNo
                + ",courseNo=" + courseNo
                + ",teacherNo=" + teacherNo
                + ",gradeNo=2026,courseName=测试课程,courseAttr=必修,teacherName=测试教师");
        return task;
    }

    private static ScheduleConfigVO buildScheduleConfig(String ruleName, String... slots) {
        CfgScheduleRule rule = new CfgScheduleRule();
        rule.setRuleName(ruleName);
        List<CfgTimeSlot> timeSlots = java.util.Arrays.stream(slots)
                .map(AdministrativeClassSchedulingPreCheckTest::buildTimeSlot)
                .toList();
        return ScheduleConfigVO.builder()
                .scheduleRule(rule)
                .timeSlots(timeSlots)
                .featureToggles(List.of())
                .build();
    }

    private static CfgTimeSlot buildTimeSlot(String slotCode) {
        CfgTimeSlot timeSlot = new CfgTimeSlot();
        timeSlot.setWeekdayNo(Integer.parseInt(slotCode.substring(0, 2)));
        timeSlot.setPeriodNo(Integer.parseInt(slotCode.substring(2, 4)));
        timeSlot.setIsTeaching(1);
        timeSlot.setIsFixedBreak(0);
        return timeSlot;
    }
}
