package com.lyk.coursearrange.controller;



import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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

    @GetMapping("/add")
    public String addAdmin() {
        Admin admin = new Admin();
        admin.setId(1);
        admin.setAdminNo("10011");
        admin.setUsername("admin");
        admin.setPassword("123");
        admin.setRealname("梁主任");
        admin.setJobtitle("教务处主任");
        admin.setDescription("认真对待工作");
        admin.setRemark("务实");
        admin.setStatus(0);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        adminService.save(admin);
        return "添加管理员成功！";
    }
}

