package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.UserLoginRequest;
import com.lyk.coursearrange.service.TeacherService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lequal
 * @since 2020-03-13
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * 讲师登录
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse teacherLogin(@RequestBody UserLoginRequest userLoginRequest) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_no", userLoginRequest.getUsername());
        // 先查询是否有该账号
        Teacher teacher2 = teacherService.getOne(wrapper);
        if (teacher2 == null) {
            return ServerResponse.ofError("账号不存在");
        } else if (teacher2.getStatus() != 0) {
            return ServerResponse.ofError("账号状态异常，请联系管理员");
        }
        // 登录,使用编号登录
        Teacher teacher = teacherService.teacherLogin(userLoginRequest.getUsername(), userLoginRequest.getPassword());
        // 判断
        if (teacher != null) {
            // 允许登录
            return ServerResponse.ofSuccess(teacher);
        }
        // 否则一律视为密码错误
        return ServerResponse.ofError("密码错误");
    }

    /**
     * 根据id查询讲师
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ServerResponse queryTeacherById(@PathVariable("id") Integer id) {

        return ServerResponse.ofSuccess(teacherService.getById(id));
    }

    /**
     * 更新讲师
     * @param teacher
     * @return
     */
    @PostMapping("/modifyteacher")
    public ServerResponse modifyTeacher(@RequestBody Teacher teacher) {

        boolean b = teacherService.updateById(teacher);

        if (b) {
            return ServerResponse.ofSuccess("更新成功");
        }
        return ServerResponse.ofError("更新失败");
    }

    /**
     * 分页查询讲师
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/queryteacher/{page}")
    public ServerResponse queryTeacher(@PathVariable(value = "page") Integer page,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        Page<Teacher> pages = new Page<>(page, limit);
        QueryWrapper<Teacher> wrapper = new QueryWrapper<Teacher>().orderByDesc("teacher_no");
        IPage<Teacher> iPage = teacherService.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 根据姓名关键字搜索讲师
     * @return
     */
    @GetMapping("/searchteacher/{page}/{keyword}")
    public ServerResponse searchTeacher(@PathVariable("keyword") String keyword, @PathVariable("page") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        wrapper.like(!StringUtils.isEmpty(keyword), "realname", keyword);
        Page<Teacher> pages = new Page<>(page, limit);
        IPage<Teacher> iPage = teacherService.page(pages, wrapper);
        if (page != null) {
            return ServerResponse.ofSuccess(iPage);
        }
        return ServerResponse.ofError("查询失败!");
    }

    /**
     * 管理员根据ID删除讲师
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeacher(@PathVariable Integer id) {
        boolean b = teacherService.removeById(id);
        if(b) {
            return ServerResponse.ofSuccess("删除成功！");
        }
        return ServerResponse.ofError("删除失败！");
    }

    // 查询讲师的课表

}

