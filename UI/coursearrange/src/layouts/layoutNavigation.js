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
