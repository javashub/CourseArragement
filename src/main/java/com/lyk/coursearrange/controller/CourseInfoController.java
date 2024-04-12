package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CourseInfo;
import com.lyk.coursearrange.service.CourseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lequal
 * @since 2020-04-03
 * description: 教材信息
 */
@RestController
@RequestMapping("/courseinfo")
public class CourseInfoController {

    @Resource
    private CourseInfoService cis;

    /**
     * 分页查询所有的教材信息
     */
    @GetMapping("/{page}")
    public ServerResponse queryCourseInfo(@PathVariable("page") Integer page,
                                          @RequestParam(defaultValue = "10") Integer limit) {

        Page<CourseInfo> pages = new Page<>(page, limit);
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<CourseInfo>().orderByAsc(CourseInfo::getCourseNo);
        IPage<CourseInfo> ipage = cis.page(pages, wrapper);
        return ServerResponse.ofSuccess(ipage);
    }

    /**
     * 添加教材信息
     */
    @PostMapping("/add")
    public ServerResponse addCourseInfo(@RequestBody CourseInfo cinfo) {
        return  cis.saveOrUpdate(cinfo) ? ServerResponse.ofSuccess("添加成功") : ServerResponse.ofError("添加失败");
    }

    /**
     * 根据ID删除教材信息
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteCourseInfoById(@PathVariable("id") Integer id) {
        return cis.removeById(id) ? ServerResponse.ofSuccess("删除成功") : ServerResponse.ofError("删除失败");
    }


    /**
     * 更新教材信息
     */
    @PostMapping("/modify/{id}")
    public ServerResponse modifyCourseInfo(@PathVariable("id") Integer id, @RequestBody CourseInfo courseInfo) {
        QueryWrapper<CourseInfo> wrapper = new QueryWrapper<CourseInfo>().eq("id", id);
        return cis.update(courseInfo, wrapper) ? ServerResponse.ofSuccess("更新成功") : ServerResponse.ofError("更新失败");
    }

    /**
     * 关键字查询教材信息
     */
    @GetMapping("/search/{page}/{keyword}")
    public ServerResponse searchCourseInfo(@PathVariable("page") Integer page,
                                           @RequestParam(defaultValue = "10") Integer limit,
                                           @PathVariable("keyword") String keyword) {
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<CourseInfo>().like(!StringUtils.isEmpty(keyword), CourseInfo::getCourseName, keyword);
        Page<CourseInfo> pages = new Page<>(page, limit);
        IPage<CourseInfo> iPage = cis.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 获取添加课程的课程编号
     */
    @GetMapping("/get-no")
    public ServerResponse getNo() {
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<CourseInfo>().select(CourseInfo::getCourseNo).orderByDesc(CourseInfo::getCourseNo);
        List<CourseInfo> list = cis.list(wrapper);
        String no = String.valueOf(Integer.parseInt(list.get(0).getCourseNo()) + 1);
        return ServerResponse.ofSuccess(no);
    }
}

