<template>
  <section class="page-card" v-loading="loading">
    <PageHeader title="学院管理" description="用于维护学院与校区归属，后续教师归属、跨学院授课配置会从这里继续扩展。">
      <template #actions>
        <el-button type="primary" @click="handleCreate">新增学院</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-grid">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索学院名称或编码，例如 信息工程学院、COLLEGE_CS"
          @keyup.enter="handleSearch"
          @clear="handleSearchReset"
        />
        <el-select
          v-model="filters.campusId"
          clearable
          placeholder="筛选所属校区，例如 主校区"
          @change="handleSearchReset"
          @clear="handleSearchReset"
        >
          <el-option v-for="campus in campusOptions" :key="campus.id" :label="campus.campusName" :value="campus.id" />
        </el-select>
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
      <el-table :data="colleges" stripe>
        <el-table-column prop="collegeCode" label="学院编码" min-width="160" />
        <el-table-column prop="collegeName" label="学院名称" min-width="180" />
        <el-table-column label="所属校区" min-width="160">
          <template #default="{ row }">
            {{ getCampusName(row.campusId) }}
          </template>
        </el-table-column>
        <el-table-column prop="deanUserId" label="负责人用户ID" width="130" />
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

    <el-dialog
      v-model="dialogVisible"
      :title="collegeForm.id ? '编辑学院' : '新增学院'"
      width="860px"
      append-to-body
      class="academy-form-dialog"
    >
      <!-- A 方案紧凑表单：删除 intro banner 与分组描述，字段统一 2 列网格，示例写进 placeholder。 -->
      <el-form ref="formRef" :model="collegeForm" :rules="formRules" label-position="top" class="academy-form">
        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">学院身份</div>
          </div>
          <!-- 2 列紧凑网格：编码 / 名称 同行，示例与格式全部写进 placeholder。 -->
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="学院编码" prop="collegeCode">
              <el-input v-model="collegeForm.collegeCode" placeholder="例如 COLLEGE_CS" />
            </el-form-item>
            <el-form-item label="学院名称" prop="collegeName">
              <el-input v-model="collegeForm.collegeName" placeholder="例如 信息工程学院" />
            </el-form-item>
          </div>
        </div>

        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">归属关系</div>
          </div>
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="所属校区">
              <el-select v-model="collegeForm.campusId" clearable placeholder="选择所属校区">
                <el-option
                  v-for="campus in campusOptions"
                  :key="campus.id"
                  :label="campus.campusName"
                  :value="campus.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="负责人用户ID">
              <el-input-number
                v-model="collegeForm.deanUserId"
                :min="0"
                :max="999999999"
                controls-position="right"
                placeholder="选填，例如 1001"
              />
            </el-form-item>
          </div>
        </div>

        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">展示与说明</div>
          </div>
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="排序">
              <el-input-number
                v-model="collegeForm.sortNo"
                :min="0"
                :max="9999"
                controls-position="right"
                placeholder="0-9999，例如 10"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="collegeForm.status" class="academy-radio-group">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <el-form-item label="备注">
            <el-input
              v-model="collegeForm.remark"
              type="textarea"
              :rows="3"
              placeholder="例如 允许教师跨学院授课"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button class="academy-dialog__ghost" @click="dialogVisible = false">取消</el-button>
        <el-button class="academy-dialog__primary" type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import PageHeader from '@/components/PageHeader.vue';
import {
  deleteCollege,
  fetchCampusList,
  fetchCollegeDetail,
  fetchCollegePage,
  saveCollege
} from '@/api/modules/system';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const formRef = ref();
const colleges = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const campusOptions = ref([]);

const filters = reactive({
  keyword: '',
  campusId: null,
  status: null
});

const collegeForm = ref(createDefaultForm());

const formRules = {
  collegeCode: [{ required: true, message: '请输入学院编码', trigger: 'blur' }],
  collegeName: [{ required: true, message: '请输入学院名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
};

function createDefaultForm() {
  return {
    id: null,
    collegeCode: '',
    collegeName: '',
    campusId: null,
    deanUserId: null,
    sortNo: 0,
    status: 1,
    remark: ''
  };
}

function getCampusName(campusId) {
  return campusOptions.value.find((item) => item.id === campusId)?.campusName || '--';
}

async function loadCampusOptions() {
  const response = await fetchCampusList();
  campusOptions.value = response.data || [];
}

async function loadColleges() {
  loading.value = true;
  try {
    const response = await fetchCollegePage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      campusId: filters.campusId ?? undefined,
      status: filters.status ?? undefined
    });
    const pageData = response.data || {};
    colleges.value = pageData.records || [];
    total.value = pageData.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadColleges();
}

function handleSearchReset() {
  handleSearch();
}

function handleReset() {
  filters.keyword = '';
  filters.campusId = null;
  filters.status = null;
  handleSearch();
}

function handlePageChange(page) {
  pageNum.value = page;
  loadColleges();
}

/** 新增学院：重置为默认表单并清除上一次的字段校验提示。 */
function handleCreate() {
  collegeForm.value = createDefaultForm();
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

/** 编辑学院：按详情接口回填表单（字段名与提交载荷保持不变）。 */
async function handleEdit(row) {
  const response = await fetchCollegeDetail(row.id);
  collegeForm.value = {
    ...createDefaultForm(),
    ...(response.data || {})
  };
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

/** 保存学院：校验通过后调用原保存接口，成功后提示并刷新列表。 */
async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  saving.value = true;
  try {
    await saveCollege(collegeForm.value);
    ElMessage.success(collegeForm.value.id ? '学院更新成功' : '学院创建成功');
    dialogVisible.value = false;
    await loadColleges();
  } finally {
    saving.value = false;
  }
}

/** 删除学院：二次确认后调用原删除接口并刷新列表。 */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除学院“${row.collegeName}”吗？`, '删除确认', {
    type: 'warning'
  });
  await deleteCollege(row.id);
  ElMessage.success('学院删除成功');
  if (colleges.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1;
  }
  await loadColleges();
}

onMounted(async () => {
  await loadCampusOptions();
  await loadColleges();
});
</script>

<style scoped>
.page-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-card {
  margin-bottom: 0;
}

.toolbar-grid {
  display: grid;
  grid-template-columns: minmax(260px, 1.5fr) 220px 180px auto;
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

@media (max-width: 1100px) {
  .toolbar-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
</style>
