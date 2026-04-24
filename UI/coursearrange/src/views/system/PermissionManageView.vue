<template>
  <section class="module-card" v-loading="loading">
    <div class="module-header">
      <div>
        <div class="module-title">权限管理</div>
        <div class="module-desc">为角色授予菜单和权限点的访问权限。</div>
      </div>
    </div>

    <!-- 角色选择 -->
    <div class="role-selector-section">
      <span class="role-selector-label">选择角色：</span>
      <el-radio-group v-model="selectedRoleId" @change="handleRoleChange">
        <el-radio-button v-for="role in roles" :key="role.id" :value="role.id">
          {{ role.roleName }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <el-empty v-if="!selectedRoleId" description="请选择一个角色" />

    <template v-else>
      <div class="role-hint">
        当前角色：<strong>{{ selectedRole.roleName }}</strong>（{{ selectedRole.roleCode }}）
      </div>

      <el-row :gutter="20">
        <!-- 左侧：菜单授权 -->
        <el-col :span="12">
          <div class="panel-header">
            <span class="panel-title">菜单授权</span>
            <el-button type="primary" link @click="openMenuDialog()">维护菜单</el-button>
          </div>
          <div class="tree-container">
            <el-tree
              ref="menuTreeRef"
              node-key="id"
              show-checkbox
              :data="menuTree"
              :props="menuTreeProps"
              default-expand-all
            />
          </div>
          <div class="action-row">
            <el-button type="primary" @click="handleSaveRoleMenus">保存菜单授权</el-button>
          </div>
        </el-col>

        <!-- 右侧：权限点授权 -->
        <el-col :span="12">
          <div class="panel-header">
            <span class="panel-title">权限点授权</span>
            <el-button type="primary" link @click="openPermissionDialog()">维护权限点</el-button>
          </div>
          <div class="perm-filter">
            <el-input v-model="permKeyword" placeholder="搜索权限名称/编码" clearable size="small" style="width: 200px;" />
            <el-select v-model="permTypeFilter" placeholder="类型" clearable size="small" style="width: 120px;">
              <el-option label="页面" value="PAGE" />
              <el-option label="按钮" value="BUTTON" />
              <el-option label="接口" value="API" />
            </el-select>
          </div>
          <div class="checkbox-container">
            <el-checkbox-group v-model="selectedPermissionIds">
              <el-checkbox v-for="perm in filteredPermissions" :key="perm.id" :value="perm.id">
                <span class="perm-label">{{ perm.permissionName }}</span>
                <span class="perm-code">（{{ perm.permissionCode }}）</span>
              </el-checkbox>
            </el-checkbox-group>
          </div>
          <div class="action-row">
            <el-button type="primary" @click="handleSaveRolePermissions">保存权限授权</el-button>
          </div>
        </el-col>
      </el-row>
    </template>

    <!-- 菜单维护弹窗 -->
    <el-dialog v-model="menuDialogVisible" :title="menuForm.id ? '编辑菜单' : '新增菜单'" width="900px" append-to-body>
      <div class="dialog-toolbar">
        <el-input v-model="menuKeyword" placeholder="按菜单名称/编码搜索" clearable @clear="menuKeyword = ''" />
        <el-select v-model="menuStatusFilter" placeholder="菜单状态" clearable @clear="menuStatusFilter = ''">
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
              <el-input v-model="menuForm.menuCode" placeholder="例如 system-rbac" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称">
              <el-input v-model="menuForm.menuName" placeholder="例如 权限管理" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="父级菜单">
              <el-select v-model="menuForm.parentId" placeholder="选择上级菜单">
                <el-option label="根节点" :value="0" />
                <el-option v-for="item in menuParentOptions" :key="item.id" :label="item.menuName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="菜单类型">
              <el-select v-model="menuForm.menuType">
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
              <el-input v-model="menuForm.routeName" placeholder="例如 RbacManage" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由路径">
              <el-input v-model="menuForm.routePath" placeholder="例如 /system/rbac" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="组件路径">
              <el-input v-model="menuForm.componentPath" placeholder="例如 system/RbacManageView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限编码">
              <el-input v-model="menuForm.permissionCode" placeholder="例如 page:system:rbac:view" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="图标">
              <el-input v-model="menuForm.icon" placeholder="例如 Setting" />
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
          <el-input v-model="menuForm.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSaveMenu">保存菜单</el-button>
      </template>
    </el-dialog>

    <!-- 权限点维护弹窗 -->
    <el-dialog v-model="permissionDialogVisible" :title="permissionForm.id ? '编辑权限点' : '新增权限点'" width="900px" append-to-body>
      <div class="dialog-toolbar">
        <el-input v-model="permissionKeyword" placeholder="按权限名称/编码搜索" clearable @clear="permissionKeyword = ''" />
        <el-select v-model="permissionTypeFilter" placeholder="权限类型" clearable @clear="permissionTypeFilter = ''">
          <el-option label="页面" value="PAGE" />
          <el-option label="按钮" value="BUTTON" />
          <el-option label="接口" value="API" />
        </el-select>
        <el-select v-model="permissionStatusFilter" placeholder="权限状态" clearable @clear="permissionStatusFilter = ''">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </div>
      <el-table :data="filteredPermissionDefs" size="small" max-height="260" class="dialog-table">
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
              <el-select v-model="permissionForm.permissionType">
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
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  assignRoleMenus,
  assignRolePermissions,
  changeMenuStatus,
  changePermissionStatus,
  deleteMenu,
  deletePermission,
  fetchAssignedMenuIds,
  fetchAssignedPermissionIds,
  fetchMenuTree,
  fetchPermissionList,
  fetchRoleList,
  saveMenu,
  savePermission
} from '@/api/modules/rbac';

const loading = ref(false);
const roles = ref([]);
const permissions = ref([]);
const menuTree = ref([]);
const selectedRoleId = ref(null);
const selectedPermissionIds = ref([]);
const menuTreeRef = ref();

// 菜单维护相关
const menuDialogVisible = ref(false);
const menuForm = ref(createDefaultMenuForm());
const menuKeyword = ref('');
const menuStatusFilter = ref('');

// 权限点维护相关
const permissionDialogVisible = ref(false);
const permissionForm = ref(createDefaultPermissionForm());
const permissionKeyword = ref('');
const permissionTypeFilter = ref('');
const permissionStatusFilter = ref('');

// 授权区搜索
const permKeyword = ref('');
const permTypeFilter = ref('');

const menuTreeProps = { children: 'children', label: 'menuName' };
const selectedRole = computed(() => roles.value.find((item) => item.id === selectedRoleId.value) || null);
const flatMenuList = computed(() => flattenMenus(menuTree.value));
const menuParentOptions = computed(() =>
  flatMenuList.value.filter((item) => item.id !== menuForm.value.id && item.menuType !== 'BUTTON')
);
const filteredMenuList = computed(() =>
  flatMenuList.value.filter((item) => {
    const keywordMatched = !menuKeyword.value || item.menuName?.includes(menuKeyword.value) || item.menuCode?.includes(menuKeyword.value);
    const statusMatched = menuStatusFilter.value === '' || item.status === menuStatusFilter.value;
    return keywordMatched && statusMatched;
  })
);
const filteredPermissionDefs = computed(() =>
  permissions.value.filter((item) => {
    const keywordMatched = !permissionKeyword.value || item.permissionName?.includes(permissionKeyword.value) || item.permissionCode?.includes(permissionKeyword.value);
    const typeMatched = !permissionTypeFilter.value || item.permissionType === permissionTypeFilter.value;
    const statusMatched = permissionStatusFilter.value === '' || item.status === permissionStatusFilter.value;
    return keywordMatched && typeMatched && statusMatched;
  })
);
const filteredPermissions = computed(() =>
  permissions.value.filter((item) => {
    const keywordMatched = !permKeyword.value || item.permissionName?.includes(permKeyword.value) || item.permissionCode?.includes(permKeyword.value);
    const typeMatched = !permTypeFilter.value || item.permissionType === permTypeFilter.value;
    return keywordMatched && typeMatched;
  })
);

async function loadRoles() {
  const response = await fetchRoleList();
  roles.value = response.data || [];
  if (!selectedRoleId.value && roles.value.length > 0) {
    selectedRoleId.value = roles.value[0].id;
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

function handleRoleChange() {
  loadSelectedRoleResources();
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
  ElMessage.success('保存菜单授权成功');
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

// ========== 菜单维护 ==========
function openMenuDialog(menu) {
  menuForm.value = menu
    ? {
        id: menu.id, menuCode: menu.menuCode, parentId: menu.parentId ?? 0,
        menuName: menu.menuName, menuType: menu.menuType, routeName: menu.routeName || '',
        routePath: menu.routePath || '', componentPath: menu.componentPath || '',
        icon: menu.icon || '', permissionCode: menu.permissionCode || '',
        isHidden: menu.isHidden ?? 0, isKeepAlive: menu.isKeepAlive ?? 0,
        sortNo: menu.sortNo || 99, status: menu.status, remark: menu.remark || ''
      }
    : createDefaultMenuForm();
  menuDialogVisible.value = true;
}

function editMenu(menu) { openMenuDialog(menu); }

function createDefaultMenuForm() {
  return {
    id: null, menuCode: '', parentId: 0, menuName: '', menuType: 'MENU',
    routeName: '', routePath: '', componentPath: '', icon: '', permissionCode: '',
    isHidden: 0, isKeepAlive: 0, sortNo: 99, status: 1, remark: ''
  };
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
  await ElMessageBox.confirm(`确认删除菜单"${menu.menuName}"吗？`, '删除确认', { type: 'warning' });
  await deleteMenu(menu.id);
  await loadMenuTree();
  await loadSelectedRoleResources();
  ElMessage.success('删除菜单成功');
}

// ========== 权限点维护 ==========
function openPermissionDialog(permission) {
  permissionForm.value = permission
    ? {
        id: permission.id, permissionCode: permission.permissionCode,
        permissionName: permission.permissionName, permissionType: permission.permissionType,
        resourcePath: permission.resourcePath || '', httpMethod: permission.httpMethod || '',
        status: permission.status, remark: permission.remark || ''
      }
    : createDefaultPermissionForm();
  permissionDialogVisible.value = true;
}

function editPermission(permission) { openPermissionDialog(permission); }

function createDefaultPermissionForm() {
  return { id: null, permissionCode: '', permissionName: '', permissionType: 'PAGE', resourcePath: '', httpMethod: '', status: 1, remark: '' };
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
  await ElMessageBox.confirm(`确认删除权限点"${permission.permissionName}"吗？`, '删除确认', { type: 'warning' });
  await deletePermission(permission.id);
  await loadPermissions();
  await loadSelectedRoleResources();
  ElMessage.success('删除权限点成功');
}

function flattenMenus(tree, result = []) {
  for (const item of tree || []) {
    result.push(item);
    flattenMenus(item.children || [], result);
  }
  return result;
}

onMounted(async () => {
  loading.value = true;
  try {
    await Promise.all([loadRoles(), loadPermissions(), loadMenuTree()]);
    await loadSelectedRoleResources();
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.module-card {
  background: #fff;
}

.module-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.module-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.module-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.role-selector-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 10px;
}

.role-selector-label {
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
  white-space: nowrap;
}

.role-hint {
  margin-bottom: 16px;
  font-size: 13px;
  color: #64748b;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.tree-container {
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 12px;
  max-height: 380px;
  overflow-y: auto;
  background: #fafbfc;
}

.tree-container::-webkit-scrollbar {
  width: 5px;
}

.tree-container::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.perm-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.checkbox-container {
  max-height: 320px;
  overflow-y: auto;
  padding: 12px;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  background: #fafbfc;
}

.checkbox-container::-webkit-scrollbar {
  width: 5px;
}

.checkbox-container::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.checkbox-container .el-checkbox {
  display: flex;
  margin-right: 0;
}

.perm-label {
  font-weight: 500;
}

.perm-code {
  font-size: 12px;
  color: #94a3b8;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-toolbar {
  display: grid;
  grid-template-columns: 1fr 140px 140px;
  gap: 12px;
}

.dialog-table {
  margin-top: 12px;
}
</style>
