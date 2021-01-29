package com.lyk.coursearrange.service;

import com.lyk.coursearrange.entity.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lequal
 * @since 2020-03-13
 */
public interface TeacherService extends IService<Teacher> {

    Teacher teacherLogin(String username, String password);

}
