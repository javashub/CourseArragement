package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.request.ClassTaskRequest;
import com.lyk.coursearrange.service.ClassTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 *
 * @author lequal
 * @since 2020-04-03
 */
@RestController
@RequestMapping("/classTask")
public class ClassTaskController {

    @Autowired
    private ClassTaskService classTaskService;

    /**
     * 查询开课任务
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/classtask/{page}")
    public ServerResponse queryClassTask(@PathVariable("page") Integer page,
                                         @RequestParam(defaultValue = "10")Integer limit) {
        Page<ClassTask> pages = new Page<>(page, limit);
        QueryWrapper<ClassTask> wrapper = new QueryWrapper<ClassTask>().orderByDesc("update_time");
        IPage<ClassTask> ipage = classTaskService.page(pages, wrapper);
        return ServerResponse.ofSuccess(ipage);
    }

    @PostMapping("/addtask")
    public ServerResponse addTask(@RequestBody ClassTaskRequest ct) {
        ClassTask c = new ClassTask();
        // 赋值
        c.setGradeNo(ct.getGradeNo());
        c.setClassNo(ct.getClassNo());
        c.setCourseNo(ct.getCourseNo());
        c.setTeacherNo(ct.getTeacherNo());
        c.setCourseAttr(ct.getCourseAttr());
        c.setStudentNum(ct.getStudentNum());
        c.setWeeksNumber(ct.getWeeksNumber()); // 一周上课课时
        c.setWeeksSum(ct.getWeeksSum()); // 周数
        c.setIsFix(ct.getIsFix());
        c.setClassTime(ct.getClassTime());
        boolean b = classTaskService.save(c);
        if (b) {
            return ServerResponse.ofSuccess("添加任务成功", classTaskService.list());
        }
        return ServerResponse.ofError("添加失败");
    }
}

