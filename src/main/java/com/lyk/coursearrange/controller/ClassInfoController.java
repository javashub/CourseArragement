package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 班级信息
 * @author lequal
 * @since 2020-03-06
 */
@RestController
public class ClassInfoController {

    @Autowired
    private ClassInfoService classInfoService;

    //根据年级查询所有班级
    @GetMapping("/queryclassbygrade/{grade}")
    public ServerResponse queryClass(@PathVariable("grade") String grade) {
        QueryWrapper<ClassInfo> wrapper = new QueryWrapper<ClassInfo>().eq("remark", grade);
        List<ClassInfo> classInfoList = classInfoService.list(wrapper);
        for(ClassInfo c : classInfoList) {
            System.out.println(c);
        }
        return ServerResponse.ofSuccess(classInfoList);
    }





}

