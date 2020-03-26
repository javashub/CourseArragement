package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.request.StudentLoginRequest;
import com.lyk.coursearrange.entity.request.StudentRegisterRequest;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.impl.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Random;

/**
 *  前端控制器
 *
 *
 * @author lequal
 * @since 2020-03-13
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;


    @Autowired
    private TokenService tokenService;
    /**
     * 学生登录
     * @param studentLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse studentLogin(@RequestBody StudentLoginRequest studentLoginRequest) {
        // 构造查询该学生帐号状态的条件，直接使用学号登录，用户名，真实姓名可能出现相同的
        // 或者干脆用用户名登录好了
        QueryWrapper<Student> wrapper = new QueryWrapper<Student>().eq("student_no", studentLoginRequest.getUsername());
        // 查询是否有该学生
        Student student2 = studentService.getOne(wrapper);

        if (student2 == null) {
            return ServerResponse.ofError("学生账号不存在!");

        }else if (student2.getStatus() != 0) {
            // 否则进行下一步验证账号的的状态
            return ServerResponse.ofError("该学生账号异常，请联系管理员");
        }
        // 调用登录
        Student student = studentService.studentLogin(studentLoginRequest.getUsername(), studentLoginRequest.getPassword());
        if (student != null) {
            //允许登录,返回token
            String token = tokenService.getToken(student);
            return ServerResponse.ofSuccess(token);
        }

        return ServerResponse.ofSuccess("密码错误！");
    }

    /**
     * 学生注册
     * @param stu
     * @return
     */
    @PostMapping("/register")
    public ServerResponse studentRegister(@RequestBody StudentRegisterRequest stu) {
        System.out.println(stu);
        Student student = new Student();
        student.setStudentNo(stu.getStudentNo()); // 学号
        student.setUsername(stu.getUsername()); // 用户名
        student.setPassword(stu.getPassword()); // 密码
        student.setRealname(stu.getRealname()); // 真实姓名
        student.setGrade(stu.getGrade()); // 年级
        student.setAddress(stu.getAddress()); // 地址
        student.setTelephone(stu.getTelephone()); // 联系方式
        boolean b = studentService.save(student);
        if (b) {
            return ServerResponse.ofSuccess("注册成功", student);
        }
        return ServerResponse.ofError("注册失败!");
    }

    /**
     * 修改学生信息(先查询出来再修改)
     * @param student
     * @return
     */
    @PostMapping("/modifystudent")
    public ServerResponse modifyStudent(@RequestBody Student student) {
        // 修改操作
        return studentService.updateById(student) ? ServerResponse.ofSuccess("修改成功") : ServerResponse.ofError("修改失败");
    }


    /**
     * 根据学生id获取实体信息再进行修改
     * @param id
     * @return
     */
    @GetMapping("/querystudent/{id}")
    public ServerResponse queryStudent(@PathVariable("id")Integer id){
        // 查询出来需要修改的学生实体
        return ServerResponse.ofSuccess(studentService.getById(id));
    }

    /**
     * 学生查询自己的课表,一人一课表
     * @return
     */
    @GetMapping("/querystudentcourse")
    public ServerResponse queryStudentCourse() {

        return ServerResponse.ofError();
    }

    /**
     * 给学生创建学号
     * @param grade
     * @return
     */
    @PostMapping("/createno/{grade}")
    public ServerResponse create(@PathVariable("grade") String grade) {
        Random r = new Random();
        // 得到当前年份字符串2020
        String str1 = LocalDateTime.now().getYear()+"";
        System.out.println(str1);
        // 得到10位学号,2020 02 7845
        do {
            // 随机四位数
            String str2 = String.valueOf(r.nextInt(10000));
            // 拼接学号  2020##****  十位(三个部分):  年:4位  年级:两位  随机数4位
            String str3 = str1 + grade + str2;
            // 查询学号是否已经存在的条件
            QueryWrapper<Student> wrapper = new QueryWrapper<Student>().eq("student_no", str3);
            Student student = studentService.getOne(wrapper);
            System.out.println("666666");
            // 如果查不到该学号，则学号可用，跳出循环
            if (student == null) {
                return ServerResponse.ofSuccess(str3);
            }
        } while(true);
    }




}

