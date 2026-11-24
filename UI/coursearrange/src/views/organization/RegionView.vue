<template>
  <section class="page-card">
    <PageHeader
      title="行政区划"
      description="省 / 市 / 区县三级字典，从左至右逐级选择，供校区等组织数据引用。"
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

    <div v-if="selectedPath" class="region-path">
      当前选择：{{ selectedPath }}
    </div>

    <div class="region-cascade">
      <section class="cascade-panel">
        <header class="cascade-panel__head">
          <span>省</span>
          <span class="cascade-panel__count">{{ provinces.length }}</span>
        </header>
        <el-scrollbar class="cascade-panel__body">
          <ul v-loading="provinceLoading" class="cascade-list">
            <template v-if="provinces.length">
              <li
                v-for="item in provinces"
                :key="item.regionCode"
                class="cascade-item"
                :class="{ 'is-active': item.regionCode === selectedProvinceCode }"
                @click="handleSelectProvince(item)"
              >
                <span class="cascade-item__name">{{ item.regionName }}</span>
                <span class="cascade-item__code">{{ item.regionCode }}</span>
              </li>
            </template>
            <li v-else class="cascade-empty">暂无数据，请先点击右上角更新行政区划</li>
          </ul>
        </el-scrollbar>
      </section>

      <section class="cascade-panel">
        <header class="cascade-panel__head">
          <span>市</span>
          <span class="cascade-panel__count">{{ cities.length }}</span>
        </header>
        <el-scrollbar class="cascade-panel__body">
          <ul v-loading="cityLoading" class="cascade-list">
            <template v-if="cities.length">
              <li
                v-for="item in cities"
                :key="item.regionCode"
                class="cascade-item"
                :class="{ 'is-active': item.regionCode === selectedCityCode }"
                @click="handleSelectCity(item)"
              >
                <span class="cascade-item__name">{{ item.regionName }}</span>
                <span class="cascade-item__code">{{ item.regionCode }}</span>
              </li>
            </template>
            <li v-else class="cascade-empty">请先选择左侧省份</li>
          </ul>
        </el-scrollbar>
      </section>

      <section class="cascade-panel">
        <header class="cascade-panel__head">
          <span>区县</span>
          <span class="cascade-panel__count">{{ districts.length }}</span>
        </header>
        <el-scrollbar class="cascade-panel__body">
          <ul v-loading="districtLoading" class="cascade-list">
            <template v-if="districts.length">
              <li
                v-for="item in districts"
                :key="item.regionCode"
                class="cascade-item"
                :class="{ 'is-active': item.regionCode === selectedDistrictCode }"
                @click="selectedDistrictCode = item.regionCode"
              >
                <span class="cascade-item__name">{{ item.regionName }}</span>
                <span class="cascade-item__code">{{ item.regionCode }}</span>
              </li>
            </template>
            <li v-else class="cascade-empty">请先选择左侧城市</li>
          </ul>
        </el-scrollbar>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import PageHeader from '@/components/PageHeader.vue';
import { fetchRegionChildren, fetchRegionPage, syncRegionDivisions } from '@/api/modules/system';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

/** 三列数据：省固定拉取 parentCode='0'，市 / 区县随上一级选择联动。 */
const provinces = ref([]);
const cities = ref([]);
const districts = ref([]);
const provinceLoading = ref(false);
const cityLoading = ref(false);
const districtLoading = ref(false);

const selectedProvinceCode = ref('');
const selectedCityCode = ref('');
const selectedDistrictCode = ref('');

/** 字典总量，仅用于页头计数徽标。 */
const total = ref(0);
const syncing = ref(false);

/** 选中路径文案：省 / 市 / 区县，未选满时只拼已选部分。 */
const selectedPath = computed(() => {
  const parts = [];
  const province = provinces.value.find((item) => item.regionCode === selectedProvinceCode.value);
  const city = cities.value.find((item) => item.regionCode === selectedCityCode.value);
  const district = districts.value.find((item) => item.regionCode === selectedDistrictCode.value);
  if (province) {
    parts.push(province.regionName);
  }
  if (city) {
    parts.push(city.regionName);
  }
  if (district) {
    parts.push(district.regionName);
  }
  return parts.join(' / ');
});

/**
 * 拉取某一级的直属下级列表；失败时保留空列表态，
 * 错误提示由请求拦截器统一弹出，这里只记录日志避免双重弹窗。
 * @param {string} parentCode 父级区划代码，省级传 '0'
 * @returns {Promise<Array>} 下级区划数组
 */
async function loadChildren(parentCode) {
  try {
    const response = await fetchRegionChildren(parentCode);
    return response.data || [];
  } catch (error) {
    // 拦截器已向用户提示失败原因，返回空数组维持面板空态即可。
    console.error('加载行政区划下级失败', parentCode, error);
    return [];
  }
}

/** 加载省级列表与字典总量。 */
async function loadProvinces() {
  provinceLoading.value = true;
  provinces.value = await loadChildren('0');
  provinceLoading.value = false;
}

/** 字典总量只取分页接口的 total，不拉明细。 */
async function loadTotal() {
  try {
    const response = await fetchRegionPage({ pageNum: 1, pageSize: 1 });
    total.value = response.data?.total || 0;
  } catch (error) {
    console.error('加载行政区划总量失败', error);
  }
}

/**
 * 选择省份：清空右侧两级选择与数据，再拉取该省城市。
 * @param {{regionCode: string}} province 选中的省级节点
 */
async function handleSelectProvince(province) {
  selectedProvinceCode.value = province.regionCode;
  selectedCityCode.value = '';
  selectedDistrictCode.value = '';
  cities.value = [];
  districts.value = [];
  cityLoading.value = true;
  cities.value = await loadChildren(province.regionCode);
  cityLoading.value = false;
}

/**
 * 选择城市：清空区县选择与数据，再拉取该市区县。
 * @param {{regionCode: string}} city 选中的市级节点
 */
async function handleSelectCity(city) {
  selectedCityCode.value = city.regionCode;
  selectedDistrictCode.value = '';
  districts.value = [];
  districtLoading.value = true;
  districts.value = await loadChildren(city.regionCode);
  districtLoading.value = false;
}

/**
 * 手动触发行政区划更新：二次确认后调用同步接口，
 * 成功展示新增 / 更新 / 停用条数与耗时，并重置三列重新拉取省级数据。
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
    // 同步后字典整体变化，清空三列选择并重新加载。
    selectedProvinceCode.value = '';
    selectedCityCode.value = '';
    selectedDistrictCode.value = '';
    cities.value = [];
    districts.value = [];
    await Promise.all([loadProvinces(), loadTotal()]);
  } catch (error) {
    // 失败提示已由拦截器统一弹出，这里只记录日志；字典与面板保持原状。
    console.error('更新行政区划失败', error);
  } finally {
    syncing.value = false;
  }
}

onMounted(() => {
  loadProvinces();
  loadTotal();
});
</script>

<style scoped>
.page-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.region-path {
  color: var(--muted);
  font-size: 12.5px;
}

.region-cascade {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.cascade-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface);
}

.cascade-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 0 0 auto;
  padding: 10px 14px;
  border-bottom: 1px solid var(--line);
  color: var(--ink);
  font-size: 13.5px;
  font-weight: 600;
}

.cascade-panel__count {
  color: var(--muted);
  font-size: 12px;
  font-weight: 500;
}

.cascade-panel__body {
  height: 480px;
}

.cascade-list {
  margin: 0;
  padding: 6px;
  list-style: none;
}

.cascade-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 9px 10px;
  border-radius: var(--radius-control);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.cascade-item:hover {
  background: var(--bg);
}

.cascade-item.is-active {
  background: var(--brand-soft);
  color: var(--brand);
  font-weight: 600;
}

.cascade-item__name {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.cascade-item__code {
  flex: 0 0 auto;
  color: var(--muted);
  font-size: 11.5px;
  font-variant-numeric: tabular-nums;
}

.cascade-item.is-active .cascade-item__code {
  color: var(--brand);
}

.cascade-empty {
  padding: 24px 12px;
  color: var(--muted);
  font-size: 12.5px;
  text-align: center;
}

@media (max-width: 900px) {
  .region-cascade {
    grid-template-columns: 1fr;
  }

  .cascade-panel__body {
    height: 260px;
  }
}
</style>
