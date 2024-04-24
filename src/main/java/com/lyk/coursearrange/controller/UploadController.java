package com.lyk.coursearrange.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: 15760
 * @Date: 2020/5/12
 * @Descripe: 文件上传，读取文件
 */
@RestController
@Slf4j
public class UploadController {


    @Resource
    private UploadService uploadService;

    /**
     * 上传课程计划Excel文件
     */
    @PostMapping("/upload")
    public ServerResponse uploadClassTaskFile(MultipartFile file) {
        log.info("上传 Excel 文件(待排课的文件)。。。");
        return uploadService.upload(file);
    }

    /**
     * 下载系统提供的Excel导入模板
     */
    @GetMapping(value = "/download", consumes = MediaType.ALL_VALUE)
    public void downloadTemplate(HttpServletResponse response) {
        // 获取文件
        File file = new File("doc/课程任务导入模板.xls");
        if (!file.exists()) {
            // 没有该模板文件就调用创建模板文件方法
            log.info("创建模板文件");
            createTemplate();
        }
        // 获取文件名字
        String fileName = file.getName();
        response.reset();
        // 设置ContentType，响应内容为二进制数据流，编码为utf-8，此处设定的编码是文件内容的编码
        response.setContentType("application/octet-stream;charset=utf-8");
        try {
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName + ";filename*=utf-8''" + URLEncoder.encode(fileName, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("文件下载失败: {}", e.getMessage());
        }
        // 实现文件下载
        byte[] buffer = new byte[1024];

        try (
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            // 获取字节流
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            log.info("文件下载成功");
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage());
        }
    }

    /**
     * 如果没有模板文件就创建模板文件
     */
    private void createTemplate() {
        ExportParams params = new ExportParams();
        params.setTitle("课程任务导入模板(请严格对照数据库信息填写)");
        params.setSheetName("课程任务模板");
        List<ClassTask> list = new ArrayList<>();
        Workbook workbook = ExcelExportUtil.exportExcel(params, ClassTask.class, list);
        try {
            // 输出模板到本地
            FileOutputStream fos = new FileOutputStream("doc/课程任务导入模板_new.xls");
            workbook.write(fos);
        } catch (Exception e) {
            log.error("创建模板文件失败: {}", e.getMessage());
        }
    }


}
