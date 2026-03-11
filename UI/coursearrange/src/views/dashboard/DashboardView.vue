<template>
  <section class="page-card">
    <h1 class="page-title">工作台</h1>
    <p class="page-description">当前页面已经接入真实登录上下文、菜单权限与系统配置接口。</p>
    <el-row :gutter="16" class="panel-list">
      <el-col :span="8">
        <el-card shadow="hover" class="overview-card">
          <template #header>当前账号</template>
          <div class="overview-value">{{ authStore.displayName }}</div>
          <div class="overview-text">{{ authStore.user?.username || '--' }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="overview-card">
          <template #header>角色数量</template>
          <div class="overview-value">{{ authStore.user?.roles?.length || 0 }}</div>
          <div class="overview-text">{{ (authStore.user?.roles || []).join(' / ') || '未分配角色' }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="overview-card">
          <template #header>权限数量</template>
          <div class="overview-value">{{ authStore.permissions.length }}</div>
          <div class="overview-text">菜单与按钮权限由后端统一下发</div>
        </el-card>
      </el-col>
    </el-row>
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
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const panels = [
  { title: '前端栈', description: 'Vue 3 + Vite + Pinia + Element Plus + 路由守卫' },
  { title: '后端栈', description: 'Spring Boot + MyBatis-Plus + Sa-Token + RBAC3' },
  { title: '当前策略', description: '先打通新认证与组织配置闭环，再逐模块迁移排课业务' }
];
</script>

<style scoped>
.panel-list {
  margin-top: 24px;
}

.overview-card {
  background: linear-gradient(180deg, #f8fbff 0%, #eef5ff 100%);
}

.overview-value {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.overview-text {
  margin-top: 8px;
  color: #667085;
}
</style>
