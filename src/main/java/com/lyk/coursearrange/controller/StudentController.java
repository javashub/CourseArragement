package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.UserLoginToken;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.request.PasswordVO;
import com.lyk.coursearrange.entity.request.StudentLoginRequest;
import com.lyk.coursearrange.entity.request.StudentRegisterRequest;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.impl.TokenService;
import com.lyk.coursearrange.util.ClassUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @Resource
    private StudentService studentService;

    @Resource
    private TokenService tokenService;


    /**
     * 学生加入班级，只有加入班级后才可以看到本班的课表，文档
     *
     * @param id      学生id
     * @param classNo 班级编号
     * @return
     */
    @PostMapping("/join/{id}/{classNo}")
    public ServerResponse joinClass(@PathVariable("id") Integer id, @PathVariable("classNo") String classNo) {
        // TODO 学生加入年级，学生查看本班的文档(文档控制器中),查看自己所在的班级课表
        Student student = studentService.getById(id);
        student.setClassNo(classNo);
        return studentService.saveOrUpdate(student) ? ServerResponse.ofSuccess("加入班级成功") : ServerResponse.ofError("加入班级失败");
    }


    /**
     * 学生登录
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

        } else if (student2.getStatus() != 0) {
            // 否则进行下一步验证账号的的状态
            return ServerResponse.ofError("该学生账号异常，请联系管理员");
        }
        // 调用登录
        Student student = studentService.studentLogin(studentLoginRequest.getUsername(), studentLoginRequest.getPassword());
        if (null != student) {
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
     */
    @PostMapping("/register")
    public ServerResponse studentRegister(@RequestBody StudentRegisterRequest stu) {
        Student student = new Student();
        student.setStudentNo(stu.getStudentNo());
        student.setUsername(stu.getUsername());
        student.setPassword(stu.getPassword());
        student.setRealname(stu.getRealname());
        student.setGrade(stu.getGrade());
        student.setAddress(stu.getAddress());
        student.setTelephone(stu.getTelephone());
        student.setEmail(stu.getEmail());
        return studentService.save(student) ? ServerResponse.ofSuccess("注册成功", student) : ServerResponse.ofError("注册失败!");
    }

    /**
     * 修改学生信息
     *
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
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @UserLoginToken
    public ServerResponse queryStudent(@PathVariable("id") Integer id) {
        // 查询出来需要修改的学生实体
        return ServerResponse.ofSuccess(studentService.getById(id));
    }

    /**
     * 更新学生
     *
     * @param student
     * @return
     */
    @PostMapping("/modify/{id}")
    public ServerResponse modifyTeacher(@PathVariable("id") Integer id, @RequestBody Student student) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>().eq(Student::getId, id);
        return studentService.update(student, wrapper) ? ServerResponse.ofSuccess("更新成功") : ServerResponse.ofError("更新失败");
    }

    /**
     * 学生查询自己的课表,根据学生所在班级查询自己的课表
     *
     * @return
     */
    @Deprecated
    @GetMapping("/coursetable/{classNo}")
    public ServerResponse queryStudentCourse(@PathVariable("classNo") String classNo) {

        return ServerResponse.ofError();
    }

    /**
     * 给学生创建学号
     *
     * @param grade
     * @return
     */
    @PostMapping("/createno/{grade}")
    public ServerResponse create(@PathVariable("grade") String grade) {
        // 得到当前年份字符串2020
        String year = LocalDateTime.now().getYear() + "";

        // 得到10位学号,2020 02 7845
        do {
            // 随机四位数
            String randomNumber = String.valueOf(ClassUtil.RANDOM.nextInt(10000));
            // 拼接学号  2020##****  十位(三个部分):  年:4位  年级:两位  随机数4位
            String studentNo = year + grade + randomNumber;
            // 查询学号是否已经存在的条件
            LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo);
            Student student = studentService.getOne(wrapper);
            // 如果查不到该学号，则学号可用，跳出循环
            if (student == null) {
                return ServerResponse.ofSuccess(studentNo);
            }
        } while (true);
    }


    /**
     * 分页获取所有学生
     */
    @GetMapping("/students/{page}")
    public ServerResponse queryStudent(@PathVariable("page") Integer page,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        Page<Student> pages = new Page<>(page, limit);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>().orderByDesc(Student::getStudentNo);
        IPage<Student> iPage = studentService.page(pages, wrapper);

        return ServerResponse.ofSuccess(iPage);

    }

    /**
     * 根据姓名关键字搜学生
     */
    @GetMapping("/search/{keyword}")
    public ServerResponse searchTeacher(@PathVariable("keyword") String keyword, @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>().orderByDesc(Student::getUpdateTime)
                .likeRight(!StringUtils.isEmpty(keyword), Student::getRealname, keyword);
        IPage<Student> iPage = studentService.page(new Page<>(page, limit), wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 管理员根据ID删除学生
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeacher(@PathVariable Integer id) {
        return studentService.removeById(id) ? ServerResponse.ofSuccess("删除成功！") : ServerResponse.ofError("删除失败！");
    }

    /**
     * 学生修改密码
     */
    @PostMapping("/password")
    public ServerResponse updatePass(@RequestBody PasswordVO passwordVO) {

        LambdaQueryWrapper<Student> wrapper =
                new LambdaQueryWrapper<Student>().eq(Student::getId, passwordVO.getId()).eq(Student::getPassword, passwordVO.getOldPass());
        Student student = studentService.getOne(wrapper);
        if (null == student) {
            return ServerResponse.ofError("旧密码错误");
        }
        // 否则进入修改密码流程
        student.setPassword(passwordVO.getNewPass());
        return studentService.updateById(student) ? ServerResponse.ofSuccess("密码修改成功") : ServerResponse.ofError("密码更新失败");
    }


}

