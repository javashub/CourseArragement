package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: 15760
 * @Date: 2020/5/20
 * @Descripe: 获取系统相关的数据
 */
@RestController
public class SystemController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseInfoService courseInfoService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private ClassTaskService classTaskService;
    @Autowired
    private TeachbuildInfoService teachbuildInfoService;

    @GetMapping("/systemdata")
    public ServerResponse systemData() {
        Map<String, Object> map = new HashMap<>();

        // 讲师人数
        int teachers = teacherService.count();
        // 学生人数
        int students = studentService.count();
        // 教材数量
        int courses = courseInfoService.count();
        // 网课数量

        // 班级数量
        int classes = classInfoService.count();
        // 教学楼数量
        int teachbuilds = teachbuildInfoService.count();
        // 教室数量

        // 当前课程任务数量
        int classtasks = classTaskService.count();
        // 学习文档数

        // 题库数量

        map.put("teachers", teachers);
        map.put("students", students);
        map.put("courses", courses);
        map.put("classes", classes);
        map.put("teachbuilds", teachbuilds);
        map.put("classtasks", classtasks);

        return ServerResponse.ofSuccess(map);
    }

}
