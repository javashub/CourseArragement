package com.lyk.coursearrange.service;

import com.lyk.coursearrange.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author lequal
 * @since 2020-03-06
 */
public interface AdminService extends IService<Admin> {

    Admin adminLogin(String username, String password);

}
