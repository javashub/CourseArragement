package com.lyk.coursearrange.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.system.dict.entity.SysDictItem;

import java.util.List;
import java.util.Map;

/**
 * 字典项服务。
 */
public interface SysDictItemService extends IService<SysDictItem> {

    List<SysDictItem> listByDictTypeCode(String dictTypeCode);

    Map<String, List<SysDictItem>> mapByDictTypeCodes(List<String> dictTypeCodes);
}
