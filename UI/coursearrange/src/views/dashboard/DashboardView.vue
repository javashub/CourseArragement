<template>
  <section class="dashboard-shell">
    <div class="hero-card page-card">
      <div class="hero-copy">
        <div class="hero-kicker">Campus Operations Ledger</div>
        <h1 class="page-title">工作台</h1>
        <p class="page-description">当前页面已经接入真实登录上下文、菜单权限与系统配置接口。现在的视觉方向改成学院档案馆式控制台，强调状态、权限和系统迁移进度的可视化层次。</p>
      </div>
      <div class="hero-side">
        <div class="hero-plate">Live Context</div>
        <div class="hero-user">{{ authStore.displayName }}</div>
        <div class="hero-meta">{{ authStore.user?.username || '--' }}</div>
      </div>
    </div>

    <div class="metrics-grid">
      <el-card shadow="hover" class="metric-card metric-card--primary">
        <div class="metric-label">当前账号</div>
        <div class="metric-value">{{ authStore.displayName }}</div>
        <div class="metric-text">{{ authStore.user?.username || '--' }}</div>
      </el-card>
      <el-card shadow="hover" class="metric-card">
        <div class="metric-label">角色数量</div>
        <div class="metric-value">{{ authStore.user?.roles?.length || 0 }}</div>
        <div class="metric-text">{{ (authStore.user?.roles || []).join(' / ') || '未分配角色' }}</div>
      </el-card>
      <el-card shadow="hover" class="metric-card">
        <div class="metric-label">权限数量</div>
        <div class="metric-value">{{ authStore.permissions.length }}</div>
        <div class="metric-text">菜单与按钮权限由后端统一下发</div>
      </el-card>
    </div>

    <el-card shadow="never" class="quick-action-card" v-if="canOpenRbac">
      <div class="quick-action-content">
        <div>
          <div class="quick-action-title">权限管理联调入口</div>
          <div class="quick-action-text">当前已接通用户、角色、菜单、权限分配接口，可以直接进入 RBAC 管理页测试，适合继续校验后端权限上下文与菜单可见性。</div>
        </div>
        <el-button type="primary" @click="router.push('/system/rbac')">进入权限管理</el-button>
      </div>
    </el-card>

    <div class="insight-grid">
      <el-card shadow="hover" class="insight-card" v-for="item in panels" :key="item.title">
        <div class="insight-index">{{ item.index }}</div>
        <div class="insight-title">{{ item.title }}</div>
        <div class="insight-description">{{ item.description }}</div>
      </el-card>
    </div>
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
  { index: '01', title: '前端栈', description: 'Vue 3 + Vite + Pinia + Element Plus + 路由守卫' },
  { index: '02', title: '后端栈', description: 'Spring Boot + MyBatis-Plus + Sa-Token + RBAC3' },
  { index: '03', title: '当前策略', description: '先打通新认证与组织配置闭环，再逐模块迁移排课业务' }
];
</script>

<style scoped>
.dashboard-shell {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(240px, 0.8fr);
  gap: 22px;
  align-items: stretch;
}

.hero-kicker {
  color: #8a6533;
  font-size: 11px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-side {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  min-height: 220px;
  padding: 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgb(255 255 255 / 0.2), transparent 28%),
    linear-gradient(155deg, #14354a 0%, #1f4d6b 58%, #345f79 100%);
  color: #f7f1e7;
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.08);
}

.hero-side::after {
  position: absolute;
  inset: auto -40px -40px auto;
  width: 160px;
  height: 160px;
  border-radius: 999px;
  background: rgb(184 135 70 / 0.18);
  content: '';
}

.hero-plate {
  margin-bottom: auto;
  color: rgb(232 211 181 / 0.88);
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.hero-user {
  position: relative;
  z-index: 1;
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
}

.hero-meta {
  position: relative;
  z-index: 1;
  margin-top: 10px;
  color: rgb(247 241 231 / 0.76);
  font-size: 14px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.metric-card {
  overflow: hidden;
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.82), rgb(250 244 234 / 0.94));
}

.metric-card--primary {
  background:
    radial-gradient(circle at top right, rgb(184 135 70 / 0.18), transparent 28%),
    linear-gradient(160deg, rgb(255 249 239 / 0.96), rgb(243 233 214 / 0.92));
}

.metric-label {
  color: #8a6533;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.metric-value {
  margin-top: 14px;
  color: #102c3c;
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: clamp(30px, 2.6vw, 44px);
  font-weight: 700;
  line-height: 1;
}

.metric-text {
  margin-top: 12px;
  color: #5f6e79;
  line-height: 1.7;
}

.quick-action-card {
  border: 1px solid rgb(31 77 107 / 0.1);
  background:
    linear-gradient(135deg, rgb(250 245 236 / 0.98), rgb(242 232 214 / 0.92));
}

.quick-action-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.quick-action-title {
  color: #102c3c;
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: 24px;
  font-weight: 700;
}

.quick-action-text {
  margin-top: 8px;
  max-width: 720px;
  color: #5f6e79;
  line-height: 1.7;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.insight-card {
  position: relative;
  min-height: 200px;
  padding-top: 54px;
  background:
    linear-gradient(180deg, rgb(255 252 246 / 0.82), rgb(249 244 236 / 0.96));
}

.insight-index {
  position: absolute;
  top: 20px;
  right: 22px;
  color: rgb(31 77 107 / 0.24);
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: 42px;
  font-weight: 700;
}

.insight-title {
  color: #102c3c;
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: 24px;
  font-weight: 700;
}

.insight-description {
  margin-top: 16px;
  color: #5f6e79;
  line-height: 1.8;
}

@media (max-width: 1024px) {
  .hero-card,
  .metrics-grid,
  .insight-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .quick-action-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
