/**
 * 系统配置页默认排课规则表单。
 */
export function createDefaultRuleFormData() {
  return {
    id: null,
    ruleCode: 'RULE_GLOBAL_DEFAULT',
    ruleName: '全局默认排课规则',
    weekDays: 5,
    dayPeriods: 8,
    morningPeriods: 4,
    afternoonPeriods: 4,
    nightPeriods: 0,
    allowWeekend: 0,
    defaultContinuousLimit: 2,
    status: 1,
    isDefault: 1,
    remark: ''
  };
}

/**
 * 系统配置页时间片行的默认结构。
 */
export function createEmptyTimeSlotData() {
  return {
    id: null,
    weekdayNo: 1,
    periodNo: 1,
    periodName: '第1节',
    timeGroup: 'MORNING',
    startTimeText: '08:00',
    endTimeText: '08:45',
    isTeaching: 1,
    isFixedBreak: 0,
    sortNo: 1,
    remark: ''
  };
}

/**
 * 校验排课规则自身是否满足基本结构约束。
 */
export function validateScheduleRuleForm(scheduleForm) {
  const messages = [];
  const segmentSum = Number(scheduleForm.morningPeriods || 0)
    + Number(scheduleForm.afternoonPeriods || 0)
    + Number(scheduleForm.nightPeriods || 0);
  if (segmentSum !== Number(scheduleForm.dayPeriods || 0)) {
    messages.push('上午、下午、晚间节次数之和必须等于每天总节数');
  }
  if (Number(scheduleForm.allowWeekend || 0) === 0 && Number(scheduleForm.weekDays || 0) > 5) {
    messages.push('未开启周末排课时，每周上课天数不能超过 5 天');
  }
  if (Number(scheduleForm.defaultContinuousLimit || 0) > Number(scheduleForm.dayPeriods || 0)) {
    messages.push('默认连堂上限不能大于每天总节数');
  }
  return {
    isValid: messages.length === 0,
    messages: messages.length ? messages : ['结构校验通过，规则可直接保存']
  };
}

/**
 * 校验时间片模板是否与当前规则完全对齐。
 * 这里不仅检查范围和重复，还要求每个“星期-节次”都存在一条记录，
 * 否则刷新后后端仍会读取到旧模板，表现为配置看似未生效。
 */
export function validateTimeSlotTemplate(timeSlots = [], scheduleForm = {}) {
  const messages = [];
  const duplicateKeys = new Set();
  const existingKeys = new Set();
  const expectedKeys = new Set();
  const expectedCount = Number(scheduleForm.weekDays || 0) * Number(scheduleForm.dayPeriods || 0);

  for (let day = 1; day <= Number(scheduleForm.weekDays || 0); day += 1) {
    for (let period = 1; period <= Number(scheduleForm.dayPeriods || 0); period += 1) {
      expectedKeys.add(`${day}-${period}`);
    }
  }

  for (const item of timeSlots) {
    const slotKey = `${item.weekdayNo}-${item.periodNo}`;
    if (duplicateKeys.has(slotKey)) {
      messages.push('同一星期和节次不能重复保存时间片');
      break;
    }
    duplicateKeys.add(slotKey);
    existingKeys.add(slotKey);
    if (Number(item.weekdayNo || 0) > Number(scheduleForm.weekDays || 0)) {
      messages.push('存在超出每周上课天数范围的时间片');
      break;
    }
    if (Number(scheduleForm.allowWeekend || 0) === 0 && Number(item.weekdayNo || 0) > 5) {
      messages.push('未开启周末排课时，不能保存周末时间片');
      break;
    }
    if (Number(item.periodNo || 0) > Number(scheduleForm.dayPeriods || 0)) {
      messages.push('存在超出每天总节数范围的时间片');
      break;
    }
    if (Number(item.isTeaching || 0) === 1 && Number(item.isFixedBreak || 0) === 1) {
      messages.push('固定休息时间片不能同时标记为可上课');
      break;
    }
  }

  if (!messages.length && expectedCount > 0) {
    if (timeSlots.length !== expectedCount || existingKeys.size !== expectedKeys.size) {
      messages.push('时间片模板未覆盖当前规则的全部星期和节次，请先重新生成模板再保存');
    } else {
      const missingKey = [...expectedKeys].find((item) => !existingKeys.has(item));
      if (missingKey) {
        messages.push('时间片模板未覆盖当前规则的全部星期和节次，请先重新生成模板再保存');
      }
    }
  }

  return {
    isValid: messages.length === 0,
    messages: messages.length ? messages : ['时间片结构校验通过，可直接保存']
  };
}

/**
 * 只有规则校验、时间片校验同时通过，并且页面上存在可保存模板时，
 * 才允许触发手动保存。
 */
export function canManuallySaveConfig(scheduleValidation, timeSlotValidation, timeSlots = []) {
  return Boolean(scheduleValidation?.isValid) && Boolean(timeSlotValidation?.isValid) && timeSlots.length > 0;
}

/**
 * 按当前规则生成完整时间片模板。
 */
export function buildTimeSlotTemplate(scheduleForm) {
  const totalPeriods = Number(scheduleForm.dayPeriods || 0);
  const weekDays = Number(scheduleForm.weekDays || 0);
  const rows = [];
  const morningStart = 8 * 60;
  const afternoonStart = 14 * 60;
  const nightStart = 19 * 60;

  for (let day = 1; day <= weekDays; day += 1) {
    for (let period = 1; period <= totalPeriods; period += 1) {
      const timeGroup = resolveTimeGroup(period, scheduleForm);
      const startMinutes = calculateStartMinutes(period, timeGroup, scheduleForm, morningStart, afternoonStart, nightStart);
      rows.push({
        ...createEmptyTimeSlotData(),
        weekdayNo: day,
        periodNo: period,
        periodName: `第${period}节`,
        timeGroup,
        startTimeText: formatMinutes(startMinutes),
        endTimeText: formatMinutes(startMinutes + 45),
        sortNo: rows.length + 1
      });
    }
  }

  return rows;
}

export function normalizeTimeSlotList(list = []) {
  return list
    .map((item) => ({
      ...createEmptyTimeSlotData(),
      ...item
    }))
    .sort((a, b) => (a.weekdayNo - b.weekdayNo) || (a.periodNo - b.periodNo));
}

function resolveTimeGroup(periodNo, scheduleForm) {
  if (periodNo <= Number(scheduleForm.morningPeriods || 0)) {
    return 'MORNING';
  }
  if (periodNo <= Number(scheduleForm.morningPeriods || 0) + Number(scheduleForm.afternoonPeriods || 0)) {
    return 'AFTERNOON';
  }
  return 'NIGHT';
}

function calculateStartMinutes(periodNo, timeGroup, scheduleForm, morningStart, afternoonStart, nightStart) {
  if (timeGroup === 'MORNING') {
    return morningStart + (periodNo - 1) * 55;
  }
  if (timeGroup === 'AFTERNOON') {
    return afternoonStart + (periodNo - Number(scheduleForm.morningPeriods || 0) - 1) * 55;
  }
  return nightStart
    + (periodNo - Number(scheduleForm.morningPeriods || 0) - Number(scheduleForm.afternoonPeriods || 0) - 1) * 55;
}

function formatMinutes(totalMinutes) {
  const hours = String(Math.floor(totalMinutes / 60)).padStart(2, '0');
  const minutes = String(totalMinutes % 60).padStart(2, '0');
  return `${hours}:${minutes}`;
}
