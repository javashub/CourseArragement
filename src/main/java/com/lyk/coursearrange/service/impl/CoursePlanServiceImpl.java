package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.CourseInfo;
import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.dao.CoursePlanDao;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.entity.response.CoursePlanVo;
import com.lyk.coursearrange.service.CoursePlanAdjustLogService;
import com.lyk.coursearrange.service.CourseInfoService;
import com.lyk.coursearrange.service.CoursePlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.service.TeacherService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lequal
 * @since 2020-04-15
 */
@Slf4j
@Service
public class CoursePlanServiceImpl extends ServiceImpl<CoursePlanDao, CoursePlan> implements CoursePlanService {

    @Resource
    private CourseInfoService courseInfoService;
    @Resource
    private TeacherService teacherService;
    @Resource
    private CoursePlanDao coursePlanDao;
    @Resource
    private CoursePlanAdjustLogService coursePlanAdjustLogService;
    @Resource
    private AuthFacadeService authFacadeService;
    @Resource
    private ScheduleLogMirrorService scheduleLogMirrorService;

    /**
     * 根据班级编号查询课表
     * @param classNo 班级编号
     */
    @Override
    public ServerResponse queryCoursePlanByClassNo(String classNo) {
        LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<CoursePlan>().eq(CoursePlan::getClassNo, classNo).orderByAsc(CoursePlan::getClassTime);
        List<CoursePlan> coursePlanList = coursePlanDao.selectList(wrapper);

        return buildCoursePlanResponse(coursePlanList, "该班级没有课表");
    }

    @Override
    public ServerResponse queryCoursePlanByTeacherNo(String teacherNo) {
        LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<CoursePlan>().eq(CoursePlan::getTeacherNo, teacherNo).orderByAsc(CoursePlan::getClassTime);
        List<CoursePlan> coursePlanList = coursePlanDao.selectList(wrapper);

        return buildCoursePlanResponse(coursePlanList, "该教师没有课表");
    }

    @Override
    public ServerResponse adjustCoursePlan(CoursePlanAdjustRequest request) {
        CoursePlan coursePlan = getById(request.getId());
        if (coursePlan == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课表记录不存在");
        }
        if (request.getClassTime() == null || request.getClassTime().isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "目标时间片不能为空");
        }
        String targetClassTime = request.getClassTime();
        String targetClassroomNo = request.getClassroomNo() != null && !request.getClassroomNo().isBlank()
                ? request.getClassroomNo()
                : coursePlan.getClassroomNo();
        String beforeClassTime = coursePlan.getClassTime();
        String beforeClassroomNo = coursePlan.getClassroomNo();
        String conflictMessage = validateAdjustConflict(coursePlan, targetClassTime, targetClassroomNo);
        if (conflictMessage != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, conflictMessage);
        }
        coursePlan.setClassTime(targetClassTime);
        coursePlan.setClassroomNo(targetClassroomNo);
        boolean updated = updateById(coursePlan);
        if (updated) {
            saveAdjustLog(coursePlan, beforeClassTime, beforeClassroomNo, targetClassTime, targetClassroomNo);
            return ServerResponse.ofSuccess("调课成功", coursePlan);
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "调课失败，请稍后重试");
    }

    private String validateAdjustConflict(CoursePlan currentPlan, String targetClassTime, String targetClassroomNo) {
        LambdaQueryWrapper<CoursePlan> classWrapper = buildAdjustConflictWrapper(currentPlan, targetClassTime)
                .eq(CoursePlan::getClassNo, currentPlan.getClassNo());
        if (count(classWrapper) > 0) {
            return "目标时间片已存在同班级课程，不能调课";
        }

        LambdaQueryWrapper<CoursePlan> teacherWrapper = buildAdjustConflictWrapper(currentPlan, targetClassTime)
                .eq(CoursePlan::getTeacherNo, currentPlan.getTeacherNo());
        if (count(teacherWrapper) > 0) {
            return "目标时间片教师已有其他课程，不能调课";
        }

        if (targetClassroomNo != null && !targetClassroomNo.isBlank()) {
            LambdaQueryWrapper<CoursePlan> classroomWrapper = buildAdjustConflictWrapper(currentPlan, targetClassTime)
                    .eq(CoursePlan::getClassroomNo, targetClassroomNo);
            if (count(classroomWrapper) > 0) {
                return "目标时间片教室已被占用，不能调课";
            }
        }
        return null;
    }

    @Override
    public List<CoursePlanAdjustLog> listRecentAdjustLogs(String semester, String classNo, String teacherNo, Integer limit) {
        LambdaQueryWrapper<CoursePlanAdjustLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoursePlanAdjustLog::getDeleted, 0)
                .eq(semester != null && !semester.isBlank(), CoursePlanAdjustLog::getSemester, semester)
                .eq(classNo != null && !classNo.isBlank(), CoursePlanAdjustLog::getClassNo, classNo)
                .eq(teacherNo != null && !teacherNo.isBlank(), CoursePlanAdjustLog::getTeacherNo, teacherNo)
                .orderByDesc(CoursePlanAdjustLog::getCreateTime)
                .last("limit " + (limit == null || limit <= 0 ? 10 : limit));
        return coursePlanAdjustLogService.list(wrapper);
    }

    private LambdaQueryWrapper<CoursePlan> buildAdjustConflictWrapper(CoursePlan currentPlan, String targetClassTime) {
        LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoursePlan::getSemester, currentPlan.getSemester())
                .eq(CoursePlan::getClassTime, targetClassTime)
                .ne(CoursePlan::getId, currentPlan.getId())
                .eq(CoursePlan::getDeleted, 0);
        return wrapper;
    }

    private void saveAdjustLog(CoursePlan coursePlan,
                               String beforeClassTime,
                               String beforeClassroomNo,
                               String targetClassTime,
                               String targetClassroomNo) {
        CoursePlanAdjustLog logEntity = new CoursePlanAdjustLog();
        logEntity.setCoursePlanId(coursePlan.getId());
        logEntity.setSemester(coursePlan.getSemester());
        logEntity.setGradeNo(coursePlan.getGradeNo());
        logEntity.setClassNo(coursePlan.getClassNo());
        logEntity.setCourseNo(coursePlan.getCourseNo());
        logEntity.setTeacherNo(coursePlan.getTeacherNo());
        logEntity.setBeforeClassTime(beforeClassTime);
        logEntity.setAfterClassTime(targetClassTime);
        logEntity.setBeforeClassroomNo(beforeClassroomNo);
        logEntity.setAfterClassroomNo(targetClassroomNo);
        logEntity.setRemark("拖拽调课");
        try {
            CurrentUserVO currentUser = authFacadeService.getCurrentUserView();
            if (currentUser != null) {
                logEntity.setOperatorUserId(currentUser.getUserId());
                logEntity.setOperatorName(currentUser.getDisplayName());
                logEntity.setOperatorType(currentUser.getUserType());
            }
        } catch (Exception exception) {
            log.warn("记录调课日志时获取当前用户失败，planId={}", coursePlan.getId(), exception);
        }
        coursePlanAdjustLogService.save(logEntity);
        scheduleLogMirrorService.mirrorAdjustLog(logEntity);
    }

    private ServerResponse buildCoursePlanResponse(List<CoursePlan> coursePlanList, String emptyMessage) {

        if (null == coursePlanList || coursePlanList.isEmpty()) {
            return ServerResponse.ofError(emptyMessage);
        }

        // 过滤出教师编号
        List<String> teacherNos = coursePlanList.stream().map(CoursePlan::getTeacherNo).distinct().collect(Collectors.toList());
        // 过滤出课程编号
        List<String> courseNos = coursePlanList.stream().map(CoursePlan::getCourseNo).distinct().collect(Collectors.toList());

        // 查询教师信息
        List<Teacher> teachers = teacherService.list(new LambdaQueryWrapper<Teacher>().in(Teacher::getTeacherNo, teacherNos));
        // 查询课程信息
        List<CourseInfo> courseInfos = courseInfoService.list(new LambdaQueryWrapper<CourseInfo>().in(CourseInfo::getCourseNo, courseNos));

        List<CoursePlanVo> coursePlanVos = new LinkedList<>();
        coursePlanList.forEach(v -> {
            // v 转换成 CoursePlanVo 对象
            CoursePlanVo coursePlanVo = new CoursePlanVo();
            BeanUtils.copyProperties(v, coursePlanVo);

            // 根据教师编号找到教师名称
            teachers.stream().filter(t -> t.getTeacherNo().equals(v.getTeacherNo())).findFirst().ifPresent(t -> coursePlanVo.setRealname(t.getRealname()));
            courseInfos.stream().filter(c -> c.getCourseNo().equals(v.getCourseNo())).findFirst().ifPresent(c -> coursePlanVo.setCourseName(c.getCourseName()));
            coursePlanVos.add(coursePlanVo);
        });

        return ServerResponse.ofSuccess(coursePlanVos);
    }
}
