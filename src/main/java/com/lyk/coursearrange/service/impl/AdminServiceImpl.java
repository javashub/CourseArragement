package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.dao.AdminDao;
import com.lyk.coursearrange.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lequal
 * @since 2020-03-06
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminDao, Admin> implements AdminService {

    @Resource
    private AdminDao adminDao;

    private final PasswordService passwordService;

    public AdminServiceImpl(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Override
    public Admin adminLogin(String username, String password) {
        // 步骤1：先按管理员编号查询账号。
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<Admin>()
                .eq(Admin::getAdminNo, username);
        Admin admin = adminDao.selectOne(wrapper);
        if (admin == null) {
            return null;
        }
        // 步骤2：优先校验 BCrypt 密码，兼容历史明文密码。
        if (passwordService.isEncoded(admin.getPassword())) {
            return passwordService.matches(password, admin.getPassword()) ? admin : null;
        }
        if (!password.equals(admin.getPassword())) {
            return null;
        }
        // 步骤3：历史明文密码登录成功后自动迁移。
        admin.setPassword(passwordService.encode(password));
        adminDao.updateById(admin);
        return admin;
    }
}
