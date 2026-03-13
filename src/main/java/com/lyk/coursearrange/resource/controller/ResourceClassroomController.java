package com.lyk.coursearrange.resource.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.resource.query.ResourceClassroomPageQuery;
import com.lyk.coursearrange.resource.request.ResourceClassroomSaveRequest;
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
 * 教室资源控制器。
 */
@RestController
@RequestMapping("/api/resources/classrooms")
public class ResourceClassroomController {

    private final BaseResourceReadService readService;
    private final BaseResourceWriteService writeService;

    public ResourceClassroomController(BaseResourceReadService readService,
                                       BaseResourceWriteService writeService) {
        this.readService = readService;
        this.writeService = writeService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute ResourceClassroomPageQuery query) {
        return ServerResponse.ofSuccess(readService.pageClassrooms(query));
    }

    @GetMapping("/{id}")
    public ServerResponse<?> detail(@PathVariable("id") Long id) {
        return ServerResponse.ofSuccess(readService.getClassroomDetail(id));
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody ResourceClassroomSaveRequest request) {
        return ServerResponse.ofSuccess(writeService.saveClassroom(request));
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable("id") Long id) {
        writeService.deleteClassroom(id);
        return ServerResponse.ofSuccess("教室删除成功");
    }
}
