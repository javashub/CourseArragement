package com.lyk.coursearrange.schedule.util;

import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * 排课任务元数据工具类。
 */
public interface ScheduleTaskMetaUtils {

    /**
     * 解析 remark 中的 key=value 元数据。
     *
     * @param remark 任务备注
     * @return 元数据映射
     */
    static Map<String, String> parseTaskRemark(String remark) {
        Map<String, String> result = new HashMap<>();
        if (remark == null || remark.isBlank()) {
            return result;
        }
        String[] parts = remark.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }

    /**
     * 构造标准任务编码。
     */
    static String buildTaskCode(String semester, String classNo, String courseNo, String teacherNo) {
        String raw = buildTaskKey(classNo, courseNo, teacherNo) + "_" + safe(semester);
        String sanitized = raw.replaceAll("[^A-Za-z0-9_]", "_");
        return sanitized.length() > 32 ? sanitized.substring(0, 32) : sanitized;
    }

    /**
     * 基于旧任务构造 remark 元数据。
     */
    static String buildTaskRemark(ClassTask legacyTask) {
        return "semester=" + safe(legacyTask.getSemester())
                + ",classNo=" + safe(legacyTask.getClassNo())
                + ",courseNo=" + safe(legacyTask.getCourseNo())
                + ",teacherNo=" + safe(legacyTask.getTeacherNo())
                + ",gradeNo=" + safe(legacyTask.getGradeNo())
                + ",courseName=" + safe(legacyTask.getCourseName())
                + ",courseAttr=" + safe(legacyTask.getCourseAttr())
                + ",teacherName=" + safe(legacyTask.getRealname());
    }

    /**
     * 基于请求参数构造标准任务编码。
     */
    static String buildTaskCode(ClassTaskDTO request) {
        return buildTaskCode(request.getSemester(), request.getClassNo(), request.getCourseNo(), request.getTeacherNo());
    }

    /**
     * 解析固定时间的星期编号。
     */
    static Integer resolveWeekdayNo(String classTime) {
        Integer slot = parseClassTime(classTime);
        if (slot == null || slot < 1) {
            return null;
        }
        return ((slot - 1) / 5) + 1;
    }

    /**
     * 解析固定时间的节次编号。
     */
    static Integer resolvePeriodNo(String classTime) {
        Integer slot = parseClassTime(classTime);
        if (slot == null || slot < 1) {
            return null;
        }
        return ((slot - 1) % 5) + 1;
    }

    static String buildTaskKey(String classNo, String courseNo, String teacherNo) {
        return safe(classNo) + "_" + safe(courseNo) + "_" + safe(teacherNo);
    }

    static String safe(String value) {
        return value == null ? "" : value;
    }

    static Integer parseClassTime(String classTime) {
        try {
            return classTime == null ? null : Integer.parseInt(classTime);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
