package com.lyk.coursearrange.excel.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基础资源 Excel 服务。
 */
public interface BaseResourceExcelService {

    void exportTeachers(String keyword, Integer status, HttpServletResponse response) throws IOException;

    void exportStudents(String keyword, Integer status, HttpServletResponse response) throws IOException;

    void exportCourses(String keyword, Integer status, HttpServletResponse response) throws IOException;
}
