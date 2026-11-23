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

/**
 * 分页查询行政区划字典。
 * @param {Object} params 查询参数：pageNum / pageSize / keyword（名称或代码模糊）/ regionLevel（1 省 2 市 3 区县）/ parentCode（父级代码）
 */
export function fetchRegionPage(params = {}) {
  return request.get('/organization/regions/page', { params });
}

/**
 * 查询某个行政区划的直属下级列表，供级联选择复用。
 * @param {string} parentCode 父级区划代码，省级传 '0'
 */
export function fetchRegionChildren(parentCode) {
  return request.get('/organization/regions/children', { params: { parentCode } });
}

/**
 * 管理员手动触发行政区划更新：从配置数据源拉取省市区三级数据并按代码增量写入。
 * @returns {Promise<{data: {inserted: number, updated: number, disabled: number, total: number, sourceUrl: string, syncedAt: string, durationMs: number}}>}
 */
export function syncRegionDivisions() {
  return request.post('/organization/regions/sync');
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
