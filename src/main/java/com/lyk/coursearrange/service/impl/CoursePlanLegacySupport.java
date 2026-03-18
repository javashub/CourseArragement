package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.dao.CoursePlanDao;
import com.lyk.coursearrange.entity.CoursePlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * legacy tb_course_plan 兼容访问层。
 */
@Slf4j
@Component
public class CoursePlanLegacySupport {

    @Resource
    private CoursePlanDao coursePlanDao;

    public List<CoursePlan> listAll() {
        try {
            return coursePlanDao.selectList(new LambdaQueryWrapper<>());
        } catch (Exception exception) {
            log.warn("查询全部 legacy 课表副本失败，将返回空列表", exception);
            return List.of();
        }
    }

    public List<CoursePlan> listByClassNo(String classNo, String semester) {
        try {
            LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<CoursePlan>()
                    .eq(CoursePlan::getClassNo, classNo)
                    .orderByAsc(CoursePlan::getClassTime);
            wrapper.eq(semester != null && !semester.isBlank(), CoursePlan::getSemester, semester);
            return coursePlanDao.selectList(wrapper);
        } catch (Exception exception) {
            log.warn("查询 legacy 班级课表失败，将返回标准课表结果或空数据，classNo={}, semester={}", classNo, semester, exception);
            return List.of();
        }
    }

    public List<CoursePlan> listByTeacherNo(String teacherNo, String semester) {
        try {
            LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<CoursePlan>()
                    .eq(CoursePlan::getTeacherNo, teacherNo)
                    .orderByAsc(CoursePlan::getClassTime);
            wrapper.eq(semester != null && !semester.isBlank(), CoursePlan::getSemester, semester);
            return coursePlanDao.selectList(wrapper);
        } catch (Exception exception) {
            log.warn("查询 legacy 教师课表失败，将返回标准课表结果或空数据，teacherNo={}, semester={}", teacherNo, semester, exception);
            return List.of();
        }
    }

    public CoursePlan getById(Integer id) {
        if (id == null) {
            return null;
        }
        try {
            return coursePlanDao.selectById(id);
        } catch (Exception exception) {
            log.warn("查询 legacy 课表记录失败，id={}", id, exception);
            return null;
        }
    }

    public boolean updateById(CoursePlan plan) {
        if (plan == null) {
            return false;
        }
        try {
            return coursePlanDao.updateById(plan) > 0;
        } catch (Exception exception) {
            log.warn("更新 legacy 课表副本失败，id={}", plan.getId(), exception);
            return false;
        }
    }

    public long count(LambdaQueryWrapper<CoursePlan> wrapper) {
        try {
            return coursePlanDao.selectCount(wrapper);
        } catch (Exception exception) {
            log.warn("统计 legacy 课表冲突失败，将忽略 legacy 冲突检测", exception);
            return 0;
        }
    }

    public CoursePlan getOne(LambdaQueryWrapper<CoursePlan> wrapper) {
        try {
            return coursePlanDao.selectOne(wrapper);
        } catch (Exception exception) {
            log.warn("查询 legacy 课表副本失败", exception);
            return null;
        }
    }

    public boolean replaceCoursePlans(String semester, List<CoursePlan> coursePlanList) {
        try {
            coursePlanDao.deleteBySemester(semester);
            for (CoursePlan coursePlan : coursePlanList) {
                coursePlanDao.insertCoursePlan(coursePlan.getGradeNo(), coursePlan.getClassNo(), coursePlan.getCourseNo(),
                        coursePlan.getTeacherNo(), coursePlan.getClassroomNo(), coursePlan.getClassTime(), semester);
            }
            return true;
        } catch (Exception exception) {
            logLegacyCoursePlanAccessFailure("写入 legacy 课表副本失败，后续将仅使用标准课表结果", semester, exception);
            return false;
        }
    }

    private void logLegacyCoursePlanAccessFailure(String message, String semester, Exception exception) {
        if (isMissingLegacyCoursePlanTable(exception)) {
            if (semester == null || semester.isBlank()) {
                log.warn("{}, tb_course_plan 已不存在，将继续使用标准课表链路", message);
            } else {
                log.warn("{}, semester={}, tb_course_plan 已不存在，将继续使用标准课表链路", message, semester);
            }
            return;
        }
        if (semester == null || semester.isBlank()) {
            log.warn(message, exception);
        } else {
            log.warn(message + "，semester={}", semester, exception);
        }
    }

    private boolean isMissingLegacyCoursePlanTable(Exception exception) {
        String message = exception == null ? null : exception.getMessage();
        return message != null && message.contains("tb_course_plan") && message.contains("doesn't exist");
    }
}
