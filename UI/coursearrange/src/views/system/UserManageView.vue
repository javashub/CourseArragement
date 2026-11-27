<template>
  <div class="module-card" v-loading="loading">
    <div class="module-header">
      <div>
        <div class="module-title">用户管理</div>
        <div class="module-desc">统一认证账号池，业务资料账号由同步链路自动进入这里。</div>
      </div>
      <div class="header-actions">
        <el-button type="success" plain @click="openBatchRoleDialog" :disabled="selectedIds.length === 0">
          <el-icon><UserFilled /></el-icon>
          批量设置角色
        </el-button>
        <el-button type="primary" @click="openDialog()">新增用户</el-button>
      </div>
    </div>

    <div class="filter-row">
      <el-input
        v-model="keyword"
        placeholder="按账号/姓名搜索"
        clearable
        style="width: 280px;"
        @keyup.enter="loadData"
        @clear="handleClear"
      />
      <el-select
        v-model="userTypeFilter"
        placeholder="用户类型"
        clearable
        style="width: 140px;"
        @change="loadData"
        @clear="handleTypeClear"
      >
        <el-option label="管理员" value="ADMIN" />
        <el-option label="教师" value="TEACHER" />
        <el-option label="学生" value="STUDENT" />
      </el-select>
      <span class="selection-hint" v-if="selectedIds.length > 0">
        已选择 {{ selectedIds.length }} 项
      </span>
    </div>

    <el-table
      :data="records"
      size="small"
      class="data-table"
      row-key="id"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" fixed="left" />
      <el-table-column prop="userCode" label="用户编码" min-width="140" />
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="100" />
      <el-table-column prop="userType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="userTypeTag(row.userType)" size="small">{{ userTypeLabel(row.userType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="mobile" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">
          <div class="role-tags">
            <el-tag
              v-for="role in row.roles || []"
              :key="role.id"
              size="small"
              effect="plain"
              type="info"
              class="role-tag"
            >
              {{ role.roleName }}
            </el-tag>
            <span v-if="!row.roles || row.roles.length === 0" class="no-role">未分配</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
          <el-button type="success" link @click="openUserRoleDialog(row)">分配角色</el-button>
          <el-button type="warning" link @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        layout="total, prev, pager, next"
        :total="total"
        :current-page="pageNum"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 用户编辑弹窗：挂接 A 方案紧凑布局；去掉账号说明 banner 与绑定信息解释段落，
         示例与格式统一写入 placeholder，字段与提交逻辑不变。 -->
    <el-dialog v-model="dialogVisible" class="compact-form" :title="form.id ? '编辑用户' : '新增用户'" width="620px" append-to-body>
      <el-form :model="form" label-position="top">
        <div class="form-grid compact-form-grid--two">
          <el-form-item label="用户编码">
            <el-input v-model="form.userCode" placeholder="例如 A_900001" />
          </el-form-item>
          <el-form-item label="登录账号">
            <el-input v-model="form.username" placeholder="例如 admin_01" />
          </el-form-item>
        </div>
        <div class="form-grid compact-form-grid--two">
          <el-form-item label="真实姓名">
            <el-input v-model="form.realName" placeholder="例如 张老师" />
          </el-form-item>
          <el-form-item label="显示名称">
            <el-input v-model="form.displayName" placeholder="选填，为空则回退真实姓名" />
          </el-form-item>
        </div>
        <div class="form-grid compact-form-grid--two">
          <el-form-item :label="form.id ? '重置密码' : '登录密码'">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              :placeholder="form.id ? '不修改可留空' : '例如 Admin@123，至少 6 位'"
            />
          </el-form-item>
          <el-form-item label="用户类型">
            <el-select v-model="form.userType" placeholder="例如 ADMIN 管理员" :disabled="isBoundSourceUser">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid compact-form-grid--two">
          <el-form-item label="手机号">
            <el-input v-model="form.mobile" placeholder="例如 13800000000，选填" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="例如 admin@school.edu.cn，选填" />
          </el-form-item>
        </div>
        <!-- 系统绑定信息：只读字段，去掉解释段落与卡片边框，仅保留短分组标题。 -->
        <div v-if="isBoundSourceUser" class="binding-panel compact-form-section">
          <div class="compact-form-section__header">
            <div class="compact-form-section__title">系统绑定信息（只读）</div>
          </div>
          <div class="form-grid compact-form-grid--two">
            <div class="binding-field">
              <span class="binding-label">来源类型</span>
              <strong>{{ form.sourceType }}</strong>
            </div>
            <div class="binding-field">
              <span class="binding-label">来源 ID</span>
              <strong>{{ form.sourceId }}</strong>
            </div>
          </div>
        </div>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="例如 独立后台账号，选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 单用户分配角色弹窗 -->
    <el-dialog v-model="userRoleDialogVisible" class="compact-form" title="分配角色" width="480px" append-to-body>
      <div class="role-dialog-user">
        <el-avatar :size="32" class="role-avatar">{{ userRoleTarget?.realName?.charAt(0) || '?' }}</el-avatar>
        <div class="role-user-info">
          <div class="role-user-name">{{ userRoleTarget?.realName || userRoleTarget?.username }}</div>
          <div class="role-user-meta">{{ userRoleTarget?.userCode }} · {{ userTypeLabel(userRoleTarget?.userType) }}</div>
        </div>
      </div>
      <el-divider style="margin: 16px 0;" />
      <div class="role-check-list">
        <el-checkbox-group v-model="userRoleCheckedIds">
          <div v-for="role in roles" :key="role.id" class="role-check-item">
            <el-checkbox :value="role.id">
              <span class="role-check-name">{{ role.roleName }}</span>
              <span class="role-check-code">（{{ role.roleCode }}）</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <el-empty v-if="roles.length === 0" description="暂无角色" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="userRoleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUserRole">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量设置角色弹窗 -->
    <el-dialog v-model="batchRoleDialogVisible" class="compact-form" title="批量设置角色" width="480px" append-to-body>
      <div class="batch-role-info">
        <el-tag type="primary" effect="plain" size="large">{{ selectedIds.length }}</el-tag>
        <span class="batch-role-text"> 个用户，将覆盖原有角色</span>
      </div>
      <el-divider style="margin: 16px 0;" />
      <div class="role-check-list">
        <el-checkbox-group v-model="batchRoleCheckedIds">
          <div v-for="role in roles" :key="role.id" class="role-check-item">
            <el-checkbox :value="role.id">
              <span class="role-check-name">{{ role.roleName }}</span>
              <span class="role-check-code">（{{ role.roleCode }}）</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <el-empty v-if="roles.length === 0" description="暂无角色" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="batchRoleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSaveRole" :disabled="batchRoleCheckedIds.length === 0">
          确认分配
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { UserFilled } from '@element-plus/icons-vue';
import { fetchUserPage, saveUser, changeUserStatus, fetchRoleList, fetchAssignedRoleIds, assignUserRoles, batchAssignUserRoles } from '@/api/modules/rbac';

const loading = ref(false);
const records = ref([]);
const keyword = ref('');
const userTypeFilter = ref('');
const pageNum = ref(1);
const pageSize = ref(15);
const total = ref(0);
const dialogVisible = ref(false);
const form = ref(createDefaultForm());
const selectedIds = ref([]);

// 角色相关
const roles = ref([]);
const userRoleDialogVisible = ref(false);
const userRoleTarget = ref(null);
const userRoleCheckedIds = ref([]);
const batchRoleDialogVisible = ref(false);
const batchRoleCheckedIds = ref([]);

const isBoundSourceUser = computed(() => Boolean(form.value.sourceType || form.value.sourceId));

async function loadData() {
  loading.value = true;
  try {
    const response = await fetchUserPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      userType: userTypeFilter.value
    });
    const pageData = response.data || {};
    records.value = pageData.records || [];
    total.value = pageData.total || 0;
    // 加载每个用户的角色
    await Promise.all(records.value.map(async (user) => {
      try {
        const res = await fetchAssignedRoleIds(user.id);
        const roleIds = res.data || [];
        user.roles = roles.value.filter((r) => roleIds.includes(r.id));
      } catch {
        user.roles = [];
      }
    }));
  } finally {
    loading.value = false;
  }
}

async function loadRoles() {
  const response = await fetchRoleList();
  roles.value = response.data || [];
}

function handleClear() {
  keyword.value = '';
  pageNum.value = 1;
  loadData();
}

function handleTypeClear() {
  userTypeFilter.value = '';
  pageNum.value = 1;
  loadData();
}

function handlePageChange(p) {
  pageNum.value = p;
  loadData();
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.id);
}

function userTypeTag(type) {
  return type === 'ADMIN' ? 'danger' : type === 'TEACHER' ? 'warning' : '';
}

function userTypeLabel(type) {
  return type === 'ADMIN' ? '管理员' : type === 'TEACHER' ? '教师' : type === 'STUDENT' ? '学生' : type;
}

function openDialog(user) {
  form.value = user
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
    : createDefaultForm();
  dialogVisible.value = true;
}

function createDefaultForm() {
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

async function handleSave() {
  if (!form.value.userCode || !form.value.username || !form.value.realName || !form.value.userType) {
    ElMessage.warning('请填写完整用户信息');
    return;
  }
  if (!form.value.id && !form.value.password) {
    ElMessage.warning('新增用户时密码不能为空');
    return;
  }
  await saveUser(form.value);
  dialogVisible.value = false;
  await loadData();
  ElMessage.success('保存用户成功');
}

async function handleToggleStatus(user) {
  const targetStatus = user.status === 1 ? 0 : 1;
  await changeUserStatus(user.id, targetStatus);
  await loadData();
  ElMessage.success('修改用户状态成功');
}

// ========== 单用户分配角色 ==========
async function openUserRoleDialog(user) {
  userRoleTarget.value = user;
  const res = await fetchAssignedRoleIds(user.id);
  userRoleCheckedIds.value = res.data || [];
  userRoleDialogVisible.value = true;
}

async function handleSaveUserRole() {
  if (!userRoleTarget.value) return;
  await assignUserRoles({
    userId: userRoleTarget.value.id,
    roleIds: userRoleCheckedIds.value
  });
  userRoleDialogVisible.value = false;
  await loadData();
  ElMessage.success('保存用户角色成功');
}

// ========== 批量设置角色 ==========
function openBatchRoleDialog() {
  batchRoleCheckedIds.value = [];
  batchRoleDialogVisible.value = true;
}

async function handleBatchSaveRole() {
  if (batchRoleCheckedIds.value.length === 0) {
    ElMessage.warning('请至少选择一个角色');
    return;
  }
  await batchAssignUserRoles({
    userIds: selectedIds.value,
    roleIds: batchRoleCheckedIds.value
  });
  batchRoleDialogVisible.value = false;
  selectedIds.value = [];
  await loadData();
  ElMessage.success('批量分配角色成功');
}

onMounted(async () => {
  loading.value = true;
  try {
    await loadRoles();
    await loadData();
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
/* 弹窗表单网格统一走全局 .form-grid + .compact-form-grid--two，
   页面 scoped 不再声明列宽与间距；form-item margin 与控件高度由全局提供。 */
.module-card {
  background: #fff;
}

.module-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
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

.header-actions {
  display: flex;
  gap: 8px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.selection-hint {
  font-size: 12px;
  color: #2563eb;
  font-weight: 600;
  padding: 4px 10px;
  background: #eff6ff;
  border-radius: 6px;
}

.data-table {
  width: 100%;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-tag {
  font-size: 11px;
}

.no-role {
  font-size: 12px;
  color: #cbd5e1;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 弹窗内的说明 banner 与分组描述已移除；绑定信息改为轻分组（短标题 + 分隔线）。
   网格列数/间距、form-item 与控件高度统一由全局 .compact-form 规则提供。 */
.binding-panel {
  margin-bottom: 10px;
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

/* 角色分配弹窗 */
.role-dialog-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.role-avatar {
  background: #e0e7ff;
  color: #4338ca;
  font-weight: 700;
  font-size: 14px;
}

.role-user-info {
  flex: 1;
}

.role-user-name {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.role-user-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.batch-role-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-role-text {
  font-size: 13px;
  color: #64748b;
}

.role-check-list {
  max-height: 360px;
  overflow-y: auto;
  padding-right: 4px;
}

.role-check-list::-webkit-scrollbar {
  width: 5px;
}

.role-check-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.role-check-item {
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.15s;
}

.role-check-item:hover {
  background: #f1f5f9;
}

.role-check-name {
  font-weight: 600;
  font-size: 14px;
}

.role-check-code {
  font-size: 12px;
  color: #94a3b8;
}
</style>
