<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-head">
        <span class="login-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="17" rx="3" />
            <path d="M3 9h18M8 2v4M16 2v4" />
          </svg>
        </span>
        <div class="login-head__text">
          <h1 class="login-title">课程排课系统</h1>
          <p class="login-subtitle">选课、排课与调课的一体化工作台</p>
        </div>
      </div>

      <el-form :model="form" label-position="top" class="login-form">
        <el-form-item label="登录身份">
          <el-segmented v-model="loginType" style="width: 100%" :options="loginTypes" block />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            show-password
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" class="login-button" :loading="loading" @click="handleLogin">
            进入系统
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { login } from '@/api/modules/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const loading = ref(false);
const loginType = ref('admin');
const loginTypes = [
  { label: '管理员', value: 'admin' },
  { label: '教师', value: 'teacher' },
  { label: '学生', value: 'student' }
];
const form = reactive({
  username: '',
  password: ''
});
const userTypeMap = {
  admin: 'ADMIN',
  teacher: 'TEACHER',
  student: 'STUDENT'
};

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入完整账号和密码');
    return;
  }
  loading.value = true;
  try {
    const response = await login({
      username: form.username,
      password: form.password,
      userType: userTypeMap[loginType.value]
    });
    const result = response.data || {};
    authStore.setLoginState(result.token || '', result.user || null);
    await authStore.loadAuthContext();
    ElMessage.success('登录成功');
    const redirectPath = (route.query.redirect || authStore.firstAccessiblePath || '/dashboard').toString();
    router.push(redirectPath);
  } catch (error) {
    // 登录失败时，不需要在控制台打印错误，因为请求层已经统一处理了
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px 16px;
  background:
    radial-gradient(circle at 12% 0%, rgb(22 93 255 / 8%), transparent 34%),
    radial-gradient(circle at 88% 8%, rgb(14 165 164 / 8%), transparent 32%),
    var(--bg);
}

.login-panel {
  width: 420px;
  max-width: 100%;
  padding: 28px 30px 24px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface);
  box-shadow: 0 20px 50px rgb(17 34 68 / 10%);
}

.login-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.login-mark {
  display: grid;
  flex: 0 0 38px;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: var(--grad);
  box-shadow: 0 8px 18px rgb(22 93 255 / 24%);
  color: #fff;
}

.login-mark svg {
  width: 20px;
  height: 20px;
}

.login-head__text {
  min-width: 0;
}

.login-title {
  margin: 0;
  color: var(--ink);
  font-family: var(--app-font-family);
  font-size: 21px;
  font-weight: 600;
  letter-spacing: 0;
  line-height: 1.25;
}

.login-subtitle {
  margin: 3px 0 0;
  color: var(--muted-2);
  font-size: 12.5px;
}

.login-form {
  margin-top: 4px;
}

.login-form :deep(.el-form-item__label) {
  color: var(--muted-2);
  font-size: 12.5px;
  font-weight: 600;
}

.login-form :deep(.el-input__wrapper) {
  padding: 2px 12px;
  border-radius: var(--radius-control) !important;
  background: var(--surface-2) !important;
  box-shadow: 0 0 0 1px var(--line-2) inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--brand) inset !important;
}

.login-form :deep(.el-segmented) {
  --el-segmented-item-selected-bg-color: var(--brand-soft);
  --el-segmented-item-selected-color: var(--brand);
  --el-segmented-bg-color: var(--surface-2);
  padding: 3px;
  border-radius: var(--radius-control);
}

.login-button {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: var(--radius-control);
  background: var(--grad);
  box-shadow: 0 10px 22px rgb(22 93 255 / 22%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0;
}

.login-button:hover {
  background: var(--grad);
  box-shadow: 0 12px 26px rgb(22 93 255 / 28%);
}

@media (max-width: 768px) {
  .login-panel {
    padding: 22px 18px 18px;
  }
}
</style>
