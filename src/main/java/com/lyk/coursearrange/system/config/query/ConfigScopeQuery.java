package com.lyk.coursearrange.system.config.query;

import lombok.Data;

/**
 * 配置作用域查询对象。
 * 步骤说明：
 * 1. 用于统一接收校区、学院、学段、学期作用域。
 * 2. 后续配置中心、排课配置、功能开关等接口都可复用。
 */
@Data
public class ConfigScopeQuery {

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long termId;
}
