package com.lyk.coursearrange.resource.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.resource.query.ResourceCoursePageQuery;
import com.lyk.coursearrange.resource.request.ResourceCourseSaveRequest;
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
 * 课程资源控制器。
 */
@RestController
@RequestMapping("/api/resources/courses")
public class ResourceCourseController {

    private final BaseResourceReadService readService;
    private final BaseResourceWriteService writeService;

    public ResourceCourseController(BaseResourceReadService readService,
                                    BaseResourceWriteService writeService) {
        this.readService = readService;
        this.writeService = writeService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute ResourceCoursePageQuery query) {
        return ServerResponse.ofSuccess(readService.pageCourses(query));
    }

    @GetMapping("/{id}")
    public ServerResponse<?> detail(@PathVariable("id") Long id) {
        return ServerResponse.ofSuccess(readService.getCourseDetail(id));
    }

    @GetMapping("/next-code")
    public ServerResponse<?> nextCode() {
        return ServerResponse.ofSuccess(readService.getNextCourseCode());
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody ResourceCourseSaveRequest request) {
        return ServerResponse.ofSuccess(writeService.saveCourse(request));
    }

    @PostMapping("/{id}/status/{status}")
    public ServerResponse<?> changeStatus(@PathVariable("id") Long id,
                                          @PathVariable("status") Integer status) {
        writeService.changeCourseStatus(id, status);
        return ServerResponse.ofSuccess("课程状态更新成功");
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable("id") Long id) {
        writeService.deleteCourse(id);
        return ServerResponse.ofSuccess("课程删除成功");
    }
}
