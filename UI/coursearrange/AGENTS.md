# AGENTS.md

## 目标

本文件约束代理在 `UI/coursearrange` 前端项目中的实现方式。规则以当前 Vue 3 + Vite + Element Plus 工程现状为准。

## 技术栈

- `Vue 3`
- `Vite 5`
- `Element Plus`
- `Pinia`
- `Vue Router 4`
- `Axios`
- `ESLint + Prettier`

禁止：

- 使用 Vue 2 / Options API 风格重写新页面
- 引入新的 UI 组件库
- 页面里直接写裸 `axios`

## 目录约定

- `src/views`：页面级组件
- `src/layouts`：布局与导航
- `src/stores`：Pinia 状态
- `src/api`：请求封装与模块 API
- `src/styles`：全局设计变量与全局覆写
- `src/utils` / `src/constants`：工具与常量

要求：

- 新页面优先放 `views`。
- 可复用布局逻辑放 `layouts`。
- 跨页面共享状态才进 `stores`。
- 所有接口都通过 `api/modules/*.js` 暴露。

## Vue 编码风格

- 新组件优先使用 `<script setup>`。
- 优先使用组合式 API：`ref`、`reactive`、`computed`。
- 方法名用动词开头，如 `loadData`、`handleSubmit`、`openDialog`。
- 页面负责展示和交互，重逻辑应提取到 helper / util。
- 纯逻辑函数尽量保持可测试。

## API 与请求层

统一走：

- `src/api/request.js`
- `src/api/modules/*.js`

要求：

- 页面和 Store 不直接使用裸 `axios`。
- 错误提示优先交给请求拦截器统一处理。
- token 头字段保持 `satoken`。
- 从响应 `data` 中取业务数据。
- 分页参数统一使用 `pageNum`、`pageSize`。

## Store 规范

参考 `stores/auth.js`、`stores/app.js`：

- Store 只承载跨页面共享状态。
- 登录态、权限、菜单继续走 `auth` store。
- 本地存储统一走 `utils/storage.js`。
- 不要在多个页面直接操作 `localStorage`。

## 路由与权限

路由定义在 `src/router/index.js`。

要求：

- 新页面要补齐 `meta.title`。
- 需要权限控制时配置 `meta.permission`。
- 路由路径应与后端菜单 `routePath` 保持一致。
- 不要绕过 `authStore.loadAuthContext()` 自己实现一套权限初始化。

## UI 风格

当前前端视觉基线来自：

- `src/styles/index.css`（唯一 token 源）
- `src/components/PageHeader.vue`（页头规范）
- `src/layouts/BasicLayout.vue` + `src/layouts/TopNav.vue`（三套壳）
- `docs/design/theme-preview/theme.css`（视觉稿对应 token）

关键词：

- 冷白数据台：`--bg #f3f8fd`、`--surface #ffffff`、`--line #e4edf7`
- 品牌蓝 `--brand #165dff` → 青绿 `--teal #0ea5a4`，主按钮/页签用 `--grad`
- 文字：`--ink #17263d`、`--ink-2 #2f4a6b`、`--muted-2 #5b7089`
- 面板圆角 `--radius 14px`，控件圆角 `--radius-control 8px`
- 无衬线：`--app-font-family`（苹果系统字体 / PingFang SC / HarmonyOS Sans SC / MiSans / Noto Sans SC）
- 数字统一 `font-variant-numeric: tabular-nums`
- 页头单行：标题 19px/600 + 12.5px 说明 + 计数 pill + 右侧操作

三套主题（同时决定色板与导航布局，`<html data-theme>`）：

- `daylight`：浅色侧栏（默认）
- `deep-teal`：深青侧栏，内容区仍为冷白
- `top-nav`：菜单移到顶栏，一级项 + 下拉子菜单，表格更宽（`--row 44px`）

要求：

- 只使用 `src/styles/index.css` 提供的 token，不新造颜色、阴影、圆角体系。
- 列表页统一用 `PageHeader`，不要在页面上再写 eyebrow / 34px 衬线标题 / 两行说明。
- 主题只影响外壳与页头；组件里不要写主题判断分支，靠 CSS 变量自动跟随。
- **课表冻结**：`views/course/ScheduleView.vue` 的 `COURSE_PALETTE`、`.grid-*`、`.time-*`、`.course-*` 与 `.timetable-wrapper` 以下样式一行都不许改，行高 108px、圆角 18px、12 色板必须保持不变；课表页只允许压缩页头为单行工具条。
- Element Plus 覆写走全局 CSS 变量与 `:deep()`；`.academy-form-dialog` 等类名保持不变。

## 样式组织

- 全局变量和公共覆写放 `src/styles/index.css`。
- 页面专属样式优先写在组件内 `style scoped`。
- `:deep()` 仅用于覆盖第三方组件内部结构。
- 不要把页面私有样式大量堆进全局样式文件。

## 测试与验证

当前轻量测试使用：

- `node:test`
- `node:assert/strict`

要求：

- 对纯函数、路由工具、文案常量优先补轻量测试。
- 测试文件命名为 `*.test.js`。

常用验证命令：

- `npm run lint`
- 必要时执行对应测试
- 涉及构建风险时执行 `npm run build`

## 禁止事项

- 引入第二套前端状态管理、请求层、UI 组件库。
- 在页面中重复写登录态、token、本地存储逻辑。
- 直接写与当前主题冲突的 UI。
- 为一次性需求过度抽象。
