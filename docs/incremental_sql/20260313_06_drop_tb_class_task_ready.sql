USE `course_arrange_v2`;

START TRANSACTION;

-- 步骤1：备份旧排课任务表
CREATE TABLE IF NOT EXISTS `bak_tb_class_task_20260313` AS
SELECT *
FROM `tb_class_task`;

-- 步骤2：删除旧排课任务表
DROP TABLE IF EXISTS `tb_class_task`;

COMMIT;
