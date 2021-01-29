<template>
  <div class="s-center">
    <el-form label-width="80px" :model="form">
      <el-form-item label="学号">
        <el-input v-model="form.studentNo" disabled></el-input>
      </el-form-item>
      <el-form-item label="真实姓名">
        <el-input v-model="form.realname" disabled></el-input>
      </el-form-item>
      <el-form-item label="年级">
        <el-input v-model="form.grade" disabled></el-input>
      </el-form-item>
      <el-form-item label="我的班级">
        <el-input v-model="form.classNo">
          <el-button slot="append" type="primary" @click="handleJoinClass()">加入班级</el-button>
        </el-input>
      </el-form-item>
      <el-form-item label="手机">
        <el-input v-model="form.telephone"></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="saveInfo()">保存</el-button>
      </el-form-item>
    </el-form>

    <el-dialog title="加入班级" :visible.sync="visibleForm" width="500px">
      <el-form :model="editFormData" label-position="right" label-width="80px">
        <el-form-item label="班级" prop="classNo">
          <el-select v-model="editFormData.classNo" placeholder="请选择">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item style="text-align:left;">
          <el-button type="primary" @click="save()" size="small">提 交</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "center",
  data() {
    return {
      form: {},
      visibleForm: false,
      editFormData: {},
      options: []
    };
  },
  mounted() {
    let student = window.localStorage.getItem("student");
    if (student != null) {
      this.form = JSON.parse(student);
    } else {
      this.$message({
        message: "获取本地用户信息失败，请重新登录",
        type: "error"
      });
    }
  },
  methods: {
    save() {
      this.$axios
        .post(
          "http://localhost:8080/student/join/" +
            this.form.id +
            "/" +
            this.editFormData.classNo
        )
        .then(r => {
          if (r.data.code == 0) {
            this.$message({
              message: r.data.message,
              type: "success"
            });
            this.form.classNo = this.editFormData.classNo
            window.localStorage.setItem('student', JSON.stringify(this.form))
          }
        });
        this.visibleForm = false;
    },
    saveInfo() {
      this.$axios
        .post("http://localhost:8080/student/modify/", this.form)
        .then(r => {
          if (r.data.code == 0) {
            this.$message({
              message: r.data.message,
              type: "success"
            });
            window.localStorage.setItem('student', JSON.stringify(this.form))
          }
        });
    },
    handleJoinClass() {
      let grade = this.form.studentNo.substring(4, 6);
      this.options = [];
      this.visibleForm = true;
      this.$axios.get("http://localhost:8080/class-grade/" + grade).then(r => {
        let data = r.data.data;
        data.map(v => {
          this.options.push({
            label: v.classNo,
            value: v.classNo
          });
        });
      });
    }
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