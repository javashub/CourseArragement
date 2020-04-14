package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.Classroom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lequal
 * @since 2020-03-23
 */
public interface ClassroomDao extends BaseMapper<Classroom> {

//    查询某个教学楼下的教室列表
    @Select("select * from tb_classroom where teachbuild_no = #{teachbuildNo}")
    List<Classroom> selectByTeachbuildNo(@Param("teachbuildNo") String teachbuildNo);

}
