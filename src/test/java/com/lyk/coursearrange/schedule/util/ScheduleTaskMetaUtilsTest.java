package com.lyk.coursearrange.schedule.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleTaskMetaUtilsTest {

    @Test
    void resolveWeekdayAndPeriod_shouldSupportFourDigitSlotCode() {
        assertEquals("0107", ScheduleTaskMetaUtils.buildClassTime(1, 7));
        assertEquals(1, ScheduleTaskMetaUtils.resolveWeekdayNo("0107"));
        assertEquals(7, ScheduleTaskMetaUtils.resolvePeriodNo("0107"));
    }
}
