-- 行政班排课联调数据修复
-- 目标：
-- 1. 消除 2025-2026-1 学期中教师 T26031 的固定时间冲突。
-- 2. 保持原有“周一/周三/周五下午固定课”规律，只做最小位移。
-- 3. 将每个冲突日中的一条任务从第 7 节调整到第 6 节。

UPDATE sch_task
SET fixed_period_no = 6,
    updated_at = NOW()
WHERE deleted = 0
  AND task_code IN ('SE26TK035', 'SE26TK045', 'SE26TK030');
