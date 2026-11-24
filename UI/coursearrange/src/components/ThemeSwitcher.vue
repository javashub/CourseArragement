<template>
  <el-dropdown
    ref="themeDropdownRef"
    trigger="click"
    placement="bottom-end"
    popper-class="theme-switcher__popper"
    @command="handleSelect"
  >
    <button
      type="button"
      class="theme-switcher"
      aria-label="切换主题"
      aria-haspopup="menu"
      title="切换主题"
      @keydown.esc="handleTriggerEscape"
    >
      <span
        class="theme-switcher__icon"
        aria-hidden="true"
        :style="{ '--theme-icon-url': `url(${pifuIcon})` }"
      />
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="option in THEME_OPTIONS"
          :key="option.value"
          :command="option.value"
          class="theme-switcher__option"
          :class="{ 'is-active': option.value === appStore.theme }"
        >
          <span
            class="theme-switcher__swatch"
            aria-hidden="true"
            :style="{ background: option.swatch }"
          />
          <span class="theme-switcher__meta">
            <b>{{ option.label }}</b>
            <small>{{ option.description }}</small>
          </span>
          <el-icon v-if="option.value === appStore.theme" class="theme-switcher__check">
            <Check />
          </el-icon>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Check } from '@element-plus/icons-vue';
import pifuIcon from '@/assets/iconfonts/icons/pifu.svg';
import { saveUiTheme } from '@/api/modules/auth';
import { THEME_OPTIONS, useAppStore } from '@/stores/app';

const appStore = useAppStore();
const themeDropdownRef = ref(null);

/** 焦点在触发按钮时按 Esc 也能关闭主题下拉。 */
function handleTriggerEscape() {
  themeDropdownRef.value?.handleClose();
}

/**
 * 选择主题后先本地生效，再异步写回当前账号。
 * 保存失败不回滚主题，只给出可重试的轻提示。
 * @param {string} theme 主题标识
 */
async function handleSelect(theme) {
  if (!theme || theme === appStore.theme) {
    return;
  }
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
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 1px solid transparent;
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease, color 0.15s ease;
}

.theme-switcher:hover,
.theme-switcher:focus-visible {
  border-color: var(--line);
  background: var(--surface-2);
  color: var(--ink-2);
}

.theme-switcher:focus-visible {
  outline: 2px solid var(--brand);
  outline-offset: 1px;
}

/* pifu.svg 只取形状；用 currentColor 填充，避免 SVG 内嵌蓝色与主题冲突。 */
.theme-switcher__icon {
  width: 16px;
  height: 16px;
  background: currentColor;
  -webkit-mask: var(--theme-icon-url) center / contain no-repeat;
  mask: var(--theme-icon-url) center / contain no-repeat;
}

:deep(.el-dropdown-menu__item.theme-switcher__option) {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 190px;
  padding: 7px 10px;
}

.theme-switcher__swatch {
  flex: 0 0 16px;
  width: 16px;
  height: 16px;
  border-radius: 4px;
  box-shadow: inset 0 0 0 1px rgb(23 38 61 / 10%);
}

.theme-switcher__meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.25;
}

.theme-switcher__meta b {
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 600;
}

.theme-switcher__meta small {
  color: var(--muted-3);
  font-size: 10.5px;
}

.theme-switcher__check {
  margin-left: auto;
  color: var(--brand);
  font-size: 13px;
}

:deep(.el-dropdown-menu__item.theme-switcher__option.is-active .theme-switcher__meta b) {
  color: var(--brand);
}
</style>
