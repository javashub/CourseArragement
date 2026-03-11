import axios from 'axios';
import { ElMessage } from 'element-plus';
import { TOKEN_KEY } from '@/constants/storage';

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
      ElMessage.error(data.message || '请求失败');
      return Promise.reject(data);
    }
    return data;
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络异常');
    return Promise.reject(error);
  }
);

export default request;
