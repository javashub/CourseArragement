package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author lequal
 * @since 2020-03-06
 */
public interface ClassInfoService extends IService<ClassInfo> {


    /**
     * 查询班级信息带详细信息
     */
    ServerResponse queryClassInfos(Integer page, Integer limit, String gradeNo);
}
