# CourseArrange 课程排课系统

基于遗传算法的高中 / 培训机构课程排课系统，采用前后端分离的单体架构。

> 项目已完成 2.0 重构：从 2021 年的 Vue 2 + JDK 1.8 升级为 **Vue 3 + Spring Boot 2.7 + Java 17**；排课主链已切换到标准表 `sch_task` / `sch_schedule_result`，不再依赖 legacy `tb_*` 表。
>
> 本项目完全开源，欢迎 Star 与反馈。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 2.7.18、MyBatis-Plus 3.5.7、Sa-Token 1.44.0、MySQL 8.0、Maven、Lombok、EasyExcel |
| 前端 | Vue 3.5、Vite 5、Element Plus 2.9、Pinia 2、Vue Router 4、Axios |
| 数据库 | MySQL 8，库名 `course_arrange_v2` |

## 功能概览

系统包含管理员（教务处）、教师、学生三类角色：

- **管理员**：基础数据（教师 / 学生 / 课程 / 教学楼 / 教室）、组织架构（校区 → 学院 → 学段 → 年级 → 行政班）、排课任务管理、自动排课与执行记录、课表查看与调课、系统配置（时间片模板 / 排课规则 / 功能开关）、数据字典、RBAC 用户 / 角色 / 菜单 / 权限、Excel 导入导出。
- **教师**：个人课表查看（简化版）。
- **学生**：个人课表查看（简化版）。

### 排课引擎

采用「双阶段」排课：先用可行解构造器生成初版课表，再用轻量遗传算法做补排与优化。已接入的约束包括：

- 时间片模板（排除不可上课 / 固定休息时间片）
- 教师禁排时间、班级禁排时间
- 连堂需求与上限、教师日上限课时
- 任务优先级
- 特殊教室 / 容量匹配
- 行政班固定时间前置校验（冲突前置拦截并给出原因）

## 目录结构

```
CourseArrange/
├── src/main/java/com/lyk/coursearrange/
│   ├── auth/           # 认证（Sa-Token）
│   ├── schedule/       # 排课域（controller / engine / entity / mapper / service）
│   ├── resource/       # 基础资源（教师 / 学生 / 课程 / 教学楼 / 教室）
│   ├── organization/   # 组织架构（校区 / 学院 / 学段 / 年级 / 行政班）
│   ├── system/         # 系统配置 / 数据字典 / RBAC
│   ├── excel/          # Excel 导入导出
│   ├── common/         # 响应模型、异常、分页等横切能力
│   └── config/         # 全局配置
├── UI/coursearrange/   # 前端工程（Vue 3 + Vite）
├── doc/                # 数据库设计与 schema.sql
├── docs/incremental_sql/ # 增量 SQL（YYYYMMDD_NN_描述.sql）
├── docs/plans/         # 阶段实施计划
└── AGENTS.md / src/AGENTS.md / UI/coursearrange/AGENTS.md  # 编码规范
```

## 快速开始

环境要求：JDK 17+、Maven 3.6+、Node.js 18+、MySQL 8。

1. 建库：按 `doc/schema.sql` 初始化 `course_arrange_v2`，再按顺序执行 `docs/incremental_sql/` 下的增量脚本。
2. 启动后端：在仓库根执行 `mvn -q -DskipTests compile` 验证编译，随后运行 `CoursearrangeApplication`，默认端口 `:8080`。
   数据库连接可用 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` 覆盖，默认库名 `course_arrange_v2`。
3. 启动前端：`cd UI/coursearrange && npm install && npm run dev`，默认端口 `:8081`，`/api` 代理到 `http://127.0.0.1:8080`。
4. 文件上传功能需要配置阿里云 OSS Key / 密钥，位置见 `src/main/java/com/lyk/coursearrange/util/AliyunUtil.java`。

## 文档

- `AGENTS.md`：仓库级编码约定；`src/AGENTS.md` / `UI/coursearrange/AGENTS.md`：前后端补充规范
- `需求文件.md`、`系统设计方案.md`：需求与设计
- `项目重构交接文档.md`、`待办.md`：重构上下文与阶段进度
- `docs/plans/`：阶段实施计划

## 开源说明

- GitHub: https://github.com/imlyk/CourseArragement
- 完全开源，欢迎 Star；如需交流，可在仓库 Issue 中提出。
