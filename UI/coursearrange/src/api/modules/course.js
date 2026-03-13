import axios from 'axios';
import request from '@/api/request';
import { TOKEN_KEY } from '@/constants/storage';

const legacyOptions = {
  baseURL: ''
};

export function fetchSemesterList() {
  return request.get('/legacy-api/semester', legacyOptions);
}

export function fetchClassTaskPage(page = 1, semester, limit = 10) {
  return request.get(`/legacy-api/classtask/${page}/${encodeURIComponent(semester)}`, {
    ...legacyOptions,
    params: { limit }
  });
}

export function createClassTask(payload) {
  return request.post('/legacy-api/addclasstask', payload, legacyOptions);
}

export function deleteClassTask(id) {
  return request.delete(`/legacy-api/deleteclasstask/${id}`, legacyOptions);
}

export function arrangeClassTask(semester) {
  return request.post(`/legacy-api/arrange/${encodeURIComponent(semester)}`, null, legacyOptions);
}

export function uploadClassTaskExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/class-task/import', formData, {
    meta: {
      silentError: true
    },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export async function downloadClassTaskTemplate() {
  const token = localStorage.getItem(TOKEN_KEY)?.replaceAll('"', '') || '';
  const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL || '/api'}/excel/class-task/template`, {
    responseType: 'blob',
    headers: token ? { satoken: token } : {}
  });
  const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' });
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = '课程任务导入模板.xlsx';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
}

export function fetchArrangeLogs(params = {}) {
  return request.get('/legacy-api/arrange/logs', {
    ...legacyOptions,
    params
  });
}

export function fetchClassInfoPage(page = 1, limit = 200, gradeNo = '') {
  return request.get(`/legacy-api/queryclassinfo/${page}`, {
    ...legacyOptions,
    params: { limit, gradeNo }
  });
}

export function fetchCoursePlanByClassNo(classNo, options = {}) {
  return request.get(`/legacy-api/courseplan/${encodeURIComponent(classNo)}`, {
    ...legacyOptions,
    ...options
  });
}

export function fetchCoursePlanByTeacherNo(teacherNo, options = {}) {
  return request.get(`/legacy-api/courseplan/teacher/${encodeURIComponent(teacherNo)}`, {
    ...legacyOptions,
    ...options
  });
}

export function adjustCoursePlan(payload) {
  return request.post('/legacy-api/courseplan/adjust', payload, legacyOptions);
}

export function fetchCoursePlanAdjustLogs(params = {}) {
  return request.get('/legacy-api/courseplan/adjust/logs', {
    ...legacyOptions,
    params
  });
}
