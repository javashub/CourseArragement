package com.lyk.coursearrange.schedule.controller;

import com.lyk.coursearrange.service.ClassInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标准班级选项控制器。
 */
@RestController
@RequestMapping("/api/resources/admin-classes")
public class AdminClassOptionController {

    private final ClassInfoService classInfoService;

    public AdminClassOptionController(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @GetMapping("/page")
    public Object page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(defaultValue = "") String gradeNo) {
        return classInfoService.queryClassInfos(pageNum, pageSize, gradeNo);
    }
}
