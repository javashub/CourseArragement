package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.request.ClassroomAddRequest;
import com.lyk.coursearrange.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lequal
 * @since 2020-03-23
 */
@Controller
@RequestMapping("/classroom")
public class ClassroomController {


    @Autowired
    private ClassroomService classroomService;

    /**
     * 带分页显示教室列表
     * @return
     */
    @GetMapping("/queryclassroom/{page}")
    public ServerResponse queryClassroom(@PathVariable("page")Integer page, @RequestParam(defaultValue = "10")Integer limit) {
        QueryWrapper<Classroom> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        Page<Classroom> pages = new Page<>(page, limit);
        // 调用分页查询
        IPage<Classroom> ipage = classroomService.page(pages, wrapper);
        if (ipage != null) {
            return ServerResponse.ofSuccess(ipage);
        }
        return ServerResponse.ofError("查询不到数据");
    }

    /**
     * 添加教室
     * @param car
     * @return
     */
    @PostMapping("/addclassroom")
    public ServerResponse addClassroom(@RequestBody ClassroomAddRequest car) {
        Classroom classroom = new Classroom();
        classroom.setClassroomNo(car.getClassroomNo());
        classroom.setClassroomName(car.getClassroomName());
        classroom.setTeachbuildNo(car.getTeachbuildingNo());
        classroom.setCapacity(car.getCapacity());
        boolean b = classroomService.save(classroom);
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
    @DeleteMapping("/deleteclassroom/{id}")
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
    @GetMapping("/queryclassroombyid/{id}")
    public ServerResponse queryClassroomByID(@PathVariable("id") Integer id) {

        return ServerResponse.ofSuccess(classroomService.getById(id));
    }

    /**
     * 更新教室信息，要先查询出来再插回去
     * @param classroom
     * @return
     */
    @PostMapping("/modifyclassroom")
    public ServerResponse modifyClassroom(@RequestBody Classroom classroom) {

        return classroomService.save(classroom) ? ServerResponse.ofSuccess("更新成功") : ServerResponse.ofError("更新失败");
    }

}

