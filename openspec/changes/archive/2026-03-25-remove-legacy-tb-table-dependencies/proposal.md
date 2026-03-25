## Why

当前仓库虽然已经把排课任务、课表查询、基础资源和组织管理的大部分主链切到了 `org_* / res_* / sch_* / cfg_*` 标准表，但代码与文档中仍残留大量 `tb_*` 旧表依赖。继续保留这套双轨逻辑会让前后端行为、数据库初始化、测试数据和后续算法重构长期处于不确定状态，因此需要把 `tb_*` 兼容链彻底清理到只剩明确保留的非排课业务域。

## What Changes

- 清理前端页面、接口适配层和文案中对排课域 `tb_*` 旧表的依赖、提示和兼容映射，只保留标准主链的数据模型与术语。
- 清理后端排课域、基础资源域、组织域中对 `tb_class_info / tb_teacher / tb_student / tb_course_info / tb_classroom / tb_teach_build_info / tb_schedule_execute_log / tb_course_plan_adjust_log / tb_grade_info / tb_course_plan` 等旧表的读写、镜像、回退和日志逻辑。
- 将班级、执行日志、调课日志等仍依赖 legacy 表的接口切换到标准表或标准事件模型，补齐缺失的标准查询与写入链路。
- 删除仅为支持排课域旧表而保留的 DAO、Entity、SQL 草案、兼容注释与初始化脚本；同步更新文档、测试和增量 SQL，明确系统不再依赖排课域 `tb_*` 旧结构。
- **BREAKING**：数据库层面不再要求为排课、课表、基础资源、组织管理准备 `tb_*` 兼容表；后续开发、测试与演示数据只基于标准表准备。

## Capabilities

### New Capabilities
- `legacy-table-cleanup`: 定义系统在排课域与基础资源域完全停止依赖 `tb_*` 旧表后的行为边界、接口契约与迁移要求。

### Modified Capabilities
- `<existing-name>`: N/A

## Impact

- 影响后端模块：`schedule`、`resource`、`organization`、`service/dao/entity` 中仍引用 `tb_*` 的类与 SQL。
- 影响前端模块：[BaseDataView.vue](/Users/lyk/dev/project_source/毕业设计/CourseArrange/UI/coursearrange/src/views/base/BaseDataView.vue)、[ScheduleView.vue](/Users/lyk/dev/project_source/毕业设计/CourseArrange/UI/coursearrange/src/views/course/ScheduleView.vue) 及相关 `api/modules/*` 适配层。
- 影响数据库与脚本：legacy bootstrap SQL、drop draft SQL、测试数据脚本、执行日志与调课日志相关表。
- 影响测试与文档：需要重写仍基于 `tb_*` 假设的 controller/service 测试、待办文档、重构方案与交接文档。
