<template>
  <!-- 讲师修改密码页面 -->
  <div class="update-form">
    <el-form ref="formRef" :model="passForm" :rules="formRules">
      <el-form-item prop="oldPass">
        <el-input v-model.trim="passForm.oldPass" placeholder="请输入旧密码" show-password type="password"></el-input>
      </el-form-item>
      <el-form-item prop="newPass">
        <el-input v-model.trim="passForm.newPass" placeholder="请输入新密码" show-password type="password"></el-input>
      </el-form-item>
      <el-form-item prop="rePass">
        <el-input v-model.trim="passForm.rePass" placeholder="请确认新密码" show-password type="password"></el-input>
      </el-form-item>
      <el-button class="button" @click="commit()" type="primary">提交</el-button>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "UpdatePass",
  data() {
    var validateOldPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入旧密码'));
      } 
      setTimeout(() => {
        if (this.passForm.oldPass !== '') {
          this.$refs.formRef.validateField('oldPass');
        }
        callback();
      })
        
      
    };
    var validateNewPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入新密码'));
      } 
      setTimeout(() => {
        if (this.passForm.newPass !== '') {
          this.$refs.formRef.validateField('newPass');
        }
        callback();
      })
        
      
    };
    var validateRePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'));
      } else if (value !== this.passForm.newPass) {
        callback(new Error('两次输入密码不一致!'));
        // this.passForm.newPass = ''
        this.passForm.rePass = ''
      } else {
        callback();
      }
    };
    return {
      passForm: {
        id: '',
        oldPass: '',
        newPass: '',
        rePass: ''
      },
      userType: '',
      formRules: {
        oldPass: [
          { required: true, message: '请输入旧密码', trigger: 'blur' },
          { validator: validateOldPass, trigger: 'blur' },
          { min: 6, max: 15, message: '长度在 6 到 15 个字符', trigger: 'blur' }
        ],
        newPass: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { validator: validateNewPass, trigger: 'blur' },
          { min: 6, max: 15, message: '长度在 6 到 15 个字符', trigger: 'blur' }
        ],
        rePass: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateRePass, trigger: 'blur' },
          { min: 6, max: 15, message: '长度在 6 到 15 个字符', trigger: 'blur' }
        ]
      },
    };
  },
  mounted() {},

  methods: {

    // 获取用户类别，1管理员，2讲师，3学生
    getUserType() {
      // 讲师
      let teacher = window.localStorage.getItem('teacher')
      // 管理员
      let admin = window.localStorage.getItem('admin')
      // 学生
      let student = window.localStorage.getItem('student')

      if (admin != null) {
        // 管理员1
        this.userType = (JSON.parse(admin)).userType
        this.passForm.id = (JSON.parse(admin)).id
      } else if (teacher != null){
        // 讲师2
        this.userType = (JSON.parse(teacher)).userType
        this.passForm.id = (JSON.parse(teacher)).id
      } else {
        this.userType = (JSON.parse(student)).userType
        this.passForm.id = (JSON.parse(student)).id
      }
    },


    // 提交更新密码请求
    commit() {
      // 获得用户类别
      this.getUserType()

      setTimeout(() => {
        this.$refs.formRef.validate((valid) => {
          if (!valid) return;
        })
      }, 1000)

      
      // 判断
      if (this.userType == 1) {
        // 管理员修改密码
        let url = "http://localhost:8080/admin/password"
        this.postForm(url)
      } else if (this.userType == 2) {
        // 讲师修改密码
        let url = "http://localhost:8080/teacher/password"
        this.postForm(url)
      } else if (this.userType == 3) {
        let url = "http://localhost:8080/student/password"
        this.postForm(url)
      }
  },

  // 发起请求
  postForm(url) {
    console.log(url)
    this.$axios.post(url, {
      id: this.passForm.id,
      oldPass: this.passForm.oldPass,
      newPass: this.passForm.newPass,
      rePass: this.passForm.rePass
    })
    .then(res => {
      if (res.data.code == 0) {
        // 密码更新成功，删除token，localStorage重新登录
        if (this.userType == 1) {
          this.$message({message: "修改密码成功 ，请重新登录", type: "success"})
          setTimeout(() => {
            localStorage.removeItem('token')
            localStorage.removeItem('admin')
            this.$router.push('/admin/login')
          }, 1000)
        } else if (this.userType == 2) {
          this.$message({message: "修改密码成功 ，请重新登录", type: "success"})
          setTimeout(() => {
            localStorage.removeItem('token')
            localStorage.removeItem('teacher')
            this.$router.push('/admin/login')
          }, 1000)
        } else if (this.userType == 3) {
          this.$message({message: "修改密码成功 ，请重新登录", type: "success"})
          setTimeout(() => {
            localStorage.removeItem('token')
            localStorage.removeItem('student')
            this.$router.push('/student/login')
          }, 1000)
        }
      } else {
        // 旧密码不正确
        this.$message.error(res.data.message)
        this.passForm.oldPass = ''
        this.passForm.newPass = ''
        this.passForm.rePass = ''
      }
    })
    .catch(error => {})
    }
  }
}
</script>

<style lang="less" scoped>

.update-form {
  width: 500px;
  border: 1px solid #eee;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #ddd;
  position: absolute;
  padding: 10px;
  border-radius: 3px;
  margin-top: 250px;
}

.button {
  display: flex;
  margin-left: 400px;
  justify-content: center;
}
</style>