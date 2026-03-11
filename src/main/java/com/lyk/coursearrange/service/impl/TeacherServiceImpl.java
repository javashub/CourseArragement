package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.dao.TeacherDao;
import com.lyk.coursearrange.service.TeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    private final PasswordService passwordService;

    public TeacherServiceImpl(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Override
    public Teacher teacherLogin(String username, String password) {
        // 步骤1：按教师编号查询讲师。
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getTeacherNo, username);
        Teacher teacher = teacherDao.selectOne(wrapper);
        if (teacher == null) {
            return null;
        }
        // 步骤2：优先匹配加密密码，兼容旧明文密码。
        if (passwordService.isEncoded(teacher.getPassword())) {
            return passwordService.matches(password, teacher.getPassword()) ? teacher : null;
        }
        if (!password.equals(teacher.getPassword())) {
            return null;
        }
        // 步骤3：历史明文密码在登录成功后自动升级。
        teacher.setPassword(passwordService.encode(password));
        teacherDao.updateById(teacher);
        return teacher;
    }
}
