function normalizePath(path) {
  if (!path) {
    return '';
  }
  if (path === '/') {
    return '/';
  }
  return path.endsWith('/') ? path.slice(0, -1) : path;
}

function flattenMenus(menus, trail = []) {
  return (menus || []).flatMap((menu) => {
    if (!menu || menu.isHidden === 1) {
      return [];
    }
    const nextTrail = [...trail, menu];
    const current = menu.routePath
      ? [
          {
            menu,
            trail: nextTrail
          }
        ]
      : [];
    return [...current, ...flattenMenus(menu.children || [], nextTrail)];
  });
}

function isPathMatch(currentPath, menuPath) {
  const normalizedCurrentPath = normalizePath(currentPath);
  const normalizedMenuPath = normalizePath(menuPath);
  if (!normalizedCurrentPath || !normalizedMenuPath) {
    return false;
  }
  return normalizedCurrentPath === normalizedMenuPath || normalizedCurrentPath.startsWith(`${normalizedMenuPath}/`);
}

export function findMenuTrailByPath(currentPath, menus) {
  const flattened = flattenMenus(menus);
  const exactMatch = flattened.find(({ menu }) => normalizePath(menu.routePath) === normalizePath(currentPath));
  if (exactMatch) {
    return exactMatch.trail;
  }

  const prefixMatches = flattened
    .filter(({ menu }) => isPathMatch(currentPath, menu.routePath))
    .sort((left, right) => normalizePath(right.menu.routePath).length - normalizePath(left.menu.routePath).length);

  return prefixMatches[0]?.trail || [];
}

export function resolveActiveMenuPath(currentPath, menus) {
  const trail = findMenuTrailByPath(currentPath, menus);
  return normalizePath(trail.at(-1)?.routePath) || normalizePath(currentPath);
}

export function resolveOpenMenuIndexes(currentPath, menus) {
  const trail = findMenuTrailByPath(currentPath, menus);
  return trail
    .slice(0, -1)
    .map((menu) => normalizePath(menu.routePath) || menu.menuCode)
    .filter(Boolean);
}

export function buildLayoutViewKey(route) {
  const routeName = route?.name ? String(route.name) : 'anonymous';
  const routePath = normalizePath(route?.path) || '/';
  const routeContext = route?.meta?.resourceTab ? String(route.meta.resourceTab) : '';
  return [routeName, routePath, routeContext].join('::');
}

function shouldKeepMenu(menu) {
  if (!menu || menu.isHidden === 1) {
    return false;
  }
  return Boolean(menu.routePath || (menu.children && menu.children.length));
}

function sortMenus(left, right) {
  return (left?.sortNo || 0) - (right?.sortNo || 0);
}

/**
 * 侧栏与顶栏共用的菜单归一：过滤隐藏项，补齐缺失的系统分组，并按 sortNo 排序。
 * 输出结构直接喂给 el-menu / TopNav，两种壳渲染同一棵树。
 */
export function normalizeMenus(menus) {
  const clonedMenus = JSON.parse(JSON.stringify(menus || []));
  const topLevelMenus = clonedMenus.filter((item) => item?.isHidden !== 1);
  const systemRoot = topLevelMenus.find(
    (item) => item?.menuCode === 'system' || item?.routePath === '/system'
  );

  if (!systemRoot) {
    const systemChildren = topLevelMenus.filter((item) => item?.routePath?.startsWith('/system/'));
    if (systemChildren.length) {
      const systemChildKeys = new Set(systemChildren.map((item) => item.id || item.menuCode));
      const remainingMenus = topLevelMenus.filter(
        (item) => !systemChildKeys.has(item.id || item.menuCode)
      );
      remainingMenus.push({
        id: 'system-catalog',
        menuCode: 'system',
        parentId: 0,
        menuName: '系统管理',
        menuType: 'CATALOG',
        routeName: 'SystemManage',
        routePath: '/system',
        icon: 'Setting',
        sortNo: 900,
        children: systemChildren.sort(sortMenus)
      });
      return remainingMenus.filter(shouldKeepMenu).sort(sortMenus);
    }
  }

  return topLevelMenus
    .map((item) => ({
      ...item,
      children: (item.children || []).filter(shouldKeepMenu).sort(sortMenus)
    }))
    .filter(shouldKeepMenu)
    .sort(sortMenus);
}

/** 顶栏一级项是否处于激活态（当前路由是它本身或它的子路由）。 */
export function isMenuActive(currentPath, menuPath) {
  return isPathMatch(currentPath, menuPath);
}
