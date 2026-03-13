package com.lyk.coursearrange.resource.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.resource.query.ResourceTeacherPageQuery;
import com.lyk.coursearrange.resource.request.ResourceTeacherSaveRequest;
import com.lyk.coursearrange.resource.service.BaseResourceReadService;
import com.lyk.coursearrange.resource.service.BaseResourceWriteService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师资源控制器。
 */
@RestController
@RequestMapping("/api/resources/teachers")
public class ResourceTeacherController {

    private final BaseResourceReadService readService;
    private final BaseResourceWriteService writeService;

    public ResourceTeacherController(BaseResourceReadService readService,
                                     BaseResourceWriteService writeService) {
        this.readService = readService;
        this.writeService = writeService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute ResourceTeacherPageQuery query) {
        return ServerResponse.ofSuccess(readService.pageTeachers(query));
    }

    @GetMapping("/{id}")
    public ServerResponse<?> detail(@PathVariable("id") Long id) {
        return ServerResponse.ofSuccess(readService.getTeacherDetail(id));
    }

    @GetMapping("/next-code")
    public ServerResponse<?> nextCode() {
        return ServerResponse.ofSuccess(readService.getNextTeacherCode());
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody ResourceTeacherSaveRequest request) {
        return ServerResponse.ofSuccess(writeService.saveTeacher(request));
    }

    @PostMapping("/{id}/status/{status}")
    public ServerResponse<?> changeStatus(@PathVariable("id") Long id,
                                          @PathVariable("status") Integer status) {
        writeService.changeTeacherStatus(id, status);
        return ServerResponse.ofSuccess("教师状态更新成功");
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable("id") Long id) {
        writeService.deleteTeacher(id);
        return ServerResponse.ofSuccess("教师删除成功");
    }
}
