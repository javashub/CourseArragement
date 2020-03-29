package com.lyk.coursearrange.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.AdminLoginRequest;
import com.lyk.coursearrange.entity.request.TeacherAddRequest;
import com.lyk.coursearrange.service.AdminService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
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


    // ↓↓↓↓↓↓↓↓↓    管理员对自己的操作       ↓↓↓↓↓↓↓↓

    /**
     * 普通管理员登录
     * @param adminLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse adminLogin(@RequestBody AdminLoginRequest adminLoginRequest) {

        Admin admin = adminService.adminLogin(adminLoginRequest.getUsername(), adminLoginRequest.getPassword());
        if (admin != null){
            return ServerResponse.ofSuccess(admin);
        }
        return ServerResponse.ofError("用户名或密码错误!");
    }

    /**
     * 管理员更新个人资料
     * @return
     */
    @PostMapping("/modifyadmin")
    public ServerResponse modifyAdmin(@RequestBody Admin admin) {
        // 修改
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


    // ↓↓↓↓↓↓↓↓↓    管理员对讲师的操作       ↓↓↓↓↓↓↓↓

    /**
     * 管理员添加讲师
     * @return
     */
    @PostMapping("/addteacher")
    public ServerResponse addAdmin(@RequestBody TeacherAddRequest teacherAddRequest) {

        return null;
    }

    /**
     * 管理员根据ID删除讲师
     * @return
     */
    @DeleteMapping("/deleteteacher/{id}")
    public ServerResponse deleteTeacher(@PathVariable Integer id) {
        boolean b = teacherService.removeById(id);
        if(b) {
            return ServerResponse.ofSuccess("删除成功！");
        }
        return ServerResponse.ofError("删除失败！");
    }


    /**
     * 管理员根据ID修改讲师信息
     * 修改后的讲师信息回写给数据库
     * @return
     */
    @PostMapping("/modifyteacher")
    public ServerResponse modifyTeacher(@RequestBody Teacher teacher) {
        // 先调用这里下面那个方法:/queryTeacher/{id}
        return teacherService.updateById(teacher) ? ServerResponse.ofSuccess("修改成功！") : ServerResponse.ofError("修改失败！");
    }


    /**
     * 根据id获取讲师实体的信息
     * 先将讲师的信息查询出来返回给前端界面
     * @param id
     * @return
     */
    @GetMapping("/queryteacher/{id}")
    public ServerResponse queryTeacherById(@PathVariable("id")Integer id){
        return ServerResponse.ofSuccess(teacherService.getById(id));
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


//    /**
//     * 查询所有讲师，带分页
//     * @return
//     */
//    @GetMapping("/queryteacher")
//    public ServerResponse queryTeacher(@RequestParam(defaultValue = "1")Integer page, @RequestParam(defaultValue = "10")Integer limit) {
//        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
//        wrapper.orderByDesc("update_time");
//        Page<Teacher> pages = new Page<>(page, limit);
//        // 调用分页查询
//        teacherService.page(pages, wrapper);
//        List<Teacher> list = pages.getRecords();
////        Long total = pages.getTotal();
////        Map<String, Object> map = new HashMap<>();
////        map.put("total", total);
////        map.put("data", list);
//        if (list != null) {
//            return ServerResponse.ofSuccess(list);
//        }
//        return ServerResponse.ofError("查询不到数据！");
//    }



    // ↓↓↓↓↓↓↓↓↓    管理员对学生的操作       ↓↓↓↓↓↓↓↓

    /**
     * 获取所有学生，带分页
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/querystudent")
    public ServerResponse queryStudent(@RequestParam(defaultValue = "1")Integer page,
                                       @RequestParam(defaultValue = "10")Integer limit) {
        Page<Student> pages = new Page<>(page, limit);
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        studentService.page(pages, wrapper);
        List<Student> list = pages.getRecords();
        if (list != null) {
            return ServerResponse.ofSuccess(list);
        }
        return ServerResponse.ofError("查询不到数据");
    }

    // 根据班级查询学生，学生表中班级字段已经是String类型的班级编号了，因此可以直接查询
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


    // 封禁学生


    // 搜索学生


    // 删除学生



    // ↓↓↓↓↓↓↓↓↓    管理员对课程的操作       ↓↓↓↓↓↓↓↓

    // 添加课程



    // 删除课程



    // 更新课程



    // 添加开课任务













    // 添加教学楼



    // 删除教学楼



    // 修改教学楼




}

