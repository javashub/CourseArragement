package com.lyk.coursearrange.schedule.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.service.ClassInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminClassOptionControllerTest {

    @Mock
    private ClassInfoService classInfoService;

    @Test
    void page_shouldClampPageSizeTo100() {
        AdminClassOptionController controller = new AdminClassOptionController(classInfoService);
        ServerResponse<?> expected = ServerResponse.ofSuccess("ok");
        when(classInfoService.queryClassInfos(1, 100, "")).thenReturn(expected);

        Object response = controller.page(1, 200, "");

        assertSame(expected, response);
        verify(classInfoService).queryClassInfos(1, 100, "");
    }

    @Test
    void options_shouldReturnAllClasses() {
        AdminClassOptionController controller = new AdminClassOptionController(classInfoService);
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassNo("C1");
        classInfo.setClassName("一班");
        when(classInfoService.listClassOptions("2025")).thenReturn(List.of(classInfo));

        ServerResponse<?> response = (ServerResponse<?>) controller.options("2025");

        assertTrue(response.isSuccess());
        assertEquals(List.of(classInfo), response.getData());
        verify(classInfoService).listClassOptions("2025");
    }
}
