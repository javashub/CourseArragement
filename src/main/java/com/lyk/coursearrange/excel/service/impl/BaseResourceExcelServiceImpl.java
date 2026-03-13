package com.lyk.coursearrange.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.vo.ImportResultVO;
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
import com.lyk.coursearrange.resource.entity.ResBuilding;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.entity.ResStudent;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResBuildingService;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.service.ResStudentService;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.resource.service.ResourceAccountSyncService;
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

    private final ResTeacherService teacherService;
    private final ResStudentService studentService;
    private final ResCourseService courseInfoService;
    private final ResBuildingService teachbuildInfoService;
    private final ResClassroomService classroomService;
    private final ResourceAccountSyncService resourceAccountSyncService;

    @Override
    public void writeTeacherTemplate(HttpServletResponse response) throws IOException {
        List<TeacherImportExcelRow> rows = new ArrayList<>();
        TeacherImportExcelRow sample = new TeacherImportExcelRow();
        sample.setTeacherNo("T2026001");
        sample.setRealname("张老师");
        sample.setJobtitle("讲师");
        sample.setTeach("主授高等数学");
        sample.setTelephone("13800000000");
        sample.setEmail("teacher@school.edu.cn");
        sample.setStatusText("启用");
        rows.add(sample);
        writeExcel(response, "教师导入模板.xlsx", "教师导入模板", TeacherImportExcelRow.class, rows);
    }

    @Override
    public void writeStudentTemplate(HttpServletResponse response) throws IOException {
        List<StudentImportExcelRow> rows = new ArrayList<>();
        StudentImportExcelRow sample = new StudentImportExcelRow();
        sample.setStudentNo("2026020001");
        sample.setRealname("李同学");
        sample.setGrade("2026级");
        sample.setClassNo("2501班");
        sample.setTelephone("13800000001");
        sample.setEmail("student@school.edu.cn");
        sample.setAddress("备注：主校区宿舍 5 栋 402");
        sample.setStatusText("启用");
        rows.add(sample);
        writeExcel(response, "学生导入模板.xlsx", "学生导入模板", StudentImportExcelRow.class, rows);
    }

    @Override
    public void writeCourseTemplate(HttpServletResponse response) throws IOException {
        List<CourseInfoImportExcelRow> rows = new ArrayList<>();
        CourseInfoImportExcelRow sample = new CourseInfoImportExcelRow();
        sample.setCourseNo("C10001");
        sample.setCourseName("高等数学");
        sample.setCourseAttr("REQUIRED");
        sample.setPublisher("高数");
        sample.setPiority(2);
        sample.setStatusText("启用");
        sample.setRemark("大一上学期核心课程");
        rows.add(sample);
        writeExcel(response, "课程导入模板.xlsx", "课程导入模板", CourseInfoImportExcelRow.class, rows);
    }

    @Override
    public void writeTeachbuildTemplate(HttpServletResponse response) throws IOException {
        List<TeachbuildImportExcelRow> rows = new ArrayList<>();
        TeachbuildImportExcelRow sample = new TeachbuildImportExcelRow();
        sample.setTeachBuildNo("B08");
        sample.setTeachBuildName("实验楼");
        sample.setTeachBuildLocation("主校区东区，默认公共教学楼");
        rows.add(sample);
        writeExcel(response, "教学楼导入模板.xlsx", "教学楼导入模板", TeachbuildImportExcelRow.class, rows);
    }

    @Override
    public void writeClassroomTemplate(HttpServletResponse response) throws IOException {
        List<ClassroomImportExcelRow> rows = new ArrayList<>();
        ClassroomImportExcelRow sample = new ClassroomImportExcelRow();
        sample.setClassroomNo("B08-302");
        sample.setClassroomName("实验楼 302");
        sample.setTeachbuildNo("B08");
        sample.setCapacity(60);
        sample.setAttr("LAB");
        sample.setRemark("支持投影和实验台");
        rows.add(sample);
        writeExcel(response, "教室导入模板.xlsx", "教室导入模板", ClassroomImportExcelRow.class, rows);
    }

    @Override
    public void exportTeachers(String keyword, Integer status, HttpServletResponse response) throws IOException {
        List<TeacherExcelRow> rows = teacherService.list(new LambdaQueryWrapper<ResTeacher>()
                        .eq(ResTeacher::getDeleted, 0)
                        .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                                .like(ResTeacher::getTeacherName, keyword)
                                .or()
                                .like(ResTeacher::getTeacherCode, keyword))
                        .eq(status != null, ResTeacher::getStatus, status)
                        .orderByAsc(ResTeacher::getTeacherCode))
                .stream()
                .map(this::toTeacherRow)
                .collect(Collectors.toList());
        writeExcel(response, "教师数据导出.xlsx", "教师数据", TeacherExcelRow.class, rows);
    }

    @Override
    public void exportStudents(String keyword, Integer status, HttpServletResponse response) throws IOException {
        List<StudentExcelRow> rows = studentService.list(new LambdaQueryWrapper<ResStudent>()
                        .eq(ResStudent::getDeleted, 0)
                        .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                                .like(ResStudent::getStudentName, keyword)
                                .or()
                                .like(ResStudent::getStudentCode, keyword))
                        .eq(status != null, ResStudent::getStatus, status)
                        .orderByAsc(ResStudent::getStudentCode))
                .stream()
                .map(this::toStudentRow)
                .collect(Collectors.toList());
        writeExcel(response, "学生数据导出.xlsx", "学生数据", StudentExcelRow.class, rows);
    }

    @Override
    public void exportCourses(String keyword, Integer status, HttpServletResponse response) throws IOException {
        List<CourseInfoExcelRow> rows = courseInfoService.list(new LambdaQueryWrapper<ResCourse>()
                        .eq(ResCourse::getDeleted, 0)
                        .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                                .like(ResCourse::getCourseName, keyword)
                                .or()
                                .like(ResCourse::getCourseCode, keyword))
                        .eq(status != null, ResCourse::getStatus, status)
                        .orderByAsc(ResCourse::getCourseCode))
                .stream()
                .map(this::toCourseRow)
                .collect(Collectors.toList());
        writeExcel(response, "课程数据导出.xlsx", "课程数据", CourseInfoExcelRow.class, rows);
    }

    @Override
    public void exportTeachbuilds(String keyword, HttpServletResponse response) throws IOException {
        List<TeachbuildExcelRow> rows = teachbuildInfoService.list(new LambdaQueryWrapper<ResBuilding>()
                        .eq(ResBuilding::getDeleted, 0)
                        .like(StringUtils.isNotBlank(keyword), ResBuilding::getBuildingName, keyword)
                        .or(StringUtils.isNotBlank(keyword))
                        .like(StringUtils.isNotBlank(keyword), ResBuilding::getBuildingCode, keyword)
                        .orderByAsc(ResBuilding::getBuildingCode))
                .stream()
                .map(this::toTeachbuildRow)
                .collect(Collectors.toList());
        writeExcel(response, "教学楼数据导出.xlsx", "教学楼数据", TeachbuildExcelRow.class, rows);
    }

    @Override
    public void exportClassrooms(String keyword, String teachbuildNo, HttpServletResponse response) throws IOException {
        List<ClassroomExcelRow> rows = classroomService.list(new LambdaQueryWrapper<ResClassroom>()
                        .eq(ResClassroom::getDeleted, 0)
                        .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                                .like(ResClassroom::getClassroomCode, keyword)
                                .or()
                                .like(ResClassroom::getClassroomName, keyword))
                        .orderByAsc(ResClassroom::getClassroomCode))
                .stream()
                .filter(item -> {
                    if (StringUtils.isBlank(teachbuildNo)) {
                        return true;
                    }
                    ResBuilding building = teachbuildInfoService.getById(item.getBuildingId());
                    return building != null && StringUtils.equals(building.getBuildingCode(), teachbuildNo);
                })
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
            ResTeacher teacher = teacherService.getOne(new LambdaQueryWrapper<ResTeacher>()
                    .eq(ResTeacher::getTeacherCode, trim(row.getTeacherNo()))
                    .eq(ResTeacher::getDeleted, 0));
            if (teacher == null) {
                teacher = new ResTeacher();
                teacher.setTeacherCode(trim(row.getTeacherNo()));
            }
            teacher.setTeacherName(trim(row.getRealname()));
            teacher.setTitleName(trim(row.getJobtitle()));
            teacher.setRemark(trim(row.getTeach()));
            teacher.setMobile(trim(row.getTelephone()));
            teacher.setEmail(trim(row.getEmail()));
            teacher.setStatus(parseResourceStatus(trim(row.getStatusText()), 1));
            teacher.setHireStatus("ACTIVE");
            teacher.setMaxWeekHours(16);
            teacher.setMaxDayHours(4);
            teacherService.saveOrUpdate(teacher);
            resourceAccountSyncService.syncTeacherAccount(teacher);
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
            ResStudent student = studentService.getOne(new LambdaQueryWrapper<ResStudent>()
                    .eq(ResStudent::getStudentCode, trim(row.getStudentNo()))
                    .eq(ResStudent::getDeleted, 0));
            if (student == null) {
                student = new ResStudent();
                student.setStudentCode(trim(row.getStudentNo()));
            }
            student.setStudentName(trim(row.getRealname()));
            student.setRemark(StringUtils.defaultIfBlank(trim(row.getAddress()), trim(row.getClassNo())));
            student.setEntryYear(parseEntryYear(row.getGrade(), row.getStudentNo()));
            student.setMobile(trim(row.getTelephone()));
            student.setEmail(trim(row.getEmail()));
            student.setStatus(parseResourceStatus(trim(row.getStatusText()), 1));
            student.setStageId(defaultStageId());
            studentService.saveOrUpdate(student);
            resourceAccountSyncService.syncStudentAccount(student);
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
            ResCourse courseInfo = courseInfoService.getOne(new LambdaQueryWrapper<ResCourse>()
                    .eq(ResCourse::getCourseCode, trim(row.getCourseNo()))
                    .eq(ResCourse::getDeleted, 0));
            if (courseInfo == null) {
                courseInfo = new ResCourse();
                courseInfo.setCourseCode(trim(row.getCourseNo()));
            }
            courseInfo.setCourseName(trim(row.getCourseName()));
            courseInfo.setCourseType(StringUtils.defaultIfBlank(trim(row.getCourseAttr()), "REQUIRED"));
            courseInfo.setCourseShortName(trim(row.getPublisher()));
            courseInfo.setWeekHours(row.getPiority() == null ? 0 : row.getPiority());
            courseInfo.setTotalHours((row.getPiority() == null ? 0 : row.getPiority()) * 16);
            courseInfo.setNeedSpecialRoom(0);
            courseInfo.setRoomType("NORMAL");
            courseInfo.setStatus(parseResourceStatus(trim(row.getStatusText()), 1));
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
            ResBuilding teachbuildInfo = teachbuildInfoService.getOne(new LambdaQueryWrapper<ResBuilding>()
                    .eq(ResBuilding::getBuildingCode, trim(row.getTeachBuildNo()))
                    .eq(ResBuilding::getDeleted, 0));
            if (teachbuildInfo == null) {
                teachbuildInfo = new ResBuilding();
                teachbuildInfo.setBuildingCode(trim(row.getTeachBuildNo()));
            }
            teachbuildInfo.setBuildingName(trim(row.getTeachBuildName()));
            teachbuildInfo.setRemark(trim(row.getTeachBuildLocation()));
            teachbuildInfo.setBuildingType("TEACHING");
            teachbuildInfo.setCampusId(defaultCampusId());
            teachbuildInfo.setStatus(1);
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
            ResBuilding building = teachbuildInfoService.getOne(new LambdaQueryWrapper<ResBuilding>()
                    .eq(ResBuilding::getBuildingCode, trim(row.getTeachbuildNo()))
                    .eq(ResBuilding::getDeleted, 0)
                    .last("limit 1"));
            if (building == null) {
                errors.add("第 " + (index + 2) + " 行教学楼编号不存在，请先导入教学楼");
                continue;
            }
            ResClassroom classroom = classroomService.getOne(new LambdaQueryWrapper<ResClassroom>()
                    .eq(ResClassroom::getClassroomCode, trim(row.getClassroomNo()))
                    .eq(ResClassroom::getDeleted, 0));
            if (classroom == null) {
                classroom = new ResClassroom();
                classroom.setClassroomCode(trim(row.getClassroomNo()));
            }
            classroom.setClassroomName(trim(row.getClassroomName()));
            classroom.setBuildingId(building.getId());
            classroom.setCampusId(building.getCampusId());
            classroom.setCollegeId(building.getCollegeId());
            classroom.setSeatCount(row.getCapacity() == null ? 0 : row.getCapacity());
            classroom.setRoomType(StringUtils.defaultIfBlank(trim(row.getAttr()), "NORMAL"));
            classroom.setStatus(1);
            classroom.setIsShared(1);
            classroom.setRemark(trim(row.getRemark()));
            classroomService.saveOrUpdate(classroom);
            imported++;
        }
        return buildImportResponse(rows.size(), errors, imported, "教室");
    }

    private TeacherExcelRow toTeacherRow(ResTeacher teacher) {
        TeacherExcelRow row = new TeacherExcelRow();
        row.setTeacherNo(teacher.getTeacherCode());
        row.setUsername(teacher.getTeacherCode());
        row.setRealname(teacher.getTeacherName());
        row.setJobtitle(teacher.getTitleName());
        row.setTeach(teacher.getRemark());
        row.setAge(null);
        row.setTelephone(teacher.getMobile());
        row.setEmail(teacher.getEmail());
        row.setAddress("");
        row.setStatusText(teacher.getStatus() != null && teacher.getStatus() == 1 ? "启用" : "停用");
        return row;
    }

    private StudentExcelRow toStudentRow(ResStudent student) {
        StudentExcelRow row = new StudentExcelRow();
        row.setStudentNo(student.getStudentCode());
        row.setUsername(student.getStudentCode());
        row.setRealname(student.getStudentName());
        row.setGrade(student.getEntryYear() == null ? "" : student.getEntryYear() + "级");
        row.setClassNo(student.getRemark());
        row.setAge(null);
        row.setTelephone(student.getMobile());
        row.setEmail(student.getEmail());
        row.setAddress(student.getRemark());
        row.setStatusText(student.getStatus() != null && student.getStatus() == 1 ? "启用" : "停用");
        return row;
    }

    private CourseInfoExcelRow toCourseRow(ResCourse course) {
        CourseInfoExcelRow row = new CourseInfoExcelRow();
        row.setCourseNo(course.getCourseCode());
        row.setCourseName(course.getCourseName());
        row.setCourseAttr(course.getCourseType());
        row.setPublisher(course.getCourseShortName());
        row.setPiority(course.getWeekHours());
        row.setStatusText(course.getStatus() != null && course.getStatus() == 1 ? "启用" : "停用");
        row.setRemark(course.getRemark());
        return row;
    }

    private TeachbuildExcelRow toTeachbuildRow(ResBuilding teachbuildInfo) {
        TeachbuildExcelRow row = new TeachbuildExcelRow();
        row.setTeachBuildNo(teachbuildInfo.getBuildingCode());
        row.setTeachBuildName(teachbuildInfo.getBuildingName());
        row.setTeachBuildLocation(teachbuildInfo.getRemark());
        return row;
    }

    private ClassroomExcelRow toClassroomRow(ResClassroom classroom) {
        ClassroomExcelRow row = new ClassroomExcelRow();
        row.setClassroomNo(classroom.getClassroomCode());
        row.setClassroomName(classroom.getClassroomName());
        ResBuilding building = teachbuildInfoService.getById(classroom.getBuildingId());
        row.setTeachbuildNo(building == null ? "" : building.getBuildingCode());
        row.setCapacity(classroom.getSeatCount());
        row.setAttr(classroom.getRoomType());
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

    private Integer parseResourceStatus(String statusText, int defaultValue) {
        if (StringUtils.isBlank(statusText)) {
            return defaultValue;
        }
        String normalized = statusText.trim();
        if ("正常".equals(normalized) || "启用".equals(normalized) || "1".equals(normalized)) {
            return 1;
        }
        if ("封禁".equals(normalized) || "停用".equals(normalized) || "0".equals(normalized)) {
            return 0;
        }
        return defaultValue;
    }

    private Long defaultCampusId() {
        return 0L;
    }

    private Long defaultStageId() {
        return 0L;
    }

    private Integer parseEntryYear(String gradeText, String studentCode) {
        String gradeNumber = StringUtils.defaultIfBlank(trim(gradeText), "").replaceAll("[^0-9]", "");
        if (gradeNumber.length() >= 4) {
            return Integer.parseInt(gradeNumber.substring(0, 4));
        }
        String studentCodeNumber = StringUtils.defaultIfBlank(trim(studentCode), "").replaceAll("[^0-9]", "");
        if (studentCodeNumber.length() >= 4) {
            return Integer.parseInt(studentCodeNumber.substring(0, 4));
        }
        return java.time.LocalDate.now().getYear();
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
