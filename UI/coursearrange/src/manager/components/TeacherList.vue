<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索讲师" v-model="keyword">
        <el-button slot="append" icon="el-icon-search" @click="searchTeacher"></el-button>
      </el-input>
    </div>
    <!-- 数据显示 -->
    <el-table :data="teacherData" size="small">
        <el-table-column label="序号" type="index" width="50"></el-table-column>
        <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
        <el-table-column prop="teacherNo" label="编号"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="realname" label="姓名"></el-table-column>
        <el-table-column prop="jobtitle" label="职称"></el-table-column>
        <el-table-column prop="teach" label="学科"></el-table-column>
        <el-table-column prop="age" label="年龄"></el-table-column>
        <el-table-column prop="address" label="地址"></el-table-column>
        <el-table-column prop="operation" label="操作">
          <el-button type="danger" size="small">删除</el-button>
          <el-button type="danger" size="small">编辑</el-button>
        </el-table-column>
    </el-table>
    <!-- 上一页，当前页，下一页 -->
  </div>
</template>

<script>
export default {
  name: 'TeacherList',
  data() {
    return {
      teacherData: [],
      keyword: '',
      page: ''
    }
  },
  mounted(){
    this.allTeacher()
  },

  methods: {
    searchTeacher() {
      this.$axios.get()
      .then()
      .catch()
    },
    // 获取所有讲师
    allTeacher() {
      this.$axios
        .get("http://localhost:8080/teacher/queryteacher")
        .then(res => {
          console.log(res.data);
          this.teacherData = res.data.data;
        })
        .catch(error => {
          console.log("查询讲师失败");
        });
    }
  }
}
</script>

<style lang="less" scoped>

  .el-input-group {
    width: 40%;
  }

  .header-menu {
    margin-bottom: 5px,;
    padding: 0;
    text-align: left;
    margin-bottom: 5px;
  }


</style>