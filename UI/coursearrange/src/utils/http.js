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

export function isUnauthorizedError(error) {
  return getErrorCode(error) === 401;
}
