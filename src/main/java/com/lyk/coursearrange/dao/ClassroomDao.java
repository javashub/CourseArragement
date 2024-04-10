package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.Classroom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lequal
 * @since 2020-03-23
 */
public interface ClassroomDao extends BaseMapper<Classroom> {

//    查询某个教学楼下的教室列表
    @Select("select * from tb_classroom where teachbuild_no = #{teachbuildNo}")
    List<Classroom> selectByTeachbuildNo(@Param("teachbuildNo") String teachbuildNo);

//    @Select("select * from tb_classroom where teachbuild_no in #{teachBuildNoList}") // 这里肯定需要优化的
//    List<Classroom> selectByTeachbuildNoList(List<String> teachBuildNoList);
}
