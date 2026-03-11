package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.StudyLogsDao;
import com.lyk.coursearrange.entity.StudyLogs;
import com.lyk.coursearrange.service.StudyLogsService;
import org.springframework.stereotype.Service;

@Service
public class StudyLogsServiceImpl extends ServiceImpl<StudyLogsDao, StudyLogs> implements StudyLogsService {
}
