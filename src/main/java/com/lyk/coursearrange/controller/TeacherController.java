package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.lyk.coursearrange.util.AliyunUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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

    @Resource
    private TeacherService teacherService;
    @Resource
    private TokenService tokenService;


    /**
     * 上传讲师证件
     */
    @PostMapping("/upload/{id}")
    public ServerResponse uploadLicense(@PathVariable("id") Integer id, MultipartFile file) {
        Map<String, Object> map = AliyunUtil.upload(file, "license");
        assert map != null;
        String license = (String) map.get("url");
        Teacher t = teacherService.getById(id);
        t.setLicense(license);
        return teacherService.updateById(t) ? ServerResponse.ofSuccess("上传证件成功") : ServerResponse.ofError("上传证件失败");
    }


    /**
     * 讲师登录
     */
    @PostMapping("/login")
    public ServerResponse teacherLogin(@RequestBody UserLoginRequest userLoginRequest) {
        Map<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>().eq(Teacher::getTeacherNo, userLoginRequest.getUsername());
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
     */
    @GetMapping("/{id}")
    public ServerResponse queryTeacherById(@PathVariable("id") Integer id) {
        return ServerResponse.ofSuccess(teacherService.getById(id));
    }

    /**
     * 更新讲师
     */
    @PostMapping("/modify")
    public ServerResponse modifyTeacher(@RequestBody Teacher teacher) {
        return teacherService.updateById(teacher) ? ServerResponse.ofSuccess("更新成功") : ServerResponse.ofError("更新失败");
    }

    /**
     * 分页查询讲师
     */
    @GetMapping("/query/{page}")
    public ServerResponse queryTeacher(@PathVariable(value = "page") Integer page,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        Page<Teacher> pages = new Page<>(page, limit);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>().orderByDesc(Teacher::getTeacherNo);
        IPage<Teacher> iPage = teacherService.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 根据姓名关键字搜索讲师
     */
    @GetMapping("/search/{page}/{keyword}")
    public ServerResponse searchTeacher(@PathVariable("keyword") String keyword,
                                        @PathVariable("page") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>().orderByDesc(Teacher::getUpdateTime)
                .likeRight(!StringUtils.isEmpty(keyword), Teacher::getRealname, keyword);
        IPage<Teacher> iPage = teacherService.page(new Page<>(page, limit), wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 管理员根据ID删除讲师
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeacher(@PathVariable Integer id) {
        return teacherService.removeById(id) ? ServerResponse.ofSuccess("删除成功！") : ServerResponse.ofError("删除失败！");
    }

    /**
     * 用于给讲师生成讲师编号,返回一个讲师编号
     */
    @GetMapping("/no")
    public ServerResponse getTeacherNo() {
        List<Teacher> teacherList = teacherService.list(new QueryWrapper<Teacher>().select().orderByDesc("teacher_no"));
        // 返回最大编号的讲师编号再+1给新添加的讲师
        return ServerResponse.ofSuccess(teacherList.get(0).getTeacherNo());
    }

    /**
     * 管理员添加讲师,默认密码是123456
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
        return teacherService.save(teacher) ? ServerResponse.ofSuccess("添加讲师成功！") : ServerResponse.ofError("添加讲师失败！");
    }

    /**
     * 根据ID封禁、解封讲师账号，状态为0时正常，1时封禁
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
     */
    @PostMapping("/password")
    public ServerResponse updatePass(@RequestBody PasswordVO passwordVO) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>().eq(Teacher::getId, passwordVO.getId()).eq(Teacher::getPassword, passwordVO.getOldPass());
        Teacher teacher = teacherService.getOne(wrapper);
        if (teacher == null) {
            return ServerResponse.ofError("旧密码错误");
        }
        // 否则进入修改密码流程
        teacher.setPassword(passwordVO.getNewPass());
        return teacherService.updateById(teacher) ? ServerResponse.ofSuccess("密码修改成功") : ServerResponse.ofError("密码更新失败");
    }

    /**
     * 查询所有讲师
     */
    @GetMapping("/all")
    public ServerResponse getAllTeacher() {
        return ServerResponse.ofSuccess(teacherService.list());
    }

}

