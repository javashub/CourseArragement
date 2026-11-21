import request from '@/api/request';

// 工作台聚合接口：只读，受 page:dashboard:view 保护。
export function fetchDashboardOverview(params = {}) {
  return request.get('/dashboard/overview', {
    params,
    meta: { silentError: true }
  });
}
