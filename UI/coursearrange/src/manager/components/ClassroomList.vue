<template>
  <div>
    <!-- 数据显示 -->
    <el-table :data="classroomData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
      <el-table-column prop="classroomNo" label="教室编号"></el-table-column>
      <el-table-column prop="classroomName" label="教室名"></el-table-column>
      <el-table-column prop="teachbuildNo" label="所属教学楼"></el-table-column>
      <el-table-column prop="capacity" label="容量"></el-table-column>
      <el-table-column prop="remark" label="备注"></el-table-column>

      <el-table-column prop="operation" label="操作">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
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
  name: "ClassroomList",
  data() {
    return {
      classroomData: [],
      page: 1,
      pageSize: 10,
      total: 0
    };
  },
  mounted() {
    this.allClassroom();
  },
  methods: {
    deleteById(val) {
      console.log(val);
    },
    editById(index, row) {
      alert(index);
      alert(row);
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v;
      this.allClassroom();
    },

    // 获取所有教室，带分页
    allClassroom() {
      this.$axios
        .get("http://localhost:8080/classroom/queryclassroom/" + this.page)
        .then(res => {
          console.log(res.data);
          let ret = res.data.data;
          this.classroomData = ret.records;
          this.total = ret.total;
        })
        .catch(error => {
          console.log("查询教室失败");
        });
    }
  }
};
</script>

<style lang="less" scoped>
.footer-button {
  margin-top: 10px;
}
</style>