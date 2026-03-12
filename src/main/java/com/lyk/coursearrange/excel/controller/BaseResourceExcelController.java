package com.lyk.coursearrange.excel.controller;

import com.lyk.coursearrange.excel.service.BaseResourceExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基础资源 Excel 控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/excel/base")
public class BaseResourceExcelController {

    private final BaseResourceExcelService baseResourceExcelService;

    @GetMapping("/teachers/export")
    public void exportTeachers(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status,
                               HttpServletResponse response) throws IOException {
        baseResourceExcelService.exportTeachers(keyword, status, response);
    }

    @GetMapping("/students/export")
    public void exportStudents(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status,
                               HttpServletResponse response) throws IOException {
        baseResourceExcelService.exportStudents(keyword, status, response);
    }

    @GetMapping("/courses/export")
    public void exportCourses(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status,
                              HttpServletResponse response) throws IOException {
        baseResourceExcelService.exportCourses(keyword, status, response);
    }
}
