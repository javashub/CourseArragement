package com.lyk.coursearrange.resource.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 教师 remark 兼容解析工具。
 * 1. 兼容旧的纯文本授课说明。
 * 2. 兼容新的 JSON 结构化约束。
 */
public final class TeacherConstraintRemarkUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private TeacherConstraintRemarkUtils() {
    }

    public static TeacherConstraint parse(String remark) {
        if (remark == null || remark.isBlank()) {
            return new TeacherConstraint("", List.of());
        }
        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(remark, MAP_TYPE);
            String teach = Objects.toString(payload.getOrDefault("teach", ""), "").trim();
            return new TeacherConstraint(teach, normalizeTimeSlots(payload.get("forbiddenTimeSlots")));
        } catch (Exception ignored) {
            return new TeacherConstraint(remark.trim(), List.of());
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> normalizeTimeSlots(Object rawValue) {
        if (!(rawValue instanceof List<?> rawList)) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (Object item : rawList) {
            String slot = Objects.toString(item, "").trim();
            if (slot.matches("\\d{1,2}")) {
                normalized.add(String.format("%02d", Integer.parseInt(slot)));
            }
        }
        return new ArrayList<>(normalized);
    }

    public record TeacherConstraint(String teach, List<String> forbiddenTimeSlots) {
    }
}
