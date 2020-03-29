package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.TeacherLoginRequest;
import com.lyk.coursearrange.service.TeacherService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
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
     * @param teacherLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse teacherLogin(@RequestBody TeacherLoginRequest teacherLoginRequest) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_no", teacherLoginRequest.getUsername());
        // 先查询是否有该账号
        Teacher teacher2 = teacherService.getOne(wrapper);
        if (teacher2 == null) {
            return ServerResponse.ofError("账号不存在");
        } else if (teacher2.getStatus() != 0) {
            return ServerResponse.ofError("账号状态异常，请联系管理员");
        }
        // 登录,使用编号登录
        Teacher teacher = teacherService.teacherLogin(teacherLoginRequest.getUsername(), teacherLoginRequest.getPassword());
        // 判断
        if (teacher != null) {
            // 允许登录
            return ServerResponse.ofSuccess(teacher);
        }
        // 否则一律视为密码错误
        return ServerResponse.ofError("密码错误");
    }

    /**
     * 根据id查询讲师个人信息
     * @param id
     * @return
     */
    @GetMapping("/queryteacherbyid/{id}")
    public ServerResponse queryTeacherById(@PathVariable("id") Integer id) {

        return ServerResponse.ofSuccess(teacherService.getById(id));
    }

    @PostMapping("/modifyteacher")
    public ServerResponse modifyTeacher(@RequestBody Teacher teacher) {
        boolean b = teacherService.save(teacher);
        if (b) {
            return ServerResponse.ofSuccess("更新成功");
        }
        return ServerResponse.ofError("更新失败");
    }

    // 查询所有讲师，分页
    @GetMapping("/queryteacher")
    public ServerResponse queryTeacher(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        Page<Teacher> pages = new Page<>(page, limit);
        QueryWrapper<Teacher> wrapper = new QueryWrapper<Teacher>().orderByDesc("update_time");
        teacherService.page(pages, wrapper);
        List<Teacher> list = pages.getRecords();
        if (list != null) {
            return ServerResponse.ofSuccess(list);
        }
        return ServerResponse.ofError("查询不到数据");
    }

    /**
     * 根据姓名关键字搜索讲师
     * @return
     */
    @GetMapping("/searchteacher/{keyword}")
    public ServerResponse searchTeacher(@PathVariable("keyword") String keyword, @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        wrapper.like(!StringUtils.isEmpty(keyword), "realname", keyword);
        Page<Teacher> pages = new Page<>(page, limit);
        teacherService.page(pages, wrapper);
        List<Teacher> list = pages.getRecords();
        if (list != null) {
            return ServerResponse.ofSuccess(list);
        }
        return ServerResponse.ofError("查询失败!");
    }



    // 查询讲师的课表

}

