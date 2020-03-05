package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.StudentDao;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.service.StudentService;
import org.springframework.stereotype.Service;

/**
 * @author: 15760
 * @Date: 2020/3/5
 * @Descripe:
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentDao, Student> implements StudentService {

}
