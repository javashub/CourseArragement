package com.lyk.coursearrange.resource.service;

import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.entity.ResStudent;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.query.ResourceClassroomPageQuery;
import com.lyk.coursearrange.resource.query.ResourceCoursePageQuery;
import com.lyk.coursearrange.resource.query.ResourceStudentPageQuery;
import com.lyk.coursearrange.resource.query.ResourceTeacherPageQuery;
import com.lyk.coursearrange.resource.vo.ResourceBuildingOptionVO;
import com.lyk.coursearrange.resource.vo.ResourceClassroomPageVO;

import java.util.List;

/**
 * 基础资源读取服务。
 */
public interface BaseResourceReadService {

    PageResponse<ResCourse> pageCourses(ResourceCoursePageQuery query);

    PageResponse<ResTeacher> pageTeachers(ResourceTeacherPageQuery query);

    ResTeacher getTeacherDetail(Long id);

    String getNextTeacherCode();

    PageResponse<ResStudent> pageStudents(ResourceStudentPageQuery query);

    ResStudent getStudentDetail(Long id);

    String getNextStudentCode(Integer entryYear);

    ResCourse getCourseDetail(Long id);

    String getNextCourseCode();

    List<ResourceBuildingOptionVO> listBuildingOptions();

    PageResponse<ResourceClassroomPageVO> pageClassrooms(ResourceClassroomPageQuery query);

    ResourceClassroomPageVO getClassroomDetail(Long id);
}
