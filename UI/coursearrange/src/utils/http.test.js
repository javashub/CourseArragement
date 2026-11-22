import test from 'node:test';
import assert from 'node:assert/strict';

import { isUnauthorizedError, isUnauthorizedPayload } from './http.js';

test('isUnauthorizedPayload 识别业务 401', () => {
  assert.equal(isUnauthorizedPayload({ code: 401 }), true);
});

test('isUnauthorizedPayload 忽略成功与其他业务码', () => {
  assert.equal(isUnauthorizedPayload({ code: 0 }), false);
  assert.equal(isUnauthorizedPayload({ code: 10000 }), false);
});

test('isUnauthorizedPayload 处理空值', () => {
  assert.equal(isUnauthorizedPayload(null), false);
  assert.equal(isUnauthorizedPayload(undefined), false);
});

test('isUnauthorizedError 识别 axios 响应中的 401', () => {
  assert.equal(isUnauthorizedError({ response: { data: { code: 401 } } }), true);
});

test('isUnauthorizedError 识别没有业务体的 HTTP 401', () => {
  assert.equal(isUnauthorizedError({ response: { status: 401, data: {} } }), true);
});

test('isUnauthorizedError 兼容直接传入业务体', () => {
  assert.equal(isUnauthorizedError({ code: 401 }), true);
});

test('isUnauthorizedError 不误判 403、500 或空错误', () => {
  assert.equal(isUnauthorizedError({ response: { status: 403, data: {} } }), false);
  assert.equal(isUnauthorizedError({ response: { data: { code: 500 } } }), false);
  assert.equal(isUnauthorizedError(null), false);
});
