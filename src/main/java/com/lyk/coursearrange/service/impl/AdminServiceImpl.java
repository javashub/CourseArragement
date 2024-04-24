package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.dao.AdminDao;
import com.lyk.coursearrange.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public Admin adminLogin(String username, String password) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<Admin>()
                .eq(Admin::getAdminNo, username)
                .eq(Admin::getPassword, password);
        return adminDao.selectOne(wrapper);
    }
}
