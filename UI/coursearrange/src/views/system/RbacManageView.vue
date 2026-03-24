<template>
  <section class="page-card" v-loading="loading">
    <h1 class="page-title">权限管理</h1>
    <p class="page-description">当前页面已接通用户、角色、菜单、权限分配接口，可直接用于联调 RBAC3 基础闭环。</p>

    <el-row :gutter="16" class="rbac-layout">
      <el-col :span="8">
        <el-card shadow="never" class="rbac-card">
          <template #header>
            <div class="card-header">
              <div>
                <div class="section-title">系统用户</div>
                <div class="section-caption">统一认证账号池，业务资料账号由同步链路自动进入这里。</div>
              </div>
              <el-button type="primary" link @click="openUserDialog()">新增用户</el-button>
            </div>
          </template>
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
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click.stop="openUserDialog(row)">编辑</el-button>
                <el-button type="warning" link @click.stop="handleToggleUserStatus(row)">
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
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

  <el-dialog v-model="roleDialogVisible" :title="roleForm.id ? '编辑角色' : '新增角色'" width="520px" append-to-body>
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

  <el-dialog v-model="userDialogVisible" :title="userForm.id ? '编辑用户' : '新增用户'" width="620px" append-to-body>
    <el-form :model="userForm" label-position="top">
      <div class="dialog-hint">
        <div class="dialog-hint-title">账号说明</div>
        <div class="dialog-hint-text">
          普通管理员可在这里维护独立后台账号。教师、学生等业务账号会自动同步到 `sys_user`，来源绑定信息不建议手工修改。
        </div>
      </div>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="用户编码">
            <el-input v-model="userForm.userCode" placeholder="例如 A_900001" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="登录账号">
            <el-input v-model="userForm.username" placeholder="请输入登录账号" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="真实姓名">
            <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="显示名称">
            <el-input v-model="userForm.displayName" placeholder="为空则回退真实姓名" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item :label="userForm.id ? '重置密码' : '登录密码'">
            <el-input
              v-model="userForm.password"
              type="password"
              show-password
              :placeholder="userForm.id ? '不修改可留空' : '请输入登录密码'"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用户类型">
            <el-select v-model="userForm.userType" placeholder="请选择用户类型" :disabled="isBoundSourceUser">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="手机号">
            <el-input v-model="userForm.mobile" placeholder="请输入手机号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
      </el-row>
      <div v-if="isBoundSourceUser" class="binding-panel">
        <div class="binding-panel-header">
          <span class="binding-panel-title">系统绑定信息</span>
          <el-tag type="warning" effect="plain">只读</el-tag>
        </div>
        <div class="binding-panel-text">
          该账号来自业务资料同步，用于把统一认证账号和教师/学生/管理员资料绑定在一起。
        </div>
        <el-row :gutter="12">
          <el-col :span="12">
            <div class="binding-field">
              <span class="binding-label">来源类型</span>
              <strong>{{ userForm.sourceType }}</strong>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="binding-field">
              <span class="binding-label">来源 ID</span>
              <strong>{{ userForm.sourceId }}</strong>
            </div>
          </el-col>
        </el-row>
      </div>
      <el-form-item label="备注">
        <el-input v-model="userForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="userDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSaveUser">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="menuDialogVisible" :title="menuForm.id ? '编辑菜单' : '新增菜单'" width="900px" append-to-body>
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
            <el-input v-model="menuForm.menuCode" placeholder="例如 system-rbac（菜单唯一编码）" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜单名称">
            <el-input v-model="menuForm.menuName" placeholder="例如 权限管理、系统配置" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="父级菜单">
            <el-select v-model="menuForm.parentId" placeholder="例如 选择“系统管理”作为上级菜单">
              <el-option label="根节点" :value="0" />
              <el-option v-for="item in menuParentOptions" :key="item.id" :label="item.menuName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="菜单类型">
            <el-select v-model="menuForm.menuType" placeholder="例如 目录、菜单、按钮">
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
            <el-input v-model="menuForm.routeName" placeholder="例如 RbacManage（前端路由名称）" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="路由路径">
            <el-input v-model="menuForm.routePath" placeholder="例如 /system/rbac（浏览器访问路径）" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="组件路径">
            <el-input v-model="menuForm.componentPath" placeholder="例如 system/RbacManageView（views 下的组件路径）" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="权限编码">
            <el-input v-model="menuForm.permissionCode" placeholder="例如 page:system:rbac:view（页面权限标识）" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="图标">
            <el-input v-model="menuForm.icon" placeholder="例如 Setting、User、House" />
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
        <el-input v-model="menuForm.remark" type="textarea" :rows="2" placeholder="例如 仅管理员可见的权限管理菜单" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="menuDialogVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleSaveMenu">保存菜单</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="permissionDialogVisible" :title="permissionForm.id ? '编辑权限点' : '新增权限点'" width="900px" append-to-body>
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
  changeUserStatus,
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
  saveUser,
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
const userDialogVisible = ref(false);
const userForm = ref(createDefaultUserForm());
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
const isBoundSourceUser = computed(() => Boolean(userForm.value.sourceType || userForm.value.sourceId));
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

function createDefaultUserForm() {
  return {
    id: null,
    userCode: '',
    username: '',
    password: '',
    realName: '',
    displayName: '',
    mobile: '',
    email: '',
    userType: 'ADMIN',
    sourceType: '',
    sourceId: null,
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

function openUserDialog(user) {
  userForm.value = user
    ? {
        id: user.id,
        userCode: user.userCode || '',
        username: user.username,
        password: '',
        realName: user.realName,
        displayName: user.displayName || '',
        mobile: user.mobile || '',
        email: user.email || '',
        userType: user.userType,
        sourceType: user.sourceType || '',
        sourceId: user.sourceId || null,
        status: user.status,
        remark: user.remark || ''
      }
    : createDefaultUserForm();
  userDialogVisible.value = true;
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

async function handleSaveUser() {
  if (!userForm.value.userCode || !userForm.value.username || !userForm.value.realName || !userForm.value.userType) {
    ElMessage.warning('请填写完整用户信息');
    return;
  }
  if (!userForm.value.id && !userForm.value.password) {
    ElMessage.warning('新增用户时密码不能为空');
    return;
  }
  await saveUser(userForm.value);
  userDialogVisible.value = false;
  await loadUsers();
  ElMessage.success('保存用户成功');
}

async function handleToggleUserStatus(user) {
  const targetStatus = user.status === 1 ? 0 : 1;
  await changeUserStatus(user.id, targetStatus);
  await loadUsers();
  ElMessage.success('修改用户状态成功');
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
  gap: 12px;
}

.filter-row {
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.section-caption {
  margin-top: 4px;
  font-size: 12px;
  color: #667085;
}

.dialog-toolbar {
  display: grid;
  grid-template-columns: 1fr 140px 140px;
  gap: 12px;
}

.dialog-hint {
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid #d9e8ff;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef5ff 100%);
}

.dialog-hint-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.dialog-hint-text {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #526071;
}

.dialog-table {
  margin-top: 16px;
}

.binding-panel {
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px dashed #f5c97a;
  border-radius: 14px;
  background: linear-gradient(180deg, #fffaf0 0%, #fff6df 100%);
}

.binding-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.binding-panel-title {
  font-size: 14px;
  font-weight: 700;
  color: #8a5a00;
}

.binding-panel-text {
  margin: 8px 0 12px;
  font-size: 13px;
  line-height: 1.6;
  color: #7a5a1f;
}

.binding-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgb(255 255 255 / 58%);
}

.binding-label {
  font-size: 12px;
  color: #8a6c34;
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
