<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索讲师" v-model="keyword">
      <el-button slot="append" type="primary" icon="el-icon-search" @click="searchTeacher">搜索</el-button>
      </el-input>
    </div>
    <!-- 数据显示 -->
    <el-table :data="teacherData" size="mini">
        <el-table-column label="序号" type="index"></el-table-column>
        <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
        <el-table-column prop="teacherNo" label="编号"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="realname" label="姓名"></el-table-column>
        <el-table-column prop="jobtitle" label="职称"></el-table-column>
        <el-table-column prop="teach" label="学科"></el-table-column>
        <el-table-column prop="age" label="年龄"></el-table-column>
        <el-table-column prop="address" label="地址"></el-table-column>
        <el-table-column prop="operation" label="操作">
          <el-button type="danger" size="mini"  @click="deleteById">删除</el-button>
          <el-button type="primary" size="mini" @click="editById">编辑</el-button>
          <!-- <el-button type="danger" size="mini" @click="operaById">封号</el-button> -->
        </el-table-column>
    </el-table>
    <!-- 上一页，当前页，下一页 -->
    <div class="footer-button">
      <el-button-group>
        <el-button type="primary" icon="el-icon-arrow-left" @click="prePage">上一页</el-button>
        
        <el-button type="primary" @click="nextPage">下一页<i class="el-icon-arrow-right el-icon--right" ></i></el-button>
      </el-button-group>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeacherList',
  data() {
    return {
      teacherData: [],
      keyword: '',
      page: 1
    }
  },
  mounted(){
    this.allTeacher()
  },

  methods: {

    prePage() {
      if (this.page >1) {
        this.page --;
        console.log(this.page)
      }
    },

    nextPage() {
      if (this.page < this.teacherData.length)
      this.page ++;
      console.log(this.page)
    },

    deleteById() {

    },
    editById() {

    },
    // 关键词搜索讲师
    searchTeacher() {
      this.$axios.get('http://localhost:8080/teacher/searchteacher/' + this.keyword)
      .then(res => {
        
        this.teacherData = res.data.data
        console.log(this.teacherData)
      })
      .catch(error => {
        console.log('找不到相关讲师')
      })
    },
    // 获取所有讲师
    allTeacher() {
      this.$axios
        .get("http://localhost:8080/teacher/queryteacher")
        .then(res => {
          // console.log(res.data);
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

  .footer-button {
    float: right;
    margin-right: 50px;
  }

  .footer-button {
    margin-top: 20px;
  }


</style>