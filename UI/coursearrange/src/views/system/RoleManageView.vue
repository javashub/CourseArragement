<template>
  <div v-loading="loading" class="module-card">
    <div class="module-header">
      <div>
        <div class="module-title">角色管理</div>
        <div class="module-desc">管理系统角色定义，角色的菜单与权限点授权请在"权限管理"中操作。</div>
      </div>
      <el-button type="primary" @click="openDialog()">新增角色</el-button>
    </div>

    <div class="filter-row">
      <el-input
        v-model="keyword"
        placeholder="按角色名称/编码搜索"
        clearable
        style="width: 280px;"
        @keyup.enter="loadData"
        @clear="handleClear"
      />
      <el-select
        v-model="statusFilter"
        placeholder="状态"
        clearable
        style="width: 120px;"
        @change="loadData"
        @clear="handleStatusClear"
      >
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
    </div>

    <el-table :data="filteredRoles" size="small" class="data-table">
      <el-table-column prop="roleCode" label="角色编码" min-width="140">
        <template #default="{ row }">
          <span class="mono-code">{{ row.roleCode }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="roleName" label="角色名称" min-width="140">
        <template #default="{ row }">
          <span class="role-name-cell">{{ row.roleName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="roleType" label="角色类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.roleType === 'SYSTEM' ? 'danger' : 'primary'" size="small" effect="plain">
            {{ row.roleType === 'SYSTEM' ? '系统' : '业务' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dataScopeType" label="数据范围" width="120">
        <template #default="{ row }">
          <span class="scope-tag">{{ dataScopeLabel(row.dataScopeType) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button
            type="danger"
            link
            :disabled="row.roleType === 'SYSTEM'"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        layout="total"
        :total="filteredRoles.length"
      />
    </div>

    <!-- 角色编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="560px" append-to-body>
      <el-form :model="form" label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="角色编码">
              <el-input v-model="form.roleCode" placeholder="例如 ADMIN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色名称">
              <el-input v-model="form.roleName" placeholder="请输入角色名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="角色类型">
              <el-select v-model="form.roleType" style="width: 100%;">
                <el-option label="系统角色" value="SYSTEM" />
                <el-option label="业务角色" value="BUSINESS" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据范围">
              <el-select v-model="form.dataScopeType" style="width: 100%;">
                <el-option label="全部" value="ALL" />
                <el-option label="校区" value="CAMPUS" />
                <el-option label="学院" value="COLLEGE" />
                <el-option label="本人" value="SELF" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortNo" :min="1" :max="999" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchRoleList, saveRole, changeRoleStatus, deleteRole } from '@/api/modules/rbac';

const loading = ref(false);
const roles = ref([]);
const keyword = ref('');
const statusFilter = ref('');
const dialogVisible = ref(false);
const form = ref(createDefaultForm());

const filteredRoles = computed(() =>
  roles.value.filter((item) => {
    const keywordMatched = !keyword.value ||
      item.roleName?.includes(keyword.value) ||
      item.roleCode?.includes(keyword.value);
    const statusMatched = statusFilter.value === '' || item.status === statusFilter.value;
    return keywordMatched && statusMatched;
  })
);

async function loadData() {
  loading.value = true;
  try {
    const response = await fetchRoleList();
    roles.value = response.data || [];
  } finally {
    loading.value = false;
  }
}

function handleClear() {
  keyword.value = '';
}

function handleStatusClear() {
  statusFilter.value = '';
}

function dataScopeLabel(val) {
  const map = { ALL: '全部', CAMPUS: '校区', COLLEGE: '学院', SELF: '本人' };
  return map[val] || val;
}

function openDialog(role) {
  form.value = role
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
    : createDefaultForm();
  dialogVisible.value = true;
}

function createDefaultForm() {
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

async function handleSave() {
  if (!form.value.roleCode || !form.value.roleName || !form.value.roleType || !form.value.dataScopeType) {
    ElMessage.warning('请填写完整角色信息');
    return;
  }
  await saveRole(form.value);
  dialogVisible.value = false;
  await loadData();
  ElMessage.success('保存角色成功');
}

async function handleToggleStatus(role) {
  const targetStatus = role.status === 1 ? 0 : 1;
  await changeRoleStatus(role.id, targetStatus);
  await loadData();
  ElMessage.success('修改角色状态成功');
}

async function handleDelete(role) {
  if (role.roleType === 'SYSTEM') {
    ElMessage.warning('系统角色不允许删除');
    return;
  }
  await ElMessageBox.confirm(`确认删除角色"${role.roleName}"吗？`, '删除确认', { type: 'warning' });
  await deleteRole(role.id);
  await loadData();
  ElMessage.success('删除角色成功');
}

onMounted(() => loadData());
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

.filter-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.data-table {
  width: 100%;
}

.mono-code {
  font-family: 'JetBrains Mono', 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
  color: #475569;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
}

.role-name-cell {
  font-weight: 600;
}

.scope-tag {
  font-size: 12px;
  color: #64748b;
}

.pagination-row {
  display: flex;
  justify-content: flex-start;
  margin-top: 16px;
}
</style>
