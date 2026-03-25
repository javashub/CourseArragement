package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.entity.response.CoursePlanVo;
import com.lyk.coursearrange.schedule.entity.SchScheduleAdjustLog;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleAdjustLogService;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.service.CoursePlanService;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import com.lyk.coursearrange.schedule.vo.ScheduleAdjustLogVO;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
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
    private SchScheduleAdjustLogService schScheduleAdjustLogService;
    @Resource
    private AuthFacadeService authFacadeService;
    @Resource
    private SchScheduleResultService schScheduleResultService;
    @Resource
    private SchTaskService schTaskService;
    @Resource
    private ResClassroomService resClassroomService;
    @Resource
    private SysUserService sysUserService;

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
        throw new BusinessException(ResultCode.NOT_FOUND, "标准课表记录不存在");
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

        saveAdjustLog(result, taskMeta, beforeWeekdayNo, beforePeriodNo, afterWeekdayNo, afterPeriodNo);
        return ServerResponse.ofSuccess("调课成功");
    }

    @Override
    public List<ScheduleAdjustLogVO> listRecentAdjustLogs(String semester, String classNo, String teacherNo, Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        List<SchScheduleAdjustLog> adjustLogs = schScheduleAdjustLogService.list(new LambdaQueryWrapper<SchScheduleAdjustLog>()
                .eq(SchScheduleAdjustLog::getDeleted, 0)
                .eq(semester != null && !semester.isBlank(), SchScheduleAdjustLog::getRemark, semester)
                .orderByDesc(SchScheduleAdjustLog::getCreatedAt)
                .last("limit " + Math.max(safeLimit * 5, safeLimit)));
        if (adjustLogs.isEmpty()) {
            return List.of();
        }
        Map<Long, SchScheduleResult> resultMap = schScheduleResultService.listByIds(adjustLogs.stream()
                        .map(SchScheduleAdjustLog::getSourceResultId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()).stream()
                .collect(Collectors.toMap(SchScheduleResult::getId, item -> item, (left, right) -> left));
        Map<Long, SchTask> taskMap = schTaskService.listByIds(resultMap.values().stream()
                        .map(SchScheduleResult::getTaskId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()).stream()
                .collect(Collectors.toMap(SchTask::getId, item -> item, (left, right) -> left));
        Map<Long, String> operatorNameMap = buildOperatorNameMap(adjustLogs.stream()
                .map(SchScheduleAdjustLog::getOperatorUserId)
                .filter(Objects::nonNull)
                .toList());
        return adjustLogs.stream()
                .map(item -> toAdjustLogVO(item, resultMap.get(item.getSourceResultId()), taskMap, operatorNameMap))
                .filter(Objects::nonNull)
                .filter(item -> classNo == null || classNo.isBlank() || classNo.equals(item.getClassNo()))
                .filter(item -> teacherNo == null || teacherNo.isBlank() || teacherNo.equals(item.getTeacherNo()))
                .limit(safeLimit)
                .toList();
    }

    @Override
    public List<String> listOccupiedClassroomNos(String buildingCode) {
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
                        .filter(code -> code != null && code.startsWith(buildingCode))
                        .distinct()
                        .toList();
            }
        } catch (Exception exception) {
            log.warn("查询标准课表占用教室失败，将返回空占用集合，buildingCode={}", buildingCode, exception);
        }
        return List.of();
    }

    private void saveAdjustLog(SchScheduleResult result,
                               Map<String, String> taskMeta,
                               Integer beforeWeekdayNo,
                               Integer beforePeriodNo,
                               Integer afterWeekdayNo,
                               Integer afterPeriodNo) {
        SchScheduleAdjustLog logEntity = new SchScheduleAdjustLog();
        logEntity.setAdjustCode("ADJ_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        logEntity.setTermId(0L);
        logEntity.setSourceResultId(result.getId());
        logEntity.setAdjustType("MOVE");
        logEntity.setBeforeWeekdayNo(beforeWeekdayNo);
        logEntity.setBeforePeriodNo(beforePeriodNo);
        logEntity.setAfterWeekdayNo(afterWeekdayNo);
        logEntity.setAfterPeriodNo(afterPeriodNo);
        logEntity.setAdjustReason(buildAdjustReason(taskMeta));
        logEntity.setStatus(1);
        logEntity.setRemark(result.getRemark());
        fillAdjustOperator(logEntity, result.getId());
        schScheduleAdjustLogService.save(logEntity);
    }

    private void fillAdjustOperator(SchScheduleAdjustLog logEntity, Long planId) {
        try {
            CurrentUserVO currentUser = authFacadeService.getCurrentUserView();
            if (currentUser != null) {
                logEntity.setOperatorUserId(currentUser.getUserId());
            }
        } catch (Exception exception) {
            log.warn("记录调课日志时获取当前用户失败，planId={}", planId, exception);
        }
    }

    private String buildAdjustReason(Map<String, String> taskMeta) {
        String courseName = taskMeta.getOrDefault("courseName", taskMeta.getOrDefault("courseNo", ""));
        String classNo = taskMeta.getOrDefault("classNo", "");
        return classNo.isBlank() ? "拖拽调课" : String.format("%s %s 拖拽调课", classNo, courseName).trim();
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
        vo.setTeacherName(teacherName);
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

    private ScheduleAdjustLogVO toAdjustLogVO(SchScheduleAdjustLog adjustLog,
                                              SchScheduleResult result,
                                              Map<Long, SchTask> taskMap,
                                              Map<Long, String> operatorNameMap) {
        if (adjustLog == null || result == null) {
            return null;
        }
        SchTask task = taskMap.get(result.getTaskId());
        Map<String, String> taskMeta = ScheduleTaskMetaUtils.parseTaskRemark(task == null ? "" : task.getRemark());
        ScheduleAdjustLogVO vo = new ScheduleAdjustLogVO();
        vo.setId(adjustLog.getId());
        vo.setClassNo(taskMeta.getOrDefault("classNo", ""));
        vo.setTeacherNo(taskMeta.getOrDefault("teacherNo", ""));
        vo.setOperatorName(resolveOperatorName(operatorNameMap, adjustLog.getOperatorUserId()));
        vo.setBeforeClassTime(toLegacyClassTime(adjustLog.getBeforeWeekdayNo(), adjustLog.getBeforePeriodNo()));
        vo.setAfterClassTime(toLegacyClassTime(adjustLog.getAfterWeekdayNo(), adjustLog.getAfterPeriodNo()));
        vo.setRemark(adjustLog.getAdjustReason());
        vo.setCreateTime(adjustLog.getCreatedAt());
        return vo;
    }

    private String resolveOperatorName(Map<Long, String> operatorNameMap, Long operatorUserId) {
        if (operatorNameMap == null || operatorNameMap.isEmpty() || operatorUserId == null) {
            return "--";
        }
        return operatorNameMap.getOrDefault(operatorUserId, "--");
    }

    private Map<Long, String> buildOperatorNameMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, this::resolveOperatorName, (left, right) -> left));
    }

    private String resolveOperatorName(SysUser user) {
        if (user == null) {
            return "--";
        }
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }

}
