package com.lyk.coursearrange.system.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 时间片 Mapper。
 */
@Mapper
public interface CfgTimeSlotMapper extends BaseMapper<CfgTimeSlot> {

    /**
     * 物理删除指定时间片。
     */
    @Delete({
            "<script>",
            "DELETE FROM cfg_time_slot WHERE id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int deletePhysicallyByIds(@Param("ids") List<Long> ids);
}
