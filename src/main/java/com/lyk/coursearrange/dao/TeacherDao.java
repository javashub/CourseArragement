package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.Teacher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author lequal
 * @since 2020-03-13
 */
public interface TeacherDao extends BaseMapper<Teacher> {

    @Select("select count(*) from tb_teacher where date(create_time) = #{yesterday}")
    int teacherReg(@Param("yesterday") String yesterday);

}
