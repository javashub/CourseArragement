package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.TeachbuildInfo;
import com.lyk.coursearrange.entity.request.TeachbuildAddRequest;
import com.lyk.coursearrange.service.TeachbuildInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lequal
 * @since 2020-03-20
 */
@RestController
@RequestMapping("/teachbuildinfo")
public class TeachBuildInfoController {

    @Resource
    private TeachbuildInfoService teachBuildInfoService;

    /**
     * 分页查询所有教学楼
     */
    @GetMapping("/list/{page}")
    public ServerResponse queryTeachbuilding(@PathVariable("page") Integer page,
                                             @RequestParam(defaultValue = "10") Integer limit) {
        Page<TeachbuildInfo> pages = new Page<>(page, limit);
        LambdaQueryWrapper<TeachbuildInfo> wrapper = new LambdaQueryWrapper<TeachbuildInfo>().orderByDesc(TeachbuildInfo::getUpdateTime);
        IPage<TeachbuildInfo> ipage = teachBuildInfoService.page(pages, wrapper);
        return ServerResponse.ofSuccess(ipage);
    }

    /**
     * 查询所有教学楼
     */
    @GetMapping("/list")
    public ServerResponse queryallTeachbuilding() {
        List<TeachbuildInfo> list = teachBuildInfoService.list();
        return ServerResponse.ofSuccess(list);
    }

    /**
     * 根据id删除教学楼
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeachbuilding(@PathVariable("id") Integer id) {
        requireTeachbuildExists(id);
        return teachBuildInfoService.removeById(id) ? ServerResponse.ofSuccess("删除成功")
                : throwBusiness(ResultCode.SYSTEM_ERROR, "删除失败");
    }

    /**
     * 添加教学楼
     */
    @PostMapping("/add")
    public ServerResponse addTeachbuilding(@RequestBody TeachbuildAddRequest t) {
        TeachbuildInfo t1 = new TeachbuildInfo();
        t1.setTeachBuildNo(t.getTeachBuildNo());
        t1.setTeachBuildName(t.getTeachBuildName());
        t1.setTeachBuildLocation(t.getTeachBuildLocation());
        return teachBuildInfoService.save(t1) ? ServerResponse.ofSuccess("添加成功")
                : throwBusiness(ResultCode.SYSTEM_ERROR, "添加失败");
    }

    /**
     * 根据id查询
     */
    @GetMapping("/select/{id}")
    public ServerResponse queryTeachBuildingById(@PathVariable("id") Integer id) {
        TeachbuildInfo teachbuildInfo = teachBuildInfoService.getById(id);
        if (teachbuildInfo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教学楼不存在");
        }
        return ServerResponse.ofSuccess(teachbuildInfo);
    }


    /**
     * 更新教学楼
     */
    @PostMapping("/modify/{id}")
    public ServerResponse modifyTeacher(@PathVariable("id") Integer id, @RequestBody TeachbuildInfo t) {
        requireTeachbuildExists(id);
        return teachBuildInfoService.updateById(t) ? ServerResponse.ofSuccess("更新成功")
                : throwBusiness(ResultCode.SYSTEM_ERROR, "更新失败");
    }

    private void requireTeachbuildExists(Integer id) {
        if (id == null || teachBuildInfoService.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教学楼不存在");
        }
    }

    private ServerResponse throwBusiness(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode, message);
    }

}
