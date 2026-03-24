USE `course_arrange_v2`;

CREATE TABLE IF NOT EXISTS `tb_course_plan_adjust_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_plan_id` int NOT NULL COMMENT '课表记录ID',
  `semester` varchar(32) DEFAULT NULL COMMENT '学期',
  `grade_no` varchar(32) DEFAULT NULL COMMENT '年级编号',
  `class_no` varchar(32) DEFAULT NULL COMMENT '班级编号',
  `course_no` varchar(32) DEFAULT NULL COMMENT '课程编号',
  `teacher_no` varchar(32) DEFAULT NULL COMMENT '教师编号',
  `before_class_time` varchar(16) DEFAULT NULL COMMENT '调整前时间片',
  `after_class_time` varchar(16) DEFAULT NULL COMMENT '调整后时间片',
  `before_classroom_no` varchar(32) DEFAULT NULL COMMENT '调整前教室编号',
  `after_classroom_no` varchar(32) DEFAULT NULL COMMENT '调整后教室编号',
  `operator_user_id` bigint DEFAULT NULL COMMENT '操作人用户ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `operator_type` varchar(32) DEFAULT NULL COMMENT '操作人类型',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_adjust_log_plan_id` (`course_plan_id`),
  KEY `idx_adjust_log_class_no` (`class_no`),
  KEY `idx_adjust_log_teacher_no` (`teacher_no`),
  KEY `idx_adjust_log_semester` (`semester`),
  KEY `idx_adjust_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课表调课日志表';
