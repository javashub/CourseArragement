# AGENTS.md

本文件是仓库级单一事实源，所有编码代理通用。`CLAUDE.md` 引用本文件；子目录规范见 `src/AGENTS.md`（后端）与 `UI/coursearrange/AGENTS.md`（前端），冲突时以子目录为准。

## 行为准则

通用编码行为（先想再写、最小实现、外科手术式改动、先定验收再验证）遵循 `karpathy-guidelines` skill；本文件负责仓库级事实与硬约束，二者冲突时以本文件为准。

## 代码注释

- AI 新增或修改的代码必须写中文注释；标识符、类型名、框架 API 等专有名词保留原文。
- 格式按语言惯例：Java 类与 public 方法用 Javadoc `/** ... */`，关键业务逻辑用行内 `//`；JavaScript / Vue 函数与方法用 JSDoc `/** ... */`，关键逻辑用 `//`。
- 注释说明业务含义与原因，必要时写明参数、返回值和边界条件；不逐行翻译代码，不写空泛套话。
- 注释与代码同步更新；实现变更时一并修正已有注释，禁止留下与实现不符的注释。

## 项目定位

基于遗传算法的高中/培训机构排课系统。后端 Spring Boot 单体提供 REST API，前端 Vue 3 SPA 经 `/api` 代理访问；数据库 MySQL 8，库名 `course_arrange_v2`。

## 技术栈

以 `pom.xml` 与 `UI/coursearrange/package.json` 为准，README 中的历史描述不作为实现依据。

- 后端：Java 17、Spring Boot 2.7.18、Maven、MyBatis-Plus 3.5.7、Sa-Token 1.44.0、MySQL 8、Lombok、EasyExcel
- 前端：Vue 3.5、Vite 5、Element Plus 2.9、Pinia 2、Vue Router 4、Axios；ESLint 8 + Prettier 3
- 测试：后端 JUnit / Spring Boot Test；前端 `node:test` + `node:assert/strict`

## 命令

后端命令在仓库根执行（无 Maven Wrapper，直接用 `mvn`）；前端命令在 `UI/coursearrange/` 执行。

| 目的 | 命令 |
| --- | --- |
| 后端编译 | `mvn -q -DskipTests compile` |
| 后端全量测试 | `mvn test` |
| 后端单测 | `mvn -q -Dtest=ClassTaskServiceImplTest test` |
| 前端依赖 | `npm install` |
| 前端开发 | `npm run dev`（:8081，`/api` 代理到 :8080） |
| 前端检查 | `npm run lint` |
| 前端构建 | `npm run build` |

默认 profile `dev`，后端 :8080。数据库连接可由 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` 覆盖，见 `src/main/resources/application-dev.yml`。

## 目录

| 路径 | 内容 |
| --- | --- |
| `src/main/java/com/lyk/coursearrange/` | 后端源码，按业务域分包（`schedule/`、`resource/`、`organization/`、`system/`、`auth/`、`excel/`），域内自带 controller / entity / mapper；`common/` 为横切能力 |
| `src/main/resources/mapper/` | MyBatis XML |
| `src/test/java/` | 后端测试 |
| `UI/coursearrange/src/` | 前端源码 |
| `doc/` | 数据库设计、`schema.sql`、算法资料、导入模板 |
| `docs/incremental_sql/` | 增量 SQL，命名 `YYYYMMDD_NN_描述.sql` |
| `docs/plans/` | 阶段实施计划 |
| `openspec/` | 变更提案与主规范 |
| `待办.md` | 阶段进度与执行规则 |
| `项目重构交接文档.md` | 重构上下文与验证记录 |

## 硬约束

- 每次只做一个明确功能切片，不混入无关重构。
- 每完成一个功能先验证编译或测试通过，再提交。
- 提交信息使用中文；不提交 `UI/coursearrange/package-lock.json` 与无关工作区噪音。
- 数据库变更只新增 `docs/incremental_sql/` 增量脚本，不改 `doc/schema.sql` 历史基线。
- 排课主链以标准表 `sch_task` / `sch_schedule_result` 为准，禁止恢复 legacy `tb_class_task` / `tb_course_plan` 运行时依赖。
- 鉴权统一 Sa-Token，token 头 `satoken`；分页参数统一 `pageNum` / `pageSize`；逻辑删除字段统一 `deleted`（1 删除 / 0 正常）。
- 新增接口使用资源化路径（如 `/api/schedule/tasks/executions`），不新增 `/courseplan/*` 兼容壳层。
- 前端接口必须经 `src/api/request.js` 与 `src/api/modules/*.js`，页面内禁止裸 `axios`。
- 排课约束（时间片模板、教师/班级禁排、连堂上限、优先级）来自系统配置与标准任务字段，禁止硬编码。

## 三级边界

- **Always**：改动前读对应子目录 `AGENTS.md`；改动后跑受影响侧的最小验证命令，保留完整失败输出。
- **Ask first**：删除旧实体/旧控制器链、调整表结构、修改 `pom.xml` 依赖、改动鉴权与权限菜单、引入新组件库或状态管理、任何 `git push` 等远端写入。
- **Never**：提交密钥或阿里云 OSS AK、把多功能切片混进一次提交、覆盖他人未提交改动。

## 上下文优先级

源码与测试为权威；`README.md` 面向人类访客，不作为实现依据。需求与设计以 `需求文件.md`、`系统设计方案.md`、`项目重构交接文档.md` 为准，阶段计划看 `docs/plans/` 与 `待办.md`。
