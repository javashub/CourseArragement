package com.lyk.coursearrange.controller;


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
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.ScheduleTaskPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lequal
 * @since 2020-04-06
 */
@RestController
@Slf4j
public class ClassTaskController {

    @Resource
    private ClassTaskService classTaskService;
    @Resource
    private ScheduleLogMirrorService scheduleLogMirrorService;
    @Resource
    private SchTaskService schTaskService;

    /**
     * 查询开课任务
     */
    @GetMapping("/classtask/{page}/{semester}")
    public ServerResponse queryClassTask(@PathVariable("page") Integer page,
                                         @PathVariable("semester") String semester,
                                         @RequestParam(defaultValue = "10") Integer limit) {
        IPage<ScheduleTaskPageVO> standardPage = queryStandardTaskPage(semester, page, limit);
        if (standardPage != null && standardPage.getTotal() > 0) {
            return ServerResponse.ofSuccess(standardPage);
        }
        try {
            IPage<ClassTask> ipage = classTaskService.pageLegacyTasks(page, limit, semester);
            return ServerResponse.ofSuccess(ipage);
        } catch (Exception exception) {
            logLegacyTaskAccessFailure("查询 legacy 开课任务失败", semester, exception);
            return ServerResponse.ofSuccess(new Page<>(page, limit, 0));
        }
    }

    /**
     * 手动添加课程任务
     *
     * @param c
     * @return
     */
    @PostMapping("/addclasstask")
    public ServerResponse addClassTask(@RequestBody ClassTaskDTO c) {
        validateTaskDuplicate(c);
        SchTask standardTask = buildStandardTask(c);
        if (!schTaskService.save(standardTask)) {
            return throwBusiness(ResultCode.SYSTEM_ERROR, "添加课程任务失败");
        }
        log.info("新增课程任务，semester={}, courseNo={}, classNo={}",
                c.getSemester(), c.getCourseNo(), c.getClassNo());
        return ServerResponse.ofSuccess("添加课程任务成功");
    }


    /**
     * 删除开课任务
     * @param id
     */
    @DeleteMapping("/deleteclasstask/{id}")
    public ServerResponse deleteClassTask(@PathVariable("id") Integer id) {
        SchTask standardTask = schTaskService.getById(id.longValue());
        if (standardTask != null && (standardTask.getDeleted() == null || standardTask.getDeleted() == 0)
                && schTaskService.removeById(standardTask.getId())) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return throwBusiness(ResultCode.NOT_FOUND, "开课任务不存在");
    }

    /**
     * 获得学期集合
     */
    @GetMapping("/semester")
    public ServerResponse queryAllSemester() {
        Set<String> standardSemesters = listStandardSemesters();
        if (!standardSemesters.isEmpty()) {
            return ServerResponse.ofSuccess(standardSemesters);
        }
        try {
            List<ClassTask> list = classTaskService.listLegacyTasks(null);
            Set<String> set = list.stream().map(ClassTask::getSemester).collect(Collectors.toSet());
            return ServerResponse.ofSuccess(set);
        } catch (Exception exception) {
            logLegacyTaskAccessFailure("查询 legacy 学期列表失败", null, exception);
            return ServerResponse.ofSuccess(Set.of());
        }
    }

    /**
     * 排课算法接口，传入学期开始去查对应学期的开课任务，进行排课，排完课程后添加到course_plan表
     */
    @PostMapping("/arrange/{semester}")
    public ServerResponse arrange(@PathVariable("semester") String semester) {
        log.info("开始执行排课，semester={}", semester);
        return classTaskService.classScheduling(semester);
    }

    /**
     * 查询最近排课执行日志
     */
    @GetMapping("/arrange/logs")
    public ServerResponse queryArrangeLogs(@RequestParam(required = false) String semester,
                                           @RequestParam(defaultValue = "10") Integer limit) {
        List<?> logs = classTaskService.listRecentExecuteLogs(semester, limit);
        return ServerResponse.ofSuccess(logs);
    }

    private ServerResponse throwBusiness(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode, message);
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
                .map(task -> buildLegacyCompatibleTaskVO(task, legacyTaskMap))
                .toList());
        return resultPage;
    }

    private ScheduleTaskPageVO buildLegacyCompatibleTaskVO(SchTask task, Map<String, ClassTask> legacyTaskMap) {
        Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(task.getRemark());
        ClassTask legacyTask = legacyTaskMap.get(buildLegacyTaskKey(
                meta.getOrDefault("semester", ""),
                meta.getOrDefault("classNo", ""),
                meta.getOrDefault("courseNo", ""),
                meta.getOrDefault("teacherNo", "")));

        ScheduleTaskPageVO vo = new ScheduleTaskPageVO();
        // 旧管理页只认 id，这里优先给 legacyId，没有就回落到标准 id，保证删除按钮可用。
        Integer legacyId = resolveLegacyTaskId(meta, legacyTask);
        vo.setId(legacyId != null ? legacyId : (task.getId() == null ? null : task.getId().intValue()));
        vo.setStandardId(task.getId());
        vo.setSemester(meta.getOrDefault("semester", ""));
        vo.setGradeNo(meta.getOrDefault("gradeNo", ""));
        vo.setClassNo(meta.getOrDefault("classNo", ""));
        vo.setCourseNo(meta.getOrDefault("courseNo", ""));
        vo.setCourseName(meta.getOrDefault("courseName", ""));
        vo.setTeacherNo(meta.getOrDefault("teacherNo", ""));
        vo.setRealname(meta.getOrDefault("teacherName", ""));
        vo.setCourseAttr(legacyTask != null && legacyTask.getCourseAttr() != null
                ? legacyTask.getCourseAttr()
                : meta.getOrDefault("courseAttr", ""));
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
        task.setSourceType("LEGACY_API");
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
