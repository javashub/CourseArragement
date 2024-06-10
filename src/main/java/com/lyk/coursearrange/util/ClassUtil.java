package com.lyk.coursearrange.util;

import com.lyk.coursearrange.common.ConstantInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author: 15760
 * @Date: 2020/4/1
 * @Descripe: 工具类
 * 固定时间：1
 * 年级编号：2
 * 班级编号：8
 * 讲师编号：5
 * 课程编号：6
 * 课程属性：2
 * 上课时间：2
 * 教室编号：6
 */
@Slf4j
public class ClassUtil {

    private ClassUtil() {
    }

    public static final Random RANDOM = new Random();

    /**
     * 课程表最大节数，当前是 25，用于生成随机时间的上限
     */
    public static final int MAX_CLASS_TIME = 25;

    /**
     * 用于切割获得编码的染色体中需要的属性
     *
     * @param aim 目标信息
     * @param source 源编码
     * @return 切割出来的目标信息
     */
    public static String cutGene(String aim, String source) {
        switch (aim) {
            case ConstantInfo.IS_FIX:
                // 固定时间 1
                return source.substring(0, 1);
            case ConstantInfo.GRADE_NO:
                // 年级编号 2
                return source.substring(1, 3);
            case ConstantInfo.CLASS_NO:
                // 班级编号 11
                return source.substring(3, 11);
            case ConstantInfo.TEACHER_NO:
                // 讲师编号 5
                return source.substring(11, 16);
            case ConstantInfo.COURSE_NO:
                // 课程编号 6
                return source.substring(16, 22);
            case ConstantInfo.COURSE_ATTR:
                // 课程属性 2
                return source.substring(22, 24);
            case ConstantInfo.CLASS_TIME:
                // 上课时间
                return source.substring(24, 26);
            case ConstantInfo.CLASSROOM_NO:
                // 教室编号
                return source.substring(26, 32);
            default:
                return "";
        }
    }



    /**
     * @author lyk
     * @description 生成随机时间
     * @date 2024/4/23 21:39
     * @return -> 两位时间
     **/
    public static String randomTime() {
        int temp = RANDOM.nextInt(MAX_CLASS_TIME) + 1;
        return temp < 10 ? ("0" + temp) : String.valueOf(temp);
    }


    /**
     * @author lyk
     * @description 生成 01 - 25 的时间集合
     * @date 2024/4/10 09:41
     * @return -> java.util.List<java.lang.String>
     **/
    private static List<String> getAllTime() {
        return IntStream.rangeClosed(1, MAX_CLASS_TIME).mapToObj(i -> i < 10 ? ("0" + i) : String.valueOf(i)).collect(Collectors.toList());
    }

    /**
     * @author lyk
     * @description 同一个班级同时有两个时间在上课的冲突问题
     * @date 2024/4/10 09:24
     * @param gene 当前发生冲突的基因编码
     * @param geneList 种群
     * @param classNo 班级
     * @param teacherNo 讲师编号
     * @param classTime 上课时间
     * @return -> java.lang.String
     **/
    public static String randomTimeForClassConflict(String gene, List<String> geneList, String classNo, String teacherNo, String classTime) {

        // 找出当前班级在 01-25 时间之间还未使用的时间
        Set<String> usedTimeList =
                geneList.stream().filter(item -> cutGene(ConstantInfo.CLASS_NO, item).equals(classNo))
                        .map(item -> cutGene(ConstantInfo.CLASS_TIME, item)).sorted().collect(Collectors.toSet());

        log.debug("{} 班级 剩余空闲上课时间 {}", classNo, usedTimeList);

        return getFreeTime(usedTimeList);
    }

    public static String randomTimeForTeacherConflict(String gene, List<String> geneList, String teacherNo, String classNo) {

        // 找出当前教师在 01-25 时间之间还未使用的时间
        Set<String> usedTimeList =
                geneList.stream().filter(item -> cutGene(ConstantInfo.TEACHER_NO, item).equals(teacherNo))
                        .map(item -> cutGene(ConstantInfo.CLASS_TIME, item)).sorted().collect(Collectors.toCollection(LinkedHashSet::new));

        log.debug("{} 讲师 已经用的上课时间 {}", teacherNo, usedTimeList);

        return getFreeTime(usedTimeList);
    }

    /**
     * @author lyk
     * @description 获取 01-25 内还未使用的时间
     * @return -> java.lang.String
     **/
    private static String getFreeTime(Set<String> usedTimeList) {
        List<String> allTime = getAllTime();

        boolean isRemoveSuccess = allTime.removeAll(usedTimeList);

        if (isRemoveSuccess && !allTime.isEmpty()) {
            int randomIndex = RANDOM.nextInt(allTime.size());
            return allTime.get(randomIndex);
        }

        return randomTime();
    }


    /**
     * 计算主要课程的期望值
     * 例如语文数学英语在高中阶段是需要设置多一点，设置在前面上课
     */
    private static int calculateMainExpect(String classTime) {
        // 主要课程期望值为10时的时间片值，放在第一节课
        String[] tenExpectValue = {"01", "06", "11", "16", "21"};
        // 主要课程期望值为8时的时间片值
        String[] eightExpectValue = {"02", "07", "12", "17", "22"};
        // 主要课程期望值为4时的时间片值
        String[] fourExpectValue = {"03", "08", "13", "18", "23"};
        // 主要课程期望值为2时的时间片值
        String[] twoExpectValue = {"04", "09", "14", "19", "24"};

        if (ArrayUtils.contains(tenExpectValue, classTime)) {
            return 10;
        } else if (ArrayUtils.contains(eightExpectValue, classTime)) {
            return 10;
        } else if (ArrayUtils.contains(fourExpectValue, classTime)) {
            return 4;
        } else if (ArrayUtils.contains(twoExpectValue, classTime)) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * 计算次要课程的期望值
     * 物理、化学、生物
     * 政治、历史、地理
     */
    private static int calculateSecondaryExpect(String classTime) {
        // 次要课程期望值为10时的时间片值
        String[] tenExpectValue = {"03", "08", "13", "18", "23"};
        // 次要课程期望值为8时的时间片值
        String[] eightExpectValue = {"02", "07", "12", "17", "22"};
        // 次要课程期望值为4时的时间片值
        String[] fourExpectValue = {"01", "04", "06", "09", "11", "16", "19", "21", "24"};
        //String [] zeroExpectValue = {"05","10","15","20","25"};//选修课期望值为0时的时间片值

        if (ArrayUtils.contains(tenExpectValue, classTime)) {
            return 10;
        } else if (ArrayUtils.contains(eightExpectValue, classTime)) {
            return 8;
        } else if (ArrayUtils.contains(fourExpectValue, classTime)) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * 计算体育课的期望值
     *
     * @param classTime 上课时间
     * @return 期望值
     */
    private static int calculatePhysicalExpect(String classTime) {
        String[] tenExpectValue = {"04", "09", "14", "19"};//体育课期望值为10时的时间片值  24
        String[] eightExpectValue = {"03", "08", "13", "18"};//体育课期望值为8时的时间片值 23
        String[] fourExpectValue = {"02", "07", "12", "17", "22"};//体育课期望值为4时的时间片值
        //String [] zeroExpectValue = {"01","05","06","10","11","15","16","20","21","23","24","25"};//体育课期望值为0时的时间片值

        if (ArrayUtils.contains(tenExpectValue, classTime)) {
            return 10;
        } else if (ArrayUtils.contains(eightExpectValue, classTime)) {
            return 8;
        } else if (ArrayUtils.contains(fourExpectValue, classTime)) {
            return 4;
        } else {
            return 0;
        }
    }


    /**
     * 计算实验课的期望值
     */
    private static int calculateExperimentExpect(String classTime) {
        String[] tenExpectValue = {"04", "09", "14", "19"};//实验课期望值为10时的时间片值
        String[] eightExpectValue = {"05", "10", "15", "20", "25"};//实验课期望值为8时的时间片值
        String[] sixExpectValue = {"03", "08", "13", "18"};//实验课期望值为6时的时间片值
        String[] fourExpectValue = {"02", "07", "12", "17", "22"};//实验课期望值为4时的时间片值
        //String [] zeroExpectValue = {"01","06","11","16","21","23","24","25"};//实验课期望值为0时的时间片值

        if (ArrayUtils.contains(tenExpectValue, classTime)) {
            return 10;
        } else if (ArrayUtils.contains(eightExpectValue, classTime)) {
            return 8;
        } else if (ArrayUtils.contains(sixExpectValue, classTime)) {
            return 6;
        } else if (ArrayUtils.contains(fourExpectValue, classTime)) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * 判断两课程的时间差在哪个区间
     * 并返回对应的期望值
     */
    private static int judgingDiscreteValues(int temp) {
        int[] tenExpectValue = {5, 6, 7, 8}; // 期望值为10时两课之间的时间差
        int[] sixExpectValue = {4, 9, 10, 11, 12, 13}; // 期望值为6时两课之间的时间差
        int[] fourExpectValue = {3, 14, 15, 16, 17, 18}; // 期望值为4时两课之间的时间差
        int[] twoExpectValue = {2, 19, 20, 21, 22, 23}; // 期望值为2时两课之间的时间差
        //int [] zeroExpectValue = {1,24};//期望值为0时两课之间的时间差
        if (ArrayUtils.contains(tenExpectValue, temp)) {
            return 10;
        } else if (ArrayUtils.contains(sixExpectValue, temp)) {
            return 6;
        } else if (ArrayUtils.contains(fourExpectValue, temp)) {
            return 4;
        } else if (ArrayUtils.contains(twoExpectValue, temp)) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * 计算每个个体的适应值
     */
    public static double calculateExpectedValue(List<String> individualList) {
        // <1 01 20200101 10010 100001 01 05> 优先 01 =》 5   05 =》 1
        // <1 01 20200101 10010 100001 03 00> 次
        // 主要课所占权重 01
        double K1 = 0.3;
        // 次要课所占权重 02
        double K2 = 0.1;
        // 体育课所占权重 03
        double K3 = 0.1;
        // 实验课所占权重 03
        double K4 = 0.3;

        // 课程离散程度所占权重
        double K5 = 0.2;

        int F1 = 0; // 主要课程期望总值
        int F2 = 0; // 次要课程期望总值
        int F3 = 0; // 体育课期望总值
        int F4 = 0; // 实验课期望总值
        int F5; // 课程离散程度期望总值

        double Fx; // 总适应度值

        // 开始计算每一个个体的适应度
        for (String gene : individualList) {
            // 获得课程属性
            String courseAttr = cutGene(ConstantInfo.COURSE_ATTR, gene);
            // 获得该课程的开课时间
            String classTime = cutGene(ConstantInfo.CLASS_TIME, gene);

            switch (courseAttr) {
                case ConstantInfo.MAIN_COURSE:
                    F1 = F1 + calculateMainExpect(classTime);
                    break;
                case ConstantInfo.SECONDARY_COURSE:
                    F2 = F2 + calculateSecondaryExpect(classTime);
                    break;
                case ConstantInfo.PHYSICAL_COURSE:
                    F3 = F3 + calculatePhysicalExpect(classTime);
                    break;
                default:
                    F4 = F4 + calculateExperimentExpect(classTime);
                    break;
            }
        }
        // 计算期望值
        F5 = calculateDiscreteExpect(individualList);
        // 总适应度 整个种群的适应度值
        Fx = K1 * F1 + K2 * F2 + K3 * F3 + K4 * F4 + K5 * F5;
        return Fx;
    }

    /**
     * 将一个个体（班级课表）的同一门课程的所有上课时间进行统计，并且进行分组
     * 每个班级的课表都算是一个个体
     */
    private static Map<String, List<String>> courseGrouping(List<String> individualList) {
        Map<String, List<String>> classTimeMap = new HashMap<>();
        // 先将一个班级课表所上的课程区分出来（排除掉重复的课程）
        for (String gene : individualList) {
            classTimeMap.put(cutGene(ConstantInfo.COURSE_NO, gene), null);
        }
        // 遍历课程编号
        for (String courseNo : classTimeMap.keySet()) {
            List<String> classTimeList = new ArrayList<>();
            for (String gene : individualList) {
                // 获得同一门课程的所有上课时间片
                if (cutGene(ConstantInfo.COURSE_NO, gene).equals(courseNo)) {
                    classTimeList.add(cutGene(ConstantInfo.CLASS_TIME, gene));
                }
            }
            // 将课程的时间片进行排序
            Collections.sort(classTimeList);
            // 每一门课对应的上课时间集合(classNo, List)
            classTimeMap.put(courseNo, classTimeList);
        }
        return classTimeMap;
    }


    /**
     * 计算课程离散度期望值
     */
    private static int calculateDiscreteExpect(List<String> individualList) {
        // 离散程度期望值
        int F5 = 0;
        // 返回每个班级的对应课程下面的排序上课时间
        Map<String, List<String>> classTimeMap = courseGrouping(individualList);

        for (List<String> classTimeList : classTimeMap.values()) {
            if (classTimeList.size() > 1) {
                for (int i = 0; i < classTimeList.size() - 1; ++i) {
                    // 计算一门课上课的时间差
                    int temp = Integer.parseInt(classTimeList.get(++i)) - Integer.parseInt(classTimeList.get(i - 1));
                    F5 = F5 + judgingDiscreteValues(temp);
                }
            }
        }
        return F5;
    }
}
