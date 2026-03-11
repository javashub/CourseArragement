<template>
  <section class="page-card">
    <h1 class="page-title">工作台</h1>
    <p class="page-description">当前阶段先完成工程与架构规范化，后续再逐步迁移旧业务页面。</p>
    <el-row :gutter="16" class="panel-list">
      <el-col :span="8" v-for="item in panels" :key="item.title">
        <el-card shadow="hover">
          <template #header>{{ item.title }}</template>
          <div>{{ item.description }}</div>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
const panels = [
  { title: '前端栈', description: 'Vue 3 + Vite + Pinia + Element Plus' },
  { title: '后端栈', description: 'Spring Boot + MyBatis-Plus + Sa-Token' },
  { title: '重构策略', description: '先规范底座，再逐模块迁移旧功能' }
];
</script>

<style scoped>
.panel-list {
  margin-top: 24px;
}
</style>
