<template>
  <el-dropdown trigger="click" placement="bottom-end" @command="handleCommand">
    <button
      type="button"
      class="account-menu__trigger"
      aria-label="账号菜单"
      aria-haspopup="menu"
    >
      <span class="account-menu__avatar" aria-hidden="true">{{ avatarText }}</span>
      <span class="account-menu__meta">
        <b class="account-menu__name" :title="authStore.displayName">{{ authStore.displayName }}</b>
        <small class="account-menu__role">{{ roleLabel }}</small>
      </span>
      <el-icon class="account-menu__chevron" aria-hidden="true"><ArrowDown /></el-icon>
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="profile" :icon="User">个人中心</el-dropdown-item>
        <el-dropdown-item
          command="logout"
          :icon="SwitchButton"
          divided
          class="account-menu__logout"
        >
          退出登录
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowDown, SwitchButton, User } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 与改造前两套外壳的角色文案保持一致，避免登录后角色说明发生变化。
const ROLE_LABELS = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生'
};

const avatarText = computed(() => (authStore.displayName || '未').slice(0, 1));
const roleLabel = computed(() => ROLE_LABELS[authStore.user?.userType] || '已登录');

/**
 * 处理账号下拉菜单命令。
 * @param {'profile'|'logout'} command 下拉项命令
 */
function handleCommand(command) {
  if (command === 'profile') {
    // 已停留在个人中心时不重复跳转，避免触发多余的路由导航。
    if (route.path !== '/profile') {
      router.push('/profile').catch(() => {});
    }
    return;
  }
  if (command === 'logout') {
    // 与改造前两个外壳的退出行为一致：只清理前端登录态，再回到登录页。
    authStore.clearLoginState();
    router.push('/login').catch(() => {});
  }
}
</script>

<style scoped>
.account-menu__trigger {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
  height: 32px;
  padding: 0 6px 0 3px;
  border: 1px solid transparent;
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--ink);
  font-family: inherit;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.account-menu__trigger:hover,
.account-menu__trigger:focus-visible {
  border-color: var(--line);
  background: var(--surface-2);
}

.account-menu__trigger:focus-visible {
  outline: 2px solid var(--brand);
  outline-offset: 1px;
}

.account-menu__avatar {
  display: grid;
  flex: 0 0 28px;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8f1ff, #dff7f5);
  color: var(--brand);
  font-size: 12px;
  font-weight: 600;
}

.account-menu__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
  line-height: 1.25;
  text-align: left;
}

.account-menu__name {
  max-width: 10em;
  overflow: hidden;
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 600;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.account-menu__role {
  color: var(--muted-3);
  font-size: 10.5px;
  white-space: nowrap;
}

.account-menu__chevron {
  flex: 0 0 auto;
  color: var(--muted-3);
  font-size: 10px;
}

/* 从下拉项中突出退出登录，但仍沿用 Element Plus 的危险色变量。 */
:deep(.el-dropdown-menu__item.account-menu__logout) {
  color: var(--el-color-danger);
}

:deep(.el-dropdown-menu__item.account-menu__logout:hover),
:deep(.el-dropdown-menu__item.account-menu__logout:focus) {
  background: var(--surface-2);
  color: var(--el-color-danger);
}

/* 窄屏只保留头像和箭头，避免账号入口挤压顶栏菜单。 */
@media (max-width: 1100px) {
  .account-menu__meta {
    display: none;
  }
}

@media (max-width: 768px) {
  .account-menu__trigger {
    gap: 6px;
    padding: 0 4px;
  }
}
</style>
