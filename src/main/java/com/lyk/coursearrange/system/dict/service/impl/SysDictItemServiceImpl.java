package com.lyk.coursearrange.system.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.dict.entity.SysDictItem;
import com.lyk.coursearrange.system.dict.mapper.SysDictItemMapper;
import com.lyk.coursearrange.system.dict.service.SysDictItemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典项服务实现。
 */
@Service
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    @Override
    public List<SysDictItem> listByDictTypeCode(String dictTypeCode) {
        if (StringUtils.isBlank(dictTypeCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictTypeCode, dictTypeCode)
                .eq(SysDictItem::getStatus, SystemConstants.Status.ENABLED)
                .orderByAsc(SysDictItem::getSortNo)
                .orderByAsc(SysDictItem::getId);
        return list(wrapper);
    }

    @Override
    public Map<String, List<SysDictItem>> mapByDictTypeCodes(List<String> dictTypeCodes) {
        if (dictTypeCodes == null || dictTypeCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysDictItem::getDictTypeCode, dictTypeCodes)
                .eq(SysDictItem::getStatus, SystemConstants.Status.ENABLED)
                .orderByAsc(SysDictItem::getDictTypeCode)
                .orderByAsc(SysDictItem::getSortNo)
                .orderByAsc(SysDictItem::getId);
        return list(wrapper).stream().collect(Collectors.groupingBy(
                SysDictItem::getDictTypeCode,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }
}
