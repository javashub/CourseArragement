<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索教材" v-model="keyword" @clear="inputListener" clearable>
        <el-button slot="append" type="primary" icon="el-icon-search" @click="searchCourse">搜索</el-button>
      </el-input>
      <el-button slot="append" type="primary" @click="addInfo">添加</el-button>
    </div>
    <!-- 上面放一个说明，添加交材按钮 -->

    <!-- 教材信息 -->
    <el-table :data="courseInfoData" size="mini" :stripe="true" :highlight-current-row="true">
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

    <!-- 弹出表单编辑教材信息 -->
    <el-dialog title="编辑教材" :visible.sync="visibleForm">
      <el-form :model="editFormData" label-position="left" label-width="80px" :rules="editFormRules">
        <el-form-item label="课程编号" prop="courseNo">
          <el-input v-model="editFormData.courseNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="editFormData.courseName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="课程属性" prop="courseAttr">
          <el-input v-model="editFormData.courseAttr" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="editFormData.publisher" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editFormData.remark" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 分页 -->
    <div class="footer-button">
      <el-pagination
        background
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
      editFormData: {
        courseNo: ''
      },
      keyword: '',
      page: 1,
      pageSize: 10,
      total: 0,
      type: 1,
      visibleForm: false,
      editFormRules: {
        courseName: [
           { required: true, message: '请输入教材名', trigger: 'blur' },
        ],
        courseAttr: [
           { required: true, message: '课程属性，01:语数英，02:物化生政史地,03:实验课,04:体育课', trigger: 'blur' },
        ]
      }
    };
  },
  mounted() {
    this.allCourseInfo();
  },
  methods: {
    // 子页面提交方法
    commit() {
      if (this.type == 1) {
        this.modifyCourseInfo(this.editFormData)
      } else {
        this.add()
      }
    },

    cancel() {
      this.visibleForm = false
      this.editFormData.courseName = ''
      this.editFormData.courseAttr = ''
      this.editFormData.publisher = ''
      this.editFormData.remark = ''
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v;
      if (this.keyword == '') {
        this.allCourseInfo()
      } else {
        this.searchCourse()
      }
    },

    // 监听搜索框
    inputListener() {
      this.allCourseInfo();
    },

    // 根据关键字搜索交材信息,有问题呀
    searchCourse() {
      this.page = 1
      this.$axios
        .get("http://localhost:8080/courseinfo/search/" + this.page + "/" + this.keyword)
        .then(res => {
          let ret = res.data.data
          this.courseInfoData = ret.records
          this.total = ret.total
          this.$message({message:'查询成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error('查询失败')
        });
    },

    // 删除教材信息
    deleteById(index, row) {
      this.deleteCourseInfoById(row.id)
    },

    // 编辑教材
    editById(index, row) {
      let modifyId = row.id
      this.editFormData = row
      this.visibleForm = true
      this.type = 1
    },

    addInfo() {
      this.type = 2 // 添加
      this.visibleForm = true
      // this.editFormData = {}
      this.$axios.get("http://localhost:8080/courseinfo/get-no")
      .then(res => {
        if (res.data.code == 0) {
          // 获取课程编号
          this.editFormData.courseNo = res.data.message
        } else {
          this.$message.error(res.data.message)
        }
      })
      .catch(error => {})
    },

    // 添加教材
    add() {
      this.$axios.post("http://localhost:8080/courseinfo/add", this.editFormData)
      .then(res => {
        if (res.data.code == 0) {
          this.allCourseInfo()
          this.$message({message:'添加成功', type: 'success'})
          this.visibleForm = false
          this.editFormData = {}
        } else {
          this.$message.error(res.data.message)
        }
      })
      .catch(error => {})
    },

    // 根据id删除教材信息
    deleteCourseInfoById(id) {
      this.$axios
      .delete("http://localhost:8080/courseinfo/delete/" + id)
      .then(res => {
        this.allCourseInfo()
        this.$message({message:'删除成功', type: 'success'})
      })
      .catch(error => {
        this.$message.error("删除失败")
      });
    },

    // 更新教材信息
    modifyCourseInfo(modifyData) {
      this.$axios
        .post("http://localhost:8080/courseinfo/modify/" + this.editFormData.id, modifyData)
        .then(res => {
          this.$message({ message: "更新成功", type: "success" })
          this.allCourseInfo()
          this.visibleForm = false
        })
        .catch(error => {
          this.$message.error("更新失败")
        });
    },

    // 分页查询所有教材信息
    allCourseInfo() {
      this.$axios
        .get("http://localhost:8080/courseinfo/" + this.page)
        .then(res => {
          let ret = res.data.data
          this.courseInfoData = ret.records
          this.total = ret.total
        })
        .catch(error => {
          this.$message.error("查询教材信息失败")
        });
    }
  }
};
</script>

<style scoped lang="less">
  .footer-button {
    margin-top: 10px;
  }

  .el-input-group {
    width: 40%;
  }

  .header-menu {
    margin-bottom: 5px;
    padding: 0;
    text-align: left;
    margin-bottom: 5px;
  }
</style>