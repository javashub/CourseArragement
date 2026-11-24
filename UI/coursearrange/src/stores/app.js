import { defineStore } from 'pinia';
import { UI_THEME_KEY } from '@/constants/storage';
import { getStorage, setStorage } from '@/utils/storage';

export const THEME_DAYLIGHT = 'daylight';
export const THEME_DEEP_TEAL = 'deep-teal';
export const THEME_TOP_NAV = 'top-nav';
export const THEME_GRAPHITE = 'graphite';
export const THEME_EMERALD = 'emerald';
export const DEFAULT_THEME = THEME_DAYLIGHT;

// 主题 = 色板 + 导航布局，共用一个 id；后端只持久化这个 id。
export const THEME_OPTIONS = [
  {
    value: THEME_DAYLIGHT,
    label: '晴空白瓷',
    description: '浅色侧栏 · 冷白内容区',
    layout: 'sidebar',
    swatch: 'linear-gradient(135deg, #ffffff 0%, #eaf3ff 100%)'
  },
  {
    value: THEME_DEEP_TEAL,
    label: '深青侧栏',
    description: '深色导航 · 冷白内容区',
    layout: 'sidebar',
    swatch: 'linear-gradient(135deg, #0b2734 0%, #165dff 100%)'
  },
  {
    value: THEME_TOP_NAV,
    label: '顶栏全宽',
    description: '菜单收到顶栏 · 表格更宽',
    layout: 'top-nav',
    swatch: 'linear-gradient(135deg, #165dff 0%, #0ea5a4 100%)'
  },
  {
    value: THEME_GRAPHITE,
    label: '石墨银蓝',
    description: '石墨侧栏 · 天蓝强调',
    layout: 'sidebar',
    swatch: 'linear-gradient(135deg, #0f172a 0%, #2563eb 100%)'
  },
  {
    value: THEME_EMERALD,
    label: '松石苔绿',
    description: '墨绿侧栏 · 薄荷强调',
    layout: 'sidebar',
    swatch: 'linear-gradient(135deg, #0b3b35 0%, #10b981 100%)'
  }
];

export function normalizeTheme(theme) {
  return THEME_OPTIONS.some((item) => item.value === theme) ? theme : DEFAULT_THEME;
}

export function resolveThemeLayout(theme) {
  return normalizeTheme(theme) === THEME_TOP_NAV ? 'top-nav' : 'sidebar';
}

export const useAppStore = defineStore('app', {
  state: () => ({
    appTitle: import.meta.env.VITE_APP_TITLE || '课程排课系统',
    // 本地缓存只做首屏乐观值，登录后以 auth context 的 user.uiTheme 为准。
    theme: normalizeTheme(getStorage(UI_THEME_KEY, DEFAULT_THEME))
  }),
  getters: {
    themeMeta: (state) => THEME_OPTIONS.find((item) => item.value === state.theme) || THEME_OPTIONS[0],
    themeLayout: (state) => resolveThemeLayout(state.theme),
    isTopNav: (state) => state.theme === THEME_TOP_NAV
  },
  actions: {
    applyTheme(theme, options = {}) {
      const nextTheme = normalizeTheme(theme);
      this.theme = nextTheme;
      if (typeof document !== 'undefined') {
        document.documentElement.setAttribute('data-theme', nextTheme);
      }
      if (options.cache !== false) {
        setStorage(UI_THEME_KEY, nextTheme);
      }
      return nextTheme;
    },
    loadTheme() {
      return this.applyTheme(this.theme, { cache: false });
    }
  }
});
