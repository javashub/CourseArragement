package com.lyk.coursearrange.controller;


import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ExerciseCategory;
import com.lyk.coursearrange.service.ExerciseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lequal
 * @since 2020-05-21
 * 测试题类别
 */
@RestController
@RequestMapping("/exercise")
public class ExerciseCategoryController {

    @Autowired
    private ExerciseCategoryService ecs;

    /**
     * 查询所有题目类别
     * @return
     */
    @GetMapping("/categories")
    public ServerResponse queryCategory() {
        // 查询所有类别
        List<ExerciseCategory> list = ecs.list();
        return ServerResponse.ofSuccess(list);
    }


    /**
     * 添加题目类别
     * @param categoryName
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addCategory(@RequestParam() String categoryName) {
        System.out.println("categoryName = " + categoryName);
        ExerciseCategory e = new ExerciseCategory();
        e.setCategoryName(categoryName);
        boolean b = ecs.save(e);
        if (b) {
            return ServerResponse.ofSuccess("添加类别成功");
        }
        return ServerResponse.ofError("添加类别失败");
    }
}

