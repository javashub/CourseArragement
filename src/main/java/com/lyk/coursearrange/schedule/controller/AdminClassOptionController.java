package com.lyk.coursearrange.schedule.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.resource.util.ClassForbiddenTimeSlotUtils;
import com.lyk.coursearrange.schedule.request.AdminClassSaveRequest;
import com.lyk.coursearrange.service.ClassInfoService;
import org.springframework.beans.BeanUtils;
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

    private final ClassInfoService classInfoService;

    public AdminClassOptionController(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @GetMapping("/page")
    public Object page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(defaultValue = "") String gradeNo) {
        return classInfoService.queryClassInfos(pageNum, Math.min(pageSize, MAX_PAGE_SIZE), gradeNo);
    }

    @GetMapping("/options")
    public Object options(@RequestParam(defaultValue = "") String gradeNo) {
        return ServerResponse.ofSuccess(classInfoService.listClassOptions(gradeNo));
    }

    @PostMapping
    public Object create(@RequestBody AdminClassSaveRequest request) {
        ClassInfo classInfo = new ClassInfo();
        applyRequest(request, classInfo);
        if (classInfoService.save(classInfo)) {
            return ServerResponse.ofSuccess("添加班级成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加班级失败");
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable Integer id, @RequestBody AdminClassSaveRequest request) {
        ClassInfo classInfo = classInfoService.getById(id);
        if (classInfo == null || classInfo.getDeleted() != null && classInfo.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }
        applyRequest(request, classInfo);
        classInfo.setId(id);
        if (classInfoService.updateById(classInfo)) {
            return ServerResponse.ofSuccess("修改班级成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "修改班级失败");
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Integer id) {
        ClassInfo classInfo = classInfoService.getById(id);
        if (classInfo == null || classInfo.getDeleted() != null && classInfo.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }
        if (classInfoService.removeById(id)) {
            return ServerResponse.ofSuccess("删除班级成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除班级失败");
    }

    private void applyRequest(AdminClassSaveRequest request, ClassInfo classInfo) {
        BeanUtils.copyProperties(request, classInfo, "teacherId", "gradeNo", "forbiddenTimeSlots");
        classInfo.setTeacher(request.getTeacherId());
        classInfo.setRemark(request.getGradeNo());
        classInfo.setForbiddenTimeSlots(ClassForbiddenTimeSlotUtils.format(
                ClassForbiddenTimeSlotUtils.parse(request.getForbiddenTimeSlots())));
    }
}
