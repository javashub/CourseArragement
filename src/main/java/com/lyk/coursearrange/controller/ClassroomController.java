package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.TeachbuildInfo;
import com.lyk.coursearrange.entity.request.ClassroomAddRequest;
import com.lyk.coursearrange.service.ClassroomService;
import com.lyk.coursearrange.service.TeachbuildInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 *
 * @author lequal
 * @since 2020-03-23
 */
@RestController
@RequestMapping("/classroom")
public class ClassroomController {


    @Autowired
    private ClassroomService classroomService;
    @Autowired
    private TeachbuildInfoService t;

    // TODO 添加教室，添加时需要查询所有的教学楼，选择教室所在教学楼并判断教室是否已经存在

    /**
     * 带分页显示教室列表
     * @return
     */
    @GetMapping("/{page}")
    public ServerResponse queryClassroom(@PathVariable("page")Integer page,
                                         @RequestParam(defaultValue = "10")Integer limit) {
        Page<Classroom> pages = new Page<>(page, limit);
        QueryWrapper<Classroom> wrapper = new QueryWrapper<Classroom>().orderByAsc("classroom_no");

        IPage<Classroom> ipage = classroomService.page(pages, wrapper);

        return ServerResponse.ofSuccess(ipage);

    }

    /**
     * 添加教室
     * @param car
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addClassroom(@RequestBody ClassroomAddRequest car) {
        System.out.println(car);
        System.out.println("======================");
        Classroom c = new Classroom();
        TeachbuildInfo teachbuildInfo = t.getOne(new QueryWrapper<TeachbuildInfo>().eq("teach_build_no", car.getTeachbuildNo()));
        if (teachbuildInfo == null) {
            return ServerResponse.ofError("教学楼编号不存在，请重新选择");
        }
        c.setCapacity(car.getCapacity());
        c.setClassroomNo(car.getClassroomNo());
        c.setTeachbuildNo(car.getTeachbuildNo());
        c.setClassroomName(car.getClassroomName());
        c.setRemark(car.getRemark());
        boolean b = classroomService.save(c);
        if (b) {
            return ServerResponse.ofSuccess("添加成功");
        }
        return ServerResponse.ofError("添加失败");
    }

    /**
     * 删除教室
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteClassroomById(@PathVariable("id") Integer id) {
        boolean b = classroomService.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return ServerResponse.ofError("删除失败");
    }

    /**
     * 根据id查询教室
     * @param id
     * @return
     */
    @GetMapping("/query/{id}")
    public ServerResponse queryClassroomByID(@PathVariable("id") Integer id) {

        return ServerResponse.ofSuccess(classroomService.getById(id));
    }

    /**
     * 更新教室
     * @param classroom
     * @return
     */
    @PostMapping("/modify")
    public ServerResponse modifyClassroom(@RequestBody Classroom classroom) {

        return classroomService.updateById(classroom) ? ServerResponse.ofSuccess("更新成功") : ServerResponse.ofError("更新失败");
    }

}

