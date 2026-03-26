import test from 'node:test';
import assert from 'node:assert/strict';

import {
  buildLayoutViewKey,
  findMenuTrailByPath,
  resolveActiveMenuPath,
  resolveOpenMenuIndexes
} from './layoutNavigation.js';

const menus = [
  {
    menuCode: 'dashboard',
    routePath: '/dashboard'
  },
  {
    menuCode: 'base-data',
    routePath: '/base-data',
    children: [
      {
        menuCode: 'teacher-manage',
        routePath: '/base-data/teachers'
      },
      {
        menuCode: 'student-manage',
        routePath: '/base-data/students'
      }
    ]
  },
  {
    menuCode: 'system',
    routePath: '/system',
    children: [
      {
        menuCode: 'system-config',
        routePath: '/system/config'
      }
    ]
  }
];

test('findMenuTrailByPath returns full menu trail for nested route', () => {
  assert.deepEqual(
    findMenuTrailByPath('/base-data/students', menus).map((item) => item.routePath),
    ['/base-data', '/base-data/students']
  );
});

test('resolveActiveMenuPath uses exact leaf route when current path matches child menu', () => {
  assert.equal(resolveActiveMenuPath('/base-data/students', menus), '/base-data/students');
});

test('resolveOpenMenuIndexes keeps parent group expanded for current child route', () => {
  assert.deepEqual(resolveOpenMenuIndexes('/base-data/students', menus), ['/base-data']);
});

test('buildLayoutViewKey stays stable for same route and separates shared-page contexts', () => {
  assert.equal(
    buildLayoutViewKey({
      path: '/base-data/teachers',
      name: 'TeacherManagePage',
      meta: {
        resourceTab: 'teacher'
      }
    }),
    'TeacherManagePage::/base-data/teachers::teacher'
  );

  assert.equal(
    buildLayoutViewKey({
      path: '/base-data/students',
      name: 'StudentManagePage',
      meta: {
        resourceTab: 'student'
      }
    }),
    'StudentManagePage::/base-data/students::student'
  );
});
