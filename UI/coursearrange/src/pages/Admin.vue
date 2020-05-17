<template>
  <div class="login-wrapper">
    
    <div class="login-box">
      <!-- 头像 -->
      <div class="login-avatar">
        <img src="@/assets/logo.png" alt />
      </div>

      <!-- 登录表单 -->
      <el-form class="login-form" ref="loginFormRef" :model="adminLoginForm" :rules="adminLoginFormRules">
        <h3>管理员登录</h3>
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input v-model="adminLoginForm.username" placeholder="请输入编号/用户名/姓名" prefix-icon="iconfont iconicon"></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input v-model="adminLoginForm.password" placeholder="请输入密码" prefix-icon="iconfont iconmima" type="password"></el-input>
        </el-form-item>
        <!-- 登录类型 -->
        <el-form-item >
          <template >
            <el-radio v-model="radio" label="1" @change="getType()">管理员</el-radio>
            <el-radio v-model="radio" label="2" @change="getType()">讲师</el-radio>
          </template>
        </el-form-item>
        
        <!-- 按钮 -->
        <el-form-item class="button">
          <el-button type="primary" @click="login">登录</el-button>
          <el-button type="info" @click="resetLoginForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Admin",
  data() {
    return {
      // 类型选择，默认选择管理员登录
      radio: '1',
      // 登录表单的对象
      adminLoginForm: {
        username: '',
        password: ''
      },
      adminLoginFormRules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 12, message: '长度在 5 到 12 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 15, message: '长度在 6 到 15 个字符', trigger: 'blur' }
        ]
      },
    }
  },
  methods: {
    // 重置方法
    resetLoginForm() {
      this.$refs.loginFormRef.resetFields();
    },
    getType() {
      // 调用这个方法直接获取到了类型
      console.log(this.radio)
    },
    
    login() {
      // 表单预验证
      // 此处可以直接获取选中的类型值
      console.log(this.radio);
      
      this.$refs.loginFormRef.validate(valid => {
        if (!valid) return;
        this.$axios.post('http://localhost:8080/admin/login', {
          username: this.adminLoginForm.username,
          password: this.adminLoginForm.password
        })
        .then((response) => {
          // 成功响应
          console.log(response);
        }).catch((error) => {
          // 失败
          alert('失败！');
          console.log(error)
        });
      })
    }
  }
};
</script>

<style lang="less" scoped>

  .login-form {
      position: absolute;
      bottom: 0;
      width: 100%;
      padding: 0 20px;
      box-sizing: border-box;
    }

  .login-type {
    display: flex;
    justify-content: left;
  }

  .button {
    display: flex;
    margin-top: 15px;
    margin-right: 30px;
    justify-content: center;
  }

  .login-wrapper {
    background: #2b4b6b;
    height: 100%;
  }

  .login-avatar {
    height: 130px;
    width: 130px;
    border: 1px solid #eee;
    border-radius: 50%;
    padding: 10px;
    box-shadow: 0 0 10px #ddd;
    position: absolute;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: #fff;
    img {
      height: 100%;
      width: 100%;
      border-radius: 50%;
      background-color: #eee;
    }
  }

  .title {
    color: #fff;
  }

  .login-box {
    width: 550px;
    height: 380px;
    background-color: #fff;
    border-radius: 3px;
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
  }
  
  
</style>