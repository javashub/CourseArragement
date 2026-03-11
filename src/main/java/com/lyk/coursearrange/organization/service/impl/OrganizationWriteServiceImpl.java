package com.lyk.coursearrange.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.organization.entity.OrgCampus;
import com.lyk.coursearrange.organization.entity.OrgCollege;
import com.lyk.coursearrange.organization.entity.OrgStage;
import com.lyk.coursearrange.organization.request.OrgCampusSaveRequest;
import com.lyk.coursearrange.organization.request.OrgCollegeSaveRequest;
import com.lyk.coursearrange.organization.request.OrgStageSaveRequest;
import com.lyk.coursearrange.organization.service.OrgCampusService;
import com.lyk.coursearrange.organization.service.OrgCollegeService;
import com.lyk.coursearrange.organization.service.OrgStageService;
import com.lyk.coursearrange.organization.service.OrganizationWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 组织架构写服务实现。
 */
@Slf4j
@Service
public class OrganizationWriteServiceImpl implements OrganizationWriteService {

    private final OrgCampusService campusService;
    private final OrgCollegeService collegeService;
    private final OrgStageService stageService;

    public OrganizationWriteServiceImpl(OrgCampusService campusService,
                                        OrgCollegeService collegeService,
                                        OrgStageService stageService) {
        this.campusService = campusService;
        this.collegeService = collegeService;
        this.stageService = stageService;
    }

    @Override
    public OrgCampus saveCampus(OrgCampusSaveRequest request) {
        validateCampusCodeUnique(request.getId(), request.getCampusCode());
        OrgCampus entity = request.getId() == null ? new OrgCampus() : getCampusOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        campusService.saveOrUpdate(entity);
        log.info("保存校区成功，campusCode={}, id={}", entity.getCampusCode(), entity.getId());
        return entity;
    }

    @Override
    public OrgCollege saveCollege(OrgCollegeSaveRequest request) {
        validateCollegeCodeUnique(request.getId(), request.getCollegeCode());
        OrgCollege entity = request.getId() == null ? new OrgCollege() : getCollegeOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        collegeService.saveOrUpdate(entity);
        log.info("保存学院成功，collegeCode={}, id={}", entity.getCollegeCode(), entity.getId());
        return entity;
    }

    @Override
    public OrgStage saveStage(OrgStageSaveRequest request) {
        validateStageCodeUnique(request.getId(), request.getStageCode());
        OrgStage entity = request.getId() == null ? new OrgStage() : getStageOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        stageService.saveOrUpdate(entity);
        log.info("保存学段成功，stageCode={}, id={}", entity.getStageCode(), entity.getId());
        return entity;
    }

    @Override
    public void deleteCampus(Long id) {
        OrgCampus campus = getCampusOrThrow(id);
        campus.setDeleted(1);
        campusService.updateById(campus);
        log.info("逻辑删除校区成功，campusCode={}, id={}", campus.getCampusCode(), campus.getId());
    }

    @Override
    public void deleteCollege(Long id) {
        OrgCollege college = getCollegeOrThrow(id);
        college.setDeleted(1);
        collegeService.updateById(college);
        log.info("逻辑删除学院成功，collegeCode={}, id={}", college.getCollegeCode(), college.getId());
    }

    @Override
    public void deleteStage(Long id) {
        OrgStage stage = getStageOrThrow(id);
        stage.setDeleted(1);
        stageService.updateById(stage);
        log.info("逻辑删除学段成功，stageCode={}, id={}", stage.getStageCode(), stage.getId());
    }

    private void validateCampusCodeUnique(Long id, String campusCode) {
        LambdaQueryWrapper<OrgCampus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCampus::getCampusCode, campusCode)
                .eq(OrgCampus::getDeleted, 0)
                .ne(id != null, OrgCampus::getId, id);
        if (campusService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "校区编码已存在");
        }
    }

    private void validateCollegeCodeUnique(Long id, String collegeCode) {
        LambdaQueryWrapper<OrgCollege> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCollege::getCollegeCode, collegeCode)
                .eq(OrgCollege::getDeleted, 0)
                .ne(id != null, OrgCollege::getId, id);
        if (collegeService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "学院编码已存在");
        }
    }

    private void validateStageCodeUnique(Long id, String stageCode) {
        LambdaQueryWrapper<OrgStage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgStage::getStageCode, stageCode)
                .eq(OrgStage::getDeleted, 0)
                .ne(id != null, OrgStage::getId, id);
        if (stageService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "学段编码已存在");
        }
    }

    private OrgCampus getCampusOrThrow(Long id) {
        OrgCampus campus = campusService.getById(id);
        if (campus == null || campus.getDeleted() != null && campus.getDeleted() == 1) {
            throwNotFound("校区不存在");
        }
        return campus;
    }

    private OrgCollege getCollegeOrThrow(Long id) {
        OrgCollege college = collegeService.getById(id);
        if (college == null || college.getDeleted() != null && college.getDeleted() == 1) {
            throwNotFound("学院不存在");
        }
        return college;
    }

    private OrgStage getStageOrThrow(Long id) {
        OrgStage stage = stageService.getById(id);
        if (stage == null || stage.getDeleted() != null && stage.getDeleted() == 1) {
            throwNotFound("学段不存在");
        }
        return stage;
    }

    private <T> T throwNotFound(String message) {
        throw new BusinessException(ResultCode.NOT_FOUND, message);
    }
}
