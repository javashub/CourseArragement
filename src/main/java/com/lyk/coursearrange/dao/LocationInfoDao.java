package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.LocationInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.entity.response.LocationVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lequal
 * @since 2020-03-20
 */
public interface LocationInfoDao extends BaseMapper<LocationInfo> {

//    @Select("select ttbi.teach_build_no, ttbi.teach_build_name, tgi.grade_no,tgi.grade_name from tb_teach_build_info ttbi join tb_location_info tli on tli.teachbuild_no = ttbi.teach_build_no join tb_grade_info tgi on tli.grade_no = tgi.grade_no limit ${page}, ${limit}")
//    List<LocationVO> locations(Integer page, Integer limit);

    @Select("select tli.id, ttbi.teach_build_no, ttbi.teach_build_name, tgi.grade_no,tgi.grade_name from tb_teach_build_info ttbi join tb_location_info tli on tli.teachbuild_no = ttbi.teach_build_no join tb_grade_info tgi on tli.grade_no = tgi.grade_no where tli.deleted = 0 limit ${page}, ${limit}")
    List<LocationVO> locations(Integer page, Integer limit);

    @Select("select count(*) from tb_teach_build_info ttbi join tb_location_info tli on tli.teachbuild_no = ttbi.teach_build_no join tb_grade_info tgi on tli.grade_no = tgi.grade_no")
    int count();
}
