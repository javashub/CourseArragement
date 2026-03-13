package com.lyk.coursearrange.schedule.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.service.CoursePlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标准课表控制器。
 */
@RestController
@RequestMapping("/api/schedule/plans")
public class SchedulePlanController {

    private final CoursePlanService coursePlanService;

    public SchedulePlanController(CoursePlanService coursePlanService) {
        this.coursePlanService = coursePlanService;
    }

    @GetMapping("/by-class")
    public ServerResponse<?> byClass(@RequestParam String classNo) {
        return coursePlanService.queryCoursePlanByClassNo(classNo);
    }

    @GetMapping("/by-teacher")
    public ServerResponse<?> byTeacher(@RequestParam String teacherNo) {
        return coursePlanService.queryCoursePlanByTeacherNo(teacherNo);
    }

    @PostMapping("/adjust")
    public ServerResponse<?> adjust(@RequestBody CoursePlanAdjustRequest request) {
        return coursePlanService.adjustCoursePlan(request);
    }

    @GetMapping("/adjust-logs")
    public ServerResponse<?> adjustLogs(@RequestParam(required = false) String semester,
                                        @RequestParam(required = false) String classNo,
                                        @RequestParam(required = false) String teacherNo,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        return ServerResponse.ofSuccess(coursePlanService.listRecentAdjustLogs(semester, classNo, teacherNo, limit));
    }
}
