package com.lyk.coursearrange.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.common.util.PageUtils;
import com.lyk.coursearrange.resource.entity.ResBuilding;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.entity.ResStudent;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.mapper.ResClassroomMapper;
import com.lyk.coursearrange.resource.query.ResourceClassroomPageQuery;
import com.lyk.coursearrange.resource.query.ResourceCoursePageQuery;
import com.lyk.coursearrange.resource.query.ResourceStudentPageQuery;
import com.lyk.coursearrange.resource.query.ResourceTeacherPageQuery;
import com.lyk.coursearrange.resource.service.BaseResourceReadService;
import com.lyk.coursearrange.resource.service.ResBuildingService;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.service.ResStudentService;
import com.lyk.coursearrange.resource.service.ResTeacherService;
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
    private final ResTeacherService teacherService;
    private final ResStudentService studentService;
    private final ResBuildingService buildingService;
    private final ResClassroomMapper classroomMapper;

    public BaseResourceReadServiceImpl(ResCourseService courseService,
                                       ResTeacherService teacherService,
                                       ResStudentService studentService,
                                       ResBuildingService buildingService,
                                       ResClassroomMapper classroomMapper) {
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.studentService = studentService;
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
    public PageResponse<ResTeacher> pageTeachers(ResourceTeacherPageQuery query) {
        Page<ResTeacher> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ResTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResTeacher::getDeleted, 0)
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w.like(ResTeacher::getTeacherCode, query.getKeyword())
                        .or()
                        .like(ResTeacher::getTeacherName, query.getKeyword())
                        .or()
                        .like(ResTeacher::getTitleName, query.getKeyword())
                        .or()
                        .like(ResTeacher::getMobile, query.getKeyword()))
                .eq(query.getStatus() != null, ResTeacher::getStatus, query.getStatus())
                .orderByDesc(ResTeacher::getUpdatedAt);
        return PageUtils.toPageResponse(teacherService.page(page, wrapper));
    }

    @Override
    public ResTeacher getTeacherDetail(Long id) {
        ResTeacher entity = teacherService.getById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教师不存在");
        }
        return entity;
    }

    @Override
    public String getNextTeacherCode() {
        LambdaQueryWrapper<ResTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResTeacher::getDeleted, 0)
                .orderByDesc(ResTeacher::getId)
                .last("limit 1");
        ResTeacher lastTeacher = teacherService.getOne(wrapper, false);
        if (lastTeacher == null || StringUtils.isBlank(lastTeacher.getTeacherCode())) {
            return "T2026001";
        }
        String code = lastTeacher.getTeacherCode().replaceAll("[^0-9]", "");
        long nextValue = StringUtils.isBlank(code) ? 2026001L : Long.parseLong(code) + 1;
        return "T" + nextValue;
    }

    @Override
    public PageResponse<ResStudent> pageStudents(ResourceStudentPageQuery query) {
        Page<ResStudent> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ResStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResStudent::getDeleted, 0)
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w.like(ResStudent::getStudentCode, query.getKeyword())
                        .or()
                        .like(ResStudent::getStudentName, query.getKeyword())
                        .or()
                        .like(ResStudent::getMobile, query.getKeyword()))
                .eq(query.getStatus() != null, ResStudent::getStatus, query.getStatus())
                .orderByDesc(ResStudent::getUpdatedAt);
        return PageUtils.toPageResponse(studentService.page(page, wrapper));
    }

    @Override
    public ResStudent getStudentDetail(Long id) {
        ResStudent entity = studentService.getById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学生不存在");
        }
        return entity;
    }

    @Override
    public String getNextStudentCode(Integer entryYear) {
        int year = entryYear == null || entryYear <= 0 ? java.time.LocalDate.now().getYear() : entryYear;
        LambdaQueryWrapper<ResStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResStudent::getDeleted, 0)
                .likeRight(ResStudent::getStudentCode, String.valueOf(year))
                .orderByDesc(ResStudent::getStudentCode)
                .last("limit 1");
        ResStudent lastStudent = studentService.getOne(wrapper, false);
        if (lastStudent == null || StringUtils.isBlank(lastStudent.getStudentCode())) {
            return year + "0001";
        }
        long nextValue = Long.parseLong(lastStudent.getStudentCode()) + 1;
        return String.valueOf(nextValue);
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
