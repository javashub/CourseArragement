import test from 'node:test';
import assert from 'node:assert/strict';

import { BASE_DATA_HERO_COPY, BASE_DATA_HERO_STATS } from './baseDataHero.js';

test('base data hero copy stays concise and guidance-focused', () => {
  assert.equal(
    BASE_DATA_HERO_COPY,
    '统一维护教师、学生、课程、教室等基础资源；先切换页签，再结合筛选、导入导出和新增编辑完成日常维护。'
  );
});

test('base data hero removes redundant status cards', () => {
  assert.deepEqual(BASE_DATA_HERO_STATS, []);
});
