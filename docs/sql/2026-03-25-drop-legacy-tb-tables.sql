-- 与 docs/incremental_sql/20260325_02_drop_legacy_tb_tables.sql 保持一致
-- 用于手工执行旧表清理。

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
