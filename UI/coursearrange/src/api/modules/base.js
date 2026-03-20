import axios from 'axios';
import request from '@/api/request';
import { TOKEN_KEY } from '@/constants/storage';

const legacyOptions = {
  baseURL: ''
};

function normalizeForbiddenTimeSlots(rawValue) {
  if (!Array.isArray(rawValue)) {
    return [];
  }
  return [...new Set(rawValue
    .map((item) => String(item ?? '').trim())
    .filter((item) => /^\d{1,2}$/.test(item))
    .map((item) => item.padStart(2, '0')))];
}

function parseTeacherRemark(remark) {
  if (!remark || typeof remark !== 'string') {
    return {
      teach: '',
      forbiddenTimeSlots: []
    };
  }
  const trimmedRemark = remark.trim();
  if (!trimmedRemark) {
    return {
      teach: '',
      forbiddenTimeSlots: []
    };
  }
  try {
    const payload = JSON.parse(trimmedRemark);
    return {
      teach: String(payload?.teach ?? '').trim(),
      forbiddenTimeSlots: normalizeForbiddenTimeSlots(payload?.forbiddenTimeSlots)
    };
  } catch (error) {
    return {
      teach: trimmedRemark,
      forbiddenTimeSlots: []
    };
  }
}

function buildTeacherRemark(payload = {}) {
  const teach = String(payload.teach ?? '').trim();
  const forbiddenTimeSlots = normalizeForbiddenTimeSlots(payload.forbiddenTimeSlots);
  if (!forbiddenTimeSlots.length) {
    return teach;
  }
  return JSON.stringify({
    teach,
    forbiddenTimeSlots
  });
}

function normalizeCourseRecord(record = {}) {
  return {
    ...record,
    courseNo: record.courseNo ?? record.courseCode ?? '',
    courseCode: record.courseCode ?? record.courseNo ?? '',
    courseAttr: record.courseAttr ?? record.courseType ?? '',
    courseType: record.courseType ?? record.courseAttr ?? '',
    publisher: record.publisher ?? record.courseShortName ?? '',
    courseShortName: record.courseShortName ?? record.publisher ?? '',
    piority: record.piority ?? record.weekHours ?? 0,
    weekHours: record.weekHours ?? record.piority ?? 0,
    needContinuous: Number(record.needContinuous ?? 0) || 0,
    continuousSize: Number(record.continuousSize ?? 1) || 1
  };
}

function normalizeTeacherRecord(record = {}) {
  const teacherRemark = parseTeacherRemark(record.remark ?? record.teach ?? '');
  return {
    ...record,
    teacherNo: record.teacherNo ?? record.teacherCode ?? '',
    teacherCode: record.teacherCode ?? record.teacherNo ?? '',
    realname: record.realname ?? record.teacherName ?? '',
    teacherName: record.teacherName ?? record.realname ?? '',
    telephone: record.telephone ?? record.mobile ?? '',
    mobile: record.mobile ?? record.telephone ?? '',
    jobtitle: record.jobtitle ?? record.titleName ?? '',
    titleName: record.titleName ?? record.jobtitle ?? '',
    teach: record.teach ?? teacherRemark.teach ?? '',
    forbiddenTimeSlots: teacherRemark.forbiddenTimeSlots,
    forbiddenTimeSlotsText: teacherRemark.forbiddenTimeSlots.join(', '),
    status: record.status ?? 1
  };
}

function normalizeStudentRecord(record = {}) {
  return {
    ...record,
    studentNo: record.studentNo ?? record.studentCode ?? '',
    studentCode: record.studentCode ?? record.studentNo ?? '',
    realname: record.realname ?? record.studentName ?? '',
    studentName: record.studentName ?? record.realname ?? '',
    telephone: record.telephone ?? record.mobile ?? '',
    mobile: record.mobile ?? record.telephone ?? '',
    grade: record.grade ?? (record.entryYear ? `${record.entryYear}级` : ''),
    classNo: record.classNo ?? '',
    status: record.status ?? 1
  };
}

function normalizeTeacherPageResponse(response) {
  return {
    ...response,
    data: {
      ...(response.data || {}),
      records: (response.data?.records || []).map(normalizeTeacherRecord)
    }
  };
}

function normalizeStudentPageResponse(response) {
  return {
    ...response,
    data: {
      ...(response.data || {}),
      records: (response.data?.records || []).map(normalizeStudentRecord)
    }
  };
}

function normalizeCoursePageResponse(response) {
  return {
    ...response,
    data: {
      ...(response.data || {}),
      records: (response.data?.records || []).map(normalizeCourseRecord)
    }
  };
}

function normalizeClassroomRecord(record = {}) {
  return {
    ...record,
    classroomNo: record.classroomNo ?? record.classroomCode ?? '',
    classroomCode: record.classroomCode ?? record.classroomNo ?? '',
    teachbuildNo: record.teachbuildNo ?? record.buildingCode ?? '',
    buildingCode: record.buildingCode ?? record.teachbuildNo ?? '',
    teachbuildName: record.teachbuildName ?? record.buildingName ?? '',
    buildingName: record.buildingName ?? record.teachbuildName ?? '',
    capacity: record.capacity ?? record.seatCount ?? 0,
    seatCount: record.seatCount ?? record.capacity ?? 0
  };
}

function normalizeClassroomPageResponse(response) {
  return {
    ...response,
    data: {
      ...(response.data || {}),
      records: (response.data?.records || []).map(normalizeClassroomRecord)
    }
  };
}

function buildTeacherPayload(payload = {}) {
  return {
    id: payload.id,
    teacherCode: payload.teacherCode ?? payload.teacherNo ?? '',
    teacherName: payload.teacherName ?? payload.realname ?? '',
    gender: payload.gender ?? '',
    mobile: payload.mobile ?? payload.telephone ?? '',
    email: payload.email ?? '',
    titleName: payload.titleName ?? payload.jobtitle ?? '',
    allowCrossCampus: Number(payload.allowCrossCampus ?? 0) || 0,
    allowCrossCollege: Number(payload.allowCrossCollege ?? 0) || 0,
    maxWeekHours: Number(payload.maxWeekHours ?? 16) || 0,
    maxDayHours: Number(payload.maxDayHours ?? 4) || 0,
    hireStatus: payload.hireStatus ?? 'ACTIVE',
    status: payload.status ?? 1,
    remark: buildTeacherRemark(payload)
  };
}

function buildStudentPayload(payload = {}) {
  return {
    id: payload.id,
    studentCode: payload.studentCode ?? payload.studentNo ?? '',
    studentName: payload.studentName ?? payload.realname ?? '',
    gender: payload.gender ?? '',
    mobile: payload.mobile ?? payload.telephone ?? '',
    email: payload.email ?? '',
    entryYear: Number(payload.entryYear ?? (payload.grade ? String(payload.grade).replaceAll(/[^0-9]/g, '').slice(0, 4) : '')) || new Date().getFullYear(),
    status: payload.status ?? 1,
    remark: payload.remark ?? payload.classNo ?? ''
  };
}

function buildCoursePayload(payload = {}) {
  const weekHours = Number(payload.weekHours ?? payload.piority ?? 0) || 0;
  return {
    id: payload.id,
    courseCode: payload.courseCode ?? payload.courseNo ?? '',
    courseName: payload.courseName ?? '',
    courseShortName: payload.courseShortName ?? payload.publisher ?? '',
    courseType: payload.courseType ?? payload.courseAttr ?? 'REQUIRED',
    totalHours: Number(payload.totalHours ?? weekHours * 16) || 0,
    weekHours,
    needContinuous: Number(payload.needContinuous ?? 0) || 0,
    continuousSize: Number(payload.continuousSize ?? 1) || 1,
    needSpecialRoom: Number(payload.needSpecialRoom ?? 0) || 0,
    roomType: payload.roomType ?? 'NORMAL',
    status: payload.status ?? 1,
    remark: payload.remark ?? ''
  };
}

export function fetchTeacherPage(page = 1, limit = 10) {
  return request.get('/resources/teachers/page', {
    params: { pageNum: page, pageSize: limit }
  }).then(normalizeTeacherPageResponse);
}

export function searchTeacherPage(keyword, page = 1, limit = 10) {
  return request.get('/resources/teachers/page', {
    params: { keyword, pageNum: page, pageSize: limit }
  }).then(normalizeTeacherPageResponse);
}

export function fetchTeacherDetail(id) {
  return request.get(`/resources/teachers/${id}`).then((response) => ({
    ...response,
    data: normalizeTeacherRecord(response.data)
  }));
}

export function createTeacher(payload) {
  return request.post('/resources/teachers', buildTeacherPayload(payload));
}

export function updateTeacher(payload) {
  return request.post('/resources/teachers', buildTeacherPayload(payload));
}

export function deleteTeacher(id) {
  return request.delete(`/resources/teachers/${id}`);
}

export function toggleTeacherStatus(id, status) {
  return request.post(`/resources/teachers/${id}/status/${status}`);
}

export function fetchNextTeacherNo() {
  return request.get('/resources/teachers/next-code');
}

export function fetchStudentPage(page = 1, limit = 10) {
  return request.get('/resources/students/page', {
    params: { pageNum: page, pageSize: limit }
  }).then(normalizeStudentPageResponse);
}

export function searchStudentPage(keyword, page = 1, limit = 10) {
  return request.get('/resources/students/page', {
    params: { keyword, pageNum: page, pageSize: limit }
  }).then(normalizeStudentPageResponse);
}

export function fetchStudentDetail(id) {
  return request.get(`/resources/students/${id}`).then((response) => ({
    ...response,
    data: normalizeStudentRecord(response.data)
  }));
}

export function createStudent(payload) {
  return request.post('/resources/students', buildStudentPayload(payload));
}

export function updateStudent(id, payload) {
  return request.post('/resources/students', buildStudentPayload({ ...payload, id }));
}

export function deleteStudent(id) {
  return request.delete(`/resources/students/${id}`);
}

export function fetchNextStudentNo(grade) {
  const entryYear = Number(String(grade || '').replaceAll(/[^0-9]/g, '').slice(0, 4)) || new Date().getFullYear();
  return request.get('/resources/students/next-code', {
    params: { entryYear }
  });
}

export function fetchCoursePage(page = 1, limit = 10) {
  return request.get('/resources/courses/page', {
    params: { pageNum: page, pageSize: limit }
  }).then(normalizeCoursePageResponse);
}

export function searchCoursePage(keyword, page = 1, limit = 10) {
  return request.get('/resources/courses/page', {
    params: { keyword, pageNum: page, pageSize: limit }
  }).then(normalizeCoursePageResponse);
}

export function createCourse(payload) {
  return request.post('/resources/courses', buildCoursePayload(payload));
}

export function updateCourse(id, payload) {
  return request.post('/resources/courses', buildCoursePayload({ ...payload, id }));
}

export function deleteCourse(id) {
  return request.delete(`/resources/courses/${id}`);
}

export function fetchNextCourseNo() {
  return request.get('/resources/courses/next-code');
}

export function fetchClassroomPage(page = 1, limit = 10, params = {}) {
  return request.get('/resources/classrooms/page', {
    params: { pageNum: page, pageSize: limit, ...params }
  }).then(normalizeClassroomPageResponse);
}

export function fetchClassroomDetail(id) {
  return request.get(`/resources/classrooms/${id}`).then((response) => ({
    ...response,
    data: normalizeClassroomRecord(response.data)
  }));
}

export function createClassroom(payload) {
  return request.post('/resources/classrooms', {
    id: payload.id,
    classroomCode: payload.classroomCode ?? payload.classroomNo ?? '',
    classroomName: payload.classroomName ?? '',
    buildingCode: payload.buildingCode ?? payload.teachbuildNo ?? '',
    seatCount: Number(payload.seatCount ?? payload.capacity ?? 0) || 0,
    roomType: payload.roomType ?? 'NORMAL',
    status: payload.status ?? 1,
    remark: payload.remark ?? ''
  });
}

export function updateClassroom(payload) {
  return createClassroom(payload);
}

export function deleteClassroom(id) {
  return request.delete(`/resources/classrooms/${id}`);
}

export function fetchLegacyClassInfoPage(page = 1, limit = 10, gradeNo = '') {
  return request.get(`/queryclassinfo/${page}`, {
    params: { limit, gradeNo }
  });
}

export function createLegacyClassInfo(payload) {
  return request.post('/addclassinfo', payload);
}

export function fetchStudentsByClassPage(page = 1, classNo, limit = 10) {
  return request.get(`/student-class/${page}/${classNo}`, {
    params: { limit }
  }).then(normalizeStudentPageResponse);
}

export function fetchTeachbuildList() {
  return request.get('/resources/buildings/options');
}

export function fetchLegacyTeachbuildPage(page = 1, limit = 10) {
  return request.get(`/teachbuildinfo/list/${page}`, {
    params: { limit }
  });
}

export function createLegacyTeachbuild(payload) {
  return request.post('/teachbuildinfo/add', payload);
}

export function updateLegacyTeachbuild(id, payload) {
  return request.post(`/teachbuildinfo/modify/${id}`, payload);
}

export function deleteLegacyTeachbuild(id) {
  return request.delete(`/teachbuildinfo/delete/${id}`);
}

export function fetchLegacyTeachbuildList() {
  return request.get('/teachbuildinfo/list');
}

export function fetchEmptyClassroomList(teachbuildNo) {
  return request.get(`/classroom/empty/${teachbuildNo}`).then((response) => ({
    ...response,
    data: (response.data || []).map(normalizeClassroomRecord)
  }));
}

export function fetchLegacyLocationPage(page = 1, limit = 10) {
  return request.get(`/locations/${page}`, {
    params: { limit }
  });
}

export function createLegacyTeachArea(payload) {
  return request.post('/setteacharea', payload);
}

export function deleteLegacyTeachArea(id) {
  return request.delete(`/location/delete/${id}`);
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
    meta: {
      silentError: true
    },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function importStudentExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/base/students/import', formData, {
    meta: {
      silentError: true
    },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function importCourseExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/base/courses/import', formData, {
    meta: {
      silentError: true
    },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function downloadTeachbuildTemplate() {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/teachbuilds/template`, '教学楼导入模板.xlsx');
}

export function downloadClassroomTemplate() {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/classrooms/template`, '教室导入模板.xlsx');
}

export function exportTeachbuildExcel(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) {
    query.set('keyword', params.keyword);
  }
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/teachbuilds/export${query.toString() ? `?${query.toString()}` : ''}`, '教学楼数据导出.xlsx');
}

export function exportClassroomExcel(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) {
    query.set('keyword', params.keyword);
  }
  if (params.teachbuildNo) {
    query.set('teachbuildNo', params.teachbuildNo);
  }
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
  return downloadExcel(`${baseURL}/excel/base/classrooms/export${query.toString() ? `?${query.toString()}` : ''}`, '教室数据导出.xlsx');
}

export function importTeachbuildExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/base/teachbuilds/import', formData, {
    meta: {
      silentError: true
    },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function importClassroomExcel(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/excel/base/classrooms/import', formData, {
    meta: {
      silentError: true
    },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}
