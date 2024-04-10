package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.dao.StudentDao;
import com.lyk.coursearrange.service.StudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lequal
 * @since 2020-03-13
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentDao, Student> implements StudentService {

    @Autowired
    private StudentDao studentDao;

    @Override
    public Student studentLogin(String username, String password) {
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        wrapper.eq("student_no", username).eq("password", password);
        // 查询数据库是否有该学生
        return studentDao.selectOne(wrapper);
    }

}
