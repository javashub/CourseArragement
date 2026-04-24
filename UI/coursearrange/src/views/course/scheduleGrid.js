export function buildWeekLabels(scheduleConfig = {}, planList = []) {
  const configuredDays = Number(scheduleConfig?.scheduleRule?.weekDays || 0);
  const maxDayFromPlans = planList.reduce((max, item) => Math.max(max, resolvePlanPosition(item)?.dayIndex || 0), 0);
  const dayCount = Math.max(configuredDays, maxDayFromPlans, 5);
  return Array.from({ length: dayCount }, (_, index) => {
    const dayIndex = index + 1;
    return {
      label: resolveWeekLabel(dayIndex),
      key: `day-${dayIndex}`,
      index: dayIndex
    };
  });
}

export function buildPeriodLabels(scheduleConfig = {}, planList = []) {
  const configuredPeriods = Number(scheduleConfig?.scheduleRule?.dayPeriods || 0);
  const maxPeriodFromPlans = planList.reduce((max, item) => Math.max(max, resolvePlanPosition(item)?.periodIndex || 0), 0);
  const periodCount = Math.max(configuredPeriods, maxPeriodFromPlans, 5);
  return Array.from({ length: periodCount }, (_, index) => {
    const periodIndex = index + 1;
    return {
      label: `第${periodIndex}节`,
      key: String(periodIndex).padStart(2, '0'),
      index: periodIndex
    };
  });
}

export function resolvePlanPosition(plan) {
  const weekdayNo = Number(plan?.weekdayNo || 0);
  const periodNo = Number(plan?.periodNo || 0);
  if (weekdayNo > 0 && periodNo > 0) {
    return {
      dayIndex: weekdayNo,
      periodIndex: periodNo
    };
  }
  return parseClassTime(plan?.classTime);
}

export function parseClassTime(classTime) {
  if (classTime == null) {
    return null;
  }
  const normalized = String(classTime).trim();
  if (!normalized) {
    return null;
  }
  if (/^\d{4}$/.test(normalized)) {
    const dayIndex = Number(normalized.slice(0, 2));
    const periodIndex = Number(normalized.slice(2, 4));
    if (dayIndex > 0 && periodIndex > 0) {
      return {
        dayIndex,
        periodIndex
      };
    }
  }
  if (/^\d{1,2}$/.test(normalized)) {
    const value = Number(normalized);
    if (!value || value < 1) {
      return null;
    }
    return {
      dayIndex: Math.ceil(value / 5),
      periodIndex: ((value - 1) % 5) + 1
    };
  }
  return null;
}

export function toClassTime(dayIndex, periodIndex) {
  if (Number(dayIndex) <= 0 || Number(periodIndex) <= 0) {
    return '';
  }
  return `${String(dayIndex).padStart(2, '0')}${String(periodIndex).padStart(2, '0')}`;
}

function resolveWeekLabel(dayIndex) {
  const builtinLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return builtinLabels[dayIndex - 1] || `周${dayIndex}`;
}
