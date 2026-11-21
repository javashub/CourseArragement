<template>
  <div class="theme-switcher" role="group" aria-label="界面主题">
    <el-tooltip
      v-for="option in THEME_OPTIONS"
      :key="option.value"
      :content="`${option.label} · ${option.description}`"
      placement="bottom"
      :show-after="200"
    >
      <button
        type="button"
        class="theme-switcher__item"
        :class="{ 'is-active': option.value === appStore.theme }"
        :aria-label="option.label"
        :aria-pressed="option.value === appStore.theme"
        :title="option.label"
        @click="handleSelect(option.value)"
      >
        <span class="theme-switcher__swatch" :style="{ background: option.swatch }" />
      </button>
    </el-tooltip>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { saveUiTheme } from '@/api/modules/auth';
import { THEME_OPTIONS, useAppStore } from '@/stores/app';

const appStore = useAppStore();

async function handleSelect(theme) {
  if (!theme || theme === appStore.theme) {
    return;
  }
  // 先本地生效，再异步写回账号；失败不回滚，只给可重试的轻提示。
  appStore.applyTheme(theme);
  try {
    await saveUiTheme({ uiTheme: theme });
  } catch (error) {
    ElMessage.warning('主题已切换，但保存到账号失败，下次登录可能恢复为原主题');
  }
}
</script>

<style scoped>
.theme-switcher {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px;
  border: 1px solid var(--line);
  border-radius: var(--radius-control);
  background: var(--surface-2);
}

.theme-switcher__item {
  position: relative;
  display: grid;
  place-items: center;
  width: 26px;
  height: 22px;
  padding: 0;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.theme-switcher__item:hover {
  border-color: var(--line-2);
  background: var(--surface);
}

.theme-switcher__item.is-active {
  border-color: var(--brand);
  background: var(--surface);
  box-shadow: 0 0 0 2px rgb(22 93 255 / 12%);
}

.theme-switcher__item.is-active .theme-switcher__swatch {
  box-shadow: inset 0 0 0 1px rgb(22 93 255 / 45%);
}

.theme-switcher__swatch {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  box-shadow: inset 0 0 0 1px rgb(23 38 61 / 10%);
}
</style>
