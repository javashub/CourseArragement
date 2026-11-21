import test from 'node:test';
import assert from 'node:assert/strict';
import { resolveAccessRedirect } from './accessGuard.js';

test('无 meta.permission 的路由直接放行', () => {
  assert.equal(
    resolveAccessRedirect({ permission: undefined, hasPermission: false, firstAccessiblePath: '', targetPath: '/x' }),
    null
  );
});

test('具备权限时放行', () => {
  assert.equal(
    resolveAccessRedirect({
      permission: 'page:dashboard:view',
      hasPermission: true,
      firstAccessiblePath: '/teacher/dashboard',
      targetPath: '/dashboard'
    }),
    null
  );
});

test('无权限时重定向到首个可访问路径', () => {
  assert.equal(
    resolveAccessRedirect({
      permission: 'page:dashboard:view',
      hasPermission: false,
      firstAccessiblePath: '/teacher/dashboard',
      targetPath: '/dashboard'
    }),
    '/teacher/dashboard'
  );
});

test('无权限且没有可访问路径时回登录页', () => {
  assert.equal(
    resolveAccessRedirect({
      permission: 'page:dashboard:view',
      hasPermission: false,
      firstAccessiblePath: '',
      targetPath: '/dashboard'
    }),
    '/login'
  );
});

test('重定向目标等于当前路径时回登录页，避免循环', () => {
  assert.equal(
    resolveAccessRedirect({
      permission: 'page:dashboard:view',
      hasPermission: false,
      firstAccessiblePath: '/dashboard',
      targetPath: '/dashboard'
    }),
    '/login'
  );
});
