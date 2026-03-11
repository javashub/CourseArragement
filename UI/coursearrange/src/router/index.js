import { createRouter, createWebHashHistory } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';
import LoginView from '@/views/auth/LoginView.vue';
import DashboardView from '@/views/dashboard/DashboardView.vue';
import CoursePlanView from '@/views/course/CoursePlanView.vue';
import ScheduleView from '@/views/course/ScheduleView.vue';
import BaseDataView from '@/views/base/BaseDataView.vue';
import SystemGuideView from '@/views/system/SystemGuideView.vue';
import CampusView from '@/views/organization/CampusView.vue';
import CollegeView from '@/views/organization/CollegeView.vue';
import StageView from '@/views/organization/StageView.vue';
import SystemConfigView from '@/views/system/SystemConfigView.vue';
import RbacManageView from '@/views/system/RbacManageView.vue';
import TeacherDashboardView from '@/views/teacher/TeacherDashboardView.vue';
import TeacherTimetableView from '@/views/teacher/TeacherTimetableView.vue';
import StudentDashboardView from '@/views/student/StudentDashboardView.vue';
import StudentTimetableView from '@/views/student/StudentTimetableView.vue';
import { useAuthStore } from '@/stores/auth';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: {
      public: true,
      title: '登录'
    }
  },
  {
    path: '/',
    component: BasicLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: DashboardView,
        meta: {
          title: '工作台',
          permission: 'page:dashboard:view'
        }
      },
      {
        path: 'course-plan',
        name: 'CoursePlan',
        component: CoursePlanView,
        meta: {
          title: '排课任务',
          permission: 'page:course-plan:view'
        }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: ScheduleView,
        meta: {
          title: '课表管理',
          permission: 'page:schedule:view'
        }
      },
      {
        path: 'base-data',
        name: 'BaseData',
        component: BaseDataView,
        meta: {
          title: '基础数据',
          permission: 'page:base-data:view'
        }
      },
      {
        path: 'organization/campus',
        name: 'CampusPage',
        component: CampusView,
        meta: {
          title: '校区管理',
          permission: 'page:campus:view'
        }
      },
      {
        path: 'organization/college',
        name: 'CollegePage',
        component: CollegeView,
        meta: {
          title: '学院管理',
          permission: 'page:college:view'
        }
      },
      {
        path: 'organization/stage',
        name: 'StagePage',
        component: StageView,
        meta: {
          title: '学段管理',
          permission: 'page:stage:view'
        }
      },
      {
        path: 'system/config',
        name: 'SystemConfigPage',
        component: SystemConfigView,
        meta: {
          title: '系统配置',
          permission: 'page:config:view'
        }
      },
      {
        path: 'system/rbac',
        name: 'RbacManagePage',
        component: RbacManageView,
        meta: {
          title: '权限管理',
          permission: 'page:rbac:view'
        }
      },
      {
        path: 'teacher/dashboard',
        name: 'TeacherDashboard',
        component: TeacherDashboardView,
        meta: {
          title: '教师工作台',
          permission: 'page:teacher-home:view'
        }
      },
      {
        path: 'teacher/timetable',
        name: 'TeacherTimetable',
        component: TeacherTimetableView,
        meta: {
          title: '我的课表',
          permission: 'page:teacher-timetable:view'
        }
      },
      {
        path: 'student/dashboard',
        name: 'StudentDashboard',
        component: StudentDashboardView,
        meta: {
          title: '学生工作台',
          permission: 'page:student-home:view'
        }
      },
      {
        path: 'student/timetable',
        name: 'StudentTimetable',
        component: StudentTimetableView,
        meta: {
          title: '我的课表',
          permission: 'page:student-timetable:view'
        }
      },
      {
        path: 'guide',
        name: 'Guide',
        component: SystemGuideView,
        meta: {
          title: '重构说明'
        }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

router.beforeEach((to) => {
  document.title = `${to.meta?.title || '页面'} - ${import.meta.env.VITE_APP_TITLE || '课程排课系统'}`;
  const authStore = useAuthStore();
  if (to.meta?.public) {
    return true;
  }
  if (!authStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    };
  }
  if (!authStore.contextLoaded) {
    return authStore
      .loadAuthContext()
      .then(() => {
        if (to.meta?.permission && !authStore.hasPermission(to.meta.permission) && !authStore.hasMenuPath(to.path)) {
          return authStore.firstAccessiblePath || '/login';
        }
        if (to.path === '/' && authStore.firstAccessiblePath) {
          return authStore.firstAccessiblePath;
        }
        return true;
      })
      .catch(() => {
        authStore.clearLoginState();
        return {
          path: '/login',
          query: {
            redirect: to.fullPath
          }
        };
      });
  }
  if (to.meta?.permission && !authStore.hasPermission(to.meta.permission) && !authStore.hasMenuPath(to.path)) {
    return authStore.firstAccessiblePath || '/login';
  }
  return true;
});

export default router;
