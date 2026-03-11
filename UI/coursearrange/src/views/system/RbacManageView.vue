<template>
  <section class="page-card" v-loading="loading">
    <h1 class="page-title">权限管理</h1>
    <p class="page-description">当前页面已接通用户、角色、菜单、权限分配接口，可直接用于联调 RBAC3 基础闭环。</p>

    <el-row :gutter="16" class="rbac-layout">
      <el-col :span="8">
        <el-card shadow="never" class="rbac-card">
          <template #header>系统用户</template>
          <div class="filter-row">
            <el-input
              v-model="userKeyword"
              placeholder="按账号/姓名搜索"
              clearable
              @keyup.enter="handleUserSearch"
              @clear="handleUserSearchClear"
            />
            <el-select
              v-model="userTypeFilter"
              placeholder="用户类型"
              clearable
              @change="handleUserSearch"
              @clear="handleUserTypeClear"
            >
              <el-option label="管理员" value="ADMIN" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </div>
          <el-table
            class="rbac-table"
            :data="users"
            size="small"
            highlight-current-row
            @current-change="handleUserChange"
          >
            <el-table-column prop="username" label="账号" min-width="120" />
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column prop="userType" label="类型" width="100" />
          </el-table>
          <div class="rbac-pagination">
            <el-pagination
              layout="prev, pager, next"
              :total="userTotal"
              :current-page="userPageNum"
              :page-size="userPageSize"
              @current-change="handleUserPageChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-row :gutter="16">
          <el-col :span="10">
            <el-card shadow="never" class="rbac-card">
              <template #header>
                <div class="card-header">
                  <span>角色分配</span>
                  <el-button type="primary" link @click="openRoleDialog()">新增角色</el-button>
                </div>
              </template>
              <div class="role-maintenance-list">
                <div v-for="role in roles" :key="role.id" class="role-maintenance-item">
                  <div>
                    <div class="role-maintenance-title">{{ role.roleName }}</div>
                    <div class="role-maintenance-meta">
                      {{ role.roleCode }} · {{ role.roleType }} · {{ role.status === 1 ? '启用' : '停用' }}
                    </div>
                  </div>
                  <div class="role-maintenance-actions">
                    <el-button type="primary" link @click="openRoleDialog(role)">编辑</el-button>
                    <el-button
                      :type="role.status === 1 ? 'warning' : 'success'"
                      link
                      @click="handleToggleRoleStatus(role)"
                    >
                      {{ role.status === 1 ? '停用' : '启用' }}
                    </el-button>
                    <el-button type="danger" link @click="handleDeleteRole(role)">删除</el-button>
                  </div>
                </div>
              </div>
              <div class="section-hint" v-if="selectedUser">
                当前用户：{{ selectedUser.realName || selectedUser.username }}
              </div>
              <el-empty v-if="!selectedUser" description="请选择左侧用户" />
              <template v-else>
                <el-checkbox-group v-model="selectedRoleIds" class="checkbox-group">
                  <el-checkbox v-for="role in roles" :key="role.id" :value="role.id">
                    {{ role.roleName }}（{{ role.roleCode }}）
                  </el-checkbox>
                </el-checkbox-group>
                <div class="action-row">
                  <el-button type="primary" @click="handleSaveUserRoles">保存角色分配</el-button>
                </div>
              </template>
            </el-card>
          </el-col>

          <el-col :span="14">
            <el-card shadow="never" class="rbac-card">
              <template #header>角色资源授权</template>
              <div class="section-hint" v-if="selectedRole">
                当前角色：{{ selectedRole.roleName }}（{{ selectedRole.roleCode }}）
              </div>
              <el-radio-group v-model="selectedRoleId" class="role-radio-group" @change="handleRoleChange">
                <el-radio-button v-for="role in roles" :key="role.id" :value="role.id">
                  {{ role.roleName }}
                </el-radio-button>
              </el-radio-group>
              <el-empty v-if="!selectedRoleId" description="请选择一个角色" />
              <template v-else>
                <div class="resource-panels">
                  <el-card shadow="never" class="resource-card">
                    <template #header>
                      <div class="card-header">
                        <span>菜单授权</span>
                        <el-button type="primary" link @click="openMenuDialog()">维护菜单</el-button>
                      </div>
                    </template>
                    <el-tree
                      ref="menuTreeRef"
                      node-key="id"
                      show-checkbox
                      :data="menuTree"
                      :props="menuTreeProps"
                      default-expand-all
                    />
                    <div class="action-row">
                      <el-button type="primary" @click="handleSaveRoleMenus">保存菜单授权</el-button>
                    </div>
                  </el-card>
                  <el-card shadow="never" class="resource-card">
                    <template #header>
                      <div class="card-header">
                        <span>权限点授权</span>
                        <el-button type="primary" link @click="openPermissionDialog()">维护权限点</el-button>
                      </div>
                    </template>
                    <el-checkbox-group v-model="selectedPermissionIds" class="checkbox-group permission-group">
                      <el-checkbox v-for="permission in permissions" :key="permission.id" :value="permission.id">
                        {{ permission.permissionName }}（{{ permission.permissionCode }}）
                      </el-checkbox>
                    </el-checkbox-group>
                    <div class="action-row">
                      <el-button type="primary" @click="handleSaveRolePermissions">保存权限授权</el-button>
                    </div>
                  </el-card>
                </div>
              </template>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </section>

  <el-dialog v-model="roleDialogVisible" :title="roleForm.id ? '编辑角色' : '新增角色'" width="520px">
    <el-form :model="roleForm" label-position="top">
      <el-form-item label="角色编码">
        <el-input v-model="roleForm.roleCode" placeholder="例如 ADMIN" />
      </el-form-item>
      <el-form-item label="角色名称">
        <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="角色类型">
            <el-input v-model="roleForm.roleType" placeholder="SYSTEM / BUSINESS" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="数据范围">
            <el-input v-model="roleForm.dataScopeType" placeholder="ALL / CAMPUS / COLLEGE" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="排序">
            <el-input-number v-model="roleForm.sortNo" :min="1" :max="999" class="full-width" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="roleForm.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="roleForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roleDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSaveRole">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="menuDialogVisible" :title="menuForm.id ? '编辑菜单' : '新增菜单'" width="900px">
    <div class="dialog-toolbar">
      <el-input
        v-model="menuKeyword"
        placeholder="按菜单名称/编码搜索"
        clearable
        @keyup.enter="handleMenuSearch"
        @clear="handleMenuSearchClear"
      />
      <el-select
        v-model="menuStatusFilter"
        placeholder="菜单状态"
        clearable
        @change="handleMenuSearch"
        @clear="handleMenuStatusClear"
      >
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
    </div>
    <el-table :data="filteredMenuList" size="small" max-height="260" class="dialog-table">
      <el-table-column prop="menuName" label="菜单名称" min-width="140" />
      <el-table-column prop="menuCode" label="菜单编码" min-width="140" />
      <el-table-column prop="menuType" label="类型" width="100" />
      <el-table-column prop="routePath" label="路由" min-width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="editMenu(row)">编辑</el-button>
          <el-button type="warning" link @click="handleToggleMenuStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button type="danger" link @click="handleDeleteMenu(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-divider />
    <el-form :model="menuForm" label-position="top">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="菜单编码">
            <el-input v-model="menuForm.menuCode" placeholder="例如 dashboard" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜单名称">
            <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="父级菜单">
            <el-select v-model="menuForm.parentId" placeholder="请选择父级">
              <el-option label="根节点" :value="0" />
              <el-option v-for="item in menuParentOptions" :key="item.id" :label="item.menuName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="菜单类型">
            <el-select v-model="menuForm.menuType" placeholder="请选择类型">
              <el-option label="目录" value="CATALOG" />
              <el-option label="菜单" value="MENU" />
              <el-option label="按钮" value="BUTTON" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-radio-group v-model="menuForm.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="路由名称">
            <el-input v-model="menuForm.routeName" placeholder="请输入 route name" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="路由路径">
            <el-input v-model="menuForm.routePath" placeholder="请输入 route path" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="组件路径">
            <el-input v-model="menuForm.componentPath" placeholder="请输入组件路径" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="权限编码">
            <el-input v-model="menuForm.permissionCode" placeholder="请输入权限编码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="图标">
            <el-input v-model="menuForm.icon" placeholder="例如 House" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否隐藏">
            <el-radio-group v-model="menuForm.isHidden">
              <el-radio :value="0">否</el-radio>
              <el-radio :value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否缓存">
            <el-radio-group v-model="menuForm.isKeepAlive">
              <el-radio :value="0">否</el-radio>
              <el-radio :value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="menuForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="menuDialogVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleSaveMenu">保存菜单</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="permissionDialogVisible" :title="permissionForm.id ? '编辑权限点' : '新增权限点'" width="900px">
    <div class="dialog-toolbar">
      <el-input
        v-model="permissionKeyword"
        placeholder="按权限名称/编码搜索"
        clearable
        @keyup.enter="handlePermissionSearch"
        @clear="handlePermissionSearchClear"
      />
      <el-select
        v-model="permissionTypeFilter"
        placeholder="权限类型"
        clearable
        @change="handlePermissionSearch"
        @clear="handlePermissionTypeClear"
      >
        <el-option label="页面" value="PAGE" />
        <el-option label="按钮" value="BUTTON" />
        <el-option label="接口" value="API" />
      </el-select>
      <el-select
        v-model="permissionStatusFilter"
        placeholder="权限状态"
        clearable
        @change="handlePermissionSearch"
        @clear="handlePermissionStatusClear"
      >
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
    </div>
    <el-table :data="filteredPermissions" size="small" max-height="260" class="dialog-table">
      <el-table-column prop="permissionName" label="权限名称" min-width="160" />
      <el-table-column prop="permissionCode" label="权限编码" min-width="160" />
      <el-table-column prop="permissionType" label="类型" width="100" />
      <el-table-column prop="resourcePath" label="资源路径" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="editPermission(row)">编辑</el-button>
          <el-button type="warning" link @click="handleTogglePermissionStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button type="danger" link @click="handleDeletePermission(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-divider />
    <el-form :model="permissionForm" label-position="top">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="权限编码">
            <el-input v-model="permissionForm.permissionCode" placeholder="例如 page:dashboard:view" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="权限名称">
            <el-input v-model="permissionForm.permissionName" placeholder="请输入权限名称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="权限类型">
            <el-select v-model="permissionForm.permissionType" placeholder="请选择类型">
              <el-option label="页面" value="PAGE" />
              <el-option label="按钮" value="BUTTON" />
              <el-option label="接口" value="API" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="请求方法">
            <el-input v-model="permissionForm.httpMethod" placeholder="GET / POST" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-radio-group v-model="permissionForm.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="资源路径">
        <el-input v-model="permissionForm.resourcePath" placeholder="例如 /api/rbac/users/page" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="permissionForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="permissionDialogVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleSavePermission">保存权限点</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  assignRoleMenus,
  assignRolePermissions,
  assignUserRoles,
  changeRoleStatus,
  changeMenuStatus,
  changePermissionStatus,
  deleteRole,
  deleteMenu,
  deletePermission,
  fetchAssignedMenuIds,
  fetchAssignedPermissionIds,
  fetchAssignedRoleIds,
  fetchMenuTree,
  fetchPermissionList,
  fetchRoleList,
  fetchUserPage,
  saveMenu,
  savePermission,
  saveRole
} from '@/api/modules/rbac';

const loading = ref(false);
const users = ref([]);
const userKeyword = ref('');
const userTypeFilter = ref('');
const userPageNum = ref(1);
const userPageSize = ref(10);
const userTotal = ref(0);
const roles = ref([]);
const permissions = ref([]);
const menuTree = ref([]);
const selectedUser = ref(null);
const selectedRoleId = ref(null);
const selectedRoleIds = ref([]);
const selectedPermissionIds = ref([]);
const menuTreeRef = ref();
const roleDialogVisible = ref(false);
const roleForm = ref(createDefaultRoleForm());
const menuDialogVisible = ref(false);
const menuForm = ref(createDefaultMenuForm());
const menuKeyword = ref('');
const menuStatusFilter = ref('');
const permissionDialogVisible = ref(false);
const permissionForm = ref(createDefaultPermissionForm());
const permissionKeyword = ref('');
const permissionTypeFilter = ref('');
const permissionStatusFilter = ref('');

const menuTreeProps = {
  children: 'children',
  label: 'menuName'
};

const selectedRole = computed(() => roles.value.find((item) => item.id === selectedRoleId.value) || null);
const flatMenuList = computed(() => flattenMenus(menuTree.value));
const menuParentOptions = computed(() =>
  flatMenuList.value.filter((item) => item.id !== menuForm.value.id && item.menuType !== 'BUTTON')
);
const filteredMenuList = computed(() =>
  flatMenuList.value.filter((item) => {
    const keywordMatched =
      !menuKeyword.value ||
      item.menuName?.includes(menuKeyword.value) ||
      item.menuCode?.includes(menuKeyword.value);
    const statusMatched = menuStatusFilter.value === '' || item.status === menuStatusFilter.value;
    return keywordMatched && statusMatched;
  })
);
const filteredPermissions = computed(() =>
  permissions.value.filter((item) => {
    const keywordMatched =
      !permissionKeyword.value ||
      item.permissionName?.includes(permissionKeyword.value) ||
      item.permissionCode?.includes(permissionKeyword.value);
    const typeMatched = !permissionTypeFilter.value || item.permissionType === permissionTypeFilter.value;
    const statusMatched = permissionStatusFilter.value === '' || item.status === permissionStatusFilter.value;
    return keywordMatched && typeMatched && statusMatched;
  })
);

async function loadUsers() {
  const response = await fetchUserPage({
    pageNum: userPageNum.value,
    pageSize: userPageSize.value,
    keyword: userKeyword.value,
    userType: userTypeFilter.value
  });
  const pageData = response.data || {};
  users.value = pageData.records || [];
  userTotal.value = pageData.total || 0;
}

function handleUserSearch() {
  userPageNum.value = 1;
  loadUsers();
}

function handleUserSearchClear() {
  userKeyword.value = '';
  userPageNum.value = 1;
  loadUsers();
}

function handleUserTypeClear() {
  userTypeFilter.value = '';
  userPageNum.value = 1;
  loadUsers();
}

function handleMenuSearch() {}

function handleMenuSearchClear() {
  menuKeyword.value = '';
}

function handleMenuStatusClear() {
  menuStatusFilter.value = '';
}

function handlePermissionSearch() {}

function handlePermissionSearchClear() {
  permissionKeyword.value = '';
}

function handlePermissionTypeClear() {
  permissionTypeFilter.value = '';
}

function handlePermissionStatusClear() {
  permissionStatusFilter.value = '';
}

async function loadRoles() {
  const response = await fetchRoleList();
  roles.value = response.data || [];
  if (!selectedRoleId.value && roles.value.length > 0) {
    selectedRoleId.value = roles.value[0].id;
  }
  if (selectedRoleId.value && !roles.value.some((item) => item.id === selectedRoleId.value)) {
    selectedRoleId.value = roles.value[0]?.id || null;
  }
}

async function loadPermissions() {
  const response = await fetchPermissionList({ status: 1 });
  permissions.value = response.data || [];
}

async function loadMenuTree() {
  const response = await fetchMenuTree();
  menuTree.value = response.data || [];
}

async function loadSelectedUserRoles() {
  if (!selectedUser.value) {
    selectedRoleIds.value = [];
    return;
  }
  const response = await fetchAssignedRoleIds(selectedUser.value.id);
  selectedRoleIds.value = response.data || [];
}

async function loadSelectedRoleResources() {
  if (!selectedRoleId.value) {
    selectedPermissionIds.value = [];
    menuTreeRef.value?.setCheckedKeys([]);
    return;
  }
  const [menuRes, permissionRes] = await Promise.all([
    fetchAssignedMenuIds(selectedRoleId.value),
    fetchAssignedPermissionIds(selectedRoleId.value)
  ]);
  selectedPermissionIds.value = permissionRes.data || [];
  menuTreeRef.value?.setCheckedKeys(menuRes.data || []);
}

function handleUserChange(currentRow) {
  selectedUser.value = currentRow;
  loadSelectedUserRoles();
}

function handleUserPageChange(pageNum) {
  userPageNum.value = pageNum;
  loadUsers();
}

function handleRoleChange() {
  loadSelectedRoleResources();
}

async function handleSaveUserRoles() {
  if (!selectedUser.value) {
    ElMessage.warning('请先选择用户');
    return;
  }
  await assignUserRoles({
    userId: selectedUser.value.id,
    roleIds: selectedRoleIds.value
  });
  ElMessage.success('保存用户角色成功');
}

async function handleSaveRoleMenus() {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择角色');
    return;
  }
  await assignRoleMenus({
    roleId: selectedRoleId.value,
    menuIds: menuTreeRef.value?.getCheckedKeys(false) || []
  });
  ElMessage.success('保存角色菜单成功');
}

async function handleSaveRolePermissions() {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择角色');
    return;
  }
  await assignRolePermissions({
    roleId: selectedRoleId.value,
    permissionIds: selectedPermissionIds.value
  });
  ElMessage.success('保存角色权限成功');
}

function createDefaultRoleForm() {
  return {
    id: null,
    roleCode: '',
    roleName: '',
    roleType: 'BUSINESS',
    dataScopeType: 'ALL',
    sortNo: 99,
    status: 1,
    remark: ''
  };
}

function createDefaultMenuForm() {
  return {
    id: null,
    menuCode: '',
    parentId: 0,
    menuName: '',
    menuType: 'MENU',
    routeName: '',
    routePath: '',
    componentPath: '',
    icon: '',
    permissionCode: '',
    isHidden: 0,
    isKeepAlive: 0,
    sortNo: 99,
    status: 1,
    remark: ''
  };
}

function createDefaultPermissionForm() {
  return {
    id: null,
    permissionCode: '',
    permissionName: '',
    permissionType: 'PAGE',
    resourcePath: '',
    httpMethod: '',
    status: 1,
    remark: ''
  };
}

function flattenMenus(tree, result = []) {
  for (const item of tree || []) {
    result.push(item);
    flattenMenus(item.children || [], result);
  }
  return result;
}

function openRoleDialog(role) {
  roleForm.value = role
    ? {
        id: role.id,
        roleCode: role.roleCode,
        roleName: role.roleName,
        roleType: role.roleType,
        dataScopeType: role.dataScopeType,
        sortNo: role.sortNo || 99,
        status: role.status,
        remark: role.remark || ''
      }
    : createDefaultRoleForm();
  roleDialogVisible.value = true;
}

function openMenuDialog(menu) {
  menuForm.value = menu
    ? {
        id: menu.id,
        menuCode: menu.menuCode,
        parentId: menu.parentId ?? 0,
        menuName: menu.menuName,
        menuType: menu.menuType,
        routeName: menu.routeName || '',
        routePath: menu.routePath || '',
        componentPath: menu.componentPath || '',
        icon: menu.icon || '',
        permissionCode: menu.permissionCode || '',
        isHidden: menu.isHidden ?? 0,
        isKeepAlive: menu.isKeepAlive ?? 0,
        sortNo: menu.sortNo || 99,
        status: menu.status,
        remark: menu.remark || ''
      }
    : createDefaultMenuForm();
  menuDialogVisible.value = true;
}

function editMenu(menu) {
  openMenuDialog(menu);
}

function openPermissionDialog(permission) {
  permissionForm.value = permission
    ? {
        id: permission.id,
        permissionCode: permission.permissionCode,
        permissionName: permission.permissionName,
        permissionType: permission.permissionType,
        resourcePath: permission.resourcePath || '',
        httpMethod: permission.httpMethod || '',
        status: permission.status,
        remark: permission.remark || ''
      }
    : createDefaultPermissionForm();
  permissionDialogVisible.value = true;
}

function editPermission(permission) {
  openPermissionDialog(permission);
}

async function handleSaveRole() {
  if (!roleForm.value.roleCode || !roleForm.value.roleName || !roleForm.value.roleType || !roleForm.value.dataScopeType) {
    ElMessage.warning('请填写完整角色信息');
    return;
  }
  await saveRole(roleForm.value);
  roleDialogVisible.value = false;
  await loadRoles();
  ElMessage.success('保存角色成功');
}

async function handleToggleRoleStatus(role) {
  const targetStatus = role.status === 1 ? 0 : 1;
  await changeRoleStatus(role.id, targetStatus);
  await loadRoles();
  ElMessage.success('修改角色状态成功');
}

async function handleSaveMenu() {
  if (!menuForm.value.menuCode || !menuForm.value.menuName || !menuForm.value.menuType) {
    ElMessage.warning('请填写完整菜单信息');
    return;
  }
  await saveMenu(menuForm.value);
  await loadMenuTree();
  await loadSelectedRoleResources();
  menuDialogVisible.value = false;
  ElMessage.success('保存菜单成功');
}

async function handleToggleMenuStatus(menu) {
  const targetStatus = menu.status === 1 ? 0 : 1;
  await changeMenuStatus(menu.id, targetStatus);
  await loadMenuTree();
  await loadSelectedRoleResources();
  ElMessage.success('修改菜单状态成功');
}

async function handleDeleteMenu(menu) {
  await ElMessageBox.confirm(`确认删除菜单“${menu.menuName}”吗？`, '删除确认', {
    type: 'warning'
  });
  await deleteMenu(menu.id);
  await loadMenuTree();
  await loadSelectedRoleResources();
  ElMessage.success('删除菜单成功');
}

async function handleSavePermission() {
  if (!permissionForm.value.permissionCode || !permissionForm.value.permissionName || !permissionForm.value.permissionType) {
    ElMessage.warning('请填写完整权限点信息');
    return;
  }
  await savePermission(permissionForm.value);
  await loadPermissions();
  await loadSelectedRoleResources();
  permissionDialogVisible.value = false;
  ElMessage.success('保存权限点成功');
}

async function handleTogglePermissionStatus(permission) {
  const targetStatus = permission.status === 1 ? 0 : 1;
  await changePermissionStatus(permission.id, targetStatus);
  await loadPermissions();
  await loadSelectedRoleResources();
  ElMessage.success('修改权限点状态成功');
}

async function handleDeletePermission(permission) {
  await ElMessageBox.confirm(`确认删除权限点“${permission.permissionName}”吗？`, '删除确认', {
    type: 'warning'
  });
  await deletePermission(permission.id);
  await loadPermissions();
  await loadSelectedRoleResources();
  ElMessage.success('删除权限点成功');
}

async function handleDeleteRole(role) {
  await ElMessageBox.confirm(`确认删除角色“${role.roleName}”吗？`, '删除确认', {
    type: 'warning'
  });
  await deleteRole(role.id);
  if (selectedRoleId.value === role.id) {
    selectedRoleId.value = null;
  }
  await loadRoles();
  await loadSelectedRoleResources();
  ElMessage.success('删除角色成功');
}

async function initializePage() {
  loading.value = true;
  try {
    await Promise.all([loadUsers(), loadRoles(), loadPermissions(), loadMenuTree()]);
    await loadSelectedRoleResources();
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  initializePage();
});
</script>

<style scoped>
.rbac-layout {
  margin-top: 24px;
}

.rbac-card {
  min-height: 620px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-row {
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: 12px;
}

.dialog-toolbar {
  display: grid;
  grid-template-columns: 1fr 140px 140px;
  gap: 12px;
}

.dialog-table {
  margin-top: 16px;
}

.rbac-table {
  margin-top: 12px;
}

.rbac-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.section-hint {
  margin-bottom: 12px;
  color: #667085;
}

.role-maintenance-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.role-maintenance-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #f8fbff;
}

.role-maintenance-title {
  font-weight: 700;
}

.role-maintenance-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #667085;
}

.role-maintenance-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0 16px;
}

.resource-panels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.resource-card {
  min-height: 460px;
}

.permission-group {
  max-height: 320px;
  overflow: auto;
  padding-right: 8px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.full-width {
  width: 100%;
}
</style>
