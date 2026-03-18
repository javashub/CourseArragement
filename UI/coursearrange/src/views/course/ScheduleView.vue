<template>
  <section class="schedule-shell">
    <div class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">Timetable Observer</div>
        <h1 class="hero-title">课表管理</h1>
        <p class="hero-description">
          当前页面只展示标准课表结果，并把课表按时间片整理成易读表格。legacy tb_course_plan 不再作为这里的查询回退来源。
        </p>
      </div>
      <div class="hero-actions">
        <el-segmented v-model="viewMode" :options="viewModeOptions" class="view-switch" @change="handleViewModeChange" />
        <el-select
          v-model="selectedSemester"
          filterable
          clearable
          allow-create
          default-first-option
          placeholder="选择学期，例如 2025-2026-1"
          class="class-select"
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
          placeholder="选择班级，例如 2401"
          class="class-select"
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
          placeholder="选择教师，例如 T2026001"
          class="class-select"
          @change="handleTargetChange"
          @clear="clearPlan"
        >
          <el-option
            v-for="item in teacherOptions"
            :key="item.teacherNo"
            :label="`${item.teacherNo} ${item.realname || ''}`"
            :value="item.teacherNo"
          />
        </el-select>
        <el-button class="ghost-action" @click="loadClassOptions">刷新班级</el-button>
        <el-button class="primary-action" type="primary" @click="loadCoursePlan">查询课表</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">{{ viewMode === 'class' ? '当前班级' : '当前教师' }}</div>
        <div class="summary-value">{{ currentTarget || '--' }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">课表条数</div>
        <div class="summary-value">{{ planList.length }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">涉及教师</div>
        <div class="summary-value">{{ teacherCount }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">涉及教室</div>
        <div class="summary-value">{{ classroomCount }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card accent-card">
        <div class="summary-label">最近调课</div>
        <div class="summary-value summary-value--small">{{ latestAdjustSummary }}</div>
      </el-card>
    </div>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <template #header>
        <div class="card-head">
          <div>
            <div class="card-title">{{ viewMode === 'class' ? '班级课表' : '教师课表' }}</div>
            <div class="card-caption">当前按标准课表结果的 01-25 时间片规则，映射成周一到周五、每天五节课的课表视图。</div>
          </div>
          <div class="header-tags">
            <el-tag v-if="canDragAdjust" type="warning" effect="plain">管理员可拖拽调课</el-tag>
            <el-tag type="info" effect="plain">{{ viewMode === 'class' ? '班级视角' : '教师视角' }}</el-tag>
          </div>
        </div>
      </template>

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

      <div v-else class="timetable-grid">
        <div class="grid-head time-head">节次</div>
        <div v-for="day in weekLabels" :key="day.key" class="grid-head">
          {{ day.label }}
        </div>

        <template v-for="period in periodLabels" :key="period.key">
          <div class="time-cell">
            <strong>{{ period.label }}</strong>
            <span>{{ period.key }}</span>
          </div>
          <div
            v-for="day in weekLabels"
            :key="`${day.key}-${period.key}`"
            class="grid-cell"
            :class="{
              'drag-target': canDragAdjust,
              'recent-drop-cell': isRecentDropCell(day.index, period.index),
              'focused-cell': focusedCellKey === `${day.index}-${period.index}`
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
                  'focused-card': focusedPlanId === item.id
                }"
                :draggable="canDragAdjust"
                @dragstart="handleDragStart(item)"
                @dragend="handleDragEnd"
              >
                <div class="course-main">
                  <strong>{{ item.courseName || item.courseNo }}</strong>
                  <span>{{ item.realname || item.teacherNo }}</span>
                </div>
                <div class="course-meta">
                  <span>教室 {{ item.classroomNo || '--' }}</span>
                  <span>{{ item.classTime }}</span>
                </div>
              </div>
            </template>
            <div v-else class="empty-slot">{{ canDragAdjust ? '拖到这里调课' : '空闲' }}</div>
          </div>
        </template>
      </div>
    </el-card>

    <el-card shadow="never" class="raw-card" v-if="planList.length">
      <template #header>
        <div class="card-head">
          <div>
            <div class="card-title">原始排课结果</div>
            <div class="card-caption">用于核对旧排课算法写入的数据结构，后续新课表组件会基于这份数据进一步规整。</div>
          </div>
        </div>
      </template>

      <el-table :data="planList" size="small" stripe max-height="320">
        <el-table-column prop="classTime" label="时间编码" width="120" />
        <el-table-column prop="courseName" label="课程" min-width="150" />
        <el-table-column prop="realname" label="教师" min-width="120" />
        <el-table-column prop="classroomNo" label="教室" min-width="120" />
        <el-table-column prop="teacherNo" label="教师编号" min-width="120" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="raw-card" v-if="adjustLogs.length">
      <template #header>
        <div class="card-head">
          <div>
            <div class="card-title">最近调课记录</div>
            <div class="card-caption">用于追踪谁把哪门课从哪个时间片调到了哪个时间片，后续会继续扩展成完整审计页。</div>
          </div>
          <div class="header-tags">
            <el-tag type="info" effect="plain">学期 {{ selectedSemester || '全部' }}</el-tag>
            <el-input
              v-model="adjustLogKeyword"
              clearable
              class="log-search"
              placeholder="筛选操作人 / 班级 / 教师编号"
              @clear="handleAdjustLogFilter"
              @input="handleAdjustLogFilter"
            />
            <el-button class="ghost-action" size="small" @click="loadAdjustLogs">刷新调课记录</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredAdjustLogs" size="small" stripe max-height="280" @row-click="focusAdjustLog">
        <el-table-column prop="operatorName" label="操作人" min-width="120" />
        <el-table-column prop="classNo" label="班级" min-width="110" />
        <el-table-column prop="teacherNo" label="教师编号" min-width="120" />
        <el-table-column prop="beforeClassTime" label="调整前" width="100" />
        <el-table-column prop="afterClassTime" label="调整后" width="100" />
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column prop="createTime" label="时间" min-width="170" />
        <el-table-column label="定位" width="90" fixed="right">
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
import { getErrorMessage } from '@/utils/http';
import {
  adjustCoursePlan,
  fetchClassOptions,
  fetchSemesterList,
  fetchCoursePlanAdjustLogs,
  fetchCoursePlanByClassNo,
  fetchCoursePlanByTeacherNo
} from '@/api/modules/course';

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

const viewModeOptions = [
  { label: '班级视角', value: 'class' },
  { label: '教师视角', value: 'teacher' }
];

const canDragAdjust = computed(
  () => viewMode.value === 'class' && authStore.hasPermission('btn:timetable:drag-adjust')
);
const teacherCount = computed(() => new Set(planList.value.map((item) => item.teacherNo).filter(Boolean)).size);
const classroomCount = computed(() => new Set(planList.value.map((item) => item.classroomNo).filter(Boolean)).size);
const currentTarget = computed(() => (viewMode.value === 'class' ? selectedClassNo.value : selectedTeacherNo.value));
const latestAdjustSummary = computed(() => {
  if (!filteredAdjustLogs.value.length) {
    return '暂无记录';
  }
  const latest = filteredAdjustLogs.value[0];
  return `${latest.beforeClassTime || '--'} → ${latest.afterClassTime || '--'}`;
});

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

const weekLabels = [
  { label: '周一', key: 'mon', index: 1 },
  { label: '周二', key: 'tue', index: 2 },
  { label: '周三', key: 'wed', index: 3 },
  { label: '周四', key: 'thu', index: 4 },
  { label: '周五', key: 'fri', index: 5 }
];

const periodLabels = [
  { label: '第1节', key: '01', index: 1 },
  { label: '第2节', key: '02', index: 2 },
  { label: '第3节', key: '03', index: 3 },
  { label: '第4节', key: '04', index: 4 },
  { label: '第5节', key: '05', index: 5 }
];

function parseClassTime(classTime) {
  const value = Number(classTime);
  if (!value || value < 1) {
    return null;
  }
  const dayIndex = Math.ceil(value / 5);
  const periodIndex = ((value - 1) % 5) + 1;
  return {
    dayIndex,
    periodIndex
  };
}

function getPlanCell(dayIndex, periodIndex) {
  return planList.value.filter((item) => {
    const parsed = parseClassTime(item.classTime);
    return parsed && parsed.dayIndex === dayIndex && parsed.periodIndex === periodIndex;
  });
}

function isRecentAdjustedPlan(item) {
  return item.id === lastAdjustedPlanId.value;
}

function isRecentDropCell(dayIndex, periodIndex) {
  if (!lastAdjustedClassTime.value) {
    return false;
  }
  const parsed = parseClassTime(lastAdjustedClassTime.value);
  return parsed && parsed.dayIndex === dayIndex && parsed.periodIndex === periodIndex;
}

function toClassTime(dayIndex, periodIndex) {
  return String((dayIndex - 1) * 5 + periodIndex).padStart(2, '0');
}

function handleDragStart(item) {
  if (!canDragAdjust.value) {
    return;
  }
  dragPlanId.value = item.id;
}

function handleDragEnd() {
  dragPlanId.value = null;
}

function handleDragOver(event) {
  if (!canDragAdjust.value) {
    return;
  }
  event.preventDefault();
}

async function handleDrop(dayIndex, periodIndex) {
  if (!canDragAdjust.value || !dragPlanId.value || adjusting.value) {
    return;
  }
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

async function handleTargetChange() {
  syncRouteQuery();
  await loadCoursePlan();
}

async function handleViewModeChange() {
  planList.value = [];
  dragPlanId.value = null;
  focusedPlanId.value = null;
  focusedCellKey.value = '';
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
  if (currentTarget.value) {
    await loadAdjustLogs();
    return;
  }
  await loadAdjustLogs();
}

function handleSemesterClear() {
  selectedSemester.value = '';
  syncRouteQuery();
  if (currentTarget.value) {
    loadAdjustLogs();
    return;
  }
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
  if (!latest) {
    return;
  }
  lastAdjustedPlanId.value = latest.coursePlanId || lastAdjustedPlanId.value;
  lastAdjustedClassTime.value = latest.afterClassTime || lastAdjustedClassTime.value;
}

function focusAdjustLog(row) {
  const parsed = parseClassTime(row.afterClassTime);
  focusedPlanId.value = row.coursePlanId || null;
  focusedCellKey.value = parsed ? `${parsed.dayIndex}-${parsed.periodIndex}` : '';
  lastAdjustedPlanId.value = row.coursePlanId || lastAdjustedPlanId.value;
  lastAdjustedClassTime.value = row.afterClassTime || lastAdjustedClassTime.value;
  ElMessage.success(`已定位到 ${row.afterClassTime || '--'} 时间片`);
}

function syncRouteQuery() {
  const query = {};
  if (viewMode.value) {
    query.view = viewMode.value;
  }
  if (selectedSemester.value) {
    query.semester = selectedSemester.value;
  }
  if (viewMode.value === 'class' && selectedClassNo.value) {
    query.classNo = selectedClassNo.value;
  }
  if (viewMode.value === 'teacher' && selectedTeacherNo.value) {
    query.teacherNo = selectedTeacherNo.value;
  }
  router.replace({
    path: '/schedule',
    query
  });
}

async function applyRouteQuery() {
  const nextView = route.query.view === 'teacher' ? 'teacher' : 'class';
  viewMode.value = nextView;
  selectedSemester.value = typeof route.query.semester === 'string' ? route.query.semester : '';
  selectedClassNo.value = nextView === 'class' && typeof route.query.classNo === 'string' ? route.query.classNo : '';
  selectedTeacherNo.value = nextView === 'teacher' && typeof route.query.teacherNo === 'string' ? route.query.teacherNo : '';
  if (currentTarget.value) {
    await loadCoursePlan();
  } else if (selectedSemester.value) {
    await loadAdjustLogs();
  }
}

onMounted(async () => {
  await Promise.all([loadClassOptions(), loadTeacherOptions(), loadSemesterOptions()]);
  await applyRouteQuery();
});
</script>

<style scoped>
.schedule-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-panel {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  border: 1px solid #dae7f4;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgb(255 255 255 / 88%), transparent 36%),
    linear-gradient(135deg, #f8fdfb 0%, #f6fbff 52%, #eef5ff 100%);
  box-shadow: 0 18px 42px rgb(17 34 68 / 7%);
}

.eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  color: #3c7b67;
  text-transform: uppercase;
}

.hero-title {
  margin: 10px 0 8px;
  font-size: 34px;
  color: #17263d;
}

.hero-description {
  max-width: 760px;
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #53667f;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.view-switch {
  min-width: 188px;
}

.class-select {
  min-width: 220px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card,
.table-card,
.raw-card {
  border: 1px solid #e5edf6;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 4%);
}

.accent-card {
  border-color: #cfe3ff;
  background: linear-gradient(135deg, rgb(232 243 255 / 88%) 0%, rgb(239 251 248 / 88%) 100%);
}

.summary-label {
  font-size: 12px;
  color: #7c8ca2;
}

.summary-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 700;
  color: #17263d;
}

.summary-value--small {
  font-size: 20px;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.header-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.log-search {
  width: 240px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #17263d;
}

.card-caption {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: #6c7d93;
}

.status-alert {
  margin-bottom: 16px;
  border-radius: 18px;
}

.timetable-grid {
  display: grid;
  grid-template-columns: 110px repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.grid-head {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 54px;
  padding: 10px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f5f9ff 0%, #edf5ff 100%);
  font-size: 13px;
  font-weight: 700;
  color: #35547b;
}

.time-head {
  background: linear-gradient(180deg, #fff8ea 0%, #fff1d2 100%);
  color: #7a5a18;
}

.time-cell {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  min-height: 132px;
  padding: 14px 12px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #f9fbfe 100%);
  border: 1px solid #e4edf7;
  color: #51657e;
}

.time-cell strong {
  color: #17263d;
}

.grid-cell {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 132px;
  padding: 10px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
  border: 1px solid #e4edf7;
}

.drag-target {
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

.empty-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 100px;
  border: 1px dashed #dbe6f2;
  border-radius: 14px;
  font-size: 13px;
  color: #95a3b8;
}

.dragging-card {
  opacity: 0.64;
  cursor: grabbing;
}

.course-card {
  padding: 12px 14px;
  border: 1px solid #dfeaf5;
  border-radius: 16px;
  background: linear-gradient(180deg, #fff 0%, #f7fbff 100%);
  cursor: grab;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.recent-adjust-card {
  border-color: #79a9ff;
  box-shadow: 0 12px 22px rgb(22 93 255 / 10%);
}

.focused-card {
  border-color: #0ea5a4;
  box-shadow: 0 12px 24px rgb(14 165 164 / 14%);
  transform: translateY(-1px);
}

.course-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #17263d;
}

.course-meta {
  display: flex;
  gap: 14px;
  margin-top: 8px;
  font-size: 12px;
  color: #71839a;
}

:deep(.primary-action.el-button) {
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #165dff 0%, #0ea5a4 100%);
  box-shadow: 0 12px 24px rgb(22 93 255 / 22%);
}

:deep(.ghost-action.el-button) {
  border-radius: 999px;
  border-color: #d7e1ef;
  background: rgb(255 255 255 / 82%);
}

@media (max-width: 960px) {
  .hero-panel,
  .card-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .log-search {
    width: 100%;
  }

  .timetable-grid {
    grid-template-columns: 1fr;
  }
}
</style>
