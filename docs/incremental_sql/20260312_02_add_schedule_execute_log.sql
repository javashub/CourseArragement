USE `course_arrange_v2`;

CREATE TABLE IF NOT EXISTS `tb_schedule_execute_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `semester` varchar(32) NOT NULL COMMENT '学期',
  `task_count` int NOT NULL DEFAULT 0 COMMENT '参与排课的任务数',
  `generated_plan_count` int NOT NULL DEFAULT 0 COMMENT '生成课表数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '执行状态：0失败，1成功',
  `duration_ms` bigint NOT NULL DEFAULT 0 COMMENT '执行耗时（毫秒）',
  `message` varchar(500) DEFAULT NULL COMMENT '执行结果信息',
  `operator_user_id` bigint DEFAULT NULL COMMENT '执行人用户ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '执行人名称',
  `operator_type` varchar(32) DEFAULT NULL COMMENT '执行人类型',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_execute_semester` (`semester`),
  KEY `idx_schedule_execute_status` (`status`),
  KEY `idx_schedule_execute_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课执行日志表';
