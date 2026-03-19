package com.lyk.coursearrange.schedule.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.ScheduleTaskPageVO;
import com.lyk.coursearrange.service.ClassTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
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
    private final ScheduleLogMirrorService scheduleLogMirrorService;

    public ScheduleTaskController(ClassTaskService classTaskService,
                                  SchTaskService schTaskService,
                                  ScheduleLogMirrorService scheduleLogMirrorService) {
        this.classTaskService = classTaskService;
        this.schTaskService = schTaskService;
        this.scheduleLogMirrorService = scheduleLogMirrorService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@RequestParam String semester,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<ScheduleTaskPageVO> standardPage = queryStandardTaskPage(semester, pageNum, pageSize);
        if (standardPage != null && standardPage.getTotal() > 0) {
            return ServerResponse.ofSuccess(standardPage);
        }
        try {
            IPage<ClassTask> page = classTaskService.pageLegacyTasks(pageNum, pageSize, semester);
            return ServerResponse.ofSuccess(page);
        } catch (Exception exception) {
            logLegacyTaskAccessFailure("查询 legacy 排课任务分页失败", semester, exception);
            return ServerResponse.ofSuccess(new Page<>(pageNum, pageSize, 0));
        }
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse<?> save(@RequestBody ClassTaskDTO request) {
        validateTaskDuplicate(request);
        SchTask standardTask = buildStandardTask(request);
        if (!schTaskService.save(standardTask)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加课程任务失败");
        }
        log.info("新增标准排课任务，semester={}, courseNo={}, classNo={}",
                request.getSemester(), request.getCourseNo(), request.getClassNo());
        return ServerResponse.ofSuccess("添加课程任务成功");
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
        Set<String> standardSemesters = listStandardSemesters();
        if (!standardSemesters.isEmpty()) {
            return ServerResponse.ofSuccess(standardSemesters);
        }
        try {
            List<ClassTask> list = classTaskService.listLegacyTasks(null);
            return ServerResponse.ofSuccess(list.stream().map(ClassTask::getSemester).collect(Collectors.toSet()));
        } catch (Exception exception) {
            logLegacyTaskAccessFailure("查询 legacy 学期列表失败", null, exception);
            return ServerResponse.ofSuccess(Set.of());
        }
    }

    @PostMapping("/arrange")
    public ServerResponse<?> arrange(@RequestParam String semester) {
        log.info("开始执行标准排课任务，semester={}", semester);
        return classTaskService.classScheduling(semester);
    }

    @GetMapping("/logs")
    public ServerResponse<?> logs(@RequestParam(required = false) String semester,
                                  @RequestParam(defaultValue = "10") Integer limit) {
        return ServerResponse.ofSuccess(classTaskService.listRecentExecuteLogs(semester, limit));
    }

    /**
     * 步骤说明：
     * 1. 优先从标准 sch_task 中读取任务。
     * 2. 再把标准任务转换成当前前端仍在使用的旧字段结构。
     * 3. 为了兼容删除、课程属性等旧能力，按学期回填 legacy 任务信息。
     */
    private IPage<ScheduleTaskPageVO> queryStandardTaskPage(String semester, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SchTask> wrapper = new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getDeleted, 0)
                .like(SchTask::getRemark, "semester=" + semester)
                .orderByDesc(SchTask::getUpdatedAt)
                .orderByDesc(SchTask::getId);
        Page<SchTask> taskPage = schTaskService.page(new Page<>(pageNum, pageSize), wrapper);
        if (taskPage.getTotal() <= 0) {
            return null;
        }

        Map<String, ClassTask> legacyTaskMap = new HashMap<>();
        try {
            Map<String, ClassTask> fetchedLegacyTaskMap = classTaskService.listLegacyTasks(semester)
                    .stream()
                    .collect(Collectors.toMap(this::buildLegacyTaskKey, item -> item, (left, right) -> left, HashMap::new));
            legacyTaskMap.putAll(fetchedLegacyTaskMap);
        } catch (Exception exception) {
            logLegacyTaskAccessFailure("查询 legacy 排课任务副本失败，将仅返回标准任务字段", semester, exception);
        }

        Page<ScheduleTaskPageVO> resultPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        resultPage.setRecords(taskPage.getRecords().stream()
                .map(task -> buildScheduleTaskVO(task, legacyTaskMap))
                .toList());
        return resultPage;
    }

    private ScheduleTaskPageVO buildScheduleTaskVO(SchTask task, Map<String, ClassTask> legacyTaskMap) {
        Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(task.getRemark());
        ClassTask legacyTask = legacyTaskMap.get(buildLegacyTaskKey(
                meta.getOrDefault("semester", ""),
                meta.getOrDefault("classNo", ""),
                meta.getOrDefault("courseNo", ""),
                meta.getOrDefault("teacherNo", "")));

        ScheduleTaskPageVO vo = new ScheduleTaskPageVO();
        vo.setId(resolveLegacyTaskId(meta, legacyTask));
        vo.setStandardId(task.getId());
        vo.setSemester(meta.getOrDefault("semester", ""));
        vo.setGradeNo(meta.getOrDefault("gradeNo", ""));
        vo.setClassNo(meta.getOrDefault("classNo", ""));
        vo.setCourseNo(meta.getOrDefault("courseNo", ""));
        vo.setCourseName(meta.getOrDefault("courseName", ""));
        vo.setTeacherNo(meta.getOrDefault("teacherNo", ""));
        vo.setRealname(meta.getOrDefault("teacherName", ""));
        vo.setCourseAttr(resolveCourseAttr(meta, legacyTask));
        vo.setStudentNum(legacyTask != null ? legacyTask.getStudentNum() : task.getStudentCount());
        vo.setWeeksNumber(legacyTask != null ? legacyTask.getWeeksNumber() : task.getWeekHours());
        vo.setWeeksSum(legacyTask != null ? legacyTask.getWeeksSum() : calculateWeeksSum(task));
        vo.setIsFix(task.getNeedFixedTime() != null && task.getNeedFixedTime() == 1 ? "1" : "0");
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

    private Integer resolveLegacyTaskId(Map<String, String> meta, ClassTask legacyTask) {
        if (legacyTask != null && legacyTask.getId() != null) {
            return legacyTask.getId();
        }
        String legacyId = meta.get("legacyId");
        if (legacyId == null || legacyId.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(legacyId);
        } catch (NumberFormatException exception) {
            log.warn("解析 legacyId 失败，legacyId={}", legacyId);
            return null;
        }
    }

    private String resolveCourseAttr(Map<String, String> meta, ClassTask legacyTask) {
        if (legacyTask != null && legacyTask.getCourseAttr() != null) {
            return legacyTask.getCourseAttr();
        }
        return meta.getOrDefault("courseAttr", "");
    }

    private Integer calculateWeeksSum(SchTask task) {
        if (task.getTotalHours() == null || task.getWeekHours() == null || task.getWeekHours() <= 0) {
            return 0;
        }
        return task.getTotalHours() / task.getWeekHours();
    }

    private String buildLegacyTaskKey(ClassTask task) {
        return buildLegacyTaskKey(task.getSemester(), task.getClassNo(), task.getCourseNo(), task.getTeacherNo());
    }

    private String buildLegacyTaskKey(String semester, String classNo, String courseNo, String teacherNo) {
        return String.join("_",
                semester == null ? "" : semester,
                classNo == null ? "" : classNo,
                courseNo == null ? "" : courseNo,
                teacherNo == null ? "" : teacherNo);
    }

    private void logLegacyTaskAccessFailure(String message, String semester, Exception exception) {
        if (isMissingLegacyTaskTable(exception)) {
            if (semester == null || semester.isBlank()) {
                log.warn("{}, tb_class_task 已不存在，将继续使用标准任务链路", message);
            } else {
                log.warn("{}, semester={}, tb_class_task 已不存在，将继续使用标准任务链路", message, semester);
            }
            return;
        }
        if (semester == null || semester.isBlank()) {
            log.warn(message, exception);
        } else {
            log.warn(message + "，semester={}", semester, exception);
        }
    }

    private boolean isMissingLegacyTaskTable(Exception exception) {
        String message = exception == null ? null : exception.getMessage();
        return message != null && message.contains("tb_class_task") && message.contains("doesn't exist");
    }

    private String toLegacyClassTime(Integer weekdayNo, Integer periodNo) {
        if (weekdayNo == null || periodNo == null || weekdayNo <= 0 || periodNo <= 0) {
            return "";
        }
        return String.format("%02d", (weekdayNo - 1) * 5 + periodNo);
    }

    private void validateTaskDuplicate(ClassTaskDTO request) {
        String taskCode = ScheduleTaskMetaUtils.buildTaskCode(request);
        LambdaQueryWrapper<SchTask> wrapper = new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getTaskCode, taskCode)
                .eq(SchTask::getDeleted, 0)
                .last("limit 1");
        if (schTaskService.getOne(wrapper, false) != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "相同班级、课程、教师、学期的任务已存在");
        }
    }

    private SchTask buildStandardTask(ClassTaskDTO request) {
        SchTask task = new SchTask();
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
        task.setNeedContinuous(0);
        task.setContinuousSize(1);
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
}
