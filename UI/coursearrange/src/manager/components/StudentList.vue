<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索学生" v-model="keyword" @clear="inputListener" clearable>
        <el-button slot="append" type="primary" icon="el-icon-search" @click="searchStudent">搜索</el-button>
      </el-input>
    </div>
    <!-- 数据显示 -->
    <el-table :data="studentData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
      <el-table-column prop="studentNo" label="学号" fixed width="100"></el-table-column>
      <el-table-column prop="realname" label="姓名" fixed width="100"></el-table-column>
      <el-table-column prop="username" label="昵称" fixed width="100"></el-table-column>
      <el-table-column prop="grade" label="年级" fixed width="100"></el-table-column>
      <el-table-column prop="classNo" label="班级" fixed width="100"></el-table-column>
      <el-table-column prop="age" label="年龄" fixed width="80"></el-table-column>
      <el-table-column prop="telephone" label="电话" fixed width="100"></el-table-column>
      <el-table-column prop="address" label="地址"></el-table-column>

      <el-table-column prop="operation" label="操作">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹出表单编辑讲师 -->
    <el-dialog title="编辑学生" :visible.sync="visibleForm">
      <el-form :model="editFormData" label-position="left" label-width="80px">
        <el-form-item label="学号">
          <el-input v-model="editFormData.teacherNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editFormData.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editFormData.realname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="editFormData.jobtitle" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="editFormData.teach" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="editFormData.telephone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editFormData.address" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input v-model="editFormData.age" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleForm = false">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 上一页，当前页，下一页 -->
    <div class="footer-button">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page.sync="page"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentList',
  data() {
    return {
      studentData: [],
      editFormData: [],
      keyword: '',
      page: 1,
      pageSize: 10,
      total: 0,
      visibleForm: false,
    }
  },

  mounted() {
    this.allStudent()
  },

  methods: {

    /***
     * 编辑提交
     */
    commit() {

    },

    inputListener() {
      this.allStudent();
    },

    /**
     * 查询所有学生
     */
    allStudent() {
      this.$axios.get('http://localhost:8080/student/querystudent/' + this.page)
      .then(res => {
        // this.$message({message:'查询成功', type: 'success'})
        console.log(res)
        let ret = res.data.data;
        this.studentData = ret.records;
        this.total = ret.total;
      })
      .catch(error => {
        this.$message.error('查询学生列表失败')
      })
    },

    searchStudent() {

    },

    deleteById() {

    },

    editById() {

    },

    handleSizeChange() {

    },

    handleCurrentChange(v) {
      this.page = v;
      this.allTeacher();
    }
  }
}
</script>

<style lang="less" scoped>

  .el-input-group {
    width: 40%;
  }
  .header-menu {
    margin-bottom: 5px;
    padding: 0;
    text-align: left;
    margin-bottom: 5px;
  }

  .footer-button {
    margin-top: 10px;
  }
</style>