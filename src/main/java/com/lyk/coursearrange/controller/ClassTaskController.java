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
import org.springframework.beans.BeanUtils;
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
        LambdaQueryWrapper<ClassTask> wrapper = new LambdaQueryWrapper<ClassTask>().eq(ClassTask::getSemester, semester);
        IPage<ClassTask> ipage = classTaskService.page(new Page<>(page, limit), wrapper);
        return ServerResponse.ofSuccess(ipage);
    }

    /**
     * 手动添加课程任务
     *
     * @param c
     * @return
     */
    @PostMapping("/addclasstask")
    public ServerResponse addClassTask(@RequestBody ClassTaskDTO c) {
        ClassTask classTask = new ClassTask();
        BeanUtils.copyProperties(c, classTask);
        log.info("新增课程任务，semester={}, courseNo={}, classNo={}",
                classTask.getSemester(), classTask.getCourseNo(), classTask.getClassNo());
        if (classTaskService.save(classTask)) {
            scheduleLogMirrorService.mirrorTask(classTask);
            return ServerResponse.ofSuccess("添加课程任务成功");
        }
        return throwBusiness(ResultCode.SYSTEM_ERROR, "添加课程任务失败");
    }


    /**
     * 删除开课任务
     * @param id
     */
    @DeleteMapping("/deleteclasstask/{id}")
    public ServerResponse deleteClassTask(@PathVariable("id") Integer id) {
        ClassTask classTask = classTaskService.getById(id);
        if (classTask != null && classTaskService.removeById(id)) {
            scheduleLogMirrorService.removeTaskMirror(classTask);
            return ServerResponse.ofSuccess("删除成功");
        }
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
        LambdaQueryWrapper<ClassTask> wrapper = new LambdaQueryWrapper<ClassTask>().select(ClassTask::getSemester).groupBy(ClassTask::getSemester);
        List<ClassTask> list = classTaskService.list(wrapper);

        Set<String> set = list.stream().map(ClassTask::getSemester).collect(Collectors.toSet());

        return ServerResponse.ofSuccess(set);
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

    private void requireClassTaskExists(Integer id) {
        if (id == null || classTaskService.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "开课任务不存在");
        }
    }

    private ServerResponse throwBusiness(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode, message);
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
        Map<String, ClassTask> legacyTaskMap = classTaskService.list(
                        new LambdaQueryWrapper<ClassTask>().eq(ClassTask::getSemester, semester))
                .stream()
                .collect(Collectors.toMap(this::buildLegacyTaskKey, item -> item, (left, right) -> left, HashMap::new));

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
}
