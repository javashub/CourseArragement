<template>
  <header class="topnav">
    <div class="topnav__brand">
      <span class="topnav__mark" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="4" width="18" height="17" rx="3" />
          <path d="M3 9h18M8 2v4M16 2v4" />
        </svg>
      </span>
      <span class="topnav__brand-text">
        <b>课程排课系统</b>
        <small>Course Arrange · v2</small>
      </span>
    </div>

    <nav class="topnav__nav" aria-label="一级导航">
      <template v-for="menu in visibleMenus" :key="menu.id || menu.menuCode">
        <el-dropdown
          v-if="menu.children && menu.children.length"
          trigger="hover"
          placement="bottom-start"
          @command="navigateToMenu"
        >
          <button
            type="button"
            class="topnav__item"
            :class="{ 'is-active': isMenuActive(route.path, menu.routePath) }"
          >
            {{ menu.menuName }}
            <el-icon class="topnav__chev"><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="child in menu.children"
                :key="child.id || child.menuCode"
                :command="child.routePath"
              >
                {{ child.menuName }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <button
          v-else
          type="button"
          class="topnav__item"
          :class="{ 'is-active': isMenuActive(route.path, menu.routePath) }"
          @click="navigateToMenu(menu.routePath)"
        >
          {{ menu.menuName }}
        </button>
      </template>
    </nav>

    <div class="topnav__right">
      <span class="topnav__divider" aria-hidden="true" />
      <ThemeSwitcher />
      <AccountMenu />
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import AccountMenu from '@/components/AccountMenu.vue';
import ThemeSwitcher from '@/components/ThemeSwitcher.vue';
import { useAuthStore } from '@/stores/auth';
import { isMenuActive, normalizeMenus } from './layoutNavigation';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const visibleMenus = computed(() => normalizeMenus(authStore.menus || []));

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
.topnav {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 52px;
  padding: 0 20px;
  border-bottom: 1px solid var(--line);
  background: var(--surface);
  grid-area: topbar;
  min-width: 0;
}

.topnav__brand {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 9px;
}

.topnav__mark {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 7px;
  background: var(--grad);
  box-shadow: 0 5px 12px rgb(22 93 255 / 28%);
  color: #fff;
}

.topnav__mark svg {
  width: 14px;
  height: 14px;
}

.topnav__brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.topnav__brand-text b {
  color: var(--ink);
  font-size: 13.5px;
  font-weight: 600;
  white-space: nowrap;
}

.topnav__brand-text small {
  color: var(--muted-3);
  font-size: 10px;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.topnav__nav {
  display: flex;
  flex: 1 1 auto;
  align-items: center;
  gap: 2px;
  height: 52px;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
}

.topnav__nav::-webkit-scrollbar {
  display: none;
}

.topnav__item {
  position: relative;
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 4px;
  height: 52px;
  padding: 0 13px;
  border: none;
  background: transparent;
  color: var(--muted);
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  cursor: pointer;
}

.topnav__item:hover {
  color: var(--ink-2);
}

.topnav__item.is-active {
  color: var(--brand);
  font-weight: 600;
}

.topnav__item.is-active::after {
  position: absolute;
  right: 12px;
  bottom: 0;
  left: 12px;
  height: 2px;
  border-radius: 2px 2px 0 0;
  background: var(--grad);
  content: '';
}

.topnav__chev {
  font-size: 10px;
  opacity: 0.7;
}

.topnav__right {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
}

.topnav__divider {
  width: 1px;
  height: 20px;
  background: var(--line);
}

@media (max-width: 1100px) {
  .topnav {
    gap: 12px;
    padding: 0 14px;
  }

  .topnav__brand-text small {
    display: none;
  }

  .topnav__item {
    padding: 0 10px;
    font-size: 12.5px;
  }
}

@media (max-width: 768px) {
  .topnav__brand-text {
    display: none;
  }
}
</style>
