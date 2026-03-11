<template>
  <section class="page-card" v-loading="loading">
    <h1 class="page-title">校区管理</h1>
    <p class="page-description">当前先接通校区查询接口，后续补新增、编辑、资源归属和跨校区排课规则。</p>
    <el-table :data="campuses" stripe>
      <el-table-column prop="campusCode" label="校区编码" min-width="140" />
      <el-table-column prop="campusName" label="校区名称" min-width="160" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column prop="sortNo" label="排序" width="90" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { fetchCampusList } from '@/api/modules/system';

const loading = ref(false);
const campuses = ref([]);

async function loadCampuses() {
  loading.value = true;
  try {
    const response = await fetchCampusList();
    campuses.value = response.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadCampuses();
});
</script>
