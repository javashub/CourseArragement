<template>
  <section class="schedule-shell">
    <div class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">Timetable Observer</div>
        <h1 class="hero-title">课表管理</h1>
        <p class="hero-description">
          当前先接通按班级查看课表结果，并把课表按时间片整理成易读表格。后续教师课表、学生课表、拖拽调课会继续挂到这里。
        </p>
      </div>
      <div class="hero-actions">
        <el-segmented v-model="viewMode" :options="viewModeOptions" class="view-switch" />
        <el-select
          v-if="viewMode === 'class'"
          v-model="selectedClassNo"
          filterable
          clearable
          placeholder="选择班级，例如 2401"
          class="class-select"
          @change="loadCoursePlan"
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
          @change="loadCoursePlan"
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
    </div>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <template #header>
        <div class="card-head">
          <div>
            <div class="card-title">{{ viewMode === 'class' ? '班级课表' : '教师课表' }}</div>
            <div class="card-caption">当前先按旧排课算法的 01-25 时间片规则，映射成周一到周五、每天五节课的课表视图。</div>
          </div>
          <el-tag type="info" effect="plain">{{ viewMode === 'class' ? '班级视角' : '教师视角' }}</el-tag>
        </div>
      </template>

      <el-empty v-if="!currentTarget && !loading" :description="viewMode === 'class' ? '请先选择班级，再查看课表' : '请先选择教师，再查看课表'" />
      <el-empty v-else-if="currentTarget && !planList.length && !loading" :description="viewMode === 'class' ? '当前班级还没有生成课表' : '当前教师还没有生成课表'" />

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
          <div v-for="day in weekLabels" :key="`${day.key}-${period.key}`" class="grid-cell">
            <template v-if="getPlanCell(day.index, period.index).length">
              <div
                v-for="item in getPlanCell(day.index, period.index)"
                :key="item.id"
                class="course-card"
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
            <div v-else class="empty-slot">空闲</div>
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
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchTeacherPage } from '@/api/modules/base';
import { fetchClassInfoPage, fetchCoursePlanByClassNo, fetchCoursePlanByTeacherNo } from '@/api/modules/course';

const loading = ref(false);
const viewMode = ref('class');
const selectedClassNo = ref('');
const selectedTeacherNo = ref('');
const classOptions = ref([]);
const teacherOptions = ref([]);
const planList = ref([]);

const viewModeOptions = [
  { label: '班级视角', value: 'class' },
  { label: '教师视角', value: 'teacher' }
];

const teacherCount = computed(() => new Set(planList.value.map((item) => item.teacherNo).filter(Boolean)).size);
const classroomCount = computed(() => new Set(planList.value.map((item) => item.classroomNo).filter(Boolean)).size);
const currentTarget = computed(() => (viewMode.value === 'class' ? selectedClassNo.value : selectedTeacherNo.value));

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

async function loadClassOptions() {
  const response = await fetchClassInfoPage(1, 200);
  classOptions.value = response.data?.records || [];
}

async function loadTeacherOptions() {
  const response = await fetchTeacherPage(1, 200);
  teacherOptions.value = response.data?.records || [];
}

async function loadCoursePlan() {
  if (!currentTarget.value) {
    planList.value = [];
    return;
  }
  loading.value = true;
  try {
    const response = viewMode.value === 'class'
      ? await fetchCoursePlanByClassNo(selectedClassNo.value)
      : await fetchCoursePlanByTeacherNo(selectedTeacherNo.value);
    planList.value = response.data || [];
  } catch (error) {
    planList.value = [];
    ElMessage.warning(viewMode.value === 'class' ? '当前班级还没有生成课表' : '当前教师还没有生成课表');
  } finally {
    loading.value = false;
  }
}

function clearPlan() {
  selectedClassNo.value = '';
  selectedTeacherNo.value = '';
  planList.value = [];
}

onMounted(async () => {
  await Promise.all([loadClassOptions(), loadTeacherOptions()]);
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

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
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

.course-card {
  padding: 12px 14px;
  border: 1px solid #dfeaf5;
  border-radius: 16px;
  background: linear-gradient(180deg, #fff 0%, #f7fbff 100%);
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

  .timetable-grid {
    grid-template-columns: 1fr;
  }
}
</style>
