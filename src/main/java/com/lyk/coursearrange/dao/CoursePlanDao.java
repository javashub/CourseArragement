package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.CoursePlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author lequal
 * @since 2020-04-15
 */
public interface CoursePlanDao extends BaseMapper<CoursePlan> {

    // 将上课计划插入上课计划的表中
//    @Insert("insert into tb_course_plan ")
//    void insertCoursePlan(CoursePlan coursePlan);

    // 插入课程计划

    @Insert("insert into tb_course_plan(grade_no, class_no, course_no, teacher_no, classroom_no, class_time, semester) values(#{grade_no}, #{class_no}, #{course_no}, #{teacher_no}, #{classroom_no}, #{class_time}, #{semester})")
    void insertCoursePlan(@Param("grade_no") String grade_no, @Param("class_no") String class_no, @Param("course_no") String course_no,
                          @Param("teacher_no") String teacher_no, @Param("classroom_no") String classroom_no, @Param("class_time") String class_time, @Param("semester") String semester);

    @Update("truncate tb_course_plan")
    void deleteAllPlan();
}
