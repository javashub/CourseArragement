<template>
  <div
    class="layout-root"
    :class="[
      `layout-root--${appStore.theme}`,
      { 'sider-collapsed': isMenuCollapsed && !appStore.isTopNav }
    ]"
  >
    <TopNav v-if="appStore.isTopNav" />

    <template v-else>
      <aside class="layout-sider">
        <div class="brand">
          <span class="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="17" rx="3" />
              <path d="M3 9h18M8 2v4M16 2v4" />
            </svg>
          </span>
          <span v-if="!isMenuCollapsed" class="brand-text">
            <span class="brand-name">课程排课系统</span>
            <span class="brand-sub">Course Arrange · v2</span>
          </span>
        </div>

        <el-menu
          :key="menuRenderKey"
          :default-active="activeMenu"
          :default-openeds="openMenuIndexes"
          :collapse="isMenuCollapsed"
          :collapse-transition="false"
          class="layout-menu"
        >
          <template v-for="menu in visibleMenus" :key="menu.id || menu.menuCode">
            <el-sub-menu
              v-if="menu.children && menu.children.length"
              :index="menu.routePath || menu.menuCode"
            >
              <template #title>
                <el-icon class="menu-icon">
                  <component :is="resolveMenuIcon(menu.icon)" />
                </el-icon>
                <span class="menu-title">{{ menu.menuName }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.id || child.menuCode"
                :index="child.routePath"
                @click="navigateToMenu(child.routePath)"
              >
                <span class="menu-leaf">{{ child.menuName }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item
              v-else
              :index="menu.routePath"
              @click="navigateToMenu(menu.routePath)"
            >
              <el-icon class="menu-icon">
                <component :is="resolveMenuIcon(menu.icon)" />
              </el-icon>
              <span class="menu-leaf">{{ menu.menuName }}</span>
            </el-menu-item>
          </template>
        </el-menu>

        <div class="sider-foot" :class="{ 'sider-foot--collapsed': isMenuCollapsed }">
          <div v-if="!isMenuCollapsed" class="sider-foot__status">
            <span>排课引擎</span>
            <b>GA · 运行中</b>
          </div>
          <button
            type="button"
            class="sider-toggle"
            :aria-label="isMenuCollapsed ? '展开侧栏' : '收起侧栏'"
            :title="isMenuCollapsed ? '展开侧栏' : '收起侧栏'"
            @click="toggleSidebar"
          >
            <el-icon><Expand v-if="isMenuCollapsed" /><Fold v-else /></el-icon>
          </button>
        </div>
      </aside>

      <header class="layout-topbar">
        <nav class="layout-crumbs" aria-label="面包屑">
          <template v-for="(item, index) in breadcrumbTrail" :key="item.id || item.menuCode || index">
            <span v-if="index" class="crumbs-sep">/</span>
            <b v-if="index === breadcrumbTrail.length - 1">{{ item.menuName }}</b>
            <span v-else>{{ item.menuName }}</span>
          </template>
        </nav>

        <div class="layout-tools">
          <span class="tool-divider" aria-hidden="true" />
          <ThemeSwitcher />
          <AccountMenu />
        </div>
      </header>
    </template>

    <TabBar
      :tabs="tabs"
      :active-path="route.path"
      @select="handleTabSelect"
      @refresh="handleTabRefresh"
      @close="handleTabClose"
    />

    <main class="layout-content">
      <router-view v-slot="{ Component, route: currentRoute }">
        <keep-alive :max="50">
          <component :is="Component" :key="buildTabViewKey(currentRoute, activeTab)" />
        </keep-alive>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Calendar,
  Collection,
  CollectionTag,
  Document,
  Expand,
  Fold,
  House,
  Lock,
  Menu as MenuIcon,
  OfficeBuilding,
  Reading,
  School,
  Setting,
  Tickets,
  User,
  UserFilled
} from '@element-plus/icons-vue';
import AccountMenu from '@/components/AccountMenu.vue';
import ThemeSwitcher from '@/components/ThemeSwitcher.vue';
import TabBar from '@/layouts/TabBar.vue';
import TopNav from '@/layouts/TopNav.vue';
import { useAppStore } from '@/stores/app';
import { useAuthStore } from '@/stores/auth';
import {
  findMenuTrailByPath,
  normalizeMenus,
  resolveActiveMenuPath,
  resolveOpenMenuIndexes
} from './layoutNavigation';
import { buildTabViewKey, refreshTab, removeTab, resolveAdjacentTab, upsertTab } from './tabsNavigation.js';

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const authStore = useAuthStore();

const isMenuCollapsed = ref(false);
const tabs = ref([]);
// 记录刚刚被关闭且尚未离开的路由，避免关闭后残留的路由变化把标签重新加回。
let closedTabPath = '';
const activeTab = computed(() => tabs.value.find((tab) => tab.path === route.path) || null);

// 路由变化时合并标签：同一路径更新而不追加，保证标签顺序与首次打开一致。
watch(
  () => route.fullPath,
  () => {
    if (route.path === closedTabPath) {
      return;
    }
    closedTabPath = '';
    tabs.value = upsertTab(tabs.value, route);
  },
  { immediate: true }
);

// 菜单图标来自后端 sys_menu.icon，这里只登记可能用到的图标，避免整包引入。
const MENU_ICONS = {
  Calendar,
  Collection,
  CollectionTag,
  Document,
  House,
  Lock,
  OfficeBuilding,
  Reading,
  School,
  Setting,
  Tickets,
  User,
  UserFilled
};

function resolveMenuIcon(iconName) {
  return MENU_ICONS[iconName] || MenuIcon;
}

const visibleMenus = computed(() => normalizeMenus(authStore.menus || []));
const activeMenu = computed(() => resolveActiveMenuPath(route.path, visibleMenus.value));
const openMenuIndexes = computed(() => resolveOpenMenuIndexes(route.path, visibleMenus.value));
const menuRenderKey = computed(() => `${activeMenu.value}::${openMenuIndexes.value.join('|')}`);
const breadcrumbTrail = computed(() => {
  const trail = findMenuTrailByPath(route.path, visibleMenus.value);
  if (trail.length) {
    return trail;
  }
  return [{ menuCode: `route-${route.path}`, menuName: route.meta?.title || '页面' }];
});

function toggleSidebar() {
  isMenuCollapsed.value = !isMenuCollapsed.value;
}

function handleTabSelect(tab) {
  if (!tab?.path || route.path === tab.path) {
    return;
  }
  router.push(tab.fullPath || tab.path).catch(() => {});
}

function handleTabRefresh(tab) {
  if (!tab?.path) {
    return;
  }
  // 刷新只递增该标签的 version；keep-alive 的 key 变化后会生成新的页面实例。
  tabs.value = refreshTab(tabs.value, tab.path);
  if (route.path !== tab.path) {
    router.push(tab.fullPath || tab.path).catch(() => {});
  }
}

function handleTabClose(tab) {
  if (!tab?.path) {
    return;
  }
  const isActive = route.path === tab.path;
  const nextTab = resolveAdjacentTab(tabs.value, tab.path);
  // 关闭后下次路由变化不得把被关闭的标签重新加回来。
  if (isActive) {
    closedTabPath = tab.path;
  }
  tabs.value = removeTab(tabs.value, tab.path);
  if (isActive) {
    if (nextTab) {
      router.push(nextTab.fullPath || nextTab.path).catch(() => {});
      return;
    }
    // 最后一个标签关闭后回到该角色的首个可访问页面；标签栏重新显示该页面。
    const fallbackPath = authStore.firstAccessiblePath || route.path;
    const fallbackRoute = router.resolve(fallbackPath);
    closedTabPath = '';
    tabs.value = upsertTab(tabs.value, fallbackRoute);
    router.push(fallbackPath).catch(() => {});
    return;
  }
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
</script>

<style scoped>
.layout-root {
  display: grid;
  grid-template-areas:
    'sider topbar'
    'sider tabs'
    'sider content';
  grid-template-columns: 232px minmax(0, 1fr);
  grid-template-rows: var(--app-header-height) 38px minmax(0, 1fr);
  height: 100vh;
  overflow: hidden;
  background: var(--bg);
  color: var(--ink);
}

.layout-root--top-nav {
  grid-template-areas:
    'topbar'
    'tabs'
    'content';
  grid-template-columns: minmax(0, 1fr);
  grid-template-rows: 52px 38px minmax(0, 1fr);
}

.layout-root:not(.layout-root--top-nav).sider-collapsed {
  grid-template-columns: 64px minmax(0, 1fr);
}

.layout-sider {
  display: flex;
  flex-direction: column;
  grid-area: sider;
  min-height: 0;
  overflow: hidden;
  border-right: 1px solid var(--sider-line);
  background: var(--sider-bg);
  box-shadow: var(--sider-shadow);
  color: var(--sider-ink);
}

.brand {
  display: flex;
  flex: 0 0 56px;
  align-items: center;
  gap: 9px;
  height: 56px;
  padding: 0 14px;
  border-bottom: 1px solid var(--sider-line);
  white-space: nowrap;
  overflow: hidden;
}

.brand-mark {
  display: grid;
  flex: 0 0 26px;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: var(--grad);
  box-shadow: 0 5px 12px rgb(22 93 255 / 28%);
  color: #fff;
}

.brand-mark svg {
  width: 15px;
  height: 15px;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.brand-name {
  color: var(--sider-title);
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.brand-sub {
  color: var(--sider-label);
  font-size: 10px;
  letter-spacing: 0.02em;
}

.layout-menu {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--sider-ink);
  --el-menu-active-color: var(--sider-active-ink);
  --el-menu-hover-bg-color: var(--sider-hover);
  --el-menu-item-height: 34px;
  --el-menu-sub-item-height: 34px;

  flex: 1;
  min-height: 0;
  padding: 8px 10px 12px;
  overflow-y: auto;
  border-right: none;
  background: transparent;
}

.layout-menu:not(.el-menu--collapse) {
  width: 100%;
}

.layout-menu.el-menu--collapse {
  width: 100%;
}

.layout-menu :deep(.el-menu-item),
.layout-menu :deep(.el-sub-menu__title) {
  position: relative;
  height: 34px;
  margin-bottom: 2px;
  padding-right: 10px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 34px;
}

.layout-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  padding-left: 30px !important;
}

.layout-menu :deep(.el-menu-item.is-active),
.layout-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: var(--sider-active-bg) !important;
  color: var(--sider-active-ink) !important;
  font-weight: 600;
}

.layout-menu :deep(.el-menu-item.is-active)::before {
  position: absolute;
  top: 8px;
  bottom: 8px;
  left: 0;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--sider-active-bar);
  content: '';
}

.layout-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--sider-active-ink);
}

.layout-menu :deep(.el-menu--inline) {
  background: transparent;
}

.menu-icon {
  flex: 0 0 16px;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  opacity: 0.78;
}

.layout-menu :deep(.el-menu-item.is-active) .menu-icon {
  opacity: 1;
}

.menu-title {
  font-weight: 600;
}

.menu-leaf {
  font-size: 13px;
}

.sider-foot {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px 10px;
  border-top: 1px solid var(--sider-line);
  color: var(--sider-label);
  font-size: 10.5px;
}

.sider-foot--collapsed {
  justify-content: center;
  padding: 8px 0 10px;
}

.sider-foot__status {
  display: flex;
  flex: 1;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.sider-foot__status b {
  color: var(--sider-title);
  font-weight: 600;
}

.sider-toggle {
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  width: 28px;
  height: 28px;
  border: 1px solid transparent;
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--sider-ink);
  cursor: pointer;
}

.sider-toggle:hover {
  border-color: var(--sider-line);
  background: var(--sider-hover);
  color: var(--sider-title);
}

.layout-topbar {
  display: flex;
  grid-area: topbar;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
  height: var(--app-header-height);
  padding: 0 20px;
  border-bottom: 1px solid var(--line);
  background: rgb(255 255 255 / 88%);
  backdrop-filter: blur(10px);
}

.layout-crumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  color: var(--muted-2);
  font-size: 12.5px;
  white-space: nowrap;
  overflow: hidden;
}

.layout-crumbs b {
  color: var(--ink);
  font-weight: 500;
}

.crumbs-sep {
  color: var(--muted-3);
}

.layout-tools {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.tool-divider {
  width: 1px;
  height: 20px;
  background: var(--line);
}

.layout-content {
  grid-area: content;
  min-width: 0;
  min-height: 0;
  padding: 14px 20px 18px;
  overflow-x: hidden;
  overflow-y: auto;
  background: var(--bg);
}

.layout-root--top-nav .layout-content {
  padding: 14px 22px 18px;
}

@media (max-width: 1100px) {
  .layout-tools {
    gap: 8px;
  }
}

@media (max-width: 900px) {
  .layout-root:not(.layout-root--top-nav) {
    grid-template-columns: 188px minmax(0, 1fr);
  }

  .layout-root:not(.layout-root--top-nav).sider-collapsed {
    grid-template-columns: 64px minmax(0, 1fr);
  }

  .brand {
    padding: 0 12px;
  }

  .brand-sub {
    display: none;
  }

  .layout-crumbs {
    display: none;
  }
}

@media (max-width: 768px) {
  .layout-topbar {
    padding: 0 12px;
  }

  .layout-content,
  .layout-root--top-nav .layout-content {
    padding: 12px;
  }
}
</style>
