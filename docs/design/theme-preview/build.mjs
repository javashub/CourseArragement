import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const outDir = join(here, 'html');
mkdirSync(outDir, { recursive: true });

/* ------------------------------------------------------------------ icons */
const ICONS = {
  grid: '<rect x="3" y="3" width="7" height="7" rx="1.6"/><rect x="14" y="3" width="7" height="7" rx="1.6"/><rect x="3" y="14" width="7" height="7" rx="1.6"/><rect x="14" y="14" width="7" height="7" rx="1.6"/>',
  calendar:
    '<rect x="3" y="4.5" width="18" height="16" rx="2.6"/><path d="M3 9.5h18M8 2.6v3.8M16 2.6v3.8"/>',
  list: '<path d="M8 6h12M8 12h12M8 18h12"/><circle cx="3.6" cy="6" r="1.1" fill="currentColor" stroke="none"/><circle cx="3.6" cy="12" r="1.1" fill="currentColor" stroke="none"/><circle cx="3.6" cy="18" r="1.1" fill="currentColor" stroke="none"/>',
  database:
    '<ellipse cx="12" cy="6" rx="7.4" ry="3"/><path d="M4.6 6v6c0 1.66 3.32 3 7.4 3s7.4-1.34 7.4-3V6"/><path d="M4.6 12v6c0 1.66 3.32 3 7.4 3s7.4-1.34 7.4-3v-6"/>',
  building:
    '<path d="M4 21V5.6A1.6 1.6 0 0 1 5.6 4h6.8A1.6 1.6 0 0 1 14 5.6V21"/><path d="M14 10.5h4.4A1.6 1.6 0 0 1 20 12.1V21"/><path d="M2.6 21h18.8"/><path d="M7.4 8h3.2M7.4 12h3.2M7.4 16h3.2M17 14.6h.01M17 18h.01"/>',
  sliders:
    '<path d="M4 7.5h8.4M17.2 7.5H20M4 16.5h2.8M11.6 16.5H20"/><circle cx="14.8" cy="7.5" r="2.2"/><circle cx="9.2" cy="16.5" r="2.2"/>',
  shield: '<path d="M12 2.9 20 6v6.1c0 4.6-3.3 7.8-8 9C7.3 19.9 4 16.7 4 12.1V6z"/><path d="m8.9 11.9 2.2 2.2 4.2-4.4"/>',
  search: '<circle cx="11" cy="11" r="6.4"/><path d="m15.8 15.8 4.4 4.4"/>',
  bell: '<path d="M18.2 8.6a6.2 6.2 0 1 0-12.4 0c0 5.1-2 6.7-2 6.7h16.4s-2-1.6-2-6.7Z"/><path d="M10.4 19a1.9 1.9 0 0 0 3.2 0"/>',
  plus: '<path d="M12 5.2v13.6M5.2 12h13.6"/>',
  upload: '<path d="M12 16.4V4.2m0 0L8 8.2m4-4 4 4"/><path d="M4 15.6v3.2a1.2 1.2 0 0 0 1.2 1.2h13.6a1.2 1.2 0 0 0 1.2-1.2v-3.2"/>',
  download: '<path d="M12 4v12.2m0 0-4-4m4 4 4-4"/><path d="M4 15.6v3.2a1.2 1.2 0 0 0 1.2 1.2h13.6a1.2 1.2 0 0 0 1.2-1.2v-3.2"/>',
  refresh: '<path d="M19.8 11.2a7.8 7.8 0 1 0-2.2 6.1"/><path d="M19.8 4.6v6.6h-6.5"/>',
  chevron: '<path d="m6.5 9.2 5.5 5.6 5.5-5.6"/>',
  user: '<circle cx="12" cy="8" r="3.9"/><path d="M4.6 20.4a7.4 7.4 0 0 1 14.8 0"/>',
  users:
    '<circle cx="9" cy="8" r="3.4"/><path d="M2.8 20a6.2 6.2 0 0 1 12.4 0"/><path d="M16.2 5.2a3.4 3.4 0 0 1 0 6.4M18.2 20a5.8 5.8 0 0 0-1.9-4.3"/>',
  clock: '<circle cx="12" cy="12" r="8.4"/><path d="M12 7.4V12l3 1.9"/>',
  filter: '<path d="M3.4 5.2h17.2l-6.8 7.9v5.7l-3.6 1.8v-7.5z"/>',
  more: '<circle cx="5.5" cy="12" r="1.35" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.35" fill="currentColor" stroke="none"/><circle cx="18.5" cy="12" r="1.35" fill="currentColor" stroke="none"/>',
  book: '<path d="M4 4.9A1.9 1.9 0 0 1 5.9 3H19v18H5.9A1.9 1.9 0 0 1 4 19.1z"/><path d="M8 7.4h7.2"/>',
  alert: '<path d="M12 3.2 3 20.3h18z"/><path d="M12 9.2v5M12 17.1h.01"/>'
};

const ico = (name, cls = '') =>
  `<svg${cls ? ` class="${cls}"` : ''} viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${ICONS[name] || ''}</svg>`;

/* ------------------------------------------------------------------ nav */
const NAV = [
  { label: '总览', items: [{ key: 'dashboard', label: '工作台', icon: 'grid' }] },
  {
    label: '教学运行',
    items: [
      { key: 'plan', label: '排课任务', icon: 'list' },
      { key: 'schedule', label: '课表管理', icon: 'calendar', count: '38' },
      { key: 'base', label: '基础数据', icon: 'database' }
    ]
  },
  {
    label: '组织架构',
    items: [
      { key: 'campus', label: '校区管理', icon: 'building' },
      { key: 'college', label: '学院管理', icon: 'book' },
      { key: 'stage', label: '学段管理', icon: 'users' }
    ]
  },
  {
    label: '系统',
    items: [
      { key: 'config', label: '系统配置', icon: 'sliders' },
      { key: 'rbac', label: '权限管理', icon: 'shield' }
    ]
  }
];

const siderNav = (active) => `
  <aside class="sider">
    <div class="brand">
      <div class="brand-mark">${ico('calendar')}</div>
      <div>
        <div class="brand-name">课程排课系统</div>
        <div class="brand-sub">Course Arrange · v2</div>
      </div>
    </div>
    <nav class="nav">
      ${NAV.map(
        (group) => `<div class="nav-group">
          <div class="nav-label">${group.label}</div>
          ${group.items
            .map(
              (item) => `<a class="nav-item${item.key === active ? ' is-active' : ''}" href="#">
                ${ico(item.icon)}
                <span>${item.label}</span>
                ${item.count ? `<span class="nav-count">${item.count}</span>` : ''}
              </a>`
            )
            .join('')}
        </div>`
      ).join('')}
    </nav>
    <div class="sider-foot">
      <div class="sider-version"><span>排课引擎</span><b>GA · 运行中</b></div>
    </div>
  </aside>`;

const topbar = (crumb) => `
  <header class="topbar">
    <div class="crumbs">${crumb.map((c, i) => (i === crumb.length - 1 ? `<b>${c}</b>` : `<span>${c}</span><span class="crumbs-sep">/</span>`)).join('')}</div>
    <div class="top-right">
      <div class="term-chip">${ico('calendar')}<span>学期</span><b>2025-2026-1</b>${ico('chevron', 'chev')}</div>
      <div class="searchbox">${ico('search')}<span>搜索班级 / 教师</span></div>
      <button class="icon-btn">${ico('bell')}<i class="dot"></i></button>
      <div class="divider-v"></div>
      <div class="userbox">
        <div class="avatar">李</div>
        <div class="user-meta">
          <div class="user-name">李教务</div>
          <div class="user-role">教务管理员</div>
        </div>
      </div>
    </div>
  </header>`;

const globalbar = (active) => `
  <header class="globalbar">
    <div class="gb-brand">
      <div class="brand-mark">${ico('calendar')}</div>
      <div class="brand-name">课程排课系统</div>
    </div>
    <nav class="gb-nav">
      ${['工作台', '排课任务', '课表管理', '基础数据', '组织架构', '系统']
        .map(
          (label, i) =>
            `<a class="gb-item${i === active ? ' is-active' : ''}" href="#">${label}</a>`
        )
        .join('')}
    </nav>
    <div class="gb-spacer"></div>
    <div class="term-chip">${ico('calendar')}<b>2025-2026-1</b>${ico('chevron', 'chev')}</div>
    <button class="icon-btn">${ico('bell')}<i class="dot"></i></button>
    <div class="avatar">李</div>
    <div class="user-meta">
      <div class="user-name">李教务</div>
      <div class="user-role">教务管理员</div>
    </div>
  </header>`;

function page({ title, theme = 'a', layout = 'sider', active, crumb, content }) {
  const body =
    layout === 'topnav'
      ? `${globalbar(active)}${content}`
      : `${siderNav(active)}<div class="main">${topbar(crumb)}${content}</div>`;
  return `<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<title>${title}</title>
<link rel="stylesheet" href="../theme.css">
</head>
<body>
<div class="app${layout === 'topnav' ? ' app--topnav' : ''}" data-theme="${theme}">
${body}
</div>
</body>
</html>`;
}

/* ------------------------------------------------------------------ data */
const TEACHERS = [
  ['T2025001', '张明远', '高级教师', 16, 4, '周三 5-6 · 周五 7-8', '高三数学 · 备课组长', '138 0013 6621', 1],
  ['T2025002', '李文静', '一级教师', 14, 4, '—', '高一英语', '139 0013 8842', 1],
  ['T2025003', '王海涛', '高级教师', 18, 5, '—', '高二物理', '137 0013 2210', 1],
  ['T2025004', '陈思远', '二级教师', 12, 3, '周四 7-8', '高一语文', '136 0013 5507', 1],
  ['T2025005', '刘雅琴', '一级教师', 15, 4, '—', '高二化学', '135 0013 9034', 0],
  ['T2025006', '赵启明', '高级教师', 16, 4, '周二 3-4', '高三生物', '138 0013 4471', 1],
  ['T2025007', '周文博', '一级教师', 14, 4, '—', '高一历史', '139 0013 1186', 1],
  ['T2025008', '孙嘉禾', '二级教师', 12, 3, '周五 5-6', '高二地理', '137 0013 7723', 1],
  ['T2025009', '吴子涵', '高级教师', 16, 4, '—', '高三英语 · 教研组长', '136 0013 9088', 1],
  ['T2025010', '郑晓东', '一级教师', 15, 4, '周一 1-2', '高二数学', '135 0013 3345', 1],
  ['T2025011', '冯若彤', '二级教师', 12, 3, '—', '高一政治', '138 0013 6672', 0],
  ['T2025012', '何家豪', '高级教师', 18, 5, '—', '高三物理', '139 0013 4419', 1],
  ['T2025013', '许静宜', '一级教师', 14, 4, '周三 1-2', '高一化学', '137 0013 2265', 1],
  ['T2025014', '高远航', '二级教师', 12, 3, '—', '高二体育', '136 0013 8810', 1]
];

const AVATAR_COLORS = [
  ['#eff6ff', '#2563eb'],
  ['#ecfdf5', '#059669'],
  ['#fef3c7', '#b45309'],
  ['#fce7f3', '#be185d'],
  ['#f3e8ff', '#7c3aed'],
  ['#e0f2fe', '#0284c7']
];

function teacherRows(count) {
  return TEACHERS.slice(0, count)
    .map(([no, name, title, week, day, forbid, note, tel, on], i) => {
      const [bg, accent] = AVATAR_COLORS[i % AVATAR_COLORS.length];
      return `<tr>
        <td class="cell-mono">${no}</td>
        <td><div class="cell-name"><span class="mini-avatar" style="background:${bg};color:${accent}">${name[0]}</span>${name}</div></td>
        <td>${title}</td>
        <td class="cell-num">${week}</td>
        <td class="cell-num">${day}</td>
        <td class="cell-note">${forbid}</td>
        <td class="cell-note">${note}</td>
        <td class="cell-mono">${tel}</td>
        <td><span class="status ${on ? 'status--on' : 'status--off'}">${on ? '启用' : '停用'}</span></td>
        <td><div class="ops"><a class="link">编辑</a><a class="link link--muted">${on ? '停用' : '启用'}</a><a class="link link--danger">删除</a></div></td>
      </tr>`;
    })
    .join('');
}

const COLS = `<colgroup>
  <col style="width:8.6%"><col style="width:8.4%"><col style="width:8.6%"><col style="width:7.6%">
  <col style="width:7.6%"><col style="width:12.6%"><col style="width:14.4%"><col style="width:10.4%">
  <col style="width:7.2%"><col style="width:14.6%">
</colgroup>`;

const teacherPanel = (count, pageSize, pagerText = '') => `
<div class="panel">
  <div class="tabs">
    <div class="tab is-active">教师管理</div>
    <div class="tab">学生管理</div>
    <div class="tab">课程管理</div>
    <div class="tab">教室管理</div>
    <div class="tabs-meta">共 <b>128</b> 位教师 · 启用 <b>121</b> · 停用 7</div>
  </div>
  <div class="toolbar">
    <div class="field field--search">${ico('search')}<span>搜索教师姓名 / 工号</span></div>
    <div class="field field--sm"><span>全部状态</span>${ico('chevron', 'chev')}</div>
    <span class="spacer"></span>
    <button class="btn btn--ghost btn--sm">${ico('upload')}导入教师</button>
    <button class="btn btn--ghost btn--sm">${ico('download')}下载模板</button>
    <button class="btn btn--ghost btn--sm">${ico('refresh')}重置</button>
  </div>
  <div class="table-wrap">
    <table>${COLS}
      <thead><tr>
        <th>教师编号</th><th>姓名</th><th>职称</th><th>周上限</th><th>日上限</th>
        <th>禁排时间</th><th>授课说明</th><th>联系电话</th><th>状态</th><th>操作</th>
      </tr></thead>
      <tbody>${teacherRows(count)}</tbody>
    </table>
  </div>
  <div class="pager">
    <span>${pagerText || `共 128 条记录 · 每页 ${pageSize} 条 · 第 1 / 13 页`}</span>
    <div class="pager-nums">
      <span class="pnum pnum--arrow">‹</span>
      <span class="pnum is-active">1</span>
      <span class="pnum">2</span>
      <span class="pnum">3</span>
      <span class="pnum">…</span>
      <span class="pnum">13</span>
      <span class="pnum pnum--arrow">›</span>
    </div>
  </div>
</div>`;

const headTeacher = (compact = false) => `
  <div class="page-head">
    <div class="page-head__main">
      <h1>教师管理</h1>
      <span class="count-pill">128</span>
      ${compact ? '' : '<span class="page-desc">维护教师档案、课时上限与禁排时间</span>'}
    </div>
    <div class="page-actions">
      <button class="btn">${ico('download')}导出名单</button>
      <button class="btn btn--primary">${ico('plus')}新增教师</button>
    </div>
  </div>`;

/* ------------------------------------------------------------------ 01 / 02 */
writeFileSync(
  join(outDir, '01-teacher-a.html'),
  page({
    title: '教师管理 · 方向 A',
    theme: 'a',
    active: 'base',
    crumb: ['基础数据', '教师管理'],
    content: `<main class="content">${headTeacher()}${teacherPanel(10, 10)}</main>`
  })
);

writeFileSync(
  join(outDir, '02-teacher-b.html'),
  page({
    title: '教师管理 · 方向 B',
    theme: 'b',
    active: 'base',
    crumb: ['基础数据', '教师管理'],
    content: `<main class="content">${headTeacher()}${teacherPanel(10, 10)}</main>`
  })
);

/* ------------------------------------------------------------------ 03 顶栏全宽 */
const headTeacherTop = `
  <div class="page-head">
    <div class="page-head__main">
      <h1>教师管理</h1>
      <span class="count-pill">128</span>
      <span class="page-desc">教师档案、课时上限与禁排时间</span>
    </div>
    <div class="page-actions">
      <div class="field field--search">${ico('search')}<span>搜索教师姓名 / 工号</span></div>
      <div class="field field--sm"><span>全部状态</span>${ico('chevron', 'chev')}</div>
      <button class="btn btn--ghost btn--sm">${ico('upload')}导入</button>
      <button class="btn btn--ghost btn--sm">${ico('download')}导出</button>
      <button class="btn btn--ghost btn--sm">${ico('more')}</button>
      <button class="btn btn--primary btn--sm">${ico('plus')}新增教师</button>
    </div>
  </div>`;

writeFileSync(
  join(outDir, '03-teacher-c.html'),
  page({
    title: '教师管理 · 方向 C',
    layout: 'topnav',
    active: 3,
    content: `<main class="content">${headTeacherTop}${teacherPanel(13, 20, '共 128 条记录 · 第 1 / 7 页')}</main>`
  })
);

/* ------------------------------------------------------------------ 04 课表 */
const PERIODS = [
  { index: 1, range: '08:00-08:45', group: 'MORNING' },
  { index: 2, range: '08:55-09:40', group: 'MORNING' },
  { index: 3, range: '10:00-10:45', group: 'MORNING' },
  { index: 4, range: '10:55-11:40', group: 'MORNING' },
  { index: 5, range: '14:00-14:45', group: 'AFTERNOON' },
  { index: 6, range: '14:55-15:40', group: 'AFTERNOON' }
];

const PALETTE = {
  math: { bg: '#eff6ff', accent: '#3b82f6', text: '#1e40af' },
  english: { bg: '#ecfdf5', accent: '#10b981', text: '#065f46' },
  data: { bg: '#f3e8ff', accent: '#8b5cf6', text: '#5b21b6' },
  physics: { bg: '#e0f2fe', accent: '#06b6d4', text: '#155e75' },
  sport: { bg: '#fff7ed', accent: '#f97316', text: '#9a3412' },
  program: { bg: '#f0fdf4', accent: '#22c55e', text: '#166534' },
  seminar: { bg: '#fce7f3', accent: '#ec4899', text: '#9d174d' }
};

const DAYS = ['周一', '周二', '周三', '周四', '周五'];

const GRID = {
  '1-1': ['高等数学', '张明远', 'A-302', 'math'],
  '1-2': ['高等数学', '张明远', 'A-302', 'math'],
  '1-3': ['大学英语', '李文静', 'B-205', 'english'],
  '1-5': ['数据结构', '王海涛', 'C-108', 'data'],
  '1-6': ['体育', '赵启明', '操场', 'sport'],
  '2-1': ['线性代数', '陈思远', 'A-210', 'math'],
  '2-3': ['程序设计', '周文博', 'C-201', 'program'],
  '2-4': ['程序设计', '周文博', 'C-201', 'program'],
  '2-5': ['大学物理', '刘雅琴', 'B-101', 'physics'],
  '3-1': ['大学英语', '李文静', 'B-205', 'english'],
  '3-2': ['大学英语', '李文静', 'B-205', 'english'],
  '3-3': ['概率论', '孙嘉禾', 'A-305', 'math'],
  '3-5': ['数据结构', '王海涛', 'C-108', 'data'],
  '3-6': ['体育', '赵启明', '操场', 'sport'],
  '4-1': ['大学物理', '刘雅琴', 'B-101', 'physics'],
  '4-2': ['大学物理', '刘雅琴', 'B-101', 'physics'],
  '4-4': ['学术写作', '吴子涵', 'B-302', 'seminar'],
  '4-5': ['程序设计', '周文博', 'C-201', 'program'],
  '5-1': ['概率论', '孙嘉禾', 'A-305', 'math'],
  '5-3': ['高等数学', '张明远', 'A-302', 'math'],
  '5-4': ['高等数学', '张明远', 'A-302', 'math'],
  '5-6': ['学术写作', '吴子涵', 'B-302', 'seminar']
};

const infoIcon = (kind) =>
  kind === 'teacher'
    ? '<svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>'
    : '<svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>';

const courseCell = (item) => {
  const p = PALETTE[item[3]];
  return `<div class="course-card" style="--course-accent:${p.accent};--course-text:${p.text};background:${p.bg}">
    <div class="course-accent"></div>
    <div class="course-body">
      <div class="course-name">${item[0]}</div>
      <div class="course-info">
        <span class="course-teacher">${infoIcon('teacher')}${item[1]}</span>
        <span class="course-room">${infoIcon('room')}${item[2]}</span>
      </div>
    </div>
  </div>`;
};

const emptyCell = `<div class="empty-slot">
  <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/></svg>
</div>`;

const scheduleGrid = `
  <div class="timetable-grid">
    <div class="grid-head time-head">时间</div>
    ${DAYS.map((d) => `<div class="grid-head">${d}</div>`).join('')}
    ${PERIODS.map(
      (p) => `<div class="time-cell time-${p.group.toLowerCase()}">
        <div class="time-badge">${p.index}</div>
        <div class="time-range">${p.range}</div>
      </div>
      ${DAYS.map((_, di) => {
        const item = GRID[`${di + 1}-${p.index}`];
        return `<div class="grid-cell">${item ? courseCell(item) : emptyCell}</div>`;
      }).join('')}`
    ).join('')}
  </div>`;

const schedulePage = `
  <main class="content">
    <div class="sched-head">
      <div class="sched-title">课表管理</div>
      <div class="sched-divider"></div>
      <div class="view-toggle">
        <button class="toggle-btn toggle-btn--active">班级</button>
        <button class="toggle-btn">教师</button>
      </div>
      <div class="stat-badges">
        <span class="stat-badge"><span class="stat-dot stat-dot--primary"></span>班级: <strong>高三(1)班</strong></span>
        <span class="stat-badge"><span class="stat-dot stat-dot--course"></span>课程 <strong>12</strong></span>
        <span class="stat-badge"><span class="stat-dot stat-dot--teacher"></span>教师 <strong>9</strong></span>
        <span class="stat-badge"><span class="stat-dot stat-dot--room"></span>教室 <strong>7</strong></span>
      </div>
      <div class="sched-spacer"></div>
      <div class="sched-select sched-select--semester"><span>2025-2026-1</span>${ico('chevron')}</div>
      <div class="sched-select sched-select--target"><span>高三(1)班</span>${ico('chevron')}</div>
      <button class="btn btn--primary">${ico('search')}查询课表</button>
    </div>
    <div class="timetable-wrapper">
      <div class="period-groups">
        <div class="period-group-chip period-group-chip--active">全部</div>
        <div class="period-group-chip">上午</div>
        <div class="period-group-chip">下午</div>
      </div>
      ${scheduleGrid}
    </div>
  </main>`;

writeFileSync(
  join(outDir, '04-schedule-a.html'),
  page({
    title: '课表管理 · 方向 A · 表格样式保持原样',
    theme: 'a',
    active: 'schedule',
    crumb: ['教学运行', '课表管理'],
    content: schedulePage
  })
);

/* ------------------------------------------------------------------ 05 工作台 */
const KPI = [
  { label: '本学期排课任务', dot: 'linear-gradient(135deg,#165dff,#0ea5a4)', value: '42', unit: '个', foot: '<span class="trend-up">本周 +6</span> · 覆盖 3 个年级', },
  { label: '已生成课表', dot: '#10b981', value: '38', unit: '/ 42', foot: '完成率 <b>90.5%</b> · 待生成 4 份' },
  { label: '待处理', dot: '#f59e0b', value: '4', unit: '项', foot: '<span class="trend-warn">2 个时间冲突</span> · 2 个未排成功' },
  { label: '教学资源', dot: '#8b5cf6', value: '128', unit: '/ 36', foot: '教师 / 班级 · 教室 <b>54</b> 间在用' }
];

const kpiRow = `<div class="kpi-row">${KPI.map(
  (k) => `<div class="kpi">
    <div class="kpi-label"><span class="dot" style="background:${k.dot}"></span>${k.label}</div>
    <div class="kpi-value">${k.value}<span>${k.unit}</span></div>
    <div class="kpi-foot">${k.foot}</div>
  </div>`
).join('')}</div>`;

const TASKS = [
  ['#A2401', '高三(1)班', '2025-2026-1', '已完成', 'ok', '10-24 14:20'],
  ['#A2402', '高二(3)班', '2025-2026-1', '执行中 82%', 'run', '10-24 14:05'],
  ['#A2403', '高一(2)班', '2025-2026-1', '待执行', 'idle', '10-24 13:58'],
  ['#A2404', '高三(5)班', '2025-2026-1', '冲突 2', 'warn', '10-24 13:40'],
  ['#A2405', '初二(1)班', '2025-2026-1', '已完成', 'ok', '10-24 11:26'],
  ['#A2406', '高二(1)班', '2025-2026-1', '已完成', 'ok', '10-24 11:02'],
  ['#A2407', '高三(2)班', '2025-2026-1', '已完成', 'ok', '10-24 10:48'],
  ['#A2408', '高一(4)班', '2025-2026-1', '待执行', 'idle', '10-24 10:31'],
  ['#A2409', '初三(2)班', '2025-2026-1', '已完成', 'ok', '10-24 09:55'],
  ['#A2410', '初二(3)班', '2025-2026-1', '已完成', 'ok', '10-24 09:20'],
  ['#A2411', '高一(3)班', '2025-2026-1', '已完成', 'ok', '10-24 08:56']
];

const statusChip = (text, kind) => {
  if (kind === 'ok') return `<span class="status status--on">${text}</span>`;
  if (kind === 'run') return `<span class="status" style="background:#eaf2ff;color:#165dff">${text}</span>`;
  if (kind === 'warn') return `<span class="status" style="background:#fff4e6;color:#d97706">${text}</span>`;
  return `<span class="status status--off">${text}</span>`;
};

const taskTable = `<div class="table-wrap">
  <table class="task-table">
    <colgroup><col style="width:13%"><col style="width:18%"><col style="width:22%"><col style="width:22%"><col style="width:25%"></colgroup>
    <thead><tr><th>任务编号</th><th>班级</th><th>学期</th><th>状态</th><th>更新时间</th></tr></thead>
    <tbody>${TASKS.map(
      ([no, cls, term, st, kind, time]) => `<tr>
        <td class="cell-mono">${no}</td>
        <td style="color:var(--ink);font-weight:500">${cls}</td>
        <td class="cell-note">${term}</td>
        <td>${statusChip(st, kind)}</td>
        <td class="cell-note">${time}</td>
      </tr>`
    ).join('')}</tbody>
  </table>
</div>`;

const dayItem = (range, idx, name, meta, room, key) => {
  const p = PALETTE[key];
  return `<div class="day-row">
    <div class="day-time">${range}<span>第 ${idx} 节</span></div>
    <div class="day-card" style="background:${p.bg};--accent:${p.accent};--text:${p.text}">
      <div>
        <div class="day-card__name">${name}</div>
        <div class="day-card__meta">${meta}</div>
      </div>
      <div class="day-card__room">${room}</div>
    </div>
  </div>`;
};

const dayList = `<div class="section-body day-list">
  ${dayItem('08:00', 1, '高等数学', '张明远 · 高三(1)班', 'A-302', 'math')}
  ${dayItem('08:55', 2, '高等数学', '张明远 · 高三(1)班', 'A-302', 'math')}
  ${dayItem('10:00', 3, '大学英语', '李文静 · 高三(1)班', 'B-205', 'english')}
  ${dayItem('14:00', 5, '数据结构', '王海涛 · 高三(1)班', 'C-108', 'data')}
</div>`;

const logs = [
  ['高三(1)班 · 大学英语', '李文静 周一 3-4 → 周三 1-2', '李教务 · 10:42'],
  ['高二(3)班 · 数据结构', '王海涛 周五 5-6 → 周四 5-6', '王主任 · 09:18'],
  ['高一(2)班 · 体育', '赵启明 周四 7-8 → 周五 7-8', '李教务 · 昨天 16:30']
];

const logList = `<div class="section-body">
  ${logs
    .map(
      ([title, meta, by]) => `<div class="log-item">
        <div class="log-dot"></div>
        <div class="log-main">
          <div class="log-title">${title}</div>
          <div class="log-meta">${meta}</div>
          <div class="log-meta">${by}</div>
        </div>
      </div>`
    )
    .join('')}
</div>`;

const dashboardContent = `
  <main class="content">
    <div class="page-head">
      <div class="page-head__main">
        <h1>工作台</h1>
        <span class="page-desc">2025-2026 学年第一学期 · 第 12 教学周</span>
      </div>
      <div class="page-actions">
        <button class="btn">${ico('calendar')}查看本周课表</button>
        <button class="btn btn--primary">${ico('refresh')}执行排课</button>
      </div>
    </div>
    ${kpiRow}
    <div class="dash-grid">
      <section class="section">
        <div class="section-head">
          <div class="section-title">${ico('list')}最近排课任务</div>
          <span class="section-sub">近 24 小时 · 共 10 条</span>
        </div>
        ${taskTable}
        <div class="pager" style="height:40px">
          <span>本月累计生成 128 张课表 · 冲突率 3.1%</span>
          <span class="link">查看全部任务</span>
        </div>
      </section>
      <div style="display:flex;flex-direction:column;gap:12px;min-height:0">
        <section class="section" style="flex:1.12">
          <div class="section-head">
            <div class="section-title">${ico('clock')}今日课表</div>
            <span class="section-sub">高三(1)班</span>
          </div>
          ${dayList}
        </section>
        <section class="section" style="flex:0.88">
          <div class="section-head">
            <div class="section-title">${ico('refresh')}最近调课</div>
            <span class="section-sub">今日 3 条</span>
          </div>
          ${logList}
        </section>
      </div>
    </div>
  </main>`;

writeFileSync(
  join(outDir, '05-dashboard-a.html'),
  page({
    title: '工作台 · 方向 A',
    theme: 'a',
    active: 'dashboard',
    crumb: ['总览', '工作台'],
    content: dashboardContent
  })
);

console.log('built', outDir);
