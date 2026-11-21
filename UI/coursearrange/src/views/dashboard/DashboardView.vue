<template>
  <section v-loading="loading" class="dashboard-shell">
    <PageHeader
      title="工作台"
      :description="headerDescription"
    >
      <template #actions>
        <el-button class="ghost-action" @click="goTo('/schedule')">查看本周课表</el-button>
        <el-button class="primary-action" type="primary" @click="goTo('/course-plan')">执行排课</el-button>
      </template>
    </PageHeader>

    <div v-if="loadError" class="dash-error" role="alert">
      <span class="dash-error__text">{{ loadError }}</span>
      <el-button size="small" @click="loadOverview">重试</el-button>
    </div>

    <div class="kpi-row">
      <article class="kpi">
        <div class="kpi-label"><span class="kpi-dot kpi-dot--brand" />本学期排课任务</div>
        <div class="kpi-value">{{ stats.total }}<span>个</span></div>
        <div class="kpi-foot">最近任务 {{ recentTasks.length }} 条 · 已生成 {{ stats.completed }}</div>
      </article>
      <article class="kpi">
        <div class="kpi-label"><span class="kpi-dot kpi-dot--teal" />已生成课表</div>
        <div class="kpi-value">{{ stats.completed }}<span>/ {{ stats.total }}</span></div>
        <div class="kpi-foot">
          完成率 <b>{{ completionRate }}</b>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-label"><span class="kpi-dot kpi-dot--amber" />待处理</div>
        <div class="kpi-value">{{ stats.pending }}<span>项</span></div>
        <div class="kpi-foot">
          <b class="trend-warn">{{ stats.conflict }}</b> 个冲突待核对
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-label"><span class="kpi-dot kpi-dot--violet" />教学资源</div>
        <div class="kpi-value">{{ resources.teacher }}<span>位教师</span></div>
        <div class="kpi-foot">班级 {{ resources.class }} · 教室 {{ resources.classroom }}</div>
      </article>
    </div>

    <div class="dash-grid">
      <section class="panel panel--tasks">
        <header class="panel-head">
          <div class="panel-title">
            <span class="panel-title__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" />
              </svg>
            </span>
            最近排课任务
          </div>
          <span class="panel-sub">按更新时间排序</span>
        </header>
        <div class="panel-body panel-body--flush">
          <el-table v-if="recentTasks.length" :data="recentTasks" size="small" max-height="420">
            <el-table-column label="任务编号" min-width="90">
              <template #default="{ row }">
                <span class="cell-mono">{{ row.taskCode || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="班级" min-width="92">
              <template #default="{ row }">
                <span class="cell-strong">{{ row.classNo || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="学期" min-width="100">
              <template #default="{ row }">
                <span class="cell-mono">{{ row.semester || semester || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="104">
              <template #default="{ row }">
                <span class="status" :class="`status--${resolveTaskStatus(row.status).tone}`">
                  {{ resolveTaskStatus(row.status).label }}
                  <template v-if="Number(row.conflictCount) > 0"> {{ row.conflictCount }}</template>
                </span>
              </template>
            </el-table-column>
            <el-table-column label="更新时间" min-width="96" align="right">
              <template #default="{ row }">
                <span class="cell-mono">{{ formatDateTime(row.updatedAt) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-else class="panel-empty">
            <p>{{ loadError ? '任务数据暂不可用' : '当前学期还没有排课任务' }}</p>
            <el-button size="small" @click="goTo('/course-plan')">去排课任务</el-button>
          </div>
        </div>
      </section>

      <div class="dash-col">
        <section class="panel">
          <header class="panel-head">
            <div class="panel-title">
              <span class="panel-title__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="17" rx="3" />
                  <path d="M3 9h18M8 2v4M16 2v4" />
                </svg>
              </span>
              今日课表
            </div>
            <span class="panel-sub">{{ todayTimetable.classNo || '暂无班级' }}</span>
          </header>
          <div class="panel-body">
            <ul v-if="todayItems.length" class="day-list">
              <li v-for="(item, index) in todayItems" :key="`${item.periodIndex}-${index}`" class="day-row">
                <div class="day-time">
                  {{ item.startTime || '--:--' }}
                  <span>第 {{ item.periodIndex ?? index + 1 }} 节</span>
                </div>
                <div class="day-card" :style="getDayCardStyle(index)">
                  <div class="day-card__main">
                    <div class="day-card__name">{{ item.courseName || '未命名课程' }}</div>
                    <div class="day-card__meta">
                      {{ item.teacherName || '--' }}
                      <template v-if="todayTimetable.classNo"> · {{ todayTimetable.classNo }}</template>
                    </div>
                  </div>
                  <span class="day-card__room">{{ item.classroomNo || '--' }}</span>
                </div>
              </li>
            </ul>
            <div v-else class="panel-empty">
              <p>{{ loadError ? '今日课表暂不可用' : '今天没有排课' }}</p>
            </div>
          </div>
        </section>

        <section class="panel">
          <header class="panel-head">
            <div class="panel-title">
              <span class="panel-title__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12a9 9 0 1 1-3-6.7" />
                  <path d="M21 3v6h-6" />
                </svg>
              </span>
              最近调课
            </div>
            <span class="panel-sub">共 {{ recentAdjustments.length }} 条</span>
          </header>
          <div class="panel-body">
            <ul v-if="recentAdjustments.length" class="log-list">
              <li v-for="(item, index) in recentAdjustments" :key="`${item.createTime}-${index}`" class="log-item">
                <span class="log-dot" aria-hidden="true" />
                <div class="log-main">
                  <div class="log-title">
                    {{ item.className || item.classNo || '--' }} · {{ item.courseName || '--' }}
                  </div>
                  <div class="log-route">
                    {{ item.beforeClassTime || '--' }} → {{ item.afterClassTime || '--' }}
                  </div>
                  <div class="log-meta">
                    {{ item.operatorName || '--' }} · {{ formatDateTime(item.createTime) }}
                  </div>
                </div>
              </li>
            </ul>
            <div v-else class="panel-empty">
              <p>{{ loadError ? '调课记录暂不可用' : '最近没有调课记录' }}</p>
            </div>
          </div>
        </section>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/PageHeader.vue';
import { fetchDashboardOverview } from '@/api/modules/dashboard';
import { getErrorMessage } from '@/utils/http';

const router = useRouter();
const loading = ref(false);
const loadError = ref('');
const overview = ref(null);

const DAY_CARD_COLORS = [
  { bg: '#eff6ff', accent: '#3b82f6', text: '#1e40af' },
  { bg: '#ecfdf5', accent: '#10b981', text: '#065f46' },
  { bg: '#f3e8ff', accent: '#8b5cf6', text: '#5b21b6' },
  { bg: '#fef3c7', accent: '#f59e0b', text: '#92400e' },
  { bg: '#fce7f3', accent: '#ec4899', text: '#9d174d' },
  { bg: '#e0f2fe', accent: '#06b6d4', text: '#155e75' }
];

const TASK_STATUS_MAP = {
  COMPLETED: { label: '已完成', tone: 'ok' },
  SUCCESS: { label: '已完成', tone: 'ok' },
  RUNNING: { label: '执行中', tone: 'run' },
  IN_PROGRESS: { label: '执行中', tone: 'run' },
  PENDING: { label: '待执行', tone: 'idle' },
  WAITING: { label: '待执行', tone: 'idle' },
  CONFLICT: { label: '冲突', tone: 'warn' },
  FAILED: { label: '失败', tone: 'bad' }
};

function toNumber(value) {
  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : 0;
}

function toArray(value) {
  return Array.isArray(value) ? value : [];
}

const semester = computed(() => overview.value?.semester || '');
const stats = computed(() => {
  const taskStats = overview.value?.taskStats || {};
  return {
    total: toNumber(taskStats.total),
    completed: toNumber(taskStats.completed),
    pending: toNumber(taskStats.pending),
    conflict: toNumber(taskStats.conflict)
  };
});
const completionRate = computed(() => {
  if (!stats.value.total) {
    return '--';
  }
  return `${Math.round((stats.value.completed / stats.value.total) * 100)}%`;
});
const resources = computed(() => {
  const counts = overview.value?.resourceCounts || {};
  return {
    teacher: toNumber(counts.teacher),
    class: toNumber(counts.class),
    classroom: toNumber(counts.classroom)
  };
});
const recentTasks = computed(() => toArray(overview.value?.recentTasks));
const todayTimetable = computed(() => overview.value?.todayTimetable || {});
const todayItems = computed(() => toArray(todayTimetable.value?.items));
const recentAdjustments = computed(() => toArray(overview.value?.recentAdjustments));
const headerDescription = computed(() => {
  if (semester.value) {
    return `${semester.value} 学期排课主链概览`;
  }
  return '排课任务、今日课表与调课的实时概览';
});

function resolveTaskStatus(status) {
  if (!status) {
    return { label: '未知', tone: 'idle' };
  }
  const normalized = String(status).trim().toUpperCase();
  return TASK_STATUS_MAP[normalized] || { label: String(status), tone: 'idle' };
}

function formatDateTime(value) {
  if (!value) {
    return '--';
  }
  const text = String(value).replace('T', ' ');
  const matched = text.match(/(\d{4})-(\d{2})-(\d{2})[ ]?(\d{2}:\d{2})/);
  if (matched) {
    return `${matched[2]}-${matched[3]} ${matched[4]}`;
  }
  return text;
}

function getDayCardStyle(index) {
  const color = DAY_CARD_COLORS[index % DAY_CARD_COLORS.length];
  return {
    '--day-bg': color.bg,
    '--day-accent': color.accent,
    '--day-text': color.text,
    background: color.bg
  };
}

function goTo(path) {
  router.push(path).catch(() => {});
}

async function loadOverview() {
  loading.value = true;
  loadError.value = '';
  try {
    const response = await fetchDashboardOverview();
    overview.value = response?.data || null;
  } catch (error) {
    overview.value = null;
    loadError.value = getErrorMessage(error, '工作台数据加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

onMounted(loadOverview);
</script>

<style scoped>
.dashboard-shell {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ---------------------------------------------------------------- 错误提示 */
.dash-error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 12px;
  border: 1px solid #f6d3d1;
  border-radius: var(--radius-control);
  background: #fdf3f2;
  color: #a4392f;
  font-size: 12.5px;
}

.dash-error__text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------------------------------------------------------------- KPI */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi {
  padding: 12px 14px 11px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--surface);
  box-shadow: var(--shadow-soft);
}

.kpi-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--muted-2);
  font-size: 12px;
}

.kpi-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.kpi-dot--brand {
  background: var(--brand);
}

.kpi-dot--teal {
  background: var(--teal);
}

.kpi-dot--amber {
  background: #f59e0b;
}

.kpi-dot--violet {
  background: #8b5cf6;
}

.kpi-value {
  margin-top: 5px;
  color: var(--ink);
  font-size: 25px;
  font-weight: 650;
  line-height: 1.1;
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
}

.kpi-value span {
  margin-left: 4px;
  color: var(--muted-3);
  font-size: 12px;
  font-weight: 500;
}

.kpi-foot {
  margin-top: 4px;
  color: var(--muted-2);
  font-size: 11.5px;
}

.kpi-foot b {
  font-weight: 600;
}

.trend-warn {
  color: #e08300;
}

/* ---------------------------------------------------------------- 面板 */
.dash-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(0, 1fr);
  gap: 12px;
  align-items: stretch;
}

.dash-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface);
  box-shadow: var(--shadow);
}

.panel-head {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  height: 42px;
  padding: 0 14px;
  border-bottom: 1px solid #f0f5fa;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 7px;
  color: var(--ink);
  font-size: 13.5px;
  font-weight: 600;
  white-space: nowrap;
}

.panel-title__icon {
  display: grid;
  place-items: center;
  width: 15px;
  height: 15px;
  color: var(--brand);
}

.panel-title__icon svg {
  width: 15px;
  height: 15px;
}

.panel-sub {
  overflow: hidden;
  color: var(--muted-2);
  font-size: 11.5px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.panel-body {
  flex: 1;
  min-height: 0;
  padding: 4px 14px;
}

.panel--tasks .panel-body {
  display: flex;
  flex-direction: column;
}

.panel-body--flush {
  padding: 0;
}

.panel-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 120px;
  color: var(--muted-2);
  font-size: 12.5px;
  text-align: center;
}

.cell-mono {
  color: var(--muted);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.cell-strong {
  color: var(--ink);
  font-weight: 500;
}

/* 状态徽章 */
.status {
  display: inline-flex;
  align-items: center;
  height: 21px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 500;
  white-space: nowrap;
}

.status--ok {
  background: #eafaf1;
  color: #0f9d63;
}

.status--run {
  background: var(--brand-soft);
  color: var(--brand);
}

.status--idle {
  background: #f2f5f9;
  color: #7b8ca0;
}

.status--warn {
  background: #fff5e6;
  color: #c46a00;
}

.status--bad {
  background: #fdf1f0;
  color: #c0392b;
}

/* 今日课表 */
.day-list,
.log-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.day-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f2f7fc;
}

.day-row:last-child {
  border-bottom: none;
}

.day-time {
  flex: 0 0 62px;
  color: var(--muted);
  font-size: 11.5px;
  font-weight: 600;
  line-height: 1.5;
}

.day-time span {
  display: block;
  color: var(--muted-3);
  font-size: 10.5px;
  font-weight: 500;
}

.day-card {
  position: relative;
  display: flex;
  flex: 1;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 7px 10px 7px 12px;
  overflow: hidden;
  border-radius: 10px;
}

.day-card::before {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 3px;
  background: var(--day-accent);
  content: '';
}

.day-card__main {
  min-width: 0;
}

.day-card__name {
  overflow: hidden;
  color: var(--day-text);
  font-size: 12.5px;
  font-weight: 600;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.day-card__meta {
  margin-top: 2px;
  overflow: hidden;
  color: var(--muted-2);
  font-size: 11px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.day-card__room {
  margin-left: auto;
  color: var(--muted);
  font-size: 11px;
  white-space: nowrap;
}

/* 最近调课 */
.log-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f2f7fc;
}

.log-item:last-child {
  border-bottom: none;
}

.log-dot {
  flex: 0 0 6px;
  width: 6px;
  height: 6px;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--grad);
}

.log-main {
  min-width: 0;
}

.log-title {
  overflow: hidden;
  color: var(--ink-2);
  font-size: 12.5px;
  font-weight: 500;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.log-route {
  margin-top: 3px;
  color: var(--brand);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}

.log-meta {
  margin-top: 2px;
  color: var(--muted-2);
  font-size: 11px;
}

/* ---------------------------------------------------------------- 响应式 */
@media (max-width: 1280px) {
  .dash-grid {
    grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr);
  }
}

@media (max-width: 1024px) {
  .kpi-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dash-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .kpi-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .panel-body {
    padding: 4px 10px;
  }

  .panel-body--flush {
    padding: 0;
  }

  .day-time {
    flex: 0 0 54px;
  }

  .dash-error {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
