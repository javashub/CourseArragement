<template>
  <div>
    <div>
      <div>
        <!-- 下拉选择学期 -->
        <el-select
          class="semester-select"
          @change="handleSelectChange"
          clearable
          v-model="value"
          placeholder="请选择学期"
        >
          <el-option v-for="(item,index) in semesterData" :key="index" :value="item"></el-option>
        </el-select>
        
        <!-- 添加课程计划 -->
        <div>
          <el-upload
            class="add-button"
            ref="upload"
            accept=".xls,.xlsx"
            action="http://localhost:8080/upload"
            :on-remove="handleRemove"
            :on-error="handleError"
            :on-success="uploadSuccess"
            :disabled="importBtnDisabled"
            :file-list="fileList"
            :auto-upload="false"
            :limit="1"
          >
            <el-button style="margin-left: 10px;" slot="trigger" size="small" type="primary">从Excel导入<i class="el-icon-upload2 el-icon--right"></i></el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" :loading="loading" @click="submitUpload">上传到服务器<i class="el-icon-upload el-icon--right"></i></el-button>
            <el-button class="add-button" size="small" type="primary" @click="addClassTask()">手动添加</el-button>
            <!-- 下载模板 <a class="atag" href="http://localhost:8080/download">-->
            <el-button class="add-button" size="small" type="primary" @click="downloadTemplate()">
              下载模板
              <i class="el-icon-download el-icon--right"></i>
            </el-button>
            <div slot="tip" class="el-upload__tip">只能上传xls/xlsx文件,导入新任务后将清空原来的任务,请一次性将本学期课程导入完毕</div>
          </el-upload>
          <el-button class="add-button" size="small" type="primary" @click="arrangeCourse()">
            排课
            <i class="el-icon-thumb el-icon--right"></i>
          </el-button>
        </div>
      </div>
    </div>
    <!-- 开课任务，等待排课的课程 -->
    <el-table class="ckasstask-table" :data="classTaskData" size="mini" :stripe="true" :highlight-current-row="true">
      <el-table-column label="序号" type="selection"></el-table-column>
      <el-table-column prop="semester" label="学期" ></el-table-column>
      <el-table-column prop="gradeNo" label="年级" width="60px"></el-table-column>
      <el-table-column prop="classNo" label="班级" ></el-table-column>
      <el-table-column prop="courseNo" label="课号" ></el-table-column>
      <el-table-column prop="courseName" label="课名" ></el-table-column>
      <el-table-column prop="courseAttr" label="课属性" ></el-table-column>
      <el-table-column prop="teacherNo" label="讲师编号" ></el-table-column>
      <el-table-column prop="realname" label="讲师" ></el-table-column>
      <el-table-column prop="studentNum" label="学生人数" ></el-table-column>
      <el-table-column prop="weeksNumber" label="周学时" ></el-table-column>
      <el-table-column prop="weeksSum" label="周数" ></el-table-column>
      <!-- 是否固定时间 -->
      <el-table-column prop="isFix" label="固定" ></el-table-column>
      <!-- 只有固定上课时间才会有固定的时间在时间这个列中 -->
      <el-table-column prop="classTime" label="时间" ></el-table-column>

      <el-table-column prop="operation" label="操作" width="150px">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹出表单添加讲师 -->
    <el-dialog title="添加任务(参照模板填写)" :visible.sync="visible">
      <el-form :model="addClassTaskForm" label-position="left" label-width="80px" :rules="addClassTaskRules">
        <el-form-item label="学期" prop="semester">
          <el-input v-model="addClassTaskForm.semester" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年级" prop="gradeNo">
          <el-input v-model="addClassTaskForm.gradeNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="班级编号" prop="classNo">
          <el-input v-model="addClassTaskForm.classNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="课程编号" prop="courseNo">
          <el-input v-model="addClassTaskForm.courseNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="课程名" prop="courseName">
          <el-input v-model="addClassTaskForm.courseName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="讲师编号" prop="teacherNo">
          <el-input v-model="addClassTaskForm.teacherNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="讲师名字" prop="realname">
          <el-input v-model="addClassTaskForm.realname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="课程属性" prop="courseAttr">
          <el-input v-model="addClassTaskForm.courseAttr" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="学生人数" prop="studentNum">
          <el-input v-model="addClassTaskForm.studentNum" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="周学时" prop="weeksNumber">
          <el-input v-model="addClassTaskForm.weeksNumber" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="周数" prop="weeksSum">
          <el-input v-model="addClassTaskForm.weeksSum" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="是否固定" prop="isFix">
          <el-input v-model="addClassTaskForm.isFix" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="上课时间" prop="classTime">
          <el-input v-model="addClassTaskForm.classTime" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visible = false">取 消</el-button>
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
  name: "ClassTaskList",
  data() {
    return {
      importBtnDisabled: false, // 按钮是否禁用,
      loading: false,
      classTaskData: [],
      semesterData: [],
      addClassTaskForm: {
        semester: '',
        gradeNo: '',
        classNo: '',
        courseNo: '',
        courseName: '',
        teacherNo: '',
        realname: '',
        courseAttr: '',
        studentNum: '',
        weeksNumber: '',
        weeksSum: '',
        isFix: '',
        classTime: ''
      },
      visible: false,
      page: 1,
      total: 0,
      pageSize: 10,
      // 学期选择绑定的值
      value: "2019-2020-1",
      // 当前选择的学期
      semester: "2019-2020-1",
      fileList: [],
      addClassTaskRules: {
        semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
        gradeNo: [{ required: true, message: '请输入年级编号', trigger: 'blur' }],
        classNo: [{ required: true, message: '请输入班级编号', trigger: 'blur' }],
        courseNo: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
        courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
        teacherNo: [{ required: true, message: '请输入讲师编号', trigger: 'blur' }],
        realname: [{ required: true, message: '请输入讲师姓名', trigger: 'blur' }],
        courseAttr: [{ required: true, message: '请输入课程属性', trigger: 'blur' }],
        studentNum: [{ required: true, message: '请输入班级学生人数', trigger: 'blur' }],
        weeksNumber: [{ required: true, message: '请输入周学时', trigger: 'blur' }],
        weeksSum: [{ required: true, message: '请输入上课周数', trigger: 'blur' }],
        isFix: [{ required: true, message: '是否固定上课时间', trigger: 'blur' }],
        classTime: []
      }
    };
  },

  /**
   * 加载Vue实例时执行
   */
  mounted() {
    this.allClassTask();
    this.getSemester();
  },

  methods: {

    // 提交添加
    commit() {
      this.$axios.post("http://localhost:8080/addclasstask", this.addClassTaskForm)
      .then(res => {
        if (res.data.code == 0) {
          // 添加完成
          this.allClassTask()
          this.visible = false
          this.$message({message: "添加课程任务成功！", type: "success"})
        } else {
          alert(res.data.message)
        }
      })
      .catch(error => {

      })
    },

    // 手动添加课程任务
    addClassTask() {
      this.visible = true
    },

    // 点击开始提交学期到系统后台排课
    arrangeCourse() {
      this.$axios.post("http://localhost:8080/arrange/" + this.semester)
      .then(res => {
        if (res.data.code == 0) {
          this.allClassTask()
          this.$message({message: '排课成功', type: 'success'})
          this.$router.push('/coursetable')
        } else {
          this.$message.error('排课失败')
          this.$message.error(res.data.message)
        }
      })
      .catch(error => {
        this.$message.error('排课失败')
      })
    },

    // 下载模板
    downloadTemplate() {
      window.location.href = 'http://localhost:8080/download'
    },

    // 上传成功
    uploadSuccess(response, file, fileList) {
      this.loading = false
      this.allClassTask()
      this.$message({message: "上传成功", type:"success"})
    },

    handleRemove(file, fileList) {

    },

    handleError(error, file, fileList) {
      alert("文件上传失败" + error)
    },

    // 提交上传文件事件
    submitUpload() {
      this.loading = true
      this.$refs.upload.submit()
    },

    // 得到对应选中的年级
    handleSelectChange(val) {
      // 这里的V就是选择的学期了
      this.semester = val
      
    },

    deleteById(index, row) {
      this.deleteClassTaskById(row.id);
    },

    editById(index, row) {
      
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v
      this.allClassTask()
    },

    /**
     * 获得学期列表后默认填充第一个到学期选择下拉列表中
     * 这里只放一个学期先吧
     */
    getSemester() {
      this.$axios
        .get("http://localhost:8080/semester")
        .then(res => {
          let ret = res.data.data
          this.semesterData = ret
        })
        .catch(error => {
          console.log("查询学期失败");
        });
    },

    /**
     * 获得所有开课任务
     */
    allClassTask() {
      this.$axios
        .get(
          "http://localhost:8080/classtask/" + this.page + "/" + this.semester
        )
        .then(res => {
          let ret = res.data.data
          this.classTaskData = ret.records
          this.total = ret.total
          if (this.total == 0) {
            this.$message({message: '查询不到开课任务', type: 'success'})
          }
        })
        .catch(error => {
          this.$message.error("查询开课任务失败");
        });
    },

    /**
     * 删除开课任务
     */
    deleteClassTaskById(id) {
      this.$axios
        .delete("http://localhost:8080/deleteclasstask/" + id)
        .then(res => {
          this.allClassTask();
          this.$message({ message: "删除成功", type: "success" })
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

.semester-select {
  float: left;
  margin-bottom: 10px;
}

.tips {
  float: left;
  margin-left: 5px;
}

.ckasstask-table {
  margin-top: 10px;
}

.add-button {
  float: left;
  margin-left: 15px;
}
</style>