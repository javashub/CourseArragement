package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.StudentDao;
import com.lyk.coursearrange.dao.TeacherDao;
import com.lyk.coursearrange.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: 15760
 * @Date: 2020/5/20
 * @Descripe: 获取系统相关的数据
 */
@RestController
public class SystemController {

    @Resource
    private TeacherService teacherService;
    @Resource
    private StudentService studentService;
    @Resource
    private CourseInfoService courseInfoService;
    @Resource
    private ClassInfoService classInfoService;
    @Resource
    private ClassTaskService classTaskService;
    @Resource
    private TeachbuildInfoService teachbuildInfoService;
    @Resource
    private ClassroomService classroomService;
    @Resource
    private StudentDao studentDao;
    @Resource
    private TeacherDao teacherDao;


    /**
     * 随便瞎写。生产环境是禁止使用 map 传参的，全部都应该使用对象传参，因为 map 不可预知集合中的内容
     */
    @GetMapping("/systemdata")
    public ServerResponse systemData() {
        Map<String, Object> map = new HashMap<>();

        // 讲师人数
        int teachers = teacherService.count();
        // 学生人数
        int students = studentService.count();
        // 教材数量
        int courses = courseInfoService.count();
        // 班级数量
        int classes = classInfoService.count();
        // 教学楼数量
        int teachbuilds = teachbuildInfoService.count();
        // 教室数量
        int classrooms = classroomService.count();
        // 当前课程任务数量
        int classtasks = classTaskService.count();

        LocalDate totay = LocalDate.now();
        String yesterday = totay.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 昨日学生注册人数
        int studentReg = studentDao.studentReg(yesterday);
        // 昨日注册讲师
        int teacherReg = teacherDao.teacherReg(yesterday);


        map.put("teachers", teachers);
        map.put("students", students);
        map.put("courses", courses);
        map.put("classes", classes);
        map.put("teachbuilds", teachbuilds);
        map.put("classtasks", classtasks);
        map.put("classrooms", classrooms);
        map.put("studentReg", studentReg);
        map.put("teacherReg", teacherReg);

        return ServerResponse.ofSuccess(map);
    }

}
