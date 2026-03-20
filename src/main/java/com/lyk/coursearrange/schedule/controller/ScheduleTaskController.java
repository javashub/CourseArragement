package com.lyk.coursearrange.schedule.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.resource.util.ClassForbiddenTimeSlotUtils;
import com.lyk.coursearrange.resource.util.TeacherForbiddenTimeSlotUtils;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.ScheduleTaskPageVO;
import com.lyk.coursearrange.service.ClassInfoService;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.system.config.query.ConfigScopeQuery;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标准排课任务控制器。
 * 说明：
 * 1. 对前端提供统一的 /api/schedule 路径。
 * 2. 当前阶段先复用旧排课服务，逐步替换内部 tb_* 实现。
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule/tasks")
public class ScheduleTaskController {

    private final ClassTaskService classTaskService;
    private final SchTaskService schTaskService;
    private final ScheduleConfigFacadeService scheduleConfigFacadeService;
    private final ResTeacherService resTeacherService;
    private final ClassInfoService classInfoService;

    public ScheduleTaskController(ClassTaskService classTaskService,
                                  SchTaskService schTaskService,
                                  ScheduleConfigFacadeService scheduleConfigFacadeService,
                                  ResTeacherService resTeacherService,
                                  ClassInfoService classInfoService) {
        this.classTaskService = classTaskService;
        this.schTaskService = schTaskService;
        this.scheduleConfigFacadeService = scheduleConfigFacadeService;
        this.resTeacherService = resTeacherService;
        this.classInfoService = classInfoService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@RequestParam String semester,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String classNo,
                                  @RequestParam(required = false) String teacherNo,
                                  @RequestParam(required = false) String courseNo) {
        IPage<ScheduleTaskPageVO> standardPage = queryStandardTaskPage(semester, pageNum, pageSize, classNo, teacherNo, courseNo);
        if (standardPage != null) {
            return ServerResponse.ofSuccess(standardPage);
        }
        return ServerResponse.ofSuccess(new Page<>(pageNum, pageSize, 0));
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse<?> save(@RequestBody ClassTaskDTO request) {
        validateTaskDuplicate(request, null);
        validateContinuousConstraint(request);
        validateTeacherForbiddenConstraint(request);
        validateClassForbiddenConstraint(request);
        SchTask standardTask = buildStandardTask(request, new SchTask());
        if (!schTaskService.save(standardTask)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加课程任务失败");
        }
        log.info("新增标准排课任务，semester={}, courseNo={}, classNo={}",
                request.getSemester(), request.getCourseNo(), request.getClassNo());
        return ServerResponse.ofSuccess("添加课程任务成功");
    }

    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse<?> update(@PathVariable("id") Long id, @RequestBody ClassTaskDTO request) {
        SchTask existingTask = schTaskService.getById(id);
        if (existingTask == null || existingTask.getDeleted() != null && existingTask.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标准排课任务不存在");
        }
        validateTaskDuplicate(request, id);
        validateContinuousConstraint(request);
        validateTeacherForbiddenConstraint(request);
        validateClassForbiddenConstraint(request);
        SchTask standardTask = buildStandardTask(request, existingTask);
        standardTask.setId(id);
        if (!schTaskService.updateById(standardTask)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "修改课程任务失败");
        }
        log.info("修改标准排课任务，id={}, semester={}, courseNo={}, classNo={}",
                id, request.getSemester(), request.getCourseNo(), request.getClassNo());
        return ServerResponse.ofSuccess("修改课程任务成功");
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable Integer id) {
        SchTask standardTask = schTaskService.getById(id.longValue());
        if (standardTask != null && (standardTask.getDeleted() == null || standardTask.getDeleted() == 0)
                && schTaskService.removeById(standardTask.getId())) {
            return ServerResponse.ofSuccess("删除成功");
        }
        throw new BusinessException(ResultCode.NOT_FOUND, "标准排课任务不存在");
    }

    @DeleteMapping("/standard/{id}")
    public ServerResponse<?> deleteStandard(@PathVariable Long id) {
        SchTask task = schTaskService.getById(id);
        if (task == null || task.getDeleted() != null && task.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标准排课任务不存在");
        }
        if (schTaskService.removeById(id)) {
            return ServerResponse.ofSuccess("删除成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除失败");
    }

    @GetMapping("/semesters")
    public ServerResponse<?> listSemesters() {
        return ServerResponse.ofSuccess(listStandardSemesters());
    }

    @PostMapping("/executions")
    public ServerResponse<?> createExecution(@RequestParam String semester) {
        log.info("开始执行标准排课任务，semester={}", semester);
        return classTaskService.classScheduling(semester);
    }

    @GetMapping("/executions")
    public ServerResponse<?> listExecutions(@RequestParam(required = false) String semester,
                                            @RequestParam(defaultValue = "10") Integer limit) {
        return ServerResponse.ofSuccess(classTaskService.listRecentExecuteLogs(semester, limit));
    }

    @PostMapping("/arrange")
    public ServerResponse<?> arrange(@RequestParam String semester) {
        return createExecution(semester);
    }

    @GetMapping("/logs")
    public ServerResponse<?> logs(@RequestParam(required = false) String semester,
                                  @RequestParam(defaultValue = "10") Integer limit) {
        return listExecutions(semester, limit);
    }

    /**
     * 步骤说明：
     * 1. 优先从标准 sch_task 中读取任务。
     * 2. 再把标准任务转换成当前前端仍在使用的旧字段结构。
     */
    private IPage<ScheduleTaskPageVO> queryStandardTaskPage(String semester,
                                                            Integer pageNum,
                                                            Integer pageSize,
                                                            String classNo,
                                                            String teacherNo,
                                                            String courseNo) {
        LambdaQueryWrapper<SchTask> wrapper = new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getDeleted, 0)
                .like(SchTask::getRemark, "semester=" + semester)
                .like(classNo != null && !classNo.isBlank(), SchTask::getRemark, ",classNo=" + ScheduleTaskMetaUtils.safe(classNo) + ",")
                .like(teacherNo != null && !teacherNo.isBlank(), SchTask::getRemark, ",teacherNo=" + ScheduleTaskMetaUtils.safe(teacherNo) + ",")
                .like(courseNo != null && !courseNo.isBlank(), SchTask::getRemark, ",courseNo=" + ScheduleTaskMetaUtils.safe(courseNo) + ",")
                .orderByDesc(SchTask::getUpdatedAt)
                .orderByDesc(SchTask::getId);
        Page<SchTask> taskPage = schTaskService.page(new Page<>(pageNum, pageSize), wrapper);
        if (taskPage.getTotal() <= 0) {
            return new Page<>(pageNum, pageSize, 0);
        }

        List<ScheduleTaskPageVO> filteredRecords = taskPage.getRecords().stream()
                .map(this::buildStandardTaskVO)
                .filter(item -> matchesFilters(item, classNo, teacherNo, courseNo))
                .toList();
        long total = taskPage.getTotal();
        if (filteredRecords.size() != taskPage.getRecords().size()) {
            total = filteredRecords.size();
        }

        Page<ScheduleTaskPageVO> resultPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), total);
        resultPage.setRecords(filteredRecords);
        return resultPage;
    }

    private ScheduleTaskPageVO buildStandardTaskVO(SchTask task) {
        Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(task.getRemark());
        ScheduleTaskPageVO vo = new ScheduleTaskPageVO();
        vo.setId(task.getId() == null ? null : task.getId().intValue());
        vo.setStandardId(task.getId());
        vo.setSemester(meta.getOrDefault("semester", ""));
        vo.setGradeNo(meta.getOrDefault("gradeNo", ""));
        vo.setClassNo(meta.getOrDefault("classNo", ""));
        vo.setCourseNo(meta.getOrDefault("courseNo", ""));
        vo.setCourseName(meta.getOrDefault("courseName", ""));
        vo.setTeacherNo(meta.getOrDefault("teacherNo", ""));
        vo.setRealname(meta.getOrDefault("teacherName", ""));
        vo.setCourseAttr(meta.getOrDefault("courseAttr", ""));
        vo.setStudentNum(task.getStudentCount());
        vo.setWeeksNumber(task.getWeekHours());
        vo.setWeeksSum(calculateWeeksSum(task));
        vo.setIsFix(task.getNeedFixedTime() != null && task.getNeedFixedTime() == 1 ? "1" : "0");
        vo.setNeedContinuous(task.getNeedContinuous() != null && task.getNeedContinuous() == 1 ? 1 : 0);
        vo.setContinuousSize(task.getContinuousSize() == null || task.getContinuousSize() <= 0 ? 1 : task.getContinuousSize());
        vo.setClassTime(toLegacyClassTime(task.getFixedWeekdayNo(), task.getFixedPeriodNo()));
        return vo;
    }

    private Set<String> listStandardSemesters() {
        LambdaQueryWrapper<SchTask> wrapper = new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getDeleted, 0)
                .select(SchTask::getRemark)
                .orderByDesc(SchTask::getUpdatedAt);
        return schTaskService.list(wrapper).stream()
                .map(SchTask::getRemark)
                .map(ScheduleTaskMetaUtils::parseTaskRemark)
                .map(meta -> meta.get("semester"))
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toSet());
    }

    private Integer calculateWeeksSum(SchTask task) {
        if (task.getTotalHours() == null || task.getWeekHours() == null || task.getWeekHours() <= 0) {
            return 0;
        }
        return task.getTotalHours() / task.getWeekHours();
    }

    private boolean matchesFilters(ScheduleTaskPageVO item, String classNo, String teacherNo, String courseNo) {
        return matches(item.getClassNo(), classNo)
                && matches(item.getTeacherNo(), teacherNo)
                && matches(item.getCourseNo(), courseNo);
    }

    private boolean matches(String actualValue, String expectedValue) {
        return expectedValue == null || expectedValue.isBlank() || expectedValue.equals(actualValue);
    }

    private String toLegacyClassTime(Integer weekdayNo, Integer periodNo) {
        if (weekdayNo == null || periodNo == null || weekdayNo <= 0 || periodNo <= 0) {
            return "";
        }
        return String.format("%02d", (weekdayNo - 1) * 5 + periodNo);
    }

    private void validateTaskDuplicate(ClassTaskDTO request, Long currentTaskId) {
        String taskCode = ScheduleTaskMetaUtils.buildTaskCode(request);
        LambdaQueryWrapper<SchTask> wrapper = new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getTaskCode, taskCode)
                .eq(SchTask::getDeleted, 0)
                .ne(currentTaskId != null, SchTask::getId, currentTaskId)
                .last("limit 1");
        if (schTaskService.getOne(wrapper, false) != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "相同班级、课程、教师、学期的任务已存在");
        }
    }

    private void validateTeacherForbiddenConstraint(ClassTaskDTO request) {
        if (!Objects.equals("1", request.getIsFix()) || request.getTeacherNo() == null || request.getTeacherNo().isBlank()) {
            return;
        }
        ResTeacher teacher = resTeacherService.getOne(new LambdaQueryWrapper<ResTeacher>()
                .eq(ResTeacher::getDeleted, 0)
                .eq(ResTeacher::getTeacherCode, request.getTeacherNo())
                .last("limit 1"), false);
        if (teacher == null) {
            return;
        }
        List<String> forbiddenTimeSlots = TeacherForbiddenTimeSlotUtils.parse(teacher.getForbiddenTimeSlots());
        if (forbiddenTimeSlots.isEmpty()) {
            return;
        }
        for (String classTime : splitClassTimes(request.getClassTime())) {
            if (forbiddenTimeSlots.contains(classTime)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR,
                        String.format("教师 %s 在时间片 %s 已配置禁排，请调整固定时间或教师约束",
                                request.getRealname() == null || request.getRealname().isBlank() ? request.getTeacherNo() : request.getRealname(),
                                classTime));
            }
        }
    }

    private void validateClassForbiddenConstraint(ClassTaskDTO request) {
        if (!Objects.equals("1", request.getIsFix()) || request.getClassNo() == null || request.getClassNo().isBlank()) {
            return;
        }
        ClassInfo classInfo = classInfoService.getOne(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getDeleted, 0)
                .eq(ClassInfo::getClassNo, request.getClassNo())
                .last("limit 1"), false);
        if (classInfo == null) {
            return;
        }
        List<String> forbiddenTimeSlots = ClassForbiddenTimeSlotUtils.parse(classInfo.getForbiddenTimeSlots());
        if (forbiddenTimeSlots.isEmpty()) {
            return;
        }
        for (String classTime : splitClassTimes(request.getClassTime())) {
            if (forbiddenTimeSlots.contains(classTime)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR,
                        String.format("班级 %s 在时间片 %s 已配置禁排，请调整固定时间或班级约束",
                                request.getClassNo(), classTime));
            }
        }
    }

    private List<String> splitClassTimes(String classTime) {
        if (classTime == null || classTime.isBlank()) {
            return List.of();
        }
        return java.util.stream.IntStream.range(0, classTime.length() / 2)
                .mapToObj(index -> classTime.substring(index * 2, index * 2 + 2))
                .toList();
    }

    private SchTask buildStandardTask(ClassTaskDTO request, SchTask task) {
        int needContinuous = request.getNeedContinuous() != null && request.getNeedContinuous() == 1 ? 1 : 0;
        int continuousSize = needContinuous == 1
                ? Math.max(2, request.getContinuousSize() == null ? 2 : request.getContinuousSize())
                : 1;
        task.setTaskCode(ScheduleTaskMetaUtils.buildTaskCode(request));
        task.setSchoolYearId(0L);
        task.setTermId(0L);
        task.setStageId(0L);
        task.setCourseId(0L);
        task.setTeacherId(0L);
        task.setStudentCount(request.getStudentNum() == null ? 0 : request.getStudentNum());
        task.setWeekHours(request.getWeeksNumber() == null ? 0 : request.getWeeksNumber());
        task.setTotalHours((request.getWeeksNumber() == null || request.getWeeksSum() == null)
                ? 0
                : request.getWeeksNumber() * request.getWeeksSum());
        task.setNeedContinuous(needContinuous);
        task.setContinuousSize(continuousSize);
        task.setNeedFixedRoom(0);
        task.setNeedFixedTime("1".equals(request.getIsFix()) ? 1 : 0);
        task.setFixedWeekdayNo(ScheduleTaskMetaUtils.resolveWeekdayNo(request.getClassTime()));
        task.setFixedPeriodNo(ScheduleTaskMetaUtils.resolvePeriodNo(request.getClassTime()));
        task.setPriorityLevel(5);
        task.setAllowConflict(0);
        task.setTaskStatus("PENDING");
        task.setSourceType("MANUAL");
        task.setStatus(1);
        task.setRemark("semester=" + ScheduleTaskMetaUtils.safe(request.getSemester())
                + ",classNo=" + ScheduleTaskMetaUtils.safe(request.getClassNo())
                + ",courseNo=" + ScheduleTaskMetaUtils.safe(request.getCourseNo())
                + ",teacherNo=" + ScheduleTaskMetaUtils.safe(request.getTeacherNo())
                + ",gradeNo=" + ScheduleTaskMetaUtils.safe(request.getGradeNo())
                + ",courseName=" + ScheduleTaskMetaUtils.safe(request.getCourseName())
                + ",courseAttr=" + ScheduleTaskMetaUtils.safe(request.getCourseAttr())
                + ",teacherName=" + ScheduleTaskMetaUtils.safe(request.getRealname()));
        return task;
    }

    private void validateContinuousConstraint(ClassTaskDTO request) {
        int needContinuous = request.getNeedContinuous() != null && request.getNeedContinuous() == 1 ? 1 : 0;
        int continuousSize = request.getContinuousSize() == null ? 0 : request.getContinuousSize();
        if (needContinuous == 0) {
            return;
        }
        if (continuousSize < 2) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "连堂任务的连堂节数不能小于 2");
        }
        ScheduleConfigVO config = scheduleConfigFacadeService == null
                ? null
                : scheduleConfigFacadeService.getScheduleConfig(new ConfigScopeQuery());
        Integer defaultContinuousLimit = config == null || config.getScheduleRule() == null
                ? null
                : config.getScheduleRule().getDefaultContinuousLimit();
        if (defaultContinuousLimit != null && defaultContinuousLimit > 0 && continuousSize > defaultContinuousLimit) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    "连堂节数不能超过当前排课规则允许的上限 " + defaultContinuousLimit);
        }
    }
}
