package com.lyk.coursearrange.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyk.coursearrange.dashboard.vo.DashboardOverviewVO;
import com.lyk.coursearrange.dashboard.vo.DashboardRecentAdjustmentVO;
import com.lyk.coursearrange.dashboard.vo.DashboardRecentTaskVO;
import com.lyk.coursearrange.dashboard.vo.DashboardResourceCountsVO;
import com.lyk.coursearrange.dashboard.vo.DashboardTaskStatsVO;
import com.lyk.coursearrange.dashboard.vo.DashboardTimetableItemVO;
import com.lyk.coursearrange.dashboard.vo.DashboardTodayTimetableVO;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.schedule.engine.model.SchedulingFailureCode;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import com.lyk.coursearrange.schedule.entity.SchScheduleAdjustLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunLog;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.AdminClassService;
import com.lyk.coursearrange.schedule.service.SchScheduleAdjustLogService;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunDetailService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunLogService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.vo.AdminClassVO;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.service.CfgTimeSlotService;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 工作台总览统计与今日课表关键逻辑测试。
 *
 * 说明：数据源固定为 sch_task / sch_schedule_result / sch_schedule_adjust_log，
 * 学期、班级、课程等展示字段来自 sch_task.remark 元数据，与排课主链口径一致。
 */
@ExtendWith(MockitoExtension.class)
class DashboardOverviewServiceTest {

    private static final String SEMESTER = "2025-2026-1";
    private static final String REMARK_CLASS_1 =
            "semester=2025-2026-1,classNo=C1,courseNo=K1,teacherNo=T1,gradeNo=G1,courseName=数学,teacherName=张老师";
    private static final String REMARK_CLASS_2 =
            "semester=2025-2026-1,classNo=C2,courseNo=K2,teacherNo=T2,gradeNo=G1,courseName=英语,teacherName=李老师";
    private static final String REMARK_CLASS_3 =
            "semester=2025-2026-1,classNo=C3,courseNo=K3,teacherNo=T3,gradeNo=G1,courseName=物理,teacherName=王老师";

    @Mock
    private SchTaskService schTaskService;
    @Mock
    private SchScheduleResultService schScheduleResultService;
    @Mock
    private SchScheduleAdjustLogService schScheduleAdjustLogService;
    @Mock
    private SchScheduleRunLogService schScheduleRunLogService;
    @Mock
    private SchScheduleRunDetailService schScheduleRunDetailService;
    @Mock
    private SysUserService sysUserService;
    @Mock
    private AdminClassService adminClassService;
    @Mock
    private ResTeacherService resTeacherService;
    @Mock
    private ResClassroomService resClassroomService;
    @Mock
    private CfgTimeSlotService cfgTimeSlotService;

    private DashboardOverviewServiceImpl service;
    private int todayWeekday;

    @BeforeEach
    void setUp() {
        service = new DashboardOverviewServiceImpl(schTaskService,
                schScheduleResultService,
                schScheduleAdjustLogService,
                schScheduleRunLogService,
                schScheduleRunDetailService,
                sysUserService,
                adminClassService,
                resTeacherService,
                resClassroomService,
                cfgTimeSlotService);
        todayWeekday = LocalDate.now().getDayOfWeek().getValue();
    }

    @Test
    void getOverview_shouldAggregateStandardSchedulingTables() {
        SchTask task1 = task(1L, "TASK-1", REMARK_CLASS_1);
        SchTask task3 = task(3L, "TASK-3", REMARK_CLASS_3);
        SchScheduleResult completed1 = result(11L, 1L, 1, 1, 5L);
        SchScheduleResult completed2 = result(12L, 2L, 2, 2, 6L);
        SchScheduleResult todayResult = result(13L, 1L, todayWeekday, 1, 5L);
        SchScheduleResult adjustedResult = result(77L, 1L, 1, 1, 5L);
        SchScheduleRunLog latestRun = new SchScheduleRunLog();
        latestRun.setId(900L);
        latestRun.setRemark(SEMESTER);

        when(schTaskService.list(any(Wrapper.class)))
                .thenReturn(List.of(task1))
                .thenReturn(List.of(task1, task3));
        when(schTaskService.count(any(Wrapper.class))).thenReturn(4L);
        when(schScheduleResultService.list(any(Wrapper.class)))
                .thenReturn(List.of(completed1, completed2))
                .thenReturn(List.of(todayResult));
        when(schScheduleRunLogService.getOne(any(Wrapper.class), ArgumentMatchers.eq(false))).thenReturn(latestRun);
        when(schScheduleRunDetailService.listByRunLogId(900L)).thenReturn(List.of(
                detail(3L, SchedulingFailureCode.CLASS_CONFLICT),
                detail(3L, SchedulingFailureCode.TEACHER_CONFLICT),
                detail(2L, SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT)));
        when(schTaskService.listByIds(ArgumentMatchers.<Long>anyCollection()))
                .thenReturn(List.of(task1))
                .thenReturn(List.of(task1));
        when(cfgTimeSlotService.list(any(Wrapper.class))).thenReturn(List.of(timeSlot(todayWeekday, 1, "08:00", "08:45")));
        when(resClassroomService.listByIds(ArgumentMatchers.<Long>anyCollection()))
                .thenReturn(List.of(classroom(5L, "R101")));
        when(schScheduleAdjustLogService.list(any(Wrapper.class))).thenReturn(List.of(adjustLog(77L, 1, 1, 2, 3)));
        when(schScheduleResultService.listByIds(ArgumentMatchers.<Long>anyCollection()))
                .thenReturn(List.of(adjustedResult));
        when(adminClassService.listClassesByCodes(ArgumentMatchers.anyCollection()))
                .thenReturn(List.of(adminClass("C1", "2024级软件工程1班")));
        when(sysUserService.listByIds(ArgumentMatchers.<Long>anyCollection())).thenReturn(List.of(operator(910001L, "系统管理员")));
        when(resTeacherService.count(any(Wrapper.class))).thenReturn(3L);
        when(adminClassService.countActiveClasses()).thenReturn(2L);
        when(resClassroomService.count(any(Wrapper.class))).thenReturn(4L);

        DashboardOverviewVO overview = service.getOverview(null);

        assertEquals(SEMESTER, overview.getSemester());

        DashboardTaskStatsVO stats = overview.getTaskStats();
        assertEquals(4, stats.getTotal());
        assertEquals(2, stats.getCompleted());
        assertEquals(1, stats.getConflict());
        assertEquals(1, stats.getPending());

        assertEquals(2, overview.getRecentTasks().size());
        DashboardRecentTaskVO recentConflictTask = overview.getRecentTasks().get(1);
        assertEquals("TASK-3", recentConflictTask.getTaskCode());
        assertEquals("C3", recentConflictTask.getClassNo());
        assertEquals(SEMESTER, recentConflictTask.getSemester());
        assertEquals("PENDING", recentConflictTask.getStatus());
        assertEquals(2, recentConflictTask.getConflictCount());

        DashboardTodayTimetableVO timetable = overview.getTodayTimetable();
        assertEquals("C1", timetable.getClassNo());
        assertEquals(1, timetable.getItems().size());
        DashboardTimetableItemVO item = timetable.getItems().get(0);
        assertEquals(1, item.getPeriodIndex());
        assertEquals("08:00", item.getStartTime());
        assertEquals("08:45", item.getEndTime());
        assertEquals("数学", item.getCourseName());
        assertEquals("张老师", item.getTeacherName());
        assertEquals("R101", item.getClassroomNo());

        assertEquals(1, overview.getRecentAdjustments().size());
        DashboardRecentAdjustmentVO adjustment = overview.getRecentAdjustments().get(0);
        assertEquals("2024级软件工程1班", adjustment.getClassName());
        assertEquals("数学", adjustment.getCourseName());
        assertEquals("0101", adjustment.getBeforeClassTime());
        assertEquals("0203", adjustment.getAfterClassTime());
        assertEquals("系统管理员", adjustment.getOperatorName());

        DashboardResourceCountsVO resourceCounts = overview.getResourceCounts();
        assertEquals(3L, resourceCounts.getTeacher());
        assertEquals(2L, resourceCounts.getClassCount());
        assertEquals(4L, resourceCounts.getClassroom());
    }

    @Test
    void getOverview_shouldDegradeToEmptyStateWithoutSemesterAndData() {
        when(schTaskService.list(any(Wrapper.class))).thenReturn(List.of());
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());
        when(resTeacherService.count(any(Wrapper.class))).thenReturn(0L);
        when(adminClassService.countActiveClasses()).thenReturn(0L);
        when(resClassroomService.count(any(Wrapper.class))).thenReturn(0L);

        DashboardOverviewVO overview = service.getOverview(null);

        assertEquals("", overview.getSemester());
        assertEquals(0, overview.getTaskStats().getTotal());
        assertEquals(0, overview.getTaskStats().getCompleted());
        assertEquals(0, overview.getTaskStats().getPending());
        assertEquals(0, overview.getTaskStats().getConflict());
        assertTrue(overview.getRecentTasks().isEmpty());
        assertNull(overview.getTodayTimetable().getClassNo());
        assertTrue(overview.getTodayTimetable().getItems().isEmpty());
        assertTrue(overview.getRecentAdjustments().isEmpty());
        assertEquals(0L, overview.getResourceCounts().getTeacher());
    }

    @Test
    void buildTaskStats_shouldTreatConflictAsUncompletedPendingAndNeverNegative() {
        SchScheduleResult completed1 = result(11L, 1L, 1, 1, 5L);
        when(schTaskService.count(any(Wrapper.class))).thenReturn(2L);
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of(completed1));

        DashboardTaskStatsVO stats = service.buildTaskStats(SEMESTER, Map.of(3L, 1));

        assertEquals(2, stats.getTotal());
        assertEquals(1, stats.getCompleted());
        assertEquals(1, stats.getConflict());
        assertEquals(0, stats.getPending());
    }

    @Test
    void resolveCurrentSemester_shouldReadLatestStandardTaskRemark() {
        when(schTaskService.list(any(Wrapper.class))).thenReturn(List.of(task(1L, "TASK-1", REMARK_CLASS_1)));

        assertEquals(SEMESTER, service.resolveCurrentSemester());
    }

    @Test
    void resolveCurrentSemester_shouldFallbackToScheduleResultWhenTasksMissing() {
        when(schTaskService.list(any(Wrapper.class))).thenReturn(List.of());
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of(result(11L, 1L, 1, 1, 5L)));

        assertEquals(SEMESTER, service.resolveCurrentSemester());
    }

    @Test
    void buildTodayTimetable_shouldPickFirstClassWithLessonWhenClassNoMissing() {
        SchTask task1 = task(1L, "TASK-1", REMARK_CLASS_1);
        SchTask task2 = task(2L, "TASK-2", REMARK_CLASS_2);
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of(
                result(21L, 2L, todayWeekday, 2, 6L),
                result(22L, 1L, todayWeekday, 1, 5L)));
        when(schTaskService.listByIds(ArgumentMatchers.<Long>anyCollection())).thenReturn(List.of(task1, task2));
        when(cfgTimeSlotService.list(any(Wrapper.class))).thenReturn(List.of(
                timeSlot(todayWeekday, 1, "08:00", "08:45"),
                timeSlot(todayWeekday, 2, "08:55", "09:40")));
        when(resClassroomService.listByIds(ArgumentMatchers.<Long>anyCollection()))
                .thenReturn(List.of(classroom(5L, "R101")));

        DashboardTodayTimetableVO timetable = service.buildTodayTimetable(SEMESTER, null);

        assertEquals("C1", timetable.getClassNo());
        assertEquals(1, timetable.getItems().size());
        assertEquals("数学", timetable.getItems().get(0).getCourseName());
        assertEquals("08:00", timetable.getItems().get(0).getStartTime());
        assertEquals("R101", timetable.getItems().get(0).getClassroomNo());
    }

    @Test
    void buildTodayTimetable_shouldHonorClassNoOverride() {
        SchTask task2 = task(2L, "TASK-2", REMARK_CLASS_2);
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of(result(21L, 2L, todayWeekday, 2, 6L)));
        when(schTaskService.listByIds(ArgumentMatchers.<Long>anyCollection())).thenReturn(List.of(task2));
        when(cfgTimeSlotService.list(any(Wrapper.class))).thenReturn(List.of(timeSlot(todayWeekday, 2, "08:55", "09:40")));
        when(resClassroomService.listByIds(ArgumentMatchers.<Long>anyCollection()))
                .thenReturn(List.of(classroom(6L, "R102")));

        DashboardTodayTimetableVO timetable = service.buildTodayTimetable(SEMESTER, " C2 ");

        assertEquals("C2", timetable.getClassNo());
        assertEquals(1, timetable.getItems().size());
        DashboardTimetableItemVO item = timetable.getItems().get(0);
        assertEquals("英语", item.getCourseName());
        assertEquals("李老师", item.getTeacherName());
        assertEquals("08:55", item.getStartTime());
        assertEquals("R102", item.getClassroomNo());
    }

    @Test
    void buildTodayTimetable_shouldReturnEmptyItemsWhenNoLessonToday() {
        when(schScheduleResultService.list(any(Wrapper.class))).thenReturn(List.of());

        DashboardTodayTimetableVO timetable = service.buildTodayTimetable(SEMESTER, null);

        assertNull(timetable.getClassNo());
        assertTrue(timetable.getItems().isEmpty());
    }

    @Test
    void loadConflictCountsByTask_shouldOnlyCountConflictReasonsOfLatestRun() {
        SchScheduleRunLog latestRun = new SchScheduleRunLog();
        latestRun.setId(900L);
        when(schScheduleRunLogService.getOne(any(Wrapper.class), ArgumentMatchers.eq(false))).thenReturn(latestRun);
        when(schScheduleRunDetailService.listByRunLogId(anyLong())).thenReturn(List.of(
                detail(3L, SchedulingFailureCode.CLASS_CONFLICT),
                detail(3L, SchedulingFailureCode.TEACHER_CONFLICT),
                detail(4L, SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT)));

        Map<Long, Integer> counts = service.loadConflictCountsByTask(SEMESTER);

        assertEquals(1, counts.size());
        assertEquals(2, counts.get(3L));
    }

    @Test
    void buildRecentAdjustments_shouldResolveClassNameAndOperatorName() {
        SchTask task1 = task(1L, "TASK-1", REMARK_CLASS_1);
        SchScheduleResult result = result(77L, 1L, 1, 1, 5L);
        when(schScheduleAdjustLogService.list(any(Wrapper.class))).thenReturn(List.of(adjustLog(77L, 1, 2, 3, 4)));
        when(schScheduleResultService.listByIds(ArgumentMatchers.<Long>anyCollection())).thenReturn(List.of(result));
        when(schTaskService.listByIds(ArgumentMatchers.<Long>anyCollection())).thenReturn(List.of(task1));
        when(adminClassService.listClassesByCodes(ArgumentMatchers.anyCollection()))
                .thenReturn(List.of(adminClass("C1", "2024级软件工程1班")));
        when(sysUserService.listByIds(ArgumentMatchers.<Long>anyCollection())).thenReturn(List.of(operator(910001L, "系统管理员")));

        List<DashboardRecentAdjustmentVO> adjustments = service.buildRecentAdjustments(SEMESTER);

        assertEquals(1, adjustments.size());
        DashboardRecentAdjustmentVO adjustment = adjustments.get(0);
        assertEquals("2024级软件工程1班", adjustment.getClassName());
        assertEquals("数学", adjustment.getCourseName());
        assertEquals("0102", adjustment.getBeforeClassTime());
        assertEquals("0304", adjustment.getAfterClassTime());
        assertEquals("系统管理员", adjustment.getOperatorName());
    }

    private SchTask task(Long id, String taskCode, String remark) {
        SchTask task = new SchTask();
        task.setId(id);
        task.setTaskCode(taskCode);
        task.setRemark(remark);
        task.setTaskStatus("PENDING");
        task.setUpdatedAt(LocalDateTime.of(2026, 11, 20, 9, 0));
        return task;
    }

    private SchScheduleResult result(Long id, Long taskId, int weekdayNo, int periodNo, Long classroomId) {
        SchScheduleResult result = new SchScheduleResult();
        result.setId(id);
        result.setTaskId(taskId);
        result.setWeekdayNo(weekdayNo);
        result.setPeriodNo(periodNo);
        result.setClassroomId(classroomId);
        result.setRemark(SEMESTER);
        return result;
    }

    private UnscheduledTaskDetail detail(Long taskId, SchedulingFailureCode reasonCode) {
        return UnscheduledTaskDetail.builder()
                .taskId(taskId)
                .reasonCode(reasonCode.name())
                .build();
    }

    private CfgTimeSlot timeSlot(int weekdayNo, int periodNo, String startTimeText, String endTimeText) {
        CfgTimeSlot timeSlot = new CfgTimeSlot();
        timeSlot.setWeekdayNo(weekdayNo);
        timeSlot.setPeriodNo(periodNo);
        timeSlot.setStartTimeText(startTimeText);
        timeSlot.setEndTimeText(endTimeText);
        return timeSlot;
    }

    private ResClassroom classroom(Long id, String classroomCode) {
        ResClassroom classroom = new ResClassroom();
        classroom.setId(id);
        classroom.setClassroomCode(classroomCode);
        return classroom;
    }

    private SchScheduleAdjustLog adjustLog(Long sourceResultId,
                                           int beforeWeekdayNo,
                                           int beforePeriodNo,
                                           int afterWeekdayNo,
                                           int afterPeriodNo) {
        SchScheduleAdjustLog adjustLog = new SchScheduleAdjustLog();
        adjustLog.setSourceResultId(sourceResultId);
        adjustLog.setOperatorUserId(910001L);
        adjustLog.setBeforeWeekdayNo(beforeWeekdayNo);
        adjustLog.setBeforePeriodNo(beforePeriodNo);
        adjustLog.setAfterWeekdayNo(afterWeekdayNo);
        adjustLog.setAfterPeriodNo(afterPeriodNo);
        adjustLog.setCreatedAt(LocalDateTime.of(2026, 11, 20, 10, 0));
        return adjustLog;
    }

    private AdminClassVO adminClass(String classNo, String className) {
        AdminClassVO adminClass = new AdminClassVO();
        adminClass.setClassNo(classNo);
        adminClass.setClassName(className);
        return adminClass;
    }

    private SysUser operator(Long id, String displayName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setDisplayName(displayName);
        return user;
    }
}
