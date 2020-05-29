package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.UserLoginToken;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.request.StudentLoginRequest;
import com.lyk.coursearrange.entity.request.StudentRegisterRequest;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.impl.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
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
        Map<String, Object> map = new HashMap<>();
        // 先判断是否有该学号，该学生
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
            map.put("student", student);
            map.put("token", token);
            return ServerResponse.ofSuccess(map);
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
        student.setStudentNo(stu.getStudentNo());
        student.setUsername(stu.getUsername());
        student.setPassword(stu.getPassword());
        student.setRealname(stu.getRealname());
        student.setGrade(stu.getGrade());
        student.setAddress(stu.getAddress());
        student.setTelephone(stu.getTelephone());
        boolean b = studentService.save(student);
        if (b) {
            return ServerResponse.ofSuccess("注册成功", student);
        }
        return ServerResponse.ofError("注册失败!");
    }

    /**
     * 修改学生信息
     * @param student
     * @return
     */
    @PostMapping("/modify")
    @UserLoginToken
    public ServerResponse modifyStudent(@RequestBody Student student) {
        // 修改操作
        return studentService.updateById(student) ? ServerResponse.ofSuccess("修改成功") : ServerResponse.ofError("修改失败");
    }


    /**
     * 根据学生id获取
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @UserLoginToken
    public ServerResponse queryStudent(@PathVariable("id")Integer id){
        // 查询出来需要修改的学生实体
        return ServerResponse.ofSuccess(studentService.getById(id));
    }

    /**
     * 更新学生
     * @param student
     * @return
     */
    @PostMapping("/modify/{id}")
    public ServerResponse modifyTeacher(@PathVariable("id") Integer id, @RequestBody Student student) {

        QueryWrapper<Student> wrapper = new QueryWrapper<Student>().eq("id", id);
        boolean b = studentService.update(student, wrapper);

        if (b) {
            return ServerResponse.ofSuccess("更新成功");
        }
        return ServerResponse.ofError("更新失败");
    }

    /**
     * 学生查询自己的课表,根据学生所在班级查询自己的课表
     * @return
     */
    @GetMapping("/coursetable/{classNo}")
    public ServerResponse queryStudentCourse(@PathVariable("classNo") String classNo) {

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


    /**
     * 获取所有学生，带分页
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/students/{page}")
    public ServerResponse queryStudent(@PathVariable("page") Integer page,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        Page<Student> pages = new Page<>(page, limit);
        QueryWrapper<Student> wrapper = new QueryWrapper<Student>().orderByDesc("student_no");
        IPage<Student> iPage = studentService.page(pages, wrapper);

        return ServerResponse.ofSuccess(iPage);

    }

    /**
     * 根据姓名关键字搜学生
     * @return
     */
    @GetMapping("/search/{keyword}")
    public ServerResponse searchTeacher(@PathVariable("keyword") String keyword, @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        wrapper.like(!StringUtils.isEmpty(keyword), "realname", keyword);
        Page<Student> pages = new Page<>(page, limit);
        IPage<Student> iPage = studentService.page(pages, wrapper);
        if (page != null) {
            return ServerResponse.ofSuccess(iPage);
        }
        return ServerResponse.ofError("查询不到数据!");
    }

    /**
     * 管理员根据ID删除学生
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeacher(@PathVariable Integer id) {
        boolean b = studentService.removeById(id);
        if(b) {
            return ServerResponse.ofSuccess("删除成功！");
        }
        return ServerResponse.ofError("删除失败！");
    }


}

