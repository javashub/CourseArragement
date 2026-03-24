<template>
  <section class="editorial-shell">
    <header class="editorial-hero page-card">
      <div class="editorial-column">
        <div class="editorial-kicker">Executive Editorial Desk</div>
        <h1 class="editorial-title">工作台</h1>
        <p class="editorial-description">
          现在的首页保留为高端编辑式控制台版本，强调排版秩序、留白、材质感与治理叙事，用更克制的方式呈现排课系统主链状态。
        </p>
      </div>
      <div class="editorial-card">
        <div class="editorial-card__label">Current Operator</div>
        <div class="editorial-card__name">{{ authStore.displayName }}</div>
        <div class="editorial-card__meta">{{ authStore.user?.username || '--' }}</div>
      </div>
    </header>

    <section class="editorial-metrics">
      <article class="metric-panel" v-for="item in panels" :key="item.title">
        <div class="metric-panel__index">{{ item.index }}</div>
        <div class="metric-panel__title">{{ item.title }}</div>
        <div class="metric-panel__desc">{{ item.description }}</div>
      </article>
    </section>

    <section class="editorial-grid">
      <article class="editorial-block editorial-block--lead">
        <div class="block-kicker">System Note</div>
        <h2 class="block-title">主链摘要</h2>
        <div class="essay-list">
          <div class="essay-item" v-for="item in essays" :key="item.title">
            <div class="essay-item__title">{{ item.title }}</div>
            <div class="essay-item__text">{{ item.text }}</div>
          </div>
        </div>
      </article>

      <article class="editorial-block">
        <div class="block-kicker">Current Focus</div>
        <h2 class="block-title">阶段重点</h2>
        <div class="focus-list">
          <div class="focus-line" v-for="item in focusList" :key="item.title">
            <div class="focus-line__mark"></div>
            <div>
              <div class="focus-line__title">{{ item.title }}</div>
              <div class="focus-line__text">{{ item.text }}</div>
            </div>
          </div>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const panels = [
  {
    index: '01',
    title: '统一主链',
    description: '认证、菜单、配置与排课入口已经聚拢到当前后台框架。'
  },
  {
    index: '02',
    title: '规则治理',
    description: '时间片模板、禁排与优先级等约束已进入标准排课流程。'
  },
  {
    index: '03',
    title: '深入开发',
    description: '当前主要工作是继续压缩旧模型依赖，整理算法边界。'
  }
];

const essays = [
  {
    title: '认证与菜单',
    text: '权限管理已经彻底回归左侧菜单，首页不再重复承担导航职责。'
  },
  {
    title: '排课任务与课表',
    text: '标准任务、标准课表和执行入口已经成为当前系统的主要运行骨架。'
  },
  {
    title: '下一阶段',
    text: '需要继续清理旧实体依赖，并为算法深入开发准备更可测试的输入结构。'
  }
];

const focusList = [
  {
    title: '收口旧课表兼容链',
    text: '继续围绕旧实体、旧 DAO 与兼容壳层做减法。'
  },
  {
    title: '梳理算法输入模型',
    text: '减少历史基因编码语义在服务层的扩散。'
  },
  {
    title: '提升前端整体完成度',
    text: '在主链稳定后再细化统计、体验和测试基础。'
  }
];
</script>

<style scoped>
.editorial-shell {
  display: flex;
  flex-direction: column;
  gap: 22px;
  color: #2f251b;
}

.editorial-hero,
.metric-panel,
.editorial-block {
  background:
    linear-gradient(180deg, rgba(255, 250, 242, 0.98), rgba(247, 238, 224, 0.94));
  border: 1px solid rgba(110, 84, 54, 0.12);
  box-shadow: 0 22px 40px rgba(110, 84, 54, 0.1);
}

.editorial-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 0.8fr);
  gap: 24px;
}

.editorial-kicker,
.block-kicker {
  color: #8a6533;
  font-size: 11px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.editorial-title,
.block-title,
.metric-panel__title,
.essay-item__title,
.focus-line__title {
  font-family: 'Bodoni 72', 'Didot', 'STSong', serif;
}

.editorial-title {
  margin: 14px 0 0;
  font-size: clamp(40px, 5vw, 64px);
  line-height: 0.95;
  color: #2c2319;
}

.editorial-description,
.metric-panel__desc,
.essay-item__text,
.focus-line__text {
  margin-top: 14px;
  color: #6a6056;
  line-height: 1.8;
}

.editorial-card {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px;
  border-radius: 22px;
  background: linear-gradient(160deg, rgba(32, 44, 57, 0.94), rgba(55, 72, 88, 0.9));
  color: #f8efe3;
}

.editorial-card__label {
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(248, 239, 227, 0.72);
}

.editorial-card__name {
  margin-top: 18px;
  font-size: 36px;
  font-family: 'Bodoni 72', 'Didot', 'STSong', serif;
}

.editorial-card__meta {
  margin-top: 8px;
  color: rgba(248, 239, 227, 0.76);
}

.editorial-metrics,
.editorial-grid {
  display: grid;
  gap: 18px;
}

.editorial-metrics {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metric-panel,
.editorial-block {
  padding: 24px;
  border-radius: 24px;
}

.metric-panel__index {
  color: rgba(138, 101, 51, 0.28);
  font-size: 42px;
  font-family: 'Bodoni 72', 'Didot', 'STSong', serif;
}

.metric-panel__title {
  margin-top: 12px;
  font-size: 28px;
  color: #2c2319;
}

.editorial-grid {
  grid-template-columns: minmax(0, 1.3fr) minmax(0, 1fr);
}

.block-title {
  margin-top: 8px;
  font-size: 32px;
  color: #2c2319;
}

.essay-list,
.focus-list {
  display: grid;
  gap: 16px;
  margin-top: 18px;
}

.essay-item {
  padding: 16px 0;
  border-bottom: 1px solid rgba(110, 84, 54, 0.12);
}

.essay-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.essay-item__title,
.focus-line__title {
  font-size: 22px;
  color: #2f251b;
}

.focus-line {
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr);
  gap: 14px;
}

.focus-line__mark {
  margin-top: 10px;
  width: 14px;
  height: 14px;
  border-radius: 999px;
  background: linear-gradient(160deg, #8a6533, #d2b07a);
}

@media (max-width: 1024px) {
  .editorial-hero,
  .editorial-metrics,
  .editorial-grid {
    grid-template-columns: 1fr;
  }
}
</style>
