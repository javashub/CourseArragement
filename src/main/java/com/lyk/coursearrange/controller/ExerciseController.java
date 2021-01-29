package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Exercise;
import com.lyk.coursearrange.entity.request.ExerciseVO;
import com.lyk.coursearrange.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * @author lequal
 * @since 2020-05-21
 */
@RestController
public class ExerciseController {

    @Autowired
    private ExerciseService es;

    /**
     * 根据类别查询题目
     * @param id
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/exercise/{categoryid}/{page}")
    public ServerResponse queryExercise(@PathVariable("categoryid") Integer id,
                                        @PathVariable("page") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper wrapper = new QueryWrapper();
        Page pages = new Page(page, limit);
        wrapper.orderByDesc("update_time");
        wrapper.eq("category_id", id);
        IPage<Exercise> iPage = es.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 查询所有题目
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/exercise/{page}")
    public ServerResponse queryAllExercise(@PathVariable("page") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.orderByDesc("update_time");
        Page pages = new Page(page, limit);
        IPage<Exercise> iPage = es.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 添加题目
     * @param exerciseVO
     * @return
     */
    @PostMapping("/addexercise")
    public ServerResponse addExercise(@RequestBody() ExerciseVO exerciseVO) {
        System.out.println(exerciseVO);
        Exercise e = new Exercise();
        e.setCategoryId(exerciseVO.getCategoryId());
        e.setClassNo(exerciseVO.getClassNo());
        e.setExerciseTitle(exerciseVO.getExerciseTitle());
        e.setMultiselect(exerciseVO.getMultiselect());
        e.setAnswer(exerciseVO.getAnswer());
        e.setOptionA(exerciseVO.getOptionA());
        e.setOptionB(exerciseVO.getOptionB());
        e.setOptionC(exerciseVO.getOptionC());
        e.setOptionD(exerciseVO.getOptionD());
        e.setOptionE(exerciseVO.getOptionE());
        e.setOptionF(exerciseVO.getOptionF());
        e.setFraction(exerciseVO.getFraction());
        boolean b = es.save(e);
        if (b) {
            return ServerResponse.ofSuccess("添加成功");
        }
        return ServerResponse.ofError("添加失败");
    }

    /**
     * 根据习题类型随机出20道题目
     * @param categoryId
     * @return
     */
    @GetMapping("/exercise/push/{categoryid}")
    public ServerResponse pushTrain(@PathVariable("categoryid") Integer categoryId) {

        QueryWrapper wrapper = new QueryWrapper();
//        wrapper.eq("")
        int min = 1;
        int max = es.list().size();
        // 随机生成
        for (int i = 0; i < 20; i++) {
            int temp = min + (int)(Math.random() * (max + 1 - min));

            List<Exercise> list = es.list();
        }
        return null;
    }


}

