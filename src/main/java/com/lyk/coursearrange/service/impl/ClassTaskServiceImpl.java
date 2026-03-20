package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.exceptions.CourseArrangeException;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.dao.ClassInfoDao;
import com.lyk.coursearrange.dao.ClassroomDao;
import com.lyk.coursearrange.dao.TeachBuildInfoDao;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import com.lyk.coursearrange.common.ConstantInfo;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.resource.util.ClassForbiddenTimeSlotUtils;
import com.lyk.coursearrange.resource.util.TeacherForbiddenTimeSlotUtils;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.service.ScheduleExecuteLogService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.query.ConfigScopeQuery;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
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
 * UPDATE tb_student
 * SET student_no = CONCAT('2024', SUBSTRING(student_no, 5))
 * WHERE student_no LIKE '2019%';
 */
@Service
@Slf4j
public class ClassTaskServiceImpl implements ClassTaskService {

    @Resource
    private TeachBuildInfoDao teachBuildInfoDao;
    @Resource
    private ClassroomDao classroomDao;
    @Resource
    private ClassInfoDao classInfoDao;
    @Resource
    private ScheduleExecuteLogService scheduleExecuteLogService;
    @Resource
    private AuthFacadeService authFacadeService;
    @Resource
    private ScheduleLogMirrorService scheduleLogMirrorService;
    @Resource
    private SchTaskService schTaskService;
    @Resource
    private ScheduleConfigFacadeService scheduleConfigFacadeService;
    @Resource
    private ResTeacherService resTeacherService;

    /**
     * 排课算法入口
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ServerResponse classScheduling(String semester) {
        long start = System.currentTimeMillis();
        int taskCount = 0;
        try {
            log.info("开始排课,时间：{}", start);
            // 1、优先从标准任务构造排课输入
            List<SchedulingTaskInput> schedulingTasks = listSchedulingTasks(semester);
            if (null == schedulingTasks || schedulingTasks.isEmpty()) {
                throw buildSchedulingException(start, semester, taskCount,
                        "排课失败，查询不到排课任务！请导入排课任务再进行排课~", ResultCode.BUSINESS_ERROR);
            }
            taskCount = schedulingTasks.size();
            SchedulingRuntimeContext runtimeContext = resolveSchedulingRuntimeContext();

            // 校验学时是否超过课表的容纳值
            checkWeeksNumber(schedulingTasks, runtimeContext.availableClassTimes());
            validateTeacherForbiddenTimeSlots(schedulingTasks);
            validateClassForbiddenTimeSlots(schedulingTasks);
            validateTeacherDayHourLimit(schedulingTasks);
            Map<String, List<String>> teacherForbiddenTimeSlots = resolveTeacherForbiddenTimeSlots(schedulingTasks);
            Map<String, List<String>> classForbiddenTimeSlots = resolveClassForbiddenTimeSlots(schedulingTasks);
            Map<String, Integer> teacherMaxDayHours = resolveTeacherMaxDayHours(schedulingTasks);

            // 2、将开课任务的各项信息进行编码成染色体，分为固定时间与不固定时间
            Map<String, List<String>> geneMap = coding(schedulingTasks, runtimeContext.availableClassTimes());
            // 3、给初始基因编码随机分配时间，得到同班上课时间不冲突的编码
            List<String> resultGeneList = codingTime(geneMap, runtimeContext.availableClassTimes());
            resultGeneList = enforceClassForbiddenTimeSlots(resultGeneList, classForbiddenTimeSlots, runtimeContext.availableClassTimes());
            resultGeneList = enforceTeacherForbiddenTimeSlots(resultGeneList, teacherForbiddenTimeSlots, runtimeContext.availableClassTimes());
            resultGeneList = enforceTeacherDayHourLimit(resultGeneList, teacherMaxDayHours, runtimeContext.availableClassTimes());
            // 4、将分配好时间的基因编码以班级分类成为以班级的个体，得到班级的不冲突时间初始编码
            Map<String, List<String>> individualMap = transformIndividual(resultGeneList);
            // 5、遗传进化(这里面这里已经处理完上课时间)
            individualMap = geneticEvolution(individualMap, runtimeContext.availableClassTimes(), teacherForbiddenTimeSlots, classForbiddenTimeSlots, teacherMaxDayHours);

            // 检测时间冲突
//            checkConflict(individualMap);

            // 6、分配教室并做教室冲突检测
            List<String> resultList = finalResult(individualMap);
            // 7、解码
            List<CoursePlan> coursePlanList = decoding(resultList);
            // 8、只写标准课表结果
            scheduleLogMirrorService.replaceScheduleResults(semester, schedulingTasks, coursePlanList);
            long duration = System.currentTimeMillis() - start;
            Map<String, Object> schedulingSummary = buildSchedulingSummary(schedulingTasks, coursePlanList);
            schedulingSummary.put("effectiveScheduleRuleName", runtimeContext.scheduleRuleName());
            schedulingSummary.put("effectiveTimeSlotCount", runtimeContext.availableClassTimes().size());
            schedulingSummary.put("timeSlotConfigApplied", runtimeContext.configApplied());
            log.info("完成排课,耗时：{}", duration);
            saveExecuteLog(semester, taskCount, coursePlanList.size(), 1, duration,
                    buildSchedulingSummaryMessage(schedulingSummary, coursePlanList.size()));
            return buildSchedulingSuccessResponse(duration, coursePlanList.size(), schedulingSummary);
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
            enrichTeacherHourLimits(tasks);
            enrichClassForbiddenTimeSlots(tasks);
            return tasks;
        }
        log.warn("标准排课任务为空，排课流程不会再回退读取 tb_class_task，semester={}", semester);
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
    public List<ScheduleExecuteLog> listRecentExecuteLogs(String semester, Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        LambdaQueryWrapper<ScheduleExecuteLog> wrapper = new LambdaQueryWrapper<ScheduleExecuteLog>()
                .eq(semester != null && !semester.isBlank(), ScheduleExecuteLog::getSemester, semester)
                .orderByDesc(ScheduleExecuteLog::getCreateTime)
                .last("limit " + safeLimit);
        return scheduleExecuteLogService.list(wrapper);
    }

    private void saveExecuteLog(String semester, int taskCount, int generatedPlanCount, int status,
                                long duration, String message) {
        ScheduleExecuteLog executeLog = new ScheduleExecuteLog();
        executeLog.setSemester(semester);
        executeLog.setTaskCount(taskCount);
        executeLog.setGeneratedPlanCount(generatedPlanCount);
        executeLog.setStatus(status);
        executeLog.setDurationMs(duration);
        executeLog.setMessage(message);
        try {
            CurrentUserVO currentUser = authFacadeService.getCurrentUserView();
            if (currentUser != null) {
                executeLog.setOperatorUserId(currentUser.getUserId());
                executeLog.setOperatorName(resolveOperatorName(currentUser));
                executeLog.setOperatorType(currentUser.getUserType());
            }
        } catch (Exception exception) {
            log.warn("获取当前操作人失败，排课日志将以匿名方式记录", exception);
        }
        scheduleExecuteLogService.save(executeLog);
        scheduleLogMirrorService.mirrorExecuteLog(executeLog);
    }

    private String resolveOperatorName(CurrentUserVO currentUser) {
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isBlank()) {
            return currentUser.getDisplayName();
        }
        if (currentUser.getRealName() != null && !currentUser.getRealName().isBlank()) {
            return currentUser.getRealName();
        }
        return currentUser.getUsername();
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
        saveExecuteLog(semester, taskCount, 0, 0, duration, message);
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
        task.setRealname(meta.getOrDefault("teacherName", ""));
        task.setCourseAttr(meta.getOrDefault("courseAttr", ""));
        task.setStudentNum(standardTask.getStudentCount() == null ? 0 : standardTask.getStudentCount());
        task.setWeeksNumber(standardTask.getWeekHours() == null ? 0 : standardTask.getWeekHours());
        task.setWeeksSum(resolveWeeksSum(standardTask));
        task.setPriorityLevel(normalizePriorityLevel(standardTask.getPriorityLevel()));
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

    void enrichClassForbiddenTimeSlots(List<SchedulingTaskInput> tasks) {
        if (tasks == null || tasks.isEmpty() || classInfoDao == null) {
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
        Map<String, ClassInfo> classInfoMap = classInfoDao.selectList(new LambdaQueryWrapper<ClassInfo>()
                        .eq(ClassInfo::getDeleted, 0)
                        .in(ClassInfo::getClassNo, classNos))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ClassInfo::getClassNo, item -> item, (left, right) -> left));
        tasks.forEach(task -> {
            ClassInfo classInfo = classInfoMap.get(task.getClassNo());
            if (classInfo != null) {
                task.setClassForbiddenTimeSlots(ClassForbiddenTimeSlotUtils.parse(classInfo.getForbiddenTimeSlots()));
            }
        });
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
        String teacherName = task.getRealname() == null || task.getRealname().isBlank()
                ? task.getTeacherNo()
                : task.getRealname();
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
    private List<String> finalResult(Map<String, List<String>> individualMap) {
        List<String> resultList = new ArrayList<>();
        List<String> resultGeneList = collectGene(individualMap);
        String classroomNo = "";
        List<String> gradeList = resultGeneList.stream()
                .map(gene -> ClassUtil.cutGene(ConstantInfo.GRADE_NO, gene))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, List<String>> gradeMap = collectGeneByGrade(resultGeneList, gradeList);
        for (Map.Entry<String, List<String>> entry : gradeMap.entrySet()) {
            String gradeNo = entry.getKey();
            List<String> teachBuildNoList = teachBuildInfoDao.selectTeachBuildList(gradeNo);

            List<String> gradeGeneList = gradeMap.get(gradeNo);

            LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<Classroom>().in(Classroom::getTeachbuildNo, teachBuildNoList);
            List<Classroom> classroomList2 = classroomDao.selectList(wrapper);

            for (String gene : gradeGeneList) {
                // 分配教室
                classroomNo = issueClassroom(gene, classroomList2, resultList);
                gene = gene + classroomNo;
                resultList.add(gene);
            }
        }
        return resultList;
    }

    /**
     * 给不同的基因编码分配教室
     *
     * @param gene          需要分配教室的基因编码
     * @param classroomList 教室
     * @param resultList    分配有教室的编码
     */
    private String issueClassroom(String gene, List<Classroom> classroomList, List<String> resultList) {
        // 处理特殊课程，实验课，体育课
        List<Classroom> sportBuilding = classroomDao.selectByTeachbuildNo("12");
        List<Classroom> experimentBuilding = classroomDao.selectByTeachbuildNo("08");
        String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
        int studentNum = classInfoDao.selectStuNum(classNo);
        String courseAttr = ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene);

        if (courseAttr.equals(ConstantInfo.EXPERIMENT_COURSE)) {
            // 03 为实验课
            return chooseClassroom(studentNum, gene, experimentBuilding, resultList);
        } else if (courseAttr.equals(ConstantInfo.PHYSICAL_COURSE)) {
            // 04为体育课
            return chooseClassroom(studentNum, gene, sportBuilding, resultList);
        } else {
            // 剩下主要课程、次要课程都放在普通的教室
            // 如果还有其他课程另外加判断课程属性，暂时设定4种：主要，次要，实验，体育。音乐舞蹈那些不算
            return chooseClassroom(studentNum, gene, classroomList, resultList);
        }
    }

    /**
     * 给不同课程的基因编码随机选择一个教室
     *
     * @param studentNum    开课的班级的学生人数
     * @param gene          需要安排教室的基因编码
     * @param classroomList 教室
     */
    private String chooseClassroom(int studentNum, String gene, List<Classroom> classroomList, List<String> resultList) {
        int min = 0;
        int max = classroomList.size() - 1;
        int temp = min + (int) (Math.random() * (max + 1 - min));
        Classroom classroom = classroomList.get(temp);
        // 分配教室
        boolean isClassRoomSuitable = judgeClassroom(studentNum, gene, classroom, resultList);
        if (isClassRoomSuitable) {
            // 该教室满足条件
            return classroom.getClassroomNo();
        } else {
            // 不满足，继续找教室
            return chooseClassroom(studentNum, gene, classroomList, resultList);
        }
    }

    /**
     * 判断教室是否符合上课班级所需
     * 即：不同属性的课要放在对应属性的教室上课
     * @param classroom  随机分配教室
     */
    private boolean judgeClassroom(int studentNum, String gene, Classroom classroom, List<String> resultList) {

        String courseAttr = ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene);
        // 只要是语数英物化生政史地这些课程都是放在普通教室上课
        if (courseAttr.equals(ConstantInfo.MAIN_COURSE) || courseAttr.equals(ConstantInfo.SECONDARY_COURSE)) {
            // 找到普通教室，普通教室的属性都是01
            if (classroom.getAttr().equals(ConstantInfo.NORMAL_CLASS_ROOM)) {
                if (classroom.getCapacity() >= studentNum) {
                    // 还要判断该教室是否在同一时间有别的班级使用了
                    return isFree(gene, resultList, classroom);
                } else {
                    // 教室容量不够
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene).equals(classroom.getAttr())) {
                if (classroom.getCapacity() >= studentNum) {
                    // 判断该教室上课时间是否重复
                    return isFree(gene, resultList, classroom);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * 判断同一时间同一个教室是否有多个班级使用
     */
    private Boolean isFree(String gene, List<String> resultList, Classroom classroom) {
        if (resultList.isEmpty()) {
            return true;
        } else {
            for (String resultGene : resultList) {
                if (ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, resultGene).equals(classroom.getClassroomNo())
                        && (ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene).equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, resultGene)))) {
                        return false;
                }
            }
        }
        return true;
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
                    String teacherName = task.getRealname() == null || task.getRealname().isBlank()
                            ? task.getTeacherNo()
                            : task.getRealname();
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
                    String teacherName = task.getRealname() == null || task.getRealname().isBlank()
                            ? task.getTeacherNo()
                            : task.getRealname();
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
