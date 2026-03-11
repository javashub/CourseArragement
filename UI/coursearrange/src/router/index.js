import { createRouter, createWebHashHistory } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';
import LoginView from '@/views/auth/LoginView.vue';
import DashboardView from '@/views/dashboard/DashboardView.vue';
import CoursePlanView from '@/views/course/CoursePlanView.vue';
import ScheduleView from '@/views/course/ScheduleView.vue';
import BaseDataView from '@/views/base/BaseDataView.vue';
import SystemGuideView from '@/views/system/SystemGuideView.vue';
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
          title: '工作台'
        }
      },
      {
        path: 'course-plan',
        name: 'CoursePlan',
        component: CoursePlanView,
        meta: {
          title: '排课任务'
        }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: ScheduleView,
        meta: {
          title: '课表管理'
        }
      },
      {
        path: 'base-data',
        name: 'BaseData',
        component: BaseDataView,
        meta: {
          title: '基础数据'
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
  return true;
});

export default router;
