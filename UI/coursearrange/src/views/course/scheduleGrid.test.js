import test from 'node:test';
import assert from 'node:assert/strict';

import {
  buildPeriodLabels,
  buildWeekLabels,
  parseClassTime,
  resolvePlanPosition,
  toClassTime
} from './scheduleGrid.js';

test('parseClassTime supports new four-digit weekday-period encoding', () => {
  assert.deepEqual(parseClassTime('0108'), {
    dayIndex: 1,
    periodIndex: 8
  });
  assert.deepEqual(parseClassTime('0506'), {
    dayIndex: 5,
    periodIndex: 6
  });
});

test('toClassTime outputs four-digit encoding for drag adjust requests', () => {
  assert.equal(toClassTime(1, 8), '0108');
  assert.equal(toClassTime(5, 6), '0506');
});

test('build grid labels follows active schedule rule instead of fixed 5x5', () => {
  const scheduleConfig = {
    scheduleRule: {
      weekDays: 5,
      dayPeriods: 8
    }
  };

  const weekLabels = buildWeekLabels(scheduleConfig, []);
  const periodLabels = buildPeriodLabels(scheduleConfig, []);

  assert.equal(weekLabels.length, 5);
  assert.equal(periodLabels.length, 8);
  assert.equal(periodLabels[7].label, '第8节');
});

test('resolvePlanPosition prefers explicit weekday and period from backend', () => {
  assert.deepEqual(
    resolvePlanPosition({
      weekdayNo: 3,
      periodNo: 7,
      classTime: '0101'
    }),
    {
      dayIndex: 3,
      periodIndex: 7
    }
  );
});
