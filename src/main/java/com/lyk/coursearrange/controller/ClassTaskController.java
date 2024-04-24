package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.service.ClassTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lequal
 * @since 2020-04-06
 */
@RestController
public class ClassTaskController {

    @Resource
    private ClassTaskService classTaskService;

    /**
     * 查询开课任务
     */
    @GetMapping("/classtask/{page}/{semester}")
    public ServerResponse queryClassTask(@PathVariable("page") Integer page,
                                         @PathVariable("semester") String semester,
                                         @RequestParam(defaultValue = "10") Integer limit) {

        LambdaQueryWrapper<ClassTask> wrapper = new LambdaQueryWrapper<ClassTask>().eq(ClassTask::getSemester, semester);
        IPage<ClassTask> ipage = classTaskService.page(new Page<>(page, limit), wrapper);
        return ipage != null ? ServerResponse.ofSuccess(ipage) : ServerResponse.ofError("查询开课任务失败！");
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
        return classTaskService.save(classTask) ? ServerResponse.ofSuccess("添加课程任务成功") : ServerResponse.ofError("添加课程任务失败");
    }


    /**
     * 删除开课任务
     * @param id
     */
    @DeleteMapping("/deleteclasstask/{id}")
    public ServerResponse deleteClassTask(@PathVariable("id") Integer id) {
        return classTaskService.removeById(id) ? ServerResponse.ofSuccess("删除成功") : ServerResponse.ofError("删除失败");
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
        return classTaskService.classScheduling(semester);
    }
}

