package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.LocationInfoDao;
import com.lyk.coursearrange.entity.LocationInfo;
import com.lyk.coursearrange.entity.request.LocationSetVO;
import com.lyk.coursearrange.entity.response.LocationVO;
import com.lyk.coursearrange.service.LocationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-03-20
 */
@RestController
public class LocationInfoController {

    @Autowired
    private LocationInfoService lis;
    @Autowired
    private LocationInfoDao lid;

    /**
     * 新增教学区域
     * @return
     */
    @PostMapping("/setteacharea")
    public ServerResponse setTeachArea(@RequestBody() LocationSetVO l) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("teachbuild_no", l.getTeachBuildNo());
        wrapper.eq("grade_no", l.getGradeNo());
        LocationInfo locationInfo = new LocationInfo();
        if (lis.getOne(wrapper) != null) {
            return ServerResponse.ofError("该教学区域已经设置过了！");
        }
        locationInfo.setTeachbuildNo(l.getTeachBuildNo());
        locationInfo.setGradeNo(l.getGradeNo());
        boolean b = lis.save(locationInfo);
        if (b) {
            return ServerResponse.ofSuccess("设置教学区域成功");
        }
        return ServerResponse.ofError("设置教学区域失败");
    }

    /**
     * 查询所有的教学区域安排信息
     * @return
     */
    @GetMapping("/locations/{page}")
    public ServerResponse queryLocationInfo(@PathVariable("page") Integer page, @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> map = new HashMap<>();
        List<LocationVO> list = lid.locations((page - 1) * limit, limit);
        int total = lid.count();
        map.put("records", list);
        map.put("total", total);
        return ServerResponse.ofSuccess(map);
    }


    /**
     * 根据id删除教学区域信息
     * @param id
     * @return
     */
    @DeleteMapping("/location/delete/{id}")
    public ServerResponse delete(@PathVariable("id") Integer id) {
        boolean b = lis.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return ServerResponse.ofError("删除失败");
    }
}

