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
        <el-select
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
        <el-button class="ghost-action" @click="loadClassOptions">刷新班级</el-button>
        <el-button class="primary-action" type="primary" @click="loadCoursePlan">查询课表</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">当前班级</div>
        <div class="summary-value">{{ selectedClassNo || '--' }}</div>
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
            <div class="card-title">班级课表</div>
            <div class="card-caption">旧接口返回的是编码型时间片，这里先按时间编号分行展示，便于验证排课结果是否写入成功。</div>
          </div>
          <el-tag type="info" effect="plain">拖拽调课下一阶段接入</el-tag>
        </div>
      </template>

      <el-empty v-if="!selectedClassNo && !loading" description="请先选择班级，再查看课表" />
      <el-empty v-else-if="selectedClassNo && !planList.length && !loading" description="当前班级还没有生成课表" />

      <el-table v-else :data="planRows" stripe>
        <el-table-column prop="timeLabel" label="时间片" min-width="120" />
        <el-table-column label="课程安排" min-width="520">
          <template #default="{ row }">
            <div class="course-stack">
              <div v-for="item in row.items" :key="item.id" class="course-card">
                <div class="course-main">
                  <strong>{{ item.courseName || item.courseNo }}</strong>
                  <span>{{ item.realname || item.teacherNo }}</span>
                </div>
                <div class="course-meta">
                  <span>教室 {{ item.classroomNo || '--' }}</span>
                  <span>学期 {{ item.semester || '--' }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
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
import { fetchClassInfoPage, fetchCoursePlanByClassNo } from '@/api/modules/course';

const loading = ref(false);
const selectedClassNo = ref('');
const classOptions = ref([]);
const planList = ref([]);

const teacherCount = computed(() => new Set(planList.value.map((item) => item.teacherNo).filter(Boolean)).size);
const classroomCount = computed(() => new Set(planList.value.map((item) => item.classroomNo).filter(Boolean)).size);

const planRows = computed(() => {
  const groups = new Map();
  for (const item of planList.value) {
    const key = item.classTime || '未标记时间';
    if (!groups.has(key)) {
      groups.set(key, []);
    }
    groups.get(key).push(item);
  }
  return [...groups.entries()]
    .sort((a, b) => String(a[0]).localeCompare(String(b[0])))
    .map(([timeLabel, items]) => ({
      timeLabel: formatTimeLabel(timeLabel),
      items
    }));
});

function formatTimeLabel(value) {
  if (!value) {
    return '未标记时间';
  }
  if (/^\d+$/.test(String(value))) {
    return `时间片 ${value}`;
  }
  return value;
}

async function loadClassOptions() {
  const response = await fetchClassInfoPage(1, 200);
  classOptions.value = response.data?.records || [];
}

async function loadCoursePlan() {
  if (!selectedClassNo.value) {
    planList.value = [];
    return;
  }
  loading.value = true;
  try {
    const response = await fetchCoursePlanByClassNo(selectedClassNo.value);
    planList.value = response.data || [];
  } catch (error) {
    planList.value = [];
    ElMessage.warning('当前班级还没有生成课表');
  } finally {
    loading.value = false;
  }
}

function clearPlan() {
  selectedClassNo.value = '';
  planList.value = [];
}

onMounted(async () => {
  await loadClassOptions();
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

.course-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
}
</style>
