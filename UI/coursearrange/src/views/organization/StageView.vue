<template>
  <section class="page-card" v-loading="loading">
    <h1 class="page-title">学段管理</h1>
    <p class="page-description">小学、初中、高中、大学等学段配置将统一在这里维护，后续补学段规则差异化能力。</p>
    <el-table :data="stages" stripe>
      <el-table-column prop="stageCode" label="学段编码" min-width="140" />
      <el-table-column prop="stageName" label="学段名称" min-width="160" />
      <el-table-column prop="stageLevel" label="层级" width="90" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { fetchStageList } from '@/api/modules/system';

const loading = ref(false);
const stages = ref([]);

async function loadStages() {
  loading.value = true;
  try {
    const response = await fetchStageList();
    stages.value = response.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadStages();
});
</script>
