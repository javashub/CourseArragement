<template>
  <section class="page-card" v-loading="loading">
    <div class="page-header">
      <div>
        <h1 class="page-title">学段管理</h1>
        <p class="page-description">用于定义小学、初中、高中、大学等学段层级，后续排课规则会按学段差异化生效。</p>
      </div>
      <el-button type="primary" @click="handleCreate">新增学段</el-button>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-grid">
        <el-select
          v-model="filters.status"
          clearable
          placeholder="筛选状态，例如 启用"
          @change="loadStages"
          @clear="loadStages"
        >
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <div class="toolbar-actions">
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="stages" stripe>
        <el-table-column prop="stageCode" label="学段编码" min-width="160" />
        <el-table-column prop="stageName" label="学段名称" min-width="180" />
        <el-table-column prop="stageLevel" label="层级" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="stageForm.id ? '编辑学段' : '新增学段'"
      width="760px"
      append-to-body
      class="academy-form-dialog"
    >
      <div class="academy-form-intro">
        <div class="academy-form-intro__eyebrow">Stage Taxonomy</div>
        <div class="academy-form-intro__title">{{ stageForm.id ? '更新学段定义' : '建立学段定义' }}</div>
        <div class="academy-form-intro__text">
          学段会参与排课规则分层、资源过滤和范围配置，建议保持编码清晰、层级稳定，便于后续扩展更多教学场景。
        </div>
      </div>

      <el-form ref="formRef" :model="stageForm" :rules="formRules" label-position="top" class="academy-form">
        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">分类信息</div>
            <div class="academy-form-section__desc">先定义学段编码、名称和层级，用于系统内统一识别。</div>
          </div>
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="学段编码" prop="stageCode">
              <el-input v-model="stageForm.stageCode" placeholder="例如 PRIMARY、JUNIOR_HIGH、UNIVERSITY" />
            </el-form-item>
            <el-form-item label="学段名称" prop="stageName">
              <el-input v-model="stageForm.stageName" placeholder="例如 小学、初中、大学" />
            </el-form-item>
          </div>
        </div>

        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">排序与状态</div>
            <div class="academy-form-section__desc">层级用于表达学段顺序，状态决定是否参与范围配置与规则下发。</div>
          </div>
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="学段层级" prop="stageLevel">
              <el-input-number v-model="stageForm.stageLevel" :min="1" :max="20" controls-position="right" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="stageForm.status" class="academy-radio-group">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <el-form-item label="备注">
            <el-input
              v-model="stageForm.remark"
              type="textarea"
              :rows="3"
              placeholder="例如 大学学段支持多学院、多专业排课"
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
import { deleteStage, fetchStageDetail, fetchStageList, saveStage } from '@/api/modules/system';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const formRef = ref();
const stages = ref([]);

const filters = reactive({
  status: null
});

const stageForm = ref(createDefaultForm());

const formRules = {
  stageCode: [{ required: true, message: '请输入学段编码', trigger: 'blur' }],
  stageName: [{ required: true, message: '请输入学段名称', trigger: 'blur' }],
  stageLevel: [{ required: true, message: '请输入学段层级', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
};

function createDefaultForm() {
  return {
    id: null,
    stageCode: '',
    stageName: '',
    stageLevel: 1,
    status: 1,
    remark: ''
  };
}

async function loadStages() {
  loading.value = true;
  try {
    const response = await fetchStageList({
      status: filters.status ?? undefined
    });
    stages.value = response.data || [];
  } finally {
    loading.value = false;
  }
}

function handleReset() {
  filters.status = null;
  loadStages();
}

function handleCreate() {
  stageForm.value = createDefaultForm();
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

async function handleEdit(row) {
  const response = await fetchStageDetail(row.id);
  stageForm.value = {
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
    await saveStage(stageForm.value);
    ElMessage.success(stageForm.value.id ? '学段更新成功' : '学段创建成功');
    dialogVisible.value = false;
    await loadStages();
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除学段“${row.stageName}”吗？`, '删除确认', {
    type: 'warning'
  });
  await deleteStage(row.id);
  ElMessage.success('学段删除成功');
  await loadStages();
}

onMounted(() => {
  loadStages();
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
  grid-template-columns: 220px auto;
  gap: 12px;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
