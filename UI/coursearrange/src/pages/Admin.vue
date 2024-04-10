<template>
  <div class="login-wrapper">
    
    <div class="login-box">
      <!-- 头像 -->
      <div class="login-avatar">
        <img src="@/assets/logo.png" alt />
      </div>

      <!-- 登录表单 -->
      <el-form class="login-form" ref="loginFormRef" :model="adminLoginForm" :rules="adminLoginFormRules">
        <h3>用户登录</h3>
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input v-model="adminLoginForm.username" placeholder="请输入账号" prefix-icon="iconfont iconicon"></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input v-model="adminLoginForm.password" placeholder="请输入密码" prefix-icon="iconfont iconmima" type="password" show-password></el-input>
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
        username: '10011',
        password: 'aizai2015'
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
    },
    
    login() {
      this.$refs.loginFormRef.validate(valid => {
        // 表单预验证
        if (!valid) return;
        if (this.radio == 1) {
          // 管理员登录
          this.$axios.post('http://localhost:8080/admin/login', {
            username: this.adminLoginForm.username,
            password: this.adminLoginForm.password
          })
          .then(res => {
            if (res.data.code == 0) {
              let ret = res.data.data
              // 保存信息，跳转到主页
              window.localStorage.setItem('token', ret.token)
              window.localStorage.setItem('admin', JSON.stringify(ret.admin))
              this.$router.push('/systemdata')
              this.$message({message: "登录成功", type: "success"})
            } else {
              alert(res.data.message)
              this.adminLoginForm.password = ''
            }
          }).catch((error) => {
            this.$message.error("登录失败")
          });
        } else if(this.radio == 2) {
          // 讲师登录
          this.$axios.post('http://localhost:8080/teacher/login', {
            username: this.adminLoginForm.username,
            password: this.adminLoginForm.password
          })
          .then(res => {
            if (res.data.code == 0) {
              let ret = res.data.data
              window.localStorage.setItem('token', ret.token)
              window.localStorage.setItem('teacher', JSON.stringify(ret.teacher))
              // 跳转
              this.$router.push('/systemdata')
              this.$message({message: "登录成功", type: "success"})
            } else {
              alert(res.data.message)
              this.adminLoginForm.password = ''
            }
          }).catch((error) => {
            this.$message.error("登录失败")
          });
        }
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