export function getErrorPayload(error) {
  return error?.response?.data || error || null;
}

export function getErrorCode(error) {
  return getErrorPayload(error)?.code;
}

export function getErrorMessage(error, fallback = '请求失败') {
  const payload = getErrorPayload(error);
  return payload?.message || error?.message || fallback;
}

/**
 * 判断业务响应体是否为登录失效。
 * 后端未登录时 HTTP 状态仍是 200，只在响应体里返回 code=401。
 * @param {{ code?: number } | null | undefined} payload 响应体
 * @returns {boolean}
 */
export function isUnauthorizedPayload(payload) {
  return payload?.code === 401;
}

export function isUnauthorizedError(error) {
  return error?.response?.status === 401 || isUnauthorizedPayload(getErrorPayload(error));
}
