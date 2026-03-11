import request from '@/api/request';

export function fetchCampusList() {
  return request.get('/campus/list');
}

export function fetchCollegeList(params = {}) {
  return request.get('/college/list', { params });
}

export function fetchStageList(params = {}) {
  return request.get('/stage/list', { params });
}

export function fetchFeatureToggles(params = {}) {
  return request.get('/config/features', { params });
}

export function fetchScheduleConfig(params = {}) {
  return request.get('/config/schedule', { params });
}

export function fetchDictTypes() {
  return request.get('/dict/types');
}
