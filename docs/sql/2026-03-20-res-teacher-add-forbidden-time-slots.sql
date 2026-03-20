-- 教师禁排时间独立字段增量脚本
-- 执行日期：2026-03-20
-- 适用库：course_arrange_v2

ALTER TABLE `res_teacher`
  ADD COLUMN `forbidden_time_slots` varchar(255) DEFAULT NULL COMMENT '教师禁排时间编码，逗号分隔'
  AFTER `max_day_hours`;

-- 兼容上一轮把禁排时间临时写入 remark JSON 的数据
UPDATE `res_teacher`
SET
  `forbidden_time_slots` = NULLIF(
    REPLACE(
      REPLACE(
        REPLACE(
          REPLACE(JSON_UNQUOTE(JSON_EXTRACT(`remark`, '$.forbiddenTimeSlots')), '[', ''),
        ']', ''),
      '"', ''),
    ' ', ''),
  ''),
  `remark` = NULLIF(JSON_UNQUOTE(JSON_EXTRACT(`remark`, '$.teach')), '')
WHERE `remark` IS NOT NULL
  AND JSON_VALID(`remark`) = 1;
