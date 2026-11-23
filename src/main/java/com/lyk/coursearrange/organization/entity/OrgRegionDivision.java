package com.lyk.coursearrange.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 中国行政区划实体（省 / 市 / 区县三级字典）。
 * 说明：
 * 1. region_code 为业务唯一键（GB/T 2260 风格代码），外部数据源按该键增量 upsert。
 * 2. parent_code 存父级代码字符串而非自增 ID，方便与外部数据集对齐；省级固定为 '0'。
 * 3. deleted 语义沿用项目统一逻辑删除：0 正常；1 表示数据源中已移除（软停用），
 *    保留行以避免校区等已有引用变成悬空编码。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_region_division")
public class OrgRegionDivision extends BaseEntity {

    /**
     * 主键，数据库自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 行政区划代码，业务唯一键。
     */
    private String regionCode;

    /**
     * 行政区划名称，如「广东省」「深圳市」「南山区」。
     */
    private String regionName;

    /**
     * 父级行政区划代码；省级为 '0'。
     */
    private String parentCode;

    /**
     * 层级：1 省 / 2 市 / 3 区县。
     */
    private Integer regionLevel;

    /**
     * 排序号，按数据源出现顺序生成。
     */
    private Integer sortNo;

    /**
     * 状态：1 启用 / 0 禁用。
     */
    private Integer status;
}
