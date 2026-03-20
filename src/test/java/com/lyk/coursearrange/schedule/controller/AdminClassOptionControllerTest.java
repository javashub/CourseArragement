package com.lyk.coursearrange.schedule.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.schedule.request.AdminClassSaveRequest;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void create_shouldSaveClassUsingRestfulResource() {
        AdminClassOptionController controller = new AdminClassOptionController(classInfoService);
        AdminClassSaveRequest request = new AdminClassSaveRequest();
        request.setGradeNo("01");
        request.setClassNo("2501");
        request.setClassName("25级1班");
        request.setTeacherId(10);
        request.setNum(45);
        request.setForbiddenTimeSlots("01,06");

        when(classInfoService.save(any(ClassInfo.class))).thenReturn(true);

        ServerResponse<?> response = (ServerResponse<?>) controller.create(request);

        assertTrue(response.isSuccess());
        verify(classInfoService).save(any(ClassInfo.class));
    }

    @Test
    void update_shouldPersistForbiddenTimeSlots() {
        AdminClassOptionController controller = new AdminClassOptionController(classInfoService);
        AdminClassSaveRequest request = new AdminClassSaveRequest();
        request.setGradeNo("02");
        request.setClassNo("2601");
        request.setClassName("26级1班");
        request.setTeacherId(11);
        request.setNum(46);
        request.setForbiddenTimeSlots("02,07");

        ClassInfo classInfo = new ClassInfo();
        classInfo.setId(1);

        when(classInfoService.getById(1)).thenReturn(classInfo);
        when(classInfoService.updateById(any(ClassInfo.class))).thenReturn(true);

        ServerResponse<?> response = (ServerResponse<?>) controller.update(1, request);

        assertTrue(response.isSuccess());
        ArgumentCaptor<ClassInfo> captor = ArgumentCaptor.forClass(ClassInfo.class);
        verify(classInfoService).updateById(captor.capture());
        assertEquals("02,07", captor.getValue().getForbiddenTimeSlots());
        assertEquals(Integer.valueOf(11), captor.getValue().getTeacher());
    }
}
