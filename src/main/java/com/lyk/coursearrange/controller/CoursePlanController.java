package com.lyk.coursearrange.controller;


import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.service.CoursePlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lequal
 * @since 2020-04-15
 */
@RestController
public class CoursePlanController {

    @Resource
    private CoursePlanService coursePlanService;


    /**
     * 根据班级查询课程表
     */
    @GetMapping("/courseplan/{classno}")
    public ServerResponse queryCoursePlanByClassNo(@PathVariable("classno") String classNo) {
        return coursePlanService.queryCoursePlanByClassNo(classNo);
    }


}

