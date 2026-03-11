<template>
  <section class="page-card" v-loading="loading">
    <h1 class="page-title">学院管理</h1>
    <p class="page-description">当前先接通学院查询接口，后续补学院与校区绑定、跨学院授课和学段关联能力。</p>
    <el-table :data="colleges" stripe>
      <el-table-column prop="collegeCode" label="学院编码" min-width="140" />
      <el-table-column prop="collegeName" label="学院名称" min-width="180" />
      <el-table-column prop="campusId" label="校区ID" width="100" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { fetchCollegeList } from '@/api/modules/system';

const loading = ref(false);
const colleges = ref([]);

async function loadColleges() {
  loading.value = true;
  try {
    const response = await fetchCollegeList();
    colleges.value = response.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadColleges();
});
</script>
