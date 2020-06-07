package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.Teacher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lequal
 * @since 2020-03-13
 */
public interface TeacherDao extends BaseMapper<Teacher> {

    @Select("SELECT * FROM tb_teacher WHERE teacher_no=#{account} AND password=#{password}" +
            "        UNION" +
            "        SELECT * FROM tb_teacher WHERE username=#{account} AND password=#{password}" +
            "        UNION" +
            "        SELECT * FROM tb_teacher WHERE realname=#{account} AND password=#{password}")
    Teacher teacherLogin(@Param("account") String account, @Param("password") String password);

    @Select("select count(*) from tb_teacher where date(create_time) = #{yesday}")
    int teacherReg(@Param("yesday") String yesday);

}
