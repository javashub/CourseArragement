import request from '@/api/request';

export function fetchCampusPage(params = {}) {
  return request.get('/campus/page', { params });
}

export function fetchCampusList() {
  return request.get('/campus/list');
}

export function fetchCampusDetail(id) {
  return request.get(`/campus/${id}`);
}

export function saveCampus(data) {
  return request.post('/campus', data);
}

export function deleteCampus(id) {
  return request.delete(`/campus/${id}`);
}

export function fetchCollegePage(params = {}) {
  return request.get('/college/page', { params });
}

export function fetchCollegeList(params = {}) {
  return request.get('/college/list', { params });
}

export function fetchCollegeDetail(id) {
  return request.get(`/college/${id}`);
}

export function saveCollege(data) {
  return request.post('/college', data);
}

export function deleteCollege(id) {
  return request.delete(`/college/${id}`);
}

export function fetchStageList(params = {}) {
  return request.get('/stage/list', { params });
}

export function fetchStageDetail(id) {
  return request.get(`/stage/${id}`);
}

export function saveStage(data) {
  return request.post('/stage', data);
}

export function deleteStage(id) {
  return request.delete(`/stage/${id}`);
}

export function fetchFeatureToggles(params = {}) {
  return request.get('/config/feature-toggles', { params });
}

export function fetchScheduleConfig(params = {}) {
  return request.get('/config/schedule-rules/active', { params });
}

export function saveScheduleConfig(data) {
  if (data?.id) {
    return request.put(`/config/schedule-rules/${data.id}`, data);
  }
  return request.post('/config/schedule-rules', data);
}

export function saveFeatureToggles(data) {
  return request.put('/config/feature-toggles', data);
}

export function saveTimeSlots(data) {
  return request.put(`/config/schedule-rules/${data.scheduleRuleId}/time-slots`, data);
}

export function fetchDictTypes() {
  return request.get('/dict/types');
}
