/**
 * 校区行政区划三级级联的纯逻辑 helper。
 *
 * 约定：接口 `GET /organization/regions/children` 返回 `[{regionCode, regionName, ...}]`，
 * 本模块把选项抽象成 `{value, label}`（`value` 为区划编码、`label` 为显示名称），
 * 便于脱离 Vue / Element Plus 做纯函数测试；页面用 `toRegionSelectOptions` 转成
 * `el-option` 需要的 `{regionCode, regionName}` 形状，模板因此可直接按原始键名绑定。
 *
 * 软停用兜底：`children` 接口只返回 `deleted=0` 的区划，已被软停用或尚未同步的旧编码
 * 不在字典里，统一补一条「label 即编码」的占位选项，保证编辑回显不丢值、下拉不空白。
 * 因此 `resolveRegionDisplay` 在本模块里主要用于纯逻辑校验与只读文案，页面下拉的显示
 * 由 el-option 的 label 承担。
 */

/** 编码为空时的列表占位显示文案。 */
const EMPTY_DISPLAY = '--';

/**
 * 把接口返回的区划列表映射为下拉选项。
 *
 * 若 `list` 为空、或 `fallbackCode` 不在映射结果中（软停用 / 字典缺失），
 * 则追加一条 `{value: fallbackCode, label: fallbackCode}` 占位选项；
 * `fallbackCode` 为空（'' / null / undefined）时不追加，避免新增表单出现空选项。
 *
 * @param {Array<{regionCode: string, regionName: string}>} list 接口返回的直属下级列表
 * @param {string} [fallbackCode] 需要保证可回显的已存编码（编辑态传表单里的编码）
 * @returns {Array<{value: string, label: string}>} 下拉选项
 */
export function buildRegionOptions(list, fallbackCode) {
  const source = Array.isArray(list) ? list : [];
  const options = source
    // 丢弃没有编码的脏数据，避免产生 value='' 的无效选项。
    .filter((item) => item && item.regionCode !== undefined && item.regionCode !== null && item.regionCode !== '')
    .map((item) => {
      const value = String(item.regionCode);
      const name = item.regionName;
      // 名称缺失时退化为编码，保证 el-select 永远有可显示文案。
      const label = name === undefined || name === null || name === '' ? value : String(name);
      return { value, label };
    });

  const code = normalizeCode(fallbackCode);
  if (code && !options.some((item) => item.value === code)) {
    // 软停用旧编码兜底：显示编码本身，提交时仍写回原编码。
    options.push({ value: code, label: code });
  }
  return options;
}

/**
 * 归一化编码：空值（undefined / null / ''）统一返回 ''，其余转成字符串，
 * 避免接口返回数字编码时与表单里的字符串编码比较不上。
 * @param {string|number|null|undefined} code 区划编码
 * @returns {string} 归一化后的编码
 */
function normalizeCode(code) {
  return code === undefined || code === null || code === '' ? '' : String(code);
}

/**
 * 按编码从选项中解析显示名称。
 *
 * 找不到对应选项时返回编码本身（软停用旧编码的兜底显示）；
 * 编码为空时返回 '--'，供列表 / 只读文案使用。
 *
 * @param {Array<{value: string, label: string}>} options `buildRegionOptions` 产出的选项
 * @param {string} code 待显示的区划编码
 * @returns {string} 显示名称
 */
export function resolveRegionDisplay(options, code) {
  const value = normalizeCode(code);
  if (!value) {
    return EMPTY_DISPLAY;
  }
  const matched = (Array.isArray(options) ? options : []).find(
    (item) => item && String(item.value) === value
  );
  return matched ? matched.label : value;
}

/**
 * 计算「改选上级后清空下级」的字段补丁。
 *
 * 省份改选时市、区县都不再属于新上级，需一并清空；城市改选时只清空区县；
 * 返回的是「字段补丁」而非完整表单，调用方用 `Object.assign` 合并回去即可，
 * 不会触碰上级字段以及 id、名称等其它业务字段。仅对表单里已存在的键生成补丁，
 * 避免向提交载荷引入新字段。
 *
 * @param {{provinceCode?: string, cityCode?: string, districtCode?: string}} current 当前校区表单快照
 * @param {'province'|'city'} parentChanged 发生改选的上级：province 省 / city 市
 * @returns {{cityCode?: string, districtCode?: string}} 需要清空的下级字段补丁
 */
export function clearDownstream(current, parentChanged) {
  // current 为当前表单快照：只为其中已存在的下级字段生成补丁，避免向提交载荷注入新键；
  // 纯函数直接调用（不传表单）时按标准字段集合返回，返回值始终可预期。
  const has = (field) =>
    !current ||
    typeof current !== 'object' ||
    Object.prototype.hasOwnProperty.call(current, field);
  const patch = {};
  // 省份改选：市、区县均不再属于新上级；城市改选：只清空区县。
  if (parentChanged === 'province' || parentChanged === 'city') {
    if (has('districtCode')) {
      patch.districtCode = '';
    }
  }
  if (parentChanged === 'province' && has('cityCode')) {
    patch.cityCode = '';
  }
  return patch;
}

/**
 * 把 helper 的 `{value, label}` 选项转换成 `el-option` 直接使用的
 * `{regionCode, regionName}`，使模板保持 `:label="item.regionName" :value="item.regionCode"` 的写法。
 *
 * @param {Array<{value: string, label: string}>} options `buildRegionOptions` 产出的选项
 * @returns {Array<{regionCode: string, regionName: string}>} 下拉组件选项
 */
export function toRegionSelectOptions(options) {
  return (Array.isArray(options) ? options : []).map((item) => ({
    regionCode: item.value,
    regionName: item.label
  }));
}
