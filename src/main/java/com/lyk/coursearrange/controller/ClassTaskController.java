package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.ScheduleTaskPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    private SchTaskService schTaskService;

    /**
     * 查询开课任务
     */
    @GetMapping("/classtask/{page}/{semester}")
    public ServerResponse queryClassTask(@PathVariable("page") Integer page,
                                         @PathVariable("semester") String semester,
                                         @RequestParam(defaultValue = "10") Integer limit) {
        IPage<ScheduleTaskPageVO> standardPage = queryStandardTaskPage(semester, page, limit);
        if (standardPage != null) {
            return ServerResponse.ofSuccess(standardPage);
        }
        return ServerResponse.ofSuccess(new Page<>(page, limit, 0));
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
        return ServerResponse.ofSuccess(listStandardSemesters());
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

    private IPage<ScheduleTaskPageVO> queryStandardTaskPage(String semester, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SchTask> wrapper = new LambdaQueryWrapper<SchTask>()
                .eq(SchTask::getDeleted, 0)
                .like(SchTask::getRemark, "semester=" + semester)
                .orderByDesc(SchTask::getUpdatedAt)
                .orderByDesc(SchTask::getId);
        Page<SchTask> taskPage = schTaskService.page(new Page<>(pageNum, pageSize), wrapper);
        if (taskPage.getTotal() <= 0) {
            return new Page<>(pageNum, pageSize, 0);
        }
        Page<ScheduleTaskPageVO> resultPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        resultPage.setRecords(taskPage.getRecords().stream()
                .map(this::buildStandardTaskVO)
                .toList());
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
