## Context

当前系统已经在以下主链上完成了标准化迁移：

- 组织管理：`org_campus / org_college / org_stage / org_grade / org_term / org_school_year`
- 基础资源：`res_teacher / res_student / res_course / res_building / res_classroom`
- 排课任务与结果：`sch_task / sch_schedule_result / sch_schedule_run_log`
- 系统配置：`cfg_schedule_rule / cfg_time_slot / cfg_feature_toggle`

但仓库里仍有一层未清理干净的 `tb_*` 兼容逻辑，主要分布在三类位置：

1. 仍由旧表直接驱动的后端接口与 DAO
   例如 `ClassInfoDao` 仍直接查询 `tb_class_info / tb_teacher / tb_grade_info`，执行日志与调课日志仍落到 `tb_schedule_execute_log / tb_course_plan_adjust_log`。
2. 为支持旧表而保留的实体、脚本和镜像代码
   例如 `ScheduleLogMirrorServiceImpl` 继续把标准执行记录与调课记录镜像回 legacy 表，SQL 脚本仍包含 bootstrap/drop draft。
3. 前端和文档中的兼容表叙事
   例如页面说明和增量 SQL 仍反复强调 `tb_*` 兼容链，导致后续开发者难以判断真实主链。

这次设计的目标不是“再补一层兼容”，而是把排课域与基础资源域彻底收口到标准表，使系统的数据模型、接口契约、测试与部署假设完全一致。

## Goals / Non-Goals

**Goals:**

- 让排课、课表、基础资源、组织管理相关前后端逻辑只依赖标准表，不再读取或写入 `tb_*` 兼容表。
- 用标准表替换班级、执行日志、调课日志等仍停留在 legacy 表的接口实现。
- 删除排课域为支持 `tb_*` 表而保留的镜像逻辑、兼容实体、旧 SQL 草案与文档说明。
- 保证清理完成后，数据库初始化、测试数据准备、页面展示和排课执行都只需要标准表。

**Non-Goals:**

- 不处理与排课主链无关、仍合法存在的其他历史业务表，例如在线课程、练习题、文档等非当前重构目标域。
- 不在本设计中重做排课算法本体，只要求算法输入输出不再依赖 `tb_*` 兼容结构。
- 不做一次性大规模数据库删表操作；优先先移除代码依赖和脚本假设，再决定何时物理下线旧表。

## Decisions

### 1. 以“业务域边界”而不是“表名前缀”作为清理范围

**Decision**

本次只清理排课域、基础资源域、组织管理域中仍依赖 `tb_*` 的逻辑，不把所有 `tb_*` 表一刀切视为非法。

**Rationale**

仓库中仍有 `tb_admin`、在线课程、练习等历史业务域。这些表虽然也以 `tb_` 开头，但不都属于当前排课重构范围。若直接按前缀无差别清理，会把问题从“主链收口”扩大成“整个旧系统重写”。

**Alternatives considered**

- 方案 A：所有 `tb_*` 一次性删除。风险过大，无法控制影响面。
- 方案 B：只清理当前前后端主线实际仍引用的排课相关 `tb_*`。可控且可验证，选择此方案。

### 2. 班级接口迁移到标准组织模型，不再保留 `tb_class_info` 作为班级真源

**Decision**

新增或改造标准班级读取链，以 `org_admin_class` 为班级主表；页面和排课任务选项统一基于标准班级模型，不再依赖 `tb_class_info / tb_grade_info / tb_teacher` 组合查询。

**Rationale**

当前班级兼容链既依赖旧表，又存在结构缺口，例如数据库里已经没有 `tb_grade_info`，但 DAO 仍在 join 该表。继续补兼容表只会延长旧结构寿命，不能解决根因。

**Alternatives considered**

- 方案 A：继续补 `tb_grade_info` 等兼容表。能短期止血，但保留错误边界。
- 方案 B：以 `org_admin_class + org_grade + res_teacher` 重写班级查询与选项接口。能彻底消除旧依赖，选择此方案。

### 3. 执行日志与调课日志统一以标准日志模型为准，移除 legacy mirror

**Decision**

- 排课执行日志以 `sch_schedule_run_log` 为唯一主链。
- 调课日志以标准调课日志模型为唯一主链，移除 `tb_course_plan_adjust_log` 镜像写入。
- 删除 `ScheduleLogMirrorServiceImpl` 中只为旧表存在的镜像责任，必要时将“标准日志写入”能力内聚回标准 service。

**Rationale**

双写日志会导致字段语义漂移，也让前端、测试、数据库脚本很难判断该信任哪张表。日志类数据天然应该只有一个事实源。

**Alternatives considered**

- 方案 A：保留双写，等待后续清理。问题已经持续多轮，不再继续拖延。
- 方案 B：本次直接把接口读写切到标准日志。虽然改动面更大，但能一次收口。

### 4. 兼容实体与 legacy SQL 脚本按“无调用即删除”原则清理

**Decision**

对于排课域相关的 legacy entity/dao/sql：

- 若仍被主链调用，先迁移调用方。
- 一旦无主链调用，立刻删除对应类、Mapper SQL、文档与 bootstrap 脚本，不保留“以后可能有用”的死代码。

**Rationale**

当前很多误判正是因为仓库里还留着大量“看起来能用，实际上不该再用”的类和脚本。只迁移逻辑不删旧代码，主链边界仍然是模糊的。

**Alternatives considered**

- 方案 A：加注释保留旧代码。会继续制造歧义。
- 方案 B：迁移后直接删掉。更干净，也更利于后续代码审查与测试覆盖。

## Risks / Trade-offs

- [班级接口从旧表改到标准表后，前端字段可能出现不兼容] → 先梳理当前页面真实消费字段，提供兼容 VO，再逐步去掉前端 normalize 壳层。
- [执行日志和调课日志切换标准表后，历史 legacy 数据可能不可见] → 明确本次以“后续运行主链正确”为目标；若要保留历史展示，单独做一次性迁移脚本，而不是长期双写。
- [删除旧 SQL/bootstrap 后，现有本地库可能失去部分测试数据来源] → 同步补齐标准表测试数据脚本，并更新文档，只允许标准表种子数据进入后续流程。
- [老实体/DAO 删除过快，可能遗漏隐藏引用] → 迁移每一批模块后运行 `rg "tb_"`、后端编译、前端构建和关键 controller/service 测试进行回归。

## Migration Plan

1. 先建立遗留依赖清单，按模块分组：班级兼容链、基础资源兼容链、日志镜像链、文档/SQL。
2. 先迁移仍在运行时使用的接口：
   - 班级查询/选项
   - 执行日志读取
   - 调课日志读取/写入
3. 再清理前端 `normalize*` 和页面文案中的 legacy 术语，确保接口和 UI 都只讲标准模型。
4. 删除对应 legacy controller/entity/dao/service/sql 脚本和不再需要的兼容说明。
5. 运行回归验证：
   - `rg -n "tb_" src/main/java UI/coursearrange/src docs`
   - `mvn -q -DskipTests compile`
   - 关键 controller/service tests
   - `cd UI/coursearrange && npm run build`
6. 若需要数据库物理下线旧表，单独产出 drop/backup SQL，作为后置步骤，不和代码逻辑收口绑死。

## Open Questions

- 调课日志是否已经有合适的标准表可直接承接；如果没有，是扩展现有标准日志表还是新建标准调课日志表。
- `tb_admin / tb_teacher / tb_student` 中的登录兼容链是否属于同一轮清理范围，还是作为“认证边界收口”单独推进。
- 某些非排课业务域的 `tb_*` 表是否要在本轮文档中明确标注为“保留历史域”，避免后续继续误伤。
