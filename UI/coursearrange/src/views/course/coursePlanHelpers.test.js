import test from 'node:test';
import assert from 'node:assert/strict';

import { formatDuration } from './coursePlanHelpers.js';

test('formatDuration formats milliseconds into readable text', () => {
  assert.equal(formatDuration(undefined), '--');
  assert.equal(formatDuration(120), '120ms');
  assert.equal(formatDuration(1450), '1.45s');
  assert.equal(formatDuration(61000), '61.00s');
});
