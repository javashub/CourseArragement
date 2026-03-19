import axios from 'axios';
import { ElMessage } from 'element-plus';
import { AUTH_CONTEXT_KEY, TOKEN_KEY, USER_KEY } from '@/constants/storage';
import { getErrorMessage, getErrorPayload } from '@/utils/http';
import { removeStorage } from '@/utils/storage';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
});

const MAX_PAGE_SIZE = 100;

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

function redirectToLogin() {
  if (redirectingToLogin) {
    return;
  }
  redirectingToLogin = true;
  removeStorage(TOKEN_KEY);
  removeStorage(USER_KEY);
  removeStorage(AUTH_CONTEXT_KEY);
  const currentHash = window.location.hash?.replace(/^#/, '') || '/';
  const onLoginPage = currentHash.startsWith('/login');
  if (!onLoginPage) {
    const redirect = encodeURIComponent(currentHash);
    window.location.replace(`${window.location.pathname}${window.location.search}#/login?redirect=${redirect}`);
    return;
  }
  window.location.replace(`${window.location.pathname}${window.location.search}#/login`);
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
      if (!response.config?.meta?.silentError) {
        ElMessage.error(data.message || '请求失败');
      }
      return Promise.reject(data);
    }
    return data;
  },
  (error) => {
    const payload = getErrorPayload(error);
    if (payload?.code === 401) {
      if (!error.config?.meta?.silentError) {
        ElMessage.error(getErrorMessage(payload, '登录已失效，请重新登录'));
      }
      redirectToLogin();
      return Promise.reject(payload);
    }
    if (!error.config?.meta?.silentError) {
      ElMessage.error(getErrorMessage(error, '网络异常'));
    }
    return Promise.reject(payload || error);
  }
);

export default request;
