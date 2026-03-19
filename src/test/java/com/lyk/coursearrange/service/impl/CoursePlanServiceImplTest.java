package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.entity.response.CoursePlanVo;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.service.CoursePlanAdjustLogService;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
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
    private CoursePlanLegacySupport coursePlanLegacySupport;
    @Mock
    private CoursePlanAdjustLogService coursePlanAdjustLogService;
    @Mock
    private AuthFacadeService authFacadeService;
    @Mock
    private ScheduleLogMirrorService scheduleLogMirrorService;
    @Mock
    private SchScheduleResultService schScheduleResultService;
    @Mock
    private SchTaskService schTaskService;
    @Mock
    private ResClassroomService resClassroomService;

    private CoursePlanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CoursePlanServiceImpl();
        ReflectionTestUtils.setField(service, "coursePlanLegacySupport", coursePlanLegacySupport);
        ReflectionTestUtils.setField(service, "coursePlanAdjustLogService", coursePlanAdjustLogService);
        ReflectionTestUtils.setField(service, "authFacadeService", authFacadeService);
        ReflectionTestUtils.setField(service, "scheduleLogMirrorService", scheduleLogMirrorService);
        ReflectionTestUtils.setField(service, "schScheduleResultService", schScheduleResultService);
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resClassroomService", resClassroomService);
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
        assertEquals("张老师", plan.getRealname());
        assertEquals("01-101", plan.getClassroomNo());
        assertEquals("08", plan.getClassTime());
        verify(coursePlanLegacySupport, never()).listAll();
    }

    @Test
    void queryCoursePlanByClassNo_shouldNotFallbackToLegacyCopies() {
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());

        ServerResponse<?> response = service.queryCoursePlanByClassNo("C1", "2025-2026-1");

        assertTrue(!response.isSuccess());
        assertEquals("该班级没有课表", response.getMessage());
        verify(coursePlanLegacySupport, never()).listByClassNo("C1", "2025-2026-1");
    }

    @Test
    void queryCoursePlanByTeacherNo_shouldNotFallbackToLegacyCopies() {
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());

        ServerResponse<?> response = service.queryCoursePlanByTeacherNo("T1", "2025-2026-1");

        assertTrue(!response.isSuccess());
        assertEquals("该教师没有课表", response.getMessage());
        verify(coursePlanLegacySupport, never()).listByTeacherNo("T1", "2025-2026-1");
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
        verify(coursePlanLegacySupport, never()).getOne(any());
        verify(coursePlanLegacySupport, never()).updateById(any(CoursePlan.class));

        ArgumentCaptor<CoursePlanAdjustLog> logCaptor = ArgumentCaptor.forClass(CoursePlanAdjustLog.class);
        verify(coursePlanAdjustLogService).save(logCaptor.capture());
        CoursePlanAdjustLog log = logCaptor.getValue();
        assertNotNull(log);
        assertNull(log.getCoursePlanId());
        assertEquals("08", log.getBeforeClassTime());
        assertEquals("09", log.getAfterClassTime());
        verify(scheduleLogMirrorService).mirrorAdjustLog(any(CoursePlanAdjustLog.class));
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
        verify(coursePlanLegacySupport, never()).getById(101);
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
        verify(coursePlanLegacySupport, never()).getById(999);
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
        verify(coursePlanLegacySupport, never()).listAll();
    }

    @Test
    void listOccupiedClassroomNos_shouldFallbackToLegacyCopiesWhenStandardQueryFails() {
        CoursePlan legacyPlan = new CoursePlan();
        legacyPlan.setClassroomNo("01-102");

        when(schScheduleResultService.list(any(Wrapper.class))).thenThrow(new RuntimeException("boom"));
        when(coursePlanLegacySupport.listAll()).thenReturn(List.of(legacyPlan));

        List<String> occupiedClassrooms = service.listOccupiedClassroomNos("01");

        assertEquals(List.of("01-102"), occupiedClassrooms);
        verify(coursePlanLegacySupport).listAll();
    }
}
