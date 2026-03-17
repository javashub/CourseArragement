package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author lequal
 * @since 2020-03-06
 */
public interface ClassInfoService extends IService<ClassInfo> {


    /**
     * 查询班级信息带详细信息
     */
    ServerResponse queryClassInfos(Integer page, Integer limit, String gradeNo);

    /**
     * 查询班级选项列表。
     */
    List<ClassInfo> listClassOptions(String gradeNo);
}
