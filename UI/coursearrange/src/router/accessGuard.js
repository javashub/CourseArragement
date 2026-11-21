/**
 * 路由访问判定：权限优先，菜单路径只做展示。
 * 返回值：null 表示放行；字符串表示重定向目标。
 */
export function resolveAccessRedirect({
  permission,
  hasPermission,
  firstAccessiblePath,
  targetPath
} = {}) {
  if (!permission) {
    return null;
  }
  if (hasPermission) {
    return null;
  }
  if (firstAccessiblePath && firstAccessiblePath !== targetPath) {
    return firstAccessiblePath;
  }
  return '/login';
}
