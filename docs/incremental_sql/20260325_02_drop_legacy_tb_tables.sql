-- =====================================================================
-- 删除已完成退役的 legacy tb_* 表
-- 说明：
-- 1. 生成依据：2026-03-25 在 mysql8/course_arrange_v2 实际执行 `SHOW TABLES LIKE 'tb\\_%'`
-- 2. 当前代码主链已不再依赖以下旧表
-- 3. 执行前请确认数据库已备份
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `tb_schedule_execute_log`;
DROP TABLE IF EXISTS `tb_course_plan_adjust_log`;
DROP TABLE IF EXISTS `tb_class_info`;
DROP TABLE IF EXISTS `tb_classroom`;
DROP TABLE IF EXISTS `tb_teach_build_info`;
DROP TABLE IF EXISTS `tb_course_info`;
DROP TABLE IF EXISTS `tb_admin`;
DROP TABLE IF EXISTS `tb_teacher`;
DROP TABLE IF EXISTS `tb_student`;
DROP TABLE IF EXISTS `tb_online_category`;
DROP TABLE IF EXISTS `tb_online_course`;

SET FOREIGN_KEY_CHECKS = 1;
