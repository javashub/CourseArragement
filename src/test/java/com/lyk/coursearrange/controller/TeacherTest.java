//package com.lyk.coursearrange.controller;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.annotation.PostConstruct;
//import java.time.LocalDateTime;
//
///**
// * @author: 15760
// * @Date: 2020/3/6
// * @Descripe: 教师类测试
// */
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class testTeacher {
//
//    @Autowired
//    private TeacherDao teacherDao;
//
//    @Test
//    @PostConstruct
//    public void testAdd() {
//        Teacher teacher = new Teacher();
//        teacher.setId(4);
//        teacher.setTeacherNo("10012");
//        teacher.setUsername("msLi");
//        teacher.setPassword("123");
//        teacher.setRealname("李雪雪");
//        teacher.setJobtitle("高级讲师");
//        teacher.setTeach("语文");
//        teacher.setAge(29);
//        teacher.setTelephone("18978899898");
//        teacher.setAddress("广西桂林市桂林电子科技大学");
//        teacher.setDescription("做人民的好教师");
//        teacher.setPower(1);
//        teacher.setPiority(2);
//        teacher.setStatus(0);
//        teacher.setCreateTime(LocalDateTime.now());
//        teacher.setUpdateTime(LocalDateTime.now());
//        teacherDao.insert(teacher);
//    }
//}
