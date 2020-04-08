package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.dao.ClassTaskDao;
import com.lyk.coursearrange.service.ClassTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 * @author lequal
 * @since 2020-04-06
 */
@Service
public class ClassTaskServiceImpl extends ServiceImpl<ClassTaskDao, ClassTask> implements ClassTaskService {

    @Autowired
    private ClassTaskDao classTaskDao;



    // 不固定上课时间
    private final String UNFIXED_TIME = "unFixedTime";

    // 固定上课时间
    private final String IS_FIX_TIME = "isFixedTime";


    /**
     * 先对全校内已经分配用于上课的教学楼信息做一个查询(针对有特殊要求的教室)
     * @param classTask
     * @return
     */
    @Transactional
    @Override
    public Boolean classScheduling(ClassTask classTask) {
        try {
            // 1、获得开课任务，知道要上什么课，等下要排什么课
            QueryWrapper<ClassTask> wrapper = new QueryWrapper<ClassTask>().eq("semester", classTask.getSemester());
            List<ClassTask> classTaskList = classTaskDao.selectList(wrapper);
            // 2、将开课任务的各项信息进行编码成染色体，分为固定时间与不固定时间的课程集合
            List<Map<String, List<String>>>  geneList = coding(classTaskList);
            // 3、给初始基因编码随机分配时间
            List<String> resultGeneList = codingTime(geneList);
            // 4、将分配好时间的基因编码以班级分类成为以班级的个体
            Map<String, List<String>> individualMap = transformIndividual(resultGeneList);
            // 5、遗传进化
            individualMap = geneticEvolution(individualMap);
            // 6、给新个体分配教室，同时要进行冲突检测
            List<String> resultList = finalResult(individualMap);
            // 7、解码最终的染色体获取其中的基因信息
            List<String> classPlan = decoding(resultList);
            // 8、写入tb_classplan数据表中供前端查询课程表使用

            // 9、
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private List decoding(List<String> resultList) {
        return null;
    }

    private List<String> finalResult(Map<String, List<String>> individualMap) {
        return null;
    }

    private Map<String, List<String>> geneticEvolution(Map<String, List<String>> individualMap) {
        return null;
    }

    private Map<String, List<String>> transformIndividual(List<String> resultGeneList) {
        return null;
    }

    private List<String> codingTime(List<Map<String, List<String>>> geneList) {

        return null;
    }

    /**
     * 编码:
     *  固定时间：1
     * 	年级编号：2
     * 	班级编号：8
     * 	讲师编号：5
     * 	课程编号：6
     * 	课程属性：2
     * 	上课时间：2
     * 	教室编号：1-6
     *
     * 将从表中查询的开课任务书对象集合进行编码，组成初始基因
     * 编码规则为：是否固定+班级编号+教师编号+课程编号+课程属性+开课时间
     * 其中如果不固定开课时间默认填充为"00"
     * 经过处理后得到开课任务中
     * @param classTaskList
     * @return List<String>
     */
    private List<Map<String, List<String>>> coding(List<ClassTask> classTaskList) {
        List<Map<String, List<String>>> geneList = new ArrayList<>();
        Map<String, List<String>> geneListMap = new HashMap<>();
        // 不固定时间
        List<String> unFixedTimeGeneList = new ArrayList<>();
        // 固定时间
        List<String> fixedTimeGeneLList = new ArrayList<>();

        for (ClassTask classTask : classTaskList) {
            // 根据isFixed判断是否固定上课时间，为1则不固定，填充00
            if (classTask.getIsFix().equals("1")) {

            }
        }

        return geneList;
    }
}
