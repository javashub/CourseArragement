<template>
  <div class="layout-wrapper">
    <aside class="layout-sider">
      <div class="brand">课程排课系统</div>
      <el-menu :default-active="activeMenu" router class="layout-menu">
        <el-menu-item index="/dashboard">工作台</el-menu-item>
        <el-menu-item index="/course-plan">排课任务</el-menu-item>
        <el-menu-item index="/schedule">课表管理</el-menu-item>
        <el-menu-item index="/base-data">基础数据</el-menu-item>
        <el-menu-item index="/guide">重构说明</el-menu-item>
      </el-menu>
    </aside>
    <div class="layout-main">
      <header class="layout-header">
        <div>
          <strong>{{ authStore.user?.realname || '未登录用户' }}</strong>
          <span class="header-subtitle">前后端规范化重构中</span>
        </div>
        <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
      </header>
      <main class="layout-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeMenu = computed(() => route.path);

function handleLogout() {
  authStore.clearLoginState();
  router.push('/login');
}
</script>

<style scoped>
.layout-wrapper {
  display: flex;
  min-height: 100vh;
}

.layout-sider {
  width: 240px;
  background: #001529;
  color: #fff;
}

.brand {
  padding: 24px;
  font-size: 18px;
  font-weight: 700;
}

.layout-menu {
  border-right: none;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.layout-header {
  height: var(--app-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--app-border-color);
}

.header-subtitle {
  margin-left: 12px;
  color: #98a2b3;
  font-size: 14px;
}

.layout-content {
  flex: 1;
  padding: 24px;
}
</style>
