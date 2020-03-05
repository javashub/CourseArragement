package com.lyk.coursearrange;

import com.lyk.coursearrange.dao.StudentDao;
import com.lyk.coursearrange.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
class CoursearrangeApplicationTests {

    @Autowired
    private StudentDao studentDao;


    @Test
    void contextLoads() {
    }

    @Test
    public void testStudent() {
        List<Student> list = studentDao.selectList(null);
        for(Student stu : list) {
            System.out.println(stu);
        }
    }

}
