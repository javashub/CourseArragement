<template>
  <div>
    <!-- 添加教学楼 -->
    <div class="add-button">
      <el-button type="primary" @click="addTeachbuild">添加</el-button>
    </div>
    <!-- 教学楼列表 -->
    <el-table :data="teachBuildData" size="mini" :stripe="true" :highlight-current-row="true">
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
    <el-dialog title="" :visible.sync="visibleForm">
      <el-form :model="editFormData" label-position="left" label-width="80px" :rules="editFormRules">
        <el-form-item label="编号">
          <el-input v-model="editFormData.teachBuildNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="名称" prop="teachBuildName">
          <el-input v-model="editFormData.teachBuildName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="所在区域" prop="teachBuildLocation">
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
import {
  createLegacyTeachbuild,
  deleteLegacyTeachbuild,
  fetchLegacyTeachbuildPage,
  updateLegacyTeachbuild
} from "@/api/modules/base";

export default {
  name: "TeachBuildingList",
  data() {
    return {
      teachBuildData: [],
      page: 1,
      pageSize: 10,
      total: 0,
      type: 1, // 编辑
      editFormData: [],
      visibleForm: false,
      editFormRules: {
        teachBuildNo: [
           { required: true, message: '请输入教学楼编号', trigger: 'blur' },
        ],
        teachBuildLocation: [
           { required: true, message: '请输入教学楼位置', trigger: 'blur' },
        ],
        teachBuildName: [
           { required: true, message: '请输入教学楼名称', trigger: 'blur' },
        ]
      }
    };
  },

  mounted() {
    this.allTeachBuilding()
  },

  methods: {

    addTeachbuild() {
      this.visibleForm = true
      this.type = 2
      this.editFormData = {}
    },

    /**
     * 提交更新
     */
    commit() {
      if (this.type == 1) {
        this.modifyTeachBuild(this.editFormData)
      } else {
        this.newteachbuild(this.editFormData)
      }
      
    },

    // 添加教学楼
    async newteachbuild(modifyData) {
      try {
        await createLegacyTeachbuild(modifyData)
        this.$message({ message: "添加成功", type: "success" })
        this.allTeachBuilding()
        this.visibleForm = false
      } catch (error) {
        this.$message.error("更新失败")
      }
    },

    handleSizeChange() {},

    deleteById(index, row) {
      this.deleteTeachBuildingById(row.id)
    },

    editById(index, row) {
      let modifyId = row.id
      this.editFormData = row
      this.visibleForm = true
      this.type = 1
    },

    handleCurrentChange(v) {
      this.page = v;
      this.allTeachBuilding()
    },

    /**
     * 更新教学楼
     */
    async modifyTeachBuild(modifyData) {
      try {
        await updateLegacyTeachbuild(this.editFormData.id, modifyData)
        this.$message({ message: "更新成功", type: "success" })
        this.allTeachBuilding()
        this.visibleForm = false
      } catch (error) {
        this.$message.error("更新失败")
      }
    },

    // 获取所有教学楼，带分页
    async allTeachBuilding() {
      try {
        const response = await fetchLegacyTeachbuildPage(this.page, this.pageSize)
        let ret = response.data || {}
        this.teachBuildData = ret.records || []
        this.total = ret.total || 0
      } catch (error) {
        console.log("查询教学楼失败")
      }
    },

    /**
     * 根据ID删除教学楼
     */
    async deleteTeachBuildingById(id) {
      try {
        await deleteLegacyTeachbuild(id)
        this.allTeachBuilding()
        this.$message({message:'删除成功', type: 'success'})
      } catch (error) {
        this.$message.error("删除失败")
      }
    }
  }
};
</script>

<style lang="less" scoped>
.footer-button {
  margin-top: 10px;
}

.add-button {
  margin-bottom: 5px;
  text-align: left;
}
</style>
