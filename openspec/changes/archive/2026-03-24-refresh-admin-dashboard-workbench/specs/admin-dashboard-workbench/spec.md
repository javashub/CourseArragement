## ADDED Requirements

### Requirement: Admin dashboard SHALL present a control-tower overview
管理员工作台首页必须作为系统主控台展示，而不是开发联调入口页。页面必须优先展示管理员身份、系统主链状态、排课域概览和当前阶段重点，使管理员在进入系统后能快速理解当前系统状态。

#### Scenario: Admin opens the dashboard
- **WHEN** 管理员访问 `/dashboard`
- **THEN** 页面必须展示工作台主视觉区域
- **THEN** 页面必须展示与当前登录账号有关的身份信息
- **THEN** 页面必须展示至少一个描述系统主链状态或当前阶段重点的模块

### Requirement: Dashboard MUST remove the duplicated RBAC entry card
当左侧菜单已经提供 `权限管理` 入口时，工作台页面不得继续展示“权限管理联调入口”这类重复导航卡片，以避免信息重复和视觉噪声。

#### Scenario: RBAC menu is already available in the sidebar
- **WHEN** 管理员工作台加载完成
- **THEN** 页面中不得出现“权限管理联调入口”卡片
- **THEN** 页面中不得再用首页主卡片区域重复承载 `/system/rbac` 的显式 CTA

### Requirement: Dashboard SHALL preserve the current editorial aesthetic while improving hierarchy
工作台页面必须延续当前学院档案馆式的视觉方向，并在此基础上提升排版层次、模块节奏、编号元素、标签元素或状态卡表达，使页面具备鲜明且统一的管理员控制台风格。

#### Scenario: Dashboard is rendered on desktop
- **WHEN** 页面在桌面端渲染
- **THEN** 主视觉、指标区和状态区必须具有明确的视觉层级
- **THEN** 页面不得退化为通用后台卡片堆叠样式

#### Scenario: Dashboard is rendered on mobile
- **WHEN** 页面在移动端渲染
- **THEN** 所有核心模块必须能够折叠为单列或窄列布局
- **THEN** 页面内容不得因为复杂版式而出现主要信息不可见或重叠

### Requirement: Dashboard SHOULD rely on existing frontend context only
本次工作台优化必须优先复用当前前端已经可获得的用户上下文、权限上下文和静态阶段信息，不得为了视觉优化新增后端接口依赖。

#### Scenario: Dashboard data is prepared
- **WHEN** 页面初始化展示内容
- **THEN** 页面必须仅依赖当前前端 store 中已有信息或本页静态配置
- **THEN** 本次变更不得要求新增首页专用后端 API 才能工作
