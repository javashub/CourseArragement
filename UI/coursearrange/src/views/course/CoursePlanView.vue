<template>
  <section class="plan-shell">
    <div class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">Task Orchestrator</div>
        <h1 class="hero-title">排课任务</h1>
        <p class="hero-description">
          先把开课任务、学期切换和一键排课打通。当前页直接接现有排课接口，后续再把任务约束和执行日志并入新模型。
        </p>
      </div>
      <div class="hero-actions">
        <el-select
          v-model="selectedSemester"
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入学期，例如 2025-2026-1"
          class="semester-select"
          @change="loadClassTasks(true)"
        >
          <el-option v-for="item in semesters" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button class="ghost-action" @click="loadClassTasks(true)">刷新任务</el-button>
        <el-button class="primary-action" type="primary" :loading="arranging" @click="handleArrange">执行排课</el-button>
      </div>
    </div>

    <el-card shadow="never" class="plan-card">
      <div class="toolbar-row">
        <div class="summary-chip">
          <span class="summary-label">当前学期</span>
          <strong>{{ selectedSemester || '--' }}</strong>
        </div>
        <div class="summary-chip">
          <span class="summary-label">任务数量</span>
          <strong>{{ taskState.total }}</strong>
        </div>
        <div class="toolbar-actions">
          <el-button class="ghost-action" @click="prefillTaskByClass">按班级回填示例</el-button>
          <el-button class="primary-action" type="primary" @click="openTaskDialog()">新增任务</el-button>
        </div>
      </div>

      <el-table :data="taskState.records" stripe v-loading="taskState.loading">
        <el-table-column prop="classNo" label="班级编号" min-width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="realname" label="教师姓名" min-width="110" />
        <el-table-column prop="courseAttr" label="课程属性" min-width="120" />
        <el-table-column prop="studentNum" label="人数" width="80" />
        <el-table-column prop="weeksNumber" label="周学时" width="90" />
        <el-table-column prop="weeksSum" label="周数" width="80" />
        <el-table-column prop="classTime" label="固定时段" min-width="130">
          <template #default="{ row }">
            {{ row.classTime || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="固定排课" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isFix === '1' ? 'warning' : 'info'" effect="plain">
              {{ row.isFix === '1' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeTask(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="taskState.pageNum"
          :page-size="taskState.pageSize"
          :total="taskState.total"
          @current-change="handleTaskPageChange"
        />
      </div>
    </el-card>

    <el-card shadow="never" class="tips-card">
      <template #header>
        <div class="card-title">联调说明</div>
      </template>
      <div class="tips-grid">
        <div class="tip-item">
          <strong>新增任务</strong>
          <span>先录入学期、班级、课程、教师和周学时，形成排课输入数据。</span>
        </div>
        <div class="tip-item">
          <strong>执行排课</strong>
          <span>当前直接调用旧排课算法接口，执行完成后结果写入 `tb_course_plan`。</span>
        </div>
        <div class="tip-item">
          <strong>查看课表</strong>
          <span>切到“课表管理”页，按班级选择后即可看到排课结果。</span>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="taskDialogVisible" title="新增排课任务" width="760px">
      <el-form :model="taskForm" label-position="top">
        <div class="form-grid">
          <el-form-item label="学期">
            <el-input v-model="taskForm.semester" placeholder="例如 2025-2026-1" />
          </el-form-item>
          <el-form-item label="年级编号">
            <el-input v-model="taskForm.gradeNo" placeholder="例如 2024、02" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="班级编号">
            <el-select v-model="taskForm.classNo" clearable filterable placeholder="例如 2401">
              <el-option v-for="item in classOptions" :key="item.classNo" :label="`${item.classNo} ${item.className || ''}`" :value="item.classNo" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程名称">
            <el-input v-model="taskForm.courseName" placeholder="例如 高等数学" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="课程编号">
            <el-input v-model="taskForm.courseNo" placeholder="例如 10001" />
          </el-form-item>
          <el-form-item label="教师姓名">
            <el-input v-model="taskForm.realname" placeholder="例如 张老师" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="教师编号">
            <el-input v-model="taskForm.teacherNo" placeholder="例如 T2026001" />
          </el-form-item>
          <el-form-item label="课程属性">
            <el-input v-model="taskForm.courseAttr" placeholder="例如 必修、实验课" />
          </el-form-item>
        </div>
        <div class="form-grid compact-grid">
          <el-form-item label="学生人数">
            <el-input-number v-model="taskForm.studentNum" :min="1" :max="300" controls-position="right" />
          </el-form-item>
          <el-form-item label="周学时">
            <el-input-number v-model="taskForm.weeksNumber" :min="1" :max="20" controls-position="right" />
          </el-form-item>
          <el-form-item label="周数">
            <el-input-number v-model="taskForm.weeksSum" :min="1" :max="30" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="是否固定上课时间">
            <el-radio-group v-model="taskForm.isFix">
              <el-radio value="0">否</el-radio>
              <el-radio value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="固定时间">
            <el-input v-model="taskForm.classTime" placeholder="例如 0102、0304，未固定可留空" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button class="primary-action" type="primary" :loading="taskSubmitting" @click="submitTask">保存任务</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  arrangeClassTask,
  createClassTask,
  deleteClassTask,
  fetchClassInfoPage,
  fetchClassTaskPage,
  fetchSemesterList
} from '@/api/modules/course';

const semesters = ref([]);
const selectedSemester = ref('');
const arranging = ref(false);
const taskDialogVisible = ref(false);
const taskSubmitting = ref(false);
const classOptions = ref([]);

const taskState = reactive({
  loading: false,
  records: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
});

const taskForm = ref(createTaskForm());

function createTaskForm() {
  return {
    semester: selectedSemester.value || '',
    gradeNo: '',
    classNo: '',
    courseNo: '',
    courseName: '',
    teacherNo: '',
    realname: '',
    courseAttr: '',
    studentNum: 40,
    weeksNumber: 4,
    weeksSum: 16,
    isFix: '0',
    classTime: ''
  };
}

async function loadSemesters() {
  const response = await fetchSemesterList();
  semesters.value = [...(response.data || [])].sort().reverse();
  if (!selectedSemester.value) {
    selectedSemester.value = semesters.value[0] || '2025-2026-1';
  }
}

async function loadClassOptions() {
  const response = await fetchClassInfoPage(1, 200);
  classOptions.value = response.data?.records || [];
}

async function loadClassTasks(resetPage = false) {
  if (!selectedSemester.value) {
    return;
  }
  if (resetPage) {
    taskState.pageNum = 1;
  }
  taskState.loading = true;
  try {
    const response = await fetchClassTaskPage(taskState.pageNum, selectedSemester.value, taskState.pageSize);
    const pageData = response.data || {};
    taskState.records = pageData.records || [];
    taskState.total = pageData.total || 0;
  } catch (error) {
    taskState.records = [];
    taskState.total = 0;
  } finally {
    taskState.loading = false;
  }
}

function handleTaskPageChange(page) {
  taskState.pageNum = page;
  loadClassTasks();
}

function openTaskDialog() {
  taskForm.value = createTaskForm();
  taskDialogVisible.value = true;
}

function prefillTaskByClass() {
  const firstClass = classOptions.value[0];
  taskForm.value = {
    ...createTaskForm(),
    gradeNo: firstClass?.remark || '',
    classNo: firstClass?.classNo || '',
    studentNum: firstClass?.num || 40
  };
  taskDialogVisible.value = true;
}

async function submitTask() {
  taskSubmitting.value = true;
  try {
    await createClassTask(taskForm.value);
    ElMessage.success('排课任务创建成功');
    taskDialogVisible.value = false;
    if (!semesters.value.includes(taskForm.value.semester)) {
      semesters.value.unshift(taskForm.value.semester);
    }
    selectedSemester.value = taskForm.value.semester;
    await loadClassTasks(true);
  } finally {
    taskSubmitting.value = false;
  }
}

async function removeTask(row) {
  await ElMessageBox.confirm(`确认删除班级“${row.classNo}”的任务吗？`, '删除确认', { type: 'warning' });
  await deleteClassTask(row.id);
  ElMessage.success('排课任务删除成功');
  await loadClassTasks();
}

async function handleArrange() {
  if (!selectedSemester.value) {
    ElMessage.warning('请先选择学期');
    return;
  }
  arranging.value = true;
  try {
    const response = await arrangeClassTask(selectedSemester.value);
    ElMessage.success(response.message || '排课执行完成');
  } finally {
    arranging.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadSemesters(), loadClassOptions()]);
  await loadClassTasks(true);
});
</script>

<style scoped>
.plan-shell {
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
    linear-gradient(135deg, #fdfcf7 0%, #f7fbff 50%, #eef7ff 100%);
  box-shadow: 0 18px 42px rgb(17 34 68 / 7%);
}

.eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  color: #7b6c45;
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

.hero-actions,
.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.semester-select {
  min-width: 220px;
}

.plan-card,
.tips-card {
  border: 1px solid #e5edf6;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 4%);
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.summary-chip {
  min-width: 160px;
  padding: 14px 16px;
  border: 1px solid #e5edf6;
  border-radius: 18px;
  background: rgb(255 255 255 / 78%);
}

.summary-label {
  display: block;
  font-size: 12px;
  color: #7c8ca2;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #17263d;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.tip-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border: 1px solid #e4edf7;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #f8fbff 100%);
  color: #5b6e86;
  font-size: 13px;
  line-height: 1.6;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.compact-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
  .toolbar-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .tips-grid,
  .form-grid,
  .compact-grid {
    grid-template-columns: 1fr;
  }
}
</style>
