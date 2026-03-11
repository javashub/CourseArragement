<template>
  <section class="page-card" v-loading="loading">
    <h1 class="page-title">权限管理</h1>
    <p class="page-description">当前页面已接通用户、角色、菜单、权限分配接口，可直接用于联调 RBAC3 基础闭环。</p>

    <el-row :gutter="16" class="rbac-layout">
      <el-col :span="8">
        <el-card shadow="never" class="rbac-card">
          <template #header>系统用户</template>
          <el-input v-model="userKeyword" placeholder="按账号/姓名搜索" clearable @keyup.enter="loadUsers" />
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
              <template #header>角色分配</template>
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
                    <template #header>菜单授权</template>
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
                    <template #header>权限点授权</template>
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
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  assignRoleMenus,
  assignRolePermissions,
  assignUserRoles,
  fetchAssignedMenuIds,
  fetchAssignedPermissionIds,
  fetchAssignedRoleIds,
  fetchMenuTree,
  fetchPermissionList,
  fetchRoleList,
  fetchUserPage
} from '@/api/modules/rbac';

const loading = ref(false);
const users = ref([]);
const userKeyword = ref('');
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

const menuTreeProps = {
  children: 'children',
  label: 'menuName'
};

const selectedRole = computed(() => roles.value.find((item) => item.id === selectedRoleId.value) || null);

async function loadUsers() {
  const response = await fetchUserPage({
    pageNum: userPageNum.value,
    pageSize: userPageSize.value,
    keyword: userKeyword.value
  });
  const pageData = response.data || {};
  users.value = pageData.records || [];
  userTotal.value = pageData.total || 0;
}

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
</style>
