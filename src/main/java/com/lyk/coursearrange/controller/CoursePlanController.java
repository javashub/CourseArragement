package com.lyk.coursearrange.controller;


import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.service.CoursePlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 根据教师查询课程表
     */
    @GetMapping("/courseplan/teacher/{teacherno}")
    public ServerResponse queryCoursePlanByTeacherNo(@PathVariable("teacherno") String teacherNo) {
        return coursePlanService.queryCoursePlanByTeacherNo(teacherNo);
    }

    /**
     * 调课接口。
     */
    @PostMapping("/courseplan/adjust")
    public ServerResponse adjustCoursePlan(@RequestBody CoursePlanAdjustRequest request) {
        return coursePlanService.adjustCoursePlan(request);
    }

    /**
     * 查询最近调课日志。
     */
    @GetMapping("/courseplan/adjust/logs")
    public ServerResponse listAdjustLogs(@RequestParam(required = false) String semester,
                                         @RequestParam(required = false) String classNo,
                                         @RequestParam(required = false) String teacherNo,
                                         @RequestParam(defaultValue = "10") Integer limit) {
        List<CoursePlanAdjustLog> logs = coursePlanService.listRecentAdjustLogs(semester, classNo, teacherNo, limit);
        return ServerResponse.ofSuccess(logs);
    }


}
