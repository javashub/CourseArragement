package com.lyk.coursearrange.schedule.util;

import com.lyk.coursearrange.entity.ClassTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleTaskMetaUtilsTest {

    @Test
    void buildTaskRemark_shouldOnlyContainStandardTaskFields() {
        ClassTask task = new ClassTask();
        task.setId(101);
        task.setSemester("2025-2026-1");
        task.setClassNo("2501");
        task.setCourseNo("10001");
        task.setTeacherNo("T2026001");
        task.setGradeNo("2025");
        task.setCourseName("高等数学");
        task.setCourseAttr("必修");
        task.setTeacherName("张老师");

        String remark = ScheduleTaskMetaUtils.buildTaskRemark(task);

        assertTrue(remark.contains("semester=2025-2026-1"));
        assertTrue(remark.contains("courseName=高等数学"));
        assertFalse(remark.contains("legacyId="));
    }

    @Test
    void resolveWeekdayAndPeriod_shouldSupportFourDigitSlotCode() {
        assertEquals("0107", ScheduleTaskMetaUtils.buildClassTime(1, 7));
        assertEquals(1, ScheduleTaskMetaUtils.resolveWeekdayNo("0107"));
        assertEquals(7, ScheduleTaskMetaUtils.resolvePeriodNo("0107"));
    }
}
