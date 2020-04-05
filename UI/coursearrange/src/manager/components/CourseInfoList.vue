<template>
  <div>
    <!-- 教材信息 -->
    <el-table :data="courseInfoData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
      <el-table-column prop="courseNo" label="课程编号"></el-table-column>
      <el-table-column prop="courseName" label="课程名"></el-table-column>
      <el-table-column prop="courseAttr" label="课程属性"></el-table-column>
      <el-table-column prop="publisher" label="出版社"></el-table-column>
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
  name: "CourseInfoList",
  data() {
    return {
      courseInfoData: [],
      page: 1,
      pageSize: 10,
      total: 0
    };
  },
  mounted() {
    this.allCourse();
  },
  methods: {
    handleSizeChange() {},
    handleCurrentChange(v) {
      this.page = v;
      this.allCourse();
    },

    deleteById(val) {
      console.log(val);
    },
    editById(index, row) {
      alert(index);
      alert(row);
    },
    allCourse() {
      this.$axios
        .get("http://localhost:8080/courseinfo/" + this.page)
        .then(res => {
          console.log(this.courseInfoData);
          let ret = res.data.data;
          this.courseInfoData = ret.records;
          this.total = ret.total;
        })
        .catch(error => {
          console.log("查询失败");
        });
    }
  }
};
</script>

<style scoped lang="less">
</style>