# legacy-table-cleanup Specification

## Purpose
TBD - created by archiving change remove-legacy-tb-table-dependencies. Update Purpose after archive.
## Requirements
### Requirement: Schedule and resource runtime SHALL rely on standard tables only
系统在排课任务、课表查询、基础资源、组织管理主链中 SHALL 只依赖标准表族 `org_* / res_* / sch_* / cfg_*`。排课运行时 MUST 不再读取、写入或回退到 `tb_class_info / tb_teacher / tb_student / tb_course_info / tb_classroom / tb_teach_build_info / tb_course_plan / tb_class_task` 等 legacy 表。

#### Scenario: Backend handles schedule task and timetable requests
- **WHEN** 后端处理排课任务分页、课表查询、基础资源分页或组织管理接口
- **THEN** 相关 service、DAO、Mapper SQL 必须只访问标准表
- **THEN** 运行链中不得再出现“标准表为空时回退 legacy 表”的逻辑

#### Scenario: Frontend consumes schedule and resource APIs
- **WHEN** 前端页面请求班级、教师、学生、课程、教室、课表和排课任务数据
- **THEN** 接口返回结构必须由标准表生成
- **THEN** 前端不得继续以 `tb_*` 兼容假设作为页面可用性的前提

### Requirement: Class data SHALL be served from the standard organization model
班级列表、班级选项、班级相关筛选与排课输入 SHALL 基于标准组织模型提供，系统 MUST 不再要求数据库存在 `tb_grade_info` 等仅服务于 legacy 班级查询的兼容表。

#### Scenario: User opens admin class list
- **WHEN** 前端请求班级分页或班级选项接口
- **THEN** 系统必须基于标准班级模型返回班级编号、名称、年级、班主任、人数和禁排时间等信息
- **THEN** 接口查询不得依赖 `tb_class_info join tb_grade_info join tb_teacher` 的 legacy 组合

#### Scenario: Database is initialized with standard tables only
- **WHEN** 开发环境只初始化标准表结构与标准种子数据
- **THEN** 班级相关接口仍必须正常返回数据
- **THEN** 不得因缺失 `tb_grade_info` 等 legacy 兼容表而报错

### Requirement: Execution and adjustment logs MUST have a single standard source of truth
排课执行记录与调课日志 MUST 以标准日志模型为唯一事实源，系统 SHALL 不再对 `tb_schedule_execute_log`、`tb_course_plan_adjust_log` 进行镜像写入或镜像读取。

#### Scenario: Schedule execution finishes
- **WHEN** 系统完成一次标准排课执行
- **THEN** 执行结果必须只写入标准执行日志模型
- **THEN** 前端执行记录接口必须从标准执行日志模型读取数据

#### Scenario: Timetable adjustment is saved
- **WHEN** 管理员完成一次标准课表调课
- **THEN** 调课记录必须只写入标准调课日志模型
- **THEN** 系统不得再为了兼容旧页面而同步写入 legacy 调课日志表

### Requirement: Legacy schedule-domain artifacts SHALL be removed after migration
一旦排课域、基础资源域与组织域完成标准化迁移，仓库中 SHALL 不再保留仅用于支持 legacy `tb_*` 主链的 DAO、Entity、SQL bootstrap、文档说明和兼容注释。

#### Scenario: Migration is completed for a legacy component
- **WHEN** 某个 legacy 组件已经没有运行时调用方
- **THEN** 对应的 DAO、Entity、Mapper SQL、增量脚本和文档说明必须被删除或改写
- **THEN** 仓库中的主线文档不得继续把该 legacy 表描述为系统依赖

#### Scenario: Repository is checked after cleanup
- **WHEN** 开发者在实现完成后搜索排课域相关 `tb_*` 关键字
- **THEN** 搜索结果中不应再包含排课、课表、基础资源、组织管理主链的 legacy 运行时依赖
- **THEN** 剩余结果如果属于其他历史业务域，必须能被明确识别为非本轮主链范围

