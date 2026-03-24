-- =====================================================================
-- 增量 SQL：初始化排课配置与时间片示例数据
-- 执行时间：2026-03-11
-- 说明：
-- 1. 用于让 /api/config/* 接口在新库中有可读取的基础数据
-- 2. 后续如果管理员页面保存配置，可继续沿用这套表
-- =====================================================================

USE `course_arrange_v2`;

CREATE TABLE IF NOT EXISTS `cfg_schedule_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_code` varchar(32) NOT NULL COMMENT '配置编码',
  `rule_name` varchar(64) NOT NULL COMMENT '配置名称',
  `campus_id` bigint DEFAULT NULL COMMENT '作用校区ID',
  `college_id` bigint DEFAULT NULL COMMENT '作用学院ID',
  `stage_id` bigint DEFAULT NULL COMMENT '作用学段ID',
  `term_id` bigint DEFAULT NULL COMMENT '作用学期ID',
  `week_days` int NOT NULL DEFAULT 5 COMMENT '每周上课天数',
  `day_periods` int NOT NULL DEFAULT 8 COMMENT '每天总节数',
  `morning_periods` int NOT NULL DEFAULT 4 COMMENT '上午节数',
  `afternoon_periods` int NOT NULL DEFAULT 4 COMMENT '下午节数',
  `night_periods` int NOT NULL DEFAULT 0 COMMENT '晚上节数',
  `allow_weekend` tinyint NOT NULL DEFAULT 0 COMMENT '是否允许周末排课',
  `default_continuous_limit` int NOT NULL DEFAULT 2 COMMENT '默认最大连堂数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认规则',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cfg_schedule_rule_code` (`rule_code`),
  KEY `idx_cfg_schedule_rule_scope` (`campus_id`, `college_id`, `stage_id`, `term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课规则配置表';

CREATE TABLE IF NOT EXISTS `cfg_time_slot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `schedule_rule_id` bigint NOT NULL COMMENT '排课规则ID',
  `weekday_no` int NOT NULL COMMENT '星期序号：1-7',
  `period_no` int NOT NULL COMMENT '节次序号',
  `period_name` varchar(32) NOT NULL COMMENT '节次名称',
  `time_group` varchar(32) NOT NULL COMMENT '时间分组：MORNING/AFTERNOON/NIGHT',
  `start_time_text` varchar(8) NOT NULL COMMENT '开始时间，格式HH:mm',
  `end_time_text` varchar(8) NOT NULL COMMENT '结束时间，格式HH:mm',
  `is_teaching` tinyint NOT NULL DEFAULT 1 COMMENT '是否可上课',
  `is_fixed_break` tinyint NOT NULL DEFAULT 0 COMMENT '是否固定休息时间',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cfg_time_slot_unique` (`schedule_rule_id`, `weekday_no`, `period_no`),
  KEY `idx_cfg_time_slot_rule` (`schedule_rule_id`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间片配置表';

CREATE TABLE IF NOT EXISTS `cfg_feature_toggle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `toggle_code` varchar(64) NOT NULL COMMENT '开关编码',
  `toggle_name` varchar(64) NOT NULL COMMENT '开关名称',
  `campus_id` bigint DEFAULT NULL COMMENT '作用校区ID',
  `college_id` bigint DEFAULT NULL COMMENT '作用学院ID',
  `stage_id` bigint DEFAULT NULL COMMENT '作用学段ID',
  `term_id` bigint DEFAULT NULL COMMENT '作用学期ID',
  `toggle_value` varchar(255) NOT NULL COMMENT '开关值',
  `value_type` varchar(32) NOT NULL DEFAULT 'BOOLEAN' COMMENT '值类型',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cfg_feature_toggle_scope` (`toggle_code`, `campus_id`, `college_id`, `stage_id`, `term_id`),
  KEY `idx_cfg_feature_toggle_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能开关配置表';

INSERT INTO `cfg_schedule_rule`
(`rule_code`, `rule_name`, `campus_id`, `college_id`, `stage_id`, `term_id`, `week_days`, `day_periods`,
 `morning_periods`, `afternoon_periods`, `night_periods`, `allow_weekend`, `default_continuous_limit`,
 `status`, `is_default`, `remark`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`)
VALUES
('DEFAULT_RULE', '默认排课规则', NULL, NULL, NULL, NULL, 5, 8, 4, 4, 0, 0, 2, 1, 1,
 '系统初始化默认规则', 0, 0, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
`rule_name` = VALUES(`rule_name`),
`week_days` = VALUES(`week_days`),
`day_periods` = VALUES(`day_periods`),
`morning_periods` = VALUES(`morning_periods`),
`afternoon_periods` = VALUES(`afternoon_periods`),
`night_periods` = VALUES(`night_periods`),
`allow_weekend` = VALUES(`allow_weekend`),
`default_continuous_limit` = VALUES(`default_continuous_limit`),
`status` = VALUES(`status`),
`is_default` = VALUES(`is_default`),
`remark` = VALUES(`remark`),
`updated_at` = NOW();

INSERT INTO `cfg_time_slot`
(`schedule_rule_id`, `weekday_no`, `period_no`, `period_name`, `time_group`, `start_time_text`, `end_time_text`,
 `is_teaching`, `is_fixed_break`, `sort_no`, `remark`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`)
SELECT r.id, t.weekday_no, t.period_no, t.period_name, t.time_group, t.start_time_text, t.end_time_text,
       1, 0, t.sort_no, '系统初始化时间片', 0, 0, NOW(), NOW(), 0
FROM (
         SELECT 1 AS weekday_no, 1 AS period_no, '第1节' AS period_name, 'MORNING' AS time_group, '08:00' AS start_time_text, '08:45' AS end_time_text, 1 AS sort_no
         UNION ALL SELECT 1, 2, '第2节', 'MORNING', '08:55', '09:40', 2
         UNION ALL SELECT 1, 3, '第3节', 'MORNING', '10:00', '10:45', 3
         UNION ALL SELECT 1, 4, '第4节', 'MORNING', '10:55', '11:40', 4
         UNION ALL SELECT 1, 5, '第5节', 'AFTERNOON', '14:00', '14:45', 5
         UNION ALL SELECT 1, 6, '第6节', 'AFTERNOON', '14:55', '15:40', 6
         UNION ALL SELECT 1, 7, '第7节', 'AFTERNOON', '16:00', '16:45', 7
         UNION ALL SELECT 1, 8, '第8节', 'AFTERNOON', '16:55', '17:40', 8
     ) t
         CROSS JOIN `cfg_schedule_rule` r
WHERE r.rule_code = 'DEFAULT_RULE'
  AND NOT EXISTS (
    SELECT 1
    FROM `cfg_time_slot` s
    WHERE s.schedule_rule_id = r.id
      AND s.weekday_no = t.weekday_no
      AND s.period_no = t.period_no
      AND s.deleted = 0
);
