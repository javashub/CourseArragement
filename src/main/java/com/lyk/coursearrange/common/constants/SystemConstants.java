package com.lyk.coursearrange.common.constants;

/**
 * 系统常量。
 * 说明：
 * 1. 对于纯字符串编码、默认值、固定开关码，使用嵌套 Interface 常量组织。
 * 2. 对于有明确语义和行为约束的枚举型值，仍然建议优先使用 enum。
 * 3. 当前这里先承载配置、状态、角色编码等通用字符串常量。
 */
public interface SystemConstants {

    interface Status {
        Integer DISABLED = 0;
        Integer ENABLED = 1;
    }

    interface RoleCode {
        String ADMIN = "ADMIN";
        String TEACHER = "TEACHER";
        String STUDENT = "STUDENT";
    }

    interface UserType {
        String ADMIN = "ADMIN";
        String TEACHER = "TEACHER";
        String STUDENT = "STUDENT";
    }

    interface SourceType {
        String ADMIN = "ADMIN";
        String TEACHER = "TEACHER";
        String STUDENT = "STUDENT";
    }

    interface FeatureToggleCode {
        String ENABLE_MULTI_CAMPUS = "ENABLE_MULTI_CAMPUS";
        String ENABLE_MULTI_COLLEGE = "ENABLE_MULTI_COLLEGE";
        String ENABLE_MULTI_STAGE = "ENABLE_MULTI_STAGE";
        String ENABLE_SHIFT_CLASS = "ENABLE_SHIFT_CLASS";
        String ALLOW_TEACHER_CROSS_CAMPUS = "ALLOW_TEACHER_CROSS_CAMPUS";
        String ALLOW_TEACHER_CROSS_COLLEGE = "ALLOW_TEACHER_CROSS_COLLEGE";
        String ENABLE_TIMETABLE_DRAG = "ENABLE_TIMETABLE_DRAG";
    }

    interface FeatureToggleName {
        String ENABLE_MULTI_CAMPUS = "是否启用多校区模式";
        String ENABLE_MULTI_COLLEGE = "是否启用多学院模式";
        String ENABLE_MULTI_STAGE = "是否启用多学段模式";
        String ENABLE_SHIFT_CLASS = "是否启用走班制";
        String ALLOW_TEACHER_CROSS_CAMPUS = "是否允许教师跨校区授课";
        String ALLOW_TEACHER_CROSS_COLLEGE = "是否允许教师跨学院授课";
        String ENABLE_TIMETABLE_DRAG = "是否启用课表拖拽调课";
    }

    interface FeatureToggleDefaultValue {
        String TRUE = "true";
        String FALSE = "false";
    }
}
