package com.lyk.coursearrange.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.lyk.coursearrange.entity.ClassTask;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: 15760
 * @Date: 2020/5/16
 * @Descripe:
 */
@SpringBootTest
//@RunWith(SpringRunner.class)
public class ExcelTest {

    @Test
    public void test1() {
        ExportParams params = new ExportParams();
        params.setTitle("课程任务导入模板(请严格对照数据库信息填写)");
        params.setSheetName("课程任务模板");
        List<ClassTask> list = new ArrayList();
//        list.add(new ClassTask("2019-2020-1", "01", "20200103", "1000008", "高二数学", "10001", "梁老师", "01", 39, 4, 12, "2", ""));
//        File savefile = new File("D:/excel/");
//        if (!savefile.exists()) {
//            savefile.mkdirs();
//        }
        Workbook workbook = ExcelExportUtil.exportExcel(params, ClassTask.class, list);
        try {
            FileOutputStream fos = new FileOutputStream("D:/excel/课程任务导入模板.xls");
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
