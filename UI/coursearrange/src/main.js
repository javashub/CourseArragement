// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import App from './App';
import router from './router';
// import ElementUI from 'element-ui';
import axios from 'axios';
import 'element-ui/lib/theme-chalk/index.css';

// 引入图标
import '@/assets/iconfonts/fonts/iconfont.css';

// 引入echarts
import echarts from 'echarts'

// 引入全局样式
import './assets/css/global.css';
import {
  Button, Select, Form, Radio, Table, Container, Message, Pagination, Dialog, Autocomplete,
  Submenu, Menu, MenuItem, MenuItemGroup, Input, Option, FormItem, TableColumn, Row,
  Icon, Dropdown, DropdownMenu, DropdownItem, Header, Aside, Main, Footer, Popover, Upload,
  MessageBox, Col, Cascader, Image, Link
} from 'element-ui';

Vue.config.productionTip = false;
Vue.prototype.$axios = axios;
Vue.prototype.$message = Message;
Vue.prototype.$echarts = echarts
Vue.prototype.$confirm = MessageBox.confirm;
Vue.prototype.$prompt = MessageBox.prompt;

// Vue.use(ElementUI);
Vue.use(Button);
Vue.use(Col);
Vue.use(Cascader);
Vue.use(Popover);
Vue.use(Footer);
Vue.use(Main);
Vue.use(Upload);
Vue.use(Aside);
Vue.use(Header);
Vue.use(DropdownItem);
Vue.use(DropdownMenu);
Vue.use(Dropdown);
Vue.use(Icon);
Vue.use(Row);
Vue.use(TableColumn);
Vue.use(FormItem);
Vue.use(Option);
Vue.use(Select);
Vue.use(Form);
Vue.use(Radio);
Vue.use(Table);
Vue.use(Container);
Vue.use(Pagination);
Vue.use(Dialog);
Vue.use(Autocomplete);
Vue.use(Submenu);
Vue.use(Menu);
Vue.use(MenuItem);
Vue.use(MenuItemGroup);
Vue.use(Input);
Vue.use(Image);
Vue.use(Link);


new Vue({
  el: '#app',
  render: h => h(App),
  router,
  components: { App },
  template: '<App/>'
});
