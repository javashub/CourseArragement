package com.lyk.coursearrange.resource.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 教师禁排时间编码工具。
 */
public final class TeacherForbiddenTimeSlotUtils {

    private TeacherForbiddenTimeSlotUtils() {
    }

    public static List<String> parse(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        String[] segments = rawValue.split("[,，\\s]+");
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String segment : segments) {
            String item = segment == null ? "" : segment.trim();
            if (item.matches("\\d{1,2}")) {
                normalized.add(String.format("%02d", Integer.parseInt(item)));
            }
        }
        return new ArrayList<>(normalized);
    }

    public static String format(List<String> values) {
        return String.join(",", parse(String.join(",", values == null ? List.of() : values)));
    }
}
