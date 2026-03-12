package com.lyk.coursearrange.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.auth.service.AuthAccountSyncService;
import com.lyk.coursearrange.auth.service.AuthLoginService;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.PasswordVO;
import com.lyk.coursearrange.entity.request.TeacherAddRequest;
import com.lyk.coursearrange.entity.request.UserLoginRequest;
import com.lyk.coursearrange.service.TeacherService;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.util.AliyunUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private PasswordService passwordService;
    @Autowired
    private AuthLoginService authLoginService;
    @Autowired
    private AuthAccountSyncService authAccountSyncService;


    /**
     * 上传讲师证件
     * @param id
     * @param file
     * @return
     */
    @PostMapping("/upload/{id}")
    public ServerResponse uploadLicense(@PathVariable("id") Integer id, MultipartFile file) {
        Map<String, Object> map = AliyunUtil.upload(file, "license");
        String license = (String) map.get("url");
        Teacher t = teacherService.getById(id);
        if (t == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "讲师不存在");
        }
        t.setLicense(license);
        boolean b = teacherService.updateById(t);
        if (b) {
            return ServerResponse.ofSuccess("上传证件成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "上传证件失败");
    }



    /**
     * 讲师登录
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse teacherLogin(@RequestBody UserLoginRequest userLoginRequest) {
        Map<String, Object> map = new HashMap<>();
        QueryWrapper<Teacher> wrapper = new QueryWrapper<Teacher>().eq("teacher_no", userLoginRequest.getUsername());
        Teacher teacherProfile = teacherService.getOne(wrapper);
        if (teacherProfile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账号不存在");
        } else if (teacherProfile.getStatus() != 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号状态异常，请联系管理员");
        }
        SysUser sysUser = authLoginService.loginTeacher(userLoginRequest.getUsername(), userLoginRequest.getPassword());
        if (sysUser != null) {
            StpUtil.login("sys_user:" + sysUser.getId());
            String token = StpUtil.getTokenValue();
            Teacher teacher = teacherService.getById(sysUser.getSourceId().intValue());
            map.put("teacher", teacher);
            map.put("token", token);
            return ServerResponse.ofSuccess(map);
        }
        // 否则一律视为密码错误
        throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码错误");
    }

    /**
     * 根据id查询讲师，用于更新操作
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ServerResponse queryTeacherById(@PathVariable("id") Integer id) {
        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "讲师不存在");
        }
        return ServerResponse.ofSuccess(teacher);
    }

    /**
     * 更新讲师
     * @param teacher
     * @return
     */
    @PostMapping("/modify")
    public ServerResponse modifyTeacher(@RequestBody Teacher teacher) {
        requireTeacherExists(teacher.getId());
        boolean b = teacherService.updateById(teacher);

        if (b) {
            return ServerResponse.ofSuccess("更新成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新失败");
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
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 管理员根据ID删除讲师
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeacher(@PathVariable Integer id) {
        requireTeacherExists(id);
        boolean b = teacherService.removeById(id);
        if(b) {
            return ServerResponse.ofSuccess("删除成功！");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除失败！");
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
        // 默认密码统一进行 BCrypt 加密，避免新账号继续写入明文密码。
        teacher.setPassword(passwordService.encode("123456"));
        teacher.setRealname(t.getRealname());
        teacher.setJobtitle(t.getJobtitle());
        teacher.setTeach(t.getTeach());
        teacher.setTelephone(t.getTelephone());
        teacher.setAddress(t.getAddress());
        teacher.setAge(t.getAge());
        boolean b = teacherService.save(teacher);
        if (b) {
            authAccountSyncService.syncTeacherAccount(teacher);
            return ServerResponse.ofSuccess("添加讲师成功！");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加讲师失败！");
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
        if (teacher == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "讲师不存在");
        }
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
        Teacher teacher = teacherService.getById(passwordVO.getId());
        if (teacher == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "讲师不存在");
        }
        boolean passwordMatched = passwordService.isEncoded(teacher.getPassword())
                ? passwordService.matches(passwordVO.getOldPass(), teacher.getPassword())
                : passwordVO.getOldPass().equals(teacher.getPassword());
        if (!passwordMatched) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "旧密码错误");
        }
        // 新密码统一使用 BCrypt 存储。
        teacher.setPassword(passwordService.encode(passwordVO.getNewPass()));
        boolean b = teacherService.updateById(teacher);
        if (b) {
            authAccountSyncService.updatePasswordBySource(SystemConstants.SourceType.TEACHER, teacher.getId().longValue(), teacher.getPassword());
            return ServerResponse.ofSuccess("密码修改成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "密码更新失败");
    }

    /**
     * 查询所有讲师
     * @return
     */
    @GetMapping("/all")
    public ServerResponse getAllTeacher() {

        return ServerResponse.ofSuccess(teacherService.list());
    }

    private void requireTeacherExists(Integer id) {
        if (id == null || teacherService.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "讲师不存在");
        }
    }

}
