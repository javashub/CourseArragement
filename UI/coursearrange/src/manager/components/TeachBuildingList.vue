<template>
  <div>
    <!-- 教学楼列表 -->
    <el-table :data="teachBuildData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
      <el-table-column prop="teachBuildNo" label="教学楼编号"></el-table-column>
      <el-table-column prop="teachBuildName" label="教学楼名称"></el-table-column>
      <el-table-column prop="teachBuildLocation" label="所属区域"></el-table-column>

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
  name: "TeachBuildingList",
  data() {
    return {
      teachBuildData: [],
      page: 1,
      pageSize: 10,
      total: 0
    };
  },
  mounted() {
    this.allTeachBuilding();
  },
  methods: {

    handleSizeChange() {

    },

    deleteById(val) {
      console.log(val);
    },

    editById(index, row) {
      alert(index);
      alert(row);
    },

    handleCurrentChange(v) {
      this.page = v;
      this.allTeachBuilding();
    },

    // 获取所有教室，带分页
    allTeachBuilding() {
      this.$axios
        .get("http://localhost:8080/teachbuildinfo/list/" + this.page)
        .then(res => {
          console.log(res.data);
          let ret = res.data.data;
          this.teachBuildData = ret.records;
          this.total = ret.total;
        })
        .catch(error => {
          console.log("查询教学楼失败");
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