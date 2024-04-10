package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.dao.ClassTaskDao;
import com.lyk.coursearrange.entity.ClassTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;

/**
 * @author: 15760
 * @Date: 2020/4/11
 * @Descripe:
 */

@SpringBootTest
public class CourseTest {

    @Autowired
    private ClassTaskDao classTaskDao;

    @Test
    public void test() {
        QueryWrapper<ClassTask> wrapper = new QueryWrapper<ClassTask>().eq("semester", "2019-2020-1");
        List<ClassTask> classTaskList = classTaskDao.selectList(wrapper);
        // 测试输出
        for (ClassTask c : classTaskList) {
            System.out.println(c);
        }
    }

    @Test
    public void test2() {

        List<String> list = classTaskDao.selectClassNo();
        // 测试输出
        for (String c : list) {
            System.out.println(c);
        }
    }


}
