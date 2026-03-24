package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.TeachbuildInfo;
import com.lyk.coursearrange.entity.request.ClassroomAddRequest;
import com.lyk.coursearrange.service.ClassroomService;
import com.lyk.coursearrange.service.CoursePlanService;
import com.lyk.coursearrange.service.TeachbuildInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lequal
 * @since 2020-03-23
 */
@RestController
@Slf4j
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

        List<String> occupiedClassroomNos;
        try {
            occupiedClassroomNos = coursePlanService.listOccupiedClassroomNos(teachbuildNo);
        } catch (Exception exception) {
            log.warn("查询占用教室失败，将按全部教室可用处理，teachbuildNo={}", teachbuildNo, exception);
            occupiedClassroomNos = Collections.emptyList();
        }
        List<Classroom> used = occupiedClassroomNos.isEmpty()
                ? Collections.emptyList()
                : classroomService.list(new LambdaQueryWrapper<Classroom>()
                .in(Classroom::getClassroomNo, occupiedClassroomNos)
                .orderByAsc(Classroom::getClassroomNo));
        Set<Classroom> newList = getSub(allClassroom, used);

        return ServerResponse.ofSuccess(newList);
    }

    /**
     * 集合取差
     */
    private Set<Classroom> getSub(List<Classroom> list1, List<Classroom> list2) {
        Set<Classroom> usedClassrooms = new HashSet<>(list2);
        Set<Classroom> emptyClassrooms = new HashSet<>();
        for (Classroom classroom : list1) {
            if (!usedClassrooms.contains(classroom)) {
                emptyClassrooms.add(classroom);
            }
        }
        return emptyClassrooms;
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
            throw new BusinessException(ResultCode.NOT_FOUND, "教学楼编号不存在，请重新选择");
        }
        c.setCapacity(car.getCapacity());
        c.setClassroomNo(car.getClassroomNo());
        c.setTeachbuildNo(car.getTeachbuildNo());
        c.setClassroomName(car.getClassroomName());
        c.setRemark(car.getRemark());
        return classroomService.save(c) ? ServerResponse.ofSuccess("添加成功")
                : throwBusiness(ResultCode.SYSTEM_ERROR, "添加失败");
    }

    /**
     * 删除教室
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteClassroomById(@PathVariable("id") Integer id) {
        requireClassroomExists(id);
        return classroomService.removeById(id) ? ServerResponse.ofSuccess("删除成功")
                : throwBusiness(ResultCode.SYSTEM_ERROR, "删除失败");
    }

    /**
     * 根据id查询教室
     */
    @GetMapping("/query/{id}")
    public ServerResponse queryClassroomByID(@PathVariable("id") Integer id) {
        Classroom classroom = classroomService.getById(id);
        if (classroom == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教室不存在");
        }
        return ServerResponse.ofSuccess(classroom);
    }

    /**
     * 更新教室
     */
    @PostMapping("/modify")
    public ServerResponse modifyClassroom(@RequestBody Classroom classroom) {
        requireClassroomExists(classroom.getId());
        return classroomService.updateById(classroom) ? ServerResponse.ofSuccess("更新成功")
                : throwBusiness(ResultCode.SYSTEM_ERROR, "更新失败");
    }

    private void requireClassroomExists(Integer id) {
        if (id == null || classroomService.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教室不存在");
        }
    }

    private ServerResponse throwBusiness(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode, message);
    }

}
