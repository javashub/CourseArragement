package com.lyk.coursearrange.excel.service;

import com.lyk.coursearrange.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 课程任务 Excel 服务。
 */
public interface ClassTaskExcelService {

    void writeTemplate(HttpServletResponse response) throws IOException;

    ServerResponse importClassTasks(MultipartFile file);
}
