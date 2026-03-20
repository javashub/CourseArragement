<template>
  <div class="layout-wrapper" :class="{ 'sider-collapsed': isCollapsed }">
    <aside class="layout-sider">
      <div class="sider-glow"></div>
      <div class="brand">
        <div class="brand-kicker">Academic Control Ledger</div>
        <div class="brand-title">课程排课系统</div>
        <div class="brand-subtitle">排课、权限、组织与配置已经进入统一主链</div>
        <div class="brand-badge">RBAC3 + 多校区 + 多学段</div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="layout-menu"
        background-color="transparent"
        text-color="#d0d9e8"
        active-text-color="#ffffff"
        :collapse="isCollapsed"
        @select="handleMenuSelect"
      >
        <template v-for="menu in visibleMenus" :key="menu.id || menu.menuCode">
          <el-sub-menu
            v-if="menu.children && menu.children.length"
            :index="menu.routePath || menu.menuCode"
          >
            <template #title>
              <span
                class="menu-title"
                :class="{ 'menu-title--link': hasRegisteredRoute(menu.routePath) }"
                @click="handleCatalogClick(menu)"
              >
                {{ menu.menuName }}
              </span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id || child.menuCode"
              :index="child.routePath"
            >
              <span class="menu-leaf">{{ child.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.routePath">
            <span class="menu-leaf">{{ menu.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>
    <div class="layout-main">
      <header class="layout-header">
        <div class="header-left">
          <el-icon class="sidebar-toggle" @click="toggleSidebar">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
          <div class="header-ribbon">Course Arrange Rebuild</div>
        </div>
        <div class="header-user">
          <div class="header-name">{{ authStore.displayName }}</div>
          <div class="header-subtitle">
            {{ authStore.user?.userType || 'GUEST' }} · 已接入后端菜单与权限上下文
          </div>
        </div>
        <div class="header-actions">
          <div class="header-chip">Live Permission Context</div>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>
      <main class="layout-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';
import { Fold, Expand } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const isCollapsed = ref(false);

const activeMenu = computed(() => route.path);
const visibleMenus = computed(() => normalizeMenus(authStore.menus || []));

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value;
}

function handleLogout() {
  authStore.clearLoginState();
  router.push('/login');
}

function handleMenuSelect(index) {
  navigateToMenu(index);
}

function handleCatalogClick(menu) {
  if (!hasRegisteredRoute(menu?.routePath)) {
    return;
  }
  navigateToMenu(menu.routePath);
}

function hasRegisteredRoute(routePath) {
  if (!routePath) {
    return false;
  }
  return router.getRoutes().some((item) => item.path === routePath);
}

function navigateToMenu(routePath) {
  if (!routePath) {
    return;
  }
  if (!hasRegisteredRoute(routePath)) {
    ElMessage.warning(`菜单路由未注册：${routePath}`);
    return;
  }
  if (route.path === routePath) {
    return;
  }
  router.push(routePath).catch(() => {});
}

function normalizeMenus(menus) {
  const clonedMenus = JSON.parse(JSON.stringify(menus || []));
  const topLevelMenus = clonedMenus.filter((item) => item?.isHidden !== 1);
  const systemRoot = topLevelMenus.find((item) => item?.menuCode === 'system' || item?.routePath === '/system');

  if (!systemRoot) {
    const systemChildren = topLevelMenus.filter((item) => item?.routePath?.startsWith('/system/'));
    if (systemChildren.length) {
      const systemChildKeys = new Set(systemChildren.map((item) => item.id || item.menuCode));
      const remainingMenus = topLevelMenus.filter((item) => !systemChildKeys.has(item.id || item.menuCode));
      remainingMenus.push({
        id: 'system-catalog',
        menuCode: 'system',
        parentId: 0,
        menuName: '系统管理',
        menuType: 'CATALOG',
        routeName: 'SystemManage',
        routePath: '/system',
        icon: 'Setting',
        sortNo: 900,
        children: systemChildren.sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
      });
      return remainingMenus.filter(shouldKeepMenu).sort(sortMenus);
    }
  }

  return topLevelMenus
    .map((item) => ({
      ...item,
      children: (item.children || []).filter(shouldKeepMenu).sort(sortMenus)
    }))
    .filter(shouldKeepMenu)
    .sort(sortMenus);
}

function shouldKeepMenu(menu) {
  if (!menu || menu.isHidden === 1) {
    return false;
  }
  return Boolean(menu.routePath || (menu.children && menu.children.length));
}

function sortMenus(a, b) {
  return (a?.sortNo || 0) - (b?.sortNo || 0);
}
</script>

<style scoped>
.layout-wrapper {
  display: flex;
  min-height: 100vh;
}

.layout-sider {
  position: relative;
  width: 240px;
  background:
    radial-gradient(circle at top, rgb(232 211 181 / 0.14), transparent 32%),
    linear-gradient(180deg, #102636 0%, #17384d 48%, #1d3143 100%);
  color: #fff;
  box-shadow: 24px 0 50px rgb(16 24 40 / 0.18);
  transition: width 0.3s ease;
}

.sider-collapsed .layout-sider {
  width: 64px;
}

.sider-glow {
  position: absolute;
  inset: 18px 18px auto;
  height: 120px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgb(184 135 70 / 0.32), rgb(184 135 70 / 0));
  filter: blur(12px);
  pointer-events: none;
}

.brand {
  position: relative;
  padding: 34px 24px 24px;
  white-space: nowrap;
  overflow: hidden;
}

.brand-kicker {
  color: rgb(232 211 181 / 0.84);
  font-size: 11px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.brand-title {
  margin-top: 10px;
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.06em;
  line-height: 1.1;
}

.brand-subtitle {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.65;
  color: rgb(214 224 232 / 0.8);
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  margin-top: 16px;
  padding: 8px 12px;
  border: 1px solid rgb(232 211 181 / 0.18);
  border-radius: 999px;
  color: rgb(244 230 205 / 0.88);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  background: rgb(255 255 255 / 0.04);
}

.layout-menu {
  padding: 8px 12px 18px;
  border-right: none;
  background: transparent;
}

.layout-menu:not(.el-menu--collapse) {
  width: 240px;
}

.layout-menu :deep(.el-menu) {
  border-right: none;
}

.layout-menu :deep(.el-menu-item),
.layout-menu :deep(.el-sub-menu__title) {
  height: 46px;
  margin-bottom: 6px;
  border-radius: 14px;
}

.layout-menu :deep(.el-menu-item.is-active),
.layout-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(90deg, rgb(184 135 70 / 0.26), rgb(184 135 70 / 0.08)) !important;
  box-shadow: inset 0 0 0 1px rgb(232 211 181 / 0.14);
}

.layout-menu :deep(.el-menu-item:hover),
.layout-menu :deep(.el-sub-menu__title:hover) {
  background: rgb(255 255 255 / 0.06) !important;
}

.menu-title {
  font-weight: 700;
  letter-spacing: 0.04em;
}

.menu-title--link {
  cursor: pointer;
}

.menu-leaf {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding-left: 16px;
  letter-spacing: 0.03em;
}

.menu-leaf::before {
  position: absolute;
  left: 0;
  width: 7px;
  height: 7px;
  content: '';
  border-radius: 999px;
  background: rgb(232 211 181 / 0.8);
  box-shadow: 0 0 0 4px rgb(232 211 181 / 0.08);
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; /* Prevents overflow from flex items */
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 10;
  height: var(--app-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 0 28px;
  background: linear-gradient(180deg, rgb(255 251 245 / 0.9), rgb(248 241 230 / 0.7));
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgb(116 91 61 / 0.12);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-toggle {
  font-size: 20px;
  color: #72665b;
  cursor: pointer;
}

.header-ribbon {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border: 1px solid rgb(184 135 70 / 0.18);
  border-radius: 999px;
  color: #805a25;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  background: rgb(255 255 255 / 0.42);
}

.header-user {
  display: flex;
  flex-direction: column;
  flex: 1;
  text-align: right;
}

.header-name {
  color: var(--app-primary-strong);
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: 24px;
  font-weight: 700;
}

.header-subtitle {
  color: #72665b;
  font-size: 13px;
  letter-spacing: 0.03em;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-chip {
  padding: 9px 14px;
  border-radius: 999px;
  background: rgb(31 77 107 / 0.08);
  color: var(--app-primary);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.layout-content {
  flex: 1;
  padding: 28px;
  background:
    radial-gradient(circle at top right, rgb(184 135 70 / 0.12), transparent 20%),
    linear-gradient(180deg, rgb(250 246 238 / 0.78) 0%, rgb(241 234 221 / 0.56) 100%);
  overflow-y: auto;
}

@media (max-width: 1024px) {
  .layout-wrapper {
    flex-direction: column;
  }

  .layout-sider {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .layout-header {
    flex-wrap: wrap;
    height: auto;
    padding: 16px 18px;
  }

  .layout-content {
    padding: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
