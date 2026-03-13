package com.lyk.coursearrange.resource.service;

import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.request.ResourceClassroomSaveRequest;
import com.lyk.coursearrange.resource.request.ResourceCourseSaveRequest;
import com.lyk.coursearrange.resource.vo.ResourceClassroomPageVO;

/**
 * 基础资源写服务。
 */
public interface BaseResourceWriteService {

    ResCourse saveCourse(ResourceCourseSaveRequest request);

    void changeCourseStatus(Long id, Integer status);

    void deleteCourse(Long id);

    ResourceClassroomPageVO saveClassroom(ResourceClassroomSaveRequest request);

    void deleteClassroom(Long id);
}
