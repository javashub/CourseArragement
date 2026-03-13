package com.lyk.coursearrange.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.common.util.PageUtils;
import com.lyk.coursearrange.resource.entity.ResBuilding;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.mapper.ResClassroomMapper;
import com.lyk.coursearrange.resource.query.ResourceClassroomPageQuery;
import com.lyk.coursearrange.resource.query.ResourceCoursePageQuery;
import com.lyk.coursearrange.resource.service.BaseResourceReadService;
import com.lyk.coursearrange.resource.service.ResBuildingService;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.vo.ResourceBuildingOptionVO;
import com.lyk.coursearrange.resource.vo.ResourceClassroomPageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 基础资源读取服务实现。
 */
@Service
public class BaseResourceReadServiceImpl implements BaseResourceReadService {

    private final ResCourseService courseService;
    private final ResBuildingService buildingService;
    private final ResClassroomMapper classroomMapper;

    public BaseResourceReadServiceImpl(ResCourseService courseService,
                                       ResBuildingService buildingService,
                                       ResClassroomMapper classroomMapper) {
        this.courseService = courseService;
        this.buildingService = buildingService;
        this.classroomMapper = classroomMapper;
    }

    @Override
    public PageResponse<ResCourse> pageCourses(ResourceCoursePageQuery query) {
        Page<ResCourse> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ResCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResCourse::getDeleted, 0)
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w.like(ResCourse::getCourseCode, query.getKeyword())
                        .or()
                        .like(ResCourse::getCourseName, query.getKeyword())
                        .or()
                        .like(ResCourse::getCourseShortName, query.getKeyword()))
                .eq(query.getStatus() != null, ResCourse::getStatus, query.getStatus())
                .orderByDesc(ResCourse::getUpdatedAt);
        return PageUtils.toPageResponse(courseService.page(page, wrapper));
    }

    @Override
    public ResCourse getCourseDetail(Long id) {
        ResCourse entity = courseService.getById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }
        return entity;
    }

    @Override
    public String getNextCourseCode() {
        LambdaQueryWrapper<ResCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResCourse::getDeleted, 0)
                .orderByDesc(ResCourse::getId)
                .last("limit 1");
        ResCourse lastCourse = courseService.getOne(wrapper, false);
        if (lastCourse == null || StringUtils.isBlank(lastCourse.getCourseCode())) {
            return "C10001";
        }
        String code = lastCourse.getCourseCode().replaceAll("[^0-9]", "");
        long nextValue = StringUtils.isBlank(code) ? 10001L : Long.parseLong(code) + 1;
        return "C" + nextValue;
    }

    @Override
    public List<ResourceBuildingOptionVO> listBuildingOptions() {
        LambdaQueryWrapper<ResBuilding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResBuilding::getDeleted, 0)
                .eq(ResBuilding::getStatus, 1)
                .orderByAsc(ResBuilding::getBuildingCode);
        return buildingService.list(wrapper).stream().map(item -> {
            ResourceBuildingOptionVO vo = new ResourceBuildingOptionVO();
            vo.setId(item.getId());
            vo.setBuildingCode(item.getBuildingCode());
            vo.setBuildingName(item.getBuildingName());
            return vo;
        }).toList();
    }

    @Override
    public PageResponse<ResourceClassroomPageVO> pageClassrooms(ResourceClassroomPageQuery query) {
        Page<ResourceClassroomPageVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageUtils.toPageResponse(classroomMapper.selectPageWithBuilding(page, query));
    }

    @Override
    public ResourceClassroomPageVO getClassroomDetail(Long id) {
        ResourceClassroomPageVO detail = classroomMapper.selectDetailById(id);
        if (detail == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教室不存在");
        }
        return detail;
    }
}
