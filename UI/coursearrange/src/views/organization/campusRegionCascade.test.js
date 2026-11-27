import test from 'node:test';
import assert from 'node:assert/strict';

import {
  buildRegionOptions,
  resolveRegionDisplay,
  clearDownstream,
  toRegionSelectOptions
} from './campusRegionCascade.js';

/** 模拟接口返回的直属下级列表（省级 / 市级 / 区县级通用形状）。 */
const REGION_LIST = [
  { regionCode: '450000', regionName: '广西壮族自治区', parentCode: '0', regionLevel: 1 },
  { regionCode: '110000', regionName: '北京市', parentCode: '0', regionLevel: 1 }
];

/** 场景一：正常回显——字典命中时按名称显示，选项结构正确。 */
test('正常回显：映射接口数据为 {value,label}，编码可解析出中文名称', () => {
  const options = buildRegionOptions(REGION_LIST, '450000');

  assert.deepEqual(options, [
    { value: '450000', label: '广西壮族自治区' },
    { value: '110000', label: '北京市' }
  ]);
  assert.equal(resolveRegionDisplay(options, '450000'), '广西壮族自治区');
  assert.equal(resolveRegionDisplay(options, '110000'), '北京市');
  // 找不到时返回编码本身；编码为空时返回 '--'。
  assert.equal(resolveRegionDisplay(options, '999999'), '999999');
  assert.equal(resolveRegionDisplay(options, ''), '--');
  assert.equal(resolveRegionDisplay(options, null), '--');
});

/** 场景二：软停用编码兜底——字典查不到的旧编码补占位，label 显示编码本身。 */
test('软停用编码兜底：字典中不存在的已存编码补占位选项并显示编码', () => {
  const options = buildRegionOptions(REGION_LIST, '450803');

  assert.deepEqual(options, [
    { value: '450000', label: '广西壮族自治区' },
    { value: '110000', label: '北京市' },
    { value: '450803', label: '450803' }
  ]);
  assert.equal(resolveRegionDisplay(options, '450803'), '450803');
  // 转成 el-option 形状后，模板仍可用 regionName / regionCode 绑定。
  assert.deepEqual(toRegionSelectOptions(options).at(-1), {
    regionCode: '450803',
    regionName: '450803'
  });
  // 已在字典中的编码不重复补占位。
  assert.equal(buildRegionOptions(REGION_LIST, '450000').length, 2);
  // fallbackCode 为空（新增表单）不追加空选项。
  assert.equal(buildRegionOptions(REGION_LIST, '').length, 2);
});

/** 场景三：改选上级清空下级——省改选清市/区县，市改选只清区县，不触碰其它字段。 */
test('改选上级清空下级：返回字段补丁且不影响上级与其它业务字段', () => {
  const form = {
    id: 7,
    campusCode: 'CAMPUS_MAIN',
    provinceCode: '450000',
    cityCode: '450800',
    districtCode: '450803'
  };

  // 改选省份：市与区县都失效。
  assert.deepEqual(clearDownstream(form, 'province'), {
    cityCode: '',
    districtCode: ''
  });
  // 改选城市：只有区县失效。
  assert.deepEqual(clearDownstream(form, 'city'), { districtCode: '' });
  // 未知上级（防御调用）：不产生任何清空字段。
  assert.deepEqual(clearDownstream(form, 'district'), {});

  // 模拟页面 Object.assign 合并，确认原编码被清空但上级字段与其它字段保持原值。
  const afterProvinceChange = Object.assign({}, form, clearDownstream(form, 'province'));
  assert.equal(afterProvinceChange.cityCode, '');
  assert.equal(afterProvinceChange.districtCode, '');
  assert.equal(afterProvinceChange.provinceCode, '450000');
  assert.equal(afterProvinceChange.campusCode, 'CAMPUS_MAIN');
});

/** 场景四：空列表——不抛错，按 fallbackCode 补编码占位；无编码时返回空数组。 */
test('空列表：接口无数据时不阻断编辑，按已存编码兜底；无编码则选项为空', () => {
  // 编辑一条已存编码的校区，字典表为空：仍能补出可回显的占位选项。
  assert.deepEqual(buildRegionOptions([], '450803'), [{ value: '450803', label: '450803' }]);
  assert.deepEqual(buildRegionOptions(null, '450000'), [{ value: '450000', label: '450000' }]);
  // 新增表单（无已存编码）且字典为空：选项为空，表单照常可保存原有编码。
  assert.deepEqual(buildRegionOptions([], ''), []);
  assert.deepEqual(buildRegionOptions(undefined, undefined), []);
  assert.equal(resolveRegionDisplay([], '450803'), '450803');
  // 脏数据（缺少 regionCode）被丢弃，不产生无效 value。
  assert.deepEqual(buildRegionOptions([{ regionName: '无名' }, { regionCode: '', regionName: '' }]), []);
});
