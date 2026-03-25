package com.lyk.coursearrange.schedule.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.schedule.request.AdminClassSaveRequest;
import com.lyk.coursearrange.schedule.service.AdminClassService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标准班级选项控制器。
 */
@RestController
@RequestMapping("/api/resources/admin-classes")
public class AdminClassOptionController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminClassService adminClassService;

    public AdminClassOptionController(AdminClassService adminClassService) {
        this.adminClassService = adminClassService;
    }

    @GetMapping("/page")
    public Object page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(defaultValue = "") String gradeNo) {
        return ServerResponse.ofSuccess(adminClassService.pageClasses(pageNum, Math.min(pageSize, MAX_PAGE_SIZE), gradeNo));
    }

    @GetMapping("/options")
    public Object options(@RequestParam(defaultValue = "") String gradeNo) {
        return ServerResponse.ofSuccess(adminClassService.listClassOptions(gradeNo));
    }

    @PostMapping
    public Object create(@RequestBody AdminClassSaveRequest request) {
        if (adminClassService.saveClass(request) != null) {
            return ServerResponse.ofSuccess("添加班级成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加班级失败");
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable Long id, @RequestBody AdminClassSaveRequest request) {
        if (adminClassService.updateClass(id, request) != null) {
            return ServerResponse.ofSuccess("修改班级成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "修改班级失败");
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        adminClassService.deleteClass(id);
        return ServerResponse.ofSuccess("删除班级成功");
    }
}
