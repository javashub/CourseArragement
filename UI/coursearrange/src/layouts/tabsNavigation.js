import { buildLayoutViewKey } from './layoutNavigation.js';

/**
 * 把当前路由合并进标签列表：按 path 去重，已存在时只更新标题与完整路径。
 * version 用于刷新时强制重建页面实例，普通路由切换必须保留原值。
 * @param {Array<{path: string, fullPath: string, title: string, version: number}>} tabs 现有标签
 * @param {{ path?: string, fullPath?: string, meta?: { title?: string } }} route 当前路由
 * @returns {Array} 新的标签列表
 */
export function upsertTab(tabs, route) {
  const list = tabs || [];
  const path = route?.path || '/';
  const title = route?.meta?.title || '页面';
  const fullPath = route?.fullPath || path;
  const index = list.findIndex((tab) => tab.path === path);

  if (index === -1) {
    return [...list, { path, fullPath, title, version: 0 }];
  }

  const next = [...list];
  next[index] = { ...next[index], fullPath, title };
  return next;
}

/**
 * 递增目标标签的 version，让 keep-alive 生成新的页面实例。
 * @param {Array} tabs 现有标签
 * @param {string} path 目标标签路径
 * @returns {Array} 新的标签列表
 */
export function refreshTab(tabs, path) {
  const list = tabs || [];
  const index = list.findIndex((tab) => tab.path === path);
  if (index === -1) {
    return list;
  }

  const next = [...list];
  next[index] = { ...next[index], version: (next[index].version || 0) + 1 };
  return next;
}

/**
 * keep-alive 的组件 key：路由身份 + 标签刷新版本。
 * 普通切换 version 不变，命中缓存；刷新后 version 变化，创建新实例。
 * @param {object} route 当前路由
 * @param {{ version?: number } | null | undefined} tab 当前标签
 * @returns {string}
 */
export function buildTabViewKey(route, tab) {
  return `${buildLayoutViewKey(route)}::${tab?.version ?? 0}`;
}

/**
 * 从标签列表移除指定路径，保持其余标签顺序不变。
 * @param {Array} tabs 现有标签
 * @param {string} path 要关闭的标签路径
 * @returns {Array} 新的标签列表
 */
export function removeTab(tabs, path) {
  return (tabs || []).filter((tab) => tab.path !== path);
}

/**
 * 关闭当前标签后选择相邻标签：优先左侧，其次右侧。
 * @param {Array} tabs 现有标签
 * @param {string} path 要关闭的标签路径
 * @returns {{path: string, fullPath: string, title: string, version: number} | null}
 */
export function resolveAdjacentTab(tabs, path) {
  const list = tabs || [];
  const index = list.findIndex((tab) => tab.path === path);
  if (index === -1) {
    return null;
  }
  return list[index - 1] || list[index + 1] || null;
}
