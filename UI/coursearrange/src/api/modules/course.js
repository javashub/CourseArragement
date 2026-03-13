import axios from 'axios';
import request from '@/api/request';
import { TOKEN_KEY } from '@/constants/storage';

const legacyOptions = {
  baseURL: ''
};

export function fetchSemesterList() {
  return request.get('/schedule/tasks/semesters');
}

export function fetchClassTaskPage(page = 1, semester, limit = 10) {
  return request.get('/schedule/tasks/page', {
    params: {
      semester,
      pageNum: page,
      pageSize: limit
    }
  });
}

export function createClassTask(payload) {
  return request.post('/schedule/tasks', payload);
}

export function deleteClassTask(id) {
  return request.delete(`/schedule/tasks/${id}`);
}

export function arrangeClassTask(semester) {
  return request.post('/schedule/tasks/arrange', null, {
    params: { semester }
  });
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
  return request.get('/schedule/tasks/logs', { params });
}

export function fetchClassInfoPage(page = 1, limit = 200, gradeNo = '') {
  return request.get('/resources/admin-classes/page', {
    params: {
      pageNum: page,
      pageSize: limit,
      gradeNo
    }
  });
}

export function fetchCoursePlanByClassNo(classNo, options = {}) {
  const { semester, ...rest } = options || {};
  return request.get('/schedule/plans/by-class', {
    params: { classNo, semester },
    ...rest
  });
}

export function fetchCoursePlanByTeacherNo(teacherNo, options = {}) {
  const { semester, ...rest } = options || {};
  return request.get('/schedule/plans/by-teacher', {
    params: { teacherNo, semester },
    ...rest
  });
}

export function adjustCoursePlan(payload) {
  return request.post('/schedule/plans/adjust', payload);
}

export function fetchCoursePlanAdjustLogs(params = {}) {
  return request.get('/schedule/plans/adjust-logs', { params });
}
