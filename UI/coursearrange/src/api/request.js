import axios from 'axios';
import { ElMessage } from 'element-plus';
import { TOKEN_KEY } from '@/constants/storage';
import { getErrorMessage, getErrorPayload } from '@/utils/http';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers['satoken'] = token.replaceAll('"', '');
  }
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
    if (!error.config?.meta?.silentError) {
      ElMessage.error(getErrorMessage(error, '网络异常'));
    }
    return Promise.reject(payload || error);
  }
);

export default request;
