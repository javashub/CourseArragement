package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.TeachbuildInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lequal
 * @since 2020-03-20
 */
public interface TeachBuildInfoDao extends BaseMapper<TeachbuildInfo> {

    @Select("select teachbuild_no from tb_location_info where grade_no = #{gradeNo}")
    String selectBuildNo(@Param("gradeNo") String gradeNo);

    @Select("select teachbuild_no from tb_location_info where grade_no = #{gradeNo}")
    List<String> selectTeachBuildList(@Param("gradeNo") String gradeNo);
}
