package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.organization.entity.OrgAdminClass;
import com.lyk.coursearrange.organization.entity.OrgGrade;
import com.lyk.coursearrange.organization.service.OrgAdminClassService;
import com.lyk.coursearrange.organization.service.OrgGradeService;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.resource.util.ClassForbiddenTimeSlotUtils;
import com.lyk.coursearrange.schedule.request.AdminClassSaveRequest;
import com.lyk.coursearrange.schedule.service.AdminClassService;
import com.lyk.coursearrange.schedule.vo.AdminClassVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 标准班级服务实现。
 */
@Service
public class AdminClassServiceImpl implements AdminClassService {

    private final OrgAdminClassService orgAdminClassService;
    private final OrgGradeService orgGradeService;
    private final ResTeacherService resTeacherService;

    public AdminClassServiceImpl(OrgAdminClassService orgAdminClassService,
                                 OrgGradeService orgGradeService,
                                 ResTeacherService resTeacherService) {
        this.orgAdminClassService = orgAdminClassService;
        this.orgGradeService = orgGradeService;
        this.resTeacherService = resTeacherService;
    }

    @Override
    public PageResponse<AdminClassVO> pageClasses(long pageNum, long pageSize, String gradeNo) {
        LambdaQueryWrapper<OrgAdminClass> wrapper = new LambdaQueryWrapper<OrgAdminClass>()
                .eq(OrgAdminClass::getDeleted, 0)
                .orderByAsc(OrgAdminClass::getClassCode)
                .orderByDesc(OrgAdminClass::getUpdatedAt);
        Long gradeId = resolveGradeId(gradeNo);
        wrapper.eq(gradeId != null, OrgAdminClass::getGradeId, gradeId);
        Page<OrgAdminClass> page = orgAdminClassService.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResponse.<AdminClassVO>builder()
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .records(toVos(page.getRecords()))
                .build();
    }

    @Override
    public List<AdminClassVO> listClassOptions(String gradeNo) {
        LambdaQueryWrapper<OrgAdminClass> wrapper = new LambdaQueryWrapper<OrgAdminClass>()
                .eq(OrgAdminClass::getDeleted, 0)
                .eq(OrgAdminClass::getStatus, 1)
                .orderByAsc(OrgAdminClass::getClassCode);
        Long gradeId = resolveGradeId(gradeNo);
        wrapper.eq(gradeId != null, OrgAdminClass::getGradeId, gradeId);
        return toVos(orgAdminClassService.list(wrapper));
    }

    @Override
    public List<AdminClassVO> listClassesByCodes(Collection<String> classNos) {
        if (classNos == null || classNos.isEmpty()) {
            return List.of();
        }
        List<String> normalizedClassNos = classNos.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        if (normalizedClassNos.isEmpty()) {
            return List.of();
        }
        return toVos(orgAdminClassService.list(new LambdaQueryWrapper<OrgAdminClass>()
                .eq(OrgAdminClass::getDeleted, 0)
                .in(OrgAdminClass::getClassCode, normalizedClassNos)));
    }

    @Override
    public AdminClassVO getClassById(Long id) {
        OrgAdminClass entity = getEntityById(id);
        return toVos(List.of(entity)).stream().findFirst().orElse(null);
    }

    @Override
    public AdminClassVO getClassByCode(String classNo) {
        if (StringUtils.isBlank(classNo)) {
            return null;
        }
        OrgAdminClass entity = orgAdminClassService.getOne(new LambdaQueryWrapper<OrgAdminClass>()
                .eq(OrgAdminClass::getDeleted, 0)
                .eq(OrgAdminClass::getClassCode, classNo)
                .last("limit 1"), false);
        if (entity == null) {
            return null;
        }
        return toVos(List.of(entity)).stream().findFirst().orElse(null);
    }

    @Override
    public AdminClassVO saveClass(AdminClassSaveRequest request) {
        validateClassCodeUnique(null, request.getClassNo());
        OrgGrade grade = getGradeByCode(request.getGradeNo());
        ResTeacher teacher = getTeacherIfPresent(request.getTeacherId());
        OrgAdminClass entity = new OrgAdminClass();
        applyRequest(entity, request, grade, teacher);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (StringUtils.isBlank(entity.getClassMode())) {
            entity.setClassMode("FIXED");
        }
        orgAdminClassService.save(entity);
        return getClassById(entity.getId());
    }

    @Override
    public AdminClassVO updateClass(Long id, AdminClassSaveRequest request) {
        OrgAdminClass entity = getEntityById(id);
        validateClassCodeUnique(id, request.getClassNo());
        OrgGrade grade = getGradeByCode(request.getGradeNo());
        ResTeacher teacher = getTeacherIfPresent(request.getTeacherId());
        applyRequest(entity, request, grade, teacher);
        orgAdminClassService.updateById(entity);
        return getClassById(id);
    }

    @Override
    public void deleteClass(Long id) {
        getEntityById(id);
        orgAdminClassService.removeById(id);
    }

    @Override
    public long countActiveClasses() {
        return orgAdminClassService.count(new LambdaQueryWrapper<OrgAdminClass>()
                .eq(OrgAdminClass::getDeleted, 0));
    }

    private void applyRequest(OrgAdminClass entity,
                              AdminClassSaveRequest request,
                              OrgGrade grade,
                              ResTeacher teacher) {
        entity.setClassCode(StringUtils.trimToEmpty(request.getClassNo()));
        entity.setClassName(StringUtils.trimToEmpty(request.getClassName()));
        entity.setStudentCount(request.getNum() == null ? 0 : request.getNum());
        entity.setGradeId(grade.getId());
        entity.setStageId(grade.getStageId());
        entity.setCampusId(grade.getCampusId());
        entity.setCollegeId(grade.getCollegeId());
        entity.setMajorId(grade.getMajorId());
        entity.setHeadTeacherId(teacher == null ? null : teacher.getId());
        entity.setForbiddenTimeSlots(ClassForbiddenTimeSlotUtils.format(
                ClassForbiddenTimeSlotUtils.parse(request.getForbiddenTimeSlots())));
    }

    private List<AdminClassVO> toVos(List<OrgAdminClass> classes) {
        if (classes == null || classes.isEmpty()) {
            return List.of();
        }
        Set<Long> gradeIds = classes.stream()
                .map(OrgAdminClass::getGradeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> teacherIds = classes.stream()
                .map(OrgAdminClass::getHeadTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, OrgGrade> gradeMap = gradeIds.isEmpty()
                ? Collections.emptyMap()
                : orgGradeService.listByIds(gradeIds).stream()
                .collect(Collectors.toMap(OrgGrade::getId, Function.identity(), (left, right) -> left));
        Map<Long, ResTeacher> teacherMap = teacherIds.isEmpty()
                ? Collections.emptyMap()
                : resTeacherService.listByIds(teacherIds).stream()
                .collect(Collectors.toMap(ResTeacher::getId, Function.identity(), (left, right) -> left));

        return classes.stream().map(item -> {
            AdminClassVO vo = new AdminClassVO();
            OrgGrade grade = gradeMap.get(item.getGradeId());
            ResTeacher teacher = teacherMap.get(item.getHeadTeacherId());
            vo.setId(item.getId());
            vo.setClassNo(item.getClassCode());
            vo.setClassName(item.getClassName());
            vo.setNum(item.getStudentCount());
            vo.setCampusId(item.getCampusId());
            vo.setCollegeId(item.getCollegeId());
            vo.setStageId(item.getStageId());
            vo.setTeacherId(item.getHeadTeacherId());
            vo.setForbiddenTimeSlots(item.getForbiddenTimeSlots());
            vo.setRemark(item.getRemark());
            vo.setStatus(item.getStatus());
            if (grade != null) {
                vo.setGradeNo(grade.getGradeCode());
                vo.setGradeName(grade.getGradeName());
            }
            if (teacher != null) {
                vo.setTeacherNo(teacher.getTeacherCode());
                vo.setTeacherName(teacher.getTeacherName());
            }
            return vo;
        }).toList();
    }

    private OrgAdminClass getEntityById(Long id) {
        OrgAdminClass entity = orgAdminClassService.getById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }
        return entity;
    }

    private OrgGrade getGradeByCode(String gradeNo) {
        if (StringUtils.isBlank(gradeNo)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "年级编号不能为空");
        }
        OrgGrade grade = orgGradeService.getOne(new LambdaQueryWrapper<OrgGrade>()
                .eq(OrgGrade::getDeleted, 0)
                .eq(OrgGrade::getGradeCode, gradeNo)
                .last("limit 1"), false);
        if (grade == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "年级不存在");
        }
        return grade;
    }

    private Long resolveGradeId(String gradeNo) {
        if (StringUtils.isBlank(gradeNo)) {
            return null;
        }
        OrgGrade grade = orgGradeService.getOne(new LambdaQueryWrapper<OrgGrade>()
                .eq(OrgGrade::getDeleted, 0)
                .eq(OrgGrade::getGradeCode, gradeNo)
                .last("limit 1"), false);
        return grade == null ? null : grade.getId();
    }

    private ResTeacher getTeacherIfPresent(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        ResTeacher teacher = resTeacherService.getById(teacherId);
        if (teacher == null || Integer.valueOf(1).equals(teacher.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班主任不存在");
        }
        return teacher;
    }

    private void validateClassCodeUnique(Long id, String classNo) {
        if (StringUtils.isBlank(classNo)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "班级编号不能为空");
        }
        long count = orgAdminClassService.count(new LambdaQueryWrapper<OrgAdminClass>()
                .eq(OrgAdminClass::getDeleted, 0)
                .eq(OrgAdminClass::getClassCode, classNo)
                .ne(id != null, OrgAdminClass::getId, id));
        if (count > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "班级编号已存在");
        }
    }
}
