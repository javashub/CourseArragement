package com.lyk.coursearrange.resource.service;

import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.query.ResourceClassroomPageQuery;
import com.lyk.coursearrange.resource.query.ResourceCoursePageQuery;
import com.lyk.coursearrange.resource.vo.ResourceBuildingOptionVO;
import com.lyk.coursearrange.resource.vo.ResourceClassroomPageVO;

import java.util.List;

/**
 * 基础资源读取服务。
 */
public interface BaseResourceReadService {

    PageResponse<ResCourse> pageCourses(ResourceCoursePageQuery query);

    ResCourse getCourseDetail(Long id);

    String getNextCourseCode();

    List<ResourceBuildingOptionVO> listBuildingOptions();

    PageResponse<ResourceClassroomPageVO> pageClassrooms(ResourceClassroomPageQuery query);

    ResourceClassroomPageVO getClassroomDetail(Long id);
}
