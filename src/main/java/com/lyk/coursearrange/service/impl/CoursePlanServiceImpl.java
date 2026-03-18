package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.entity.response.CoursePlanVo;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.service.CoursePlanAdjustLogService;
import com.lyk.coursearrange.service.CoursePlanService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author lequal
 * @since 2020-04-15
 */
@Slf4j
@Service
public class CoursePlanServiceImpl implements CoursePlanService {

    @Resource
    private CoursePlanAdjustLogService coursePlanAdjustLogService;
    @Resource
    private AuthFacadeService authFacadeService;
    @Resource
    private ScheduleLogMirrorService scheduleLogMirrorService;
    @Resource
    private SchScheduleResultService schScheduleResultService;
    @Resource
    private SchTaskService schTaskService;
    @Resource
    private ResClassroomService resClassroomService;
    @Resource
    private CoursePlanLegacySupport coursePlanLegacySupport;

    /**
     * 根据班级编号查询课表
     * @param classNo 班级编号
     */
    @Override
    public ServerResponse queryCoursePlanByClassNo(String classNo, String semester) {
        List<CoursePlanVo> standardPlans = listStandardPlans(semester, classNo, null);
        if (!standardPlans.isEmpty()) {
            return ServerResponse.ofSuccess(standardPlans);
        }
        return ServerResponse.ofError("该班级没有课表");
    }

    @Override
    public ServerResponse queryCoursePlanByTeacherNo(String teacherNo, String semester) {
        List<CoursePlanVo> standardPlans = listStandardPlans(semester, null, teacherNo);
        if (!standardPlans.isEmpty()) {
            return ServerResponse.ofSuccess(standardPlans);
        }
        return ServerResponse.ofError("该教师没有课表");
    }

    @Override
    public ServerResponse adjustCoursePlan(CoursePlanAdjustRequest request) {
        if (request.getStandardResultId() != null) {
            return adjustStandardCoursePlan(request);
        }
        if (request.getId() != null) {
            SchScheduleResult standardResult = schScheduleResultService.getById(request.getId().longValue());
            if (standardResult != null) {
                request.setStandardResultId(standardResult.getId());
                return adjustStandardCoursePlan(request);
            }
        }
        CoursePlan coursePlan = coursePlanLegacySupport.getById(request.getId());
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
        boolean updated = coursePlanLegacySupport.updateById(coursePlan);
        if (updated) {
            scheduleLogMirrorService.syncAdjustedPlan(coursePlan, beforeClassTime);
            saveAdjustLog(coursePlan, beforeClassTime, beforeClassroomNo, targetClassTime, targetClassroomNo);
            return ServerResponse.ofSuccess("调课成功", coursePlan);
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "调课失败，请稍后重试");
    }

    private ServerResponse adjustStandardCoursePlan(CoursePlanAdjustRequest request) {
        SchScheduleResult result = schScheduleResultService.getById(request.getStandardResultId());
        if (result == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标准课表记录不存在");
        }
        if (request.getClassTime() == null || request.getClassTime().isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "目标时间片不能为空");
        }

        SchTask task = schTaskService.getById(result.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课表对应的排课任务不存在");
        }
        Map<String, String> taskMeta = ScheduleTaskMetaUtils.parseTaskRemark(task.getRemark());
        String semester = result.getRemark();
        String targetClassTime = request.getClassTime();
        Integer afterWeekdayNo = parseWeekdayNo(targetClassTime);
        Integer afterPeriodNo = parsePeriodNo(targetClassTime);
        if (afterWeekdayNo == null || afterPeriodNo == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "目标时间片格式不正确");
        }
        String conflictMessage = validateStandardAdjustConflict(result, taskMeta, semester, afterWeekdayNo, afterPeriodNo);
        if (conflictMessage != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, conflictMessage);
        }

        Integer beforeWeekdayNo = result.getWeekdayNo();
        Integer beforePeriodNo = result.getPeriodNo();
        result.setWeekdayNo(afterWeekdayNo);
        result.setPeriodNo(afterPeriodNo);
        result.setSourceType("DRAG");
        boolean updated = schScheduleResultService.updateById(result);
        if (!updated) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "调课失败，请稍后重试");
        }

        CoursePlan legacyPlan = findLegacyPlanByStandard(result, taskMeta, semester, beforeWeekdayNo, beforePeriodNo);
        if (legacyPlan != null) {
            legacyPlan.setClassTime(targetClassTime);
            if (request.getClassroomNo() != null && !request.getClassroomNo().isBlank()) {
                legacyPlan.setClassroomNo(request.getClassroomNo());
            }
            coursePlanLegacySupport.updateById(legacyPlan);
        }
        saveAdjustLog(result, taskMeta, beforeWeekdayNo, beforePeriodNo, afterWeekdayNo, afterPeriodNo, legacyPlan);
        return ServerResponse.ofSuccess("调课成功");
    }

    private String validateAdjustConflict(CoursePlan currentPlan, String targetClassTime, String targetClassroomNo) {
        LambdaQueryWrapper<CoursePlan> classWrapper = buildAdjustConflictWrapper(currentPlan, targetClassTime)
                .eq(CoursePlan::getClassNo, currentPlan.getClassNo());
        if (coursePlanLegacySupport.count(classWrapper) > 0) {
            return "目标时间片已存在同班级课程，不能调课";
        }

        LambdaQueryWrapper<CoursePlan> teacherWrapper = buildAdjustConflictWrapper(currentPlan, targetClassTime)
                .eq(CoursePlan::getTeacherNo, currentPlan.getTeacherNo());
        if (coursePlanLegacySupport.count(teacherWrapper) > 0) {
            return "目标时间片教师已有其他课程，不能调课";
        }

        if (targetClassroomNo != null && !targetClassroomNo.isBlank()) {
            LambdaQueryWrapper<CoursePlan> classroomWrapper = buildAdjustConflictWrapper(currentPlan, targetClassTime)
                    .eq(CoursePlan::getClassroomNo, targetClassroomNo);
            if (coursePlanLegacySupport.count(classroomWrapper) > 0) {
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

    @Override
    public List<String> listOccupiedClassroomNos(String teachbuildNo) {
        try {
            List<SchScheduleResult> standardResults = schScheduleResultService.list(new LambdaQueryWrapper<SchScheduleResult>()
                    .eq(SchScheduleResult::getDeleted, 0)
                    .isNotNull(SchScheduleResult::getClassroomId));
            if (!standardResults.isEmpty()) {
                List<Long> classroomIds = standardResults.stream()
                        .map(SchScheduleResult::getClassroomId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
                if (classroomIds.isEmpty()) {
                    return List.of();
                }
                return resClassroomService.listByIds(classroomIds).stream()
                        .map(ResClassroom::getClassroomCode)
                        .filter(code -> code != null && code.startsWith(teachbuildNo))
                        .distinct()
                        .toList();
            }
        } catch (Exception exception) {
            log.warn("查询标准课表占用教室失败，将回退 legacy 副本，teachbuildNo={}", teachbuildNo, exception);
        }
        return coursePlanLegacySupport.listAll().stream()
                .map(CoursePlan::getClassroomNo)
                .filter(code -> code != null && code.startsWith(teachbuildNo))
                .distinct()
                .toList();
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

    private void saveAdjustLog(SchScheduleResult result,
                               Map<String, String> taskMeta,
                               Integer beforeWeekdayNo,
                               Integer beforePeriodNo,
                               Integer afterWeekdayNo,
                               Integer afterPeriodNo,
                               CoursePlan legacyPlan) {
        CoursePlanAdjustLog logEntity = new CoursePlanAdjustLog();
        logEntity.setCoursePlanId(legacyPlan == null ? null : legacyPlan.getId());
        logEntity.setSemester(result.getRemark());
        logEntity.setGradeNo(taskMeta.getOrDefault("gradeNo", ""));
        logEntity.setClassNo(taskMeta.getOrDefault("classNo", ""));
        logEntity.setCourseNo(taskMeta.getOrDefault("courseNo", ""));
        logEntity.setTeacherNo(taskMeta.getOrDefault("teacherNo", ""));
        logEntity.setBeforeClassTime(toLegacyClassTime(beforeWeekdayNo, beforePeriodNo));
        logEntity.setAfterClassTime(toLegacyClassTime(afterWeekdayNo, afterPeriodNo));
        logEntity.setBeforeClassroomNo(legacyPlan == null ? null : legacyPlan.getClassroomNo());
        logEntity.setAfterClassroomNo(legacyPlan == null ? null : legacyPlan.getClassroomNo());
        logEntity.setRemark("拖拽调课");
        fillAdjustOperator(logEntity, legacyPlan == null ? result.getId().intValue() : legacyPlan.getId());
        coursePlanAdjustLogService.save(logEntity);
        scheduleLogMirrorService.mirrorAdjustLog(logEntity);
    }

    private void fillAdjustOperator(CoursePlanAdjustLog logEntity, Integer planId) {
        try {
            CurrentUserVO currentUser = authFacadeService.getCurrentUserView();
            if (currentUser != null) {
                logEntity.setOperatorUserId(currentUser.getUserId());
                logEntity.setOperatorName(currentUser.getDisplayName());
                logEntity.setOperatorType(currentUser.getUserType());
            }
        } catch (Exception exception) {
            log.warn("记录调课日志时获取当前用户失败，planId={}", planId, exception);
        }
    }

    private String validateStandardAdjustConflict(SchScheduleResult currentResult,
                                                  Map<String, String> currentMeta,
                                                  String semester,
                                                  Integer targetWeekdayNo,
                                                  Integer targetPeriodNo) {
        LambdaQueryWrapper<SchScheduleResult> wrapper = new LambdaQueryWrapper<SchScheduleResult>()
                .eq(SchScheduleResult::getDeleted, 0)
                .eq(SchScheduleResult::getRemark, semester)
                .eq(SchScheduleResult::getWeekdayNo, targetWeekdayNo)
                .eq(SchScheduleResult::getPeriodNo, targetPeriodNo)
                .ne(SchScheduleResult::getId, currentResult.getId());
        List<SchScheduleResult> conflicts = schScheduleResultService.list(wrapper);
        if (conflicts.isEmpty()) {
            return null;
        }
        List<Long> taskIds = conflicts.stream()
                .map(SchScheduleResult::getTaskId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        Map<Long, SchTask> taskMap = schTaskService.listByIds(taskIds).stream()
                .collect(Collectors.toMap(SchTask::getId, item -> item));
        for (SchScheduleResult conflict : conflicts) {
            Map<String, String> meta = ScheduleTaskMetaUtils.parseTaskRemark(
                    taskMap.get(conflict.getTaskId()) == null ? "" : taskMap.get(conflict.getTaskId()).getRemark()
            );
            if (currentMeta.getOrDefault("classNo", "").equals(meta.getOrDefault("classNo", ""))) {
                return "目标时间片已存在同班级课程，不能调课";
            }
            if (currentMeta.getOrDefault("teacherNo", "").equals(meta.getOrDefault("teacherNo", ""))) {
                return "目标时间片教师已有其他课程，不能调课";
            }
        }
        return null;
    }

    private CoursePlan findLegacyPlanByStandard(SchScheduleResult result,
                                                Map<String, String> taskMeta,
                                                String semester,
                                                Integer beforeWeekdayNo,
                                                Integer beforePeriodNo) {
        LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<CoursePlan>()
                .eq(CoursePlan::getSemester, semester)
                .eq(CoursePlan::getClassNo, taskMeta.getOrDefault("classNo", ""))
                .eq(CoursePlan::getCourseNo, taskMeta.getOrDefault("courseNo", ""))
                .eq(CoursePlan::getTeacherNo, taskMeta.getOrDefault("teacherNo", ""))
                .eq(beforeWeekdayNo != null && beforePeriodNo != null, CoursePlan::getClassTime, toLegacyClassTime(beforeWeekdayNo, beforePeriodNo))
                .last("limit 1");
        return coursePlanLegacySupport.getOne(wrapper);
    }

    private List<CoursePlanVo> listStandardPlans(String semester, String classNo, String teacherNo) {
        LambdaQueryWrapper<SchScheduleResult> resultWrapper = new LambdaQueryWrapper<>();
        resultWrapper.eq(SchScheduleResult::getDeleted, 0)
                .eq(semester != null && !semester.isBlank(), SchScheduleResult::getRemark, semester)
                .orderByAsc(SchScheduleResult::getWeekdayNo)
                .orderByAsc(SchScheduleResult::getPeriodNo);
        List<SchScheduleResult> results = schScheduleResultService.list(resultWrapper);
        if (results.isEmpty()) {
            return List.of();
        }
        List<Long> taskIds = results.stream()
                .map(SchScheduleResult::getTaskId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (taskIds.isEmpty()) {
            return List.of();
        }
        List<SchTask> tasks = schTaskService.listByIds(taskIds);
        Map<Long, SchTask> taskMap = new HashMap<>();
        for (SchTask task : tasks) {
            taskMap.put(task.getId(), task);
        }
        Map<Long, String> classroomCodeMap = buildClassroomCodeMap(results);
        return results.stream()
                .map(item -> buildStandardPlanVo(item, taskMap.get(item.getTaskId()), classroomCodeMap))
                .filter(item -> item != null)
                .filter(item -> classNo == null || classNo.isBlank() || classNo.equals(item.getClassNo()))
                .filter(item -> teacherNo == null || teacherNo.isBlank() || teacherNo.equals(item.getTeacherNo()))
                .toList();
    }

    private Map<Long, String> buildClassroomCodeMap(List<SchScheduleResult> results) {
        List<Long> classroomIds = results.stream()
                .map(SchScheduleResult::getClassroomId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (classroomIds.isEmpty()) {
            return Map.of();
        }
        return resClassroomService.listByIds(classroomIds).stream()
                .collect(Collectors.toMap(ResClassroom::getId, ResClassroom::getClassroomCode, (left, right) -> left));
    }

    private CoursePlanVo buildStandardPlanVo(SchScheduleResult result, SchTask task, Map<Long, String> classroomCodeMap) {
        if (task == null) {
            return null;
        }
        Map<String, String> taskMeta = parseTaskRemark(task.getRemark());
        String classNo = taskMeta.getOrDefault("classNo", "");
        String courseNo = taskMeta.getOrDefault("courseNo", "");
        String teacherNo = taskMeta.getOrDefault("teacherNo", "");
        String courseName = taskMeta.getOrDefault("courseName", courseNo);
        String teacherName = taskMeta.getOrDefault("teacherName", teacherNo);
        CoursePlanVo vo = new CoursePlanVo();
        vo.setId(result.getId() == null ? null : result.getId().intValue());
        vo.setStandardResultId(result.getId());
        vo.setSemester(result.getRemark());
        vo.setClassNo(classNo);
        vo.setCourseNo(courseNo);
        vo.setTeacherNo(teacherNo);
        vo.setCourseName(courseName);
        vo.setRealname(teacherName);
        vo.setGradeNo(taskMeta.getOrDefault("gradeNo", ""));
        vo.setClassroomNo(classroomCodeMap.getOrDefault(result.getClassroomId(), ""));
        vo.setClassTime(toLegacyClassTime(result.getWeekdayNo(), result.getPeriodNo()));
        return vo;
    }

    private Map<String, String> parseTaskRemark(String remark) {
        return ScheduleTaskMetaUtils.parseTaskRemark(remark);
    }

    private Integer parseWeekdayNo(String classTime) {
        return ScheduleTaskMetaUtils.resolveWeekdayNo(classTime);
    }

    private Integer parsePeriodNo(String classTime) {
        return ScheduleTaskMetaUtils.resolvePeriodNo(classTime);
    }

    private String toLegacyClassTime(Integer weekdayNo, Integer periodNo) {
        if (weekdayNo == null || periodNo == null) {
            return "";
        }
        return String.format("%02d", (weekdayNo - 1) * 5 + periodNo);
    }

}
