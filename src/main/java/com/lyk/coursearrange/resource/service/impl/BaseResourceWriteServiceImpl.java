package com.lyk.coursearrange.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.resource.entity.ResBuilding;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.request.ResourceClassroomSaveRequest;
import com.lyk.coursearrange.resource.request.ResourceCourseSaveRequest;
import com.lyk.coursearrange.resource.service.BaseResourceReadService;
import com.lyk.coursearrange.resource.service.BaseResourceWriteService;
import com.lyk.coursearrange.resource.service.ResBuildingService;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.vo.ResourceClassroomPageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础资源写服务实现。
 */
@Slf4j
@Service
public class BaseResourceWriteServiceImpl implements BaseResourceWriteService {

    private final ResCourseService courseService;
    private final ResBuildingService buildingService;
    private final ResClassroomService classroomService;
    private final BaseResourceReadService readService;

    public BaseResourceWriteServiceImpl(ResCourseService courseService,
                                        ResBuildingService buildingService,
                                        ResClassroomService classroomService,
                                        BaseResourceReadService readService) {
        this.courseService = courseService;
        this.buildingService = buildingService;
        this.classroomService = classroomService;
        this.readService = readService;
    }

    @Override
    public ResCourse saveCourse(ResourceCourseSaveRequest request) {
        validateCourseCodeUnique(request.getId(), request.getCourseCode());
        ResCourse entity = request.getId() == null ? new ResCourse() : getCourseOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        if (entity.getNeedSpecialRoom() == null) {
            entity.setNeedSpecialRoom(0);
        }
        if (StringUtils.isBlank(entity.getRoomType())) {
            entity.setRoomType("NORMAL");
        }
        courseService.saveOrUpdate(entity);
        log.info("保存资源课程成功，courseCode={}, id={}", entity.getCourseCode(), entity.getId());
        return entity;
    }

    @Override
    public void changeCourseStatus(Long id, Integer status) {
        ResCourse entity = getCourseOrThrow(id);
        entity.setStatus(status);
        courseService.updateById(entity);
        log.info("修改资源课程状态成功，id={}, status={}", id, status);
    }

    @Override
    public void deleteCourse(Long id) {
        getCourseOrThrow(id);
        courseService.removeById(id);
        log.info("删除资源课程成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceClassroomPageVO saveClassroom(ResourceClassroomSaveRequest request) {
        validateClassroomCodeUnique(request.getId(), request.getClassroomCode());
        ResBuilding building = getBuildingByCodeOrThrow(request.getBuildingCode());
        ResClassroom entity = request.getId() == null ? new ResClassroom() : getClassroomOrThrow(request.getId());
        entity.setClassroomCode(request.getClassroomCode());
        entity.setClassroomName(request.getClassroomName());
        entity.setBuildingId(building.getId());
        entity.setCampusId(building.getCampusId());
        entity.setCollegeId(building.getCollegeId());
        entity.setSeatCount(request.getSeatCount());
        entity.setRoomType(request.getRoomType());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        if (entity.getIsShared() == null) {
            entity.setIsShared(1);
        }
        classroomService.saveOrUpdate(entity);
        log.info("保存资源教室成功，classroomCode={}, id={}", entity.getClassroomCode(), entity.getId());
        return readService.getClassroomDetail(entity.getId());
    }

    @Override
    public void deleteClassroom(Long id) {
        getClassroomOrThrow(id);
        classroomService.removeById(id);
        log.info("删除资源教室成功，id={}", id);
    }

    private void validateCourseCodeUnique(Long id, String courseCode) {
        LambdaQueryWrapper<ResCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResCourse::getCourseCode, courseCode)
                .eq(ResCourse::getDeleted, 0)
                .ne(id != null, ResCourse::getId, id);
        if (courseService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "课程编码已存在");
        }
    }

    private void validateClassroomCodeUnique(Long id, String classroomCode) {
        LambdaQueryWrapper<ResClassroom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResClassroom::getClassroomCode, classroomCode)
                .eq(ResClassroom::getDeleted, 0)
                .ne(id != null, ResClassroom::getId, id);
        if (classroomService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "教室编码已存在");
        }
    }

    private ResBuilding getBuildingByCodeOrThrow(String buildingCode) {
        LambdaQueryWrapper<ResBuilding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResBuilding::getBuildingCode, buildingCode)
                .eq(ResBuilding::getDeleted, 0)
                .last("limit 1");
        ResBuilding entity = buildingService.getOne(wrapper, false);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教学楼不存在");
        }
        return entity;
    }

    private ResCourse getCourseOrThrow(Long id) {
        ResCourse entity = courseService.getById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }
        return entity;
    }

    private ResClassroom getClassroomOrThrow(Long id) {
        ResClassroom entity = classroomService.getById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教室不存在");
        }
        return entity;
    }
}
