package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.StudentDao;
import com.lyk.coursearrange.dao.TeacherDao;
import com.lyk.coursearrange.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.SimpleDateFormat;
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
    @Autowired
    private DocService docService;
    @Autowired
    private ExerciseService exerciseService;
    @Autowired
    private ClassroomService classroomService;
    @Autowired
    private StudentDao studentDao;
    @Autowired
    private  OnlineCourseService onlineCourseService;
    @Autowired
    private TeacherDao teacherDao;


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
        // 学习文档数
        int docs = docService.count();
        // 题库数量
        int exercises = exerciseService.count();
        // 网课数量
        int onlineCourse = onlineCourseService.count();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-1);
        Date d = cal.getTime();
        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
        //获取昨天日期
        String yesday = sp.format(d);
//        String yesday = "2020-03-26";

        // 昨日学生注册人数
        int studentReg = studentDao.studentReg(yesday);
        // 昨日注册讲师
        int teacherReg = teacherDao.teacherReg(yesday);


        map.put("teachers", teachers);
        map.put("students", students);
        map.put("courses", courses);
        map.put("classes", classes);
        map.put("teachbuilds", teachbuilds);
        map.put("classtasks", classtasks);
        map.put("docs", docs);
        map.put("exercises", exercises);
        map.put("classrooms", classrooms);
        map.put("studentReg", studentReg);
        map.put("onlineCourse", onlineCourse);
        map.put("teacherReg", teacherReg);

        return ServerResponse.ofSuccess(map);
    }

}
