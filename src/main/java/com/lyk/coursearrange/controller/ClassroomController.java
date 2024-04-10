package com.lyk.coursearrange.controller;


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

import java.util.*;

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
    @Autowired
    private CoursePlanService coursePlanService;

    // 根据教学楼编号查询空教室
    @GetMapping("/empty/{teachbuildno}")
    public ServerResponse getEmptyClassroom(@PathVariable("teachbuildno") String teachbuildNo) {
        // 首先查询该教学楼下面的所有教室
        QueryWrapper<Classroom> wrapper1 = new QueryWrapper();
        wrapper1.eq("teachbuild_no", teachbuildNo);
        // 指定教学楼下的所有教室
        List<Classroom> allClassroom = classroomService.list(wrapper1);

        List<CoursePlan> coursePlanList = coursePlanService.list();
        // 得到已经使用了的教室编号
        Set<String> usedClaassroom = new HashSet<>();
        for (int i = 0; i < coursePlanList.size(); i++) {
            // 截取占用的教室所属编号前两位，即教学楼编号
            if (teachbuildNo.equals(coursePlanList.get(i).getClassroomNo().substring(0, 2))) {
                usedClaassroom.add(coursePlanList.get(i).getClassroomNo());
            }
        }

        QueryWrapper<Classroom> wrapper2 = new QueryWrapper();
        wrapper2.in("classroom_no", usedClaassroom);
        wrapper2.orderByAsc("classroom_no");

        List<Classroom> used = classroomService.list(wrapper2);
        // 取差
        Set<Classroom> newList = getSub(allClassroom, used);

        return ServerResponse.ofSuccess(newList);
    }

    /**
     * 集合取差
     * @param list1
     * @param list2
     * @return
     */
    private Set<Classroom> getSub(List<Classroom> list1, List<Classroom> list2) {
        Set<Classroom> newList = new HashSet<>();
        for (int i = 0; i <list1.size(); i++) {
            //遍历集合2，判断集合1中是否包含集合2中元素，若包含，则把这个共同元素加入新集合中
            for (int j = 0; j <list2.size(); j++) {
                if (!(list1.get(i).equals(list2.get(j)) || list1.get(i) == list2.get(j))) {
                    newList.add(list1.get(i));
                }
            }
        }
        return newList;
    }

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

