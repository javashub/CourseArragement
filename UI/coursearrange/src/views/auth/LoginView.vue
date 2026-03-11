<template>
  <div class="login-page">
    <div class="login-panel page-card">
      <h1 class="page-title">课程排课系统</h1>
      <p class="page-description">Vue 3 + Vite + Pinia + Element Plus 规范化骨架</p>
      <el-form :model="form" label-position="top" class="login-form">
        <el-form-item label="登录身份">
          <el-segmented v-model="loginType" :options="loginTypes" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" @click="handleLogin">进入系统</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          当前前端已完成工程规范化骨架，旧页面功能会分模块逐步迁移。
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { loginByAdmin, loginByStudent, loginByTeacher } from '@/api/modules/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
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

const requestMap = {
  admin: loginByAdmin,
  teacher: loginByTeacher,
  student: loginByStudent
};

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入完整账号和密码');
    return;
  }

  try {
    const response = await requestMap[loginType.value]({
      username: form.username,
      password: form.password
    });
    const result = response.data || {};
    authStore.setLoginState(result.token || '', result.admin || result.teacher || result.student || null);
    ElMessage.success('登录成功');
    router.push((route.query.redirect || '/dashboard').toString());
  } catch (error) {
    console.error(error);
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #f7f8fa 100%);
}

.login-panel {
  width: 420px;
}

.login-form {
  margin: 24px 0;
}

.login-button {
  width: 100%;
}
</style>
