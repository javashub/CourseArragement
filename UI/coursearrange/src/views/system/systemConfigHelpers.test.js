import test from 'node:test';
import assert from 'node:assert/strict';

import {
  buildTimeSlotTemplate,
  canManuallySaveConfig,
  createDefaultRuleFormData,
  validateScheduleRuleForm,
  validateTimeSlotTemplate
} from './systemConfigHelpers.js';

test('validateTimeSlotTemplate rejects incomplete template after rule structure changes', () => {
  const rule = {
    ...createDefaultRuleFormData(),
    weekDays: 5,
    dayPeriods: 8,
    morningPeriods: 5,
    afternoonPeriods: 3,
    nightPeriods: 0
  };

  const incompleteTemplate = buildTimeSlotTemplate({
    ...rule,
    weekDays: 1
  });

  const result = validateTimeSlotTemplate(incompleteTemplate, rule);

  assert.equal(result.isValid, false);
  assert.match(result.messages[0], /时间片模板未覆盖当前规则的全部星期和节次/);
  assert.equal(canManuallySaveConfig(validateScheduleRuleForm(rule), result, incompleteTemplate), false);
});

test('validateTimeSlotTemplate accepts fully generated template and manual save becomes available', () => {
  const rule = {
    ...createDefaultRuleFormData(),
    weekDays: 5,
    dayPeriods: 8,
    morningPeriods: 5,
    afternoonPeriods: 3,
    nightPeriods: 0
  };

  const template = buildTimeSlotTemplate(rule);
  const scheduleValidation = validateScheduleRuleForm(rule);
  const timeSlotValidation = validateTimeSlotTemplate(template, rule);

  assert.equal(template.length, 40);
  assert.equal(scheduleValidation.isValid, true);
  assert.equal(timeSlotValidation.isValid, true);
  assert.equal(canManuallySaveConfig(scheduleValidation, timeSlotValidation, template), true);
});
