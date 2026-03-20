package com.lyk.coursearrange.resource.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeacherForbiddenTimeSlotUtilsTest {

    @Test
    void parse_shouldNormalizeAndDeduplicateTimeSlots() {
        List<String> values = TeacherForbiddenTimeSlotUtils.parse("1, 06，06 11");

        assertEquals(List.of("01", "06", "11"), values);
    }

    @Test
    void format_shouldReturnCommaSeparatedNormalizedTimeSlots() {
        String value = TeacherForbiddenTimeSlotUtils.format(List.of("1", "06", "06", "11"));

        assertEquals("01,06,11", value);
    }
}
