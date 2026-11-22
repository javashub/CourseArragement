-- 目的：
-- 1. 修正 2025-2026-1 学期测试任务中“需要特殊教室”被错误写成“固定具体教室”的脏数据
-- 2. 保留特殊教室类型约束，移除不合理的 fixed_room_id 绑定，让排课算法按房间类型自动分配
--
-- 背景：
-- - 当前一批 seed 任务里，LAB / COMPUTER / SPORTS 课程大量写入了 need_fixed_room=1
-- - 其中部分 fixed_room_id 指向已不存在的教室，部分指向了错误类型的教室
-- - 算法匹配时会优先按 fixed_room_id 过滤，导致候选教室直接为空，最终报 SPECIAL_ROOM_UNAVAILABLE

UPDATE sch_task t
JOIN res_course c ON c.id = t.course_id
SET t.need_fixed_room = 0,
    t.fixed_room_id = NULL,
    t.updated_at = NOW()
WHERE t.deleted = 0
  AND c.deleted = 0
  AND t.remark LIKE '%semester=2025-2026-1%'
  AND c.need_special_room = 1
  AND t.need_fixed_room = 1;
