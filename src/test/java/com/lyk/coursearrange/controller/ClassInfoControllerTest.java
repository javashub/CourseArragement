package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.service.ClassInfoService;
import com.lyk.coursearrange.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassInfoControllerTest {

    @Mock
    private ClassInfoService classInfoService;
    @Mock
    private StudentService studentService;

    private ClassInfoController controller;

    @BeforeEach
    void setUp() {
        controller = new ClassInfoController();
        ReflectionTestUtils.setField(controller, "classInfoService", classInfoService);
        ReflectionTestUtils.setField(controller, "studentService", studentService);
    }

    @Test
    void queryClass_shouldUseClassOptionsService() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassNo("2501");
        when(classInfoService.listClassOptions("2025")).thenReturn(List.of(classInfo));

        ServerResponse response = controller.queryClass("2025");

        assertTrue(response.isSuccess());
        assertEquals(List.of(classInfo), response.getData());
        verify(classInfoService).listClassOptions("2025");
    }
}
