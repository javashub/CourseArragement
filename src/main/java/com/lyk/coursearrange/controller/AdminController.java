package com.lyk.coursearrange.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.PasswordVO;
import com.lyk.coursearrange.entity.request.UserLoginRequest;
import com.lyk.coursearrange.entity.request.TeacherAddRequest;
import com.lyk.coursearrange.service.AdminService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherService;
import com.lyk.coursearrange.service.impl.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-03-06
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private TokenService tokenService;


    /**
     * 获得系统数据
     * @return
     */
    @GetMapping("/systemdata")
    public ServerResponse systemData() {
        // 学生数量
        int students = studentService.count();
        // 讲师数量
        int teachers = teacherService.count();
        // 课程数量

        Map<String, Object> map = new HashMap<>();
        map.put("students", students);
        map.put("teachers", teachers);
        return ServerResponse.ofSuccess(map);
    }

    /**
     * 管理员登录
     * @param adminLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse adminLogin(@RequestBody UserLoginRequest adminLoginRequest) {
        Map<String, Object> map = new HashMap();
        Admin admin = adminService.adminLogin(adminLoginRequest.getUsername(), adminLoginRequest.getPassword());
        if (admin != null){
            String token = tokenService.getToken(admin);
            map.put("admin", admin);
            map.put("token", token);
            return ServerResponse.ofSuccess(map);
        }
        return ServerResponse.ofError("用户名或密码错误!");
    }

    /**
     * 管理员更新个人资料
     * @return
     */
    @PostMapping("/modifyadmin")
    public ServerResponse modifyAdmin(@RequestBody Admin admin) {

        return adminService.updateById(admin) ? ServerResponse.ofSuccess("更新成功！") : ServerResponse.ofError("更新失败！");
    }

    /**
     * 根据ID查询管理员实体信息
     * @param id
     * @return
     */
    @GetMapping("/queryadmin/{id}")
    public ServerResponse queryAdmin(@PathVariable("id") Integer id) {
        return ServerResponse.ofSuccess(adminService.getById(id));
    }




    /**
     * 管理员添加讲师
     * @return
     */
    @PostMapping("/addteacher")
    public ServerResponse addAdmin(@RequestBody TeacherAddRequest teacherAddRequest) {

        return null;
    }



    /**
     * 根据ID封禁、解封讲师账号，状态为0时正常，1时封禁
     * @param id
     * @return
     */
    @GetMapping("/lockteacher/{id}")
    public ServerResponse lockTeacher(@PathVariable("id") Integer id) {

        // 先查出来再修改，
        Teacher teacher = teacherService.getById(id);
        // 修改
        if (teacher.getStatus() == 0) {
            teacher.setStatus(1);
        } else {
            teacher.setStatus(0);
        }
        teacherService.updateById(teacher);
        return ServerResponse.ofSuccess("操作成功！");
    }


    /**
     * 根据班级查询学生
     * @param classno
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/querystudentbyclassno/{classno}")
    public ServerResponse queryStudentByClassno(@PathVariable("classno") String classno,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer limit) {
        Page<Student> pages = new Page<>();
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        wrapper.eq("class_no", classno).orderByDesc("update_time");
        studentService.page(pages, wrapper);
        // 查询学生列表
        List<Student> list = pages.getRecords();
        if (list != null) {
            return ServerResponse.ofSuccess(list);
        }
        return ServerResponse.ofError("查询不到数据");
    }

    /**
     * 管理员修改密码
     * @param passwordVO
     * @return
     */
    @PostMapping("/password")
    public ServerResponse updatePass(@RequestBody PasswordVO passwordVO) {
        System.out.println(passwordVO + "======");
        QueryWrapper<Admin> wrapper = new QueryWrapper();
        wrapper.eq("id", passwordVO.getId());
        wrapper.eq("password", passwordVO.getOldPass());
        Admin admin = adminService.getOne(wrapper);
        if (admin == null) {
            return ServerResponse.ofError("旧密码错误");
        }
        // 否则进入修改密码流程
        admin.setPassword(passwordVO.getNewPass());
        boolean b = adminService.updateById(admin);
        if (b) {
            return ServerResponse.ofSuccess("密码修改成功");
        }
        return ServerResponse.ofError("密码更新失败");
    }


}

