package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.PasswordVO;
import com.lyk.coursearrange.entity.request.TeacherAddRequest;
import com.lyk.coursearrange.entity.request.UserLoginRequest;
import com.lyk.coursearrange.service.TeacherService;
import com.lyk.coursearrange.service.impl.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-03-13
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private TokenService tokenService;
    /**
     * 讲师登录
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse teacherLogin(@RequestBody UserLoginRequest userLoginRequest) {
        Map<String, Object> map = new HashMap<>();
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

        if (teacher != null) {
            // 允许登录
            String token = tokenService.getToken(teacher);
            map.put("teacher", teacher);
            map.put("token", token);
            return ServerResponse.ofSuccess(map);
        }
        // 否则一律视为密码错误
        return ServerResponse.ofError("密码错误");
    }

    /**
     * 根据id查询讲师，用于更新操作
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
    @PostMapping("/modify")
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
    @GetMapping("/query/{page}")
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
    @GetMapping("/search/{page}/{keyword}")
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

    /**
     * 用于给讲师生成讲师编号,返回一个讲师编号
     * @return
     */
    @GetMapping("/no")
    public ServerResponse getTeacherNo() {

        List<Teacher> teacherList = teacherService.list(new QueryWrapper<Teacher>().select().orderByDesc("teacher_no"));

        // 返回最大编号的讲师编号再+1给新添加的讲师
        return ServerResponse.ofSuccess(teacherList.get(0).getTeacherNo());
    }

    /**
     * 管理员添加讲师,默认密码是123456
     * @param t
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addTeacher(@RequestBody TeacherAddRequest t) {
        Teacher teacher = new Teacher();
        teacher.setTeacherNo(t.getTeacherNo());
        teacher.setUsername(t.getUsername());
        teacher.setEmail(t.getEmail());
        // 每一个新增的讲师密码默认是123456
        teacher.setPassword("123456");
        teacher.setRealname(t.getRealname());
        teacher.setJobtitle(t.getJobtitle());
        teacher.setTeach(t.getTeach());
        teacher.setTelephone(t.getTelephone());
        teacher.setAddress(t.getAddress());
        teacher.setAge(t.getAge());
        boolean b = teacherService.save(teacher);
        if (b) {
            return ServerResponse.ofSuccess("添加讲师成功！");
        }
        return ServerResponse.ofError("添加讲师失败！");
    }

    /**
     * 根据ID封禁、解封讲师账号，状态为0时正常，1时封禁
     * @param id
     * @return
     */
    @GetMapping("/lock/{id}")
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
     * 修改密码
     * @param passwordVO
     * @return
     */
    @PostMapping("/password")
    public ServerResponse updatePass(@RequestBody PasswordVO passwordVO) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper();
        wrapper.eq("id", passwordVO.getId());
        wrapper.eq("password", passwordVO.getOldPass());
        Teacher teacher = teacherService.getOne(wrapper);
        if (teacher == null) {
            return ServerResponse.ofError("旧密码错误");
        }
        // 否则进入修改密码流程
        teacher.setPassword(passwordVO.getNewPass());
        boolean b = teacherService.updateById(teacher);
        if (b) {
            return ServerResponse.ofSuccess("密码修改成功");
        }
        return ServerResponse.ofError("密码更新失败");
    }

    /**
     * 查询所有讲师
     * @return
     */
    @GetMapping("/all")
    public ServerResponse getAllTeacher() {

        return ServerResponse.ofSuccess(teacherService.list());
    }

}

