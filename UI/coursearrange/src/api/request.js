import axios from 'axios';
import { ElMessage } from 'element-plus';
import { AUTH_CONTEXT_KEY, TOKEN_KEY, USER_KEY } from '@/constants/storage';
import {
  getErrorMessage,
  getErrorPayload,
  isUnauthorizedError,
  isUnauthorizedPayload
} from '@/utils/http';
import { removeStorage } from '@/utils/storage';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
});

const MAX_PAGE_SIZE = 100;
const AUTH_EXPIRED_MESSAGE = '登录已过期，请重新登录';
// 给 message 的入场动画留出可见时间，再整页回到登录页。
const AUTH_EXPIRED_REDIRECT_DELAY = 1000;

let redirectingToLogin = false;

function clampPageParam(value) {
  if (value === null || value === undefined || value === '') {
    return value;
  }
  const numericValue = Number(value);
  if (!Number.isFinite(numericValue)) {
    return value;
  }
  const normalizedValue = Math.trunc(numericValue);
  if (normalizedValue < 1) {
    return 1;
  }
  return Math.min(normalizedValue, MAX_PAGE_SIZE);
}

function normalizePagingParams(params) {
  if (!params || typeof params !== 'object') {
    return params;
  }
  const normalizedParams = { ...params };
  if (Object.prototype.hasOwnProperty.call(normalizedParams, 'pageSize')) {
    normalizedParams.pageSize = clampPageParam(normalizedParams.pageSize);
  }
  if (Object.prototype.hasOwnProperty.call(normalizedParams, 'limit')) {
    normalizedParams.limit = clampPageParam(normalizedParams.limit);
  }
  return normalizedParams;
}

function clearAuthStorage() {
  // 只清理认证相关键，保留主题等非认证偏好。
  removeStorage(TOKEN_KEY);
  removeStorage(USER_KEY);
  removeStorage(AUTH_CONTEXT_KEY);
}

function buildLoginUrl() {
  const currentHash = window.location.hash?.replace(/^#/, '') || '/';
  const baseUrl = `${window.location.pathname}${window.location.search}`;
  if (currentHash.startsWith('/login')) {
    return `${baseUrl}#/login`;
  }
  return `${baseUrl}#/login?redirect=${encodeURIComponent(currentHash)}`;
}

function performRedirectToLogin() {
  window.location.replace(buildLoginUrl());
  // hash 路由下 replace 只改 hash，不会重建应用；显式 reload 清空 Pinia 中的旧登录态。
  window.location.reload();
}

/**
 * 统一处理登录失效：立即清理凭证并提示，等 message 动画可见后再整页跳转。
 * 并发 401 只会进入一次，避免重复提示与重复跳转。
 */
function handleUnauthorized() {
  if (redirectingToLogin) {
    return;
  }
  redirectingToLogin = true;
  // 先清 token，避免等待提示动画期间继续携带旧凭证请求接口。
  clearAuthStorage();
  ElMessage.error(AUTH_EXPIRED_MESSAGE);
  window.setTimeout(performRedirectToLogin, AUTH_EXPIRED_REDIRECT_DELAY);
}

request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers['satoken'] = token.replaceAll('"', '');
  }
  config.params = normalizePagingParams(config.params);
  return config;
});

request.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (typeof data?.code === 'number' && data.code !== 0) {
      if (isUnauthorizedPayload(data)) {
        handleUnauthorized();
        return Promise.reject(data);
      }
      if (!response.config?.meta?.silentError) {
        ElMessage.error(data.message || '请求失败');
      }
      return Promise.reject(data);
    }
    return data;
  },
  (error) => {
    const payload = getErrorPayload(error);
    if (isUnauthorizedError(error)) {
      handleUnauthorized();
      return Promise.reject(payload);
    }
    if (!error.config?.meta?.silentError) {
      ElMessage.error(getErrorMessage(error, '网络异常'));
    }
    return Promise.reject(payload || error);
  }
);

export default request;
