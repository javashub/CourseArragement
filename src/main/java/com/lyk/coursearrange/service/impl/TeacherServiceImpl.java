package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.dao.TeacherDao;
import com.lyk.coursearrange.service.TeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lequal
 * @since 2020-03-13
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherDao, Teacher> implements TeacherService {

    @Resource
    private TeacherDao teacherDao;

    @Override
    public Teacher teacherLogin(String username, String password) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getTeacherNo, username)
                .eq(Teacher::getPassword, password);
        return teacherDao.selectOne(wrapper);
    }
}
