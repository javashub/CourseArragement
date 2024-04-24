package com.lyk.coursearrange.service;

import com.lyk.coursearrange.entity.Student;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author lequal
 * @since 2020-03-13
 */
public interface StudentService extends IService<Student> {

    Student studentLogin(String username, String password);
}
