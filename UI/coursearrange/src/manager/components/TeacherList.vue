<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索讲师" v-model="keyword" @clear="inputListener" clearable>
        <el-button slot="append" type="primary" icon="el-icon-search" @click="searchTeacher">搜索</el-button>
      </el-input>
    </div>
    <!-- 数据显示 -->
    <el-table :data="teacherData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <el-table-column prop="teacherNo" label="编号" fixed width="100"></el-table-column>
      <el-table-column prop="username" label="用户名" fixed width="100"></el-table-column>
      <el-table-column prop="realname" label="姓名" fixed width="100"></el-table-column>
      <el-table-column prop="jobtitle" label="职称" fixed width="100"></el-table-column>
      <el-table-column prop="teach" label="学科" fixed width="100"></el-table-column>
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
    <el-dialog title="编辑讲师" :visible.sync="visibleForm">
      <el-form :model="editFormData" label-position="left" label-width="80px">
        <el-form-item label="编号">
          <el-input v-model="editFormData.teacherNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editFormData.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editFormData.realname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="editFormData.jobtitle" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="科目">
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
  name: "TeacherList",
  data() {
    return {
      teacherData: [],
      keyword: "",
      page: 1,
      pageSize: 10,
      total: 0,
      visibleForm: false,
      editFormData: []
      
    };
  },
  mounted() {
    this.allTeacher();
  },

  methods: {
    // 提交更新讲师信息
    commit() {
      // let modifyData = this.editFormData
      this.modifyTeacher(this.editFormData);
    },

    inputListener() {
      this.allTeacher();
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v;
      this.allTeacher();
    },

    deleteById(index, row) {
      this.deleteTeacherById(row.id);
    },

    editById(index, row) {
      let modifyId = row.id;
      this.editFormData = row;
      this.visibleForm = true;
    },

    /**
     * 根据ID更新讲师
     */
    modifyTeacher(modifyData) {
      this.$axios
        .post("http://localhost:8080/teacher/modifyteacher/" + this.editFormData.id, modifyData)
        .then(res => {
          this.$message({ message: "更新成功", type: "success" });
          this.allTeacher();
          this.visibleForm = false;
        })
        .catch(error => {
          this.$message.error("更新失败");
        });
    },

    /**
     * 关键词搜索讲师
     */
    searchTeacher() {
      this.$axios
        .get("http://localhost:8080/teacher/searchteacher/" + this.keyword)
        .then(res => {
          this.teacherData = res.data.data.records;
          this.$message({message:'查询成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error('查询失败')
        });
    },

    /**
     * 根据ID删除讲师
     */
    deleteTeacherById(id) {
      this.$axios
        .delete("http://localhost:8080/teacher/delete/" + id)
        .then(res => {
          this.allTeacher();
          this.$message({message:'删除成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error("删除失败");
        });
    },

    /**
     * 获取所有讲师，带分页
     */
    allTeacher() {
      this.$axios
        .get("http://localhost:8080/teacher/queryteacher/" + this.page)
        .then(res => {
          let ret = res.data.data;
          this.teacherData = ret.records;
          this.total = ret.total;
          // this.$message({message:'查询成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error("查询讲师列表失败");
        });
    }
  }
};
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