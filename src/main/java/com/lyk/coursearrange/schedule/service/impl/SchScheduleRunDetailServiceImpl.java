package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunDetail;
import com.lyk.coursearrange.schedule.mapper.SchScheduleRunDetailMapper;
import com.lyk.coursearrange.schedule.service.SchScheduleRunDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 排课执行失败任务明细服务实现。
 */
@Service
public class SchScheduleRunDetailServiceImpl extends ServiceImpl<SchScheduleRunDetailMapper, SchScheduleRunDetail>
        implements SchScheduleRunDetailService {

    @Override
    public void replaceRunDetails(Long runLogId, List<UnscheduledTaskDetail> details) {
        remove(new LambdaQueryWrapper<SchScheduleRunDetail>()
                .eq(SchScheduleRunDetail::getRunLogId, runLogId));
        if (details == null || details.isEmpty()) {
            return;
        }
        details.stream()
                .filter(Objects::nonNull)
                .map(detail -> {
                    SchScheduleRunDetail entity = new SchScheduleRunDetail();
                    entity.setRunLogId(runLogId);
                    entity.setTaskId(detail.getTaskId());
                    entity.setTaskCode(detail.getTaskCode());
                    entity.setClassNo(detail.getClassNo());
                    entity.setCourseNo(detail.getCourseNo());
                    entity.setTeacherNo(detail.getTeacherNo());
                    entity.setReasonCode(detail.getReasonCode());
                    entity.setReasonMessage(detail.getReasonMessage());
                    return entity;
                })
                .forEach(this::save);
    }

    @Override
    public List<UnscheduledTaskDetail> listByRunLogId(Long runLogId) {
        return list(new LambdaQueryWrapper<SchScheduleRunDetail>()
                .eq(SchScheduleRunDetail::getRunLogId, runLogId)
                .eq(SchScheduleRunDetail::getDeleted, 0)
                .orderByAsc(SchScheduleRunDetail::getId))
                .stream()
                .map(entity -> UnscheduledTaskDetail.builder()
                        .taskId(entity.getTaskId())
                        .taskCode(entity.getTaskCode())
                        .classNo(entity.getClassNo())
                        .courseNo(entity.getCourseNo())
                        .teacherNo(entity.getTeacherNo())
                        .reasonCode(entity.getReasonCode())
                        .reasonMessage(entity.getReasonMessage())
                        .build())
                .toList();
    }
}
