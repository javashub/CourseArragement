<template>
  <section class="page-card" v-loading="loading">
    <div class="page-header">
      <div>
        <h1 class="page-title">校区管理</h1>
        <p class="page-description">维护多校区办学场景下的校区信息，后续教学楼、教室、教师归属都会挂在这里。</p>
      </div>
      <el-button type="primary" @click="handleCreate">新增校区</el-button>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-grid">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索校区名称或编码，例如 主校区、CAMPUS_MAIN"
          @keyup.enter="handleSearch"
          @clear="handleSearchReset"
        />
        <el-select
          v-model="filters.status"
          clearable
          placeholder="筛选状态，例如 启用"
          @change="handleSearchReset"
          @clear="handleSearchReset"
        >
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <div class="toolbar-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="campuses" stripe>
        <el-table-column prop="campusCode" label="校区编码" min-width="160" />
        <el-table-column prop="campusName" label="校区名称" min-width="180" />
        <el-table-column prop="campusType" label="校区类型" min-width="120">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.campusType || '--' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所在地区" min-width="220">
          <template #default="{ row }">
            {{ formatRegion(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="campusForm.id ? '编辑校区' : '新增校区'" width="640px">
      <el-form ref="formRef" :model="campusForm" :rules="formRules" label-width="110px">
        <el-form-item label="校区编码" prop="campusCode">
          <el-input v-model="campusForm.campusCode" placeholder="例如 CAMPUS_MAIN、BJ_XC" />
        </el-form-item>
        <el-form-item label="校区名称" prop="campusName">
          <el-input v-model="campusForm.campusName" placeholder="例如 主校区、西城校区" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="校区类型" prop="campusType">
              <el-input v-model="campusForm.campusType" placeholder="例如 主校区、分校区、校外点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="campusForm.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="省份编码">
              <el-input v-model="campusForm.provinceCode" placeholder="例如 110000" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市编码">
              <el-input v-model="campusForm.cityCode" placeholder="例如 110100" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区县编码">
              <el-input v-model="campusForm.districtCode" placeholder="例如 110108" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="campusForm.address" placeholder="例如 北京市海淀区学院路 1 号" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="campusForm.sortNo" :min="0" :max="9999" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input
            v-model="campusForm.remark"
            type="textarea"
            :rows="3"
            placeholder="例如 承担高中部与大学部课程安排"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteCampus, fetchCampusDetail, fetchCampusPage, saveCampus } from '@/api/modules/system';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const formRef = ref();
const campuses = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const filters = reactive({
  keyword: '',
  status: null
});

const campusForm = ref(createDefaultForm());

const formRules = {
  campusCode: [{ required: true, message: '请输入校区编码', trigger: 'blur' }],
  campusName: [{ required: true, message: '请输入校区名称', trigger: 'blur' }],
  campusType: [{ required: true, message: '请输入校区类型', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
};

function createDefaultForm() {
  return {
    id: null,
    campusCode: '',
    campusName: '',
    campusType: '',
    provinceCode: '',
    cityCode: '',
    districtCode: '',
    address: '',
    sortNo: 0,
    status: 1,
    remark: ''
  };
}

function formatRegion(row) {
  return [row.provinceCode, row.cityCode, row.districtCode].filter(Boolean).join(' / ') || '--';
}

async function loadCampuses() {
  loading.value = true;
  try {
    const response = await fetchCampusPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      status: filters.status ?? undefined
    });
    const pageData = response.data || {};
    campuses.value = pageData.records || [];
    total.value = pageData.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadCampuses();
}

function handleSearchReset() {
  handleSearch();
}

function handleReset() {
  filters.keyword = '';
  filters.status = null;
  handleSearch();
}

function handlePageChange(page) {
  pageNum.value = page;
  loadCampuses();
}

function handleCreate() {
  campusForm.value = createDefaultForm();
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

async function handleEdit(row) {
  const response = await fetchCampusDetail(row.id);
  campusForm.value = {
    ...createDefaultForm(),
    ...(response.data || {})
  };
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  saving.value = true;
  try {
    await saveCampus(campusForm.value);
    ElMessage.success(campusForm.value.id ? '校区更新成功' : '校区创建成功');
    dialogVisible.value = false;
    await loadCampuses();
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除校区“${row.campusName}”吗？`, '删除确认', {
    type: 'warning'
  });
  await deleteCampus(row.id);
  ElMessage.success('校区删除成功');
  if (campuses.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1;
  }
  await loadCampuses();
}

onMounted(() => {
  loadCampuses();
});
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.toolbar-card {
  margin-bottom: 16px;
}

.toolbar-grid {
  display: grid;
  grid-template-columns: minmax(280px, 1.5fr) 180px auto;
  gap: 12px;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
  }

  .toolbar-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
</style>
