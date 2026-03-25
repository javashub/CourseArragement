package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.entity.response.CoursePlanVo;
import com.lyk.coursearrange.schedule.entity.SchScheduleAdjustLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleAdjustLogService;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoursePlanServiceImplTest {

    @Mock
    private SchScheduleAdjustLogService schScheduleAdjustLogService;
    @Mock
    private AuthFacadeService authFacadeService;
    @Mock
    private SchScheduleResultService schScheduleResultService;
    @Mock
    private SchTaskService schTaskService;
    @Mock
    private ResClassroomService resClassroomService;
    @Mock
    private SysUserService sysUserService;

    private CoursePlanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CoursePlanServiceImpl();
        ReflectionTestUtils.setField(service, "schScheduleAdjustLogService", schScheduleAdjustLogService);
        ReflectionTestUtils.setField(service, "authFacadeService", authFacadeService);
        ReflectionTestUtils.setField(service, "schScheduleResultService", schScheduleResultService);
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resClassroomService", resClassroomService);
        ReflectionTestUtils.setField(service, "sysUserService", sysUserService);
    }

    @Test
    void queryCoursePlanByClassNo_shouldPreferStandardResults() {
        SchScheduleResult result = new SchScheduleResult();
        result.setId(101L);
        result.setTaskId(201L);
        result.setRemark("2025-2026-1");
        result.setWeekdayNo(2);
        result.setPeriodNo(3);
        result.setClassroomId(501L);
        result.setDeleted(0);

        SchTask task = new SchTask();
        task.setId(201L);
        task.setRemark("gradeNo=G1,classNo=C1,courseNo=K1,courseName=数学,teacherNo=T1,teacherName=张老师");

        ResClassroom classroom = new ResClassroom();
        classroom.setId(501L);
        classroom.setClassroomCode("01-101");

        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of(result));
        when(schTaskService.listByIds(anyCollection())).thenReturn(List.of(task));
        when(resClassroomService.listByIds(List.of(501L))).thenReturn(List.of(classroom));

        ServerResponse<?> response = service.queryCoursePlanByClassNo("C1", "2025-2026-1");

        assertTrue(response.isSuccess());
        List<?> plans = (List<?>) response.getData();
        assertEquals(1, plans.size());
        CoursePlanVo plan = (CoursePlanVo) plans.get(0);
        assertEquals(Integer.valueOf(101), plan.getId());
        assertEquals(Long.valueOf(101L), plan.getStandardResultId());
        assertEquals("C1", plan.getClassNo());
        assertEquals("数学", plan.getCourseName());
        assertEquals("张老师", plan.getTeacherName());
        assertEquals("01-101", plan.getClassroomNo());
        assertEquals("08", plan.getClassTime());
    }

    @Test
    void queryCoursePlanByClassNo_shouldNotFallbackToLegacyCopies() {
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());

        ServerResponse<?> response = service.queryCoursePlanByClassNo("C1", "2025-2026-1");

        assertTrue(!response.isSuccess());
        assertEquals("该班级没有课表", response.getMessage());
    }

    @Test
    void queryCoursePlanByTeacherNo_shouldNotFallbackToLegacyCopies() {
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());

        ServerResponse<?> response = service.queryCoursePlanByTeacherNo("T1", "2025-2026-1");

        assertTrue(!response.isSuccess());
        assertEquals("该教师没有课表", response.getMessage());
    }

    @Test
    void adjustCoursePlan_shouldOnlyUpdateStandardResultWhenStandardResultMatches() {
        SchScheduleResult result = new SchScheduleResult();
        result.setId(101L);
        result.setTaskId(201L);
        result.setRemark("2025-2026-1");
        result.setWeekdayNo(2);
        result.setPeriodNo(3);
        result.setDeleted(0);

        SchTask task = new SchTask();
        task.setId(201L);
        task.setRemark("gradeNo=G1,classNo=C1,courseNo=K1,teacherNo=T1");

        CoursePlanAdjustRequest request = new CoursePlanAdjustRequest();
        request.setStandardResultId(101L);
        request.setClassTime("09");

        when(schScheduleResultService.getById(101L)).thenReturn(result);
        when(schTaskService.getById(201L)).thenReturn(task);
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());
        when(schScheduleResultService.updateById(any(SchScheduleResult.class))).thenReturn(true);

        ServerResponse<?> response = service.adjustCoursePlan(request);

        assertTrue(response.isSuccess());
        ArgumentCaptor<SchScheduleAdjustLog> logCaptor = ArgumentCaptor.forClass(SchScheduleAdjustLog.class);
        verify(schScheduleAdjustLogService).save(logCaptor.capture());
        SchScheduleAdjustLog log = logCaptor.getValue();
        assertNotNull(log);
        assertEquals(Long.valueOf(101L), log.getSourceResultId());
        assertEquals(Integer.valueOf(2), log.getBeforeWeekdayNo());
        assertEquals(Integer.valueOf(3), log.getBeforePeriodNo());
        assertEquals(Integer.valueOf(2), log.getAfterWeekdayNo());
        assertEquals(Integer.valueOf(4), log.getAfterPeriodNo());
    }

    @Test
    void adjustCoursePlan_shouldPreferStandardResultLookupBeforeLegacyIdLookup() {
        SchScheduleResult result = new SchScheduleResult();
        result.setId(101L);
        result.setTaskId(201L);
        result.setRemark("2025-2026-1");
        result.setWeekdayNo(2);
        result.setPeriodNo(3);
        result.setDeleted(0);

        SchTask task = new SchTask();
        task.setId(201L);
        task.setRemark("gradeNo=G1,classNo=C1,courseNo=K1,teacherNo=T1");

        CoursePlanAdjustRequest request = new CoursePlanAdjustRequest();
        request.setId(101);
        request.setClassTime("09");

        when(schScheduleResultService.getById(101L)).thenReturn(result);
        when(schTaskService.getById(201L)).thenReturn(task);
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());
        when(schScheduleResultService.updateById(any(SchScheduleResult.class))).thenReturn(true);

        ServerResponse<?> response = service.adjustCoursePlan(request);

        assertTrue(response.isSuccess());
        assertEquals(Long.valueOf(101L), request.getStandardResultId());
    }

    @Test
    void adjustCoursePlan_shouldNotFallbackToLegacyPlanWhenStandardResultMissing() {
        CoursePlanAdjustRequest request = new CoursePlanAdjustRequest();
        request.setId(999);
        request.setClassTime("09");

        when(schScheduleResultService.getById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.adjustCoursePlan(request));

        assertEquals(ResultCode.NOT_FOUND.getCode(), exception.getCode());
        assertEquals("标准课表记录不存在", exception.getMessage());
    }

    @Test
    void listOccupiedClassroomNos_shouldPreferStandardResults() {
        SchScheduleResult result = new SchScheduleResult();
        result.setId(101L);
        result.setClassroomId(501L);
        result.setDeleted(0);

        ResClassroom classroom = new ResClassroom();
        classroom.setId(501L);
        classroom.setClassroomCode("01-101");

        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of(result));
        when(resClassroomService.listByIds(List.of(501L))).thenReturn(List.of(classroom));

        List<String> occupiedClassrooms = service.listOccupiedClassroomNos("01");

        assertEquals(List.of("01-101"), occupiedClassrooms);
    }

    @Test
    void listOccupiedClassroomNos_shouldReturnEmptyWhenStandardQueryFails() {
        when(schScheduleResultService.list(any(Wrapper.class))).thenThrow(new RuntimeException("boom"));

        List<String> occupiedClassrooms = service.listOccupiedClassroomNos("01");

        assertEquals(List.of(), occupiedClassrooms);
    }

    @Test
    void listRecentAdjustLogs_shouldReadStandardAdjustLogsOnly() {
        SchScheduleAdjustLog adjustLog = new SchScheduleAdjustLog();
        adjustLog.setId(1L);
        adjustLog.setSourceResultId(101L);
        adjustLog.setBeforeWeekdayNo(2);
        adjustLog.setBeforePeriodNo(3);
        adjustLog.setAfterWeekdayNo(2);
        adjustLog.setAfterPeriodNo(4);
        adjustLog.setAdjustReason("拖拽调课");
        adjustLog.setRemark("2025-2026-1");
        adjustLog.setCreatedAt(java.time.LocalDateTime.of(2026, 3, 25, 9, 30, 0));

        SchScheduleResult result = new SchScheduleResult();
        result.setId(101L);
        result.setTaskId(201L);

        SchTask task = new SchTask();
        task.setId(201L);
        task.setRemark("semester=2025-2026-1,classNo=C1,teacherNo=T1,courseNo=K1");

        when(schScheduleAdjustLogService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<SchScheduleAdjustLog>>any()))
                .thenReturn(List.of(adjustLog));
        when(schScheduleResultService.listByIds(List.of(101L))).thenReturn(List.of(result));
        when(schTaskService.listByIds(List.of(201L))).thenReturn(List.of(task));

        List<?> logs = service.listRecentAdjustLogs("2025-2026-1", "C1", "T1", 10);

        assertEquals(1, logs.size());
    }
}
