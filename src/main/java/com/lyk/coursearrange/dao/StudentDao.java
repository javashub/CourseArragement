package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.Student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lequal
 * @since 2020-03-13
 */
public interface StudentDao extends BaseMapper<Student> {

    @Select("select count(*) from tb_student where date(create_time) = #{yesday}")
    int studentReg(@Param("yesday") String yesday);

}
