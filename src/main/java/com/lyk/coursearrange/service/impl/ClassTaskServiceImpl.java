package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.exceptions.CourseArrangeException;
import com.lyk.coursearrange.dao.*;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.Classroom;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.common.ConstantInfo;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lequal
 * @since 2020-04-06
 * UPDATE tb_student
 * SET student_no = CONCAT('2024', SUBSTRING(student_no, 5))
 * WHERE student_no LIKE '2019%';
 */
@Service
@Slf4j
public class ClassTaskServiceImpl extends ServiceImpl<ClassTaskDao, ClassTask> implements ClassTaskService {

    @Resource
    private ClassTaskDao classTaskDao;
    @Resource
    private TeachBuildInfoDao teachBuildInfoDao;
    @Resource
    private ClassroomDao classroomDao;
    @Resource
    private ClassInfoDao classInfoDao;
    @Resource
    private CoursePlanDao coursePlanDao;

    /**
     * 排课算法入口
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ServerResponse classScheduling(String semester) {
        try {
            log.info("开始排课,时间：" + System.currentTimeMillis());
            long start = System.currentTimeMillis();
            // 1、获得开课任务 tb_class_task 表
            List<ClassTask> classTaskList = classTaskDao.selectList(new LambdaQueryWrapper<ClassTask>().eq(ClassTask::getSemester, semester));
            if (null == classTaskList || classTaskList.isEmpty()) {
                return ServerResponse.ofError("排课失败，查询不到排课任务！请导入排课任务再进行排课~");
            }

            // 校验学时是否超过课表的容纳值
            checkWeeksNumber(classTaskList);

            // 2、将开课任务的各项信息进行编码成染色体，分为固定时间与不固定时间
            Map<String, List<String>> geneMap = coding(classTaskList);
            // 3、给初始基因编码随机分配时间，得到同班上课时间不冲突的编码
            List<String> resultGeneList = codingTime(geneMap);
            // 4、将分配好时间的基因编码以班级分类成为以班级的个体，得到班级的不冲突时间初始编码
            Map<String, List<String>> individualMap = transformIndividual(resultGeneList);
            // 5、遗传进化(这里面这里已经处理完上课时间)
            individualMap = geneticEvolution(individualMap);

            // 检测时间冲突
//            checkConflict(individualMap);

            // 6、分配教室并做教室冲突检测
            List<String> resultList = finalResult(individualMap);
            // 7、解码
            List<CoursePlan> coursePlanList = decoding(resultList);
            // 8、写入tb_course_plan上课计划表
            coursePlanDao.deleteAllPlan();
            for (CoursePlan coursePlan : coursePlanList) {
                coursePlanDao.insertCoursePlan(coursePlan.getGradeNo(), coursePlan.getClassNo(), coursePlan.getCourseNo(),
                        coursePlan.getTeacherNo(), coursePlan.getClassroomNo(), coursePlan.getClassTime(), semester);
            }
            log.info("完成排课,耗时：" + (System.currentTimeMillis() - start));
            return ServerResponse.ofSuccess(String.format("排课成功，耗时：%sms", System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.error("排课失败： {}", e.getMessage());
            return ServerResponse.ofError("排课失败，出现异常!");
        }
    }

    private void checkWeeksNumber(List<ClassTask> classTaskList) {
        classTaskList.stream().collect(Collectors.groupingBy(ClassTask::getClassNo)).forEach((k, v) -> {
            int sum = v.stream().mapToInt(ClassTask::getWeeksNumber).sum();
            if (sum > ClassUtil.MAX_CLASS_TIME * 2) {
                throw new CourseArrangeException(String.format("班级：%s 的学时超过 %s，不能排课！", k, ClassUtil.MAX_CLASS_TIME * 2));
            }
        });
    }

    /**
     * 是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间
     */
    private void checkConflict(Map<String, List<String>> individualMap) {

        Map<String, Map<String, List<String>>> classMap = new HashMap<>();

        // 遍历map 中的每个班级并判断里面同一个course_no、teacher_no 下是否有两个一样的 class_time
        for (Map.Entry<String, List<String>> entry : individualMap.entrySet()) {

            List<String> geneList = entry.getValue();

            // 遍历 geneList并过滤同一个course_no、teacher_no 下是否有两个一样的 class_time
            Map<String, List<String>> map = new HashMap<>();
            for (String gene : geneList) {
                String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
                String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
                String key = teacherNo + "--" + classTime;
                if (!map.containsKey(key)) {
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(gene);
            }

            // 每个班级的冲突 map
            classMap.put(entry.getKey(), map);
        }

        for (Map.Entry<String, Map<String, List<String>>> entry : classMap.entrySet()) {
            Map<String, List<String>> conflictMap = entry.getValue();
            log.info("遍历 {} 班", entry.getKey());
            for (Map.Entry<String, List<String>> e : conflictMap.entrySet()) {
                String key = e.getKey();
                if (e.getValue().size() > 1) {
                    log.error("出现冲突 {}", key);
                    e.getValue().stream().map(item -> item.substring(11, 22) + "-" + item.substring(24, 26))
                            .collect(Collectors.toList()).forEach(i -> log.error("冲突的课程：{}", i));
                }
            }
            log.info("完成遍历 {} 班", entry.getKey());
        }
    }


    /**
     * 开始给进化完的基因编码分配教室，即在原来的编码中加上教室编号
     */
    private List<String> finalResult(Map<String, List<String>> individualMap) {
        List<String> resultList = new ArrayList<>();
        List<String> resultGeneList = collectGene(individualMap);
        String classroomNo = "";
        List<String> gradeList = classTaskDao.selectByColumnName(ConstantInfo.GRADE_NO);
        Map<String, List<String>> gradeMap = collectGeneByGrade(resultGeneList, gradeList);
        for (Map.Entry<String, List<String>> entry : gradeMap.entrySet()) {
            String gradeNo = entry.getKey();
            List<String> teachBuildNoList = teachBuildInfoDao.selectTeachBuildList(gradeNo);

            List<String> gradeGeneList = gradeMap.get(gradeNo);

            LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<Classroom>().in(Classroom::getTeachbuildNo, teachBuildNoList);
            List<Classroom> classroomList2 = classroomDao.selectList(wrapper);

            for (String gene : gradeGeneList) {
                // 分配教室
                classroomNo = issueClassroom(gene, classroomList2, resultList);
                gene = gene + classroomNo;
                resultList.add(gene);
            }
        }
        return resultList;
    }

    /**
     * 给不同的基因编码分配教室
     *
     * @param gene          需要分配教室的基因编码
     * @param classroomList 教室
     * @param resultList    分配有教室的编码
     */
    private String issueClassroom(String gene, List<Classroom> classroomList, List<String> resultList) {
        // 处理特殊课程，实验课，体育课
        List<Classroom> sportBuilding = classroomDao.selectByTeachbuildNo("12");
        List<Classroom> experimentBuilding = classroomDao.selectByTeachbuildNo("08");
        String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
        int studentNum = classInfoDao.selectStuNum(classNo);
        String courseAttr = ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene);

        if (courseAttr.equals(ConstantInfo.EXPERIMENT_COURSE)) {
            // 03 为实验课
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
     * 给不同课程的基因编码随机选择一个教室
     *
     * @param studentNum    开课的班级的学生人数
     * @param gene          需要安排教室的基因编码
     * @param classroomList 教室
     */
    private String chooseClassroom(int studentNum, String gene, List<Classroom> classroomList, List<String> resultList) {
        int min = 0;
        int max = classroomList.size() - 1;
        int temp = min + (int) (Math.random() * (max + 1 - min));
        Classroom classroom = classroomList.get(temp);
        // 分配教室
        boolean isClassRoomSuitable = judgeClassroom(studentNum, gene, classroom, resultList);
        if (isClassRoomSuitable) {
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
     * @param classroom  随机分配教室
     */
    private boolean judgeClassroom(int studentNum, String gene, Classroom classroom, List<String> resultList) {

        String courseAttr = ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene);
        // 只要是语数英物化生政史地这些课程都是放在普通教室上课
        if (courseAttr.equals(ConstantInfo.MAIN_COURSE) || courseAttr.equals(ConstantInfo.SECONDARY_COURSE)) {
            // 找到普通教室，普通教室的属性都是01
            if (classroom.getAttr().equals(ConstantInfo.NORMAL_CLASS_ROOM)) {
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
            if (ClassUtil.cutGene(ConstantInfo.COURSE_ATTR, gene).equals(classroom.getAttr())) {
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
     */
    private Boolean isFree(String gene, List<String> resultList, Classroom classroom) {
        if (resultList.isEmpty()) {
            return true;
        } else {
            for (String resultGene : resultList) {
                if (ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, resultGene).equals(classroom.getClassroomNo())
                        && (ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene).equals(ClassUtil.cutGene(ConstantInfo.CLASS_TIME, resultGene)))) {
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * 将所有的基因编码按照年级分类
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
            if (!geneList.isEmpty()) {
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
     * 2、交叉，选择
     * 3、变异
     * 4、重复2,3步骤
     * 5、直到达到终止条件
     *
     * @param individualMap 按班级分的基因编码
     */
    private Map<String, List<String>> geneticEvolution(Map<String, List<String>> individualMap) {
        List<String> resultGeneList;

        for (int i = 0; i < ConstantInfo.GENERATION; ++i) {
            hybridization(individualMap);
            List<String> allIndividual = collectGene(individualMap);
            resultGeneList = geneMutation(allIndividual);
            List<String> list = conflictResolution(resultGeneList);
            individualMap.clear();
            individualMap = transformIndividual(list);
        }

        return individualMap;
    }


    /**
     * 冲突消除,同一个讲师同一时间上多门课。解决：重新分配一个时间，直到所有的基因编码中
     * 不再存在上课时间冲突为止
     * 因素：讲师-课程-时间-教室
     *
     * @param resultGeneList 所有个体集合 （大种群）
     */
    private List<String> conflictResolution(List<String> resultGeneList) {
        int conflictTimes = 0;
        exit:
        for (int i = 0; i < resultGeneList.size(); i++) {
            String gene = resultGeneList.get(i);
            String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
            String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
            String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);

            for (int j = i + 1; j < resultGeneList.size(); j++) {
                String tempGene = resultGeneList.get(j);
                String tempTeacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, tempGene);
                String tempClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, tempGene);
                String tempClassNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, tempGene);
                // 冲突检测
                if (classTime.equals(tempClassTime) && classNo.equals(tempClassNo)) {
                    log.error("一个班级在同一时间上上多门课 {}", conflictTimes++);

                    String newClassTime = ClassUtil.randomTimeForClassConflict(gene, resultGeneList, classNo, teacherNo, classTime);

                    replaceConflictTime(resultGeneList, tempGene, newClassTime);

                    continue exit;
                } else if (classTime.equals(tempClassTime) && teacherNo.equals(tempTeacherNo)) {
                    log.error("同一个老师在同一时间上上多门课 {}", conflictTimes++);
                    String newClassTime = ClassUtil.randomTimeForTeacherConflict(gene, resultGeneList, teacherNo, classNo);
                    replaceConflictTime(resultGeneList, tempGene, newClassTime);
                    continue exit;
                }
            }
        }
        log.error("冲突发生次数: {}", conflictTimes);
        return resultGeneList;
    }

    private List<String> conflictResolution1(List<String> resultGeneList) {
        int conflictTimes = 0;
        eitx:
        for (int i = 0; i < resultGeneList.size(); i++) {
            String gene = resultGeneList.get(i);
            String teacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, gene);
            String classTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, gene);
            String classNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene);
            for (int j = i + 1; j < resultGeneList.size(); j++) {
                String tempGene = resultGeneList.get(j);
                String tempTeacherNo = ClassUtil.cutGene(ConstantInfo.TEACHER_NO, tempGene);
                String tempClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, tempGene);
                String tempClassNo = ClassUtil.cutGene(ConstantInfo.CLASS_NO, tempGene);
                // 冲突检测
                if (classTime.equals(tempClassTime) && (classNo.equals(tempClassNo) || teacherNo.equals(tempTeacherNo))) {
                        log.debug("出现冲突情况: {}", conflictTimes++);
                        String newClassTime = ClassUtil.randomTime();
                        String newGene = gene.substring(0, 24) + newClassTime;
                        replace(resultGeneList, tempGene, newGene);
                        continue eitx;

                }
            }
        }
        log.error("冲突发生次数: {}", conflictTimes);
        return resultGeneList;
    }

    private void replaceConflictTime(List<String> resultGeneList, String gene, String newClassTime) {
        String newGene = gene.substring(0, 24) + newClassTime;

        // 替换新的随机时间给剩余大种群里面的编码
        replace(resultGeneList, gene, newGene);
    }

    /**
     * 替换基因编码
     */
    private void replace(List<String> resultGeneList, String oldGene, String newGene) {
        for (int i = 0; i < resultGeneList.size(); i++) {
            if (resultGeneList.get(i).equals(oldGene)) {
                resultGeneList.set(i, newGene);
                log.info("替换冲突时间");
                break;
            }
        }
    }


    /**
     * 重新合拢交叉后的个体,即不分班级的基因编码，得到所有的编码
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
     */
    private List<String> geneMutation(List<String> resultGeneList) {
        final double mutationRate = 0.005;
        int mutationNumber = (int) (resultGeneList.size() * mutationRate);

        if (mutationNumber < 1) {
            mutationNumber = 1;
        }

        for (int i = 0; i < mutationNumber; i++) {
            int randomIndex = ClassUtil.RANDOM.nextInt(resultGeneList.size());
            String gene = resultGeneList.get(randomIndex);
            if (ClassUtil.cutGene(ConstantInfo.IS_FIX, gene).equals(ConstantInfo.FIX_TIME_FLAG)) {
                log.debug("固定时间的不会发生变异！！{} {}", ClassUtil.cutGene(gene, ConstantInfo.COURSE_NO), ClassUtil.cutGene(gene, ConstantInfo.CLASS_TIME));
                break;
            } else {
                String newClassTime = ClassUtil.randomTime();
                gene = gene.substring(0, 24) + newClassTime;
                resultGeneList.remove(randomIndex);
                resultGeneList.add(randomIndex, gene);
            }
        }
        return resultGeneList;
    }

    /**
     * 给每个班级交叉：一个班级看作一个种群
     */
    private Map<String, List<String>> hybridization(Map<String, List<String>> individualMap) {
        for (Map.Entry<String, List<String>> entry : individualMap.entrySet()) {
            String classNo = entry.getKey();
            List<String> individualList = individualMap.get(classNo);
            List<String> oldIndividualList = new ArrayList<>(individualList);

            selectGene(individualList);

            // 计算并对比子父代的适应度值，高的留下进行下一代遗传，相当于进化，
            if (ClassUtil.calculateExpectedValue(individualList) >= ClassUtil.calculateExpectedValue(oldIndividualList)) {
                individualMap.put(classNo, individualList);
            } else {
                individualMap.put(classNo, oldIndividualList);
            }
        }
        return individualMap;
    }


    /**
     * 个体中随机选择基因进行交叉(交换上课时间)
     */
    private List<String> selectGene(List<String> individualList) {
        int individualListSize = individualList.size();
        boolean flag;
        do {
            int firstIndex = ClassUtil.RANDOM.nextInt(individualListSize);
            int secondIndex = ClassUtil.RANDOM.nextInt(individualListSize);

            String firstGene = individualList.get(firstIndex);
            String secondGene = individualList.get(secondIndex);

            if (firstIndex == secondIndex) {
                flag = false;
            } else if (ClassUtil.cutGene(ConstantInfo.IS_FIX, firstGene).equals(ConstantInfo.FIX_TIME_FLAG)
                    || ClassUtil.cutGene(ConstantInfo.IS_FIX, secondGene).equals(ConstantInfo.FIX_TIME_FLAG)) {
                flag = false;
            } else {
                String firstClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, firstGene);
                String secondClassTime = ClassUtil.cutGene(ConstantInfo.CLASS_TIME, secondGene);
                firstGene = firstGene.substring(0, 24) + secondClassTime;
                secondGene = secondGene.substring(0, 24) + firstClassTime;
                individualList.set(firstIndex, firstGene);
                individualList.set(secondIndex, secondGene);
                flag = true;
            }
        } while (!flag);
        return individualList;
    }


    /**
     * 编码规则: （位）
     * 固定时间：1
     * 年级编号：2
     * 班级编号：8
     * 讲师编号：5
     * 课程编号：6
     * 课程属性：2
     * 上课时间：2
     * 教室编号：6
     * <p>
     * 编码规则为：是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间
     */
    private Map<String, List<String>> coding(List<ClassTask> classTaskList) {
        Map<String, List<String>> geneMap = new HashMap<>();
        List<String> unFixedTimeGeneList = new ArrayList<>();
        List<String> fixedTimeGeneList = new ArrayList<>();

        for (ClassTask classTask : classTaskList) {
            // 1，不固定上课时间，默认默认不再填充 00
            if (classTask.getIsFix().equals(ConstantInfo.UN_FIX_TIME_FLAG)) {
                // 得到每周上课的节数，因为设定2学时为一节课
                int size = classTask.getWeeksNumber() / 2;
                for (int i = 0; i < size; i++) {
                    String gene = classTask.getIsFix() + classTask.getGradeNo() + classTask.getClassNo()
                            + classTask.getTeacherNo() + classTask.getCourseNo() + classTask.getCourseAttr();

                    unFixedTimeGeneList.add(gene);
                }
            }
            // 2,固定上课时间
            if (classTask.getIsFix().equals(ConstantInfo.FIX_TIME_FLAG)) {
                int size = classTask.getWeeksNumber() / 2;
                for (int i = 0; i < size; i++) {
                    // 获得设定的固定时间：04 07
                    String classTime = classTask.getClassTime().substring(i * 2, (i + 1) * 2);
                    String gene = classTask.getIsFix() + classTask.getGradeNo() + classTask.getClassNo()
                            + classTask.getTeacherNo() + classTask.getCourseNo() + classTask.getCourseAttr() + classTime;
                    fixedTimeGeneList.add(gene);
                }
            }
        }
        geneMap.put(ConstantInfo.UN_FIXED_TIME, unFixedTimeGeneList);
        geneMap.put(ConstantInfo.IS_FIX_TIME, fixedTimeGeneList);

        return geneMap;
    }

    /**
     * 给初始基因编码随机分配时间(那些不固定上课时间的课程)
     *
     * @param geneMap 固定时间与不固定时间的编码集合
     */
    private List<String> codingTime(Map<String, List<String>> geneMap) {

        List<String> fixedTimeGeneList = geneMap.get(ConstantInfo.IS_FIX_TIME);
        List<String> unFixedTimeGeneList = geneMap.get(ConstantInfo.UN_FIXED_TIME);
        List<String> resultGeneList = new ArrayList<>(fixedTimeGeneList);

        for (String gene : unFixedTimeGeneList) {
            String classTime = ClassUtil.randomTime();
            gene = gene + classTime;
            resultGeneList.add(gene);
        }
        return resultGeneList;
    }

    /**
     * 将初始基因编码(都分配好时间)划分以班级为单位的个体
     * 班级编号的集合，去重
     */
    private Map<String, List<String>> transformIndividual(List<String> resultGeneList) {
        Map<String, List<String>> individualMap = new HashMap<>();
        List<String> classNoList = classTaskDao.selectClassNo();

        for (String classNo : classNoList) {
            List<String> geneList = new ArrayList<>();
            for (String gene : resultGeneList) {
                if (classNo.equals(ClassUtil.cutGene(ConstantInfo.CLASS_NO, gene))) {
                    geneList.add(gene);
                }
            }
            if (!geneList.isEmpty()) {
                individualMap.put(classNo, geneList);
            }
        }
        return individualMap;
    }


    /**
     * 解码染色体中的基因，按照之前的编码解
     * 编码:
     * 固定时间：1
     * 年级编号：2
     * 班级编号：8
     * 讲师编号：5
     * 课程编号：6
     * 课程属性：2
     * 上课时间：2
     * 教室编号：6
     * 编码规则为：是否固定+年级编号+班级编号+教师编号+课程编号+课程属性+上课时间+教室编号(遗传算法执行完最后再分配教室)
     * 其中如果不固定开课时间默认填充为"00"
     *
     * @param resultList 全部上课计划实体
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
