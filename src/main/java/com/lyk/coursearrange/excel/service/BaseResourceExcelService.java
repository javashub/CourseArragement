package com.lyk.coursearrange.excel.service;

import com.lyk.coursearrange.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基础资源 Excel 服务。
 */
public interface BaseResourceExcelService {

    void writeTeacherTemplate(HttpServletResponse response) throws IOException;

    void writeStudentTemplate(HttpServletResponse response) throws IOException;

    void writeCourseTemplate(HttpServletResponse response) throws IOException;

    void exportTeachers(String keyword, Integer status, HttpServletResponse response) throws IOException;

    void exportStudents(String keyword, Integer status, HttpServletResponse response) throws IOException;

    void exportCourses(String keyword, Integer status, HttpServletResponse response) throws IOException;

    ServerResponse importTeachers(MultipartFile file);

    ServerResponse importStudents(MultipartFile file);

    ServerResponse importCourses(MultipartFile file);
}
