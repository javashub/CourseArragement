# 主题改版预览

> 目标：替换现有「学院风」（暖米色 + 深蓝 + 金色 + 衬线标题 + 玻璃质感），让整体视觉与**课表页**统一；同时压缩页头，把竖向空间还给数据。
>
> **课表表格本身（网格、圆角、108px 行高、课程配色、时段徽章）一行样式都不改**，只压缩它上方的页头。

## 一、现状诊断

当前工程其实是**两套视觉并存**：

| 区域 | 现状 | 色系 |
| --- | --- | --- |
| 全局壳层 `src/styles/index.css:2-14`、`src/layouts/BasicLayout.vue:172-181` | 暖米色背景、深蓝侧栏、金色强调、衬线大标题、网格纹理 + 柔光叠加 | `#f3ede2` / `#1f4d6b` / `#b88746` |
| 课表页 `src/views/course/ScheduleView.vue:298-311` | 冷白卡片、蓝→青渐变、彩色课程块 | `#165dff → #0ea5a4` / `#17263d` |
| 基础数据、排课任务、系统配置 | 已经局部改用课表的冷色系，但页头仍是「eyebrow + 34px 标题 + 两行说明」 | 混合 |
| 工作台 `src/views/dashboard/DashboardView.vue:126-153` | 64px 衬线标题 + 编辑式大段说明 + 编号卡片 | 暖米色 |

问题点正好对应你的三条反馈：

1. **风格割裂**：壳层和课表不是一家人，切换页面像换了一个系统。
2. **字体不美**：`Iowan Old Style / Baskerville / Songti SC` 作为中文标题回退到宋体，配 0.06em 字距，像公文；全大写英文小标签（`Executive Editorial Desk`、`Academic Control Ledger`）和界面语言不搭。
3. **页头吃空间**：列表页在表格出现前要经过 `72px 顶栏 + 28px 内边距 + 152px hero + 卡片内边距 + 42px 页签 + 48px 工具条`，约 **380px**；1440×900 下首屏只剩 5 行左右表格。

## 二、新视觉方向：冷白 · 晴空

所有颜色直接从课表页取样，保证「换主题 = 回到课表」：

| Token | 值 | 来源 |
| --- | --- | --- |
| `--brand` | `#165dff` | 课表 toggle / 查询按钮 |
| `--teal` | `#0ea5a4` | 课表渐变终点 |
| `--grad` | `linear-gradient(135deg, #165dff, #0ea5a4)` | 课表主按钮、页签下划线 |
| `--ink` | `#17263d` | 课表标题 |
| `--ink-2` | `#2f4a6b` | 课表次级标题 `#35547b` 微调 |
| `--muted` | `#566b85` | 课表辅助文字 `#53667f` |
| `--muted-2` | `#5b7089` | 说明文字 / 表头（对比度 5.1:1） |
| `--line` | `#e4edf7` | 课表格子描边 |
| `--bg` | `#f3f8fd` | 冷白工作区 |
| 面板圆角 | `14px`；控件 `8px` | 比原来的 24–30px 更利落 |
| 行高 | 列表 `54px` / 顶栏版 `44px` | 原 Element 默认约 48px |

字体方案（不再用衬线）：

```css
font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC',
  'HarmonyOS Sans SC', 'MiSans', 'Noto Sans SC',
  'Segoe UI', 'Microsoft YaHei', sans-serif;
font-variant-numeric: tabular-nums;
```

- 页标题 `19px / 600`，分区标题 `13.5px / 600`，正文 `12.5–13px`，表头 `11.5px / 600`。
- 中文不再加字距；数字统一等宽（课时、编号、时间列对齐）。
- 如果在意 Windows 上的观感，可自托管 MiSans / HarmonyOS Sans SC 子集（约 1.5–3MB）；不建议引入思源宋体。

### 空间账

| | 旧 | 新 | 差值 |
| --- | --- | --- | --- |
| 顶栏 | 72px | 56px | −16 |
| 内容内边距 | 28px | 16px | −12 |
| 页头（标题 + 说明） | 约 152px（含 28px 上下 padding） | 38px（标题与说明同一行） | −114 |
| 表格起点（1440×900 实测） | 约 380px | 222px | **−158px ≈ 多 2.5 行** |
| 卡片圆角 / 阴影 | 30px / 18px 45px | 14px / 1px 描边 | 更轻 |

## 三、三个方向

| 图 | 方向 | 特点 | 代价 |
| --- | --- | --- | --- |
| `01-teacher-a.png` | **A · 晴空白瓷**（推荐） | 浅色侧栏，最贴近现有结构；改动只在壳层与页头，风险最低；和课表白卡最协调 | 个性最克制 |
| `02-teacher-b.png` | **B · 深青侧栏** | 保留深色导航的对比，投影/大屏更稳；蓝色高亮在深底上更醒目 | 深色面积大，与课表白卡反差强 |
| `03-teacher-c.png` | **C · 顶栏全宽** | 去掉侧栏，表格宽度 1166→1396px（+20%），行高 44px，一屏 13 行 | 菜单层级要收进下拉，路由改造最大 |

建议：**以 A 为主线**，吸收 C 的「页头合并筛选」细节；B 作为可选主题（同一套 token 换侧栏底色即可）。

## 四、效果图

- `01-teacher-a.png` — 方向 A，教师管理，1440×900
- `02-teacher-b.png` — 方向 B，教师管理，1440×900
- `03-teacher-c.png` — 方向 C，教师管理，1440×900
- `04-schedule-a.png` — 方向 A，课表管理，1440×980（**表格样式原样**，页头由三行卡片压成 54px 单行工具条）
- `05-dashboard-a.png` — 方向 A，工作台概念稿（KPI + 最近任务 + 今日课表 + 最近调课）

## 五、落地步骤（确认方向后执行）

0. 先改 `UI/coursearrange/AGENTS.md:84-103` 的视觉基线描述（学院风 → 冷白数据台），否则后续代理会按旧规则把样式改回去。
1. `src/styles/index.css`：替换 token；删除 `html` 暖色渐变（`:31-36`）与 `body::before/::after` 纹理（`:45-69`）；`.page-card` 去掉装饰边框（`:120-138`）、padding 30→18、圆角 30→14；`.page-title`（`:140-148`）改无衬线 19px；`.el-card`（`:158-163`）圆角 24→14；`.academy-form-dialog`（`:190-216`）去掉衬线与米色玻璃，**保留类名**避免改所有调用点；补 `.page-head` 与 Element Plus `--el-color-primary` 映射。
2. `src/layouts/BasicLayout.vue`：顶栏 72→56，移除 `header-ribbon`/`header-chip` 英文标签，用户名去掉 24px 衬线；侧栏两套 token（白 / 深青）。
3. 新增 `src/components/PageHeader.vue`（title / description / count / actions），替换 6 个页面的 hero：`BaseDataView.vue:3-9`、`CoursePlanView.vue:4-27`、`SystemConfigView.vue:3-20`、`CampusView.vue:5-6`、`CollegeView.vue:5-6`、`StageView.vue:5-6`。
4. `ScheduleView.vue` **只改顶部三行卡片（`:4-96`）**为单行工具条；`.timetable-wrapper` 及以下（`:134-199`、`:776-1055`）保持不动。
5. `DashboardView.vue` 按 05 稿替换；`LoginView.vue:88-135` 换色、去金色 eyebrow 与衬线。
6. 验证：`cd UI/coursearrange && npm run lint && npm run build`；1440×900 / 1280×800 截图对照；检查 Element Plus 表格、弹窗、下拉的覆写；正文对比度 ≥ 4.5:1。

## 六、待确认

1. 选 A / B / C（或 A + B 换肤）。
2. 工作台是否允许接真实统计/任务数据，还是先放静态占位？
3. 是否需要暗色模式（当前只做浅色）。
4. 是否接受自托管中文字体（体积换观感）。

## 七、重生成

```bash
cd docs/design/theme-preview
node build.mjs                      # 生成 html/*.html
node measure.mjs "file://$PWD/html/01-teacher-a.html" 1440 900 ".content,.panel,table"
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
  --headless --disable-gpu --no-sandbox --hide-scrollbars \
  --force-device-scale-factor=2 --window-size=1440,900 \
  --screenshot="$PWD/01-teacher-a.png" "file://$PWD/html/01-teacher-a.html"
```

`theme.css` 是唯一视觉源；`build.mjs` 只负责拼页面与示例数据。
