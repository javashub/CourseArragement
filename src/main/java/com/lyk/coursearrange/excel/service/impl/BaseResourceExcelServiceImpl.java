package com.lyk.coursearrange.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.AuthAccountSyncService;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.vo.ImportResultVO;
import com.lyk.coursearrange.entity.CourseInfo;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.TeachbuildInfo;
import com.lyk.coursearrange.excel.model.ClassroomExcelRow;
import com.lyk.coursearrange.excel.model.ClassroomImportExcelRow;
import com.lyk.coursearrange.excel.model.CourseInfoExcelRow;
import com.lyk.coursearrange.excel.model.CourseInfoImportExcelRow;
import com.lyk.coursearrange.excel.model.StudentExcelRow;
import com.lyk.coursearrange.excel.model.StudentImportExcelRow;
import com.lyk.coursearrange.excel.model.TeachbuildExcelRow;
import com.lyk.coursearrange.excel.model.TeachbuildImportExcelRow;
import com.lyk.coursearrange.excel.model.TeacherExcelRow;
import com.lyk.coursearrange.excel.model.TeacherImportExcelRow;
import com.lyk.coursearrange.excel.service.BaseResourceExcelService;
import com.lyk.coursearrange.service.ClassroomService;
import com.lyk.coursearrange.service.CourseInfoService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeachbuildInfoService;
import com.lyk.coursearrange.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础资源 Excel 服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseResourceExcelServiceImpl implements BaseResourceExcelService {

    private final TeacherService teacherService;
    private final StudentService studentService;
    private final CourseInfoService courseInfoService;
    private final TeachbuildInfoService teachbuildInfoService;
    private final ClassroomService classroomService;
    private final PasswordService passwordService;
    private final AuthAccountSyncService authAccountSyncService;

    @Override
    public void writeTeacherTemplate(HttpServletResponse response) throws IOException {
        List<TeacherImportExcelRow> rows = new ArrayList<>();
        TeacherImportExcelRow sample = new TeacherImportExcelRow();
        sample.setTeacherNo("T2026001");
        sample.setUsername("zhangsan");
        sample.setRealname("张老师");
        sample.setJobtitle("讲师");
        sample.setTeach("高等数学");
        sample.setAge(35);
        sample.setTelephone("13800000000");
        sample.setEmail("teacher@school.edu.cn");
        sample.setAddress("主校区教师公寓 3 栋 302");
        sample.setStatusText("正常");
        rows.add(sample);
        writeExcel(response, "教师导入模板.xlsx", "教师导入模板", TeacherImportExcelRow.class, rows);
    }

    @Override
    public void writeStudentTemplate(HttpServletResponse response) throws IOException {
        List<StudentImportExcelRow> rows = new ArrayList<>();
        StudentImportExcelRow sample = new StudentImportExcelRow();
        sample.setStudentNo("2026020001");
        sample.setUsername("lisi");
        sample.setRealname("李同学");
        sample.setGrade("2025");
        sample.setClassNo("2501");
        sample.setAge(18);
        sample.setTelephone("13800000001");
        sample.setEmail("student@school.edu.cn");
        sample.setAddress("主校区宿舍 5 栋 402");
        sample.setStatusText("正常");
        rows.add(sample);
        writeExcel(response, "学生导入模板.xlsx", "学生导入模板", StudentImportExcelRow.class, rows);
    }

    @Override
    public void writeCourseTemplate(HttpServletResponse response) throws IOException {
        List<CourseInfoImportExcelRow> rows = new ArrayList<>();
        CourseInfoImportExcelRow sample = new CourseInfoImportExcelRow();
        sample.setCourseNo("10001");
        sample.setCourseName("高等数学");
        sample.setCourseAttr("必修");
        sample.setPublisher("高等教育出版社");
        sample.setPiority(1);
        sample.setStatusText("正常");
        sample.setRemark("大一上学期核心课程");
        rows.add(sample);
        writeExcel(response, "课程导入模板.xlsx", "课程导入模板", CourseInfoImportExcelRow.class, rows);
    }

    @Override
    public void writeTeachbuildTemplate(HttpServletResponse response) throws IOException {
        List<TeachbuildImportExcelRow> rows = new ArrayList<>();
        TeachbuildImportExcelRow sample = new TeachbuildImportExcelRow();
        sample.setTeachBuildNo("08");
        sample.setTeachBuildName("实验楼");
        sample.setTeachBuildLocation("主校区东区");
        rows.add(sample);
        writeExcel(response, "教学楼导入模板.xlsx", "教学楼导入模板", TeachbuildImportExcelRow.class, rows);
    }

    @Override
    public void writeClassroomTemplate(HttpServletResponse response) throws IOException {
        List<ClassroomImportExcelRow> rows = new ArrayList<>();
        ClassroomImportExcelRow sample = new ClassroomImportExcelRow();
        sample.setClassroomNo("08-302");
        sample.setClassroomName("实验楼 302");
        sample.setTeachbuildNo("08");
        sample.setCapacity(60);
        sample.setAttr("实验室");
        sample.setRemark("支持投影和实验台");
        rows.add(sample);
        writeExcel(response, "教室导入模板.xlsx", "教室导入模板", ClassroomImportExcelRow.class, rows);
    }

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

    @Override
    public void exportTeachbuilds(String keyword, HttpServletResponse response) throws IOException {
        List<TeachbuildExcelRow> rows = teachbuildInfoService.list(new LambdaQueryWrapper<TeachbuildInfo>()
                        .like(StringUtils.isNotBlank(keyword), TeachbuildInfo::getTeachBuildName, keyword)
                        .or(StringUtils.isNotBlank(keyword))
                        .like(StringUtils.isNotBlank(keyword), TeachbuildInfo::getTeachBuildNo, keyword)
                        .orderByAsc(TeachbuildInfo::getTeachBuildNo))
                .stream()
                .map(this::toTeachbuildRow)
                .collect(Collectors.toList());
        writeExcel(response, "教学楼数据导出.xlsx", "教学楼数据", TeachbuildExcelRow.class, rows);
    }

    @Override
    public void exportClassrooms(String keyword, String teachbuildNo, HttpServletResponse response) throws IOException {
        List<ClassroomExcelRow> rows = classroomService.list(new LambdaQueryWrapper<Classroom>()
                        .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                                .like(Classroom::getClassroomNo, keyword)
                                .or()
                                .like(Classroom::getClassroomName, keyword))
                        .eq(StringUtils.isNotBlank(teachbuildNo), Classroom::getTeachbuildNo, teachbuildNo)
                        .orderByAsc(Classroom::getClassroomNo))
                .stream()
                .map(this::toClassroomRow)
                .collect(Collectors.toList());
        writeExcel(response, "教室数据导出.xlsx", "教室数据", ClassroomExcelRow.class, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse importTeachers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ServerResponse.ofError("请选择要导入的教师 Excel 文件");
        }
        List<TeacherImportExcelRow> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(TeacherImportExcelRow.class).sheet().doReadSync();
        } catch (Exception exception) {
            log.error("读取教师 Excel 失败", exception);
            return ServerResponse.ofError("读取教师 Excel 失败，请检查模板格式");
        }
        List<String> errors = new ArrayList<>();
        int imported = 0;
        for (int index = 0; index < rows.size(); index++) {
            TeacherImportExcelRow row = rows.get(index);
            int before = errors.size();
            validateTeacherRow(row, index + 2, errors);
            if (errors.size() > before) {
                continue;
            }
            Teacher teacher = teacherService.getOne(new LambdaQueryWrapper<Teacher>().eq(Teacher::getTeacherNo, trim(row.getTeacherNo())));
            boolean isNew = teacher == null;
            if (isNew) {
                teacher = new Teacher();
                teacher.setTeacherNo(trim(row.getTeacherNo()));
                teacher.setPassword(passwordService.encode("123456"));
            }
            teacher.setUsername(StringUtils.defaultIfBlank(trim(row.getUsername()), teacher.getTeacherNo()));
            teacher.setRealname(trim(row.getRealname()));
            teacher.setJobtitle(trim(row.getJobtitle()));
            teacher.setTeach(trim(row.getTeach()));
            teacher.setAge(row.getAge());
            teacher.setTelephone(trim(row.getTelephone()));
            teacher.setEmail(trim(row.getEmail()));
            teacher.setAddress(trim(row.getAddress()));
            teacher.setStatus(parseStatus(trim(row.getStatusText()), 0));
            teacherService.saveOrUpdate(teacher);
            authAccountSyncService.syncTeacherAccount(teacher);
            imported++;
        }
        return buildImportResponse(rows.size(), errors, imported, "教师");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse importStudents(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ServerResponse.ofError("请选择要导入的学生 Excel 文件");
        }
        List<StudentImportExcelRow> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(StudentImportExcelRow.class).sheet().doReadSync();
        } catch (Exception exception) {
            log.error("读取学生 Excel 失败", exception);
            return ServerResponse.ofError("读取学生 Excel 失败，请检查模板格式");
        }
        List<String> errors = new ArrayList<>();
        int imported = 0;
        for (int index = 0; index < rows.size(); index++) {
            StudentImportExcelRow row = rows.get(index);
            int before = errors.size();
            validateStudentRow(row, index + 2, errors);
            if (errors.size() > before) {
                continue;
            }
            Student student = studentService.getOne(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, trim(row.getStudentNo())));
            boolean isNew = student == null;
            if (isNew) {
                student = new Student();
                student.setStudentNo(trim(row.getStudentNo()));
                student.setPassword(passwordService.encode("123456"));
            }
            student.setUsername(StringUtils.defaultIfBlank(trim(row.getUsername()), student.getStudentNo()));
            student.setRealname(trim(row.getRealname()));
            student.setGrade(trim(row.getGrade()));
            student.setClassNo(trim(row.getClassNo()));
            student.setAge(row.getAge());
            student.setTelephone(trim(row.getTelephone()));
            student.setEmail(trim(row.getEmail()));
            student.setAddress(trim(row.getAddress()));
            student.setStatus(parseStatus(trim(row.getStatusText()), 0));
            studentService.saveOrUpdate(student);
            authAccountSyncService.syncStudentAccount(student);
            imported++;
        }
        return buildImportResponse(rows.size(), errors, imported, "学生");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse importCourses(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ServerResponse.ofError("请选择要导入的课程 Excel 文件");
        }
        List<CourseInfoImportExcelRow> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(CourseInfoImportExcelRow.class).sheet().doReadSync();
        } catch (Exception exception) {
            log.error("读取课程 Excel 失败", exception);
            return ServerResponse.ofError("读取课程 Excel 失败，请检查模板格式");
        }
        List<String> errors = new ArrayList<>();
        int imported = 0;
        for (int index = 0; index < rows.size(); index++) {
            CourseInfoImportExcelRow row = rows.get(index);
            int before = errors.size();
            validateCourseRow(row, index + 2, errors);
            if (errors.size() > before) {
                continue;
            }
            CourseInfo courseInfo = courseInfoService.getOne(new LambdaQueryWrapper<CourseInfo>().eq(CourseInfo::getCourseNo, trim(row.getCourseNo())));
            if (courseInfo == null) {
                courseInfo = new CourseInfo();
                courseInfo.setCourseNo(trim(row.getCourseNo()));
            }
            courseInfo.setCourseName(trim(row.getCourseName()));
            courseInfo.setCourseAttr(trim(row.getCourseAttr()));
            courseInfo.setPublisher(trim(row.getPublisher()));
            courseInfo.setPiority(row.getPiority() == null ? 0 : row.getPiority());
            courseInfo.setStatus(parseStatus(trim(row.getStatusText()), 0));
            courseInfo.setRemark(trim(row.getRemark()));
            courseInfoService.saveOrUpdate(courseInfo);
            imported++;
        }
        return buildImportResponse(rows.size(), errors, imported, "课程");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse importTeachbuilds(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ServerResponse.ofError("请选择要导入的教学楼 Excel 文件");
        }
        List<TeachbuildImportExcelRow> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(TeachbuildImportExcelRow.class).sheet().doReadSync();
        } catch (Exception exception) {
            log.error("读取教学楼 Excel 失败", exception);
            return ServerResponse.ofError("读取教学楼 Excel 失败，请检查模板格式");
        }
        List<String> errors = new ArrayList<>();
        int imported = 0;
        for (int index = 0; index < rows.size(); index++) {
            TeachbuildImportExcelRow row = rows.get(index);
            int before = errors.size();
            validateTeachbuildRow(row, index + 2, errors);
            if (errors.size() > before) {
                continue;
            }
            TeachbuildInfo teachbuildInfo = teachbuildInfoService.getOne(new LambdaQueryWrapper<TeachbuildInfo>()
                    .eq(TeachbuildInfo::getTeachBuildNo, trim(row.getTeachBuildNo())));
            if (teachbuildInfo == null) {
                teachbuildInfo = new TeachbuildInfo();
                teachbuildInfo.setTeachBuildNo(trim(row.getTeachBuildNo()));
            }
            teachbuildInfo.setTeachBuildName(trim(row.getTeachBuildName()));
            teachbuildInfo.setTeachBuildLocation(trim(row.getTeachBuildLocation()));
            teachbuildInfoService.saveOrUpdate(teachbuildInfo);
            imported++;
        }
        return buildImportResponse(rows.size(), errors, imported, "教学楼");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse importClassrooms(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ServerResponse.ofError("请选择要导入的教室 Excel 文件");
        }
        List<ClassroomImportExcelRow> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(ClassroomImportExcelRow.class).sheet().doReadSync();
        } catch (Exception exception) {
            log.error("读取教室 Excel 失败", exception);
            return ServerResponse.ofError("读取教室 Excel 失败，请检查模板格式");
        }
        List<String> errors = new ArrayList<>();
        int imported = 0;
        for (int index = 0; index < rows.size(); index++) {
            ClassroomImportExcelRow row = rows.get(index);
            int before = errors.size();
            validateClassroomRow(row, index + 2, errors);
            if (errors.size() > before) {
                continue;
            }
            Classroom classroom = classroomService.getOne(new LambdaQueryWrapper<Classroom>()
                    .eq(Classroom::getClassroomNo, trim(row.getClassroomNo())));
            if (classroom == null) {
                classroom = new Classroom();
                classroom.setClassroomNo(trim(row.getClassroomNo()));
            }
            classroom.setClassroomName(trim(row.getClassroomName()));
            classroom.setTeachbuildNo(trim(row.getTeachbuildNo()));
            classroom.setCapacity(row.getCapacity() == null ? 0 : row.getCapacity());
            classroom.setAttr(trim(row.getAttr()));
            classroom.setRemark(trim(row.getRemark()));
            classroomService.saveOrUpdate(classroom);
            imported++;
        }
        return buildImportResponse(rows.size(), errors, imported, "教室");
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

    private TeachbuildExcelRow toTeachbuildRow(TeachbuildInfo teachbuildInfo) {
        TeachbuildExcelRow row = new TeachbuildExcelRow();
        row.setTeachBuildNo(teachbuildInfo.getTeachBuildNo());
        row.setTeachBuildName(teachbuildInfo.getTeachBuildName());
        row.setTeachBuildLocation(teachbuildInfo.getTeachBuildLocation());
        return row;
    }

    private ClassroomExcelRow toClassroomRow(Classroom classroom) {
        ClassroomExcelRow row = new ClassroomExcelRow();
        row.setClassroomNo(classroom.getClassroomNo());
        row.setClassroomName(classroom.getClassroomName());
        row.setTeachbuildNo(classroom.getTeachbuildNo());
        row.setCapacity(classroom.getCapacity());
        row.setAttr(classroom.getAttr());
        row.setRemark(classroom.getRemark());
        return row;
    }

    private void validateTeacherRow(TeacherImportExcelRow row, int rowNum, List<String> errors) {
        if (StringUtils.isBlank(trim(row.getTeacherNo()))) {
            errors.add("第 " + rowNum + " 行缺少教师编号");
        }
        if (StringUtils.isBlank(trim(row.getRealname()))) {
            errors.add("第 " + rowNum + " 行缺少教师姓名");
        }
    }

    private void validateStudentRow(StudentImportExcelRow row, int rowNum, List<String> errors) {
        if (StringUtils.isBlank(trim(row.getStudentNo()))) {
            errors.add("第 " + rowNum + " 行缺少学号");
        }
        if (StringUtils.isBlank(trim(row.getRealname()))) {
            errors.add("第 " + rowNum + " 行缺少学生姓名");
        }
    }

    private void validateCourseRow(CourseInfoImportExcelRow row, int rowNum, List<String> errors) {
        if (StringUtils.isBlank(trim(row.getCourseNo()))) {
            errors.add("第 " + rowNum + " 行缺少课程编号");
        }
        if (StringUtils.isBlank(trim(row.getCourseName()))) {
            errors.add("第 " + rowNum + " 行缺少课程名称");
        }
    }

    private void validateTeachbuildRow(TeachbuildImportExcelRow row, int rowNum, List<String> errors) {
        if (StringUtils.isBlank(trim(row.getTeachBuildNo()))) {
            errors.add("第 " + rowNum + " 行缺少教学楼编号");
        }
        if (StringUtils.isBlank(trim(row.getTeachBuildName()))) {
            errors.add("第 " + rowNum + " 行缺少教学楼名称");
        }
    }

    private void validateClassroomRow(ClassroomImportExcelRow row, int rowNum, List<String> errors) {
        if (StringUtils.isBlank(trim(row.getClassroomNo()))) {
            errors.add("第 " + rowNum + " 行缺少教室编号");
        }
        if (StringUtils.isBlank(trim(row.getClassroomName()))) {
            errors.add("第 " + rowNum + " 行缺少教室名称");
        }
        if (StringUtils.isBlank(trim(row.getTeachbuildNo()))) {
            errors.add("第 " + rowNum + " 行缺少教学楼编号");
        }
    }

    private ServerResponse buildImportResponse(int totalCount, List<String> errors, int imported, String resourceName) {
        if (!errors.isEmpty()) {
            return ServerResponse.ofError(resourceName + "导入失败，请修正后重试", buildImportResult(totalCount, imported, errors));
        }
        return ServerResponse.ofSuccess(String.format("%s导入成功，共 %s 条", resourceName, imported),
                buildImportResult(totalCount, imported, List.of()));
    }

    private Integer parseStatus(String statusText, int defaultValue) {
        if (StringUtils.isBlank(statusText)) {
            return defaultValue;
        }
        String normalized = statusText.trim();
        if ("正常".equals(normalized) || "启用".equals(normalized) || "0".equals(normalized)) {
            return 0;
        }
        if ("封禁".equals(normalized) || "停用".equals(normalized) || "1".equals(normalized)) {
            return 1;
        }
        return defaultValue;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private ImportResultVO buildImportResult(int totalCount, int successCount, List<String> errors) {
        return ImportResultVO.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failedCount(Math.max(totalCount - successCount, 0))
                .errors(errors)
                .build();
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
