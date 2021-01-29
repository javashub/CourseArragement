import Vue from 'vue';
import Router from 'vue-router';
import Home from '@/home/Home';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import Admin from '@/pages/Admin';
import ManagerMain from '@/manager/ManagerMain';
import TeacherList from '@/manager/components/TeacherList';
import TeachAreaSetting from '@/manager/components/TeachAreaSetting';
import SystemData from '@/manager/SystemData';
import OnlineCourse from '@/manager/components/OnlineCourse';
import StudyDocs from '@/manager/components/StudyDocs';
import HomeWork from '@/manager/components/HomeWork';
import Exercise from '@/manager/components/Exercise';
import ClassroomList from '@/manager/components/ClassroomList';
import TeachBuildingList from '@/manager/components/TeachBuildingList';
import CourseTable from '@/manager/components/CourseTable';
import StudentCourseTable from '@/home/components/CourseTable';
import EmptyClassroom from '@/home/components/EmptyClassroom';
import StudentCenter from '@/home/components/Center';
import StudentPassword from '@/home/components/Password';
import CourseInfoList from '@/manager/components/CourseInfoList';
import StudentList from '@/manager/components/StudentList';
import ClassTaskList from '@/manager/components/ClassTaskList';
import ClassManager from '@/manager/components/ClassManager';
import UpdatePass from '@/pages/components/UpdatePass';
import StudentDoc from '@/home/components/StudentDoc';
import OnlineCategory from '@/manager/components/OnlineCategory';
import Help from '@/manager/components/Help';
import StudentMain from '@/home/StudentMain';

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/student/login',
      name: 'Login',
      component: Login,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/student/register',
      name: 'Register',
      component: Register,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: Admin,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/student',
      name: 'Student',
      component: StudentMain,
      meta: { 
        noRequireAuth: true
      },
      children: [
        
        {
          path: '/emptyclassroom',
          name: 'EmptyClassroom',
          component: EmptyClassroom
        },
        {
          path: '',
          name: 'CourseList-default',
          component: StudentCourseTable
        },
        {
          path: '/courseList',
          name: 'CourseList',
          component: StudentCourseTable
        },
        {
          path: '/center',
          name: 'StudentCenter',
          component: StudentCenter
        },
        {
          path: '/password',
          name: 'Password',
          component: StudentPassword
        },
        {
          path: '/studentdoc',
          name: 'StudentDoc',
          component: StudentDoc
        }
      ]
    },
    {
      path: '/admin',
      name: 'Admin',
      component: ManagerMain,
      children: [
        {
          path: '/help',
          name: 'Help',
          component: Help
        },
        {
          path: '/onlinecategory',
          name: 'OnlineCategory',
          component: OnlineCategory
        },
        {
          path: '/updatepass',
          name: 'UpdatePass',
          component: UpdatePass
        },
        {
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
        },
        {
          path: '/setteacharea',
          name: 'TeachAreaSetting',
          component: TeachAreaSetting
        },
        {
          path: '/onlinecourse',
          name: 'OnlineCourse',
          component: OnlineCourse
        },
        {
          path: '/studydocs',
          name: 'StudyDocs',
          component: StudyDocs
        },
        {
          path: '/homework',
          name: 'HomeWork',
          component: HomeWork
        },
        {
          path: '/exercise',
          name: 'Exercise',
          component: Exercise
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  if (!to.matched.some(res => res.meta.noRequireAuth)) {// 判断是否需要登录权限
    if (window.localStorage.getItem('teacher') || window.localStorage.getItem("admin") ) {// 判断是否登录
      next()
    } else {
      next({
        path: '/',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    next()
  }
  next()
})


export default router