package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.service.ClassroomService;
import com.lyk.coursearrange.service.CoursePlanService;
import com.lyk.coursearrange.service.TeachbuildInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassroomControllerTest {

    @Mock
    private ClassroomService classroomService;
    @Mock
    private TeachbuildInfoService teachbuildInfoService;
    @Mock
    private CoursePlanService coursePlanService;

    private ClassroomController controller;

    @BeforeEach
    void setUp() {
        controller = new ClassroomController();
        ReflectionTestUtils.setField(controller, "classroomService", classroomService);
        ReflectionTestUtils.setField(controller, "t", teachbuildInfoService);
        ReflectionTestUtils.setField(controller, "coursePlanService", coursePlanService);
    }

    @Test
    void getEmptyClassroom_shouldExcludeOccupiedClassroomsFromService() {
        Classroom room101 = buildClassroom(1, "01-101", "01");
        Classroom room102 = buildClassroom(2, "01-102", "01");

        when(classroomService.list(any(Wrapper.class))).thenReturn(List.of(room101, room102), List.of(room101));
        when(coursePlanService.listOccupiedClassroomNos("01")).thenReturn(List.of("01-101"));

        ServerResponse<?> response = controller.getEmptyClassroom("01");

        assertTrue(response.isSuccess());
        Set<?> emptyRooms = (Set<?>) response.getData();
        assertEquals(1, emptyRooms.size());
        assertTrue(emptyRooms.contains(room102));
    }

    private Classroom buildClassroom(int id, String classroomNo, String teachbuildNo) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setClassroomNo(classroomNo);
        classroom.setClassroomName(classroomNo);
        classroom.setTeachbuildNo(teachbuildNo);
        classroom.setDeleted(0);
        return classroom;
    }
}
