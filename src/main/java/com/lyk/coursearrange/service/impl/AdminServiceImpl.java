package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.dao.AdminDao;
import com.lyk.coursearrange.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author lequal
 * @since 2020-03-06
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminDao, Admin> implements AdminService {

    @Autowired
    private AdminDao adminDao;

    @Override
    public Admin adminLogin(String username, String password) {

        return adminDao.adminLogin(username,password);
    }
}
