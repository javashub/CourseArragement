<template>
  <section class="config-shell" v-loading="loading">
    <div class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">Scheduling Control Deck</div>
        <h1 class="hero-title">系统配置</h1>
        <p class="hero-description">
          这里决定系统是否启用多校区、多学院、走班制，以及每周上课天数、每日节次数和时间片模板。后续排课算法会以这里的配置为准。
        </p>
      </div>
      <div class="hero-actions">
        <el-button class="ghost-action" @click="loadConfig">重新加载</el-button>
        <el-button class="primary-action" type="primary" :loading="scheduleSaving" @click="saveRuleSection">
          保存排课规则
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="scope-card">
      <div class="scope-header">
        <div>
          <div class="scope-title">配置范围</div>
          <div class="scope-caption">可按校区、学院、学段维护差异化配置；清空后会自动重新加载默认配置。</div>
        </div>
        <el-button text type="primary" @click="handleScopeReset">恢复全局默认</el-button>
      </div>
      <div class="scope-grid">
        <el-select
          v-model="scope.campusId"
          clearable
          placeholder="选择校区，例如 主校区"
          @change="handleCampusChange"
          @clear="handleCampusChange"
        >
          <el-option v-for="campus in campuses" :key="campus.id" :label="campus.campusName" :value="campus.id" />
        </el-select>
        <el-select
          v-model="scope.collegeId"
          clearable
          placeholder="选择学院，例如 信息工程学院"
          @change="handleScopeReload"
          @clear="handleScopeReload"
        >
          <el-option v-for="college in colleges" :key="college.id" :label="college.collegeName" :value="college.id" />
        </el-select>
        <el-select
          v-model="scope.stageId"
          clearable
          placeholder="选择学段，例如 高中"
          @change="handleScopeReload"
          @clear="handleScopeReload"
        >
          <el-option v-for="stage in stages" :key="stage.id" :label="stage.stageName" :value="stage.id" />
        </el-select>
      </div>
    </el-card>

    <div class="config-grid">
      <el-card shadow="never" class="config-card schedule-card">
        <template #header>
          <div class="card-head">
            <div>
              <div class="card-title">排课规则</div>
              <div class="card-caption">先把时间结构和规则骨架定下来，后续排课算法直接消费这里的数据。</div>
            </div>
            <el-tag type="success" effect="plain">{{ scheduleForm.status === 1 ? '启用中' : '停用中' }}</el-tag>
          </div>
        </template>

        <el-form :model="scheduleForm" label-position="top" class="rule-form">
          <el-alert
            v-if="scheduleValidation.messages.length"
            class="config-alert"
            :type="scheduleValidation.isValid ? 'success' : 'warning'"
            :title="scheduleValidation.isValid ? '当前规则结构可保存' : '当前规则存在待处理项'"
            :description="scheduleValidation.messages.join('；')"
            :closable="false"
            show-icon
          />
          <div class="form-grid">
            <el-form-item label="规则编码">
              <el-input v-model="scheduleForm.ruleCode" placeholder="例如 RULE_GLOBAL_DEFAULT、RULE_CAMPUS_MAIN" />
            </el-form-item>
            <el-form-item label="规则名称">
              <el-input v-model="scheduleForm.ruleName" placeholder="例如 全局默认规则、主校区高中部规则" />
            </el-form-item>
          </div>

          <div class="metrics-grid">
            <div class="metric-box">
              <span class="metric-label">每周上课天数</span>
              <el-input-number v-model="scheduleForm.weekDays" :min="1" :max="7" controls-position="right" />
            </div>
            <div class="metric-box">
              <span class="metric-label">每天总节数</span>
              <el-input-number v-model="scheduleForm.dayPeriods" :min="1" :max="20" controls-position="right" />
            </div>
            <div class="metric-box">
              <span class="metric-label">默认连堂上限</span>
              <el-input-number v-model="scheduleForm.defaultContinuousLimit" :min="1" :max="6" controls-position="right" />
            </div>
          </div>

          <div class="form-grid compact-grid">
            <el-form-item label="上午节数">
              <el-input-number v-model="scheduleForm.morningPeriods" :min="0" :max="20" controls-position="right" />
            </el-form-item>
            <el-form-item label="下午节数">
              <el-input-number v-model="scheduleForm.afternoonPeriods" :min="0" :max="20" controls-position="right" />
            </el-form-item>
            <el-form-item label="晚间节数">
              <el-input-number v-model="scheduleForm.nightPeriods" :min="0" :max="20" controls-position="right" />
            </el-form-item>
          </div>

          <div class="switch-row">
            <div class="switch-card">
              <div>
                <div class="switch-title">允许周末排课</div>
                <div class="switch-caption">培训机构、走班制或补课场景通常需要开启。</div>
              </div>
              <el-switch v-model="scheduleForm.allowWeekend" :active-value="1" :inactive-value="0" />
            </div>
            <div class="switch-card">
              <div>
                <div class="switch-title">设为默认规则</div>
                <div class="switch-caption">当前范围下优先使用这套规则作为排课入口。</div>
              </div>
              <el-switch v-model="scheduleForm.isDefault" :active-value="1" :inactive-value="0" />
            </div>
            <div class="switch-card">
              <div>
                <div class="switch-title">规则状态</div>
                <div class="switch-caption">停用后仅保留历史数据，不参与新排课。</div>
              </div>
              <el-switch v-model="scheduleForm.status" :active-value="1" :inactive-value="0" />
            </div>
          </div>

          <el-form-item label="规则备注">
            <el-input
              v-model="scheduleForm.remark"
              type="textarea"
              :rows="3"
              placeholder="例如 高中部每周上课 6 天，周六上午允许补排理化实验课"
            />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="config-card feature-card">
        <template #header>
          <div class="card-head">
            <div>
              <div class="card-title">功能开关</div>
              <div class="card-caption">控制系统启用哪些复杂场景，前后端都会基于这里做能力开关。</div>
            </div>
            <el-button class="soft-action" type="primary" plain :loading="featureSaving" @click="saveFeatureSection">
              保存开关
            </el-button>
          </div>
        </template>

        <div class="feature-stack">
          <div v-for="item in featureToggles" :key="item.toggleCode" class="feature-item">
            <div class="feature-copy">
              <div class="feature-name">{{ item.toggleName }}</div>
              <div class="feature-code">{{ item.toggleCode }}</div>
            </div>
            <div class="feature-control">
              <el-switch v-model="item.toggleValue" active-value="true" inactive-value="false" />
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card shadow="never" class="config-card time-card">
      <template #header>
        <div class="card-head">
          <div>
            <div class="card-title">时间片模板</div>
            <div class="card-caption">管理员可先生成默认时间片，再按实际作息微调每节课的开始和结束时间。</div>
          </div>
          <div class="header-actions">
            <el-button class="ghost-action" @click="generateTemplate">按规则生成模板</el-button>
            <el-button class="soft-action" type="primary" plain @click="appendTimeSlot">新增时间片</el-button>
            <el-button class="primary-action" type="primary" :loading="timeSlotSaving" @click="saveTimeSlotSection">
              保存时间片
            </el-button>
          </div>
        </div>
      </template>

      <div class="time-summary">
        <el-tag effect="plain" type="info">当前共 {{ timeSlots.length }} 条时间片</el-tag>
        <span>建议先保存排课规则，再保存时间片。</span>
      </div>

      <el-alert
        v-if="timeSlotValidation.messages.length"
        class="config-alert"
        :type="timeSlotValidation.isValid ? 'success' : 'warning'"
        :title="timeSlotValidation.isValid ? '当前时间片结构可保存' : '当前时间片存在待处理项'"
        :description="timeSlotValidation.messages.join('；')"
        :closable="false"
        show-icon
      />

      <el-table :data="timeSlots" stripe max-height="520">
        <el-table-column label="星期" width="100">
          <template #default="{ row }">
            <el-select v-model="row.weekdayNo">
              <el-option v-for="day in weekdayOptions" :key="day.value" :label="day.label" :value="day.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="节次" width="90">
          <template #default="{ row }">
            <el-input-number v-model="row.periodNo" :min="1" :max="30" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="节次名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.periodName" placeholder="例如 第1节、早读、晚自习" />
          </template>
        </el-table-column>
        <el-table-column label="时间分组" width="130">
          <template #default="{ row }">
            <el-select v-model="row.timeGroup">
              <el-option label="上午" value="MORNING" />
              <el-option label="下午" value="AFTERNOON" />
              <el-option label="晚间" value="NIGHT" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="130">
          <template #default="{ row }">
            <el-input v-model="row.startTimeText" placeholder="例如 08:00" />
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="130">
          <template #default="{ row }">
            <el-input v-model="row.endTimeText" placeholder="例如 08:45" />
          </template>
        </el-table-column>
        <el-table-column label="可上课" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.isTeaching" :active-value="1" :inactive-value="0" />
          </template>
        </el-table-column>
        <el-table-column label="固定休息" width="110">
          <template #default="{ row }">
            <el-switch v-model="row.isFixedBreak" :active-value="1" :inactive-value="0" />
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.remark" placeholder="例如 周一升旗后第一节顺延 10 分钟" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeTimeSlot($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchCampusList,
  fetchCollegeList,
  fetchFeatureToggles,
  fetchScheduleConfig,
  fetchStageList,
  saveFeatureToggles,
  saveScheduleConfig,
  saveTimeSlots
} from '@/api/modules/system';

const loading = ref(false);
const scheduleSaving = ref(false);
const featureSaving = ref(false);
const timeSlotSaving = ref(false);

const campuses = ref([]);
const colleges = ref([]);
const stages = ref([]);
const featureToggles = ref([]);
const timeSlots = ref([]);

const weekdayOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
];

const scope = reactive({
  campusId: null,
  collegeId: null,
  stageId: null
});

const scheduleForm = reactive(createDefaultRuleForm());

const scheduleValidation = computed(() => {
  const messages = [];
  const segmentSum = Number(scheduleForm.morningPeriods || 0) + Number(scheduleForm.afternoonPeriods || 0) + Number(scheduleForm.nightPeriods || 0);
  if (segmentSum !== Number(scheduleForm.dayPeriods || 0)) {
    messages.push('上午、下午、晚间节次数之和必须等于每天总节数');
  }
  if (Number(scheduleForm.allowWeekend || 0) === 0 && Number(scheduleForm.weekDays || 0) > 5) {
    messages.push('未开启周末排课时，每周上课天数不能超过 5 天');
  }
  if (Number(scheduleForm.defaultContinuousLimit || 0) > Number(scheduleForm.dayPeriods || 0)) {
    messages.push('默认连堂上限不能大于每天总节数');
  }
  return {
    isValid: messages.length === 0,
    messages: messages.length ? messages : ['结构校验通过，规则可直接保存']
  };
});

const timeSlotValidation = computed(() => {
  const messages = [];
  const duplicateKeys = new Set();
  for (const item of timeSlots.value) {
    const slotKey = `${item.weekdayNo}-${item.periodNo}`;
    if (duplicateKeys.has(slotKey)) {
      messages.push('同一星期和节次不能重复保存时间片');
      break;
    }
    duplicateKeys.add(slotKey);
    if (Number(item.weekdayNo || 0) > Number(scheduleForm.weekDays || 0)) {
      messages.push('存在超出每周上课天数范围的时间片');
      break;
    }
    if (Number(scheduleForm.allowWeekend || 0) === 0 && Number(item.weekdayNo || 0) > 5) {
      messages.push('未开启周末排课时，不能保存周末时间片');
      break;
    }
    if (Number(item.periodNo || 0) > Number(scheduleForm.dayPeriods || 0)) {
      messages.push('存在超出每天总节数范围的时间片');
      break;
    }
    if (Number(item.isTeaching || 0) === 1 && Number(item.isFixedBreak || 0) === 1) {
      messages.push('固定休息时间片不能同时标记为可上课');
      break;
    }
  }
  return {
    isValid: messages.length === 0,
    messages: messages.length ? messages : ['时间片结构校验通过，可直接保存']
  };
});

function createDefaultRuleForm() {
  return {
    id: null,
    ruleCode: 'RULE_GLOBAL_DEFAULT',
    ruleName: '全局默认排课规则',
    weekDays: 5,
    dayPeriods: 8,
    morningPeriods: 4,
    afternoonPeriods: 4,
    nightPeriods: 0,
    allowWeekend: 0,
    defaultContinuousLimit: 2,
    status: 1,
    isDefault: 1,
    remark: ''
  };
}

function createEmptyTimeSlot() {
  return {
    id: null,
    weekdayNo: 1,
    periodNo: 1,
    periodName: '第1节',
    timeGroup: 'MORNING',
    startTimeText: '08:00',
    endTimeText: '08:45',
    isTeaching: 1,
    isFixedBreak: 0,
    sortNo: 1,
    remark: ''
  };
}

function applyScheduleData(scheduleRule) {
  Object.assign(scheduleForm, createDefaultRuleForm(), scheduleRule || {});
  if (!scheduleForm.ruleCode) {
    scheduleForm.ruleCode = buildRuleCode();
  }
  if (!scheduleForm.ruleName) {
    scheduleForm.ruleName = buildRuleName();
  }
}

function buildRuleCode() {
  const segments = ['RULE'];
  if (scope.campusId) {
    segments.push(`CAMPUS_${scope.campusId}`);
  }
  if (scope.collegeId) {
    segments.push(`COLLEGE_${scope.collegeId}`);
  }
  if (scope.stageId) {
    segments.push(`STAGE_${scope.stageId}`);
  }
  if (segments.length === 1) {
    segments.push('GLOBAL_DEFAULT');
  }
  return segments.join('_');
}

function buildRuleName() {
  const labels = [];
  if (scope.campusId) {
    labels.push(campuses.value.find((item) => item.id === scope.campusId)?.campusName);
  }
  if (scope.collegeId) {
    labels.push(colleges.value.find((item) => item.id === scope.collegeId)?.collegeName);
  }
  if (scope.stageId) {
    labels.push(stages.value.find((item) => item.id === scope.stageId)?.stageName);
  }
  return `${labels.filter(Boolean).join(' / ') || '全局默认'}排课规则`;
}

function normalizeTimeSlots(list = []) {
  return list
    .map((item) => ({
      ...createEmptyTimeSlot(),
      ...item
    }))
    .sort((a, b) => (a.weekdayNo - b.weekdayNo) || (a.periodNo - b.periodNo));
}

async function loadScopeOptions() {
  const [campusRes, stageRes] = await Promise.all([fetchCampusList(), fetchStageList()]);
  campuses.value = campusRes.data || [];
  stages.value = stageRes.data || [];
  await loadCollegeOptions();
}

async function loadCollegeOptions() {
  const response = await fetchCollegeList({
    campusId: scope.campusId ?? undefined
  });
  colleges.value = response.data || [];
  if (scope.collegeId && !colleges.value.some((item) => item.id === scope.collegeId)) {
    scope.collegeId = null;
  }
}

async function loadConfig() {
  loading.value = true;
  try {
    const params = {
      campusId: scope.campusId ?? undefined,
      collegeId: scope.collegeId ?? undefined,
      stageId: scope.stageId ?? undefined
    };
    const [scheduleRes, featureRes] = await Promise.all([fetchScheduleConfig(params), fetchFeatureToggles(params)]);
    const schedulePayload = scheduleRes.data || {};
    applyScheduleData(schedulePayload.scheduleRule);
    featureToggles.value = (featureRes.data || []).map((item) => ({
      ...item,
      toggleValue: item.toggleValue || 'false',
      valueType: item.valueType || 'BOOLEAN',
      status: item.status ?? 1,
      remark: item.remark || ''
    }));
    timeSlots.value = normalizeTimeSlots(schedulePayload.timeSlots || []);
  } finally {
    loading.value = false;
  }
}

async function handleCampusChange() {
  await loadCollegeOptions();
  await loadConfig();
}

async function handleScopeReload() {
  await loadConfig();
}

async function handleScopeReset() {
  scope.campusId = null;
  scope.collegeId = null;
  scope.stageId = null;
  await loadCollegeOptions();
  await loadConfig();
}

async function saveRuleSection() {
  if (!scheduleValidation.value.isValid) {
    ElMessage.warning(scheduleValidation.value.messages[0]);
    return;
  }
  scheduleSaving.value = true;
  try {
    const response = await saveScheduleConfig({
      ...scheduleForm,
      ruleCode: scheduleForm.ruleCode || buildRuleCode(),
      ruleName: scheduleForm.ruleName || buildRuleName(),
      campusId: scope.campusId,
      collegeId: scope.collegeId,
      stageId: scope.stageId,
      termId: null
    });
    applyScheduleData(response.data || scheduleForm);
    ElMessage.success('排课规则保存成功');
  } finally {
    scheduleSaving.value = false;
  }
}

async function saveFeatureSection() {
  featureSaving.value = true;
  try {
    await saveFeatureToggles({
      items: featureToggles.value.map((item) => ({
        id: item.id,
        toggleCode: item.toggleCode,
        toggleName: item.toggleName,
        campusId: scope.campusId,
        collegeId: scope.collegeId,
        stageId: scope.stageId,
        termId: null,
        toggleValue: item.toggleValue,
        valueType: item.valueType || 'BOOLEAN',
        status: 1,
        remark: item.remark || ''
      }))
    });
    ElMessage.success('功能开关保存成功');
    await loadConfig();
  } finally {
    featureSaving.value = false;
  }
}

async function saveTimeSlotSection() {
  if (!scheduleForm.id) {
    await saveRuleSection();
  }
  if (!scheduleForm.id) {
    return;
  }
  if (!timeSlots.value.length) {
    ElMessage.warning('请先生成或新增至少一条时间片');
    return;
  }
  if (!timeSlotValidation.value.isValid) {
    ElMessage.warning(timeSlotValidation.value.messages[0]);
    return;
  }
  timeSlotSaving.value = true;
  try {
    await saveTimeSlots({
      scheduleRuleId: scheduleForm.id,
      items: timeSlots.value.map((item, index) => ({
        id: item.id,
        weekdayNo: item.weekdayNo,
        periodNo: item.periodNo,
        periodName: item.periodName,
        timeGroup: item.timeGroup,
        startTimeText: item.startTimeText,
        endTimeText: item.endTimeText,
        isTeaching: item.isTeaching,
        isFixedBreak: item.isFixedBreak,
        sortNo: index + 1,
        remark: item.remark || ''
      }))
    });
    ElMessage.success('时间片保存成功');
    await loadConfig();
  } finally {
    timeSlotSaving.value = false;
  }
}

function appendTimeSlot() {
  const lastItem = timeSlots.value[timeSlots.value.length - 1];
  const nextPeriod = lastItem ? lastItem.periodNo + 1 : 1;
  timeSlots.value.push({
    ...createEmptyTimeSlot(),
    periodNo: nextPeriod,
    periodName: `第${nextPeriod}节`,
    sortNo: timeSlots.value.length + 1
  });
}

function removeTimeSlot(index) {
  timeSlots.value.splice(index, 1);
}

function generateTemplate() {
  const totalPeriods = scheduleForm.dayPeriods || 0;
  const weekDays = scheduleForm.weekDays || 0;
  const rows = [];
  const morningStart = 8 * 60;
  const afternoonStart = 14 * 60;
  const nightStart = 19 * 60;

  for (let day = 1; day <= weekDays; day += 1) {
    for (let period = 1; period <= totalPeriods; period += 1) {
      const timeGroup = resolveTimeGroup(period);
      const startMinutes = calculateStartMinutes(period, timeGroup, morningStart, afternoonStart, nightStart);
      rows.push({
        id: null,
        weekdayNo: day,
        periodNo: period,
        periodName: `第${period}节`,
        timeGroup,
        startTimeText: formatMinutes(startMinutes),
        endTimeText: formatMinutes(startMinutes + 45),
        isTeaching: 1,
        isFixedBreak: 0,
        sortNo: rows.length + 1,
        remark: ''
      });
    }
  }
  timeSlots.value = rows;
  ElMessage.success('已按当前规则生成默认时间片模板');
}

function resolveTimeGroup(periodNo) {
  if (periodNo <= scheduleForm.morningPeriods) {
    return 'MORNING';
  }
  if (periodNo <= scheduleForm.morningPeriods + scheduleForm.afternoonPeriods) {
    return 'AFTERNOON';
  }
  return 'NIGHT';
}

function calculateStartMinutes(periodNo, timeGroup, morningStart, afternoonStart, nightStart) {
  if (timeGroup === 'MORNING') {
    return morningStart + (periodNo - 1) * 55;
  }
  if (timeGroup === 'AFTERNOON') {
    return afternoonStart + (periodNo - scheduleForm.morningPeriods - 1) * 55;
  }
  return nightStart + (periodNo - scheduleForm.morningPeriods - scheduleForm.afternoonPeriods - 1) * 55;
}

function formatMinutes(totalMinutes) {
  const hours = String(Math.floor(totalMinutes / 60)).padStart(2, '0');
  const minutes = String(totalMinutes % 60).padStart(2, '0');
  return `${hours}:${minutes}`;
}

onMounted(async () => {
  await loadScopeOptions();
  await loadConfig();
});
</script>

<style scoped>
.config-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-panel {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 26px 28px;
  overflow: hidden;
  border: 1px solid #dbe7f6;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgb(255 255 255 / 88%), transparent 38%),
    linear-gradient(135deg, #f6fbff 0%, #edf5ff 52%, #eef8f1 100%);
  box-shadow: 0 18px 46px rgb(28 71 123 / 10%);
}

.hero-panel::after {
  position: absolute;
  inset: auto -40px -58px auto;
  width: 220px;
  height: 220px;
  content: '';
  border-radius: 999px;
  background: radial-gradient(circle, rgb(70 136 255 / 18%) 0%, rgb(70 136 255 / 0%) 72%);
}

.eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  color: #5c6f89;
  text-transform: uppercase;
}

.hero-title {
  margin: 10px 0 8px;
  font-size: 34px;
  line-height: 1.05;
  color: #10233c;
}

.hero-description {
  max-width: 760px;
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #4b6079;
}

.hero-actions,
.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.scope-card,
.config-card {
  border: 1px solid #e5edf6;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 5%);
}

.scope-header,
.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.scope-title,
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #10233c;
}

.scope-caption,
.card-caption {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: #66788f;
}

.scope-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.config-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 18px;
}

.schedule-card {
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
}

.feature-card {
  background: linear-gradient(180deg, #fffdf9 0%, #fffbf1 100%);
}

.rule-form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.compact-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 6px 0 8px;
}

.metric-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border: 1px solid #dfebf6;
  border-radius: 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #f3f8ff 100%);
}

.metric-label {
  font-size: 13px;
  font-weight: 700;
  color: #41556f;
}

.switch-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 8px 0 18px;
}

.switch-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e5edf6;
  border-radius: 18px;
  background: #fff;
}

.switch-title {
  font-size: 13px;
  font-weight: 700;
  color: #10233c;
}

.switch-caption {
  margin-top: 5px;
  font-size: 12px;
  line-height: 1.5;
  color: #75859a;
}

.feature-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 15px 16px;
  border: 1px solid #f0e1c5;
  border-radius: 18px;
  background: rgb(255 255 255 / 78%);
}

.feature-name {
  font-size: 14px;
  font-weight: 700;
  color: #513d10;
}

.feature-code {
  margin-top: 4px;
  font-size: 12px;
  color: #8a6f39;
}

.time-card {
  background: linear-gradient(180deg, #fff 0%, #fcfcfe 100%);
}

.time-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #66788f;
}

.config-alert {
  margin-bottom: 16px;
  border-radius: 18px;
}

:deep(.primary-action.el-button) {
  min-width: 116px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #1c6cf5 0%, #0f9d7a 100%);
  box-shadow: 0 12px 24px rgb(28 108 245 / 24%);
}

:deep(.ghost-action.el-button) {
  border-radius: 999px;
  border-color: #d6e1ef;
  background: rgb(255 255 255 / 78%);
}

:deep(.soft-action.el-button) {
  border-radius: 999px;
  border-color: #cfe0f8;
  background: linear-gradient(180deg, #fff 0%, #f5f9ff 100%);
}

@media (max-width: 1200px) {
  .config-grid {
    grid-template-columns: 1fr;
  }

  .metrics-grid,
  .switch-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .hero-panel,
  .scope-header,
  .card-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .scope-grid,
  .form-grid,
  .compact-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 28px;
  }
}
</style>
