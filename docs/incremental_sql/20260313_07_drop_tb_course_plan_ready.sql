USE `course_arrange_v2`;

START TRANSACTION;

-- 步骤1：备份旧课表结果表
CREATE TABLE IF NOT EXISTS `bak_tb_course_plan_20260313` AS
SELECT *
FROM `tb_course_plan`;

-- 步骤2：删除旧课表结果表
DROP TABLE IF EXISTS `tb_course_plan`;

COMMIT;
