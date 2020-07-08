<template>
  <div class="s-center">
    <el-form label-width="80px" :model="form">
      <el-form-item label="原始密码">
        <el-input v-model="form.oldPass" type="password" show-password></el-input>
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPass" type="password" show-password></el-input>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.rePass" type="password" show-password></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="save()">修改</el-button>
      </el-form-item>
    </el-form>

  </div>
</template>

<script>
export default {
  name: "password",
  data() {
    return {
      form: {},
      student:{}
    };
  },
  mounted() {
    let student = window.localStorage.getItem("student");
    if (student != null) {
      this.student = JSON.parse(student);
    } else {
      this.$message({
        message: "获取本地用户信息失败，请重新登录",
        type: "error"
      });
    }
  },
  methods: {
    save() {
      let data = this.form;
      data.id = this.student.id;
      this.$axios
        .post(
          "http://localhost:8080/student/password",
          data
        )
        .then(r => {
          if (r.data.code == 0) {
            this.$message({
              message: '密码更新成功，请重新登录！',
              type: "success"
            });
            setTimeout(() => {
              window.localStorage.removeItem('student')
              this.$router.push('/student/login')
            }, 1000);
          }else{
            this.$message({
              message: r.data.message,
              type: "error"
            });
          }
          this.form = {}
        });
    },
  }
};
</script>

<style lang="less">
.s-center {
  width: 500px;
  text-align: left;
  padding: 30px;
}
</style>