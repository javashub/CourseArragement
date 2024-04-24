package com.lyk.coursearrange.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.ClassTaskDao;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: 15760
 * @Date: 2020/5/13
 * @Descripe:
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Resource
    private ClassTaskService classTaskService;
    @Resource
    private ClassTaskDao classTaskDao;

    /**
     * 文件上传实现并解析Excel存入数据库
     */
    @Override
    public ServerResponse upload(MultipartFile file) {

        ImportParams params = new ImportParams();
        params.setTitleRows(1);
        params.setHeadRows(1);

        List<ClassTask> list = null;
        try {
            // list就是Excel文件中每一行的记录
            list = ExcelImportUtil.importExcel(
                    file.getInputStream(),
                    ClassTask.class, params);
        } catch (Exception e) {
            log.error("导入课程任务失败: {}", e.getMessage());
        }
        // 调用课程任务存入数据库方法
        assert list != null;
        return save(list) ? ServerResponse.ofSuccess("导入课程任务成功") : ServerResponse.ofError("导入课程任务失败");
    }

    /**
     * 将文件中的数据插入数据库
     */
    private boolean save(List<ClassTask> list) {
        // 清空旧任务
        clearClassTaskOld();
        int i = 0;
        // 遍历课程任务插入数据库
        for (ClassTask classTask : list) {
            ClassTask c = new ClassTask();
            BeanUtils.copyProperties(classTask, c);
            boolean b = classTaskService.save(c);
            if (b) {
                i+=1;
            }
        }
        return i == list.size();
    }

    /**
     * 清空旧的课程任务
     */
    private void clearClassTaskOld() {
        classTaskDao.clearClassTaskOld();
    }

}
