## 1. 遗留依赖盘点

- [x] 1.1 盘点后端 `schedule / resource / organization / service / dao / entity` 中所有仍引用排课域 `tb_*` 表的类、SQL 和注释，形成可执行清单。
- [x] 1.2 盘点前端 `views / api/modules / stores / docs` 中所有仍以 legacy 表为前提的适配代码、页面文案和脚本说明。

## 2. 班级与基础资源主链收口

- [x] 2.1 设计并实现基于 `org_admin_class / org_grade / res_teacher` 的标准班级分页与选项查询，替换 `ClassInfoDao` 对 `tb_class_info / tb_grade_info / tb_teacher` 的依赖。
- [x] 2.2 调整排课任务页、班级选项接口和相关前端适配层，使班级相关展示字段完全来自标准班级模型。
- [x] 2.3 清理基础资源与组织管理模块中剩余的 `tb_teacher / tb_student / tb_course_info / tb_classroom / tb_teach_build_info` 运行时依赖、兼容 VO 和 normalize 壳层。

## 3. 日志与课表兼容链清理

- [x] 3.1 将排课执行记录接口统一切换到 `sch_schedule_run_log`，删除对 `tb_schedule_execute_log` 的读取和镜像写入。
- [x] 3.2 为调课日志确定标准落表方案，迁移调课记录读写链，移除 `tb_course_plan_adjust_log` 兼容写入。
- [x] 3.3 删除 `ScheduleLogMirrorServiceImpl` 及相关 legacy mirror 逻辑，确保课表结果、执行日志和调课日志只有一个标准事实源。

## 4. 代码与脚本文档清理

- [x] 4.1 删除排课域不再使用的 legacy DAO、Entity、Mapper SQL、bootstrap/drop draft SQL 与过期增量脚本。
- [x] 4.2 更新前端页面说明、待办文档、重构方案、交接文档和 OpenSpec 资料，明确主链只依赖标准表。
- [x] 4.3 补齐标准表测试数据脚本与必要迁移脚本，替代所有以排课域 `tb_*` 表为前提的初始化方式。

## 5. 回归验证

- [x] 5.1 为班级查询、排课任务、课表查询、执行日志与调课日志补齐或更新 controller/service 测试。
- [x] 5.2 运行 `rg -n "tb_" src/main/java UI/coursearrange/src docs`，确认排课域主链不再残留 legacy 运行时依赖。
- [x] 5.3 运行 `mvn -q -DskipTests compile`、关键测试命令和 `cd UI/coursearrange && npm run build`，确认清理后前后端仍可编译与打包。
