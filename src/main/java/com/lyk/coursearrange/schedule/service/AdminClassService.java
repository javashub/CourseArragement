package com.lyk.coursearrange.schedule.service;

import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.schedule.request.AdminClassSaveRequest;
import com.lyk.coursearrange.schedule.vo.AdminClassVO;

import java.util.Collection;
import java.util.List;

/**
 * 标准班级服务。
 */
public interface AdminClassService {

    PageResponse<AdminClassVO> pageClasses(long pageNum, long pageSize, String gradeNo);

    List<AdminClassVO> listClassOptions(String gradeNo);

    List<AdminClassVO> listClassesByCodes(Collection<String> classNos);

    AdminClassVO getClassById(Long id);

    AdminClassVO getClassByCode(String classNo);

    AdminClassVO saveClass(AdminClassSaveRequest request);

    AdminClassVO updateClass(Long id, AdminClassSaveRequest request);

    void deleteClass(Long id);

    long countActiveClasses();
}
