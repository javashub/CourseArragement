import { defineStore } from 'pinia';
import { TOKEN_KEY, USER_KEY } from '@/constants/storage';
import { getStorage, removeStorage, setStorage } from '@/utils/storage';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getStorage(TOKEN_KEY, ''),
    user: getStorage(USER_KEY, null)
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setLoginState(token, user) {
      this.token = token;
      this.user = user;
      setStorage(TOKEN_KEY, token);
      setStorage(USER_KEY, user);
    },
    clearLoginState() {
      this.token = '';
      this.user = null;
      removeStorage(TOKEN_KEY);
      removeStorage(USER_KEY);
    }
  }
});
