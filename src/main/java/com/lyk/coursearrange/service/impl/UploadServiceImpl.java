package com.lyk.coursearrange.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.ClassTaskDao;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * @author: 15760
 * @Date: 2020/5/13
 * @Descripe:
 */
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private ClassTaskService classTaskService;
    @Autowired
    private ClassTaskDao classTaskDao;

    /**
     * 文件上传实现并解析Excel存入数据库
     *
     * @param file
     * @return
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
            e.printStackTrace();
        }
        // 调用课程任务存入数据库方法
        boolean b = save(list);
        if (b) {
            return ServerResponse.ofSuccess("导入课程任务成功");
        }
        return ServerResponse.ofError("导入课程任务失败");
    }

    /**
     * 将文件中的数据插入数据库
     * @param list
     * @return
     */
    private boolean save(List<ClassTask> list) {
        // 清空旧任务
        clearClassTaskOld();
        int i = 0;
        // 遍历课程任务插入数据库
        for (ClassTask classTask : list) {
            ClassTask c = new ClassTask();
            c.setSemester(classTask.getSemester());
            c.setGradeNo(classTask.getGradeNo());
            c.setClassNo(classTask.getClassNo());
            c.setCourseNo(classTask.getCourseNo());
            c.setCourseName(classTask.getCourseName());
            c.setTeacherNo(classTask.getTeacherNo());
            c.setRealname(classTask.getRealname());
            c.setCourseAttr(classTask.getCourseAttr());
            c.setStudentNum(classTask.getStudentNum());
            c.setWeeksSum(classTask.getWeeksSum());
            c.setWeeksNumber(classTask.getWeeksNumber());
            c.setIsFix(classTask.getIsFix());
            c.setClassTime(classTask.getClassTime());
            boolean b = classTaskService.save(c);
            if (b) {
                i = i + 1;
            }
        }
        if (i == list.size()) {
            return true;
        }
        return false;
    }

    /**
     * 清空旧的课程任务
     */
    private void clearClassTaskOld() {
        classTaskDao.clearClassTaskOld();
    }

}
