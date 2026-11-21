package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.exceptions.CourseArrangeException;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.common.ConstantInfo;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.resource.util.ClassForbiddenTimeSlotUtils;
import com.lyk.coursearrange.resource.util.TeacherForbiddenTimeSlotUtils;
import com.lyk.coursearrange.schedule.engine.AdministrativeClassSchedulingPreCheck;
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
    private AdministrativeClassSchedulingPreCheck administrativeClassSchedulingPreCheck;
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
            List<SchedulingTask> engineTasks = buildEngineTasks(schedulingTasks);
            validateAdministrativeClassFixedConstraints(start, semester, taskCount,
                    engineTasks, runtimeContext.availableClassTimes());
            SchedulingExecutionResult executionResult = schedulingEngine.execute(SchedulingEngineRequest.builder()
                    .semester(semester)
                    .timeSlotCodes(runtimeContext.availableClassTimes())
                    .tasks(engineTasks)
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

    private void validateAdministrativeClassFixedConstraints(long start,
                                                             String semester,
                                                             int taskCount,
                                                             List<SchedulingTask> engineTasks,
                                                             List<String> availableClassTimes) {
        if (administrativeClassSchedulingPreCheck == null) {
            return;
        }
        List<AdministrativeClassSchedulingPreCheck.PreCheckIssue> issues =
                administrativeClassSchedulingPreCheck.validate(engineTasks, availableClassTimes);
        if (issues.isEmpty()) {
            return;
        }
        AdministrativeClassSchedulingPreCheck.PreCheckIssue firstIssue = issues.get(0);
        String message = String.format("排课前校验失败，存在固定约束冲突或非法固定时间，请先修正任务数据。任务：%s，原因：%s",
                firstIssue.taskCode(),
                firstIssue.reasonMessage());
        throw buildSchedulingException(start, semester, taskCount, message, ResultCode.BUSINESS_ERROR);
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
            Long operatorUserId = item.getOperatorUserId();
            vo.setId(item.getId());
            vo.setSemester(item.getRemark());
            vo.setTaskCount(item.getTaskTotal());
            vo.setGeneratedPlanCount(item.getTaskSuccess());
            vo.setStatus("SUCCESS".equalsIgnoreCase(item.getRunStatus()) || "PARTIAL".equalsIgnoreCase(item.getRunStatus()) ? 1 : 0);
            vo.setDurationMs(resolveDurationMs(item));
            vo.setMessage(item.getFailureReason());
            vo.setOperatorUserId(operatorUserId);
            vo.setOperatorName(operatorUserId == null ? "--" : operatorNameMap.getOrDefault(operatorUserId, "--"));
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


    private String toLegacyClassTime(Integer weekdayNo, Integer periodNo) {
        return ScheduleTaskMetaUtils.buildClassTime(weekdayNo, periodNo);
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



    private List<String> splitClassTimes(String classTime) {
        if (classTime == null || classTime.isBlank()) {
            return List.of();
        }
        if (classTime.contains(",") || classTime.contains("，")) {
            return java.util.Arrays.stream(classTime.split("[,，]"))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        if (classTime.length() <= 4) {
            return List.of(classTime);
        }
        List<String> classTimes = new ArrayList<>();
        if (classTime.length() % 4 == 0) {
            for (int i = 0; i + 3 < classTime.length(); i += 4) {
                classTimes.add(classTime.substring(i, i + 4));
            }
            return classTimes;
        }
        for (int i = 0; i + 1 < classTime.length(); i += 2) {
            classTimes.add(classTime.substring(i, i + 2));
        }
        return classTimes;
    }

    private String buildTeacherWeekdayKey(String teacherNo, String classTime) {
        return teacherNo + "::" + resolveWeekdayNo(classTime);
    }

    private int resolveWeekdayNo(String classTime) {
        Integer weekdayNo = ScheduleTaskMetaUtils.resolveWeekdayNo(classTime);
        return weekdayNo == null ? 0 : weekdayNo;
    }


    private List<String> defaultAvailableClassTimes() {
        return java.util.stream.IntStream.rangeClosed(1, 25)
                .mapToObj(i -> i < 10 ? ("0" + i) : String.valueOf(i))
                .toList();
    }

    private boolean isFixedTask(SchedulingTaskInput classTask) {
        return "1".equals(classTask.getIsFix()) || ConstantInfo.FIX_TIME_FLAG.equals(classTask.getIsFix());
    }


    private String toLegacyClassTime(CfgTimeSlot timeSlot) {
        if (timeSlot.getWeekdayNo() == null || timeSlot.getPeriodNo() == null) {
            return null;
        }
        if (timeSlot.getWeekdayNo() < 1 || timeSlot.getPeriodNo() < 1) {
            return null;
        }
        return ScheduleTaskMetaUtils.buildClassTime(timeSlot.getWeekdayNo(), timeSlot.getPeriodNo());
    }

    static record SchedulingRuntimeContext(String scheduleRuleName, List<String> availableClassTimes, boolean configApplied) {
    }
}
