import request from '@/api/request';

export function loginByAdmin(payload) {
  return request.post('/admin/login', payload);
}

export function loginByTeacher(payload) {
  return request.post('/teacher/login', payload);
}

export function loginByStudent(payload) {
  return request.post('/student/login', payload);
}
