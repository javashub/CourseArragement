package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.TeachbuildInfo;
import com.lyk.coursearrange.entity.request.ClassroomAddRequest;
import com.lyk.coursearrange.service.ClassroomService;
import com.lyk.coursearrange.service.CoursePlanService;
import com.lyk.coursearrange.service.TeachbuildInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author lequal
 * @since 2020-03-23
 */
@RestController
@RequestMapping("/classroom")
public class ClassroomController {


    @Resource
    private ClassroomService classroomService;
    @Resource
    private TeachbuildInfoService t;
    @Resource
    private CoursePlanService coursePlanService;

    /**
     * @return -> com.lyk.coursearrange.common.ServerResponse
     * @author lyk
     * @description 根据教学楼编号查询空教室
     **/
    @GetMapping("/empty/{teachbuildno}")
    public ServerResponse getEmptyClassroom(@PathVariable("teachbuildno") String teachbuildNo) {
        // 首先查询该教学楼下面的所有教室
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<Classroom>().eq(Classroom::getTeachbuildNo, teachbuildNo);
        // 指定教学楼下的所有教室
        List<Classroom> allClassroom = classroomService.list(wrapper);

        List<CoursePlan> coursePlanList = coursePlanService.list();
        // 得到已经使用了的教室编号
        Set<String> usedClassroom = new HashSet<>();
        for (CoursePlan coursePlan : coursePlanList) {
            // 截取占用的教室所属编号前两位，即教学楼编号
            if (teachbuildNo.equals(coursePlan.getClassroomNo().substring(0, 2))) {
                usedClassroom.add(coursePlan.getClassroomNo());
            }
        }

        LambdaQueryWrapper<Classroom> wrapper1 =
                new LambdaQueryWrapper<Classroom>().in(Classroom::getClassroomNo, usedClassroom)
                        .orderByAsc(Classroom::getClassroomNo);

        List<Classroom> used = classroomService.list(wrapper1);
        // 取差
        Set<Classroom> newList = getSub(allClassroom, used);

        return ServerResponse.ofSuccess(newList);
    }

    /**
     * 集合取差
     */
    private Set<Classroom> getSub(List<Classroom> list1, List<Classroom> list2) {
        Set<Classroom> newList = new HashSet<>();
        for (Classroom classroom : list1) {
            //遍历集合2，判断集合1中是否包含集合2中元素，若包含，则把这个共同元素加入新集合中
            for (Classroom value : list2) {
                if (!classroom.equals(value)) {
                    newList.add(classroom);
                }
            }
        }
        return newList;
    }

    /**
     * 带分页显示教室列表
     */
    @GetMapping("/{page}")
    public ServerResponse queryClassroom(@PathVariable("page") Integer page,
                                         @RequestParam(defaultValue = "10") Integer limit) {
        Page<Classroom> pages = new Page<>(page, limit);
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<Classroom>().orderByAsc(Classroom::getClassroomNo);
        IPage<Classroom> ipage = classroomService.page(pages, wrapper);
        return ServerResponse.ofSuccess(ipage);
    }

    /**
     * 添加教室
     */
    @PostMapping("/add")
    public ServerResponse addClassroom(@RequestBody ClassroomAddRequest car) {
        Classroom c = new Classroom();
        LambdaQueryWrapper<TeachbuildInfo> wrapper = new LambdaQueryWrapper<TeachbuildInfo>().eq(TeachbuildInfo::getTeachBuildNo, car.getTeachbuildNo());
        TeachbuildInfo teachbuildInfo = t.getOne(wrapper);
        if (teachbuildInfo == null) {
            return ServerResponse.ofError("教学楼编号不存在，请重新选择");
        }
        c.setCapacity(car.getCapacity());
        c.setClassroomNo(car.getClassroomNo());
        c.setTeachbuildNo(car.getTeachbuildNo());
        c.setClassroomName(car.getClassroomName());
        c.setRemark(car.getRemark());
        return classroomService.save(c) ? ServerResponse.ofSuccess("添加成功") : ServerResponse.ofError("添加失败");
    }

    /**
     * 删除教室
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteClassroomById(@PathVariable("id") Integer id) {
        return classroomService.removeById(id) ? ServerResponse.ofSuccess("删除成功") : ServerResponse.ofError("删除失败");
    }

    /**
     * 根据id查询教室
     */
    @GetMapping("/query/{id}")
    public ServerResponse queryClassroomByID(@PathVariable("id") Integer id) {
        return ServerResponse.ofSuccess(classroomService.getById(id));
    }

    /**
     * 更新教室
     */
    @PostMapping("/modify")
    public ServerResponse modifyClassroom(@RequestBody Classroom classroom) {
        return classroomService.updateById(classroom) ? ServerResponse.ofSuccess("更新成功") : ServerResponse.ofError("更新失败");
    }

}

