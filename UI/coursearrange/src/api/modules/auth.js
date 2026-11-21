import request from '@/api/request';

export function login(payload) {
  return request.post('/auth/login', payload);
}

export function fetchAuthContext() {
  return request.get('/auth/context');
}

// 账号级主题偏好：失败时不阻断本次切换，由调用方给轻提示。
export function saveUiTheme(payload) {
  return request.put('/auth/ui-theme', payload, { meta: { silentError: true } });
}
