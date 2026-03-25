<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="panel-glow"></div>
      <div class="panel-content">
        <h1 class="page-title">课程排课系统</h1>
        <p class="page-description">Academic Control Ledger</p>
        
        <el-form :model="form" label-position="top" class="login-form">
          <el-form-item label="登录身份">
            <el-segmented  v-model="loginType" style="width: 100%;" :options="loginTypes" block />
          </el-form-item>
          <el-form-item label="账号">
            <el-input v-model="form.username" size="large" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-button size="large" type="primary" class="login-button" :loading="loading" @click="handleLogin">进入系统</el-button>
          </el-form-item>
        </el-form>
      </div>
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
  padding: 40px 0;
  background:
    radial-gradient(circle at top right, rgb(184 135 70 / 0.08), transparent 40%),
    linear-gradient(180deg, rgb(250 246 238 / 0.78) 0%, rgb(241 234 221 / 0.56) 100%);
}

.login-panel {
  position: relative;
  width: 540px;
  background: linear-gradient(180deg, rgb(255 251 245 / 0.9), rgb(248 241 230 / 0.7));
  border-radius: 12px;
  border: 1px solid rgb(116 91 61 / 0.12);
  box-shadow: 0 16px 40px rgb(116 91 61 / 0.15);
  backdrop-filter: blur(14px);
}

.panel-glow {
  position: absolute;
  inset: 24px 24px auto;
  height: 120px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgb(184 135 70 / 0.2), rgb(184 135 70 / 0));
  filter: blur(12px);
  pointer-events: none;
}

.panel-content {
  position: relative;
  padding: 40px 50px 50px;
}

.page-title {
  font-family: 'Iowan Old Style', 'Baskerville', 'Songti SC', serif;
  font-size: 2.5rem;
  font-weight: 700;
  text-align: center;
  letter-spacing: 0.08em;
  color: #3d3126;
  margin: 0;
}

.page-description {
  text-align: center;
  color: rgb(184 135 70);
  font-size: 13px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-top: 8px;
}

.login-form {
  margin-top: 40px;
}

.login-form :deep(.el-form-item__label) {
  color: #72665b;
  font-weight: 600;
}

.login-form :deep(.el-input__wrapper) {
  padding: 4px 15px;
  background-color: transparent;
  border: none;
  border-bottom: 1px solid rgb(116 91 61 / 0.2);
  border-radius: 0;
  box-shadow: none;
  transition: border-color 0.2s;
}
.login-form :deep(.el-input__wrapper:focus-within) {
  border-bottom-color: rgb(184 135 70);
}

.login-form :deep(.el-segmented) {
  --el-segmented-item-selected-bg-color: rgb(244 230 205 / 0.6);
  --el-segmented-item-selected-color: #805a25;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 8px;
  background: #102636;
  border-color: #102636;
  font-weight: 600;
}

.login-button:hover {
  background: #17384d;
  border-color: #17384d;
}
</style>
