package com.lyk.coursearrange.system.config.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;

import java.util.List;

/**
 * 时间片服务。
 */
public interface CfgTimeSlotService extends IService<CfgTimeSlot> {

    /**
     * 物理删除时间片。
     *
     * <p>cfg_time_slot 存在基于规则+星期+节次的唯一索引，
     * 如果仅做逻辑删除，再插入同键新记录会触发唯一键冲突，
     * 因此配置重生成场景需要真正删除已废弃的时间片记录。</p>
     *
     * @param ids 需要物理删除的主键集合
     */
    void removePhysicallyByIds(List<Long> ids);
}
