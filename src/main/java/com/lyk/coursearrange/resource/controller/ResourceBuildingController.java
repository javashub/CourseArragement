package com.lyk.coursearrange.resource.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.resource.service.BaseResourceReadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教学楼资源控制器。
 */
@RestController
@RequestMapping("/api/resources/buildings")
public class ResourceBuildingController {

    private final BaseResourceReadService readService;

    public ResourceBuildingController(BaseResourceReadService readService) {
        this.readService = readService;
    }

    @GetMapping("/options")
    public ServerResponse<?> listOptions() {
        return ServerResponse.ofSuccess(readService.listBuildingOptions());
    }
}
