<template>
  <div class="layout-wrapper">
    <aside class="layout-sider">
      <div class="brand">
        <div class="brand-title">课程排课系统</div>
        <div class="brand-subtitle">RBAC3 + 多校区 + 多学段</div>
      </div>
      <el-menu :default-active="activeMenu" router class="layout-menu" background-color="transparent" text-color="#d0d9e8" active-text-color="#ffffff">
        <template v-for="menu in visibleMenus" :key="menu.id || menu.menuCode">
          <el-sub-menu
            v-if="menu.children && menu.children.length"
            :index="menu.routePath || menu.menuCode"
          >
            <template #title>{{ menu.menuName }}</template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id || child.menuCode"
              :index="child.routePath"
            >
              {{ child.menuName }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.routePath">
            {{ menu.menuName }}
          </el-menu-item>
        </template>
      </el-menu>
    </aside>
    <div class="layout-main">
      <header class="layout-header">
        <div class="header-user">
          <div class="header-name">{{ authStore.displayName }}</div>
          <div class="header-subtitle">
            {{ authStore.user?.userType || 'GUEST' }} · 已接入后端菜单与权限上下文
          </div>
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
const visibleMenus = computed(() =>
  (authStore.menus || []).filter((item) => item?.isHidden !== 1 && item?.routePath)
);

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
  background:
    radial-gradient(circle at top, rgb(54 84 135 / 45%), transparent 35%),
    linear-gradient(180deg, #0f172a 0%, #132238 56%, #182c49 100%);
  color: #fff;
}

.brand {
  padding: 28px 24px 20px;
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.brand-subtitle {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.layout-menu {
  border-right: none;
  background: transparent;
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
  padding: 0 28px;
  background: rgb(255 255 255 / 88%);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgb(226 232 240 / 88%);
}

.header-user {
  display: flex;
  flex-direction: column;
}

.header-name {
  font-size: 18px;
  font-weight: 700;
}

.header-subtitle {
  color: #98a2b3;
  font-size: 13px;
}

.layout-content {
  flex: 1;
  padding: 28px;
  background:
    linear-gradient(180deg, rgb(247 250 252 / 92%) 0%, #eef4ff 100%);
}
</style>
