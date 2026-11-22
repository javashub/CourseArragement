import test from 'node:test';
import assert from 'node:assert/strict';

import {
  buildTabViewKey,
  refreshTab,
  removeTab,
  resolveAdjacentTab,
  upsertTab
} from './tabsNavigation.js';

const dashboardRoute = {
  path: '/dashboard',
  fullPath: '/dashboard',
  name: 'Dashboard',
  meta: { title: '工作台' }
};

test('upsertTab 追加新标签并初始化 version', () => {
  const tabs = upsertTab([], dashboardRoute);
  assert.deepEqual(tabs, [
    { path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 }
  ]);
});

test('upsertTab 对同一路径只更新不追加', () => {
  const initial = upsertTab([], dashboardRoute);
  const next = upsertTab(initial, {
    ...dashboardRoute,
    fullPath: '/dashboard?tab=1',
    meta: { title: '新标题' }
  });
  assert.equal(next.length, 1);
  assert.equal(next[0].fullPath, '/dashboard?tab=1');
  assert.equal(next[0].title, '新标题');
  assert.equal(next[0].version, 0);
});

test('upsertTab 保持标签首次打开顺序', () => {
  const first = upsertTab([], dashboardRoute);
  const second = upsertTab(first, {
    path: '/base-data/teachers',
    fullPath: '/base-data/teachers',
    meta: { title: '教师管理' }
  });
  assert.deepEqual(second.map((tab) => tab.path), ['/dashboard', '/base-data/teachers']);
});

test('upsertTab 对缺失标题使用兜底文案', () => {
  const tabs = upsertTab([], { path: '/profile', fullPath: '/profile' });
  assert.equal(tabs[0].title, '页面');
});

test('refreshTab 只递增目标标签的 version', () => {
  const tabs = [
    { path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 },
    { path: '/profile', fullPath: '/profile', title: '个人中心', version: 2 }
  ];
  const next = refreshTab(tabs, '/dashboard');
  assert.equal(next[0].version, 1);
  assert.equal(next[1].version, 2);
});

test('refreshTab 遇到不存在的路径保持原列表', () => {
  const tabs = [{ path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 }];
  assert.equal(refreshTab(tabs, '/missing'), tabs);
});

test('buildTabViewKey 在刷新后变化，普通切换保持稳定', () => {
  const tab = { path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 };
  const baseKey = buildTabViewKey(dashboardRoute, tab);
  assert.equal(baseKey, buildTabViewKey(dashboardRoute, tab));
  assert.notEqual(baseKey, buildTabViewKey(dashboardRoute, { ...tab, version: 1 }));
});

test('removeTab 删除目标标签并保持其余顺序', () => {
  const tabs = [
    { path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 },
    { path: '/profile', fullPath: '/profile', title: '个人中心', version: 0 }
  ];
  const next = removeTab(tabs, '/dashboard');
  assert.deepEqual(next.map((tab) => tab.path), ['/profile']);
  assert.equal(tabs.length, 2);
});

test('resolveAdjacentTab 优先选择左侧标签', () => {
  const tabs = [
    { path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 },
    { path: '/profile', fullPath: '/profile', title: '个人中心', version: 0 },
    { path: '/schedule', fullPath: '/schedule', title: '课表管理', version: 0 }
  ];
  assert.equal(resolveAdjacentTab(tabs, '/profile').path, '/dashboard');
});

test('resolveAdjacentTab 第一个标签关闭后选择右侧标签', () => {
  const tabs = [
    { path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 },
    { path: '/profile', fullPath: '/profile', title: '个人中心', version: 0 }
  ];
  assert.equal(resolveAdjacentTab(tabs, '/dashboard').path, '/profile');
});

test('resolveAdjacentTab 只有一个标签或路径不存在时返回 null', () => {
  const tabs = [{ path: '/dashboard', fullPath: '/dashboard', title: '工作台', version: 0 }];
  assert.equal(resolveAdjacentTab(tabs, '/dashboard'), null);
  assert.equal(resolveAdjacentTab(tabs, '/missing'), null);
});
