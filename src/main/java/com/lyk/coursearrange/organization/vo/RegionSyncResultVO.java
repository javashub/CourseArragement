package com.lyk.coursearrange.organization.vo;

import lombok.Data;

/**
 * 行政区划同步结果视图对象。
 * 说明：单次「更新行政区划」的执行摘要，向管理员展示本次新增 / 更新 / 停用的条数与耗时。
 */
@Data
public class RegionSyncResultVO {

    /**
     * 本次新增的区划条数（库中原本不存在的代码）。
     */
    private long inserted;

    /**
     * 本次更新的区划条数（含由软停用恢复为正常的记录，不含无变化记录）。
     */
    private long updated;

    /**
     * 本次软停用的区划条数（数据源中已不存在的既有代码，置 deleted=1）。
     */
    private long disabled;

    /**
     * 数据源解析出的区划总条数。
     */
    private long total;

    /**
     * 本次实际使用的数据源地址。
     */
    private String sourceUrl;

    /**
     * 同步完成时间，格式 yyyy-MM-dd HH:mm:ss。
     */
    private String syncedAt;

    /**
     * 本次同步耗时（毫秒）。
     */
    private long durationMs;
}
