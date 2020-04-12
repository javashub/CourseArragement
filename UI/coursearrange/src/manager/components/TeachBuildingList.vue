<template>
  <div>
    <!-- 添加教学楼 -->
    
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

    <!-- 弹出表单编辑教学楼 -->
    <el-dialog title="编辑教学楼" :visible.sync="visibleForm">
      <el-form :model="editFormData" label-position="left" label-width="80px">
        <el-form-item label="编号">
          <el-input v-model="editFormData.teachBuildNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="editFormData.teachBuildName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="所在区域">
          <el-input v-model="editFormData.teachBuildLocation" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleForm = false">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>

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
      total: 0,
      editFormData: [],
      visibleForm: false
    };
  },
  mounted() {
    this.allTeachBuilding()
  },
  methods: {

    /**
     * 提交更新
     */
    commit() {
      this.modifyTeachBuild(this.editFormData)
    },

    handleSizeChange() {

    },

    deleteById(index, row) {
      this.deleteTeachBuildingById(row.id)
    },

    editById(index, row) {
      let modifyId = row.id
      this.editFormData = row
      this.visibleForm = true
    },

    handleCurrentChange(v) {
      this.page = v;
      this.allTeachBuilding()
    },

    /**
     * 更新教学楼
     */
    modifyTeachBuild(modifyData) {
      this.$axios
        .post("http://localhost:8080/teachbuildinfo/modify/" + this.editFormData.id, modifyData)
        .then(res => {
          this.$message({ message: "更新成功", type: "success" })
          this.allTeachBuilding()
          this.visibleForm = false
        })
        .catch(error => {
          this.$message.error("更新失败")
        });
    },

    // 获取所有教学楼，带分页
    allTeachBuilding() {
      this.$axios
        .get("http://localhost:8080/teachbuildinfo/list/" + this.page)
        .then(res => {
          console.log(res.data)
          let ret = res.data.data
          this.teachBuildData = ret.records
          this.total = ret.total
        })
        .catch(error => {
          console.log("查询教学楼失败")
        });
    },

    /**
     * 根据ID删除教学楼
     */
    deleteTeachBuildingById(id) {
      this.$axios
        .delete("http://localhost:8080/teachbuildinfo/delete/" + id)
        .then(res => {
          this.allTeachBuilding()
          this.$message({message:'删除成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error("删除失败")
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