package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.service.ClassTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lequal
 * @since 2020-04-06
 */
@RestController
public class ClassTaskController {


    @Autowired
    private ClassTaskService classTaskService;

    /**
     * 查询开课任务
     */
    @GetMapping("/classtask/{page}/{semester}")
    public ServerResponse queryClassTask(@PathVariable("page") Integer page,
                                         @PathVariable("semester") String semester,
                                         @RequestParam(defaultValue = "10") Integer limit) {
        Page<ClassTask> pages = new Page<>(page, limit);
        QueryWrapper<ClassTask> wrapper = new QueryWrapper<ClassTask>().eq("semester", semester);
        IPage<ClassTask> ipage = classTaskService.page(pages, wrapper);

        if (ipage != null) {
            return ServerResponse.ofSuccess(ipage);
        }
        return ServerResponse.ofError("查询开课任务失败！");
    }


    /**
     * 删除开课任务
     * @param id
     * @return
     */
    @DeleteMapping("/deleteclasstask/{id}")
    public ServerResponse deleteClassTask(@PathVariable("id") Integer id) {

        boolean b = classTaskService.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return ServerResponse.ofError("删除失败");
    }

    /**
     * 获得学期集合,如：
     * 2019-2020-1
     * 2019-2020-2
     * @return
     */
    @GetMapping("/semester")
    public ServerResponse queryAllSemester() {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.select("semester");
        wrapper.groupBy("semester");
        List<ClassTask> list = classTaskService.list(wrapper);
        Set set = new HashSet();

        for (ClassTask c : list) {
            set.add(c.getSemester());
        }

        return ServerResponse.ofSuccess(set);
    }
}

