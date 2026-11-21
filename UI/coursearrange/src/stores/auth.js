import { defineStore } from 'pinia';
import { fetchAuthContext } from '@/api/modules/auth';
import { AUTH_CONTEXT_KEY, TOKEN_KEY, USER_KEY } from '@/constants/storage';
import { useAppStore } from '@/stores/app';
import { getStorage, removeStorage, setStorage } from '@/utils/storage';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getStorage(TOKEN_KEY, ''),
    user: getStorage(USER_KEY, null),
    authContext: getStorage(AUTH_CONTEXT_KEY, null),
    permissions: getStorage(AUTH_CONTEXT_KEY, null)?.permissions || [],
    menus: getStorage(AUTH_CONTEXT_KEY, null)?.menus || []
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    contextLoaded: (state) => Boolean(state.authContext),
    displayName: (state) =>
      state.user?.displayName || state.user?.realName || state.user?.realname || state.user?.username || '未登录用户',
    hasPermission: (state) => (permissionCode) => {
      if (!permissionCode) {
        return true;
      }
      return state.permissions.includes(permissionCode);
    },
    hasMenuPath: (state) => (routePath) => {
      const walk = (menuList) => {
        for (const item of menuList || []) {
          if (item?.routePath === routePath && item?.isHidden !== 1) {
            return true;
          }
          if (walk(item?.children || [])) {
            return true;
          }
        }
        return false;
      };
      return walk(state.menus);
    },
    firstAccessiblePath: (state) => {
      const walk = (menuList) => {
        for (const item of menuList || []) {
          if (item?.isHidden === 1) {
            continue;
          }
          if (item?.routePath && (!item.permissionCode || state.permissions.includes(item.permissionCode))) {
            return item.routePath;
          }
          const childPath = walk(item?.children || []);
          if (childPath) {
            return childPath;
          }
        }
        return '';
      };
      return walk(state.menus);
    }
  },
  actions: {
    setLoginState(token, user) {
      this.token = token;
      this.user = user;
      this.authContext = null;
      this.permissions = [];
      this.menus = [];
      setStorage(TOKEN_KEY, token);
      setStorage(USER_KEY, user);
      removeStorage(AUTH_CONTEXT_KEY);
    },
    setAuthContext(context) {
      this.authContext = context;
      this.user = context?.user || this.user;
      this.permissions = context?.permissions || [];
      this.menus = context?.menus || [];
      setStorage(USER_KEY, this.user);
      setStorage(AUTH_CONTEXT_KEY, context);
      this.syncThemeFromAccount(context);
    },
    // 登录上下文里的 user.uiTheme 覆盖本地缓存；接口未返回该字段时沿用本地值（默认 daylight）。
    syncThemeFromAccount(context) {
      const appStore = useAppStore();
      const accountTheme = context?.user?.uiTheme;
      if (accountTheme) {
        appStore.applyTheme(accountTheme);
        return;
      }
      appStore.loadTheme();
    },
    async loadAuthContext() {
      const response = await fetchAuthContext();
      this.setAuthContext(response.data || {});
      return this.authContext;
    },
    clearLoginState() {
      this.token = '';
      this.user = null;
      this.authContext = null;
      this.permissions = [];
      this.menus = [];
      removeStorage(TOKEN_KEY);
      removeStorage(USER_KEY);
      removeStorage(AUTH_CONTEXT_KEY);
    }
  }
});
