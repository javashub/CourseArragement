import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
  state: () => ({
    appTitle: import.meta.env.VITE_APP_TITLE || '课程排课系统'
  })
});
