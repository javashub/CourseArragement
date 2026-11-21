package com.lyk.coursearrange.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.dashboard.service.DashboardOverviewService;
import com.lyk.coursearrange.dashboard.vo.DashboardOverviewVO;
import com.lyk.coursearrange.dashboard.vo.DashboardRecentAdjustmentVO;
import com.lyk.coursearrange.dashboard.vo.DashboardRecentTaskVO;
import com.lyk.coursearrange.dashboard.vo.DashboardResourceCountsVO;
import com.lyk.coursearrange.dashboard.vo.DashboardTaskStatsVO;
import com.lyk.coursearrange.dashboard.vo.DashboardTimetableItemVO;
import com.lyk.coursearrange.dashboard.vo.DashboardTodayTimetableVO;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.entity.ResTeacher;
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
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.AdminClassVO;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.service.CfgTimeSlotService;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作台总览服务实现。
 *
 * 数据口径说明：
 * 1. 学期、班级、课程、教师信息存在 sch_task.remark 的 key=value 元数据里（与排课主链保持一致）。
 * 2. sch_schedule_result.remark 存学期；今日课表与已排课表数都以该字段过滤。
 * 3. 冲突数取最近一次执行（sch_schedule_run_log）中原因码为教师/班级/教室冲突的失败任务数。
 * 4. 全程只读，不写 sch_task / sch_schedule_result / 调课日志。
 */
@Service
public class DashboardOverviewServiceImpl implements DashboardOverviewService {

    private static final int RECENT_LIMIT = 10;

    private static final Set<String> CONFLICT_REASON_CODES = Set.of(
            SchedulingFailureCode.TEACHER_CONFLICT.name(),
            SchedulingFailureCode.CLASS_CONFLICT.name(),
            SchedulingFailureCode.CLASSROOM_CONFLICT.name());

    private final SchTaskService schTaskService;
    private final SchScheduleResultService schScheduleResultService;
    private final SchScheduleAdjustLogService schScheduleAdjustLogService;
    private final SchScheduleRunLogService schScheduleRunLogService;
    private final SchScheduleRunDetailService schScheduleRunDetailService;
    private final SysUserService sysUserService;
    private final AdminClassService adminClassService;
    private final ResTeacherService resTeacherService;
    private final ResClassroomService resClassroomService;
    private final CfgTimeSlotService cfgTimeSlotService;

    public DashboardOverviewServiceImpl(SchTaskService schTaskService,
                                        SchScheduleResultService schScheduleResultService,
                                        SchScheduleAdjustLogService schScheduleAdjustLogService,
                                        SchScheduleRunLogService schScheduleRunLogService,
                                        SchScheduleRunDetailService schScheduleRunDetailService,
                                        SysUserService sysUserService,
                                        AdminClassService adminClassService,
                                        ResTeacherService resTeacherService,
                                        ResClassroomService resClassroomService,
                                        CfgTimeSlotService cfgTimeSlotService) {
        this.schTaskService = schTaskService;
        this.schScheduleResultService = schScheduleResultService;
        this.schScheduleAdjustLogService = schScheduleAdjustLogService;
        this.schScheduleRunLogService = schScheduleRunLogService;
        this.schScheduleRunDetailService = schScheduleRunDetailService;
        this.sysUserService = sysUserService;
        this.adminClassService = adminClassService;
        this.resTeacherService = resTeacherService;
        this.resClassroomService = resClassroomService;
        this.cfgTimeSlotService = cfgTimeSlotService;
    }

    @Override
    public DashboardOverviewVO getOverview(String classNo) {
        String semester = resolveCurrentSemester();
        Map<Long, Integer> conflictCounts = loadConflictCountsByTask(semester);
        DashboardOverviewVO overview = new DashboardOverviewVO();
        overview.setSemester(semester);
        overview.setTaskStats(buildTaskStats(semester, conflictCounts));
        overview.setRecentTasks(buildRecentTasks(semester, conflictCounts));
        overview.setTodayTimetable(buildTodayTimetable(semester, classNo));
        overview.setRecentAdjustments(buildRecentAdjustments(semester));
        overview.setResourceCounts(buildResourceCounts());
        return overview;
    }

    /**
     * 当前学期 = 最近更新的标准任务所属学期；任务缺失时回退最近课表结果的学期。
     */
    String resolveCurrentSemester() {
        List<SchTask> latestTasks = schTaskService.list(new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getDeleted, 0)
                .orderByDesc(SchTask::getUpdatedAt)
                .orderByDesc(SchTask::getId)
                .last("limit 1"));
        String semester = latestTasks.isEmpty()
                ? null
                : ScheduleTaskMetaUtils.parseTaskRemark(latestTasks.get(0).getRemark()).get("semester");
        if (hasSemester(semester)) {
            return semester;
        }
        List<SchScheduleResult> latestResults = schScheduleResultService.list(new LambdaQueryWrapper<SchScheduleResult>()
                .eq(SchScheduleResult::getDeleted, 0)
                .orderByDesc(SchScheduleResult::getUpdatedAt)
                .orderByDesc(SchScheduleResult::getId)
                .last("limit 1"));
        String resultSemester = latestResults.isEmpty() ? null : latestResults.get(0).getRemark();
        return hasSemester(resultSemester) ? resultSemester : "";
    }

    DashboardTaskStatsVO buildTaskStats(String semester, Map<Long, Integer> conflictCounts) {
        DashboardTaskStatsVO stats = new DashboardTaskStatsVO();
        if (!hasSemester(semester)) {
            return stats;
        }
        int total = (int) schTaskService.count(semesterTaskWrapper(semester));
        int completed = countCompletedTasks(semester);
        int conflict = conflictCounts == null ? 0 : conflictCounts.size();
        stats.setTotal(total);
        stats.setCompleted(completed);
        stats.setConflict(conflict);
        stats.setPending(Math.max(0, total - completed - conflict));
        return stats;
    }

    List<DashboardRecentTaskVO> buildRecentTasks(String semester, Map<Long, Integer> conflictCounts) {
        if (!hasSemester(semester)) {
            return new ArrayList<>();
        }
        List<SchTask> tasks = schTaskService.list(semesterTaskWrapper(semester)
                .orderByDesc(SchTask::getUpdatedAt)
                .orderByDesc(SchTask::getId)
                .last("limit " + RECENT_LIMIT));
        List<DashboardRecentTaskVO> recentTasks = new ArrayList<>(tasks.size());
        for (SchTask task : tasks) {
            Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(task.getRemark());
            DashboardRecentTaskVO vo = new DashboardRecentTaskVO();
            vo.setTaskCode(task.getTaskCode());
            vo.setClassNo(meta.getOrDefault("classNo", ""));
            vo.setSemester(meta.getOrDefault("semester", semester));
            vo.setStatus(task.getTaskStatus());
            vo.setConflictCount(conflictCounts == null ? 0 : conflictCounts.getOrDefault(task.getId(), 0));
            vo.setUpdatedAt(task.getUpdatedAt());
            recentTasks.add(vo);
        }
        return recentTasks;
    }

    DashboardTodayTimetableVO buildTodayTimetable(String semester, String classNo) {
        DashboardTodayTimetableVO timetable = new DashboardTodayTimetableVO();
        String overrideClassNo = classNo == null ? "" : classNo.trim();
        timetable.setClassNo(overrideClassNo.isBlank() ? null : overrideClassNo);
        if (!hasSemester(semester)) {
            return timetable;
        }
        int weekdayNo = LocalDate.now().getDayOfWeek().getValue();
        List<SchScheduleResult> results = schScheduleResultService.list(new LambdaQueryWrapper<SchScheduleResult>()
                .eq(SchScheduleResult::getDeleted, 0)
                .eq(SchScheduleResult::getRemark, semester)
                .eq(SchScheduleResult::getWeekdayNo, weekdayNo)
                .orderByAsc(SchScheduleResult::getPeriodNo)
                .orderByAsc(SchScheduleResult::getId));
        if (results.isEmpty()) {
            return timetable;
        }
        Map<Long, SchTask> taskMap = loadTasksByIds(results.stream()
                .map(SchScheduleResult::getTaskId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        List<TimetableEntry> entries = new ArrayList<>();
        for (SchScheduleResult result : results) {
            Long taskId = result.getTaskId();
            SchTask task = taskId == null ? null : taskMap.get(taskId);
            if (task == null) {
                continue;
            }
            Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(task.getRemark());
            String entryClassNo = meta.getOrDefault("classNo", "");
            if (entryClassNo.isBlank()) {
                continue;
            }
            entries.add(new TimetableEntry(entryClassNo,
                    result.getClassroomId(),
                    result.getPeriodNo(),
                    meta.getOrDefault("courseName", meta.getOrDefault("courseNo", "")),
                    meta.getOrDefault("teacherName", meta.getOrDefault("teacherNo", ""))));
        }
        if (entries.isEmpty()) {
            return timetable;
        }
        String selectedClassNo = overrideClassNo.isBlank()
                ? entries.stream().map(TimetableEntry::classNo).distinct().sorted().findFirst().orElse(null)
                : overrideClassNo;
        if (selectedClassNo == null) {
            return timetable;
        }
        timetable.setClassNo(selectedClassNo);
        List<TimetableEntry> selectedEntries = entries.stream()
                .filter(entry -> selectedClassNo.equals(entry.classNo()))
                .toList();
        if (selectedEntries.isEmpty()) {
            return timetable;
        }
        Map<Integer, CfgTimeSlot> timeSlotMap = loadTimeSlotsByPeriod(weekdayNo);
        Map<Long, String> classroomCodeMap = loadClassroomCodes(selectedEntries.stream()
                .map(TimetableEntry::classroomId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        List<DashboardTimetableItemVO> items = new ArrayList<>(selectedEntries.size());
        for (TimetableEntry entry : selectedEntries) {
            CfgTimeSlot timeSlot = timeSlotMap.get(entry.periodNo());
            DashboardTimetableItemVO item = new DashboardTimetableItemVO();
            item.setPeriodIndex(entry.periodNo());
            item.setStartTime(timeSlot == null ? "" : safe(timeSlot.getStartTimeText()));
            item.setEndTime(timeSlot == null ? "" : safe(timeSlot.getEndTimeText()));
            item.setCourseName(entry.courseName());
            item.setTeacherName(entry.teacherName());
            item.setClassroomNo(entry.classroomId() == null ? "" : classroomCodeMap.getOrDefault(entry.classroomId(), ""));
            items.add(item);
        }
        timetable.setItems(items);
        return timetable;
    }

    List<DashboardRecentAdjustmentVO> buildRecentAdjustments(String semester) {
        if (!hasSemester(semester)) {
            return new ArrayList<>();
        }
        List<SchScheduleAdjustLog> adjustLogs = schScheduleAdjustLogService.list(new LambdaQueryWrapper<SchScheduleAdjustLog>()
                .eq(SchScheduleAdjustLog::getDeleted, 0)
                .eq(SchScheduleAdjustLog::getRemark, semester)
                .orderByDesc(SchScheduleAdjustLog::getCreatedAt)
                .orderByDesc(SchScheduleAdjustLog::getId)
                .last("limit " + RECENT_LIMIT));
        if (adjustLogs.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, SchScheduleResult> resultMap = loadResultsByIds(adjustLogs.stream()
                .map(SchScheduleAdjustLog::getSourceResultId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        Map<Long, SchTask> taskMap = loadTasksByIds(resultMap.values().stream()
                .map(SchScheduleResult::getTaskId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        Map<String, String> classNameMap = loadClassNames(taskMap.values().stream()
                .map(task -> ScheduleTaskMetaUtils.parseTaskRemark(task.getRemark()).get("classNo"))
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        Map<Long, String> operatorNameMap = loadOperatorNames(adjustLogs.stream()
                .map(SchScheduleAdjustLog::getOperatorUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        List<DashboardRecentAdjustmentVO> adjustments = new ArrayList<>(adjustLogs.size());
        for (SchScheduleAdjustLog adjustLog : adjustLogs) {
            Long sourceResultId = adjustLog.getSourceResultId();
            SchScheduleResult result = sourceResultId == null ? null : resultMap.get(sourceResultId);
            Long taskId = result == null ? null : result.getTaskId();
            SchTask task = taskId == null ? null : taskMap.get(taskId);
            Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(task == null ? "" : task.getRemark());
            String classNo = meta.getOrDefault("classNo", "");
            Long operatorUserId = adjustLog.getOperatorUserId();
            DashboardRecentAdjustmentVO vo = new DashboardRecentAdjustmentVO();
            vo.setClassName(classNameMap.getOrDefault(classNo, classNo));
            vo.setCourseName(meta.getOrDefault("courseName", meta.getOrDefault("courseNo", "")));
            vo.setBeforeClassTime(ScheduleTaskMetaUtils.buildClassTime(adjustLog.getBeforeWeekdayNo(), adjustLog.getBeforePeriodNo()));
            vo.setAfterClassTime(ScheduleTaskMetaUtils.buildClassTime(adjustLog.getAfterWeekdayNo(), adjustLog.getAfterPeriodNo()));
            vo.setOperatorName(operatorUserId == null ? "--" : operatorNameMap.getOrDefault(operatorUserId, "--"));
            vo.setCreateTime(adjustLog.getCreatedAt());
            adjustments.add(vo);
        }
        return adjustments;
    }

    DashboardResourceCountsVO buildResourceCounts() {
        DashboardResourceCountsVO counts = new DashboardResourceCountsVO();
        counts.setTeacher(resTeacherService.count(new LambdaQueryWrapper<ResTeacher>()
                .eq(ResTeacher::getDeleted, 0)));
        counts.setClassCount(adminClassService.countActiveClasses());
        counts.setClassroom(resClassroomService.count(new LambdaQueryWrapper<ResClassroom>()
                .eq(ResClassroom::getDeleted, 0)));
        return counts;
    }

    /**
     * 最近一次执行中，各任务的冲突次数；无执行日志时为空。
     */
    Map<Long, Integer> loadConflictCountsByTask(String semester) {
        if (!hasSemester(semester)) {
            return new LinkedHashMap<>();
        }
        SchScheduleRunLog latestRun = schScheduleRunLogService.getOne(new LambdaQueryWrapper<SchScheduleRunLog>()
                .eq(SchScheduleRunLog::getDeleted, 0)
                .eq(SchScheduleRunLog::getRemark, semester)
                .orderByDesc(SchScheduleRunLog::getCreatedAt)
                .orderByDesc(SchScheduleRunLog::getId)
                .last("limit 1"), false);
        if (latestRun == null || latestRun.getId() == null) {
            return new LinkedHashMap<>();
        }
        List<UnscheduledTaskDetail> details = schScheduleRunDetailService.listByRunLogId(latestRun.getId());
        if (details == null || details.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<Long, Integer> conflictCounts = new LinkedHashMap<>();
        for (UnscheduledTaskDetail detail : details) {
            if (detail == null || detail.getTaskId() == null || !isConflictReason(detail.getReasonCode())) {
                continue;
            }
            conflictCounts.merge(detail.getTaskId(), 1, Integer::sum);
        }
        return conflictCounts;
    }

    private int countCompletedTasks(String semester) {
        List<SchScheduleResult> results = schScheduleResultService.list(new LambdaQueryWrapper<SchScheduleResult>()
                .eq(SchScheduleResult::getDeleted, 0)
                .eq(SchScheduleResult::getRemark, semester));
        return (int) results.stream()
                .map(SchScheduleResult::getTaskId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .count();
    }

    private Map<Long, SchTask> loadTasksByIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of();
        }
        return schTaskService.listByIds(taskIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SchTask::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, SchScheduleResult> loadResultsByIds(List<Long> resultIds) {
        if (resultIds == null || resultIds.isEmpty()) {
            return Map.of();
        }
        return schScheduleResultService.listByIds(resultIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SchScheduleResult::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Integer, CfgTimeSlot> loadTimeSlotsByPeriod(int weekdayNo) {
        List<CfgTimeSlot> timeSlots = cfgTimeSlotService.list(new LambdaQueryWrapper<CfgTimeSlot>()
                .eq(CfgTimeSlot::getDeleted, 0)
                .eq(CfgTimeSlot::getWeekdayNo, weekdayNo));
        Map<Integer, CfgTimeSlot> timeSlotMap = new LinkedHashMap<>();
        for (CfgTimeSlot timeSlot : timeSlots) {
            if (timeSlot.getPeriodNo() == null) {
                continue;
            }
            timeSlotMap.putIfAbsent(timeSlot.getPeriodNo(), timeSlot);
        }
        return timeSlotMap;
    }

    private Map<Long, String> loadClassroomCodes(List<Long> classroomIds) {
        if (classroomIds.isEmpty()) {
            return Map.of();
        }
        return resClassroomService.listByIds(classroomIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ResClassroom::getId, ResClassroom::getClassroomCode, (left, right) -> left));
    }

    private Map<String, String> loadClassNames(List<String> classNos) {
        if (classNos.isEmpty()) {
            return Map.of();
        }
        List<AdminClassVO> classes = adminClassService.listClassesByCodes(classNos);
        if (classes == null || classes.isEmpty()) {
            return Map.of();
        }
        return classes.stream()
                .filter(item -> item != null && item.getClassNo() != null)
                .collect(Collectors.toMap(AdminClassVO::getClassNo,
                        item -> item.getClassName() == null ? item.getClassNo() : item.getClassName(),
                        (left, right) -> left));
    }

    private Map<Long, String> loadOperatorNames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserService.listByIds(userIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SysUser::getId, this::resolveOperatorName, (left, right) -> left));
    }

    private String resolveOperatorName(SysUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername() == null ? "--" : user.getUsername();
    }

    private LambdaQueryWrapper<SchTask> semesterTaskWrapper(String semester) {
        return new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getDeleted, 0)
                .like(SchTask::getRemark, "semester=" + semester);
    }

    private boolean isConflictReason(String reasonCode) {
        return reasonCode != null && CONFLICT_REASON_CODES.contains(reasonCode);
    }

    private boolean hasSemester(String semester) {
        return semester != null && !semester.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 今日课表中间态：班级 + 节次 + 课程/教师。
     */
    private record TimetableEntry(String classNo,
                                  Long classroomId,
                                  Integer periodNo,
                                  String courseName,
                                  String teacherName) {
    }
}
