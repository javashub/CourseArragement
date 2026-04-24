<template>
  <section class="rbac-container">
    <div class="rbac-header">
      <h1 class="page-title">权限管理</h1>
      <p class="page-desc">RBAC3 权限模型：用户、角色、菜单/权限的资源授权闭环。</p>
    </div>

    <div class="rbac-layout">
      <!-- 左侧菜单 -->
      <aside class="rbac-sidebar">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="rbac-tab"
          :class="{ 'rbac-tab--active': activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <el-icon class="rbac-tab-icon"><component :is="tab.icon" /></el-icon>
          <span class="rbac-tab-label">{{ tab.label }}</span>
        </div>
      </aside>

      <!-- 右侧内容 -->
      <main class="rbac-content">
        <UserManageView v-if="activeTab === 'user'" />
        <RoleManageView v-else-if="activeTab === 'role'" />
        <PermissionManageView v-else-if="activeTab === 'permission'" />
      </main>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { User, Avatar, Lock } from '@element-plus/icons-vue';
import UserManageView from './UserManageView.vue';
import RoleManageView from './RoleManageView.vue';
import PermissionManageView from './PermissionManageView.vue';

const activeTab = ref('user');

const tabs = [
  { key: 'user', label: '用户管理', icon: User },
  { key: 'role', label: '角色管理', icon: Avatar },
  { key: 'permission', label: '权限管理', icon: Lock }
];
</script>

<style scoped>
.rbac-container {
  padding: 24px;
  background: #f1f5f9;
  min-height: 100%;
}

.rbac-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px;
}

.page-desc {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}

.rbac-layout {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  min-height: 600px;
  overflow: hidden;
}

.rbac-sidebar {
  width: 180px;
  flex-shrink: 0;
  background: #f8fafc;
  border-right: 1px solid #e5e7eb;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rbac-tab {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #64748b;
  transition: all 0.15s;
  user-select: none;
}

.rbac-tab:hover {
  background: #e2e8f0;
  color: #334155;
}

.rbac-tab--active {
  background: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}

.rbac-tab--active .rbac-tab-icon {
  color: #2563eb;
}

.rbac-tab-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.rbac-content {
  flex: 1;
  min-width: 0;
  padding: 24px;
}
</style>
