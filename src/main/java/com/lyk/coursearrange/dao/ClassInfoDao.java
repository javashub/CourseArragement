package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.ClassInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lequal
 * @since 2020-03-06
 */
public interface ClassInfoDao extends BaseMapper<ClassInfo> {

    // 获得班级的人数
    @Select("select num from tb_class_info where class_no = #{classNo}")
    int selectStuNum(@Param("classNo") String classNo);

}
