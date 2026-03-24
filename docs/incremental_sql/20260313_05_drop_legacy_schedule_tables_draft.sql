USE `course_arrange_v2`;

-- 说明：
-- 1. 这是删除旧排课任务/旧课表表的草案脚本，当前先不要执行。
-- 2. 仅在以下条件全部满足后再执行：
--    2.1 前端任务页、课表页、排课执行链都已经不再依赖 tb_class_task / tb_course_plan
--    2.2 标准表 sch_task / sch_schedule_result 已完成全量迁移
--    2.3 你已经完成历史数据校验和备份
-- 3. 本脚本默认先备份旧表，再删除旧表。

START TRANSACTION;

-- 步骤1：备份旧排课任务表
CREATE TABLE IF NOT EXISTS `bak_tb_class_task_20260313` AS
SELECT *
FROM `tb_class_task`;

-- 步骤2：备份旧课表结果表
CREATE TABLE IF NOT EXISTS `bak_tb_course_plan_20260313` AS
SELECT *
FROM `tb_course_plan`;

-- 步骤3：如需保留调课日志与执行日志，可不删除以下日志表
-- DROP TABLE IF EXISTS `tb_course_plan_adjust_log`;
-- DROP TABLE IF EXISTS `tb_schedule_execute_log`;

-- 步骤4：删除旧任务与旧课表主表
DROP TABLE IF EXISTS `tb_class_task`;
DROP TABLE IF EXISTS `tb_course_plan`;

COMMIT;
