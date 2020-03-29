import Vue from 'vue';
import Router from 'vue-router';
import Home from '@/home/Home';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import Admin from '@/pages/Admin';
import ManagerMain from '@/manager/ManagerMain';
import TeacherList from '@/manager/components/TeacherList';
import AddTeacher from '@/manager/components/AddTeacher';

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
      name: 'AdminLogin',
      component: Admin
    },
    {
      path: '/admin',
      name: 'Admin',
      component: ManagerMain,
      children: [
        {
          path: '/teacherList',
          name: 'TeacherList',
          component: TeacherList
        },
        {
          path: '/addTeacher',
          name: 'AddTeacher',
          component: AddTeacher
        }
      ]
    }
  ]
})
