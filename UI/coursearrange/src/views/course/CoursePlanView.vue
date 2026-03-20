<template>
  <section class="plan-shell">
    <div class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">Task Orchestrator</div>
        <h1 class="hero-title">排课任务</h1>
        <p class="hero-description">
          先把开课任务、学期切换和一键排课打通。当前页已经优先读取标准排课任务，排课执行仍兼容旧算法链路，后续会继续替换旧表实现。
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
          @change="handleSemesterChange"
        >
          <el-option v-for="item in semesters" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button class="ghost-action" @click="loadClassTasks(true)">刷新任务</el-button>
        <el-button class="primary-action" type="primary" :loading="arranging" @click="handleArrange">执行排课</el-button>
      </div>
    </div>

    <el-card shadow="never" class="plan-card config-card">
      <div class="config-headline">
        <div>
          <div class="card-title">当前生效排课规则</div>
          <div class="card-subtitle">
            自动排课现在会优先读取系统配置中的时间片模板，并排除“不可上课 / 固定休息”的时间片。
          </div>
        </div>
        <el-tag :type="runtimeConfig.configApplied ? 'success' : 'info'" effect="light" round>
          {{ runtimeConfig.configApplied ? '已应用系统配置' : '回退默认 25 格时间片' }}
        </el-tag>
      </div>
      <div class="config-strip">
        <div class="config-pill">
          <span class="config-label">规则名称</span>
          <strong>{{ runtimeConfig.ruleName || '未配置默认规则' }}</strong>
        </div>
        <div class="config-pill">
          <span class="config-label">可用时间片</span>
          <strong>{{ runtimeConfig.effectiveTimeSlotCount }}</strong>
        </div>
        <div class="config-pill">
          <span class="config-label">教学时间片总数</span>
          <strong>{{ runtimeConfig.rawTeachingTimeSlotCount }}</strong>
        </div>
        <div class="config-pill">
          <span class="config-label">默认连堂上限</span>
          <strong>{{ runtimeConfig.defaultContinuousLimit || '--' }}</strong>
        </div>
      </div>
      <p class="config-note">
        当前遗传排课算法仍运行在 legacy 25 格编码窗口内，因此只会消费工作日 1-5、节次 1-5 范围内的可教学时间片。
      </p>
    </el-card>

    <el-card v-if="latestArrangeSummary" shadow="never" class="plan-card summary-card">
      <div class="summary-header">
        <div>
          <div class="card-title">排课结果摘要</div>
          <div class="card-subtitle">
            当前仅展示本次会话中最近一次标准排课执行结果，摘要口径按任务维度统计。
          </div>
        </div>
        <el-tag :type="latestArrangeSummary.unscheduledTaskCount > 0 ? 'warning' : 'success'" effect="light" round>
          {{ latestArrangeSummary.unscheduledTaskCount > 0 ? '存在未排成功任务' : '任务已全部生成课表' }}
        </el-tag>
      </div>
      <div class="summary-metrics">
        <article v-for="item in arrangeSummaryCards" :key="item.label" class="metric-tile">
          <span class="metric-label">{{ item.label }}</span>
          <strong class="metric-value">{{ item.value }}</strong>
          <span class="metric-note">{{ item.note }}</span>
        </article>
      </div>
      <div v-if="latestArrangeSummary.unscheduledTasks.length" class="reason-board">
        <div class="reason-title">未排成功任务与原因</div>
        <div class="reason-list">
          <div v-for="item in latestArrangeSummary.unscheduledTasks" :key="item" class="reason-item">
            {{ item }}
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="plan-card">
      <div class="toolbar-row">
        <div class="summary-group">
          <div class="summary-chip">
            <span class="summary-label">当前学期</span>
            <strong>{{ selectedSemester || '--' }}</strong>
          </div>
          <div class="summary-chip">
            <span class="summary-label">当前页任务</span>
            <strong>{{ taskState.filteredCount }}</strong>
          </div>
          <div class="summary-chip accent-chip">
            <span class="summary-label">最近执行</span>
            <strong>{{ latestExecutionSummary }}</strong>
          </div>
        </div>
        <div class="toolbar-actions">
          <el-upload
            class="excel-upload"
            :show-file-list="false"
            :auto-upload="false"
            :before-upload="handleExcelUpload"
            accept=".xls,.xlsx"
          >
            <el-button class="ghost-action">导入任务</el-button>
          </el-upload>
          <el-button class="ghost-action" @click="handleTemplateDownload">下载模板</el-button>
          <el-button class="ghost-action" @click="prefillTaskByClass">按班级回填示例</el-button>
          <el-button class="primary-action" type="primary" @click="openTaskDialog()">新增任务</el-button>
        </div>
      </div>

      <el-alert
        v-if="taskStatus"
        class="status-alert"
        :type="taskStatus.type"
        :title="taskStatus.title"
        :description="taskStatus.description"
        :closable="false"
        show-icon
      />

      <div class="filter-row">
        <el-input
          v-model="taskState.keyword"
          clearable
          class="filter-input"
          placeholder="搜索班级 / 课程 / 教师"
          @input="applyTaskFilters"
          @clear="applyTaskFilters"
        />
        <el-select
          v-model="taskState.courseFilter"
          clearable
          filterable
          class="filter-select"
          placeholder="按课程筛选"
          @change="applyTaskFilters"
          @clear="applyTaskFilters"
        >
          <el-option
            v-for="item in courseOptions"
            :key="item.id"
            :label="`${item.courseNo} ${item.courseName}`"
            :value="item.courseNo"
          />
        </el-select>
        <el-select
          v-model="taskState.teacherFilter"
          clearable
          filterable
          class="filter-select"
          placeholder="按教师筛选"
          @change="applyTaskFilters"
          @clear="applyTaskFilters"
        >
          <el-option
            v-for="item in teacherOptions"
            :key="item.id"
            :label="`${item.teacherNo} ${item.realname || ''}`"
            :value="item.teacherNo"
          />
        </el-select>
        <el-select
          v-model="taskState.fixFilter"
          clearable
          class="filter-select filter-select--narrow"
          placeholder="固定排课"
          @change="applyTaskFilters"
          @clear="applyTaskFilters"
        >
          <el-option label="固定" value="1" />
          <el-option label="非固定" value="0" />
        </el-select>
        <el-button class="ghost-action" @click="resetTaskFilters">重置筛选</el-button>
      </div>

      <el-table :data="taskState.displayRecords" stripe v-loading="taskState.loading">
        <el-table-column prop="classNo" label="班级编号" min-width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="realname" label="教师姓名" min-width="110" />
        <el-table-column prop="courseAttr" label="课程属性" min-width="120" />
        <el-table-column prop="studentNum" label="人数" width="80" />
        <el-table-column prop="weeksNumber" label="周学时" width="90" />
        <el-table-column prop="weeksSum" label="周数" width="80" />
        <el-table-column label="连堂需求" min-width="140">
          <template #default="{ row }">
            <el-tag :type="row.needContinuous === 1 ? 'warning' : 'info'" effect="plain">
              {{ row.needContinuous === 1 ? `连堂 ${row.continuousSize || 2} 节` : '常规排课' }}
            </el-tag>
          </template>
        </el-table-column>
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
            <el-space>
              <el-button link type="primary" @click="goToSchedule(row.classNo)">课表</el-button>
              <el-button link type="danger" :disabled="!row.id && !row.standardId" @click="removeTask(row)">删除</el-button>
            </el-space>
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

    <el-card shadow="never" class="plan-card">
      <template #header>
        <div class="log-header">
          <div>
            <div class="card-title">最近排课执行记录</div>
            <div class="card-subtitle">保留当前学期最近 {{ executionLogLimit }} 次执行结果，方便核对成功、失败和耗时。</div>
          </div>
          <el-button class="ghost-action" :loading="executionLogsLoading" @click="loadExecutionLogs">
            刷新记录
          </el-button>
        </div>
      </template>
      <el-alert
        v-if="executionLogStatus"
        class="status-alert"
        :type="executionLogStatus.type"
        :title="executionLogStatus.title"
        :description="executionLogStatus.description"
        :closable="false"
        show-icon
      />
      <el-table :data="executionLogs" stripe v-loading="executionLogsLoading" empty-text="暂无排课执行记录">
        <el-table-column prop="createTime" label="执行时间" min-width="170" />
        <el-table-column prop="semester" label="学期" min-width="120" />
        <el-table-column label="执行状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskCount" label="任务数" width="90" />
        <el-table-column prop="generatedPlanCount" label="生成课表" width="110" />
        <el-table-column label="耗时" width="120">
          <template #default="{ row }">
            {{ formatDuration(row.durationMs) }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="执行人" width="120">
          <template #default="{ row }">
            {{ row.operatorName || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="message" label="执行结果" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goToSchedule('', row.semester)">查看课表</el-button>
          </template>
        </el-table-column>
      </el-table>
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
          <span>当前统一通过标准排课接口触发排课，内部仍复用现有算法实现，后续会继续替换旧表逻辑。</span>
        </div>
        <div class="tip-item">
          <strong>查看课表</strong>
          <span>切到“课表管理”页，按班级选择后即可看到排课结果。</span>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="tips-card">
      <template #header>
        <div class="card-title">录入约束建议</div>
      </template>
      <div class="constraint-grid">
        <div class="constraint-item">
          <strong>周学时建议</strong>
          <span>优先录入偶数节，例如 2、4、6。旧算法对单双周和连排支持有限，先按常规课处理。</span>
        </div>
        <div class="constraint-item">
          <strong>固定时间编码</strong>
          <span>固定排课请填写两位时间编码，如 `01`、`13`。当前课表视图按周一到周五、每天五节映射。</span>
        </div>
        <div class="constraint-item">
          <strong>人数与教室容量</strong>
          <span>学生人数应尽量贴近真实班级人数，过大的人数会降低教室匹配成功率。</span>
        </div>
        <div class="constraint-item">
          <strong>教师与班级</strong>
          <span>同一教师、同一班级在同一学期内不要重复录入明显冲突的固定时段任务。</span>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="taskDialogVisible" title="新增排课任务" width="760px">
      <el-form :model="taskForm" label-position="top">
        <div class="dialog-alert">
          <div class="alert-title">录入前检查</div>
          <div class="alert-text">
            请先确认学期、班级、课程、教师四项都已对应真实数据；固定时间仅在必须占位时填写。
          </div>
        </div>
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
            <el-select
              v-model="taskForm.courseNo"
              clearable
              filterable
              placeholder="选择课程，例如 10001 高等数学"
              @change="handleCourseChange"
              @clear="clearCourseSelection"
            >
              <el-option
                v-for="item in courseOptions"
                :key="item.id"
                :label="`${item.courseNo} ${item.courseName}`"
                :value="item.courseNo"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="课程编号">
            <el-input v-model="taskForm.courseNo" placeholder="例如 10001，建议优先从上方课程下拉选择" />
          </el-form-item>
          <el-form-item label="教师姓名">
            <el-select
              v-model="taskForm.teacherNo"
              clearable
              filterable
              placeholder="选择教师，例如 T2026001 张老师"
              @change="handleTeacherChange"
              @clear="clearTeacherSelection"
            >
              <el-option
                v-for="item in teacherOptions"
                :key="item.id"
                :label="`${item.teacherNo} ${item.realname || ''}`"
                :value="item.teacherNo"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="教师编号">
            <el-input v-model="taskForm.teacherNo" placeholder="例如 T2026001，建议优先从上方教师下拉选择" />
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
        <div class="constraint-band">
          <div class="constraint-band__copy">
            <div class="constraint-band__title">连堂约束</div>
            <div class="constraint-band__text">
              需要连堂时会把该任务作为连续节次约束参与排课，当前规则默认上限为
              {{ runtimeConfig.defaultContinuousLimit || '未配置' }} 节。
            </div>
          </div>
          <div class="constraint-band__controls">
            <el-form-item label="是否连堂">
              <el-radio-group v-model="taskForm.needContinuous">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="连堂节数">
              <el-input-number
                v-model="taskForm.continuousSize"
                :min="2"
                :max="Math.max(runtimeConfig.defaultContinuousLimit || 2, 2)"
                :disabled="taskForm.needContinuous !== 1"
                controls-position="right"
              />
            </el-form-item>
          </div>
        </div>
        <div class="form-grid">
          <el-form-item label="是否固定上课时间">
            <el-radio-group v-model="taskForm.isFix">
              <el-radio value="0">否</el-radio>
              <el-radio value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="固定时间">
            <el-input v-model="taskForm.classTime" placeholder="例如 01、13；未固定可留空，不建议一次填多个编码" />
          </el-form-item>
        </div>
        <div class="validation-preview">
          <el-tag :type="formValidation.isValid ? 'success' : 'warning'" effect="plain">
            {{ formValidation.isValid ? '当前表单可提交' : '当前表单存在待处理项' }}
          </el-tag>
          <div class="validation-list">
            <span v-for="item in formValidation.messages" :key="item">{{ item }}</span>
          </div>
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
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchCoursePage, fetchTeacherPage } from '@/api/modules/base';
import { getErrorMessage } from '@/utils/http';
import { fetchScheduleConfig } from '@/api/modules/system';
import {
  arrangeClassTask,
  createClassTask,
  deleteStandardClassTask,
  downloadClassTaskTemplate,
  fetchArrangeLogs,
  fetchClassOptions,
  fetchClassTaskPage,
  fetchSemesterList,
  uploadClassTaskExcel
} from '@/api/modules/course';

const semesters = ref([]);
const selectedSemester = ref('');
const router = useRouter();
const arranging = ref(false);
const taskDialogVisible = ref(false);
const taskSubmitting = ref(false);
const classOptions = ref([]);
const courseOptions = ref([]);
const teacherOptions = ref([]);
const executionLogs = ref([]);
const executionLogsLoading = ref(false);
const taskLoadError = ref('');
const executionLogError = ref('');
const executionLogLimit = 8;
const latestArrangeSummary = ref(null);
const runtimeConfig = reactive({
  ruleName: '',
  effectiveTimeSlotCount: 25,
  rawTeachingTimeSlotCount: 0,
  defaultContinuousLimit: 0,
  configApplied: false
});

const taskState = reactive({
  loading: false,
  records: [],
  displayRecords: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  courseFilter: '',
  teacherFilter: '',
  fixFilter: '',
  filteredCount: 0
});

const taskForm = ref(createTaskForm());

const latestExecutionSummary = computed(() => {
  if (latestArrangeSummary.value) {
    return `成功率 ${formatSuccessRate(latestArrangeSummary.value.successRate)}`;
  }
  if (!executionLogs.value.length) {
    return '暂无记录';
  }
  const latest = executionLogs.value[0];
  return latest.status === 1 ? '最近一次成功' : '最近一次失败';
});

const arrangeSummaryCards = computed(() => {
  if (!latestArrangeSummary.value) {
    return [];
  }
  const summary = latestArrangeSummary.value;
  return [
    {
      label: '任务总数',
      value: summary.taskCount,
      note: '本次排课接收的标准任务数'
    },
    {
      label: '成功任务',
      value: summary.scheduledTaskCount,
      note: '至少生成一条标准课表记录的任务'
    },
    {
      label: '未成功任务',
      value: summary.unscheduledTaskCount,
      note: '未生成标准课表记录的任务'
    },
    {
      label: '冲突任务',
      value: summary.conflictTaskCount,
      note: '当前按未成功任务口径统计'
    },
    {
      label: '生成课表',
      value: summary.generatedPlanCount,
      note: '标准课表结果记录总数'
    },
    {
      label: '成功率',
      value: formatSuccessRate(summary.successRate),
      note: '成功任务数 / 任务总数'
    }
  ];
});

const formValidation = computed(() => {
  const messages = [];
  if (!taskForm.value.semester?.trim()) {
    messages.push('请填写学期');
  }
  if (!taskForm.value.classNo?.trim()) {
    messages.push('请关联班级');
  }
  if (!taskForm.value.courseNo?.trim() || !taskForm.value.courseName?.trim()) {
    messages.push('课程编号和课程名称需要同时填写');
  }
  if (!taskForm.value.teacherNo?.trim() || !taskForm.value.realname?.trim()) {
    messages.push('教师编号和教师姓名需要同时填写');
  }
  if (!taskForm.value.weeksNumber || taskForm.value.weeksNumber < 1) {
    messages.push('周学时必须大于 0');
  } else if (taskForm.value.weeksNumber % 2 !== 0) {
    messages.push('当前建议优先使用偶数周学时');
  }
  if (taskForm.value.isFix === '1') {
    if (!taskForm.value.classTime?.trim()) {
      messages.push('固定排课时必须填写固定时间');
    } else if (!/^\d{2}$/.test(taskForm.value.classTime.trim())) {
      messages.push('固定时间当前只支持两位编码，例如 01、13');
    }
  }
  if (taskForm.value.needContinuous === 1) {
    if (!taskForm.value.continuousSize || taskForm.value.continuousSize < 2) {
      messages.push('连堂任务的连堂节数不能小于 2');
    }
    if (runtimeConfig.defaultContinuousLimit > 0 && taskForm.value.continuousSize > runtimeConfig.defaultContinuousLimit) {
      messages.push(`连堂节数不能超过当前规则上限 ${runtimeConfig.defaultContinuousLimit}`);
    }
  }
  if (taskForm.value.studentNum > 120) {
    messages.push('学生人数较大，请确认是否需要大教室');
  }
  return {
    isValid: messages.length === 0,
    messages: messages.length ? messages : ['基础信息完整，可继续保存']
  };
});

const taskStatus = computed(() => {
  if (taskLoadError.value) {
    return {
      type: 'warning',
      title: '任务加载失败',
      description: taskLoadError.value
    };
  }
  if (!taskState.loading && selectedSemester.value && !taskState.records.length) {
    return {
      type: 'info',
      title: '当前学期暂无排课任务',
      description: '可以先导入任务，或者手动新增一条排课任务。'
    };
  }
  return null;
});

const executionLogStatus = computed(() => {
  if (executionLogError.value) {
    return {
      type: 'warning',
      title: '执行记录加载失败',
      description: executionLogError.value
    };
  }
  return null;
});

function getImportResult(payload) {
  return payload?.data || payload?.response?.data?.data || null;
}

function buildImportSummary(payload) {
  const result = getImportResult(payload);
  if (!result) {
    return '课程任务导入成功';
  }
  const totalCount = result.totalCount ?? 0;
  const successCount = result.successCount ?? 0;
  const failedCount = result.failedCount ?? 0;
  return `课程任务导入完成，合计 ${totalCount} 条，成功 ${successCount} 条，失败 ${failedCount} 条`;
}

function normalizeArrangeSummary(payload) {
  if (!payload) {
    return null;
  }
  return {
    durationMs: Number(payload.durationMs || 0),
    generatedPlanCount: Number(payload.generatedPlanCount || 0),
    taskCount: Number(payload.taskCount || 0),
    scheduledTaskCount: Number(payload.scheduledTaskCount || 0),
    unscheduledTaskCount: Number(payload.unscheduledTaskCount || 0),
    conflictTaskCount: Number(payload.conflictTaskCount || 0),
    successRate: Number(payload.successRate || 0),
    unscheduledTasks: Array.isArray(payload.unscheduledTasks) ? payload.unscheduledTasks : [],
    effectiveScheduleRuleName: payload.effectiveScheduleRuleName || '',
    effectiveTimeSlotCount: Number(payload.effectiveTimeSlotCount || 0),
    timeSlotConfigApplied: Boolean(payload.timeSlotConfigApplied)
  };
}

function formatSuccessRate(value) {
  return `${Number(value || 0).toFixed(2)}%`;
}

async function showImportErrors(payload) {
  const result = getImportResult(payload);
  const errors = Array.isArray(result?.errors) ? result.errors : [];
  if (!errors.length) {
    return;
  }
  const previewErrors = errors.slice(0, 12).join('<br/>');
  const appendix = errors.length > 12 ? `<br/><br/>仅展示前 12 条，共 ${errors.length} 条。` : '';
  await ElMessageBox.alert(`${previewErrors}${appendix}`, '课程任务导入失败明细', {
    type: 'error',
    dangerouslyUseHTMLString: true,
    confirmButtonText: '我知道了'
  });
}

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
    needContinuous: 0,
    continuousSize: 2,
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
  const response = await fetchClassOptions();
  classOptions.value = response.data || [];
}

async function loadCourseOptions() {
  const response = await fetchCoursePage(1, 200);
  courseOptions.value = (response.data?.records || []).filter((item) => item.status === 0);
}

async function loadTeacherOptions() {
  const response = await fetchTeacherPage(1, 200);
  teacherOptions.value = (response.data?.records || []).filter((item) => item.status === 0);
}

function normalizeEffectiveTimeSlots(timeSlots = []) {
  return (timeSlots || [])
    .filter((item) => Number(item?.isTeaching || 0) === 1)
    .filter((item) => Number(item?.isFixedBreak || 0) === 0)
    .filter((item) => Number(item?.weekdayNo || 0) >= 1 && Number(item?.weekdayNo || 0) <= 5)
    .filter((item) => Number(item?.periodNo || 0) >= 1 && Number(item?.periodNo || 0) <= 5);
}

async function loadRuntimeConfig() {
  try {
    const response = await fetchScheduleConfig();
    const payload = response.data || {};
    const effectiveTimeSlots = normalizeEffectiveTimeSlots(payload.timeSlots || []);
    const rawTeachingTimeSlots = (payload.timeSlots || [])
      .filter((item) => Number(item?.isTeaching || 0) === 1)
      .filter((item) => Number(item?.isFixedBreak || 0) === 0);
    runtimeConfig.ruleName = payload.scheduleRule?.ruleName || '默认 25 格时间片';
    runtimeConfig.effectiveTimeSlotCount = effectiveTimeSlots.length || 25;
    runtimeConfig.rawTeachingTimeSlotCount = rawTeachingTimeSlots.length;
    runtimeConfig.defaultContinuousLimit = Number(payload.scheduleRule?.defaultContinuousLimit || 0) || 0;
    runtimeConfig.configApplied = effectiveTimeSlots.length > 0;
  } catch (error) {
    runtimeConfig.ruleName = '默认 25 格时间片';
    runtimeConfig.effectiveTimeSlotCount = 25;
    runtimeConfig.rawTeachingTimeSlotCount = 0;
    runtimeConfig.defaultContinuousLimit = 0;
    runtimeConfig.configApplied = false;
  }
}

async function loadClassTasks(resetPage = false) {
  if (!selectedSemester.value) {
    return;
  }
  if (resetPage) {
    taskState.pageNum = 1;
  }
  taskState.loading = true;
  taskLoadError.value = '';
  try {
    const response = await fetchClassTaskPage(taskState.pageNum, selectedSemester.value, taskState.pageSize);
    const pageData = response.data || {};
    taskState.records = pageData.records || [];
    taskState.total = pageData.total || 0;
    applyTaskFilters();
  } catch (error) {
    taskState.records = [];
    taskState.displayRecords = [];
    taskState.total = 0;
    taskState.filteredCount = 0;
    taskLoadError.value = getErrorMessage(error, '排课任务加载失败，请稍后重试');
  } finally {
    taskState.loading = false;
  }
}

function applyTaskFilters() {
  const keyword = taskState.keyword.trim().toLowerCase();
  taskState.displayRecords = taskState.records.filter((item) => {
    const matchKeyword = !keyword || [item.classNo, item.courseName, item.realname, item.courseNo, item.teacherNo]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(keyword);
    const matchCourse = !taskState.courseFilter || item.courseNo === taskState.courseFilter;
    const matchTeacher = !taskState.teacherFilter || item.teacherNo === taskState.teacherFilter;
    const matchFix = !taskState.fixFilter || item.isFix === taskState.fixFilter;
    return matchKeyword && matchCourse && matchTeacher && matchFix;
  });
  taskState.filteredCount = taskState.displayRecords.length;
}

function resetTaskFilters() {
  taskState.keyword = '';
  taskState.courseFilter = '';
  taskState.teacherFilter = '';
  taskState.fixFilter = '';
  applyTaskFilters();
}

async function loadExecutionLogs() {
  if (!selectedSemester.value) {
    executionLogs.value = [];
    executionLogError.value = '';
    return;
  }
  executionLogsLoading.value = true;
  executionLogError.value = '';
  try {
    const response = await fetchArrangeLogs({
      semester: selectedSemester.value,
      limit: executionLogLimit
    });
    executionLogs.value = response.data || [];
  } catch (error) {
    executionLogs.value = [];
    executionLogError.value = getErrorMessage(error, '排课执行记录加载失败，请稍后重试');
  } finally {
    executionLogsLoading.value = false;
  }
}

function handleTaskPageChange(page) {
  taskState.pageNum = page;
  loadClassTasks();
}

async function handleSemesterChange() {
  latestArrangeSummary.value = null;
  await Promise.all([loadClassTasks(true), loadExecutionLogs()]);
}

function openTaskDialog() {
  taskForm.value = createTaskForm();
  taskDialogVisible.value = true;
}

async function handleTemplateDownload() {
  try {
    await downloadClassTaskTemplate();
  } catch (error) {
    ElMessage.error('模板下载失败，请稍后重试');
  }
}

async function handleExcelUpload(file) {
  try {
    const response = await uploadClassTaskExcel(file);
    ElMessage.success(buildImportSummary(response));
    await Promise.all([loadClassTasks(true), loadExecutionLogs()]);
  } catch (error) {
    await showImportErrors(error);
    return false;
  }
  return false;
}

function handleCourseChange(courseNo) {
  const currentCourse = courseOptions.value.find((item) => item.courseNo === courseNo);
  if (!currentCourse) {
    return;
  }
  taskForm.value.courseNo = currentCourse.courseNo || '';
  taskForm.value.courseName = currentCourse.courseName || '';
  taskForm.value.courseAttr = currentCourse.courseAttr || taskForm.value.courseAttr;
  taskForm.value.needContinuous = Number(currentCourse.needContinuous || 0) === 1 ? 1 : taskForm.value.needContinuous;
  taskForm.value.continuousSize = Number(currentCourse.needContinuous || 0) === 1
    ? Math.max(2, Number(currentCourse.continuousSize || 2))
    : taskForm.value.continuousSize;
}

function clearCourseSelection() {
  taskForm.value.courseName = '';
  taskForm.value.courseAttr = '';
}

function handleTeacherChange(teacherNo) {
  const currentTeacher = teacherOptions.value.find((item) => item.teacherNo === teacherNo);
  if (!currentTeacher) {
    return;
  }
  taskForm.value.teacherNo = currentTeacher.teacherNo || '';
  taskForm.value.realname = currentTeacher.realname || '';
}

function clearTeacherSelection() {
  taskForm.value.realname = '';
}

function goToSchedule(classNo = '', semester = selectedSemester.value) {
  const query = {
    view: 'class',
    semester: semester || selectedSemester.value || ''
  };
  if (classNo) {
    query.classNo = classNo;
  }
  router.push({
    path: '/schedule',
    query
  });
}

function prefillTaskByClass() {
  const firstClass = classOptions.value[0];
  const firstCourse = courseOptions.value[0];
  const firstTeacher = teacherOptions.value[0];
  taskForm.value = {
    ...createTaskForm(),
    gradeNo: firstClass?.remark || '',
    classNo: firstClass?.classNo || '',
    studentNum: firstClass?.num || 40,
    courseNo: firstCourse?.courseNo || '',
    courseName: firstCourse?.courseName || '',
    courseAttr: firstCourse?.courseAttr || '',
    needContinuous: Number(firstCourse?.needContinuous || 0) === 1 ? 1 : 0,
    continuousSize: Number(firstCourse?.needContinuous || 0) === 1 ? Math.max(2, Number(firstCourse?.continuousSize || 2)) : 2,
    teacherNo: firstTeacher?.teacherNo || '',
    realname: firstTeacher?.realname || ''
  };
  taskDialogVisible.value = true;
}

async function submitTask() {
  const semester = taskForm.value.semester?.trim();
  const courseNo = taskForm.value.courseNo?.trim();
  const courseName = taskForm.value.courseName?.trim();
  const teacherNo = taskForm.value.teacherNo?.trim();
  const realname = taskForm.value.realname?.trim();
  const fixedTime = taskForm.value.classTime?.trim();
  if (!semester) {
    ElMessage.warning('请先填写学期');
    return;
  }
  if (!taskForm.value.classNo) {
    ElMessage.warning('请先选择班级');
    return;
  }
  if (!courseNo || !courseName) {
    ElMessage.warning('请完整填写课程编号和课程名称');
    return;
  }
  if (!teacherNo || !realname) {
    ElMessage.warning('请完整填写教师编号和教师姓名');
    return;
  }
  if (!taskForm.value.weeksNumber || taskForm.value.weeksNumber < 1) {
    ElMessage.warning('周学时必须大于 0');
    return;
  }
  if (!taskForm.value.weeksSum || taskForm.value.weeksSum < 1) {
    ElMessage.warning('周数必须大于 0');
    return;
  }
  if (taskForm.value.needContinuous === 1) {
    if (!taskForm.value.continuousSize || taskForm.value.continuousSize < 2) {
      ElMessage.warning('连堂任务的连堂节数不能小于 2');
      return;
    }
    if (runtimeConfig.defaultContinuousLimit > 0 && taskForm.value.continuousSize > runtimeConfig.defaultContinuousLimit) {
      ElMessage.warning(`连堂节数不能超过当前规则上限 ${runtimeConfig.defaultContinuousLimit}`);
      return;
    }
  }
  if (taskForm.value.isFix === '1') {
    if (!fixedTime) {
      ElMessage.warning('固定排课时必须填写固定时间');
      return;
    }
    if (!/^\d{2}$/.test(fixedTime)) {
      ElMessage.warning('固定时间当前只支持两位编码，例如 01、13');
      return;
    }
  } else {
    taskForm.value.classTime = '';
  }

  taskSubmitting.value = true;
  try {
    await createClassTask({
      ...taskForm.value,
      semester,
      courseNo,
      courseName,
      teacherNo,
      realname,
      needContinuous: taskForm.value.needContinuous === 1 ? 1 : 0,
      continuousSize: taskForm.value.needContinuous === 1 ? Math.max(2, Number(taskForm.value.continuousSize || 2)) : 1,
      classTime: fixedTime || ''
    });
    ElMessage.success('排课任务创建成功');
    taskDialogVisible.value = false;
    if (!semesters.value.includes(taskForm.value.semester)) {
      semesters.value.unshift(taskForm.value.semester);
    }
    selectedSemester.value = taskForm.value.semester;
    await loadClassTasks(true);
    await loadExecutionLogs();
  } finally {
    taskSubmitting.value = false;
  }
}

async function removeTask(row) {
  await ElMessageBox.confirm(`确认删除班级“${row.classNo}”的任务吗？`, '删除确认', { type: 'warning' });
  await deleteStandardClassTask(row.standardId || row.id);
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
    latestArrangeSummary.value = normalizeArrangeSummary(response.data);
    if (latestArrangeSummary.value) {
      runtimeConfig.ruleName = latestArrangeSummary.value.effectiveScheduleRuleName || runtimeConfig.ruleName;
      runtimeConfig.effectiveTimeSlotCount = latestArrangeSummary.value.effectiveTimeSlotCount || runtimeConfig.effectiveTimeSlotCount;
      runtimeConfig.configApplied = latestArrangeSummary.value.timeSlotConfigApplied;
    }
    ElMessage({
      type: latestArrangeSummary.value?.unscheduledTaskCount > 0 ? 'warning' : 'success',
      message: response.message || '排课执行完成'
    });
    await Promise.all([loadClassTasks(true), loadExecutionLogs()]);
  } finally {
    arranging.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadSemesters(), loadClassOptions(), loadCourseOptions(), loadTeacherOptions(), loadRuntimeConfig()]);
  await handleSemesterChange();
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

.excel-upload {
  display: inline-flex;
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

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-input {
  width: 260px;
}

.filter-select {
  width: 220px;
}

.filter-select--narrow {
  width: 140px;
}

.summary-group {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.summary-chip {
  min-width: 160px;
  padding: 14px 16px;
  border: 1px solid #e5edf6;
  border-radius: 18px;
  background: rgb(255 255 255 / 78%);
}

.accent-chip {
  border-color: #cfe3ff;
  background: linear-gradient(135deg, rgb(229 240 255 / 92%) 0%, rgb(236 251 248 / 92%) 100%);
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

.card-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #70839b;
}

.status-alert {
  margin-bottom: 16px;
  border-radius: 18px;
}

.log-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.summary-card {
  overflow: hidden;
  background:
    linear-gradient(135deg, rgb(248 244 234 / 90%) 0%, rgb(255 255 255 / 96%) 44%, rgb(240 247 255 / 96%) 100%);
}

.config-card {
  background:
    radial-gradient(circle at top right, rgb(214 229 255 / 45%), transparent 30%),
    linear-gradient(135deg, rgb(249 251 255 / 96%) 0%, rgb(255 252 245 / 96%) 100%);
}

.config-headline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.config-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.config-pill {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px 20px;
  border: 1px solid #d7e1ef;
  border-radius: 20px;
  background: rgb(255 255 255 / 82%);
}

.config-label {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #7b8aa0;
}

.config-note {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: #61748d;
}

.summary-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.summary-metrics {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.metric-tile {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 148px;
  padding: 18px;
  border: 1px solid rgb(179 154 102 / 22%);
  border-radius: 20px;
  background:
    linear-gradient(180deg, rgb(255 255 255 / 88%) 0%, rgb(252 249 242 / 95%) 100%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 75%);
}

.metric-tile::before {
  content: '';
  position: absolute;
  inset: 0 auto auto 0;
  width: 44px;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, #8c6c2b 0%, #d3b36a 100%);
}

.metric-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #7a6846;
}

.metric-value {
  font-size: 28px;
  line-height: 1;
  color: #17263d;
}

.metric-note {
  font-size: 12px;
  line-height: 1.6;
  color: #6f7f95;
}

.reason-board {
  margin-top: 18px;
  padding: 18px 20px;
  border: 1px solid #eadfbe;
  border-radius: 22px;
  background: linear-gradient(135deg, rgb(255 251 242 / 95%) 0%, rgb(255 247 231 / 92%) 100%);
}

.reason-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #694f18;
}

.reason-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.reason-item {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgb(255 255 255 / 75%);
  color: #6b5d40;
  font-size: 13px;
  line-height: 1.65;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.constraint-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.constraint-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border: 1px solid #e4edf7;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #fbfcff 100%);
  color: #586b83;
  font-size: 13px;
  line-height: 1.7;
}

.dialog-alert {
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid #dce8f8;
  border-radius: 16px;
  background: linear-gradient(135deg, rgb(249 252 255 / 96%) 0%, rgb(240 248 255 / 96%) 100%);
}

.alert-title {
  font-size: 13px;
  font-weight: 700;
  color: #1d3557;
}

.alert-text {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #60748d;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.compact-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.constraint-band {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 1fr);
  gap: 18px;
  margin-bottom: 14px;
  padding: 18px 20px;
  border: 1px solid #eadfbe;
  border-radius: 22px;
  background: linear-gradient(135deg, rgb(255 250 240 / 96%) 0%, rgb(250 244 230 / 88%) 100%);
}

.constraint-band__copy {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.constraint-band__title {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #7c5b1e;
}

.constraint-band__text {
  font-size: 13px;
  line-height: 1.7;
  color: #6f634d;
}

.constraint-band__controls {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  align-items: start;
}

.validation-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 8px;
}

.validation-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: #6e8197;
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
  .toolbar-row,
  .log-header,
  .summary-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-input,
  .filter-select,
  .filter-select--narrow {
    width: 100%;
  }

  .tips-grid,
  .constraint-grid,
  .constraint-band,
  .constraint-band__controls,
  .form-grid,
  .compact-grid,
  .config-strip,
  .summary-metrics,
  .reason-list {
    grid-template-columns: 1fr;
  }
}
</style>
