package com.lyk.coursearrange.resource.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.resource.query.ResourceStudentPageQuery;
import com.lyk.coursearrange.resource.request.ResourceStudentSaveRequest;
import com.lyk.coursearrange.resource.service.BaseResourceReadService;
import com.lyk.coursearrange.resource.service.BaseResourceWriteService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生资源控制器。
 */
@RestController
@RequestMapping("/api/resources/students")
public class ResourceStudentController {

    private final BaseResourceReadService readService;
    private final BaseResourceWriteService writeService;

    public ResourceStudentController(BaseResourceReadService readService,
                                     BaseResourceWriteService writeService) {
        this.readService = readService;
        this.writeService = writeService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute ResourceStudentPageQuery query) {
        return ServerResponse.ofSuccess(readService.pageStudents(query));
    }

    @GetMapping("/{id}")
    public ServerResponse<?> detail(@PathVariable("id") Long id) {
        return ServerResponse.ofSuccess(readService.getStudentDetail(id));
    }

    @GetMapping("/next-code")
    public ServerResponse<?> nextCode(@RequestParam(value = "entryYear", required = false) Integer entryYear) {
        return ServerResponse.ofSuccess(readService.getNextStudentCode(entryYear));
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody ResourceStudentSaveRequest request) {
        return ServerResponse.ofSuccess(writeService.saveStudent(request));
    }

    @PostMapping("/{id}/status/{status}")
    public ServerResponse<?> changeStatus(@PathVariable("id") Long id,
                                          @PathVariable("status") Integer status) {
        writeService.changeStudentStatus(id, status);
        return ServerResponse.ofSuccess("学生状态更新成功");
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable("id") Long id) {
        writeService.deleteStudent(id);
        return ServerResponse.ofSuccess("学生删除成功");
    }
}
