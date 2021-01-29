package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.service.ClassTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * @author lequal
 * @since 2020-04-06
 */
@RestController
public class ClassTaskController {

    Logger log = LoggerFactory.getLogger(ClassTaskController.class);

    @Autowired
    private ClassTaskService classTaskService;

    /**
     * 查询开课任务
     */
    @GetMapping("/classtask/{page}/{semester}")
    public ServerResponse queryClassTask(@PathVariable("page") Integer page,
                                         @PathVariable("semester") String semester,
                                         @RequestParam(defaultValue = "10") Integer limit) {
        Page<ClassTask> pages = new Page<>(page, limit);
        QueryWrapper<ClassTask> wrapper = new QueryWrapper<ClassTask>().eq("semester", semester);
        IPage<ClassTask> ipage = classTaskService.page(pages, wrapper);

        if (ipage != null) {
            return ServerResponse.ofSuccess(ipage);
        }
        return ServerResponse.ofError("查询开课任务失败！");
    }

    /**
     * 手动添加课程任务
     *
     * @param c
     * @return
     */
    @PostMapping("/addclasstask")
    public ServerResponse addClassTask(@RequestBody() ClassTaskDTO c) {
        System.out.println(c);
        ClassTask classTask = new ClassTask();
        classTask.setSemester(c.getSemester());
        classTask.setGradeNo(c.getGradeNo());
        classTask.setClassNo(c.getClassNo());
        classTask.setCourseNo(c.getCourseNo());
        classTask.setCourseName(c.getCourseName());
        classTask.setTeacherNo(c.getTeacherNo());
        classTask.setRealname(c.getRealname());
        classTask.setCourseAttr(c.getCourseAttr());
        classTask.setStudentNum(c.getStudentNum());
        classTask.setWeeksNumber(c.getWeeksNumber());
        classTask.setWeeksSum(c.getWeeksSum());
        classTask.setIsFix(c.getIsFix());
        classTask.setClassTime(c.getClassTime());

        boolean b = classTaskService.save(classTask);

        if (b) {
            return ServerResponse.ofSuccess("添加课程任务成功");
        }
        return ServerResponse.ofError("添加课程任务失败");
    }


    /**
     * 删除开课任务
     * @param id
     * @return
     */
    @DeleteMapping("/deleteclasstask/{id}")
    public ServerResponse deleteClassTask(@PathVariable("id") Integer id) {

        boolean b = classTaskService.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return ServerResponse.ofError("删除失败");
    }

    /**
     * 获得学期集合,如：
     * 2019-2020-1
     * 2019-2020-2
     *
     * @return
     */
    @GetMapping("/semester")
    public ServerResponse queryAllSemester() {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.select("semester");
        wrapper.groupBy("semester");
        List<ClassTask> list = classTaskService.list(wrapper);
        Set set = new HashSet();

        for (ClassTask c : list) {
            set.add(c.getSemester());
        }

        return ServerResponse.ofSuccess(set);
    }

    /**
     * 排课算法接口，传入学期开始去查对应学期的开课任务，进行排课，排完课程后添加到course_plan表
     *
     * @param semester
     * @return
     */
    @PostMapping("/arrange/{semester}")
    public ServerResponse arrange(@PathVariable("semester") String semester) {
        return classTaskService.classScheduling(semester);
    }
}

