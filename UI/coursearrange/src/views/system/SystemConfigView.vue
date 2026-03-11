<template>
  <section class="page-card" v-loading="loading">
    <h1 class="page-title">系统配置</h1>
    <p class="page-description">这部分已接通功能开关和排课规则接口，后续会扩展为管理员可视化配置页。</p>
    <el-row :gutter="16" class="config-list">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>排课规则</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="规则名称">{{ scheduleConfig?.ruleName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="每周上课天数">{{ scheduleConfig?.weekDays ?? '--' }}</el-descriptions-item>
            <el-descriptions-item label="每日节次数">{{ scheduleConfig?.dayPeriods ?? '--' }}</el-descriptions-item>
            <el-descriptions-item label="默认连堂上限">{{ scheduleConfig?.defaultContinuousLimit ?? '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>功能开关</template>
          <el-table :data="featureToggles" stripe>
            <el-table-column prop="featureCode" label="开关编码" min-width="160" />
            <el-table-column prop="featureName" label="开关名称" min-width="180" />
            <el-table-column prop="featureValue" label="当前值" width="100" />
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { fetchFeatureToggles, fetchScheduleConfig } from '@/api/modules/system';

const loading = ref(false);
const scheduleConfig = ref(null);
const featureToggles = ref([]);

async function loadConfig() {
  loading.value = true;
  try {
    const [scheduleRes, featureRes] = await Promise.all([fetchScheduleConfig(), fetchFeatureToggles()]);
    scheduleConfig.value = scheduleRes.data || null;
    featureToggles.value = featureRes.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadConfig();
});
</script>

<style scoped>
.config-list {
  margin-top: 24px;
}
</style>
