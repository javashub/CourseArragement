import Vue from 'vue';
import Router from 'vue-router';
import Home from '@/home/Home';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import Admin from '@/pages/Admin';

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/student/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/student/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/admin/login',
      name: 'Admin',
      component: Admin
    }
  ]
})
