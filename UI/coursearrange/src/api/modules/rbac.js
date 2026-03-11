import request from '@/api/request';

export function fetchUserPage(params) {
  return request.get('/rbac/users/page', { params });
}

export function fetchRoleList(params = {}) {
  return request.get('/rbac/roles/list', { params });
}

export function fetchPermissionList(params = {}) {
  return request.get('/rbac/permissions/list', { params });
}

export function fetchMenuTree() {
  return request.get('/rbac/menus/tree');
}

export function fetchAssignedRoleIds(userId) {
  return request.get(`/rbac/assign/user-roles/${userId}`);
}

export function fetchAssignedMenuIds(roleId) {
  return request.get(`/rbac/assign/role-menus/${roleId}`);
}

export function fetchAssignedPermissionIds(roleId) {
  return request.get(`/rbac/assign/role-permissions/${roleId}`);
}

export function assignUserRoles(payload) {
  return request.post('/rbac/assign/user-roles', payload);
}

export function assignRoleMenus(payload) {
  return request.post('/rbac/assign/role-menus', payload);
}

export function assignRolePermissions(payload) {
  return request.post('/rbac/assign/role-permissions', payload);
}
