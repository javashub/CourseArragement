import Vue from 'vue';
import Router from 'vue-router';
import Home from '@/home/Home';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import Admin from '@/pages/Admin';
import ManagerMain from '@/manager/ManagerMain';
import TeacherList from '@/manager/components/TeacherList';
import AddTeacher from '@/manager/components/AddTeacher';
import SystemData from '@/manager/SystemData';
import AddClassroom from '@/manager/components/AddClassroom';
import ClassroomList from '@/manager/components/ClassroomList';
import TeachBuildingList from '@/manager/components/TeachBuildingList';
import CourseTable from '@/manager/components/CourseTable';
import CourseInfoList from '@/manager/components/CourseInfoList';
import StudentList from '@/manager/components/StudentList';
import ClassTaskList from '@/manager/components/ClassTaskList';
import ClassManager from '@/manager/components/ClassManager';


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
          // 登录成功跳转到该路径
          path: '/addclassroom',
          name: 'AddClassroom',
          component: AddClassroom
        },
        {
          // 登录成功跳转到该路径
          path: '/systemdata',
          name: 'SystemData',
          component: SystemData
        },
        {
          path: '/teacherlist',
          name: 'TeacherList',
          component: TeacherList
        },
        {
          path: '/studentlist',
          name: 'StudentList',
          component: StudentList
        },
        {
          path: '/teachbuildinglist',
          name: 'TeachBuildingList',
          component: TeachBuildingList
        },
        {
          path: '/addteacher',
          name: 'AddTeacher',
          component: AddTeacher
        },
        {
          path: '/classroomlist',
          name: 'ClassroomList',
          component: ClassroomList
        },
        {
          path: '/coursetable',
          name: 'CourseTable',
          component: CourseTable
        },
        {
          path: '/courseinfolist',
          name: 'CourseInfoList',
          component: CourseInfoList
        },
        {
          path: '/classtasklist',
          name: 'ClassTaskList',
          component: ClassTaskList
        },
        {
          path: '/classmanager',
          name: 'ClasssManager',
          component: ClassManager
        }
      ]
    }
  ]
})
