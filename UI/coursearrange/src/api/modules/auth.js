import request from '@/api/request';

export function login(payload) {
  return request.post('/auth/login', payload);
}

export function fetchAuthContext() {
  return request.get('/auth/context');
}
