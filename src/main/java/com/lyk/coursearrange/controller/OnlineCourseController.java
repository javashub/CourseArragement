package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.OnlineCourse;
import com.lyk.coursearrange.entity.request.OnlineCourseAddVO;
import com.lyk.coursearrange.service.OnlineCourseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网课名称，每一个网课下面有1到多个视频
 * @author lequal
 * @since 2020-06-04
 */
@RestController
@RequestMapping("/onlinecourse")
public class OnlineCourseController {

    @Autowired
    private OnlineCourseService ocs;

    /**
     * 首页展示的热门课程
     * @param limit 查询数据的条数
     * @return
     */
    @GetMapping("/hot/{page}/{limit}")
    public ServerResponse hotCourse(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.orderByDesc("clicks");
        Page<OnlineCourse> pages = new Page<>(page, limit);
        IPage<OnlineCourse> iPage = ocs.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 根据二级分类id查询网课
     * @param id 二级分类id
     * @return
     */
    @GetMapping("/get-list/{id}/{page}/{limit}")
    public ServerResponse queryByCategory(@PathVariable("id") Integer id,
                                          @PathVariable("page") Integer page,
                                          @PathVariable("limit") Integer limit) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("online_category_id", id);
        Page<OnlineCourse> pages = new Page<>(page, limit);
        IPage<OnlineCourse> iPage = ocs.page(pages, wrapper);
        if (page != null) {
            return ServerResponse.ofSuccess(iPage);
        }
        return ServerResponse.ofError("查询不到数据");
    }

    /**
     * 分页查询网课
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/list/{page}")
    public ServerResponse list(@PathVariable("page") Integer page, @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.orderByDesc("update_time");
        Page<OnlineCourse> pages = new Page<>(page, limit);
        IPage<OnlineCourse> iPage = ocs.page(pages, wrapper);
        if (page != null) {
            return ServerResponse.ofSuccess(iPage);
        }
        return ServerResponse.ofError("查询不到数据");
    }


    /**
     * 添加网课
     * @param on
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addCourse(@RequestBody OnlineCourseAddVO on) {
        OnlineCourse onlineCourse = new OnlineCourse();
        // 获取网课编号
        String no = getCourseNo();
        if(!StringUtils.isEmpty(no)) {
            onlineCourse.setOnlineNo(no);
        }
        onlineCourse.setOnlineName(on.getOnlineName());
        onlineCourse.setDescription(on.getDescription());
        onlineCourse.setCover(on.getCover());
        onlineCourse.setOnlineCategoryId(on.getOnlineCategoryId());
        onlineCourse.setOnlineCategoryName(on.getOnlineCategoryName());
        onlineCourse.setFromUserId(on.getFromUserId());
        onlineCourse.setFromUserType(on.getFromUserType());
        onlineCourse.setFromUserName(on.getFromUserName());
        boolean b = ocs.save(onlineCourse);
        if (b) {
            return ServerResponse.ofSuccess("添加网课成功");
        }
        return ServerResponse.ofError("添加网课失败");
    }

    /**
     * 根据id删除网课
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse delete(@PathVariable("id") Integer id) {
        boolean b = ocs.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除网课成功");
        }
        return ServerResponse.ofError("删除网课失败");
    }


    /**
     * 自动获取网课编号
     * @return
     */
    private String getCourseNo() {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.select("online_no").orderByDesc("online_no");
        List<OnlineCourse> list = ocs.list(wrapper);
        String no = String.valueOf(Integer.parseInt(list.get(0).getOnlineNo() + 1));
        return no;
    }

}

