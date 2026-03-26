package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.exceptions.CourseArrangeException;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.common.ConstantInfo;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.resource.util.ClassForbiddenTimeSlotUtils;
import com.lyk.coursearrange.resource.util.TeacherForbiddenTimeSlotUtils;
import com.lyk.coursearrange.schedule.engine.SchedulingEngine;
import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.AdminClassService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunLogService;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunDetailService;
import com.lyk.coursearrange.schedule.service.ScheduleResultWriteService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.AdminClassVO;
import com.lyk.coursearrange.schedule.vo.ScheduleExecutionDetailVO;
import com.lyk.coursearrange.schedule.vo.ScheduleRunLogVO;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.query.ConfigScopeQuery;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import com.lyk.coursearrange.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lequal
 * @since 2020-04-06
 */
@Service
@Slf4j
public class ClassTaskServiceImpl implements ClassTaskService {

    @Resource
    private AuthFacadeService authFacadeService;
    @Resource
    private ScheduleResultWriteService scheduleResultWriteService;
    @Resource
    private SchTaskService schTaskService;
    @Resource
    private SchScheduleRunLogService schScheduleRunLogService;
    @Resource
    private ScheduleConfigFacadeService scheduleConfigFacadeService;
    @Resource
    private ResClassroomService resClassroomService;
    @Resource
    private ResCourseService resCourseService;
    @Resource
    private ResTeacherService resTeacherService;
    @Resource
    private AdminClassService adminClassService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SchedulingEngine schedulingEngine;
    @Resource
    private SchScheduleRunDetailService schScheduleRunDetailService;
    @Resource
    private SchScheduleResultService schScheduleResultService;

    /**
     * 排课算法入口
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ServerResponse classScheduling(String semester) {
        long start = System.currentTimeMillis();
        int taskCount = 0;
        try {
            List<SchedulingTaskInput> schedulingTasks = listSchedulingTasks(semester);
            if (schedulingTasks == null || schedulingTasks.isEmpty()) {
                throw buildSchedulingException(start, semester, taskCount,
                        "排课失败，查询不到排课任务！请导入排课任务再进行排课~", ResultCode.BUSINESS_ERROR);
            }
            SchedulingRuntimeContext runtimeContext = resolveSchedulingRuntimeContext();
            taskCount = schedulingTasks.size();

            checkWeeksNumber(schedulingTasks, runtimeContext.availableClassTimes());
            validateTeacherForbiddenTimeSlots(schedulingTasks);
            validateClassForbiddenTimeSlots(schedulingTasks);
            validateTeacherDayHourLimit(schedulingTasks);
            SchedulingExecutionResult executionResult = schedulingEngine.execute(SchedulingEngineRequest.builder()
                    .semester(semester)
                    .timeSlotCodes(runtimeContext.availableClassTimes())
                    .tasks(buildEngineTasks(schedulingTasks))
                    .classrooms(buildSchedulingClassrooms())
                    .build());

            long duration = System.currentTimeMillis() - start;
            Long runLogId = saveExecuteLog(semester,
                    executionResult.getTaskCount(),
                    executionResult.getScheduledTaskCount(),
                    executionResult.getUnscheduledTaskCount(),
                    duration,
                    buildSchedulingSummaryMessage(executionResult),
                    executionResult.getUnscheduledTaskCount() > 0 ? "PARTIAL" : "SUCCESS");
            scheduleResultWriteService.replaceScheduleResults(semester, runLogId, schedulingTasks, executionResult.getAssignments());
            schScheduleRunDetailService.replaceRunDetails(runLogId, executionResult.getUnscheduledTasks());

            Map<String, Object> schedulingSummary = buildSchedulingSummary(runLogId, executionResult, runtimeContext);
            return buildSchedulingSuccessResponse(duration, executionResult.getAssignments().size(), schedulingSummary);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception e) {
            log.error("排课失败： {}", e.getMessage(), e);
            throw buildSchedulingException(start, semester, taskCount, buildExecuteErrorMessage(e), ResultCode.BUSINESS_ERROR);
        }
    }

    ServerResponse<Map<String, Object>> buildSchedulingSuccessResponse(long duration, int generatedPlanCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("durationMs", duration);
        data.put("generatedPlanCount", generatedPlanCount);
        return ServerResponse.ofSuccess(String.format("排课成功，标准课表已生成，耗时：%sms", duration), data);
    }

    ServerResponse<Map<String, Object>> buildSchedulingSuccessResponse(long duration,
                                                                      int generatedPlanCount,
                                                                      Map<String, Object> schedulingSummary) {
        Map<String, Object> data = new HashMap<>();
        data.put("durationMs", duration);
        data.put("generatedPlanCount", generatedPlanCount);
        data.putAll(schedulingSummary);
        int unscheduledTaskCount = asInt(schedulingSummary.get("unscheduledTaskCount"));
        String message = unscheduledTaskCount > 0
                ? String.format("排课完成，但仍有 %s 个任务未生成标准课表，耗时：%sms", unscheduledTaskCount, duration)
                : String.format("排课成功，标准课表已生成，耗时：%sms", duration);
        return ServerResponse.ofSuccess(message, data);
    }

    private Map<String, Object> buildSchedulingSummary(Long runLogId,
                                                       SchedulingExecutionResult executionResult,
                                                       SchedulingRuntimeContext runtimeContext) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("runLogId", runLogId);
        summary.put("taskCount", executionResult.getTaskCount());
        summary.put("scheduledTaskCount", executionResult.getScheduledTaskCount());
        summary.put("unscheduledTaskCount", executionResult.getUnscheduledTaskCount());
        summary.put("conflictTaskCount", executionResult.getUnscheduledTaskCount());
        summary.put("successRate", executionResult.getSuccessRate());
        summary.put("unscheduledTasks", executionResult.getUnscheduledTasks());
        summary.put("constraintSummary", executionResult.getConstraintSummary());
        summary.put("effectiveScheduleRuleName", runtimeContext.scheduleRuleName());
        summary.put("effectiveTimeSlotCount", runtimeContext.availableClassTimes().size());
        summary.put("timeSlotConfigApplied", runtimeContext.configApplied());
        return summary;
    }

    private String buildSchedulingSummaryMessage(SchedulingExecutionResult executionResult) {
        if (executionResult.getUnscheduledTaskCount() != null && executionResult.getUnscheduledTaskCount() > 0) {
            return String.format("排课完成，%s 个任务未排成，生成 %s 条课表记录",
                    executionResult.getUnscheduledTaskCount(),
                    executionResult.getAssignments() == null ? 0 : executionResult.getAssignments().size());
        }
        return String.format("排课成功，生成 %s 条课表记录",
                executionResult.getAssignments() == null ? 0 : executionResult.getAssignments().size());
    }

    private List<SchedulingTask> buildEngineTasks(List<SchedulingTaskInput> schedulingTasks) {
        if (schedulingTasks == null || schedulingTasks.isEmpty()) {
            return List.of();
        }
        return schedulingTasks.stream()
                .map(task -> SchedulingTask.builder()
                        .taskId(task.getStandardTaskId())
                        .taskCode(task.getTaskCode())
                        .semester(task.getSemester())
                        .classNo(task.getClassNo())
                        .courseNo(task.getCourseNo())
                        .teacherNo(task.getTeacherNo())
                        .courseName(task.getCourseName())
                        .teacherName(task.getTeacherName())
                        .courseAttr(task.getCourseAttr())
                        .studentCount(task.getStudentNum())
                        .weekHours(task.getWeeksNumber())
                        .priorityLevel(task.getPriorityLevel())
                        .fixedTime(isFixedTask(task))
                        .fixedTimeSlots(splitClassTimes(task.getClassTime()))
                        .needContinuous(task.getNeedContinuous() != null && task.getNeedContinuous() == 1)
                        .continuousSize(task.getContinuousSize())
                        .teacherForbiddenTimeSlots(task.getTeacherForbiddenTimeSlots())
                        .classForbiddenTimeSlots(task.getClassForbiddenTimeSlots())
                        .teacherMaxDayHours(task.getMaxDayHours())
                        .needSpecialRoom(task.getNeedSpecialRoom() != null && task.getNeedSpecialRoom() == 1)
                        .requiredRoomType(resolveRequiredRoomType(task))
                        .fixedRoomId(task.getFixedRoomId())
                        .campusId(task.getCampusId())
                        .collegeId(task.getCollegeId())
                        .build())
                .toList();
    }

    private List<SchedulingClassroom> buildSchedulingClassrooms() {
        if (resClassroomService == null) {
            return List.of();
        }
        return resClassroomService.list(new LambdaQueryWrapper<ResClassroom>()
                        .eq(ResClassroom::getDeleted, 0)
                        .eq(ResClassroom::getStatus, 1))
                .stream()
                .map(item -> SchedulingClassroom.builder()
                        .classroomId(item.getId())
                        .classroomCode(item.getClassroomCode())
                        .roomType(item.getRoomType())
                        .seatCount(item.getSeatCount())
                        .campusId(item.getCampusId())
                        .collegeId(item.getCollegeId())
                        .build())
                .toList();
    }

    Map<String, Object> buildSchedulingSummary(List<SchedulingTaskInput> schedulingTasks, List<CoursePlan> coursePlanList) {
        Set<String> scheduledTaskKeys = coursePlanList.stream()
                .map(this::buildTaskKey)
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> unscheduledTasks = schedulingTasks.stream()
                .filter(Objects::nonNull)
                .filter(task -> !scheduledTaskKeys.contains(buildTaskKey(task)))
                .map(this::buildUnscheduledTaskReason)
                .toList();

        int taskCount = schedulingTasks.size();
        int unscheduledTaskCount = unscheduledTasks.size();
        int scheduledTaskCount = Math.max(taskCount - unscheduledTaskCount, 0);
        double successRate = taskCount <= 0
                ? 0D
                : BigDecimal.valueOf((double) scheduledTaskCount * 100 / taskCount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("taskCount", taskCount);
        summary.put("scheduledTaskCount", scheduledTaskCount);
        summary.put("unscheduledTaskCount", unscheduledTaskCount);
        summary.put("conflictTaskCount", unscheduledTaskCount);
        summary.put("successRate", successRate);
        summary.put("unscheduledTasks", unscheduledTasks);
        return summary;
    }

    /**
     * 步骤说明：
     * 1. 排课算法当前仍然使用 ClassTask 作为内存输入对象。
     * 2. 任务来源优先切到 sch_task，再转成算法输入对象。
     * 3. 排课执行本身只使用标准任务；缺失标准任务时直接返回空。
     */
    List<SchedulingTaskInput> listSchedulingTasks(String semester) {
        List<SchTask> standardTasks = schTaskService.list(new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getDeleted, 0)
                .like(SchTask::getRemark, "semester=" + semester)
                .orderByAsc(SchTask::getId));
        if (!standardTasks.isEmpty()) {
            List<SchedulingTaskInput> tasks = standardTasks.stream()
                    .map(this::convertStandardTaskToSchedulingTask)
                    .filter(Objects::nonNull)
                    .sorted(Comparator
                            .comparing(SchedulingTaskInput::getPriorityLevel, Comparator.nullsLast(Integer::compareTo))
                            .reversed()
                            .thenComparing(SchedulingTaskInput::getId, Comparator.nullsLast(Integer::compareTo)))
                    .toList();
            enrichCourseRoomRequirements(tasks);
            enrichTeacherHourLimits(tasks);
            enrichClassForbiddenTimeSlots(tasks);
            return tasks;
        }
        log.warn("标准排课任务为空，排课流程不会再回退读取 legacy 排课任务表，semester={}", semester);
        return List.of();
    }

    SchedulingRuntimeContext resolveSchedulingRuntimeContext() {
        ScheduleConfigVO scheduleConfig = scheduleConfigFacadeService == null
                ? null
                : scheduleConfigFacadeService.getScheduleConfig(new ConfigScopeQuery());
        List<String> availableClassTimes = scheduleConfig == null
                ? defaultAvailableClassTimes()
                : scheduleConfig.getTimeSlots().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getIsTeaching() != null && item.getIsTeaching() == 1)
                .filter(item -> item.getIsFixedBreak() == null || item.getIsFixedBreak() == 0)
                .map(this::toLegacyClassTime)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        if (availableClassTimes.isEmpty()) {
            return new SchedulingRuntimeContext("默认 25 格时间片", defaultAvailableClassTimes(), false);
        }
        String ruleName = scheduleConfig != null && scheduleConfig.getScheduleRule() != null
                ? scheduleConfig.getScheduleRule().getRuleName()
                : "默认排课规则";
        return new SchedulingRuntimeContext(ruleName, availableClassTimes, true);
    }

    @Override
    public long countScheduleTasks() {
        return schTaskService.count(new LambdaQueryWrapper<SchTask>().eq(SchTask::getDeleted, 0));
    }

    @Override
    public List<ScheduleRunLogVO> listRecentExecuteLogs(String semester, Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        List<SchScheduleRunLog> logs = schScheduleRunLogService.list(new LambdaQueryWrapper<SchScheduleRunLog>()
                .eq(semester != null && !semester.isBlank(), SchScheduleRunLog::getRemark, semester)
                .eq(SchScheduleRunLog::getDeleted, 0)
                .orderByDesc(SchScheduleRunLog::getCreatedAt)
                .last("limit " + safeLimit));
        if (logs.isEmpty()) {
            return List.of();
        }
        Map<Long, String> operatorNameMap = buildOperatorNameMap(logs.stream()
                .map(SchScheduleRunLog::getOperatorUserId)
                .filter(Objects::nonNull)
                .toList());
        return logs.stream().map(item -> {
            ScheduleRunLogVO vo = new ScheduleRunLogVO();
            vo.setId(item.getId());
            vo.setSemester(item.getRemark());
            vo.setTaskCount(item.getTaskTotal());
            vo.setGeneratedPlanCount(item.getTaskSuccess());
            vo.setStatus("SUCCESS".equalsIgnoreCase(item.getRunStatus()) || "PARTIAL".equalsIgnoreCase(item.getRunStatus()) ? 1 : 0);
            vo.setDurationMs(resolveDurationMs(item));
            vo.setMessage(item.getFailureReason());
            vo.setOperatorUserId(item.getOperatorUserId());
            vo.setOperatorName(operatorNameMap.getOrDefault(item.getOperatorUserId(), "--"));
            vo.setCreateTime(item.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    public ScheduleExecutionDetailVO getExecutionDetail(Long runLogId) {
        SchScheduleRunLog runLog = schScheduleRunLogService.getById(runLogId);
        if (runLog == null || (runLog.getDeleted() != null && runLog.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "执行记录不存在");
        }
        List<UnscheduledTaskDetail> unscheduledTasks = schScheduleRunDetailService.listByRunLogId(runLogId);
        List<SchedulingConstraintSummary> constraintSummary = unscheduledTasks.stream()
                .collect(Collectors.groupingBy(UnscheduledTaskDetail::getReasonCode, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new SchedulingConstraintSummary(
                        entry.getKey(),
                        resolveReasonLabel(entry.getKey()),
                        entry.getValue().intValue()
                ))
                .toList();
        int generatedResultCount = schScheduleResultService.list(new LambdaQueryWrapper<SchScheduleResult>()
                        .eq(SchScheduleResult::getRunLogId, runLogId)
                        .eq(SchScheduleResult::getDeleted, 0))
                .size();
        ScheduleExecutionDetailVO detail = new ScheduleExecutionDetailVO();
        detail.setRunLogId(runLog.getId());
        detail.setSemester(runLog.getRemark());
        detail.setTaskCount(runLog.getTaskTotal());
        detail.setScheduledTaskCount(runLog.getTaskSuccess());
        detail.setUnscheduledTaskCount(runLog.getTaskFailed());
        detail.setSuccessRate(calculateSuccessRate(runLog.getTaskTotal(), runLog.getTaskSuccess()));
        detail.setGeneratedResultCount(generatedResultCount);
        detail.setDurationMs(resolveDurationMs(runLog));
        detail.setRunStatus(runLog.getRunStatus());
        detail.setMessage(runLog.getFailureReason());
        detail.setUnscheduledTasks(unscheduledTasks);
        detail.setConstraintSummary(constraintSummary);
        return detail;
    }

    private Long saveExecuteLog(String semester,
                                int taskCount,
                                int scheduledTaskCount,
                                int unscheduledTaskCount,
                                long duration,
                                String message,
                                String runStatus) {
        SchScheduleRunLog executeLog = new SchScheduleRunLog();
        executeLog.setRunCode("RUN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        executeLog.setTermId(0L);
        executeLog.setRunType("MANUAL");
        executeLog.setAlgorithmType("HYBRID_V2");
        executeLog.setTaskTotal(taskCount);
        executeLog.setTaskSuccess(scheduledTaskCount);
        executeLog.setTaskFailed(unscheduledTaskCount);
        executeLog.setRunStatus(runStatus);
        executeLog.setFailureReason(message);
        executeLog.setStartedAt(resolveStartedAt(duration));
        executeLog.setFinishedAt(java.time.LocalDateTime.now());
        executeLog.setRemark(semester);
        try {
            CurrentUserVO currentUser = authFacadeService == null ? null : authFacadeService.getCurrentUserView();
            if (currentUser != null) {
                executeLog.setOperatorUserId(currentUser.getUserId());
            }
        } catch (Exception exception) {
            log.warn("获取当前操作人失败，排课日志将以匿名方式记录", exception);
        }
        schScheduleRunLogService.save(executeLog);
        return executeLog.getId();
    }

    private String buildExecuteErrorMessage(Exception exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "排课失败，系统未返回明确错误信息";
        }
        return exception.getMessage();
    }

    private BusinessException buildSchedulingException(long start,
                                                       String semester,
                                                       int taskCount,
                                                       String message,
                                                       ResultCode resultCode) {
        long duration = System.currentTimeMillis() - start;
        saveExecuteLog(semester, taskCount, 0, taskCount, duration, message, "FAILED");
        return new BusinessException(resultCode, message);
    }

    private SchedulingTaskInput convertStandardTaskToSchedulingTask(SchTask standardTask) {
        Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(standardTask.getRemark());
        String semester = meta.get("semester");
        String classNo = meta.get("classNo");
        String courseNo = meta.get("courseNo");
        String teacherNo = meta.get("teacherNo");
        if (semester == null || semester.isBlank()
                || classNo == null || classNo.isBlank()
                || courseNo == null || courseNo.isBlank()
                || teacherNo == null || teacherNo.isBlank()) {
            return null;
        }

        SchedulingTaskInput task = new SchedulingTaskInput();
        task.setSemester(semester);
        task.setGradeNo(meta.getOrDefault("gradeNo", ""));
        task.setClassNo(classNo);
        task.setCourseNo(courseNo);
        task.setCourseName(meta.getOrDefault("courseName", ""));
        task.setTeacherNo(teacherNo);
        task.setTeacherName(meta.getOrDefault("teacherName", ""));
        task.setCourseAttr(meta.getOrDefault("courseAttr", ""));
        task.setTaskCode(standardTask.getTaskCode());
        task.setStandardTaskId(standardTask.getId());
        task.setCampusId(standardTask.getCampusId());
        task.setCollegeId(standardTask.getCollegeId());
        task.setStudentNum(standardTask.getStudentCount() == null ? 0 : standardTask.getStudentCount());
        task.setWeeksNumber(standardTask.getWeekHours() == null ? 0 : standardTask.getWeekHours());
        task.setWeeksSum(resolveWeeksSum(standardTask));
        task.setPriorityLevel(normalizePriorityLevel(standardTask.getPriorityLevel()));
        task.setNeedContinuous(standardTask.getNeedContinuous() == null ? 0 : standardTask.getNeedContinuous());
        task.setContinuousSize(standardTask.getContinuousSize() == null ? 1 : standardTask.getContinuousSize());
        task.setNeedFixedRoom(standardTask.getNeedFixedRoom() == null ? 0 : standardTask.getNeedFixedRoom());
        task.setFixedRoomId(standardTask.getFixedRoomId());
        task.setIsFix(standardTask.getNeedFixedTime() != null && standardTask.getNeedFixedTime() == 1 ? "1" : "0");
        task.setClassTime(toLegacyClassTime(standardTask.getFixedWeekdayNo(), standardTask.getFixedPeriodNo()));
        return task;
    }

    private int normalizePriorityLevel(Integer priorityLevel) {
        if (priorityLevel == null) {
            return 5;
        }
        return Math.max(1, Math.min(priorityLevel, 9));
    }

    void enrichTeacherHourLimits(List<SchedulingTaskInput> tasks) {
        if (tasks == null || tasks.isEmpty() || resTeacherService == null) {
            return;
        }
        List<String> teacherCodes = tasks.stream()
                .map(SchedulingTaskInput::getTeacherNo)
                .filter(Objects::nonNull)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
        if (teacherCodes.isEmpty()) {
            return;
        }
        Map<String, ResTeacher> teacherMap = resTeacherService.list(new LambdaQueryWrapper<ResTeacher>()
                        .eq(ResTeacher::getDeleted, 0)
                        .in(ResTeacher::getTeacherCode, teacherCodes))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ResTeacher::getTeacherCode, item -> item, (left, right) -> left));
        tasks.forEach(task -> {
            ResTeacher teacher = teacherMap.get(task.getTeacherNo());
            if (teacher != null) {
                task.setMaxWeekHours(teacher.getMaxWeekHours());
                task.setMaxDayHours(teacher.getMaxDayHours());
                task.setTeacherForbiddenTimeSlots(TeacherForbiddenTimeSlotUtils.parse(teacher.getForbiddenTimeSlots()));
            }
        });
    }

    void enrichCourseRoomRequirements(List<SchedulingTaskInput> tasks) {
        if (tasks == null || tasks.isEmpty() || resCourseService == null) {
            return;
        }
        List<String> courseCodes = tasks.stream()
                .map(SchedulingTaskInput::getCourseNo)
                .filter(Objects::nonNull)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
        if (courseCodes.isEmpty()) {
            return;
        }
        Map<String, ResCourse> courseMap = resCourseService.list(new LambdaQueryWrapper<ResCourse>()
                        .eq(ResCourse::getDeleted, 0)
                        .in(ResCourse::getCourseCode, courseCodes))
                .stream()
                .collect(Collectors.toMap(ResCourse::getCourseCode, item -> item, (left, right) -> left));
        tasks.forEach(task -> {
            ResCourse course = courseMap.get(task.getCourseNo());
            if (course != null) {
                task.setNeedSpecialRoom(course.getNeedSpecialRoom());
                task.setRoomType(course.getRoomType());
                if (task.getNeedContinuous() == null || task.getNeedContinuous() == 0) {
                    task.setNeedContinuous(course.getNeedContinuous());
                }
                if (task.getContinuousSize() == null || task.getContinuousSize() <= 1) {
                    task.setContinuousSize(course.getContinuousSize());
                }
            } else if (task.getRoomType() == null || task.getRoomType().isBlank()) {
                task.setRoomType(mapRoomTypeFromCourseAttr(task.getCourseAttr()));
            }
        });
    }

    void enrichClassForbiddenTimeSlots(List<SchedulingTaskInput> tasks) {
        if (tasks == null || tasks.isEmpty() || adminClassService == null) {
            return;
        }
        List<String> classNos = tasks.stream()
                .map(SchedulingTaskInput::getClassNo)
                .filter(Objects::nonNull)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
        if (classNos.isEmpty()) {
            return;
        }
        Map<String, AdminClassVO> classInfoMap = adminClassService.listClassesByCodes(classNos).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AdminClassVO::getClassNo, item -> item, (left, right) -> left));
        tasks.forEach(task -> {
            AdminClassVO classInfo = classInfoMap.get(task.getClassNo());
            if (classInfo != null) {
                task.setClassForbiddenTimeSlots(ClassForbiddenTimeSlotUtils.parse(classInfo.getForbiddenTimeSlots()));
            }
        });
    }

    private Map<Long, String> buildOperatorNameMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, this::resolveOperatorName, (left, right) -> left));
    }

    private String resolveOperatorName(SysUser user) {
        if (user == null) {
            return "--";
        }
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }

    private Long resolveDurationMs(SchScheduleRunLog logEntity) {
        if (logEntity.getStartedAt() == null || logEntity.getFinishedAt() == null) {
            return 0L;
        }
        return java.time.Duration.between(logEntity.getStartedAt(), logEntity.getFinishedAt()).toMillis();
    }

    private java.time.LocalDateTime resolveStartedAt(long duration) {
        return java.time.LocalDateTime.now().minusNanos(duration * 1_000_000);
    }

    private Integer resolveWeeksSum(SchTask standardTask) {
        if (standardTask.getTotalHours() == null || standardTask.getWeekHours() == null || standardTask.getWeekHours() <= 0) {
            return 0;
        }
        return standardTask.getTotalHours() / standardTask.getWeekHours();
    }

    private String buildSchedulingSummaryMessage(Map<String, Object> schedulingSummary, int generatedPlanCount) {
        int taskCount = asInt(schedulingSummary.get("taskCount"));
        int scheduledTaskCount = asInt(schedulingSummary.get("scheduledTaskCount"));
        int unscheduledTaskCount = asInt(schedulingSummary.get("unscheduledTaskCount"));
        if (unscheduledTaskCount > 0) {
            return String.format("排课完成，生成 %s 条课表记录，成功 %s/%s 个任务，未完成 %s 个任务",
                    generatedPlanCount, scheduledTaskCount, taskCount, unscheduledTaskCount);
        }
        return String.format("排课成功，生成 %s 条课表记录，成功覆盖 %s 个任务",
                generatedPlanCount, scheduledTaskCount);
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private double calculateSuccessRate(Integer taskCount, Integer scheduledTaskCount) {
        int safeTaskCount = taskCount == null ? 0 : taskCount;
        int safeScheduledCount = scheduledTaskCount == null ? 0 : scheduledTaskCount;
        if (safeTaskCount <= 0) {
            return 0D;
        }
        return BigDecimal.valueOf((double) safeScheduledCount * 100 / safeTaskCount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String resolveReasonLabel(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return "没有可用时间片";
        }
        try {
            return com.lyk.coursearrange.schedule.engine.model.SchedulingFailureCode.valueOf(reasonCode).getDefaultMessage();
        } catch (IllegalArgumentException exception) {
            return reasonCode;
        }
    }

    private String resolveRequiredRoomType(SchedulingTaskInput task) {
        if (task.getRoomType() != null && !task.getRoomType().isBlank()) {
            return task.getRoomType();
        }
        return mapRoomTypeFromCourseAttr(task.getCourseAttr());
    }

    private String mapRoomTypeFromCourseAttr(String courseAttr) {
        if (courseAttr == null || courseAttr.isBlank()) {
            return "NORMAL";
        }
        String normalized = courseAttr.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("LAB") || normalized.contains("实验") || ConstantInfo.EXPERIMENT_COURSE.equals(courseAttr)) {
            return "LAB";
        }
        if (normalized.contains("COMPUTER") || normalized.contains("信息") || ConstantInfo.TECHNOLOGY_COURSE.equals(courseAttr)) {
            return "COMPUTER";
        }
        if (normalized.contains("体育") || ConstantInfo.PHYSICAL_COURSE.equals(courseAttr)) {
            return "SPORT";
        }
        if (normalized.contains("音乐") || normalized.contains("舞蹈")
                || ConstantInfo.MUSIC_COURSE.equals(courseAttr)
                || ConstantInfo.DANCE_COURSE.equals(courseAttr)) {
            return "SPECIAL";
        }
        return "NORMAL";
    }

    private String buildTaskKey(SchedulingTaskInput task) {
        if (task == null) {
            return "";
        }
        return buildTaskKey(task.getClassNo(), task.getCourseNo(), task.getTeacherNo());
    }

    private String buildTaskKey(CoursePlan coursePlan) {
        if (coursePlan == null) {
            return "";
        }
        return buildTaskKey(coursePlan.getClassNo(), coursePlan.getCourseNo(), coursePlan.getTeacherNo());
    }

    private String buildTaskKey(String classNo, String courseNo, String teacherNo) {
        if (classNo == null || courseNo == null || teacherNo == null) {
            return "";
        }
        return String.join("::", classNo, courseNo, teacherNo);
    }

    private String buildUnscheduledTaskReason(SchedulingTaskInput task) {
        String courseName = task.getCourseName() == null || task.getCourseName().isBlank()
                ? task.getCourseNo()
                : task.getCourseName();
        String teacherName = task.getTeacherName() == null || task.getTeacherName().isBlank()
                ? task.getTeacherNo()
                : task.getTeacherName();
        return String.format("%s / %s / %s：未生成对应课表记录，请检查固定时间、教师冲突与教室容量约束",
                task.getClassNo(), courseName, teacherName);
    }

    private String toLegacyClassTime(Integer weekdayNo, Integer periodNo) {
        if (weekdayNo == null || periodNo == null || weekdayNo <= 0 || periodNo <= 0) {
            return "";
        }
        return String.format("%02d", (weekdayNo - 1) * 5 + periodNo);
    }

    private void checkWeeksNumber(List<SchedulingTaskInput> classTaskList) {
        checkWeeksNumber(classTaskList, defaultAvailableClassTimes());
    }

    void checkWeeksNumber(List<SchedulingTaskInput> classTaskList, List<String> availableClassTimes) {
        int maxWeeklyHours = Math.max(availableClassTimes.size(), 1) * 2;
        classTaskList.stream().collect(Collectors.groupingBy(SchedulingTaskInput::getClassNo)).forEach((k, v) -> {
            int sum = v.stream().mapToInt(SchedulingTaskInput::getWeeksNumber).sum();
            if (sum > maxWeeklyHours) {
                throw new CourseArrangeException(String.format("班级：%s 的学时超过 %s，不能排课！", k, maxWeeklyHours));
            }
        });
    }

    /**
     * 是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间
     */
    private void checkConflict(Map<String, List<String>> individualMap) {

        Map<String, Map<String, List<String>>> classMap = new HashMap<>();

        // 遍历map 中的每个班级并判断里面同一个course_no、teacher_no 下是否有两个一样的 class_time
        for (Map.Entry<String, List<String>> entry : individualMap.entrySet()) {

            List<String> geneList = entry.getValue();

            // 遍历 geneList并过滤同一个course_no、teacher_no 下是否有两个一样的 class_time
            Map<String, List<String>> map = new HashMap<>();
            for (String gene : geneList) {
                String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
                String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
                String key = teacherNo + "--" + classTime;
                if (!map.containsKey(key)) {
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(gene);
            }

            // 每个班级的冲突 map
            classMap.put(entry.getKey(), map);
        }

        for (Map.Entry<String, Map<String, List<String>>> entry : classMap.entrySet()) {
            Map<String, List<String>> conflictMap = entry.getValue();
            log.info("遍历 {} 班", entry.getKey());
            for (Map.Entry<String, List<String>> e : conflictMap.entrySet()) {
                String key = e.getKey();
                if (e.getValue().size() > 1) {
                    log.error("出现冲突 {}", key);
                    e.getValue().stream().map(item -> item.substring(11, 22) + "-" + item.substring(24, 26))
                            .collect(Collectors.toList()).forEach(i -> log.error("冲突的课程：{}", i));
                }
            }
            log.info("完成遍历 {} 班", entry.getKey());
        }
    }


    /**
     * 开始给进化完的基因编码分配教室，即在原来的编码中加上教室编号
     */
    List<String> finalResult(Map<String, List<String>> individualMap) {
        List<String> resultList = new ArrayList<>();
        List<String> resultGeneList = collectGene(individualMap);
        if (resultGeneList.isEmpty()) {
            return resultList;
        }
        Map<String, AdminClassVO> adminClassMap = loadAdminClassMap(resultGeneList);
        List<StandardClassroomOption> standardClassrooms = listStandardClassrooms();
        for (String gene : resultGeneList) {
            String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
            AdminClassVO adminClass = adminClassMap.get(classNo);
            int studentNum = adminClass == null || adminClass.getNum() == null ? 0 : adminClass.getNum();
            String classroomNo = issueClassroom(gene, studentNum, adminClass, standardClassrooms, resultList);
            resultList.add(gene + classroomNo);
        }
        return resultList;
    }

    private Map<String, AdminClassVO> loadAdminClassMap(List<String> resultGeneList) {
        if (adminClassService == null || resultGeneList == null || resultGeneList.isEmpty()) {
            return Map.of();
        }
        List<String> classNos = resultGeneList.stream()
                .map(gene -> ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene))
                .filter(Objects::nonNull)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
        if (classNos.isEmpty()) {
            return Map.of();
        }
        return adminClassService.listClassesByCodes(classNos).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AdminClassVO::getClassNo, item -> item, (left, right) -> left));
    }

    private List<StandardClassroomOption> listStandardClassrooms() {
        if (resClassroomService == null) {
            return List.of();
        }
        return resClassroomService.list(new LambdaQueryWrapper<ResClassroom>()
                        .eq(ResClassroom::getDeleted, 0)
                        .eq(ResClassroom::getStatus, 1)
                        .orderByAsc(ResClassroom::getCampusId)
                        .orderByAsc(ResClassroom::getCollegeId)
                        .orderByAsc(ResClassroom::getClassroomCode))
                .stream()
                .filter(Objects::nonNull)
                .map(this::toStandardClassroomOption)
                .toList();
    }

    private StandardClassroomOption toStandardClassroomOption(ResClassroom classroom) {
        StandardClassroomOption option = new StandardClassroomOption();
        option.setClassroomCode(classroom.getClassroomCode());
        option.setCampusId(classroom.getCampusId());
        option.setCollegeId(classroom.getCollegeId());
        option.setRoomType(normalizeRoomType(classroom.getRoomType()));
        option.setSeatCount(classroom.getSeatCount() == null ? 0 : classroom.getSeatCount());
        return option;
    }

    /**
     * 给不同的基因编码分配教室
     *
     * @param gene 需要分配教室的基因编码
     */
    private String issueClassroom(String gene,
                                  int studentNum,
                                  AdminClassVO adminClass,
                                  List<StandardClassroomOption> standardClassrooms,
                                  List<String> resultList) {
        List<StandardClassroomOption> classroomOptions = resolveCandidateClassrooms(gene, adminClass, standardClassrooms);
        return chooseClassroom(studentNum, gene, classroomOptions, resultList);
    }

    private List<StandardClassroomOption> resolveCandidateClassrooms(String gene,
                                                                     AdminClassVO adminClass,
                                                                     List<StandardClassroomOption> standardClassrooms) {
        if (standardClassrooms == null || standardClassrooms.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "标准教室资源为空，无法完成排课");
        }
        Set<String> requiredRoomTypes = resolveRequiredRoomTypes(ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene));
        List<StandardClassroomOption> typeMatched = standardClassrooms.stream()
                .filter(item -> requiredRoomTypes.contains(item.getRoomType()))
                .sorted(Comparator
                        .comparing(StandardClassroomOption::getSeatCount, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(StandardClassroomOption::getClassroomCode, Comparator.nullsLast(String::compareTo)))
                .toList();
        if (typeMatched.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    String.format("标准教室资源不足，课程属性 %s 没有可用教室类型", ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene)));
        }
        List<StandardClassroomOption> scoped = filterByScope(typeMatched, adminClass);
        return scoped.isEmpty() ? typeMatched : scoped;
    }

    private List<StandardClassroomOption> filterByScope(List<StandardClassroomOption> classroomOptions, AdminClassVO adminClass) {
        if (adminClass == null || classroomOptions == null || classroomOptions.isEmpty()) {
            return classroomOptions == null ? List.of() : classroomOptions;
        }
        List<StandardClassroomOption> exactScope = classroomOptions.stream()
                .filter(item -> Objects.equals(item.getCampusId(), adminClass.getCampusId()))
                .filter(item -> adminClass.getCollegeId() == null || Objects.equals(item.getCollegeId(), adminClass.getCollegeId()))
                .toList();
        if (!exactScope.isEmpty()) {
            return exactScope;
        }
        if (adminClass.getCampusId() == null) {
            return classroomOptions;
        }
        List<StandardClassroomOption> campusScope = classroomOptions.stream()
                .filter(item -> Objects.equals(item.getCampusId(), adminClass.getCampusId()))
                .toList();
        return campusScope.isEmpty() ? classroomOptions : campusScope;
    }

    private Set<String> resolveRequiredRoomTypes(String courseAttr) {
        if (ConstantInfo.EXPERIMENT_COURSE.equals(courseAttr)) {
            return Set.of("LAB");
        }
        if (ConstantInfo.PHYSICAL_COURSE.equals(courseAttr)) {
            return Set.of("SPORT");
        }
        if (ConstantInfo.TECHNOLOGY_COURSE.equals(courseAttr)) {
            return Set.of("COMPUTER");
        }
        if (ConstantInfo.MUSIC_COURSE.equals(courseAttr) || ConstantInfo.DANCE_COURSE.equals(courseAttr)) {
            return Set.of("SPECIAL");
        }
        return Set.of("NORMAL", "MULTIMEDIA");
    }

    private String normalizeRoomType(String roomType) {
        return roomType == null || roomType.isBlank() ? "NORMAL" : roomType.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 给不同课程的基因编码随机选择一个教室
     *
     * @param studentNum    开课的班级的学生人数
     * @param gene          需要安排教室的基因编码
     * @param classroomList 教室
     */
    private String chooseClassroom(int studentNum, String gene, List<StandardClassroomOption> classroomList, List<String> resultList) {
        if (classroomList == null || classroomList.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    String.format("班级 %s 缺少可分配的标准教室资源",
                            ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene)));
        }
        for (StandardClassroomOption classroom : classroomList) {
            if (judgeClassroom(studentNum, gene, classroom, resultList)) {
                return classroom.getClassroomCode();
            }
        }
        throw new BusinessException(ResultCode.BUSINESS_ERROR,
                String.format("班级 %s 在时间片 %s 没有满足容量与占用约束的标准教室",
                        ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene),
                        ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene)));
    }

    /**
     * 判断教室是否符合上课班级所需
     * 即：不同属性的课要放在对应属性的教室上课
     * @param classroom  随机分配教室
     */
    private boolean judgeClassroom(int studentNum, String gene, StandardClassroomOption classroom, List<String> resultList) {
        if (classroom == null) {
            return false;
        }
        if (classroom.getSeatCount() != null && classroom.getSeatCount() < studentNum) {
            return false;
        }
        return isFree(gene, resultList, classroom);
    }

     /**
     * 判断同一时间同一个教室是否有多个班级使用
     */
    private Boolean isFree(String gene, List<String> resultList, StandardClassroomOption classroom) {
        if (resultList.isEmpty()) {
            return true;
        } else {
            for (String resultGene : resultList) {
                if (ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, resultGene).equals(classroom.getClassroomCode())
                        && (ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene).equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, resultGene)))) {
                        return false;
                }
            }
        }
        return true;
    }

    private static final class StandardClassroomOption {

        private String classroomCode;
        private Long campusId;
        private Long collegeId;
        private String roomType;
        private Integer seatCount;

        public String getClassroomCode() {
            return classroomCode;
        }

        public void setClassroomCode(String classroomCode) {
            this.classroomCode = classroomCode;
        }

        public Long getCampusId() {
            return campusId;
        }

        public void setCampusId(Long campusId) {
            this.campusId = campusId;
        }

        public Long getCollegeId() {
            return collegeId;
        }

        public void setCollegeId(Long collegeId) {
            this.collegeId = collegeId;
        }

        public String getRoomType() {
            return roomType;
        }

        public void setRoomType(String roomType) {
            this.roomType = roomType;
        }

        public Integer getSeatCount() {
            return seatCount;
        }

        public void setSeatCount(Integer seatCount) {
            this.seatCount = seatCount;
        }
    }

    /**
     * 将所有的基因编码按照年级分类
     */
    private Map<String, List<String>> collectGeneByGrade(List<String> resultGeneList, List<String> gradeList) {
        Map<String, List<String>> map = new HashMap<>();
        for (String gradeNo : gradeList) {
            List<String> geneList = new ArrayList<>();
            // 找到基因编码集合中相应的年级并归类
            for (String gene : resultGeneList) {
                if (ClassUtil.cutGene(ConstantInfo.GRADE_NO, gene).equals(gradeNo)) {
                    // 将当前的年级的基因编码加入集合
                    geneList.add(gene);
                }
            }
            // 将当前年级对应的编码集合放入集合
            if (!geneList.isEmpty()) {
                map.put(gradeNo, geneList);
            }
        }
        // 得到不同年级的基因编码(年级，编码集合)
        return map;
    }

    /**
     * 遗传进化(每个班级中多条基因编码)
     * 步骤：
     * 1、初始化种群
     * 2、交叉，选择
     * 3、变异
     * 4、重复2,3步骤
     * 5、直到达到终止条件
     *
     * @param individualMap 按班级分的基因编码
     */
    private Map<String, List<String>> geneticEvolution(Map<String, List<String>> individualMap,
                                                       List<String> availableClassTimes,
                                                       Map<String, List<String>> teacherForbiddenTimeSlots,
                                                       Map<String, List<String>> classForbiddenTimeSlots,
                                                       Map<String, Integer> teacherMaxDayHours) {
        List<String> resultGeneList;

        for (int i = 0; i < ConstantInfo.GENERATION; ++i) {
            hybridization(individualMap);
            List<String> allIndividual = collectGene(individualMap);
            resultGeneList = geneMutation(allIndividual, availableClassTimes);
            List<String> list = conflictResolution(resultGeneList, availableClassTimes, teacherForbiddenTimeSlots, classForbiddenTimeSlots, teacherMaxDayHours);
            individualMap.clear();
            individualMap = transformIndividual(list);
        }

        return individualMap;
    }


    /**
     * 冲突消除,同一个讲师同一时间上多门课。解决：重新分配一个时间，直到所有的基因编码中
     * 不再存在上课时间冲突为止
     * 因素：讲师-课程-时间-教室
     *
     * @param resultGeneList 所有个体集合 （大种群）
     */
    private List<String> conflictResolution(List<String> resultGeneList,
                                            List<String> availableClassTimes,
                                            Map<String, List<String>> teacherForbiddenTimeSlots,
                                            Map<String, List<String>> classForbiddenTimeSlots,
                                            Map<String, Integer> teacherMaxDayHours) {
        int conflictTimes = 0;
        exit:
        for (int i = 0; i < resultGeneList.size(); i++) {
            String gene = resultGeneList.get(i);
            String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
            String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
            String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);

            for (int j = i + 1; j < resultGeneList.size(); j++) {
                String tempGene = resultGeneList.get(j);
                String tempTeacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, tempGene);
                String tempClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, tempGene);
                String tempClassNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, tempGene);
                // 冲突检测
                if (classTime.equals(tempClassTime) && classNo.equals(tempClassNo)) {
                    log.error("一个班级在同一时间上上多门课 {}", conflictTimes++);

                    String newClassTime = ClassUtil.randomTimeForClassConflict(gene, resultGeneList, classNo, teacherNo, classTime, availableClassTimes);

                    replaceConflictTime(resultGeneList, tempGene, newClassTime);

                    continue exit;
                } else if (classTime.equals(tempClassTime) && teacherNo.equals(tempTeacherNo)) {
                    log.error("同一个老师在同一时间上上多门课 {}", conflictTimes++);
                    String newClassTime = ClassUtil.randomTimeForTeacherConflict(gene, resultGeneList, teacherNo, classNo, availableClassTimes);
                    replaceConflictTime(resultGeneList, tempGene, newClassTime);
                    continue exit;
                }
            }
        }
        log.error("冲突发生次数: {}", conflictTimes);
        resultGeneList = enforceClassForbiddenTimeSlots(resultGeneList, classForbiddenTimeSlots, availableClassTimes);
        resultGeneList = enforceTeacherForbiddenTimeSlots(resultGeneList, teacherForbiddenTimeSlots, availableClassTimes);
        return enforceTeacherDayHourLimit(resultGeneList, teacherMaxDayHours, availableClassTimes);
    }

    private List<String> conflictResolution1(List<String> resultGeneList) {
        int conflictTimes = 0;
        eitx:
        for (int i = 0; i < resultGeneList.size(); i++) {
            String gene = resultGeneList.get(i);
            String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
            String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
            String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
            for (int j = i + 1; j < resultGeneList.size(); j++) {
                String tempGene = resultGeneList.get(j);
                String tempTeacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, tempGene);
                String tempClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, tempGene);
                String tempClassNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, tempGene);
                // 冲突检测
                if (classTime.equals(tempClassTime) && (classNo.equals(tempClassNo) || teacherNo.equals(tempTeacherNo))) {
                        log.debug("出现冲突情况: {}", conflictTimes++);
                        String newClassTime = ClassUtil.randomTime();
                        String newGene = gene.substring(0, 24) + newClassTime;
                        replace(resultGeneList, tempGene, newGene);
                        continue eitx;

                }
            }
        }
        log.error("冲突发生次数: {}", conflictTimes);
        return resultGeneList;
    }

    private void replaceConflictTime(List<String> resultGeneList, String gene, String newClassTime) {
        String newGene = gene.substring(0, 24) + newClassTime;

        // 替换新的随机时间给剩余大种群里面的编码
        replace(resultGeneList, gene, newGene);
    }

    /**
     * 替换基因编码
     */
    private void replace(List<String> resultGeneList, String oldGene, String newGene) {
        for (int i = 0; i < resultGeneList.size(); i++) {
            if (resultGeneList.get(i).equals(oldGene)) {
                resultGeneList.set(i, newGene);
                log.info("替换冲突时间");
                break;
            }
        }
    }


    /**
     * 重新合拢交叉后的个体,即不分班级的基因编码，得到所有的编码
     */
    private List<String> collectGene(Map<String, List<String>> individualMap) {
        List<String> resultList = new ArrayList<>();
        for (List<String> individualList : individualMap.values()) {
            resultList.addAll(individualList);
        }
        return resultList;
    }

    /**
     * 基因变异
     */
    private List<String> geneMutation(List<String> resultGeneList, List<String> availableClassTimes) {
        final double mutationRate = 0.005;
        int mutationNumber = (int) (resultGeneList.size() * mutationRate);

        if (mutationNumber < 1) {
            mutationNumber = 1;
        }

        for (int i = 0; i < mutationNumber; i++) {
            int randomIndex = ClassUtil.RANDOM.nextInt(resultGeneList.size());
            String gene = resultGeneList.get(randomIndex);
            if (ClassUtil.cutGene(ConstantInfo.IS_FIX, gene).equals(ConstantInfo.FIX_TIME_FLAG)) {
                log.debug("固定时间的不会发生变异！！{} {}", ClassUtil.cutGene(gene, ConstantInfo.COURSE_NO), ClassUtil.cutGene(gene, ConstantInfo.CLASS_TIME));
                break;
            } else {
                String newClassTime = ClassUtil.randomTime(availableClassTimes);
                gene = gene.substring(0, 24) + newClassTime;
                resultGeneList.remove(randomIndex);
                resultGeneList.add(randomIndex, gene);
            }
        }
        return resultGeneList;
    }

    /**
     * 给每个班级交叉：一个班级看作一个种群
     */
    private Map<String, List<String>> hybridization(Map<String, List<String>> individualMap) {
        for (Map.Entry<String, List<String>> entry : individualMap.entrySet()) {
            String classNo = entry.getKey();
            List<String> individualList = individualMap.get(classNo);
            List<String> oldIndividualList = new ArrayList<>(individualList);

            selectGene(individualList);

            // 计算并对比子父代的适应度值，高的留下进行下一代遗传，相当于进化，
            if (ClassUtil.calculateExpectedValue(individualList) >= ClassUtil.calculateExpectedValue(oldIndividualList)) {
                individualMap.put(classNo, individualList);
            } else {
                individualMap.put(classNo, oldIndividualList);
            }
        }
        return individualMap;
    }


    /**
     * 个体中随机选择基因进行交叉(交换上课时间)
     */
    private List<String> selectGene(List<String> individualList) {
        int individualListSize = individualList.size();
        boolean flag;
        do {
            int firstIndex = ClassUtil.RANDOM.nextInt(individualListSize);
            int secondIndex = ClassUtil.RANDOM.nextInt(individualListSize);

            String firstGene = individualList.get(firstIndex);
            String secondGene = individualList.get(secondIndex);

            if (firstIndex == secondIndex) {
                flag = false;
            } else if (ClassUtil.cutGene(ConstantInfo.IS_FIX, firstGene).equals(ConstantInfo.FIX_TIME_FLAG)
                    || ClassUtil.cutGene(ConstantInfo.IS_FIX, secondGene).equals(ConstantInfo.FIX_TIME_FLAG)) {
                flag = false;
            } else {
                String firstClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, firstGene);
                String secondClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, secondGene);
                firstGene = firstGene.substring(0, 24) + secondClassTime;
                secondGene = secondGene.substring(0, 24) + firstClassTime;
                individualList.set(firstIndex, firstGene);
                individualList.set(secondIndex, secondGene);
                flag = true;
            }
        } while (!flag);
        return individualList;
    }


    /**
     * 编码规则: （位）
     * 固定时间：1
     * 年级编号：2
     * 班级编号：8
     * 讲师编号：5
     * 课程编号：6
     * 课程属性：2
     * 上课时间：2
     * 教室编号：6
     * <p>
     * 编码规则为：是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间
     */
    private Map<String, List<String>> coding(List<SchedulingTaskInput> classTaskList) {
        return coding(classTaskList, defaultAvailableClassTimes());
    }

    Map<String, List<String>> coding(List<SchedulingTaskInput> classTaskList, List<String> availableClassTimes) {
        Map<String, List<String>> geneMap = new HashMap<>();
        List<String> unFixedTimeGeneList = new ArrayList<>();
        List<String> fixedTimeGeneList = new ArrayList<>();

        for (SchedulingTaskInput classTask : classTaskList) {
            // 1，不固定上课时间，默认默认不再填充 00
            if (isUnfixedTask(classTask)) {
                // 得到每周上课的节数，因为设定2学时为一节课
                int size = classTask.getWeeksNumber() / 2;
                for (int i = 0; i < size; i++) {
                    String gene = ConstantInfo.UN_FIX_TIME_FLAG + classTask.getGradeNo() + classTask.getClassNo()
                            + classTask.getTeacherNo() + classTask.getCourseNo() + classTask.getCourseAttr();

                    unFixedTimeGeneList.add(gene);
                }
            }
            // 2,固定上课时间
            if (isFixedTask(classTask)) {
                int size = classTask.getWeeksNumber() / 2;
                for (int i = 0; i < size; i++) {
                    // 获得设定的固定时间：04 07
                    String classTime = classTask.getClassTime().substring(i * 2, (i + 1) * 2);
                    if (!availableClassTimes.contains(classTime)) {
                        throw new BusinessException(ResultCode.BUSINESS_ERROR,
                                String.format("固定时间编码 %s 不在当前排课规则允许的时间片内", classTime));
                    }
                    String gene = ConstantInfo.FIX_TIME_FLAG + classTask.getGradeNo() + classTask.getClassNo()
                            + classTask.getTeacherNo() + classTask.getCourseNo() + classTask.getCourseAttr() + classTime;
                    fixedTimeGeneList.add(gene);
                }
            }
        }
        geneMap.put(ConstantInfo.UN_FIXED_TIME, unFixedTimeGeneList);
        geneMap.put(ConstantInfo.IS_FIX_TIME, fixedTimeGeneList);

        return geneMap;
    }

    /**
     * 给初始基因编码随机分配时间(那些不固定上课时间的课程)
     *
     * @param geneMap 固定时间与不固定时间的编码集合
     */
    private List<String> codingTime(Map<String, List<String>> geneMap, List<String> availableClassTimes) {

        List<String> fixedTimeGeneList = geneMap.get(ConstantInfo.IS_FIX_TIME);
        List<String> unFixedTimeGeneList = geneMap.get(ConstantInfo.UN_FIXED_TIME);
        List<String> resultGeneList = new ArrayList<>(fixedTimeGeneList);

        for (String gene : unFixedTimeGeneList) {
            String classTime = ClassUtil.randomTime(availableClassTimes);
            gene = gene + classTime;
            resultGeneList.add(gene);
        }
        return resultGeneList;
    }

    void validateTeacherForbiddenTimeSlots(List<SchedulingTaskInput> schedulingTasks) {
        if (schedulingTasks == null || schedulingTasks.isEmpty()) {
            return;
        }
        for (SchedulingTaskInput task : schedulingTasks) {
            if (task == null || !isFixedTask(task)) {
                continue;
            }
            List<String> forbiddenTimeSlots = task.getTeacherForbiddenTimeSlots();
            if (forbiddenTimeSlots == null || forbiddenTimeSlots.isEmpty()) {
                continue;
            }
            for (String classTime : splitClassTimes(task.getClassTime())) {
                if (forbiddenTimeSlots.contains(classTime)) {
                    String teacherName = task.getTeacherName() == null || task.getTeacherName().isBlank()
                            ? task.getTeacherNo()
                            : task.getTeacherName();
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("教师 %s 在时间片 %s 已配置禁排，请调整固定时间或教师约束", teacherName, classTime));
                }
            }
        }
    }

    Map<String, List<String>> resolveTeacherForbiddenTimeSlots(List<SchedulingTaskInput> schedulingTasks) {
        if (schedulingTasks == null || schedulingTasks.isEmpty()) {
            return Map.of();
        }
        return schedulingTasks.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getTeacherNo() != null && !item.getTeacherNo().isBlank())
                .filter(item -> item.getTeacherForbiddenTimeSlots() != null && !item.getTeacherForbiddenTimeSlots().isEmpty())
                .collect(Collectors.toMap(
                        SchedulingTaskInput::getTeacherNo,
                        item -> new ArrayList<>(item.getTeacherForbiddenTimeSlots()),
                        (left, right) -> {
                            LinkedHashSet<String> merged = new LinkedHashSet<>(left);
                            merged.addAll(right);
                            return new ArrayList<>(merged);
                        },
                        LinkedHashMap::new
                ));
    }

    void validateClassForbiddenTimeSlots(List<SchedulingTaskInput> schedulingTasks) {
        if (schedulingTasks == null || schedulingTasks.isEmpty()) {
            return;
        }
        for (SchedulingTaskInput task : schedulingTasks) {
            if (task == null || !isFixedTask(task)) {
                continue;
            }
            List<String> forbiddenTimeSlots = task.getClassForbiddenTimeSlots();
            if (forbiddenTimeSlots == null || forbiddenTimeSlots.isEmpty()) {
                continue;
            }
            for (String classTime : splitClassTimes(task.getClassTime())) {
                if (forbiddenTimeSlots.contains(classTime)) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("班级 %s 在时间片 %s 已配置禁排，请调整固定时间或班级约束", task.getClassNo(), classTime));
                }
            }
        }
    }

    Map<String, List<String>> resolveClassForbiddenTimeSlots(List<SchedulingTaskInput> schedulingTasks) {
        if (schedulingTasks == null || schedulingTasks.isEmpty()) {
            return Map.of();
        }
        return schedulingTasks.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getClassNo() != null && !item.getClassNo().isBlank())
                .filter(item -> item.getClassForbiddenTimeSlots() != null && !item.getClassForbiddenTimeSlots().isEmpty())
                .collect(Collectors.toMap(
                        SchedulingTaskInput::getClassNo,
                        item -> new ArrayList<>(item.getClassForbiddenTimeSlots()),
                        (left, right) -> {
                            LinkedHashSet<String> merged = new LinkedHashSet<>(left);
                            merged.addAll(right);
                            return new ArrayList<>(merged);
                        },
                        LinkedHashMap::new
                ));
    }

    void validateTeacherDayHourLimit(List<SchedulingTaskInput> schedulingTasks) {
        if (schedulingTasks == null || schedulingTasks.isEmpty()) {
            return;
        }
        Map<String, Integer> teacherDayCount = new HashMap<>();
        for (SchedulingTaskInput task : schedulingTasks) {
            if (task == null || !isFixedTask(task)) {
                continue;
            }
            int maxDayHours = task.getMaxDayHours() == null ? 0 : task.getMaxDayHours();
            if (maxDayHours <= 0 || task.getClassTime() == null || task.getClassTime().isBlank()) {
                continue;
            }
            for (String classTime : splitClassTimes(task.getClassTime())) {
                String key = buildTeacherWeekdayKey(task.getTeacherNo(), classTime);
                int nextCount = teacherDayCount.getOrDefault(key, 0) + 1;
                if (nextCount > maxDayHours) {
                    String teacherName = task.getTeacherName() == null || task.getTeacherName().isBlank()
                            ? task.getTeacherNo()
                            : task.getTeacherName();
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("教师 %s 在同一天的固定课已超过日上限课时 %s，请调整任务或教师配置", teacherName, maxDayHours));
                }
                teacherDayCount.put(key, nextCount);
            }
        }
    }

    Map<String, Integer> resolveTeacherMaxDayHours(List<SchedulingTaskInput> schedulingTasks) {
        if (schedulingTasks == null || schedulingTasks.isEmpty()) {
            return Map.of();
        }
        return schedulingTasks.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getTeacherNo() != null && !item.getTeacherNo().isBlank())
                .filter(item -> item.getMaxDayHours() != null && item.getMaxDayHours() > 0)
                .collect(Collectors.toMap(SchedulingTaskInput::getTeacherNo, SchedulingTaskInput::getMaxDayHours, Math::min, LinkedHashMap::new));
    }

    List<String> enforceTeacherDayHourLimit(List<String> resultGeneList,
                                            Map<String, Integer> teacherMaxDayHours,
                                            List<String> availableClassTimes) {
        if (resultGeneList == null || resultGeneList.isEmpty() || teacherMaxDayHours == null || teacherMaxDayHours.isEmpty()) {
            return resultGeneList;
        }
        boolean adjusted;
        int guard = 0;
        do {
            adjusted = false;
            Map<String, List<String>> teacherDayGenes = resultGeneList.stream()
                    .collect(Collectors.groupingBy(gene -> buildTeacherWeekdayKey(
                            ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene),
                            ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene))));
            for (Map.Entry<String, List<String>> entry : teacherDayGenes.entrySet()) {
                String teacherNo = entry.getKey().split("::")[0];
                int limit = teacherMaxDayHours.getOrDefault(teacherNo, 0);
                if (limit <= 0 || entry.getValue().size() <= limit) {
                    continue;
                }
                List<String> movableGenes = entry.getValue().stream()
                        .filter(gene -> !ConstantInfo.FIX_TIME_FLAG.equals(ClassUtil.cutGene(ConstantInfo.IS_FIX, gene)))
                        .toList();
                if (movableGenes.isEmpty()) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("教师 %s 的固定排课已经超过日上限课时 %s，请调整固定时间任务或教师配置", teacherNo, limit));
                }
                String geneToMove = movableGenes.get(movableGenes.size() - 1);
                String newClassTime = pickClassTimeForTeacherDayLimit(geneToMove, resultGeneList, teacherMaxDayHours, availableClassTimes);
                if (Objects.equals(newClassTime, ClassUtil.cutGene(ConstantInfo.CLASS_TIME, geneToMove))) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("教师 %s 无法在当前时间片配置下满足日上限课时 %s，请调整任务量、教师上限或时间片模板", teacherNo, limit));
                }
                replaceConflictTime(resultGeneList, geneToMove, newClassTime);
                adjusted = true;
                break;
            }
            guard++;
        } while (adjusted && guard < 500);
        return resultGeneList;
    }

    List<String> enforceTeacherForbiddenTimeSlots(List<String> resultGeneList,
                                                  Map<String, List<String>> teacherForbiddenTimeSlots,
                                                  List<String> availableClassTimes) {
        if (resultGeneList == null || resultGeneList.isEmpty()
                || teacherForbiddenTimeSlots == null || teacherForbiddenTimeSlots.isEmpty()) {
            return resultGeneList;
        }
        boolean adjusted;
        int guard = 0;
        do {
            adjusted = false;
            for (String gene : new ArrayList<>(resultGeneList)) {
                String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
                List<String> forbiddenSlots = teacherForbiddenTimeSlots.getOrDefault(teacherNo, List.of());
                if (forbiddenSlots.isEmpty()) {
                    continue;
                }
                String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
                if (!forbiddenSlots.contains(classTime)) {
                    continue;
                }
                if (ConstantInfo.FIX_TIME_FLAG.equals(ClassUtil.cutGene(ConstantInfo.IS_FIX, gene))) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("教师 %s 在时间片 %s 已配置禁排，固定排课无法继续执行", teacherNo, classTime));
                }
                String newClassTime = pickClassTimeForTeacherForbiddenSlot(gene, resultGeneList, teacherForbiddenTimeSlots, availableClassTimes);
                if (Objects.equals(newClassTime, classTime)) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("教师 %s 的禁排时间过多，当前时间片模板下无法为任务分配可用时间", teacherNo));
                }
                replaceConflictTime(resultGeneList, gene, newClassTime);
                adjusted = true;
                break;
            }
            guard++;
        } while (adjusted && guard < 500);
        return resultGeneList;
    }

    List<String> enforceClassForbiddenTimeSlots(List<String> resultGeneList,
                                                Map<String, List<String>> classForbiddenTimeSlots,
                                                List<String> availableClassTimes) {
        if (resultGeneList == null || resultGeneList.isEmpty()
                || classForbiddenTimeSlots == null || classForbiddenTimeSlots.isEmpty()) {
            return resultGeneList;
        }
        boolean adjusted;
        int guard = 0;
        do {
            adjusted = false;
            for (String gene : new ArrayList<>(resultGeneList)) {
                String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
                List<String> forbiddenSlots = classForbiddenTimeSlots.getOrDefault(classNo, List.of());
                if (forbiddenSlots.isEmpty()) {
                    continue;
                }
                String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
                if (!forbiddenSlots.contains(classTime)) {
                    continue;
                }
                if (ConstantInfo.FIX_TIME_FLAG.equals(ClassUtil.cutGene(ConstantInfo.IS_FIX, gene))) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("班级 %s 在时间片 %s 已配置禁排，固定排课无法继续执行", classNo, classTime));
                }
                String newClassTime = pickClassTimeForClassForbiddenSlot(gene, resultGeneList, classForbiddenTimeSlots, availableClassTimes);
                if (Objects.equals(newClassTime, classTime)) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR,
                            String.format("班级 %s 的禁排时间过多，当前时间片模板下无法为任务分配可用时间", classNo));
                }
                replaceConflictTime(resultGeneList, gene, newClassTime);
                adjusted = true;
                break;
            }
            guard++;
        } while (adjusted && guard < 500);
        return resultGeneList;
    }

    private String pickClassTimeForTeacherDayLimit(String gene,
                                                   List<String> resultGeneList,
                                                   Map<String, Integer> teacherMaxDayHours,
                                                   List<String> availableClassTimes) {
        List<String> candidates = new ArrayList<>(availableClassTimes == null || availableClassTimes.isEmpty()
                ? defaultAvailableClassTimes()
                : availableClassTimes);
        Collections.shuffle(candidates, ClassUtil.RANDOM);
        String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
        String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
        String currentClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
        int maxDayHours = teacherMaxDayHours.getOrDefault(teacherNo, 0);
        for (String candidate : candidates) {
            if (Objects.equals(candidate, currentClassTime)) {
                continue;
            }
            boolean classBusy = resultGeneList.stream()
                    .filter(item -> !item.equals(gene))
                    .anyMatch(item -> classNo.equals(ClassUtil.cutGene(ConstantInfo.CLASS_NO, item))
                            && candidate.equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, item)));
            if (classBusy) {
                continue;
            }
            boolean teacherBusy = resultGeneList.stream()
                    .filter(item -> !item.equals(gene))
                    .anyMatch(item -> teacherNo.equals(ClassUtil.cutGene(ConstantInfo.TEACHER_NO, item))
                            && candidate.equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, item)));
            if (teacherBusy) {
                continue;
            }
            if (maxDayHours > 0) {
                long sameDayCount = resultGeneList.stream()
                        .filter(item -> !item.equals(gene))
                        .filter(item -> teacherNo.equals(ClassUtil.cutGene(ConstantInfo.TEACHER_NO, item)))
                        .map(item -> ClassUtil.cutGene(ConstantInfo.CLASS_TIME, item))
                        .filter(classTime -> resolveWeekdayNo(classTime) == resolveWeekdayNo(candidate))
                        .count();
                if (sameDayCount >= maxDayHours) {
                    continue;
                }
            }
            return candidate;
        }
        return currentClassTime;
    }

    private String pickClassTimeForClassForbiddenSlot(String gene,
                                                      List<String> resultGeneList,
                                                      Map<String, List<String>> classForbiddenTimeSlots,
                                                      List<String> availableClassTimes) {
        List<String> candidates = new ArrayList<>(availableClassTimes == null || availableClassTimes.isEmpty()
                ? defaultAvailableClassTimes()
                : availableClassTimes);
        Collections.shuffle(candidates, ClassUtil.RANDOM);
        String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
        String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
        String currentClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
        List<String> forbiddenSlots = classForbiddenTimeSlots.getOrDefault(classNo, List.of());
        for (String candidate : candidates) {
            if (Objects.equals(candidate, currentClassTime) || forbiddenSlots.contains(candidate)) {
                continue;
            }
            boolean classBusy = resultGeneList.stream()
                    .filter(item -> !item.equals(gene))
                    .anyMatch(item -> classNo.equals(ClassUtil.cutGene(ConstantInfo.CLASS_NO, item))
                            && candidate.equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, item)));
            if (classBusy) {
                continue;
            }
            boolean teacherBusy = resultGeneList.stream()
                    .filter(item -> !item.equals(gene))
                    .anyMatch(item -> teacherNo.equals(ClassUtil.cutGene(ConstantInfo.TEACHER_NO, item))
                            && candidate.equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, item)));
            if (teacherBusy) {
                continue;
            }
            return candidate;
        }
        return currentClassTime;
    }

    private String pickClassTimeForTeacherForbiddenSlot(String gene,
                                                        List<String> resultGeneList,
                                                        Map<String, List<String>> teacherForbiddenTimeSlots,
                                                        List<String> availableClassTimes) {
        List<String> candidates = new ArrayList<>(availableClassTimes == null || availableClassTimes.isEmpty()
                ? defaultAvailableClassTimes()
                : availableClassTimes);
        Collections.shuffle(candidates, ClassUtil.RANDOM);
        String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
        String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
        String currentClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
        List<String> forbiddenSlots = teacherForbiddenTimeSlots.getOrDefault(teacherNo, List.of());
        for (String candidate : candidates) {
            if (Objects.equals(candidate, currentClassTime) || forbiddenSlots.contains(candidate)) {
                continue;
            }
            boolean classBusy = resultGeneList.stream()
                    .filter(item -> !item.equals(gene))
                    .anyMatch(item -> classNo.equals(ClassUtil.cutGene(ConstantInfo.CLASS_NO, item))
                            && candidate.equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, item)));
            if (classBusy) {
                continue;
            }
            boolean teacherBusy = resultGeneList.stream()
                    .filter(item -> !item.equals(gene))
                    .anyMatch(item -> teacherNo.equals(ClassUtil.cutGene(ConstantInfo.TEACHER_NO, item))
                            && candidate.equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, item)));
            if (teacherBusy) {
                continue;
            }
            return candidate;
        }
        return currentClassTime;
    }

    private List<String> splitClassTimes(String classTime) {
        if (classTime == null || classTime.isBlank()) {
            return List.of();
        }
        List<String> classTimes = new ArrayList<>();
        for (int i = 0; i + 1 < classTime.length(); i += 2) {
            classTimes.add(classTime.substring(i, i + 2));
        }
        return classTimes;
    }

    private String buildTeacherWeekdayKey(String teacherNo, String classTime) {
        return teacherNo + "::" + resolveWeekdayNo(classTime);
    }

    private int resolveWeekdayNo(String classTime) {
        if (classTime == null || classTime.isBlank()) {
            return 0;
        }
        int classTimeNo = Integer.parseInt(classTime);
        return ((classTimeNo - 1) / 5) + 1;
    }

    /**
     * 将初始基因编码(都分配好时间)划分以班级为单位的个体
     * 班级编号的集合，去重
     */
    private Map<String, List<String>> transformIndividual(List<String> resultGeneList) {
        Map<String, List<String>> individualMap = new HashMap<>();
        List<String> classNoList = resultGeneList.stream()
                .map(gene -> ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        for (String classNo : classNoList) {
            List<String> geneList = new ArrayList<>();
            for (String gene : resultGeneList) {
                if (classNo.equals(ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene))) {
                    geneList.add(gene);
                }
            }
            if (!geneList.isEmpty()) {
                individualMap.put(classNo, geneList);
            }
        }
        return individualMap;
    }


    /**
     * 解码染色体中的基因，按照之前的编码解
     * 编码:
     * 固定时间：1
     * 年级编号：2
     * 班级编号：8
     * 讲师编号：5
     * 课程编号：6
     * 课程属性：2
     * 上课时间：2
     * 教室编号：6
     * 编码规则为：是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间+教室编号(遗传算法执行完最后再分配教室)
     * 其中如果不固定开课时间默认填充为"00"
     *
     * @param resultList 全部上课计划实体
     */
    private List<CoursePlan> decoding(List<String> resultList) {
        List<CoursePlan> coursePlanList = new ArrayList<>();
        for (String gene : resultList) {
            CoursePlan coursePlan = new CoursePlan();
            // 年级
            coursePlan.setGradeNo(ClassUtil.cutGene(ConstantInfo.GRADE_NO, gene));
            // 班级
            coursePlan.setClassNo(ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene));
            // 课程
            coursePlan.setCourseNo(ClassUtil.cutGene(ConstantInfo.COURSE_NO, gene));
            // 讲师
            coursePlan.setTeacherNo(ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene));
            // 教室
            coursePlan.setClassroomNo(ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, gene));
            // 上课时间
            coursePlan.setClassTime(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene));
            coursePlanList.add(coursePlan);
        }
        return coursePlanList;
    }

    private List<String> defaultAvailableClassTimes() {
        return java.util.stream.IntStream.rangeClosed(1, ClassUtil.MAX_CLASS_TIME)
                .mapToObj(i -> i < 10 ? ("0" + i) : String.valueOf(i))
                .toList();
    }

    private boolean isFixedTask(SchedulingTaskInput classTask) {
        return "1".equals(classTask.getIsFix()) || ConstantInfo.FIX_TIME_FLAG.equals(classTask.getIsFix());
    }

    private boolean isUnfixedTask(SchedulingTaskInput classTask) {
        return "0".equals(classTask.getIsFix());
    }

    private String toLegacyClassTime(CfgTimeSlot timeSlot) {
        if (timeSlot.getWeekdayNo() == null || timeSlot.getPeriodNo() == null) {
            return null;
        }
        if (timeSlot.getWeekdayNo() < 1 || timeSlot.getWeekdayNo() > 5 || timeSlot.getPeriodNo() < 1 || timeSlot.getPeriodNo() > 5) {
            return null;
        }
        return String.format("%02d", (timeSlot.getWeekdayNo() - 1) * 5 + timeSlot.getPeriodNo());
    }

    static record SchedulingRuntimeContext(String scheduleRuleName, List<String> availableClassTimes, boolean configApplied) {
    }
}
