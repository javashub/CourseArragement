<template>
  <div>
    <!-- 学生端查看文档页面 -->
    <el-table :data="docData" :stripe="true" :highlight-current-row="true">
      <el-table-column label="序号" type="selection"></el-table-column>
      <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
      <el-table-column prop="docName" label="文件名"></el-table-column>
      <el-table-column prop="description" label="简介"></el-table-column>
      <el-table-column prop="fromUserName" label="发布者"></el-table-column>
      <el-table-column prop="createTime" label="上传时间"></el-table-column>

      <el-table-column prop="operation" label="操作">
        <template slot-scope="scope">
          <!-- <el-button type="text" size="small" @click="previewById(scope.$index, scope.row)">预览</el-button> -->
          <el-button type="text" size="small" @click="downloadById(scope.$index, scope.row)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页插件 -->
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
  name: "StudentDoc",
  data() {
    return {
      docData: [],
      // 学生班级编号
      toClassNo: "",
      page: 1,
      total: 0,
      pageSize: 10
    };
  },
  mounted() {
    this.allDocs();
  },
  methods: {
    // 下载
    downloadById(index, row) {
      console.log(row.docUrl);
      window.location.href = row.docUrl;
    },

    handleSizeChange(v) {
      this.page = v;
      this.allDocs();
    },

    handleCurrentChange() {},

    // 查询所有文档
    allDocs() {
      let user = window.localStorage.getItem("student");
      // 得到学生所在班级的编号
      this.toClassNo = JSON.parse(user).classNo;
      if (this.classNo === "" || this.classNo === null) {
        this.$message.error("您还未加入班级呢，暂时无法查看文档哦");
      } else {
        this.$axios
          .get(
            "http://localhost:8080/docs-class/" +
              this.page +
              "/" +
              this.toClassNo
          )
          .then(res => {
            console.log(res);
            if (res.data.code == 0) {
              let ret = res.data.data;
              this.docData = ret.records;
              this.total = ret.total;
            } else {
              this.$message.error(res.data.message);
            }
          })
          .catch(error => {});
      }
    }
  }
};
</script>

<style lang="less" scoped>
</style>