# 主题与工作台改版 · 联调自查记录

> 对应变更：`openspec/changes/add-ui-theme-and-dashboard`（前端部分 tasks 2.1–2.10、4.1–4.3）
> 采集方式：`npm run dev`（:8081，后端 :8080 实际数据），Chrome + Playwright 脚本登录 `900001` 后测量与截图。
> 视觉源：`docs/design/theme-preview/theme.css`。

## 一、截图

| 文件 | 页面 | 视口 | 说明 |
| --- | --- | --- | --- |
| `01-dashboard-daylight-1440.png` | 工作台 | 1440×900 | KPI 4 卡 + 最近任务 / 今日课表 / 最近调课（真实聚合接口） |
| `02-teacher-daylight-1440.png` | 教师管理 | 1440×900 | 单行页头 + 列表首行 228px |
| `03-teacher-deep-teal-1440.png` | 教师管理 | 1440×900 | `deep-teal` 深青侧栏 |
| `04-teacher-topnav-1440.png` | 教师管理 | 1440×900 | `top-nav` 顶栏导航，表格全宽 |
| `05-schedule-daylight-1280.png` | 课表管理 | 1280×800 | 单行工具条；网格 108px / 18px |
| `06-dashboard-768.png` | 工作台 | 768×900 | KPI 与下方面板单列，无横向滚动 |
| `07-login-1440.png` | 登录 | 1440×900 | 冷白色板，无金色 eyebrow / 衬线标题 |

## 二、关键测量（Playwright `getBoundingClientRect` / `getComputedStyle`）

| 指标 | 1440×900 | 1280×800 | 结论 |
| --- | --- | --- | --- |
| 教师管理表格起点 | 196px（`top-nav` 192px） | 196px（`top-nav` 192px） | ≤ 230px |
| 教师管理表格首行 | 228px（`top-nav` 224px） | 228px（`top-nav` 224px） | ≤ 230px |
| 课表 `.grid-cell` / `.time-cell` 高度 | 108px | 108px | 与改版前一致 |
| 课表单元格圆角 | 18px | 18px | 不变 |
| 课表课程卡圆角 | 16px | 16px | 不变 |
| 课程卡强调色去重数（35 张卡） | 9 种 | 9 种 | 仍由 `COURSE_PALETTE` 决定 |
| 课表工具条高度 | 54px（单行） | 54px（单行，侧栏版内容宽 1008px 仍单行） | 三行卡片 → 单行 |
| 页面横向滚动 | `scrollWidth === innerWidth` | `scrollWidth === innerWidth` | 三主题均无横向滚动 |
| 工作台 768 列数 | — | KPI/面板均单列（556px） | 无重叠、无溢出 |

## 三、主题与持久化

- `<html data-theme>` 由 `stores/app.js` 写入；`daylight` / `deep-teal` / `top-nav` 三选一，未知值回退 `daylight`。
- `top-nav`：顶栏 7 个一级项（工作台、排课任务、课表管理、基础数据、组织架构、系统管理、重构说明），分组下拉含 9 个子项，与侧栏菜单树同源。
- 账号级持久化：切换后 `PUT /api/auth/ui-theme` 保存；刷新页面 `data-theme` 仍为 `top-nav`，`/api/auth/context` 返回 `user.uiTheme` 一致。
- 保存失败：强制 500 后主题仍为 `deep-teal`，提示「主题已切换，但保存到账号失败…」，不回滚。
- 未知主题：接口返回 `neon-midnight` 时回退 `daylight`，页面正常。

## 四、复现命令

```bash
cd UI/coursearrange
npm run lint && npm run build
# 浏览器验证脚本不入库，关键测量值见上表
```
