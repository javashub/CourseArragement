package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.OnlineCategory;
import com.lyk.coursearrange.service.OnlineCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lequal
 * @since 2020-06-04
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class OnlineCategoryController {

    @Autowired
    private OnlineCategoryService ocs;

    /**
     * 添加类别
     * @param categoryNo
     * @param categoryName
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addCategory(@RequestParam(name = "categoryNo") String categoryNo,
                                      @RequestParam(name = "categoryName") String categoryName,
                                      @RequestParam(name = "parentId") Integer parentId) {
        OnlineCategory onlineCategory = new OnlineCategory();
        onlineCategory.setCategoryNo(categoryNo);
        onlineCategory.setCategoryName(categoryName);
        onlineCategory.setParentId(parentId);
        boolean b = ocs.save(onlineCategory);
        if (b) {
            return ServerResponse.ofSuccess("添加成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加失败");
    }


    /**
     * 删除类别
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteCategory(@PathVariable("id") Integer id) {
        requireCategoryExists(id);
        boolean b = ocs.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除类别成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除类别失败");
    }

    /**
     * 查询所有的一级类别
     * @return
     */
    @GetMapping("/one")
    public ServerResponse queryOne() {
        QueryWrapper wrapper = new QueryWrapper();
        // 查询父id为0的类别，即一级分类
        wrapper.eq("parent_id", 0);
        List<OnlineCategory> list = ocs.list(wrapper);
        return ServerResponse.ofSuccess(list);
    }

    /**
     * 查询每个类别下面的二级类别
     * @param id 一级分类id
     * @return
     */
    @GetMapping("/two/{id}")
    public ServerResponse queryTwo(@PathVariable("id") Integer id) {
        QueryWrapper wrapper = new QueryWrapper();
        // 该id下面的二级分类
        wrapper.eq("parent_id", id);
        List<OnlineCategory> list = ocs.list(wrapper);
        return ServerResponse.ofSuccess(list);
    }


    /**
     * 点击添加类别的时候请求这个接口给前端返回一个编号
     * @return
     */
    @GetMapping("/get-no")
    public ServerResponse getNo() {
        QueryWrapper<OnlineCategory> wrapper = new QueryWrapper<OnlineCategory>().select("category_no").orderByDesc("category_no");
        List<OnlineCategory> list = ocs.list(wrapper);
        if (list.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "暂无在线分类数据，无法生成编号");
        }
        String no = String.valueOf(Integer.parseInt(list.get(0).getCategoryNo()) + 1);
        log.info("生成在线分类编号成功，categoryNo={}", no);
        // 返回自动生成的编号，从res.data.message中获取
        return ServerResponse.ofSuccess(no);
    }

    private void requireCategoryExists(Integer id) {
        if (id == null || ocs.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "在线分类不存在");
        }
    }

}
