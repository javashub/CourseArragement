package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
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

    /**
     * 查询开课任务
     */
    @GetMapping("/classtask/{page}/{semester}")
    public ServerResponse queryClassTask(@PathVariable("page") Integer page,
                                         @PathVariable("semester") String semester,
                                         @RequestParam(defaultValue = "10") Integer limit) {

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
        requireClassTaskExists(id);
        ClassTask classTask = classTaskService.getById(id);
        if (classTaskService.removeById(id)) {
            scheduleLogMirrorService.removeTaskMirror(classTask);
            return ServerResponse.ofSuccess("删除成功");
        }
        return throwBusiness(ResultCode.SYSTEM_ERROR, "删除失败");
    }

    /**
     * 获得学期集合
     */
    @GetMapping("/semester")
    public ServerResponse queryAllSemester() {
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
}
