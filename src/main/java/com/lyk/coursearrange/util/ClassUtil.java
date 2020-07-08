package com.lyk.coursearrange.util;

import com.lyk.coursearrange.entity.request.ConstantInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author: 15760
 * @Date: 2020/4/1
 * @Descripe: 判断冲突，解码
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

    /**
     * 用于切割获得编码的染色体中需要的属性
     *
     * @param aim
     * @param source
     * @return
     */
    public static String cutGene(String aim, String source) {
        switch (aim) {
            case ConstantInfo.IS_FIX:
                return source.substring(0, 1); // 固定时间 1
            case ConstantInfo.GRADE_NO:
                return source.substring(1, 3); // 年级编号 2
            case ConstantInfo.CLASS_NO:
                return source.substring(3, 11); // 班级编号 11
            case ConstantInfo.TEACHER_NO:
                return source.substring(11, 16); // 讲师编号 5
            case ConstantInfo.COURSE_NO:
                return source.substring(16, 22); // 课程编号 6
            case ConstantInfo.COURSE_ATTR:
                return source.substring(22, 24); // 课程属性 2
            case ConstantInfo.CLASS_TIME:
                return source.substring(24, 26); // 上课时间
            case ConstantInfo.CLASSROOM_NO:
                return source.substring(26, 32); // 教室编号
            default:
                return "";
        }
    }

    public static Boolean judgeTime(String time, String gene, List<String> geneList) {

        for (String str : geneList) {
            // 讲师--时间    班级--时间
            // 得到遍历编码中的讲师、班级编号
            String teacherNo = cutGene(ConstantInfo.TEACHER_NO, str);
            String classNo = cutGene(ConstantInfo.CLASS_NO, str);
            String classTime = cutGene(ConstantInfo.CLASS_TIME, str);
            if (((teacherNo.equals(cutGene(ConstantInfo.TEACHER_NO, gene))) && (classTime.equals(time)))
                    || classNo.equals(cutGene(ConstantInfo.CLASS_TIME, gene)) && time.equals(cutGene(ConstantInfo.CLASS_TIME, str))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 判断同一个班级同一时间内是否有冲突的上课情况
     * @param time 随机生成的时间
     * @param gene 待分配时间的编码
     * @param geneList
     * @return
     */
    public static Boolean isTimeRepeat(String time, String gene, List<String> geneList) {
        // 先从染色体中获得班级编号
        String classNo = cutGene(ConstantInfo.CLASS_NO, gene);
        String teacherNo = cutGene(ConstantInfo.TEACHER_NO,gene);
        for (String str : geneList) {

            if(time.equals(cutGene(ConstantInfo.CLASS_TIME,str))){
                if(classNo.equals(cutGene(ConstantInfo.CLASS_NO,str)) || teacherNo.equals(cutGene(ConstantInfo.TEACHER_NO,str))){
                    log.info("{},time:{},老师冲突:{}{},班级冲突:{}{}",geneList.size(),time,teacherNo,teacherNo.equals(cutGene(ConstantInfo.TEACHER_NO,str)),classNo,classNo.equals(cutGene(ConstantInfo.CLASS_NO,str)));
                    return false;
                }
            }

//            String teacherNo2 = cutGene(ConstantInfo.TEACHER_NO,str);
//            if(teacherNo.equals(teacherNo2)){
//                String classTime2 = cutGene(ConstantInfo.CLASS_TIME,str);
//                if(classTime2.equals(time)){
//                    return false;
//                }
//            }

            /*
            // 判断班级编号是否相等，这种情况下只是处理了同班上课时间不冲突的情况，还有同讲师同一时间的未处理
            if (classNo.equals(cutGene(ConstantInfo.CLASS_NO, str))) {
                // 在班级编号相等的情况下再看看上课时间是否相等,不相等就返回true
                String classTime = cutGene(ConstantInfo.CLASS_TIME, str);
                if (time.equals(classTime)) {
                    return false;
                }
            } else {
                // 如果是不同班级之间判断老师的上课时间
                String teacherNo = cutGene(ConstantInfo.TEACHER_NO, str);
                String teacherNo2 = cutGene(ConstantInfo.TEACHER_NO, gene);
                String classTime = cutGene(ConstantInfo.CLASS_TIME, str);
                // 用下面这条会出现一些冲突
//                String classTime = cutGene(ConstantInfo.CLASS_TIME, gene);

                if (teacherNo.equals(teacherNo2) && time.equals(classTime)) {
                    return false;
                }
            }
             */
        }
        return true;
    }


    /**
     * 方法不适用多任务的情况，会出现递归调用导致栈溢出
     * @param gene     待分配时间的基因编码
     * @param geneList 需要比对的编码集合，最初为固定时间的编码，逐渐增加
     * @return
     */
    public static String randomTime2(String gene, List<String> geneList) {
        int min = 1;
        int max = 25;
        String time;
        //随机生成1到25范围的数字，并将其转化为字符串，方便进行时间编码
        int temp = min + (int) (Math.random() * (max + 1 - min));
        // 转化成2位字符串
        if (temp < 10) {
            time = "0" + temp;
        } else {
            time = "" + temp;
        }

        if (isTimeRepeat(time, gene, geneList)) {
            // 不冲突
            return time;
        } else {
            // 递归调用导致出现栈溢出
            // 冲突，重新生成随机时间
            return randomTime(gene, geneList);
        }
    }

    /**
     * 这种随机生成时间的方式，初始可能出现有同班级相同时间的情况
     * @param gene
     * @param geneList
     * @return
     */
    public static String randomTime(String gene, List<String> geneList) {

        exit:
        for (; ;) {
            int min = 1;
            int max = 25;
            String time;

            int temp = min + (int) (Math.random() * (max + 1 - min));
            System.out.println(temp);
            if (temp < 10) {
                time = "0" + temp;
            } else {
                time = "" + temp;
            }

            return time;
//            if (isTimeRepeat(time, gene, geneList)) {
//                return time;
//            } else {
//                System.out.println("执行退出了");
//                continue exit;
//            }
        }
    }


    /**
     * 计算主要课程的期望值
     * 例如语文数学英语在高中阶段是需要设置多一点，设置在前面上课
     * @param classTime
     * @return
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
        // 主要课程期望值为0时的时间片值
        //String [] zeroExpectValue = {"05","10","15","20","25"};

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
     * @param classTime
     * @return
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
     * @param classTime
     * @return
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
     *
     * @param classTime
     * @return
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
     * @param temp
     * @return
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
     * @param individualList
     * @return
     */
    public static double calculatExpectedValue(List<String> individualList) {
        double K1 = 0.3; // 主要课所占权重
        double K2 = 0.1; // 次要课所占权重
        double K3 = 0.1; // 体育课所占权重
        double K4 = 0.3; // 实验课所占权重
        double K5 = 0.2; // 课程离散程度所占权重

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

            if (courseAttr.equals(ConstantInfo.MAIN_COURSE)) {
                F1 = F1 + calculateMainExpect(classTime);
            } else if (courseAttr.equals(ConstantInfo.SECONDARY_COURSE)) {
                F2 = F2 + calculateSecondaryExpect(classTime);
            } else if (courseAttr.equals(ConstantInfo.PHYSICAL_COURSE)) {
                F3 = F3 + calculatePhysicalExpect(classTime);
            } else {
                F4 = F4 + calculateExperimentExpect(classTime);
            }
        }
        // 计算期望值
        F5 = calculateDiscreteExpect(individualList);
        // 总适应度
        Fx = K1 * F1 + K2 * F2 + K3 * F3 + K4 * F4 + K5 * F5;
        return Fx; // 整个种群的适应度值
    }

    /**
     * 将一个个体（班级课表）的同一门课程的所有上课时间进行统计，并且进行分组
     * 每个班级的课表都算是一个个体
     * @param individualList
     * @return
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
     * @param individualList 每个班级的编码集合
     * @return
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
