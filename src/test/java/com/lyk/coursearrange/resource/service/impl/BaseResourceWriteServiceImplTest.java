package com.lyk.coursearrange.resource.service.impl;

import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.request.ResourceTeacherSaveRequest;
import com.lyk.coursearrange.resource.service.BaseResourceReadService;
import com.lyk.coursearrange.resource.service.ResourceAccountSyncService;
import com.lyk.coursearrange.resource.service.ResBuildingService;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.service.ResStudentService;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseResourceWriteServiceImplTest {

    @Mock
    private ResCourseService courseService;
    @Mock
    private ResTeacherService teacherService;
    @Mock
    private ResStudentService studentService;
    @Mock
    private ResBuildingService buildingService;
    @Mock
    private ResClassroomService classroomService;
    @Mock
    private ResourceAccountSyncService resourceAccountSyncService;
    @Mock
    private BaseResourceReadService readService;

    @Test
    void saveTeacher_shouldPersistDedicatedForbiddenTimeSlotsField() {
        BaseResourceWriteServiceImpl service = new BaseResourceWriteServiceImpl(
                courseService,
                teacherService,
                studentService,
                buildingService,
                classroomService,
                resourceAccountSyncService,
                readService
        );
        when(teacherService.count(any())).thenReturn(0L);

        ResourceTeacherSaveRequest request = new ResourceTeacherSaveRequest();
        request.setTeacherCode("T0001");
        request.setTeacherName("张老师");
        request.setTitleName("讲师");
        request.setRemark("主授高等数学");
        request.setForbiddenTimeSlots("01,06,11");
        request.setStatus(1);

        service.saveTeacher(request);

        ArgumentCaptor<ResTeacher> captor = ArgumentCaptor.forClass(ResTeacher.class);
        verify(teacherService).saveOrUpdate(captor.capture());
        assertEquals("主授高等数学", captor.getValue().getRemark());
        assertEquals("01,06,11", captor.getValue().getForbiddenTimeSlots());
    }
}
