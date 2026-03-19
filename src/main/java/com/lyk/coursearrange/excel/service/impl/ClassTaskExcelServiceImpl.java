package com.lyk.coursearrange.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.vo.ImportResultVO;
import com.lyk.coursearrange.excel.model.ClassTaskExcelRow;
import com.lyk.coursearrange.excel.service.ClassTaskExcelService;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 课程任务 Excel 服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassTaskExcelServiceImpl implements ClassTaskExcelService {

    private static final String TEMPLATE_FILE_NAME = "课程任务导入模板.xlsx";

    private final SchTaskService schTaskService;

    @Override
    public void writeTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition",
                "attachment;filename*=utf-8''" + URLEncoder.encode(TEMPLATE_FILE_NAME, StandardCharsets.UTF_8.name()));
        EasyExcel.write(response.getOutputStream(), ClassTaskExcelRow.class)
                .sheet("课程任务模板")
                .doWrite(buildTemplateRows());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse importClassTasks(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ServerResponse.ofError("请选择要导入的 Excel 文件");
        }
        List<ClassTaskExcelRow> rows;
        try {
            rows = EasyExcel.read(file.getInputStream())
                    .head(ClassTaskExcelRow.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception exception) {
            log.error("读取课程任务 Excel 失败", exception);
            return ServerResponse.ofError("读取 Excel 失败，请检查模板格式是否正确");
        }
        if (rows == null || rows.isEmpty()) {
            return ServerResponse.ofError("Excel 中没有可导入的课程任务数据");
        }

        List<String> errors = new ArrayList<>();
        List<SchTask> standardTasks = new ArrayList<>();
        Set<String> semesters = new LinkedHashSet<>();
        for (int index = 0; index < rows.size(); index++) {
            ClassTaskExcelRow row = rows.get(index);
            int errorCountBefore = errors.size();
            validateRow(row, index + 2, errors);
            if (errors.size() > errorCountBefore) {
                continue;
            }
            SchTask standardTask = convertRow(row);
            standardTasks.add(standardTask);
            semesters.add(trim(row.getSemester()));
        }
        if (!errors.isEmpty()) {
            return ServerResponse.ofError("课程任务导入失败，请修正后重试", buildImportResult(rows.size(), standardTasks.size(), errors));
        }

        replaceStandardTasks(semesters, standardTasks);
        return ServerResponse.ofSuccess(
                String.format("课程任务导入成功，共 %s 条，已覆盖学期：%s", standardTasks.size(), String.join("、", semesters)),
                buildImportResult(rows.size(), standardTasks.size(), List.of())
        );
    }

    private void replaceStandardTasks(Set<String> semesters, List<SchTask> standardTasks) {
        LambdaQueryWrapper<SchTask> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(SchTask::getDeleted, 0)
                .and(wrapper -> {
                    boolean first = true;
                    for (String semester : semesters) {
                        if (semester == null || semester.isBlank()) {
                            continue;
                        }
                        if (first) {
                            wrapper.like(SchTask::getRemark, "semester=" + semester);
                            first = false;
                        } else {
                            wrapper.or().like(SchTask::getRemark, "semester=" + semester);
                        }
                    }
                });
        if (!schTaskService.remove(removeWrapper)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "课程任务导入失败，清理原有标准任务失败");
        }
        if (!schTaskService.saveBatch(standardTasks)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "课程任务导入失败，保存标准任务失败");
        }
    }

    private List<ClassTaskExcelRow> buildTemplateRows() {
        List<ClassTaskExcelRow> rows = new ArrayList<>();
        ClassTaskExcelRow sample = new ClassTaskExcelRow();
        sample.setSemester("2025-2026-1");
        sample.setGradeNo("2025");
        sample.setClassNo("2501");
        sample.setCourseNo("10001");
        sample.setCourseName("高等数学");
        sample.setTeacherNo("T2026001");
        sample.setRealname("张老师");
        sample.setCourseAttr("必修");
        sample.setStudentNum(45);
        sample.setWeeksNumber(4);
        sample.setWeeksSum(16);
        sample.setIsFix("0");
        sample.setClassTime("");
        rows.add(sample);
        return rows;
    }

    private void validateRow(ClassTaskExcelRow row, int excelRowNum, List<String> errors) {
        String semester = trim(row.getSemester());
        String classNo = trim(row.getClassNo());
        String courseNo = trim(row.getCourseNo());
        String courseName = trim(row.getCourseName());
        String teacherNo = trim(row.getTeacherNo());
        String realname = trim(row.getRealname());
        String isFix = StringUtils.defaultIfBlank(trim(row.getIsFix()), "0");
        String classTime = trim(row.getClassTime());

        if (StringUtils.isBlank(semester)) {
            errors.add("第 " + excelRowNum + " 行缺少学期");
        }
        if (StringUtils.isBlank(classNo)) {
            errors.add("第 " + excelRowNum + " 行缺少班级编号");
        }
        if (StringUtils.isBlank(courseNo) || StringUtils.isBlank(courseName)) {
            errors.add("第 " + excelRowNum + " 行课程编号和课程名称必须同时填写");
        }
        if (StringUtils.isBlank(teacherNo) || StringUtils.isBlank(realname)) {
            errors.add("第 " + excelRowNum + " 行教师编号和教师姓名必须同时填写");
        }
        if (row.getWeeksNumber() == null || row.getWeeksNumber() < 1) {
            errors.add("第 " + excelRowNum + " 行周学时必须大于 0");
        }
        if (row.getWeeksSum() == null || row.getWeeksSum() < 1) {
            errors.add("第 " + excelRowNum + " 行周数必须大于 0");
        }
        if (!"0".equals(isFix) && !"1".equals(isFix)) {
            errors.add("第 " + excelRowNum + " 行是否固定排课只能填写 0 或 1");
        }
        if ("1".equals(isFix)) {
            if (StringUtils.isBlank(classTime)) {
                errors.add("第 " + excelRowNum + " 行固定排课时必须填写固定时间编码");
            } else if (!classTime.matches("^\\d{2}$")) {
                errors.add("第 " + excelRowNum + " 行固定时间编码格式错误，应为两位数字");
            }
        }
    }

    private SchTask convertRow(ClassTaskExcelRow row) {
        String semester = trim(row.getSemester());
        String classNo = trim(row.getClassNo());
        String courseNo = trim(row.getCourseNo());
        String teacherNo = trim(row.getTeacherNo());
        String gradeNo = trim(row.getGradeNo());
        String courseName = trim(row.getCourseName());
        String teacherName = trim(row.getRealname());
        String courseAttr = trim(row.getCourseAttr());
        String isFix = StringUtils.defaultIfBlank(trim(row.getIsFix()), "0");
        String classTime = "1".equals(isFix) ? trim(row.getClassTime()) : "";

        SchTask task = new SchTask();
        task.setTaskCode(ScheduleTaskMetaUtils.buildTaskCode(semester, classNo, courseNo, teacherNo));
        task.setSchoolYearId(0L);
        task.setTermId(0L);
        task.setStageId(0L);
        task.setCourseId(0L);
        task.setTeacherId(0L);
        task.setStudentCount(row.getStudentNum() == null ? 0 : row.getStudentNum());
        task.setWeekHours(row.getWeeksNumber() == null ? 0 : row.getWeeksNumber());
        task.setTotalHours((row.getWeeksNumber() == null || row.getWeeksSum() == null) ? 0 : row.getWeeksNumber() * row.getWeeksSum());
        task.setNeedContinuous(0);
        task.setContinuousSize(1);
        task.setNeedFixedRoom(0);
        task.setNeedFixedTime("1".equals(isFix) ? 1 : 0);
        task.setFixedWeekdayNo(ScheduleTaskMetaUtils.resolveWeekdayNo(classTime));
        task.setFixedPeriodNo(ScheduleTaskMetaUtils.resolvePeriodNo(classTime));
        task.setPriorityLevel(5);
        task.setAllowConflict(0);
        task.setTaskStatus("PENDING");
        task.setSourceType("EXCEL_IMPORT");
        task.setStatus(1);
        task.setRemark("semester=" + ScheduleTaskMetaUtils.safe(semester)
                + ",classNo=" + ScheduleTaskMetaUtils.safe(classNo)
                + ",courseNo=" + ScheduleTaskMetaUtils.safe(courseNo)
                + ",teacherNo=" + ScheduleTaskMetaUtils.safe(teacherNo)
                + ",gradeNo=" + ScheduleTaskMetaUtils.safe(gradeNo)
                + ",courseName=" + ScheduleTaskMetaUtils.safe(courseName)
                + ",courseAttr=" + ScheduleTaskMetaUtils.safe(courseAttr)
                + ",teacherName=" + ScheduleTaskMetaUtils.safe(teacherName));
        return task;
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
}
