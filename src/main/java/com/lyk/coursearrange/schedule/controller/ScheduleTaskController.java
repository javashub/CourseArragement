package com.lyk.coursearrange.schedule.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.service.ClassTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    private final ScheduleLogMirrorService scheduleLogMirrorService;

    public ScheduleTaskController(ClassTaskService classTaskService,
                                  ScheduleLogMirrorService scheduleLogMirrorService) {
        this.classTaskService = classTaskService;
        this.scheduleLogMirrorService = scheduleLogMirrorService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@RequestParam String semester,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<ClassTask> wrapper = new LambdaQueryWrapper<ClassTask>()
                .eq(ClassTask::getSemester, semester)
                .orderByDesc(ClassTask::getId);
        IPage<ClassTask> page = classTaskService.page(new Page<>(pageNum, pageSize), wrapper);
        return ServerResponse.ofSuccess(page);
    }

    @PostMapping
    public ServerResponse<?> save(@RequestBody ClassTaskDTO request) {
        ClassTask classTask = new ClassTask();
        BeanUtils.copyProperties(request, classTask);
        log.info("新增标准排课任务，semester={}, courseNo={}, classNo={}",
                classTask.getSemester(), classTask.getCourseNo(), classTask.getClassNo());
        if (classTaskService.save(classTask)) {
            scheduleLogMirrorService.mirrorTask(classTask);
            return ServerResponse.ofSuccess("添加课程任务成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加课程任务失败");
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable Integer id) {
        requireClassTaskExists(id);
        ClassTask classTask = classTaskService.getById(id);
        if (classTaskService.removeById(id)) {
            scheduleLogMirrorService.removeTaskMirror(classTask);
            return ServerResponse.ofSuccess("删除成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除失败");
    }

    @GetMapping("/semesters")
    public ServerResponse<?> listSemesters() {
        LambdaQueryWrapper<ClassTask> wrapper = new LambdaQueryWrapper<ClassTask>()
                .select(ClassTask::getSemester)
                .groupBy(ClassTask::getSemester);
        List<ClassTask> list = classTaskService.list(wrapper);
        Set<String> semesters = list.stream().map(ClassTask::getSemester).collect(Collectors.toSet());
        return ServerResponse.ofSuccess(semesters);
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

    private void requireClassTaskExists(Integer id) {
        if (id == null || classTaskService.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "开课任务不存在");
        }
    }
}
