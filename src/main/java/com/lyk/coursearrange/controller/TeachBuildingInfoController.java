package com.lyk.coursearrange.controller;


import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.TeachBuildingInfo;
import com.lyk.coursearrange.entity.request.TeachbuildingAddRequest;
import com.lyk.coursearrange.service.TeachBuildingInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试完成2020.03.22
 *
 *
 *
 * @author lequal
 * @since 2020-03-20
 */
@RestController
@RequestMapping("/teachbuildinginfo")
public class TeachBuildingInfoController {

    @Autowired
    private TeachBuildingInfoService teachBuildingInfoService;

    /**
     * 查询所有教室
     * @return
     */
    @GetMapping("/list")
    public ServerResponse queryTeachbuilding() {
        // 查询所有教室
        List<TeachBuildingInfo> list = teachBuildingInfoService.list();
        if (list != null) {
            return ServerResponse.ofSuccess(list);
        }
        return ServerResponse.ofSuccess("查询失败");
    }

    /**
     * 根据id删除教室
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeachbuilding(@PathVariable("id") Integer id) {
        boolean b = teachBuildingInfoService.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除教室成功");
        }
        return ServerResponse.ofError("删除失败");
    }

    /**
     * 添加教室
     * @param t
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addTeachbuilding(@RequestBody TeachbuildingAddRequest t) {
        TeachBuildingInfo t1 = new TeachBuildingInfo();
        t1.setTeachBuildNo(t.getTeachBuildNo());
        t1.setTeachBuildName(t.getTeachBuildName());
        t1.setTeachBuildLocation(t.getTeachBuildLocation());
        boolean b = teachBuildingInfoService.save(t1);
        if (b) {
            return ServerResponse.ofSuccess("添加成功");
        }
        return ServerResponse.ofError("添加失败");
    }

    /**
     * 根据id查询教室信息
     * @param id
     * @return
     */
    @GetMapping("/select/{id}")
    public ServerResponse queryTeachBuildingById(@PathVariable("id") Integer id) {
        // 返回当前需要更新的对象
        return ServerResponse.ofSuccess(teachBuildingInfoService.getById(id));
    }

    @PostMapping("/modify")
    public ServerResponse modifyTeachBuilding(@RequestBody TeachBuildingInfo t) {
        return teachBuildingInfoService.updateById(t) ? ServerResponse.ofSuccess("更新成功") : ServerResponse.ofError("更新失败");
    }


}

