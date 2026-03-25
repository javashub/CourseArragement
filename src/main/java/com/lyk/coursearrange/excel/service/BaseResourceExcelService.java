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

    void writeTeachbuildTemplate(HttpServletResponse response) throws IOException;

    void writeClassroomTemplate(HttpServletResponse response) throws IOException;

    void exportTeachers(String keyword, Integer status, HttpServletResponse response) throws IOException;

    void exportStudents(String keyword, Integer status, HttpServletResponse response) throws IOException;

    void exportCourses(String keyword, Integer status, HttpServletResponse response) throws IOException;

    void exportTeachbuilds(String keyword, HttpServletResponse response) throws IOException;

    void exportClassrooms(String keyword, String buildingCode, HttpServletResponse response) throws IOException;

    ServerResponse importTeachers(MultipartFile file);

    ServerResponse importStudents(MultipartFile file);

    ServerResponse importCourses(MultipartFile file);

    ServerResponse importTeachbuilds(MultipartFile file);

    ServerResponse importClassrooms(MultipartFile file);
}
