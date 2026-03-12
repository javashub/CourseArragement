package com.lyk.coursearrange.excel.controller;

import com.lyk.coursearrange.excel.service.BaseResourceExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/teachers/template")
    public void downloadTeacherTemplate(HttpServletResponse response) throws IOException {
        baseResourceExcelService.writeTeacherTemplate(response);
    }

    @GetMapping("/teachers/export")
    public void exportTeachers(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status,
                               HttpServletResponse response) throws IOException {
        baseResourceExcelService.exportTeachers(keyword, status, response);
    }

    @PostMapping("/teachers/import")
    public Object importTeachers(@RequestPart("file") MultipartFile file) {
        return baseResourceExcelService.importTeachers(file);
    }

    @GetMapping("/students/template")
    public void downloadStudentTemplate(HttpServletResponse response) throws IOException {
        baseResourceExcelService.writeStudentTemplate(response);
    }

    @GetMapping("/students/export")
    public void exportStudents(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status,
                               HttpServletResponse response) throws IOException {
        baseResourceExcelService.exportStudents(keyword, status, response);
    }

    @PostMapping("/students/import")
    public Object importStudents(@RequestPart("file") MultipartFile file) {
        return baseResourceExcelService.importStudents(file);
    }

    @GetMapping("/courses/template")
    public void downloadCourseTemplate(HttpServletResponse response) throws IOException {
        baseResourceExcelService.writeCourseTemplate(response);
    }

    @GetMapping("/courses/export")
    public void exportCourses(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status,
                              HttpServletResponse response) throws IOException {
        baseResourceExcelService.exportCourses(keyword, status, response);
    }

    @PostMapping("/courses/import")
    public Object importCourses(@RequestPart("file") MultipartFile file) {
        return baseResourceExcelService.importCourses(file);
    }
}
