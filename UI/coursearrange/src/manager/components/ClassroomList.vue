<template>
  <div>
    <div class="add-button">
      <el-button type="primary" @click="addRoom()">添加</el-button>
    </div>
    <!-- 数据显示 -->
    <el-table :data="classroomData" size="mini" :stripe="true" :highlight-current-row="true">
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

    <!-- 弹出表单编辑教室 -->
    <el-dialog title="" :visible.sync="visibleForm">
      <el-form :model="editFormData" label-position="left" label-width="80px" :rules="editFormRules">
        <el-form-item label="编号" prop="classroomNo">
          <el-input v-model="editFormData.classroomNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="名称" prop="classroomName">
          <el-input v-model="editFormData.classroomName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="所在楼栋" prop="teachbuildNo">
          <el-input v-model="editFormData.teachbuildNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input v-model="editFormData.capacity" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label=" 备注" prop="remark">
          <el-input v-model="editFormData.remark" autocomplete="off"></el-input>
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
  name: "ClassroomList",
  data() {
    return {
      classroomData: [],
      page: 1,
      pageSize: 10,
      total: 0,
      type: 1,
      editFormData: [],
      visibleForm: false,
      editFormRules: {
        classroomNo: [
           { required: true, message: '请输入教室编号', trigger: 'blur' },
        ],
        classroomName: [
           { required: true, message: '请输入教室名称', trigger: 'blur' },
        ],
        teachbuildNo: [
           { required: true, message: '请输入所在教学楼', trigger: 'blur' },
        ],
        capacity: [
           { required: true, message: '请输入教教室容量', trigger: 'blur' },
        ]
      }
    };
  },
  computed: {
    isAdd: () => {
      return true;
    }
  },
  mounted() {
    this.allClassroom()
  },
  methods: {

    addRoom() {
      this.editFormData = {}
      this.visibleForm = true
      this.type = 2
    },

    commit() {
      if (this.type === 1) {
        this.modifyClassroom(this.editFormData)
      } else {
        alert('添加')
        this.addClassroom(this.editFormData)
      } 
      
    },

    deleteById(index, row) {
      this.deleteClassroomById(row.id)
    },

    editById(index, row) {
      this.type = 1
      let modifyId = row.id
      this.editFormData = row
      this.visibleForm = true
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v;
      this.allClassroom()
    },

    /**
     * 添加教室
     */
    addClassroom(modifyData) {
      this.$axios
        .post("http://localhost:8080/classroom/add", modifyData)
        .then(res => {
          if (res.data.code == 0) {
            this.$message({ message: "添加成功", type: "success" })
            this.allClassroom()
            this.visibleForm = false
          } else {
            this.$message.error(res.data.message)
          }
          
        })
        .catch(error => {
          this.$message.error("更新失败")
        });
    },

    /**
     * 根据ID更新教室
     */
    modifyClassroom(modifyData) {
      this.$axios
        .post("http://localhost:8080/modify", modifyData)
        .then(res => {
          if (res.data.code == 0) {
            this.$message({ message: "更新成功", type: "success" })
            this.allClassroom()
            this.visibleForm = false
          } else {
            this.$message.error(res.data.message)
          }
        })
        .catch(error => {
          this.$message.error("更新失败")
        });
    },

    /**
     * 根据ID删除教室
     */
    deleteClassroomById(id) {
      this.$axios
        .delete("http://localhost:8080/classroom/delete/" + id)
        .then(res => {
          if (res.data.code == 0) {
            this.allClassroom();
            this.$message({message:'删除成功', type: 'success'})
          } else {
            this.$message.error(res.data.message)
          }
        })
        .catch(error => {
          this.$message.error("删除失败");
        });
    },

    // 获取所有教室，带分页
    allClassroom() {
      this.$axios
        .get("http://localhost:8080/classroom/" + this.page)
        .then(res => {
          console.log(res)
          if (res.data.code == 0) {
            let ret = res.data.data;
            this.classroomData = ret.records;
            this.total = ret.total;
          }
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

.add-button {
  margin-bottom: 5px;
  text-align: left;
}
</style>