package com.lyk.coursearrange.system.dict.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.dict.entity.SysDictType;
import com.lyk.coursearrange.system.dict.service.SysDictItemService;
import com.lyk.coursearrange.system.dict.service.SysDictTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典控制器。
 * 步骤说明：
 * 1. 提供字典类型列表接口。
 * 2. 提供单个字典类型项列表接口。
 * 3. 提供多个字典类型批量映射接口，便于前端一次性获取多个下拉数据源。
 */
@RestController
@RequestMapping("/api/dict")
public class SysDictController {

    private final SysDictTypeService dictTypeService;
    private final SysDictItemService dictItemService;

    public SysDictController(SysDictTypeService dictTypeService, SysDictItemService dictItemService) {
        this.dictTypeService = dictTypeService;
        this.dictItemService = dictItemService;
    }

    @GetMapping("/types")
    public ServerResponse<?> listTypes() {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDeleted, 0)
                .eq(SysDictType::getStatus, SystemConstants.Status.ENABLED)
                .orderByAsc(SysDictType::getSortNo)
                .orderByAsc(SysDictType::getId);
        return ServerResponse.ofSuccess(dictTypeService.list(wrapper));
    }

    @GetMapping("/items/{dictTypeCode}")
    public ServerResponse<?> listItems(@PathVariable("dictTypeCode") String dictTypeCode) {
        return ServerResponse.ofSuccess(dictItemService.listByDictTypeCode(dictTypeCode));
    }

    @GetMapping("/map")
    public ServerResponse<?> map(@RequestParam("codes") String codes) {
        List<String> dictTypeCodes = Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return ServerResponse.ofSuccess(dictItemService.mapByDictTypeCodes(dictTypeCodes));
    }
}
