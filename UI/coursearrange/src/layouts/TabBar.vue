<template>
  <nav class="tab-bar" aria-label="多标签页导航">
    <div class="tab-bar__list">
      <el-dropdown
        v-for="tab in tabs"
        :key="tab.path"
        trigger="contextmenu"
        placement="bottom-start"
        @command="handleCommand($event, tab)"
      >
        <div
          class="tab-bar__item"
          :class="{ 'is-active': tab.path === activePath }"
          :aria-current="tab.path === activePath ? 'page' : undefined"
          :title="tab.title"
        >
          <button type="button" class="tab-bar__tab" @click="$emit('select', tab)">
            {{ tab.title }}
          </button>
          <button
            type="button"
            class="tab-bar__close"
            aria-label="关闭标签"
            :title="`关闭 ${tab.title}`"
            @click.stop="$emit('close', tab)"
          >
            <el-icon><Close /></el-icon>
          </button>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="refresh" :icon="Refresh">刷新</el-dropdown-item>
            <el-dropdown-item command="close" :icon="Close">关闭</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <span class="tab-bar__hint">右键标签可刷新</span>
  </nav>
</template>

<script setup>
import { Close, Refresh } from '@element-plus/icons-vue';

defineProps({
  tabs: {
    type: Array,
    default: () => []
  },
  activePath: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['select', 'refresh', 'close']);

/**
 * 处理标签右键菜单命令。
 * @param {'refresh'|'close'} command 菜单命令
 * @param {{ path: string }} tab 目标标签
 */
function handleCommand(command, tab) {
  if (command === 'refresh') {
    emit('refresh', tab);
  } else if (command === 'close') {
    emit('close', tab);
  }
}
</script>

<style scoped>
.tab-bar {
  display: flex;
  grid-area: tabs;
  align-items: center;
  gap: 8px;
  min-width: 0;
  height: 38px;
  padding: 0 12px;
  border-bottom: 1px solid var(--line);
  background: var(--surface);
  overflow: hidden;
}

.tab-bar__list {
  display: flex;
  flex: 1 1 auto;
  align-items: center;
  min-width: 0;
  height: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
}

.tab-bar__list::-webkit-scrollbar {
  display: none;
}

.tab-bar__list :deep(.el-dropdown) {
  position: relative;
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  height: 100%;
}

/* 参考浏览器标签：相邻标签共用一条竖分隔线。 */
.tab-bar__list :deep(.el-dropdown + .el-dropdown)::before {
  width: 1px;
  height: 14px;
  margin: 0 2px;
  background: var(--line-2);
  content: '';
}

.tab-bar__item {
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: 220px;
  height: 100%;
  padding: 0 10px;
  border-bottom: 2px solid transparent;
  color: var(--muted);
  transition: color 0.15s ease, border-color 0.15s ease;
}

.tab-bar__item:hover {
  color: var(--ink-2);
}

.tab-bar__item.is-active {
  border-bottom-color: var(--brand);
  color: var(--brand);
  font-weight: 600;
}

.tab-bar__tab {
  overflow: hidden;
  padding: 0;
  border: none;
  background: transparent;
  color: inherit;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: inherit;
  white-space: nowrap;
  text-overflow: ellipsis;
  cursor: pointer;
}

.tab-bar__close {
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  width: 16px;
  height: 16px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--muted-3);
  font-size: 11px;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.tab-bar__close:hover {
  background: var(--surface-2);
  color: var(--ink-2);
}

.tab-bar__item.is-active .tab-bar__close:hover {
  background: var(--brand-soft);
  color: var(--brand);
}

.tab-bar__hint {
  flex: 0 0 auto;
  color: var(--muted-3);
  font-size: 11px;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .tab-bar {
    padding: 0 8px;
  }

  .tab-bar__item {
    max-width: 160px;
    padding: 0 8px;
  }

  .tab-bar__hint {
    display: none;
  }
}
</style>
