<template>
  <section class="page-card" v-loading="loading">
    <PageHeader title="校区管理" description="维护多校区办学场景下的校区信息，后续教学楼、教室、教师归属都会挂在这里。">
      <template #actions>
        <el-button type="primary" @click="handleCreate">新增校区</el-button>
      </template>
    </PageHeader>

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

    <!-- A 方案紧凑表单：无 intro banner / 分组描述，只留短标题 + 细分隔线，footer 常驻。 -->
    <el-dialog
      v-model="dialogVisible"
      :title="campusForm.id ? '编辑校区' : '新增校区'"
      width="820px"
      append-to-body
      class="academy-form-dialog"
    >
      <el-form ref="formRef" :model="campusForm" :rules="formRules" label-position="top" class="academy-form">
        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">基础信息</div>
          </div>
          <!-- 2 列网格：编码 / 名称 / 类型 / 状态 紧凑排布，示例全部写进 placeholder。 -->
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="校区编码" prop="campusCode">
              <el-input v-model="campusForm.campusCode" placeholder="例如 CAMPUS_MAIN" />
            </el-form-item>
            <el-form-item label="校区名称" prop="campusName">
              <el-input v-model="campusForm.campusName" placeholder="例如 主校区" />
            </el-form-item>
            <el-form-item label="校区类型" prop="campusType">
              <el-input v-model="campusForm.campusType" placeholder="例如 主校区、分校区" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="campusForm.status" class="academy-radio-group">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>

        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">区域与地址</div>
          </div>
          <!-- 省 / 市 / 区县三级联动：按名称点选、按编码落库，v-model 仍绑定原三个字段。 -->
          <div class="academy-form-grid academy-form-grid--three">
            <el-form-item label="省份编码">
              <el-select
                v-model="campusForm.provinceCode"
                clearable
                filterable
                placeholder="选择省"
                @change="handleProvinceChange"
              >
                <el-option
                  v-for="item in provinceOptions"
                  :key="item.regionCode"
                  :label="item.regionName"
                  :value="item.regionCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="城市编码">
              <el-select
                v-model="campusForm.cityCode"
                clearable
                filterable
                placeholder="选择市"
                @change="handleCityChange"
              >
                <el-option
                  v-for="item in cityOptions"
                  :key="item.regionCode"
                  :label="item.regionName"
                  :value="item.regionCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="区县编码">
              <el-select v-model="campusForm.districtCode" clearable filterable placeholder="选择区县">
                <el-option
                  v-for="item in districtOptions"
                  :key="item.regionCode"
                  :label="item.regionName"
                  :value="item.regionCode"
                />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="详细地址">
            <el-input v-model="campusForm.address" placeholder="例如 北京市海淀区学院路 1 号" />
          </el-form-item>
        </div>

        <div class="academy-form-section">
          <div class="academy-form-section__header">
            <div class="academy-form-section__title">展示与备注</div>
          </div>
          <div class="academy-form-grid academy-form-grid--two">
            <el-form-item label="排序">
              <el-input-number
                v-model="campusForm.sortNo"
                :min="0"
                :max="9999"
                controls-position="right"
                placeholder="0-9999，例如 10"
              />
            </el-form-item>
          </div>
          <el-form-item label="备注">
            <el-input
              v-model="campusForm.remark"
              type="textarea"
              :rows="3"
              placeholder="例如 承担高中部与大学部课程安排"
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
  deleteCampus,
  fetchCampusDetail,
  fetchCampusPage,
  fetchRegionChildren,
  saveCampus
} from '@/api/modules/system';
import {
  buildRegionOptions,
  clearDownstream,
  toRegionSelectOptions
} from './campusRegionCascade';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const formRef = ref();
const campuses = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

/** 省 / 市 / 区县三级下拉选项，初始为空；新增与编辑时按层级填充。 */
const provinceOptions = ref([]);
const cityOptions = ref([]);
const districtOptions = ref([]);

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

/** 列表仍按编码拼接展示，本次改造不扩到列表显示中文名称。 */
function formatRegion(row) {
  return [row.provinceCode, row.cityCode, row.districtCode].filter(Boolean).join(' / ') || '--';
}

/**
 * 拉取某一级的直属下级列表并转成 el-option 选项。
 * 失败时返回兜底选项（编码本身），错误提示由请求拦截器统一弹出，这里只记日志，
 * 保证字典为空 / 接口不可用时编辑弹窗仍能打开、原编码不丢失。
 * @param {string} parentCode 父级编码，省级传 '0'
 * @param {string} fallbackCode 需要保证可回显的已存编码（新增时传空）
 * @returns {Promise<Array<{regionCode: string, regionName: string}>>} 下拉选项
 */
async function loadRegionOptions(parentCode, fallbackCode) {
  try {
    const response = await fetchRegionChildren(parentCode);
    return toRegionSelectOptions(buildRegionOptions(response.data || [], fallbackCode));
  } catch (error) {
    // 拦截器已提示失败原因；仅补编码占位，避免阻断编辑与提交。
    console.error('加载行政区划下级失败', parentCode, error);
    return toRegionSelectOptions(buildRegionOptions([], fallbackCode));
  }
}

/** 加载省级选项：parentCode='0'。 */
async function loadProvinceOptions() {
  provinceOptions.value = await loadRegionOptions('0', '');
}

/**
 * 改选省份：清空市、区县两个字段及两级选项，再按新省份编码拉取城市列表。
 * @param {string} code 选中的省份编码
 */
async function handleProvinceChange(code) {
  Object.assign(campusForm.value, clearDownstream(campusForm.value, 'province'));
  cityOptions.value = [];
  districtOptions.value = [];
  if (code) {
    cityOptions.value = await loadRegionOptions(code, '');
  }
}

/**
 * 改选城市：清空区县字段及选项，再按新城市编码拉取区县列表。
 * @param {string} code 选中的城市编码
 */
async function handleCityChange(code) {
  Object.assign(campusForm.value, clearDownstream(campusForm.value, 'city'));
  districtOptions.value = [];
  if (code) {
    districtOptions.value = await loadRegionOptions(code, '');
  }
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

/** 新增：重置表单、清空三级选项，只拉省级列表即可逐级点选。 */
function handleCreate() {
  campusForm.value = createDefaultForm();
  provinceOptions.value = [];
  cityOptions.value = [];
  districtOptions.value = [];
  dialogVisible.value = true;
  formRef.value?.clearValidate();
  loadProvinceOptions();
}

/**
 * 编辑：回填表单后按已存编码依次拉省 → 市 → 区县列表。
 * 各级把已存编码作为 fallbackCode 传入，字典查不到（含软停用）时补编码占位，
 * 保证 el-select 能显示原值、保存仍提交原编码；任一级失败都不会阻断弹窗。
 */
async function handleEdit(row) {
  const response = await fetchCampusDetail(row.id);
  campusForm.value = {
    ...createDefaultForm(),
    ...(response.data || {})
  };
  provinceOptions.value = [];
  cityOptions.value = [];
  districtOptions.value = [];
  dialogVisible.value = true;
  formRef.value?.clearValidate();

  const { provinceCode, cityCode, districtCode } = campusForm.value;
  // 省级：parentCode='0'，用已存省编码兜底回显。
  provinceOptions.value = await loadRegionOptions('0', provinceCode);
  // 有省编码才拉市级，并用已存市编码兜底回显。
  if (provinceCode) {
    cityOptions.value = await loadRegionOptions(provinceCode, cityCode);
  }
  // 有市编码才拉区县级，并用已存区县编码兜底回显。
  if (cityCode) {
    districtOptions.value = await loadRegionOptions(cityCode, districtCode);
  }
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
  .toolbar-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
</style>
