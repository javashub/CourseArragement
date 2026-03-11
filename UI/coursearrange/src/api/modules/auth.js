import request from '@/api/request';

export function loginByAdmin(payload) {
  return request.post('/legacy-api/admin/login', payload, {
    baseURL: ''
  });
}

export function loginByTeacher(payload) {
  return request.post('/legacy-api/teacher/login', payload, {
    baseURL: ''
  });
}

export function loginByStudent(payload) {
  return request.post('/legacy-api/student/login', payload, {
    baseURL: ''
  });
}

export function fetchAuthContext() {
  return request.get('/auth/context');
}
