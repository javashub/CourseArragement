INSERT INTO cfg_time_slot (
  schedule_rule_id,
  weekday_no,
  period_no,
  period_name,
  time_group,
  start_time_text,
  end_time_text,
  is_teaching,
  is_fixed_break,
  sort_no,
  created_by,
  updated_by,
  created_at,
  updated_at,
  deleted,
  remark
)
SELECT
  1 AS schedule_rule_id,
  seed.weekday_no,
  seed.period_no,
  CONCAT('第', seed.period_no, '节') AS period_name,
  CASE
    WHEN seed.period_no <= 4 THEN 'MORNING'
    ELSE 'AFTERNOON'
  END AS time_group,
  CASE seed.period_no
    WHEN 1 THEN '08:00'
    WHEN 2 THEN '08:55'
    WHEN 3 THEN '10:00'
    WHEN 4 THEN '10:55'
    WHEN 5 THEN '14:00'
  END AS start_time_text,
  CASE seed.period_no
    WHEN 1 THEN '08:45'
    WHEN 2 THEN '09:40'
    WHEN 3 THEN '10:45'
    WHEN 4 THEN '11:40'
    WHEN 5 THEN '14:45'
  END AS end_time_text,
  1 AS is_teaching,
  0 AS is_fixed_break,
  ((seed.weekday_no - 1) * 5 + seed.period_no) AS sort_no,
  NULL AS created_by,
  NULL AS updated_by,
  NOW() AS created_at,
  NOW() AS updated_at,
  0 AS deleted,
  'auto-fill-25-grid' AS remark
FROM (
  SELECT 1 AS weekday_no, 1 AS period_no
  UNION ALL SELECT 1, 2
  UNION ALL SELECT 1, 3
  UNION ALL SELECT 1, 4
  UNION ALL SELECT 1, 5
  UNION ALL SELECT 2, 1
  UNION ALL SELECT 2, 2
  UNION ALL SELECT 2, 3
  UNION ALL SELECT 2, 4
  UNION ALL SELECT 2, 5
  UNION ALL SELECT 3, 1
  UNION ALL SELECT 3, 2
  UNION ALL SELECT 3, 3
  UNION ALL SELECT 3, 4
  UNION ALL SELECT 3, 5
  UNION ALL SELECT 4, 1
  UNION ALL SELECT 4, 2
  UNION ALL SELECT 4, 3
  UNION ALL SELECT 4, 4
  UNION ALL SELECT 4, 5
  UNION ALL SELECT 5, 1
  UNION ALL SELECT 5, 2
  UNION ALL SELECT 5, 3
  UNION ALL SELECT 5, 4
  UNION ALL SELECT 5, 5
) seed
LEFT JOIN cfg_time_slot existing
  ON existing.deleted = 0
 AND existing.schedule_rule_id = 1
 AND existing.weekday_no = seed.weekday_no
 AND existing.period_no = seed.period_no
WHERE existing.id IS NULL;
