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
    <el-card shadow="never" class="quick-action-card" v-if="canOpenRbac">
      <div class="quick-action-content">
        <div>
          <div class="quick-action-title">权限管理联调入口</div>
          <div class="quick-action-text">当前已接通用户、角色、菜单、权限分配接口，可以直接进入 RBAC 管理页测试。</div>
        </div>
        <el-button type="primary" @click="router.push('/system/rbac')">进入权限管理</el-button>
      </div>
    </el-card>
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
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const canOpenRbac = computed(() => authStore.hasPermission('page:rbac:view'));

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

.quick-action-card {
  margin-top: 24px;
  border: 1px solid #dbe7ff;
  background: linear-gradient(90deg, #f7fbff 0%, #eef4ff 100%);
}

.quick-action-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.quick-action-title {
  font-size: 18px;
  font-weight: 700;
}

.quick-action-text {
  margin-top: 8px;
  color: #667085;
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
