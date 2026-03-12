import axios from 'axios';
import request from '@/api/request';
import { TOKEN_KEY } from '@/constants/storage';

const legacyOptions = {
  baseURL: ''
};

export function fetchTeacherPage(page = 1, limit = 10) {
  return request.get(`/legacy-api/teacher/query/${page}`, {
    ...legacyOptions,
    params: { limit }
  });
}

export function searchTeacherPage(keyword, page = 1, limit = 10) {
  return request.get(`/legacy-api/teacher/search/${page}/${encodeURIComponent(keyword)}`, {
    ...legacyOptions,
    params: { limit }
  });
}

export function fetchTeacherDetail(id) {
  return request.get(`/legacy-api/teacher/${id}`, legacyOptions);
}

export function createTeacher(payload) {
  return request.post('/legacy-api/teacher/add', payload, legacyOptions);
}

export function updateTeacher(payload) {
  return request.post('/legacy-api/teacher/modify', payload, legacyOptions);
}

export function deleteTeacher(id) {
  return request.delete(`/legacy-api/teacher/delete/${id}`, legacyOptions);
}

export function toggleTeacherStatus(id) {
  return request.get(`/legacy-api/teacher/lock/${id}`, legacyOptions);
}

export function fetchNextTeacherNo() {
  return request.get('/legacy-api/teacher/no', legacyOptions);
}

export function fetchStudentPage(page = 1, limit = 10) {
  return request.get(`/legacy-api/student/students/${page}`, {
    ...legacyOptions,
    params: { limit }
  });
}

export function searchStudentPage(keyword, page = 1, limit = 10) {
  return request.get(`/legacy-api/student/search/${encodeURIComponent(keyword)}`, {
    ...legacyOptions,
    params: { page, limit }
  });
}

export function fetchStudentDetail(id) {
  return request.get(`/legacy-api/student/${id}`, legacyOptions);
}

export function createStudent(payload) {
  return request.post('/legacy-api/student/register', payload, legacyOptions);
}

export function updateStudent(id, payload) {
  return request.post(`/legacy-api/student/modify/${id}`, payload, legacyOptions);
}

export function deleteStudent(id) {
  return request.delete(`/legacy-api/student/delete/${id}`, legacyOptions);
}

export function fetchNextStudentNo(grade) {
  return request.post(`/legacy-api/student/createno/${encodeURIComponent(grade)}`, null, legacyOptions);
}

export function fetchCoursePage(page = 1, limit = 10) {
  return request.get(`/legacy-api/courseinfo/${page}`, {
    ...legacyOptions,
    params: { limit }
  });
}

export function searchCoursePage(keyword, page = 1, limit = 10) {
  return request.get(`/legacy-api/courseinfo/search/${page}/${encodeURIComponent(keyword)}`, {
    ...legacyOptions,
    params: { limit }
  });
}

export function createCourse(payload) {
  return request.post('/legacy-api/courseinfo/add', payload, legacyOptions);
}

export function updateCourse(id, payload) {
  return request.post(`/legacy-api/courseinfo/modify/${id}`, payload, legacyOptions);
}

export function deleteCourse(id) {
  return request.delete(`/legacy-api/courseinfo/delete/${id}`, legacyOptions);
}

export function fetchNextCourseNo() {
  return request.get('/legacy-api/courseinfo/get-no', legacyOptions);
}

export function fetchClassroomPage(page = 1, limit = 10) {
  return request.get(`/legacy-api/classroom/${page}`, {
    ...legacyOptions,
    params: { limit }
  });
}

export function fetchClassroomDetail(id) {
  return request.get(`/legacy-api/classroom/query/${id}`, legacyOptions);
}

export function createClassroom(payload) {
  return request.post('/legacy-api/classroom/add', payload, legacyOptions);
}

export function updateClassroom(payload) {
  return request.post('/legacy-api/classroom/modify', payload, legacyOptions);
}

export function deleteClassroom(id) {
  return request.delete(`/legacy-api/classroom/delete/${id}`, legacyOptions);
}

export function fetchTeachbuildList() {
  return request.get('/legacy-api/teachbuildinfo/list', legacyOptions);
}

async function downloadExcel(url, fileName) {
  const token = localStorage.getItem(TOKEN_KEY)?.replaceAll('"', '') || '';
  const response = await axios.get(url, {
    responseType: 'blob',
    headers: token ? { satoken: token } : {}
  });
  const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' });
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
}

export function exportTeacherExcel(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) {
    query.set('keyword', params.keyword);
  }
  if (params.status !== '' && params.status !== null && params.status !== undefined) {
    query.set('status', params.status);
  }
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/teachers/export${query.toString() ? `?${query.toString()}` : ''}`, '教师数据导出.xlsx');
}

export function exportStudentExcel(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) {
    query.set('keyword', params.keyword);
  }
  if (params.status !== '' && params.status !== null && params.status !== undefined) {
    query.set('status', params.status);
  }
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/students/export${query.toString() ? `?${query.toString()}` : ''}`, '学生数据导出.xlsx');
}

export function exportCourseExcel(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) {
    query.set('keyword', params.keyword);
  }
  if (params.status !== '' && params.status !== null && params.status !== undefined) {
    query.set('status', params.status);
  }
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/courses/export${query.toString() ? `?${query.toString()}` : ''}`, '课程数据导出.xlsx');
}

export function downloadTeacherTemplate() {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/teachers/template`, '教师导入模板.xlsx');
}

export function downloadStudentTemplate() {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/students/template`, '学生导入模板.xlsx');
}

export function downloadCourseTemplate() {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/courses/template`, '课程导入模板.xlsx');
}

export function importTeacherExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/base/teachers/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function importStudentExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/base/students/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function importCourseExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/base/courses/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}
