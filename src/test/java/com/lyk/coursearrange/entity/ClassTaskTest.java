package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ClassTaskTest {

    @Test
    void classTask_shouldBePlainSchedulingInputObject() {
        assertFalse(ClassTask.class.isAnnotationPresent(TableName.class));
        assertFalse(Model.class.isAssignableFrom(ClassTask.class));
    }
}
