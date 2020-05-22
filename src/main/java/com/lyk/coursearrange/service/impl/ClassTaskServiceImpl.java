package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.dao.*;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.request.ConstantInfo;
import com.lyk.coursearrange.service.ClassTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lequal
 * @since 2020-04-06
 */
@Service
public class ClassTaskServiceImpl extends ServiceImpl<ClassTaskDao, ClassTask> implements ClassTaskService {

    Logger log = LoggerFactory.getLogger(ClassTaskServiceImpl.class);

    @Autowired
    private ClassTaskDao classTaskDao;
    @Autowired
    private TeachBuildInfoDao teachBuildInfoDao;
    @Autowired
    private ClassroomDao classroomDao;
    @Autowired
    private ClassInfoDao classInfoDao;
    @Autowired
    private CoursePlanDao coursePlanDao;


    // 不固定上课时间
    private final String UNFIXED_TIME = "unFixedTime";

    // 固定上课时间
    private final String IS_FIX_TIME = "isFixedTime";


    /**
     * 先对全校内已经分配用于上课的教学楼信息做一个查询(针对有特殊要求的教室)
     * @param
     * @return
     */
    @Transactional
    @Override
    public Boolean classScheduling(String semester) {
        try {
            log.debug("开始排课,时间：" + System.currentTimeMillis());
            // 1、获得开课任务，知道要上什么课，等下要排什么课
//            List<ClassTask> classTaskList = classTaskDao.selectBySemester(classTask);
            QueryWrapper<ClassTask> wrapper = new QueryWrapper<ClassTask>().eq("semester", semester);
            List<ClassTask> classTaskList = classTaskDao.selectList(wrapper);

            // 2、将开课任务的各项信息进行编码成染色体，分为固定时间与不固定时间的课程集合
            List<Map<String, List<String>>>  geneList = coding(classTaskList);
            // 3、给初始基因编码随机分配时间
            List<String> resultGeneList = codingTime(geneList);
            // 4、将分配好时间的基因编码以班级分类成为以班级的个体
            Map<String, List<String>> individualMap = transformIndividual(resultGeneList);
            // 5、遗传进化(核心算法)
            individualMap = geneticEvolution(individualMap);
            // 6、给新个体分配教室，同时要进行冲突检测
            List<String> resultList = finalResult(individualMap);
            // 7、解码最终的染色体获取其中的基因信息
            List<CoursePlan> coursePlanList = decoding(resultList);
            // 8、写入tb_course_plan上课计划表
            coursePlanDao.deleteAllPlan(); // 先删除原来的课程计划
            for (CoursePlan coursePlan : coursePlanList) {
                coursePlanDao.insertCoursePlan(coursePlan.getGradeNo(), coursePlan.getClassNo(), coursePlan.getCourseNo(),
                        coursePlan.getTeacherNo(), coursePlan.getClassroomNo(), coursePlan.getClassTime(), semester);
            }
            return true;
        } catch (Exception e) {
            log.error("the error message is:" + "    " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 开始给进化完的基因编码分配教室，即在原来的编码中加上教室编号
     * @param individualMap
     * @return
     */
    private List<String> finalResult(Map<String, List<String>> individualMap) {
        // 存放编上教室编号的完整基因编码
        List<String> resultList = new ArrayList<>();
        // 将map集合中的基因编码再次全部混合
        List<String> resultGeneList = collectGene(individualMap);
        String classroomNo = "";
        // 得到课程任务的年级列表
        List<String> gradeList = classTaskDao.selectByColumnName(ConstantInfo.GRADE_NO);
//        List<String> gradeList = classTaskDao.selectByGradeNo();
        // 将基因编码按照年级分配
        Map<String, List<String>> gradeMap = collectGeneByGrade(resultGeneList, gradeList);
        // 这里需要根据安排教学区域时选的教学楼进行安排课程
        for (String gradeNo : gradeMap.keySet()) {
            // 找到当前年级对应的教学楼编号
            String teachBuildNo = teachBuildInfoDao.selectBuildNo(gradeNo);
            // 得到不同年级的课程基因编码
            List<String> gradeGeneList = gradeMap.get(gradeNo);
            // 看看对应教学楼下有多少教室，在设定的教学楼下开始随机分配教室
            List<Classroom> classroomList = classroomDao.selectByTeachbuildNo(teachBuildNo);
            for (String gene : gradeGeneList) {
                classroomNo = issueClassroom(gene, classroomList, resultList);
                // 基因编码中加入教室编号，至此所有基因信息编码完成，得到染色体
                gene = gene + classroomNo;
                // 将最终的编码加入集合中
                resultList.add(gene);
            }
        }
        // 完整的基因编码
        return resultList;
    }

    /**
     * 给不同的基因编码分配教室
     * @param gene 需要分配教室的基因编码
     * @param classroomList 教室
     * @param resultList
     * @return
     */
    private String issueClassroom(String gene, List<Classroom> classroomList, List<String> resultList) {
        // 处理特殊课程，实验课，体育课
        // 体育课
        List<Classroom> sportBuilding = classroomDao.selectByTeachbuildNo("12");
        // 实验课
        List<Classroom> experimentBuilding = classroomDao.selectByTeachbuildNo("08");
        // 获得班级编号
        String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
        // 得到该班级的学生人数
        int studentNum = classInfoDao.selectStuNum(classNo);
        // 得到课程属性
        String courseAttr = ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene);

        if (courseAttr.equals(ConstantInfo.EXPERIMENT_COURSE)) {
            // 03为实验课
            return chooseClassroom(studentNum, gene, experimentBuilding, resultList);
        } else if (courseAttr.equals(ConstantInfo.PHYSICAL_COURSE)) {
            // 04为体育课
            return chooseClassroom(studentNum, gene, sportBuilding, resultList);
        } else {
            // 剩下主要课程、次要课程都放在普通的教室
            // 如果还有其他课程另外加判断课程属性，暂时设定4种：主要，次要，实验，体育。音乐舞蹈那些不算
            return chooseClassroom(studentNum, gene, classroomList, resultList);
        }
    }

    /**
     * 给不同课程的基因编码选择相应的教室
     * @param studentNum 开课的班级的学生人数
     * @param gene 需要安排教室的基因编码
     * @param classroomList 教室
     * @param resultList
     * @return
     */
    private String chooseClassroom(int studentNum, String gene, List<Classroom> classroomList, List<String> resultList) {
        // 使用随机分配教室的方式，只要可以放下所有学生即可满足条件
        int min = 0;
        int max = classroomList.size() - 1;
//        Random random = new Random();
        // 用于随机选取教室
        int temp = min + (int)(Math.random() * (max + 1 - min));
        // 随机教室
//        int temp = random.nextInt(max) % (max - min + 1) + min;
        Classroom classroom = classroomList.get(temp);
        // 判断是否满足条件
        if (judgeClassroom(studentNum, gene, classroom, resultList)) {
            // 该教室满足条件
            return classroom.getClassroomNo();
        } else {
            // 不满足，继续找教室
            return chooseClassroom(studentNum, gene, classroomList, resultList);
        }
    }

    /**
     * 判断教室是否符合上课班级所需
     * 即：不同属性的课要放在对应属性的教室上课
     * @param studentNum
     * @param gene
     * @param classroom
     * @param resultList
     * @return
     */
    private Boolean judgeClassroom(int studentNum, String gene, Classroom classroom, List<String> resultList) {
        String courseAttr = ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene);
        // 只要是语数英物化生政史地这些课程都是放在普通教室上课
        if (courseAttr.equals(ConstantInfo.MAIN_COURSE) || courseAttr.equals(ConstantInfo.SECONDARY_COURSE)) {
            // 找到普通教室，普通教室的属性都是01
            if (classroom.getAttr().equals("01")) {
                // 判断上课人数与教室容量
                if (classroom.getCapacity() >= studentNum) {
                    // 还要判断该教室是否在同一时间有别的班级使用了
                    return isFree(gene, resultList, classroom);
                } else {
                    // 教室容量不够
                    return false;
                }
            } else {
                return false;
            }
        } else {
            // 剩余的课程应该要放在相对应的教室上课
            if (ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene).equals(classroom.getAttr())) {
                // 判断人数
                if (classroom.getCapacity() >= studentNum) {
                    // 判断该教室上课时间是否重复
                    return isFree(gene, resultList, classroom);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * 判断同一时间同一个教室是否有多个班级使用
     * @param gene
     * @param resultList
     * @param classroom
     * @return
     */
    private Boolean isFree(String gene, List<String> resultList, Classroom classroom) {
        // 如果resultList为空说明还没有教室被分配,直接返回true
        if (resultList.size() == 0) {
            return true;
        } else {
            for (String resultGene : resultList) {
                // 如果当前教室在之前分配了则需要去判断时间是否有冲突
                if (ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, resultGene).equals(classroom.getClassroomNo())) {
                    // 判断时间是否一样，一样则表示有冲突
                    if (ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene).equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, resultGene))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 将所有的基因编码按照年级分类
     * @param resultGeneList
     * @param gradeList
     * @return
     */
    private Map<String, List<String>> collectGeneByGrade(List<String> resultGeneList, List<String> gradeList) {
        Map<String, List<String>> map = new HashMap<>();
        for (String gradeNo : gradeList) {
            List<String> geneList = new ArrayList<>();
            // 找到基因编码集合中相应的年级并归类
            for (String gene : resultGeneList) {
                if (ClassUtil.cutGene(ConstantInfo.GRADE_NO, gene).equals(gradeNo)) {
                    // 将当前的年级的基因编码加入集合
                    geneList.add(gene);
                }
            }
            // 将当前年级对应的编码集合放入集合
            if (geneList.size() > 0) {
                map.put(gradeNo, geneList);
            }
        }
        // 得到不同年级的基因编码(年级，编码集合)
        return map;
    }

    /**
     * 遗传进化(每个班级中多条基因编码)
     * 步骤：
     * 1、初始化种群
     * 2、交叉
     * 3、变异
     * 4、重复2,3步骤
     * 5、直到达到终止条件
     * @param individualMap
     * @return
     */
    private Map<String, List<String>> geneticEvolution(Map<String, List<String>> individualMap) {
        // 遗传代数
        int generation = ConstantInfo.GENERATION;
        List<String> resultGeneList;
        for (int i = 0; i < generation; ++i) {
            // 1、交叉
            individualMap = hybridization(individualMap);
            // 2、合拢个体准备进行变异
            collectGene(individualMap);
            // 2,3、合拢每个班级中的所有基因编码，然后进行变异操作
            resultGeneList = geneMutation(collectGene(individualMap));
            // 4、冲突检测，消除冲突
            conflictResolution(resultGeneList);
            // 5、将消除冲突后的个体再次分割，再分班进入下一次进化
            individualMap = transformIndividual(conflictResolution(resultGeneList));
        }
        return individualMap;
    }

    /**
     * 判断冲突：同时一时间一个教室上多门课
     * @return
     */
    private String positionConflict() {
        return "";
    }



    /**
     * 冲突消除,同一个讲师同一时间上多门课。解决：重新分配一个时间，直到所有的基因编码中
     * 不再存在上课时间冲突为止
     * 因素：讲师-教室-时间-课程
     * @param resultGeneList
     * @return
     */
    private List<String> conflictResolution(List<String> resultGeneList) {
        eitx:
        for (int i = 0; i < resultGeneList.size(); ++i) {
            // 得到集合中每一条基因编码的编码信息
            String gene = resultGeneList.get(i);
            String techerNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
            String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
            for (int j = i + 1; j < resultGeneList.size(); ++j) {
                // 再找剩余的基因编码对比
                String tempGene = resultGeneList.get(j);
                String tempTecherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, tempGene);
                String tempClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, tempGene);
                // 判断是否有一样的
                if (techerNo.equals(tempTecherNo) && classTime.equals(tempClassTime)) {
                    // 说明同一讲师同一时间有两门以上的课要上，冲突出现，重新给这门课找一个时间
                    String newClassTime = ClassUtil.randomTime(gene, resultGeneList);
                    gene = gene.substring(0, 24) + newClassTime;
                    continue  eitx;
                }
            }
        }
        return resultGeneList;
    }

    /**
     * 重新合拢交叉后的个体,即不分班级的基因编码
     * @param individualMap
     * @return
     */
    private List<String> collectGene(Map<String, List<String>> individualMap) {
        List<String> resultList = new ArrayList<>();
        for (List<String> individualList : individualMap.values()) {
            resultList.addAll(individualList);
        }
        return resultList;
    }

    /**
     * 基因变异
     * @param resultGeneList
     * @return
     */
    private List<String> geneMutation(List<String> resultGeneList) {
        int min = 0;
        int max = resultGeneList.size() - 1;
        // 变异率，需要合理设置，太低则不容易进化得到最优解；太高则容易失去种群原来的优秀解
        double mutationRate = 0.01;
        // 设定每一代中需要变异的基因个数，基因数*变异率
        int mutationNumber = (int)(resultGeneList.size() * mutationRate);
        if (mutationNumber < 1) {
            mutationNumber = 1;
        }
        for (int i = 0; i < mutationNumber; ) {
            // 生成一个随机数，以随机拿一条基因编码
            int temp = min + (int)(Math.random() * (max + 1 - min));
            String gene = resultGeneList.get(temp);
            if (ClassUtil.cutGene(ConstantInfo.IS_FIX, gene).equals("2")) {
                break;
            } else {
                // 再随机给它一个上课时间
                String newClassTime = ClassUtil.randomTime(gene, resultGeneList);
                gene = gene.substring(0, 24) + newClassTime;
                resultGeneList.remove(temp);
                resultGeneList.add(temp, gene);
                i = i + 1;
            }
        }
        return resultGeneList;
    }

    /**
     * 交叉：种群(每个班级的初始课表基因编码)
     * @param individualMap
     * @return
     */
    private Map<String, List<String>> hybridization(Map<String, List<String>> individualMap) {
        // 对每一个班级的基因编码片段进行交叉
        for (String classNo : individualMap.keySet()) {
            // 得到班级
            List<String> individualList = individualMap.get(classNo);
            // 保存上一代
            List<String> oldIndividualList = individualList;
            // 交叉生成新个体,得到新生代
            individualList = selectGene(individualList);
            // 对比子父代的适应度值，高的留下进行下一代遗传
            if (ClassUtil.calculatExpectedValue(individualList) >= ClassUtil.calculatExpectedValue(oldIndividualList)) {
                individualMap.put(classNo, individualList);
            } else {
                individualMap.put(classNo, oldIndividualList);
            }
        }
        return individualMap;
    }


    /**
     * 个体中随机选择基因进行交叉(交换上课时间)
     * @return
     */
    private List<String> selectGene(List<String> individualList) {
        int min = 0;
        int max = individualList.size() - 1;
        boolean flag;
        do {
            // 从班级集合中随机选取两个坐标以便获得随机的两条基因编码
            int firstIndex = min + (int)(Math.random() * (max + 1 - min));
            int secondIndex = min + (int)(Math.random() * (max + 1 - min));
            // 获取随机基因编码
            String firstGene = individualList.get(firstIndex);
            String secondGene = individualList.get(secondIndex);
            if (firstIndex == secondIndex) {
                flag = false;
            } else if(ClassUtil.cutGene(ConstantInfo.IS_FIX, firstGene).equals("2") || ClassUtil.cutGene(ConstantInfo.IS_FIX, secondGene).equals("2")) {
                // 上课时间已经固定，重新选择
                flag = false;
            } else {
                // 分别获取两条基因编码中的上课时间，开始交叉
                String firstClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, firstGene);
                String secondClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, secondGene);
                // 交换它们的上课时间
                firstGene = firstGene.substring(0, 24) + secondClassTime;
                secondGene = secondGene.substring(0, 24) + firstClassTime;
                // 将新得到的两条基因编码替换原来班级中的基因编码
                individualList.remove(firstIndex);
                individualList.add(firstIndex, firstGene);
                individualList.remove(secondIndex);
                individualList.add(secondIndex, secondGene);
                flag = true;
            }
        } while (!flag);
        return individualList;
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
     * 编码规则为：是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间(固定)
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
        List<String> fixedTimeGeneList = new ArrayList<>();

        for (ClassTask classTask : classTaskList) {
            // isFix为1则不固定上课时间，默认填充00
            if (classTask.getIsFix().equals("1")) {
                // 得到每周上课的次数，因为字段为每周学时，课程表设定中约定一节课2学时
                int size = classTask.getWeeksNumber() / 2;
                for (int i = 0; i < size; i++) {
                    // 编码:固定时间的课程
                    String gene = classTask.getIsFix() + classTask.getGradeNo() + classTask.getClassNo()
                            + classTask.getTeacherNo() + classTask.getCourseNo() + classTask.getCourseAttr() + ConstantInfo.DEFAULT_CLASS_TIME;
                    unFixedTimeGeneList.add(gene);
                }
            }

            // isFix为2则固定上课时间
            if (classTask.getIsFix().equals("2")) {
                int size = classTask.getWeeksNumber() / 2;
                for (int i = 0; i < size; i++) {
                    // 获得设定的固定时间
                    String classTime = classTask.getClassTime().substring(i * 2, (i + 1) * 2);
                    // 编码
                    String gene = classTask.getIsFix() + classTask.getGradeNo() + classTask.getClassNo()
                            + classTask.getTeacherNo() + classTask.getCourseNo() + classTask.getCourseAttr() + classTime;
                    fixedTimeGeneList.add(gene);
                }
            }
        }
        // 将两种上课时间的集合放入集合中
        geneListMap.put(UNFIXED_TIME, unFixedTimeGeneList);
        geneListMap.put(IS_FIX_TIME, fixedTimeGeneList);
        geneList.add(geneListMap);
        // 得到不含教室的初始基因编码
        return geneList;
    }

    /**
     * 给初始基因编码随机分配时间(那些不固定上课时间的课程)
     * @param geneList
     * @return
     */
    private List<String> codingTime(List<Map<String, List<String>>> geneList) {
        List<String> resultGeneList = new ArrayList<>();
        List<String> isFixedTimeGeneList = geneList.get(0).get(IS_FIX_TIME);
        List<String> unFixedTimeGeneList = geneList.get(0).get(UNFIXED_TIME);
        // 将固定上课时间的课程基因编码集合全部加入集合，用于等下判断后面分配上课时间的时候有没有跟现有固定时间的课程冲突
        resultGeneList.addAll(isFixedTimeGeneList);
        // 排之前没有固定时间的课程
        for (String gene : unFixedTimeGeneList) {
            // 获得一个随机时间
            String classTime = ClassUtil.randomTime(gene, resultGeneList);
            // 得到分配好随机上课时间的基因编码
            gene = gene.substring(0, 24) + classTime;
            resultGeneList.add(gene);
        }
        return resultGeneList;
    }

    /**
     * 将初始基因编码(都分配好时间)划分以班级为单位的个体
     * 班级编号的集合，去重
     * @param resultGeneList
     * @return
     */
    private Map<String, List<String>> transformIndividual(List<String> resultGeneList) {
        Map<String, List<String>> individualMap = new HashMap<>();
        // 查询开课的班级
        List<String> classNoList = classTaskDao.selectClassNo();
        for (String classNo : classNoList) {
            List<String> geneList = new ArrayList<>();
            for (String gene : resultGeneList) {
                // 切割查看基因编码中班级编号的信息
                if (classNo.equals(ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene))) {
                    // 把含有该班级编号的基因编码加入集合
                    geneList.add(gene);
                }
            }
            // 根据班级分配基因编码集合
            if (geneList.size() > 1) {
                individualMap.put(classNo, geneList);
            }
        }
        // 得到不同班级的初始课表(未进行冲突检测)
        return individualMap;
    }


    /**
     * 解码染色体中的基因，按照之前的编码解
     * 编码:
     *  固定时间：1
     * 	年级编号：2
     * 	班级编号：8
     * 	讲师编号：5
     * 	课程编号：6
     * 	课程属性：2
     * 	上课时间：2
     * 	教室编号：6
     * 编码规则为：是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间(固定) + 教室编号(最后才分配)
     * 其中如果不固定开课时间默认填充为"00"
     * @param resultList 全部上课计划实体
     * @return
     */
    private List<CoursePlan> decoding(List<String> resultList) {
        List<CoursePlan> coursePlanList = new ArrayList<>();
        for (String gene : resultList) {
            CoursePlan coursePlan = new CoursePlan();
            // 年级
            coursePlan.setGradeNo(ClassUtil.cutGene(ConstantInfo.GRADE_NO, gene));
            // 班级
            coursePlan.setClassNo(ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene));
            // 课程
            coursePlan.setCourseNo(ClassUtil.cutGene(ConstantInfo.COURSE_NO, gene));
            // 讲师
            coursePlan.setTeacherNo(ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene));
            // 教室
            coursePlan.setClassroomNo(ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, gene));
            // 上课时间
            coursePlan.setClassTime(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene));
            coursePlanList.add(coursePlan);
        }
        return coursePlanList;
    }
}
