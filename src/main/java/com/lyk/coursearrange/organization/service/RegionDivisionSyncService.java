package com.lyk.coursearrange.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.organization.entity.OrgRegionDivision;
import com.lyk.coursearrange.organization.vo.RegionSyncResultVO;

/**
 * 行政区划同步服务。
 * 说明：
 * 1. 继承 IService 以复用 MyBatis-Plus 的分页与列表查询能力（/page、/children 接口）。
 * 2. syncFromSource 从配置的数据源拉取省市区三级数据并按 region_code 增量入库，
 *    同一时刻只允许一个同步任务执行。
 */
public interface RegionDivisionSyncService extends IService<OrgRegionDivision> {

    /**
     * 从配置的数据源拉取行政区划数据并增量写入字典。
     *
     * @return 本次同步的结果摘要（新增 / 更新 / 停用条数与耗时）
     * @throws com.lyk.coursearrange.common.exception.BusinessException 数据源不可达、
     *         返回内容无法解析、解析结果为空或已有同步任务在执行时抛出
     */
    RegionSyncResultVO syncFromSource();
}
