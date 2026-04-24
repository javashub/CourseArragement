<template>
  <section class="schedule-shell">
    <!-- 顶部卡片：三行布局 -->
    <div class="header-card">
      <!-- 第一行：标题 -->
      <div class="header-row title-row">
        <h1 class="page-title">课表管理</h1>
      </div>

      <!-- 第二行：视角按钮 + 统计数据 -->
      <div class="header-row stats-row">
        <div class="view-toggle">
          <button
            class="toggle-btn"
            :class="{ 'toggle-btn--active': viewMode === 'teacher' }"
            @click="viewMode = 'teacher'; handleViewModeChange()"
          >
            教师
          </button>
          <button
            class="toggle-btn"
            :class="{ 'toggle-btn--active': viewMode === 'class' }"
            @click="viewMode = 'class'; handleViewModeChange()"
          >
            班级
          </button>
        </div>
        <div class="stat-badges">
          <span class="stat-badge">
            {{ viewMode === 'class' ? '班级' : '教师' }}: <strong>{{ currentTarget || '--' }}</strong>
          </span>
          <span class="stat-badge stat-badge--accent">
            课程 <strong>{{ planList.length }}</strong>
          </span>
          <span class="stat-badge stat-badge--accent">
            教师 <strong>{{ teacherCount }}</strong>
          </span>
          <span class="stat-badge stat-badge--accent">
            教室 <strong>{{ classroomCount }}</strong>
          </span>
        </div>
      </div>

      <!-- 第三行：筛选条件 + 查询按钮 -->
      <div class="header-row filter-row">
        <el-select
          v-model="selectedSemester"
          filterable
          clearable
          allow-create
          default-first-option
          placeholder="学期"
          class="filter-select"
          @change="handleSemesterChange"
          @clear="handleSemesterClear"
        >
          <el-option v-for="item in semesterOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select
          v-if="viewMode === 'class'"
          v-model="selectedClassNo"
          filterable
          clearable
          placeholder="班级"
          class="filter-select"
          @change="handleTargetChange"
          @clear="clearPlan"
        >
          <el-option v-for="item in classOptions" :key="item.classNo" :label="`${item.classNo} ${item.className || ''}`" :value="item.classNo" />
        </el-select>
        <el-select
          v-else
          v-model="selectedTeacherNo"
          filterable
          clearable
          placeholder="教师"
          class="filter-select"
          @change="handleTargetChange"
          @clear="clearPlan"
        >
          <el-option
            v-for="item in teacherOptions"
            :key="item.teacherNo"
            :label="`${item.teacherNo} ${item.teacherName || ''}`"
            :value="item.teacherNo"
          />
        </el-select>
        <el-button type="primary" class="query-btn" @click="loadCoursePlan">查询课表</el-button>
      </div>
    </div>

    <!-- 课表主体 -->
    <div class="timetable-wrapper" v-loading="loading">
      <el-alert
        v-if="planStatus"
        class="status-alert"
        :type="planStatus.type"
        :title="planStatus.title"
        :description="planStatus.description"
        :closable="false"
        show-icon
      />

      <el-empty v-if="!currentTarget && !loading" :description="viewMode === 'class' ? '请先选择班级，再查看课表' : '请先选择教师，再查看课表'" />
      <el-empty v-else-if="currentTarget && !planList.length && !loading" :description="viewMode === 'class' ? '当前班级还没有生成标准课表' : '当前教师还没有生成标准课表'" />

      <template v-else>
        <!-- 时段分组筛选 -->
        <div class="period-groups" v-if="periodGroups.length">
          <div
            class="period-group-chip"
            :class="{ 'period-group-chip--active': activePeriodGroup === 'ALL' }"
            @click="activePeriodGroup = 'ALL'"
          >
            全部
          </div>
          <div
            v-for="g in periodGroups"
            :key="g.label"
            class="period-group-chip"
            :class="{ 'period-group-chip--active': activePeriodGroup === g.label }"
            @click="activePeriodGroup = g.label"
          >
            {{ g.label }}
          </div>
        </div>

        <div class="timetable-grid" :style="gridStyle">
          <!-- 表头 -->
          <div class="grid-head time-head">时间</div>
          <div v-for="day in weekLabels" :key="day.key" class="grid-head">
            {{ day.label }}
          </div>

          <!-- 时段行 -->
          <template v-for="period in periodLabels" :key="period.key">
            <div class="time-cell" :class="getTimeGroupClass(period)">
              <div class="time-badge">{{ period.index }}</div>
              <div class="time-range" v-if="period.timeRange">{{ period.timeRange }}</div>
            </div>
            <div
              v-for="day in weekLabels"
              :key="`${day.key}-${period.key}`"
              class="grid-cell"
              :class="{
                'drag-target': canDragAdjust,
                'recent-drop-cell': isRecentDropCell(day.index, period.index),
                'focused-cell': focusedCellKey === `${day.index}-${period.index}`,
                'period-dimmed': isPeriodDimmed(period)
              }"
              @dragover="handleDragOver"
              @drop="handleDrop(day.index, period.index)"
            >
              <template v-if="getPlanCell(day.index, period.index).length">
                <div
                  v-for="item in getPlanCell(day.index, period.index)"
                  :key="item.id"
                  class="course-card"
                  :class="{
                    'dragging-card': dragPlanId === item.id,
                    'recent-adjust-card': isRecentAdjustedPlan(item),
                    'focused-card': focusedPlanId === item.id,
                    'card-dimmed': isPeriodDimmed(period)
                  }"
                  :style="getCardStyle(item)"
                  :draggable="canDragAdjust"
                  @dragstart="handleDragStart(item)"
                  @dragend="handleDragEnd"
                >
                  <div class="course-accent" />
                  <div class="course-body">
                    <div class="course-name">{{ item.courseName || item.courseNo }}</div>
                    <div class="course-info">
                      <span class="course-teacher">
                        <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                        {{ item.teacherName || item.teacherNo }}
                      </span>
                      <span class="course-room">
                        <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                        {{ item.classroomNo || '--' }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
              <div v-else class="empty-slot">
                <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/></svg>
                <span v-if="canDragAdjust">拖入调课</span>
              </div>
            </div>
          </template>
        </div>
      </template>
    </div>

    <!-- 调课记录 -->
    <el-card shadow="never" class="log-card" v-if="adjustLogs.length">
      <template #header>
        <div class="card-head">
          <div>
            <div class="card-title">最近调课记录</div>
          </div>
          <div class="header-tags">
            <el-input
              v-model="adjustLogKeyword"
              clearable
              class="log-search"
              placeholder="搜索操作人 / 班级 / 教师"
              @clear="handleAdjustLogFilter"
              @input="handleAdjustLogFilter"
            />
          </div>
        </div>
      </template>

      <el-table :data="filteredAdjustLogs" size="small" stripe max-height="280">
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="classNo" label="班级" width="110" />
        <el-table-column prop="teacherNo" label="教师" width="120" />
        <el-table-column label="调整" min-width="160">
          <template #default="{ row }">
            <span class="adjust-route">{{ row.beforeClassTime }} → {{ row.afterClassTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column prop="createTime" label="时间" min-width="170" />
        <el-table-column label="定位" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="focusAdjustLog(row)">定位</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { fetchTeacherPage } from '@/api/modules/base';
import { fetchScheduleConfig } from '@/api/modules/system';
import { getErrorMessage } from '@/utils/http';
import {
  adjustCoursePlan,
  fetchClassOptions,
  fetchSemesterList,
  fetchCoursePlanAdjustLogs,
  fetchCoursePlanByClassNo,
  fetchCoursePlanByTeacherNo
} from '@/api/modules/course';
import {
  buildPeriodLabels,
  buildWeekLabels,
  groupPeriods,
  resolvePlanPosition,
  toClassTime
} from './scheduleGrid.js';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const loading = ref(false);
const planLoadError = ref('');
const viewMode = ref('class');
const selectedSemester = ref('');
const selectedClassNo = ref('');
const selectedTeacherNo = ref('');
const semesterOptions = ref([]);
const classOptions = ref([]);
const teacherOptions = ref([]);
const scheduleConfig = ref(null);
const planList = ref([]);
const adjustLogs = ref([]);
const filteredAdjustLogs = ref([]);
const dragPlanId = ref(null);
const adjusting = ref(false);
const lastAdjustedPlanId = ref(null);
const lastAdjustedClassTime = ref('');
const focusedPlanId = ref(null);
const focusedCellKey = ref('');
const adjustLogKeyword = ref('');
const activePeriodGroup = ref('ALL');

const viewModeOptions = [
  { label: '班级', value: 'class' },
  { label: '教师', value: 'teacher' }
];

// ========== 课程颜色 ==========
const COURSE_PALETTE = [
  { bg: '#eff6ff', accent: '#3b82f6', text: '#1e40af' },  // 蓝
  { bg: '#ecfdf5', accent: '#10b981', text: '#065f46' },  // 绿
  { bg: '#fef3c7', accent: '#f59e0b', text: '#92400e' },  // 琥珀
  { bg: '#fce7f3', accent: '#ec4899', text: '#9d174d' },  // 粉
  { bg: '#f3e8ff', accent: '#8b5cf6', text: '#5b21b6' },  // 紫
  { bg: '#e0f2fe', accent: '#06b6d4', text: '#155e75' },  // 青
  { bg: '#fff7ed', accent: '#f97316', text: '#9a3412' },  // 橙
  { bg: '#f0fdf4', accent: '#22c55e', text: '#166534' },  // 翠绿
  { bg: '#fdf2f8', accent: '#d946ef', text: '#86198f' },  // 品红
  { bg: '#ecfeff', accent: '#06b6d4', text: '#164e63' },  // 天蓝
  { bg: '#fefce8', accent: '#eab308', text: '#854d0e' },  // 黄
  { bg: '#f5f3ff', accent: '#7c3aed', text: '#4c1d95' },  // 深紫
];

function getCardStyle(item) {
  const colors = getCourseColorVars(item);
  return {
    '--course-accent': colors.accent,
    '--course-text': colors.text,
    background: colors.bg
  };
}

function getCourseColor(item) {
  const name = item.courseName || item.courseNo || '';
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  const color = COURSE_PALETTE[Math.abs(hash) % COURSE_PALETTE.length];
  return color.accent;
}

function getCourseColorVars(item) {
  const name = item.courseName || item.courseNo || '';
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return COURSE_PALETTE[Math.abs(hash) % COURSE_PALETTE.length];
}

// ========== 计算属性 ==========
const canDragAdjust = computed(
  () => viewMode.value === 'class' && authStore.hasPermission('btn:timetable:drag-adjust')
);
const teacherCount = computed(() => new Set(planList.value.map((item) => item.teacherNo).filter(Boolean)).size);
const classroomCount = computed(() => new Set(planList.value.map((item) => item.classroomNo).filter(Boolean)).size);
const currentTarget = computed(() => (viewMode.value === 'class' ? selectedClassNo.value : selectedTeacherNo.value));

const planStatus = computed(() => {
  if (planLoadError.value) {
    return {
      type: 'warning',
      title: '课表加载失败',
      description: planLoadError.value
    };
  }
  if (!loading.value && currentTarget.value && !planList.value.length) {
    return {
      type: 'info',
      title: '暂无课表数据',
      description: viewMode.value === 'class' ? '当前班级还没有生成课表。' : '当前教师还没有生成课表。'
    };
  }
  return null;
});

const weekLabels = computed(() => buildWeekLabels(scheduleConfig.value, planList.value));
const periodLabels = computed(() => buildPeriodLabels(scheduleConfig.value, planList.value));
const periodGroups = computed(() => groupPeriods(periodLabels.value));

function isPeriodDimmed(period) {
  if (activePeriodGroup.value === 'ALL') return false;
  return period.groupLabel !== activePeriodGroup.value;
}
// 动态网格列数
const gridStyle = computed(() => {
  const cols = weekLabels.value.length;
  return `grid-template-columns: 88px repeat(${cols}, minmax(0, 1fr));`;
});

function getPlanCell(dayIndex, periodIndex) {
  return planList.value.filter((item) => {
    const parsed = resolvePlanPosition(item);
    return parsed && parsed.dayIndex === dayIndex && parsed.periodIndex === periodIndex;
  });
}

function getTimeGroupClass(period) {
  if (period.group === 'MORNING') return 'time-morning';
  if (period.group === 'AFTERNOON') return 'time-afternoon';
  if (period.group === 'EVENING') return 'time-evening';
  return '';
}

function isRecentAdjustedPlan(item) {
  return item.id === lastAdjustedPlanId.value;
}

function isRecentDropCell(dayIndex, periodIndex) {
  if (!lastAdjustedClassTime.value) {
    return false;
  }
  const parsed = resolvePlanPosition({ classTime: lastAdjustedClassTime.value });
  return parsed && parsed.dayIndex === dayIndex && parsed.periodIndex === periodIndex;
}

// ========== 拖拽调课 ==========
function handleDragStart(item) {
  if (!canDragAdjust.value) return;
  dragPlanId.value = item.id;
}

function handleDragEnd() {
  dragPlanId.value = null;
}

function handleDragOver(event) {
  if (!canDragAdjust.value) return;
  event.preventDefault();
}

async function handleDrop(dayIndex, periodIndex) {
  if (!canDragAdjust.value || !dragPlanId.value || adjusting.value) return;
  const targetClassTime = toClassTime(dayIndex, periodIndex);
  const targetPlan = planList.value.find((item) => item.id === dragPlanId.value);
  if (!targetPlan || targetPlan.classTime === targetClassTime) {
    dragPlanId.value = null;
    return;
  }
  adjusting.value = true;
  try {
    await adjustCoursePlan({
      id: targetPlan.id,
      standardResultId: targetPlan.standardResultId,
      classTime: targetClassTime,
      classroomNo: targetPlan.classroomNo
    });
    lastAdjustedPlanId.value = targetPlan.id;
    lastAdjustedClassTime.value = targetClassTime;
    focusedPlanId.value = targetPlan.id;
    focusedCellKey.value = `${dayIndex}-${periodIndex}`;
    ElMessage.success('调课成功');
    await loadCoursePlan();
  } finally {
    adjusting.value = false;
    dragPlanId.value = null;
  }
}

// ========== 数据加载 ==========
async function loadClassOptions() {
  const response = await fetchClassOptions();
  classOptions.value = response.data || [];
}

async function loadSemesterOptions() {
  const response = await fetchSemesterList();
  semesterOptions.value = [...(response.data || [])].sort().reverse();
}

async function loadTeacherOptions() {
  const response = await fetchTeacherPage(1, 200);
  teacherOptions.value = response.data?.records || [];
}

async function loadScheduleConfig() {
  const response = await fetchScheduleConfig();
  scheduleConfig.value = response.data || null;
}

async function loadCoursePlan() {
  if (!currentTarget.value) {
    planList.value = [];
    adjustLogs.value = [];
    filteredAdjustLogs.value = [];
    planLoadError.value = '';
    return;
  }
  loading.value = true;
  planLoadError.value = '';
  try {
    const response = viewMode.value === 'class'
      ? await fetchCoursePlanByClassNo(selectedClassNo.value, { semester: selectedSemester.value, meta: { silentError: true } })
      : await fetchCoursePlanByTeacherNo(selectedTeacherNo.value, { semester: selectedSemester.value, meta: { silentError: true } });
    planList.value = response.data || [];
    await loadAdjustLogs();
  } catch (error) {
    planList.value = [];
    adjustLogs.value = [];
    filteredAdjustLogs.value = [];
    const message = getErrorMessage(error, '课表加载失败，请稍后重试');
    if (!message.includes('没有课表')) {
      planLoadError.value = message;
    }
  } finally {
    loading.value = false;
  }
}

async function loadAdjustLogs() {
  const response = await fetchCoursePlanAdjustLogs({
    semester: selectedSemester.value || undefined,
    classNo: viewMode.value === 'class' ? selectedClassNo.value : undefined,
    teacherNo: viewMode.value === 'teacher' ? selectedTeacherNo.value : undefined,
    limit: 10
  });
  adjustLogs.value = response.data || [];
  applyAdjustLogFilter();
  syncRecentAdjustState();
}

// ========== 交互 ==========
async function handleTargetChange() {
  syncRouteQuery();
  await loadCoursePlan();
}

async function handleViewModeChange() {
  planList.value = [];
  dragPlanId.value = null;
  focusedPlanId.value = null;
  focusedCellKey.value = '';
  activePeriodGroup.value = 'ALL';
  if (viewMode.value === 'class') {
    selectedTeacherNo.value = '';
  } else {
    selectedClassNo.value = '';
  }
  syncRouteQuery();
  if (currentTarget.value) {
    await loadCoursePlan();
    return;
  }
  await loadAdjustLogs();
}

async function handleSemesterChange() {
  syncRouteQuery();
  if (currentTarget.value) await loadAdjustLogs();
  else await loadAdjustLogs();
}

function handleSemesterClear() {
  selectedSemester.value = '';
  syncRouteQuery();
  loadAdjustLogs();
}

function clearPlan() {
  selectedClassNo.value = '';
  selectedTeacherNo.value = '';
  planList.value = [];
  adjustLogs.value = [];
  filteredAdjustLogs.value = [];
  planLoadError.value = '';
  dragPlanId.value = null;
  lastAdjustedPlanId.value = null;
  lastAdjustedClassTime.value = '';
  focusedPlanId.value = null;
  focusedCellKey.value = '';
  activePeriodGroup.value = 'ALL';
  syncRouteQuery();
}

function handleAdjustLogFilter() {
  applyAdjustLogFilter();
}

function applyAdjustLogFilter() {
  const keyword = adjustLogKeyword.value.trim().toLowerCase();
  if (!keyword) {
    filteredAdjustLogs.value = [...adjustLogs.value];
    return;
  }
  filteredAdjustLogs.value = adjustLogs.value.filter((item) => {
    const target = [item.operatorName, item.classNo, item.teacherNo, item.remark]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
    return target.includes(keyword);
  });
}

function syncRecentAdjustState() {
  const latest = adjustLogs.value[0];
  if (!latest) return;
  lastAdjustedPlanId.value = latest.coursePlanId || lastAdjustedPlanId.value;
  lastAdjustedClassTime.value = latest.afterClassTime || lastAdjustedClassTime.value;
}

function focusAdjustLog(row) {
  const parsed = resolvePlanPosition({ classTime: row.afterClassTime });
  focusedPlanId.value = row.coursePlanId || null;
  focusedCellKey.value = parsed ? `${parsed.dayIndex}-${parsed.periodIndex}` : '';
  lastAdjustedPlanId.value = row.coursePlanId || lastAdjustedPlanId.value;
  lastAdjustedClassTime.value = row.afterClassTime || lastAdjustedClassTime.value;
  ElMessage.success('已定位');
}

function syncRouteQuery() {
  const query = {};
  if (viewMode.value) query.view = viewMode.value;
  if (selectedSemester.value) query.semester = selectedSemester.value;
  if (viewMode.value === 'class' && selectedClassNo.value) query.classNo = selectedClassNo.value;
  if (viewMode.value === 'teacher' && selectedTeacherNo.value) query.teacherNo = selectedTeacherNo.value;
  router.replace({ path: '/schedule', query });
}

async function applyRouteQuery() {
  const nextView = route.query.view === 'teacher' ? 'teacher' : 'class';
  viewMode.value = nextView;
  selectedSemester.value = typeof route.query.semester === 'string' ? route.query.semester : '';
  selectedClassNo.value = nextView === 'class' && typeof route.query.classNo === 'string' ? route.query.classNo : '';
  selectedTeacherNo.value = nextView === 'teacher' && typeof route.query.teacherNo === 'string' ? route.query.teacherNo : '';
  if (currentTarget.value) await loadCoursePlan();
  else if (selectedSemester.value) await loadAdjustLogs();
}

onMounted(async () => {
  await Promise.all([loadClassOptions(), loadTeacherOptions(), loadSemesterOptions(), loadScheduleConfig()]);
  await applyRouteQuery();
});
</script>

<style scoped>
.schedule-shell {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ========== 顶部三行卡片 ========== */
.header-card {
  padding: 24px 28px 20px;
  border: 1px solid #dae7f4;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgb(255 255 255 / 88%), transparent 36%),
    linear-gradient(135deg, #f8fdfb 0%, #f6fbff 52%, #eef5ff 100%);
  box-shadow: 0 18px 42px rgb(17 34 68 / 7%);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

/* 第一行：标题 */
.title-row {
  padding-bottom: 2px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #17263d;
  margin: 0;
}

/* 第二行：视角按钮 + 统计 */
.stats-row {
  flex-wrap: wrap;
}

.view-toggle {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #c5d8e8;
  flex-shrink: 0;
}

.toggle-btn {
  padding: 6px 18px;
  font-size: 13px;
  font-weight: 600;
  border: none;
  background: rgb(255 255 255 / 60%);
  color: #53667f;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}

.toggle-btn:hover {
  color: #17263d;
  background: rgb(255 255 255 / 80%);
}

.toggle-btn--active {
  background: linear-gradient(135deg, #165dff 0%, #0ea5a4 100%);
  color: #fff;
}

.stat-badges {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.stat-badge {
  font-size: 13px;
  color: #53667f;
  font-weight: 500;
}

.stat-badge strong {
  color: #17263d;
  font-weight: 700;
}

.stat-badge--accent {
  color: #6c7d93;
}

.stat-badge--accent strong {
  color: #17263d;
}

/* 第三行：筛选 + 查询 */
.filter-row {
  flex-wrap: wrap;
}

.filter-select {
  min-width: 160px;
}

:deep(.query-btn.el-button--primary) {
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #165dff 0%, #0ea5a4 100%);
  box-shadow: 0 12px 24px rgb(22 93 255 / 22%);
  font-weight: 600;
}

/* ========== 课表容器 ========== */
.timetable-wrapper {
  border: 1px solid #e5edf6;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 4%);
  padding: 20px;
  background: #fff;
}

.status-alert {
  margin-bottom: 16px;
  border-radius: 18px;
}

/* ========== 时段分组标签 ========== */
.period-groups {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.period-group-chip {
  font-size: 12px;
  font-weight: 600;
  color: #35547b;
  padding: 4px 14px;
  border-radius: 20px;
  background: linear-gradient(180deg, #f5f9ff 0%, #edf5ff 100%);
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.period-group-chip:hover {
  background: linear-gradient(180deg, #e8f0ff 0%, #dde8f8 100%);
}

.period-group-chip--active {
  background: linear-gradient(135deg, #165dff 0%, #0ea5a4 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgb(22 93 255 / 18%);
}

/* 时段过滤高亮/淡化 */
.period-dimmed {
  opacity: 0.3;
  filter: grayscale(40%);
  transition: opacity 0.3s, filter 0.3s;
}

.period-dimmed:hover {
  opacity: 0.55;
}

.card-dimmed {
  opacity: 0.25;
  filter: grayscale(50%);
  pointer-events: none;
  transition: opacity 0.3s, filter 0.3s;
}

/* ========== 课表网格 ========== */
.timetable-grid {
  display: grid;
  gap: 8px;
}

/* 表头 */
.grid-head {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 10px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 700;
  color: #35547b;
  background: linear-gradient(180deg, #f5f9ff 0%, #edf5ff 100%);
}

.time-head {
  background: linear-gradient(180deg, #fff8ea 0%, #fff1d2 100%);
  color: #7a5a18;
}

/* 时间列 */
.time-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 108px;
  padding: 12px 8px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #f9fbfe 100%);
  border: 1px solid #e4edf7;
  gap: 4px;
  color: #51657e;
}

.time-badge {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 800;
  color: #fff;
}

.time-morning .time-badge {
  background: linear-gradient(135deg, #f97316, #fb923c);
}

.time-afternoon .time-badge {
  background: linear-gradient(135deg, #6366f1, #818cf8);
}

.time-evening .time-badge {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
}

.time-range {
  font-size: 11px;
  font-weight: 600;
  color: #17263d;
  white-space: nowrap;
  letter-spacing: 0.01em;
}

/* 课表格子 */
.grid-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 108px;
  padding: 8px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
  border: 1px solid #e4edf7;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.drag-target:hover {
  border-color: #9fc2ff;
  background: linear-gradient(180deg, #fff 0%, #f5faff 100%);
}

.recent-drop-cell {
  border-color: #79a9ff;
  background: linear-gradient(180deg, #fff 0%, #eef5ff 100%);
  box-shadow: inset 0 0 0 1px rgb(121 169 255 / 28%);
}

.focused-cell {
  border-color: #0ea5a4;
  background: linear-gradient(180deg, #fff 0%, #eefcf9 100%);
  box-shadow: inset 0 0 0 1px rgb(14 165 164 / 24%);
}

/* 空闲格子 */
.empty-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 80px;
  gap: 4px;
  border: 1px dashed #dbe6f2;
  border-radius: 14px;
  font-size: 12px;
  color: #95a3b8;
  transition: all 0.2s;
}

.drag-target:hover .empty-slot {
  border-color: #9fc2ff;
  color: #79a9ff;
  background: rgb(239 245 255 / 50%);
}

.empty-icon {
  width: 16px;
  height: 16px;
}

/* ========== 课程卡片 ========== */
.course-card {
  position: relative;
  padding: 10px 10px 10px 14px;
  border-radius: 16px;
  border: 1px solid transparent;
  cursor: grab;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  animation: cardAppear 0.3s ease backwards;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.course-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgb(0 0 0 / 8%);
}

.course-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--course-accent, #94a3b8);
  border-radius: 10px 0 0 10px;
}

.course-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.course-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--course-text, #17263d);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 11px;
  color: #71839a;
}

.course-teacher,
.course-room {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-icon {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
  opacity: 0.6;
}

.dragging-card {
  opacity: 0.5;
  cursor: grabbing;
}

.recent-adjust-card {
  border-color: #79a9ff;
  box-shadow: 0 12px 22px rgb(22 93 255 / 10%);
}

.focused-card {
  border-color: #0ea5a4;
  box-shadow: 0 12px 24px rgb(14 165 164 / 14%);
}

/* ========== 调课记录 ========== */
.log-card {
  border: 1px solid #e5edf6;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 4%);
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #17263d;
}

.header-tags {
  display: flex;
  gap: 10px;
  align-items: center;
}

.log-search {
  width: 220px;
}

.adjust-route {
  font-size: 12px;
  font-weight: 600;
  color: #165dff;
  background: #edf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

/* ========== 响应式 ========== */
@media (max-width: 960px) {
  .schedule-shell {
    padding: 10px;
  }

  .header-card {
    gap: 8px;
    padding: 16px 18px;
    border-radius: 20px;
  }

  .page-title {
    font-size: 22px;
  }

  .stats-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    min-width: 100%;
  }

  .timetable-grid {
    grid-template-columns: 1fr !important;
  }

  .time-cell {
    flex-direction: row;
    gap: 12px;
    min-height: auto;
  }
}
</style>
