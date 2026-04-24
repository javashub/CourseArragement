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

- `src/styles/index.css`
- `LoginView.vue`
- `BasicLayout.vue`

关键词：

- 学院/教务气质
- 暖米色背景、深蓝主色、金色强调
- 渐变、柔和阴影、玻璃质感
- 标题偏衬线，正文偏无衬线

要求：

- 优先复用全局 CSS 变量，如 `--app-primary`、`--app-accent`。
- 优先复用 `.page-card` 等现有页面容器模式。
- 不要做成默认的白底后台模板风格。
- 不要新增与现有体系冲突的颜色、阴影、圆角体系。
- 样式上优先延续当前项目语言，而不是重新设计一套。

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
