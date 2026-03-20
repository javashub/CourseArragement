ALTER TABLE `tb_class_info`
ADD COLUMN `forbidden_time_slots` varchar(255) DEFAULT NULL COMMENT '班级禁排时间编码，逗号分隔' AFTER `remark`;
