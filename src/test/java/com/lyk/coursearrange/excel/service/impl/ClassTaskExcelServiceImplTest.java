package com.lyk.coursearrange.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.vo.ImportResultVO;
import com.lyk.coursearrange.excel.model.ClassTaskExcelRow;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassTaskExcelServiceImplTest {

    @Mock
    private SchTaskService schTaskService;

    @Test
    void importClassTasks_shouldReplaceStandardTasksOnly() throws IOException {
        ClassTaskExcelServiceImpl service = new ClassTaskExcelServiceImpl(schTaskService);
        MultipartFile file = buildExcelFile(buildRow());

        when(schTaskService.remove(any())).thenReturn(true);
        when(schTaskService.saveBatch(any())).thenReturn(true);

        ServerResponse response = service.importClassTasks(file);

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("课程任务导入成功，共 1 条"));
        assertInstanceOf(ImportResultVO.class, response.getData());
        ImportResultVO result = (ImportResultVO) response.getData();
        assertEquals(1, result.getSuccessCount());

        ArgumentCaptor<List<SchTask>> taskCaptor = ArgumentCaptor.forClass(List.class);
        verify(schTaskService).saveBatch(taskCaptor.capture());
        List<SchTask> savedTasks = taskCaptor.getValue();
        assertEquals(1, savedTasks.size());
        SchTask task = savedTasks.get(0);
        assertEquals("2501_10001_T2026001_2025_2026_1", task.getTaskCode());
        assertEquals("EXCEL_IMPORT", task.getSourceType());
        assertEquals(Integer.valueOf(45), task.getStudentCount());
        assertEquals(Integer.valueOf(4), task.getWeekHours());
        assertEquals(Integer.valueOf(64), task.getTotalHours());
        assertEquals("semester=2025-2026-1,classNo=2501,courseNo=10001,teacherNo=T2026001,gradeNo=2025,courseName=高等数学,courseAttr=必修,teacherName=张老师",
                task.getRemark());
    }

    private ClassTaskExcelRow buildRow() {
        ClassTaskExcelRow row = new ClassTaskExcelRow();
        row.setSemester("2025-2026-1");
        row.setGradeNo("2025");
        row.setClassNo("2501");
        row.setCourseNo("10001");
        row.setCourseName("高等数学");
        row.setTeacherNo("T2026001");
        row.setTeacherName("张老师");
        row.setCourseAttr("必修");
        row.setStudentNum(45);
        row.setWeeksNumber(4);
        row.setWeeksSum(16);
        row.setIsFix("0");
        row.setClassTime("");
        return row;
    }

    private MultipartFile buildExcelFile(ClassTaskExcelRow row) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, ClassTaskExcelRow.class)
                .sheet("课程任务")
                .doWrite(List.of(row));
        return new MockMultipartFile(
                "file",
                "class-task.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray());
    }
}
