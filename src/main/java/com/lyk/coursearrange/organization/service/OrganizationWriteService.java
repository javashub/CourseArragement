package com.lyk.coursearrange.organization.service;

import com.lyk.coursearrange.organization.entity.OrgCampus;
import com.lyk.coursearrange.organization.entity.OrgCollege;
import com.lyk.coursearrange.organization.entity.OrgStage;
import com.lyk.coursearrange.organization.request.OrgCampusSaveRequest;
import com.lyk.coursearrange.organization.request.OrgCollegeSaveRequest;
import com.lyk.coursearrange.organization.request.OrgStageSaveRequest;

/**
 * 组织架构写服务。
 * 场景说明：
 * 1. 校区、学院、学段的保存逻辑都属于单表写操作，但校验规则类似。
 * 2. 这里使用应用服务统一收口，避免控制器各自散落校验和赋值逻辑。
 */
public interface OrganizationWriteService {

    OrgCampus saveCampus(OrgCampusSaveRequest request);

    OrgCollege saveCollege(OrgCollegeSaveRequest request);

    OrgStage saveStage(OrgStageSaveRequest request);

    void deleteCampus(Long id);

    void deleteCollege(Long id);

    void deleteStage(Long id);
}
