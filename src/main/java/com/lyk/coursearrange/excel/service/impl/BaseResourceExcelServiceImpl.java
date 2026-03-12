package com.lyk.coursearrange.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.entity.CourseInfo;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.excel.model.CourseInfoExcelRow;
import com.lyk.coursearrange.excel.model.StudentExcelRow;
import com.lyk.coursearrange.excel.model.TeacherExcelRow;
import com.lyk.coursearrange.excel.service.BaseResourceExcelService;
import com.lyk.coursearrange.service.CourseInfoService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础资源 Excel 服务实现。
 */
@Service
@RequiredArgsConstructor
public class BaseResourceExcelServiceImpl implements BaseResourceExcelService {

    private final TeacherService teacherService;
    private final StudentService studentService;
    private final CourseInfoService courseInfoService;

    @Override
    public void exportTeachers(String keyword, Integer status, HttpServletResponse response) throws IOException {
        List<TeacherExcelRow> rows = teacherService.list(new LambdaQueryWrapper<Teacher>()
                        .like(StringUtils.isNotBlank(keyword), Teacher::getRealname, keyword)
                        .eq(status != null, Teacher::getStatus, status)
                        .orderByAsc(Teacher::getTeacherNo))
                .stream()
                .map(this::toTeacherRow)
                .collect(Collectors.toList());
        writeExcel(response, "教师数据导出.xlsx", "教师数据", TeacherExcelRow.class, rows);
    }

    @Override
    public void exportStudents(String keyword, Integer status, HttpServletResponse response) throws IOException {
        List<StudentExcelRow> rows = studentService.list(new LambdaQueryWrapper<Student>()
                        .like(StringUtils.isNotBlank(keyword), Student::getRealname, keyword)
                        .eq(status != null, Student::getStatus, status)
                        .orderByAsc(Student::getStudentNo))
                .stream()
                .map(this::toStudentRow)
                .collect(Collectors.toList());
        writeExcel(response, "学生数据导出.xlsx", "学生数据", StudentExcelRow.class, rows);
    }

    @Override
    public void exportCourses(String keyword, Integer status, HttpServletResponse response) throws IOException {
        List<CourseInfoExcelRow> rows = courseInfoService.list(new LambdaQueryWrapper<CourseInfo>()
                        .like(StringUtils.isNotBlank(keyword), CourseInfo::getCourseName, keyword)
                        .eq(status != null, CourseInfo::getStatus, status)
                        .orderByAsc(CourseInfo::getCourseNo))
                .stream()
                .map(this::toCourseRow)
                .collect(Collectors.toList());
        writeExcel(response, "课程数据导出.xlsx", "课程数据", CourseInfoExcelRow.class, rows);
    }

    private TeacherExcelRow toTeacherRow(Teacher teacher) {
        TeacherExcelRow row = new TeacherExcelRow();
        row.setTeacherNo(teacher.getTeacherNo());
        row.setUsername(teacher.getUsername());
        row.setRealname(teacher.getRealname());
        row.setJobtitle(teacher.getJobtitle());
        row.setTeach(teacher.getTeach());
        row.setAge(teacher.getAge());
        row.setTelephone(teacher.getTelephone());
        row.setEmail(teacher.getEmail());
        row.setAddress(teacher.getAddress());
        row.setStatusText(teacher.getStatus() != null && teacher.getStatus() == 0 ? "正常" : "封禁");
        return row;
    }

    private StudentExcelRow toStudentRow(Student student) {
        StudentExcelRow row = new StudentExcelRow();
        row.setStudentNo(student.getStudentNo());
        row.setUsername(student.getUsername());
        row.setRealname(student.getRealname());
        row.setGrade(student.getGrade());
        row.setClassNo(student.getClassNo());
        row.setAge(student.getAge());
        row.setTelephone(student.getTelephone());
        row.setEmail(student.getEmail());
        row.setAddress(student.getAddress());
        row.setStatusText(student.getStatus() != null && student.getStatus() == 0 ? "正常" : "封禁");
        return row;
    }

    private CourseInfoExcelRow toCourseRow(CourseInfo course) {
        CourseInfoExcelRow row = new CourseInfoExcelRow();
        row.setCourseNo(course.getCourseNo());
        row.setCourseName(course.getCourseName());
        row.setCourseAttr(course.getCourseAttr());
        row.setPublisher(course.getPublisher());
        row.setPiority(course.getPiority());
        row.setStatusText(course.getStatus() != null && course.getStatus() == 0 ? "正常" : "停用");
        row.setRemark(course.getRemark());
        return row;
    }

    private <T> void writeExcel(HttpServletResponse response, String fileName, String sheetName,
                                Class<T> headClass, List<T> rows) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition",
                "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        EasyExcel.write(response.getOutputStream(), headClass)
                .sheet(sheetName)
                .doWrite(rows);
    }
}
