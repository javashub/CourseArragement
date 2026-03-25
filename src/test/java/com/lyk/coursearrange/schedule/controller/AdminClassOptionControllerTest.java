package com.lyk.coursearrange.schedule.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.schedule.request.AdminClassSaveRequest;
import com.lyk.coursearrange.schedule.service.AdminClassService;
import com.lyk.coursearrange.schedule.vo.AdminClassVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminClassOptionControllerTest {

    @Mock
    private AdminClassService adminClassService;

    @Test
    void page_shouldClampPageSizeTo100() {
        AdminClassOptionController controller = new AdminClassOptionController(adminClassService);
        when(adminClassService.pageClasses(1, 100, ""))
                .thenReturn(PageResponse.<AdminClassVO>builder().pageNum(1L).pageSize(100L).total(0L).records(List.of()).build());

        ServerResponse<?> response = (ServerResponse<?>) controller.page(1, 200, "");

        assertTrue(response.isSuccess());
        verify(adminClassService).pageClasses(1, 100, "");
    }

    @Test
    void options_shouldReturnAllClasses() {
        AdminClassOptionController controller = new AdminClassOptionController(adminClassService);
        AdminClassVO classInfo = new AdminClassVO();
        classInfo.setClassNo("C1");
        classInfo.setClassName("一班");
        when(adminClassService.listClassOptions("2025")).thenReturn(List.of(classInfo));

        ServerResponse<?> response = (ServerResponse<?>) controller.options("2025");

        assertTrue(response.isSuccess());
        assertEquals(List.of(classInfo), response.getData());
        verify(adminClassService).listClassOptions("2025");
    }

    @Test
    void create_shouldSaveClassUsingRestfulResource() {
        AdminClassOptionController controller = new AdminClassOptionController(adminClassService);
        AdminClassSaveRequest request = new AdminClassSaveRequest();
        request.setGradeNo("01");
        request.setClassNo("2501");
        request.setClassName("25级1班");
        request.setTeacherId(10L);
        request.setNum(45);
        request.setForbiddenTimeSlots("01,06");

        when(adminClassService.saveClass(any(AdminClassSaveRequest.class))).thenReturn(new AdminClassVO());

        ServerResponse<?> response = (ServerResponse<?>) controller.create(request);

        assertTrue(response.isSuccess());
        verify(adminClassService).saveClass(request);
    }

    @Test
    void update_shouldDelegateToStandardClassService() {
        AdminClassOptionController controller = new AdminClassOptionController(adminClassService);
        AdminClassSaveRequest request = new AdminClassSaveRequest();
        request.setGradeNo("02");
        request.setClassNo("2601");
        request.setClassName("26级1班");
        request.setTeacherId(11L);
        request.setNum(46);
        request.setForbiddenTimeSlots("02,07");

        when(adminClassService.updateClass(1L, request)).thenReturn(new AdminClassVO());

        ServerResponse<?> response = (ServerResponse<?>) controller.update(1L, request);

        assertTrue(response.isSuccess());
        verify(adminClassService).updateClass(1L, request);
    }
}
