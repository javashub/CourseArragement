package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CourseInfo;
import com.lyk.coursearrange.entity.request.CourseInfoAddRequest;
import com.lyk.coursearrange.service.CourseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lequal
 * @since 2020-04-03
 */
@RestController
@RequestMapping("/courseinfo")
public class CourseInfoController {

    @Autowired
    private CourseInfoService cis;

    /**
     * 分页查询所有的教材信息
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/{page}")
    public ServerResponse queryCourseInfo(@PathVariable("page") Integer page,
                                          @RequestParam(defaultValue = "10") Integer limit) {

        Page<CourseInfo> pages = new Page<>(page, limit);
        QueryWrapper<CourseInfo> wrapper = new QueryWrapper<CourseInfo>().orderByDesc("update_time");
        IPage<CourseInfo> ipage = cis.page(pages, wrapper);
        if (ipage != null) {
            return ServerResponse.ofSuccess(ipage);
        }
        return ServerResponse.ofError("查询教材失败");
    }

    /**
     * 添加教材信息
     * @param cinfo
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addCourseInfo(@RequestBody CourseInfoAddRequest cinfo) {
        CourseInfoAddRequest c = new CourseInfoAddRequest();
        return null;
    }

}

