package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CourseInfo;
import com.lyk.coursearrange.entity.request.CourseInfoAddRequest;
import com.lyk.coursearrange.service.CourseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * @author lequal
 * @since 2020-04-03
 * description: 教材信息
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
        QueryWrapper<CourseInfo> wrapper = new QueryWrapper<CourseInfo>().orderByAsc("course_no");
        IPage<CourseInfo> ipage = cis.page(pages, wrapper);
        return ServerResponse.ofSuccess(ipage);
    }

    /**
     * 添加教材信息
     * @param cinfo
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addCourseInfo(@RequestBody CourseInfo cinfo) {
        boolean b = cis.saveOrUpdate(cinfo);
        if (b) {
            return ServerResponse.ofSuccess("添加成功");
        }
        return ServerResponse.ofError("添加失败");
    }

    /**
     * 根据ID删除教材信息
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteCourseInfoById(@PathVariable("id") Integer id) {
        boolean b = cis.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return ServerResponse.ofError("删除失败");
    }


    /**
     * 更新教材信息
     * @param id
     * @param courseInfo
     * @return
     */
    @PostMapping("/modify/{id}")
    public ServerResponse modifyCourseInfo(@PathVariable("id") Integer id, @RequestBody CourseInfo courseInfo) {
        QueryWrapper<CourseInfo> wrapper = new QueryWrapper<CourseInfo>().eq("id", id);
        boolean b = cis.update(courseInfo, wrapper);
        if (b) {
            return ServerResponse.ofSuccess("更新成功");
        }
        return ServerResponse.ofError("更新失败");
    }

    /**
     * 关键字查询教材信息
     * @param page
     * @param limit
     * @param keyword
     * @return
     */
    @GetMapping("/search/{page}/{keyword}")
    public ServerResponse searchCourseInfo(@PathVariable("page") Integer page,
                                           @RequestParam(defaultValue = "10") Integer limit,
                                           @PathVariable("keyword") String keyword) {
        QueryWrapper<CourseInfo> wrapper = new QueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(keyword), "course_name", keyword);
        Page<CourseInfo> pages = new Page<>(page, limit);
        IPage<CourseInfo> iPage = cis.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 获取添加课程的课程编号
     * @return
     */
    @GetMapping("/get-no")
    public ServerResponse getNo() {
        QueryWrapper<CourseInfo> wrapper = new QueryWrapper<CourseInfo>().select("course_no").orderByDesc("course_no");
        List<CourseInfo> list = cis.list(wrapper);
        String no = String.valueOf(Integer.parseInt(list.get(0).getCourseNo()) + 1);
        System.out.println(no + "=========");
        return ServerResponse.ofSuccess(no);
    }
}

