// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import App from './App';
import router from './router';
import ElementUI from 'element-ui';
import axios from 'axios';
import 'element-ui/lib/theme-chalk/index.css';
// 引入图标
import '@/assets/iconfonts/fonts/iconfont.css';
// 引入全局样式
import './assets/css/global.css';
import { Button, Select, Form, Radio, Table, Container, Message } from 'element-ui';

Vue.config.productionTip = false;
Vue.prototype.$axios = axios;
// 配置axios请求的根路径
axios.defaults.baseURL = 'http://127.0.0.1:8080/'
Vue.use(ElementUI);
Vue.use(Button);
Vue.use(Select);
Vue.use(Form);
Vue.use(Radio);
Vue.use(Table);
Vue.use(Container);
//Vue.use(Message);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  render: h => h(App),
  router,
  components: { App },
  template: '<App/>'
});
