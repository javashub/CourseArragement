import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import { useAppStore } from './stores/app';
import '@/styles/index.css';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

// 首屏把缓存主题落到 <html data-theme>，与 index.html 的内联脚本保持一致。
useAppStore().loadTheme();

app.mount('#app');
