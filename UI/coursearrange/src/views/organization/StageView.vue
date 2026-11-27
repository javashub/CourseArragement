<template>
  <section class="page-card" v-loading="loading">
    <PageHeader title="学段管理" description="用于定义小学、初中、高中、大学等学段层级，后续排课规则会按学段差异化生效。">
      <template #actions>
        <el-button type="primary" @click="handleCreate">新增学段</el-button>
      </template>
    </PageHeader>

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
      <!-- A 方案紧凑表单：删除 intro banner 与分组描述，字段统一 2 列网格，示例写进 placeholder。 -->
      <el-form ref="formRef" :model="stageForm" :rules="formRules" label-position="top" class="academy-form">
        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">分类信息</div>
          </div>
          <!-- 2 列紧凑网格：编码 / 名称 同行，示例与格式全部写进 placeholder。 -->
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="学段编码" prop="stageCode">
              <el-input v-model="stageForm.stageCode" placeholder="例如 PRIMARY" />
            </el-form-item>
            <el-form-item label="学段名称" prop="stageName">
              <el-input v-model="stageForm.stageName" placeholder="例如 小学" />
            </el-form-item>
          </div>
        </div>

        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">排序与状态</div>
          </div>
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="学段层级" prop="stageLevel">
              <el-input-number
                v-model="stageForm.stageLevel"
                :min="1"
                :max="20"
                controls-position="right"
                placeholder="1-20，例如 1"
              />
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
import PageHeader from '@/components/PageHeader.vue';
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

/** 新增学段：重置为默认表单并清除上一次的字段校验提示。 */
function handleCreate() {
  stageForm.value = createDefaultForm();
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

/** 编辑学段：按详情接口回填表单（字段名与提交载荷保持不变）。 */
async function handleEdit(row) {
  const response = await fetchStageDetail(row.id);
  stageForm.value = {
    ...createDefaultForm(),
    ...(response.data || {})
  };
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

/** 保存学段：校验通过后调用原保存接口，成功后提示并刷新列表。 */
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

/** 删除学段：二次确认后调用原删除接口并刷新列表。 */
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
  grid-template-columns: 220px auto;
  gap: 12px;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .toolbar-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
</style>
