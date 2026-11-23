package com.lyk.coursearrange.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.organization.entity.OrgRegionDivision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 行政区划 Mapper。
 * 说明：两个自定义 SQL 是数据源同步专用——同步必须看到含逻辑删除在内的全部记录，
 * 且要把软停用的记录恢复为正常，这两种操作 MyBatis-Plus 的 @TableLogic 自动过滤
 * 无法覆盖，属于同步流程的必要例外；常规业务查询仍走 BaseMapper 标准方法。
 */
@Mapper
public interface OrgRegionDivisionMapper extends BaseMapper<OrgRegionDivision> {

    /**
     * 查询全部行政区划（含 deleted=1 的软停用记录）。
     * 用途：同步前构建 code -> 记录映射，避免对已停用代码重复插入触发唯一键冲突。
     *
     * @return 全量记录列表
     */
    @Select("SELECT * FROM org_region_division")
    List<OrgRegionDivision> selectAllIncludingDeleted();

    /**
     * 按 region_code 更新区划字段并把记录恢复为正常状态（deleted=0）。
     * 用途：数据源中仍存在的记录做增量更新；该 SQL 不带 deleted 过滤条件，
     * 因此既能更新正常记录，也能把此前软停用的记录重新启用。
     *
     * @param region 待更新的区划记录（region_code 作为定位键）
     * @return 影响行数
     */
    @Update("UPDATE org_region_division SET region_name = #{region.regionName}, "
            + "parent_code = #{region.parentCode}, region_level = #{region.regionLevel}, "
            + "sort_no = #{region.sortNo}, deleted = 0, updated_at = NOW() "
            + "WHERE region_code = #{region.regionCode}")
    int updateByRegionCode(@Param("region") OrgRegionDivision region);
}
