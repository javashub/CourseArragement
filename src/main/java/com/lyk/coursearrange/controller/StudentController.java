package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.request.StudentLoginRequest;
import com.lyk.coursearrange.entity.request.StudentRegisterRequest;
import com.lyk.coursearrange.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lequal
 * @since 2020-03-13
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;


    /**
     * 学生登录
     * @param studentLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse studentLogin(@RequestBody StudentLoginRequest studentLoginRequest) {
        System.out.println("学生登录进来了。。。。。");
        // 构造查询该学生帐号状态的条件，后面需要修改条件成学号、用户名、真实姓名这些条件
        // 或者干脆用用户名登录好了
        QueryWrapper<Student> wrapper = new QueryWrapper<Student>().eq("student_no", studentLoginRequest.getUsername());
        // 查询是否有该学生
        Student student2 = studentService.getOne(wrapper);
        Student student = studentService.studentLogin(studentLoginRequest.getUsername(), studentLoginRequest.getPassword());

        if (student2 == null) {
            return ServerResponse.ofError("学生账号不存在!");

        }else if (student2.getStatus() != 0) {
            // 否则进行下一步验证账号的的状态
            return ServerResponse.ofError("该学生账号异常，请联系管理员");
        } else if (student != null) {
            //允许登录
            return ServerResponse.ofSuccess(student);
        }

        return ServerResponse.ofSuccess("账号或密码错误！");
    }

    /**
     * 学生注册
     * @param studentRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public ServerResponse studentRegister(@RequestBody StudentRegisterRequest studentRegisterRequest) {

        return ServerResponse.ofError("注册失败!");
    }

    /**
     * 修改学生信息(先查询出来再修改)
     * @param student
     * @return
     */
    @PostMapping("/modifystudent")
    public ServerResponse modifyStudent(@RequestBody Student student) {
        // 修改
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


    // 选网课


    // 查看课表


    // 预约场地


}

