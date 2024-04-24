package com.lyk.coursearrange.controller;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import javax.annotation.Resource;
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

    @Resource
    private AdminService adminService;
    @Resource
    private TokenService tokenService;


    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ServerResponse adminLogin(@RequestBody UserLoginRequest adminLoginRequest) {
        Map<String, Object> map = new HashMap<>();
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
     */
    @PostMapping("/modify")
    public ServerResponse modifyAdmin(@RequestBody Admin admin) {
        return adminService.updateById(admin) ? ServerResponse.ofSuccess("更新成功！") : ServerResponse.ofError("更新失败！");
    }

    /**
     * 根据ID查询管理员信息
     */
    @GetMapping("/{id}")
    public ServerResponse queryAdmin(@PathVariable("id") Integer id) {
        return ServerResponse.ofSuccess(adminService.getById(id));
    }


    /**
     * 管理员修改密码
     */
    @PostMapping("/password")
    public ServerResponse updatePass(@RequestBody PasswordVO passwordVO) {
        LambdaQueryWrapper<Admin> wrapper =
                new LambdaQueryWrapper<Admin>().eq(Admin::getId, passwordVO.getId()).eq(Admin::getPassword, passwordVO.getOldPass());
        Admin admin = adminService.getOne(wrapper);
        if (null == admin) {
            return ServerResponse.ofError("旧密码错误");
        }
        // 否则进入修改密码流程
        admin.setPassword(passwordVO.getNewPass());
        return adminService.updateById(admin) ? ServerResponse.ofSuccess("密码修改成功") : ServerResponse.ofError("密码更新失败");
    }


}

