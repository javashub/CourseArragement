package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.LocationInfoDao;
import com.lyk.coursearrange.entity.LocationInfo;
import com.lyk.coursearrange.entity.TeachbuildInfo;
import com.lyk.coursearrange.entity.request.LocationSetVO;
import com.lyk.coursearrange.entity.response.LocationVO;
import com.lyk.coursearrange.service.LocationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-03-20
 */
@RestController
public class LocationInfoController {

    @Resource
    private LocationInfoService lis;
    @Resource
    private LocationInfoDao lid;

    /**
     * 新增教学区域
     */
    @PostMapping("/setteacharea")
    public ServerResponse setTeachArea(@RequestBody() LocationSetVO l) {
        LambdaQueryWrapper<LocationInfo> wrapper =
                new LambdaQueryWrapper<LocationInfo>().eq(LocationInfo::getTeachbuildNo, l.getTeachBuildNo())
                        .eq(LocationInfo::getGradeNo, l.getGradeNo());

        if (lis.getOne(wrapper) != null) {
            return ServerResponse.ofError("该教学区域已经设置过了！");
        }
        LocationInfo locationInfo = new LocationInfo();
        locationInfo.setTeachbuildNo(l.getTeachBuildNo());
        locationInfo.setGradeNo(l.getGradeNo());
        return  lis.save(locationInfo) ? ServerResponse.ofSuccess("设置教学区域成功") : ServerResponse.ofError("设置教学区域失败");
    }

    /**
     * 查询所有的教学区域安排信息
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
     */
    @DeleteMapping("/location/delete/{id}")
    public ServerResponse delete(@PathVariable("id") Integer id) {
        return  lis.removeById(id) ? ServerResponse.ofSuccess("删除成功") : ServerResponse.ofError("删除失败");
    }
}

