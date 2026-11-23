<template>
  <section v-loading="loading" class="page-card">
    <PageHeader
      title="行政区划"
      description="省市区三级区划字典，供校区等组织数据引用，可从公开数据源手动更新。"
      :count="total"
    >
      <template #actions>
        <el-button
          v-if="authStore.hasPermission('btn:region:sync')"
          type="primary"
          :loading="syncing"
          @click="handleSync"
        >
          更新行政区划
        </el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-grid">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索区划名称或代码，例如 北京市、110000"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="filters.regionLevel" placeholder="选择层级">
          <el-option label="全部层级" value="" />
          <el-option label="省" :value="1" />
          <el-option label="市" :value="2" />
          <el-option label="区县" :value="3" />
        </el-select>
        <el-input
          v-model="filters.parentCode"
          clearable
          placeholder="父级代码，例如 110000"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <div class="toolbar-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="regions" stripe>
        <template #empty>暂无数据，请先点击右上角更新</template>
        <el-table-column prop="regionCode" label="区划代码" min-width="120" />
        <el-table-column prop="regionName" label="名称" min-width="180" />
        <el-table-column label="层级" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" :type="levelTagType(row.regionLevel)">
              {{ levelText(row.regionLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentCode" label="父级代码" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
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
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import PageHeader from '@/components/PageHeader.vue';
import { fetchRegionPage, syncRegionDivisions } from '@/api/modules/system';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const loading = ref(false);
const syncing = ref(false);
const regions = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const filters = reactive({
  keyword: '',
  regionLevel: '',
  parentCode: ''
});

/** 层级代码转中文文案；未知层级回退为占位符，避免空白单元格。 */
function levelText(level) {
  return { 1: '省', 2: '市', 3: '区县' }[level] || '--';
}

/** 层级代码转 el-tag 配色，仅用 Element Plus 语义色，不引入新颜色。 */
function levelTagType(level) {
  return { 1: 'primary', 2: 'success', 3: 'warning' }[level] || 'info';
}

/**
 * 加载行政区划分页数据；失败时保留空列表态，
 * 错误提示已由请求拦截器统一弹出，这里只记录日志避免双重弹窗。
 */
async function loadRegions() {
  loading.value = true;
  try {
    const response = await fetchRegionPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      regionLevel: filters.regionLevel === '' ? undefined : filters.regionLevel,
      parentCode: filters.parentCode || undefined
    });
    const pageData = response.data || {};
    regions.value = pageData.records || [];
    total.value = pageData.total || 0;
  } catch (error) {
    // 拦截器已向用户提示失败原因，列表维持空态即可。
    console.error('加载行政区划失败', error);
  } finally {
    loading.value = false;
  }
}

/** 触发筛选查询：回到第一页再拉取，避免停留在越页码。 */
function handleSearch() {
  pageNum.value = 1;
  loadRegions();
}

/** 清空筛选条件并重新查询。 */
function handleReset() {
  filters.keyword = '';
  filters.regionLevel = '';
  filters.parentCode = '';
  handleSearch();
}

/** 分页切换。 */
function handlePageChange(page) {
  pageNum.value = page;
  loadRegions();
}

/**
 * 手动触发行政区划更新：二次确认后调用同步接口，
 * 成功展示新增 / 更新 / 停用条数与耗时并刷新列表。
 */
async function handleSync() {
  // 取消确认时静默返回，不当作错误处理。
  const confirmed = await ElMessageBox.confirm(
    '将从公开数据源拉取省市区三级行政区划并覆盖本地字典，是否继续？',
    '更新行政区划',
    { type: 'warning' }
  )
    .then(() => true)
    .catch(() => false);
  if (!confirmed) {
    return;
  }
  syncing.value = true;
  try {
    const response = await syncRegionDivisions();
    const result = response.data || {};
    ElMessage.success(
      `新增 ${result.inserted ?? 0} / 更新 ${result.updated ?? 0} / 停用 ${result.disabled ?? 0}，耗时 ${result.durationMs ?? 0} ms`
    );
    pageNum.value = 1;
    await loadRegions();
  } catch (error) {
    // 失败提示已由拦截器统一弹出，这里只记录日志；字典与列表保持原状。
    console.error('更新行政区划失败', error);
  } finally {
    syncing.value = false;
  }
}

onMounted(() => {
  loadRegions();
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
  grid-template-columns: minmax(280px, 1.5fr) 160px minmax(220px, 1fr) auto;
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
