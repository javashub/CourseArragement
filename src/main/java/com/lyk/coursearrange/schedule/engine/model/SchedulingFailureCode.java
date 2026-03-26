package com.lyk.coursearrange.schedule.engine.model;

/**
 * 排课失败原因编码。
 */
public enum SchedulingFailureCode {

    NO_AVAILABLE_TIME_SLOT("没有可用时间片"),
    TEACHER_CONFLICT("教师时间冲突"),
    CLASS_CONFLICT("班级时间冲突"),
    CLASSROOM_CONFLICT("教室时间冲突"),
    TEACHER_FORBIDDEN_TIME("教师禁排时间"),
    CLASS_FORBIDDEN_TIME("班级禁排时间"),
    TEACHER_DAY_HOUR_LIMIT("教师日课时上限"),
    SPECIAL_ROOM_UNAVAILABLE("特殊教室不可用"),
    CONTINUOUS_SLOT_UNAVAILABLE("连堂连续时间片不可用"),
    FIXED_TIME_UNSATISFIED("固定时间约束无法满足");

    private final String defaultMessage;

    SchedulingFailureCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
