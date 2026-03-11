<template>
  <section class="page-card">
    <h1 class="page-title">基础数据</h1>
    <p class="page-description">这里先展示新架构下已经接通的组织、配置与字典数据概况。</p>
    <el-row :gutter="16" class="summary-list" v-loading="loading">
      <el-col :span="8" v-for="item in summaryCards" :key="item.title">
        <el-card shadow="hover">
          <template #header>{{ item.title }}</template>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-text">{{ item.description }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="table-list">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>校区列表</template>
          <el-table :data="campuses" size="small" max-height="280">
            <el-table-column prop="campusCode" label="编码" min-width="120" />
            <el-table-column prop="campusName" label="名称" min-width="140" />
            <el-table-column prop="status" label="状态" width="80" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>学院列表</template>
          <el-table :data="colleges" size="small" max-height="280">
            <el-table-column prop="collegeCode" label="编码" min-width="120" />
            <el-table-column prop="collegeName" label="名称" min-width="140" />
            <el-table-column prop="campusId" label="校区ID" width="90" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import {
  fetchCampusList,
  fetchCollegeList,
  fetchDictTypes,
  fetchFeatureToggles,
  fetchScheduleConfig,
  fetchStageList
} from '@/api/modules/system';

const loading = ref(false);
const campuses = ref([]);
const colleges = ref([]);
const stages = ref([]);
const featureToggles = ref([]);
const dictTypes = ref([]);
const scheduleRule = ref(null);

const summaryCards = computed(() => [
  {
    title: '校区数量',
    value: campuses.value.length,
    description: '支持多校区配置与独立资源范围'
  },
  {
    title: '学院数量',
    value: colleges.value.length,
    description: '学院可继续承接大学多学院场景'
  },
  {
    title: '功能开关',
    value: featureToggles.value.length,
    description: '走班制、跨校区、跨学院等开关统一收口'
  },
  {
    title: '学段数量',
    value: stages.value.length,
    description: '当前已接入学段基础配置'
  },
  {
    title: '字典类型',
    value: dictTypes.value.length,
    description: '下拉项与标签值由字典接口统一提供'
  },
  {
    title: '排课规则',
    value: scheduleRule.value?.ruleName || '--',
    description: '每周天数、每天节次和连堂限制已可读取'
  }
]);

async function loadBaseDataOverview() {
  loading.value = true;
  try {
    const [campusRes, collegeRes, stageRes, featureRes, dictRes, scheduleRes] = await Promise.all([
      fetchCampusList(),
      fetchCollegeList(),
      fetchStageList(),
      fetchFeatureToggles(),
      fetchDictTypes(),
      fetchScheduleConfig()
    ]);
    campuses.value = campusRes.data || [];
    colleges.value = collegeRes.data || [];
    stages.value = stageRes.data || [];
    featureToggles.value = featureRes.data || [];
    dictTypes.value = dictRes.data || [];
    scheduleRule.value = scheduleRes.data || null;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadBaseDataOverview();
});
</script>

<style scoped>
.summary-list {
  margin-top: 24px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.summary-text {
  margin-top: 8px;
  color: #667085;
}

.table-list {
  margin-top: 16px;
}
</style>
