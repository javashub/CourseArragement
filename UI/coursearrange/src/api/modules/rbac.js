import request from '@/api/request';

export function fetchUserPage(params) {
  return request.get('/rbac/users/page', { params });
}

export function fetchRoleList(params = {}) {
  return request.get('/rbac/roles/list', { params });
}

export function saveRole(payload) {
  return request.post('/rbac/roles', payload);
}

export function changeRoleStatus(roleId, status) {
  return request.post(`/rbac/roles/${roleId}/status/${status}`);
}

export function deleteRole(roleId) {
  return request.delete(`/rbac/roles/${roleId}`);
}

export function fetchPermissionList(params = {}) {
  return request.get('/rbac/permissions/list', { params });
}

export function fetchMenuTree() {
  return request.get('/rbac/menus/tree');
}

export function saveMenu(payload) {
  return request.post('/rbac/menus', payload);
}

export function changeMenuStatus(menuId, status) {
  return request.post(`/rbac/menus/${menuId}/status/${status}`);
}

export function deleteMenu(menuId) {
  return request.delete(`/rbac/menus/${menuId}`);
}

export function savePermission(payload) {
  return request.post('/rbac/permissions', payload);
}

export function changePermissionStatus(permissionId, status) {
  return request.post(`/rbac/permissions/${permissionId}/status/${status}`);
}

export function deletePermission(permissionId) {
  return request.delete(`/rbac/permissions/${permissionId}`);
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
