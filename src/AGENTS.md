# AGENTS.md（后端）

约束 `src/` 下的 Java 代码改动。仓库级规则见 `../AGENTS.md`，冲突时以本文件为准。

## 分层与落点

后端按**业务域**分包，不要按技术层把新代码放进顶层目录。

- `schedule/`：排课域，域内有 `controller/`、`engine/`、`entity/`、`mapper/`、`request/`、`service/`；算法主体在 `engine/`（`SchedulingEngine`、`FeasibleSolutionBuilder`、`GeneticScheduleOptimizer`、`SchedulingConstraintEvaluator`）。
- `resource/` / `organization/` / `system/` / `auth/` / `excel/`：资源、组织、系统配置与 RBAC、认证、导入导出。各域自带 `controller/`、`entity/`、`mapper/`，部分域另有 `config/`、`query/`、`request/`、`model/`、`service/impl/`。
- `common/`：横切能力，按职责分 `api/`（`ApiResponse`、`PageResponse`）、`model/`（`PageQuery`）、`enums/`（`ErrorCodeEnum`、`ResultCode`）、`exceptions/`（全局异常处理）、`util/`（分页工具），另有顶层 `ResponseCode`、`ResultData`、`ServerResponse`。
- 当前 `ResultData` / `ServerResponse` / `ApiResponse` 多套响应结构并存，属历史未收口状态：新增接口沿用**同域已有**的那一套，禁止再造新结构；统一收口目标需先与用户确认。
- `config/` 放全局配置类，`util/` 放无状态工具。顶层 `controller/`、`dao/`、`entity/`、`service/` 为历史过渡目录，新代码不要写入。
- `CoursePlan*` 旧链处于收口阶段，只减不增。

## 编码约定

- Java 17 + Spring Boot 2.7.18；Lombok 只用于样板代码，实体不承载业务逻辑。
- controller 只做入参校验与结果封装，业务规则落在 service / engine。
- 逻辑删除字段 `deleted`（1 删除 / 0 正常）由 MyBatis-Plus 自动过滤，禁止手写 SQL 绕过。
- 分页入参复用 `common/model/PageQuery`（`pageNum` 默认 1，`pageSize` 默认 10、上限 100），返回复用 `common/api/PageResponse`；时间类型用 `LocalDateTime` / `LocalDate`，序列化格式由 `application-dev.yml` 统一配置。
- 排课约束走系统配置与 `SchTask` 字段（时间片模板、教师/班级禁排、连堂上限、`priorityLevel`），禁止在 engine 内硬编码常量。
- 染色体编码使用结构化对象，禁止回退到拼接字符串 + 定长切片解析。

## 数据库变更

- 只新增 `docs/incremental_sql/YYYYMMDD_NN_描述.sql`，不修改 `doc/schema.sql`。
- 改动 `sys_menu` / `sys_role_menu` 时同步权限上下文与前端路由 `meta.permission`。
- 禁止恢复 legacy `tb_*` 表依赖。

## 测试与验证

- 新增或修改 service / controller 必须补对应 `*Test`，命名与被测类一致。
- 最小验证 `mvn -q -Dtest=<TestClass> test`；提交前 `mvn -q -DskipTests compile`；较大改动跑 `mvn test`。
- 算法改动必须补可重复的硬约束回归测试，不依赖随机种子偶然通过。

## 禁止

- 引入第二套鉴权（旧 JWT 链已删除）、新的统一响应结构或分页模型。
- 在 `schedule/engine/` 内访问 HTTP 会话或前端页面语义。
- 为一次性需求新建通用抽象、配置开关或兼容壳层。
