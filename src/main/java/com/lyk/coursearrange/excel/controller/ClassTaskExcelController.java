package com.lyk.coursearrange.excel.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.excel.service.ClassTaskExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 课程任务 Excel 控制器。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/excel/class-task")
public class ClassTaskExcelController {

    private final ClassTaskExcelService classTaskExcelService;

    /**
     * 下载课程任务导入模板。
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        classTaskExcelService.writeTemplate(response);
    }

    /**
     * 导入课程任务。
     */
    @PostMapping("/import")
    public ServerResponse importClassTasks(@RequestPart("file") MultipartFile file) {
        log.info("开始导入课程任务 Excel");
        return classTaskExcelService.importClassTasks(file);
    }
}
