package com.lyk.coursearrange.resource.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.query.ResourceClassroomPageQuery;
import com.lyk.coursearrange.resource.vo.ResourceClassroomPageVO;
import org.apache.ibatis.annotations.Param;

/**
 * 教室资源 Mapper。
 */
public interface ResClassroomMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<ResClassroom> {

    IPage<ResourceClassroomPageVO> selectPageWithBuilding(Page<ResourceClassroomPageVO> page,
                                                          @Param("query") ResourceClassroomPageQuery query);

    ResourceClassroomPageVO selectDetailById(@Param("id") Long id);
}
