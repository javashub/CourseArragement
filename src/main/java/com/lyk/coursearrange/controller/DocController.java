package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Doc;
import com.lyk.coursearrange.entity.request.DocsVO;
import com.lyk.coursearrange.service.DocService;
import org.apache.commons.collections4.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lequal
 * @since 2020-05-27
 */
@RestController
public class DocController {

    @Autowired
    private DocService docService;

    /**
     * 上传DOC文档，只负责上传并返回文件的url
     * @param file
     * @return
     */
    @PostMapping("/uploaddocs")
    public ServerResponse uploadDocs(MultipartFile file) {
        return docService.uploadDocs(file);
    }

    /**
     * 添加文档的相关描述信息
     * @param docsVO
     * @return
     */
    @PostMapping("/adddocs")
    public ServerResponse addDocs(@RequestBody DocsVO docsVO) {
        return docService.addDcos(docsVO);
    }

    /**
     * 下载文档
     * @param id
     * @return
     */
    @GetMapping(value = "/downloaddocs", consumes = MediaType.ALL_VALUE)
    public ServerResponse downloadDocs(Integer id) {
        return docService.downloadDocs(id);
    }

    /**
     * 分页查询所有的文档
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/docs/{page}")
    public ServerResponse allDocs(@PathVariable("page") Integer page, @RequestParam(defaultValue = "10") Integer limit) {
        Page<Doc> pages = new Page<>(page, limit);
        QueryWrapper<Doc> wrapper = new QueryWrapper<Doc>().orderByDesc("create_time");
        IPage<Doc> iPage = docService.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }


    /**
     * 根据班级查询文档，用于学生端
     * @param page
     * @param toClassNo
     * @param limit
     * @return
     */
    @GetMapping("/docs-class/{page}/{toClassNo}")
    public ServerResponse getDocsByClass(@PathVariable("page") Integer page, @PathVariable("toClassNo") String toClassNo,
                                         @RequestParam(defaultValue = "10") Integer limit) {
        Page<Doc> pages = new Page<>(page, limit);
        QueryWrapper<Doc> wrapper = new QueryWrapper<Doc>().orderByDesc("create_time").eq("to_class_no", toClassNo);

        IPage<Doc> iPage = docService.page(pages, wrapper);
        System.out.println(iPage);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 根据id删除文档
     * @param id
     * @return
     */
    @DeleteMapping("/deletedoc")
    public ServerResponse delete(@RequestParam Integer id) {
        boolean b = docService.removeById(id);
        if (b) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return ServerResponse.ofError("删除失败");
    }



}

