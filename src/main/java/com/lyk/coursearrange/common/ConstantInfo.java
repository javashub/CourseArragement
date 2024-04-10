package com.lyk.coursearrange.common;

/**
 * @author: lequal
 * @Date: 2020/3/20
 * @Descripe: 定义属性,方便切割编码的时候选择,可以替换为接口，接口天然带 static final
 */
public interface ConstantInfo {

    /**
     * 是否固定上课时间 1位
     */
    String IS_FIX = "isFix";

    // 不固定上课时间 1，作为归类 map 的 key
    String UN_FIXED_TIME = "unFixedTime";

    // 固定上课时间 2, 作为归类 map 的 key
    String IS_FIX_TIME = "isFixedTime";

    // 不固定时间的标志
    String UN_FIX_TIME_FLAG = "1";
    // 固定时间的标志
    String FIX_TIME_FLAG = "2";


    // 普通教室 01
    String NORMAL_CLASS_ROOM = "01";

    // 年级编号 2位
    String GRADE_NO = "grade_no";

    // 班级编号 位
    String CLASS_NO = "class_no";

    // 教师编号  位
    String TEACHER_NO = "teacher_no";

    // 课程编号  位
    String COURSE_NO = "course_no";

    // 课程属性 1位
    String COURSE_ATTR = "courseAttr";

    // 教室编号6位
    String CLASSROOM_NO = "classroom_no";

    // 上课时间2位
    String CLASS_TIME = "class_time";

    // 开课学期
    String SEMESTER = "semester";

    // 默认课程的编码
    String DEFAULT_CLASS_TIME = "00";

    // 设置各种类型的课程的适应度(码值)
    // 语数英
    String MAIN_COURSE = "01";

    // 物理化学生物 政治历史地理
    String SECONDARY_COURSE = "02";

    // 实验课
    String EXPERIMENT_COURSE = "03";

    // 体育课
    String PHYSICAL_COURSE = "04";

    // 音乐课
    String MUSIC_COURSE = "05";

    // 舞蹈课
    String DANCE_COURSE = "06";

    // 信息技术
    String TECHNOLOGY_COURSE = "07";

    // 设置遗传代数
    int GENERATION = 50;

}
