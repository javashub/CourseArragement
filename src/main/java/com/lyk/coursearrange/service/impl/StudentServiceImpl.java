package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.dao.StudentDao;
import com.lyk.coursearrange.service.StudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lequal
 * @since 2020-03-13
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentDao, Student> implements StudentService {

    @Resource
    private StudentDao studentDao;

    private final PasswordService passwordService;

    public StudentServiceImpl(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Override
    public Student studentLogin(String username, String password) {
        // 步骤1：按学号查询学生账号。
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, username);
        Student student = studentDao.selectOne(wrapper);
        if (student == null) {
            return null;
        }
        // 步骤2：优先匹配加密密码，兼容旧明文密码。
        if (passwordService.isEncoded(student.getPassword())) {
            return passwordService.matches(password, student.getPassword()) ? student : null;
        }
        if (!password.equals(student.getPassword())) {
            return null;
        }
        // 步骤3：登录成功后自动升级历史明文密码。
        student.setPassword(passwordService.encode(password));
        studentDao.updateById(student);
        return student;
    }

}
