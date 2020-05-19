<template>
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
    <el-upload
      class="add-button"
      ref="upload"
      accept=".xls,.xlsx"
      action="http://localhost:8080/upload"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :on-error="handleError"
      :on-success="uploadSuccess"
      :file-list="fileList"
      :auto-upload="false"
      :limit="1"
    >
      <el-button slot="trigger" size="small" type="primary">选取课程任务<i class="el-icon-upload2 el-icon--right"></i></el-button>
      <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器<i class="el-icon-upload el-icon--right"></i></el-button>
      <div slot="tip" class="el-upload__tip">只能上传xls/xlsx文件,导入新任务后将清空原来的任务,请一次性将本学期课程导入完毕</div>
    </el-upload>
    <!-- 下载模板 <a class="atag" href="http://localhost:8080/download">-->
    <el-button class="add-button" size="small" type="primary" @click="downloadTemplate()">
      下载模板
      <i class="el-icon-download el-icon--right"></i>
    </el-button>

    <el-button class="add-button" size="small" type="primary" @click="arrangeCourse()">
      排课
      <i class="el-icon-thumb el-icon--right"></i>
    </el-button>

    <!-- 开课任务，等待排课的课程 -->
    <el-table class="ckasstask-table" :data="classTaskData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <el-table-column prop="semester" label="学期" ></el-table-column>
      <el-table-column prop="gradeNo" label="年级" ></el-table-column>
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
      <el-table-column prop="ixFix" label="固定" ></el-table-column>
      <!-- 只有固定上课时间才会有固定的时间在时间这个列中 -->
      <el-table-column prop="classTime" label="时间" ></el-table-column>

      <el-table-column prop="operation" label="操作" width="150px">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 开课任务，等待排课的课程
    <el-table class="ckasstask-table" :data="classTaskData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <el-table-column prop="semester" label="学期" width="90"></el-table-column>
      <el-table-column prop="gradeNo" label="年级" width="45px"></el-table-column>
      <el-table-column prop="classNo" label="班级" width="80px"></el-table-column>
      <el-table-column prop="courseNo" label="课号" width="70px"></el-table-column>
      <el-table-column prop="courseName" label="课名" width="150px"></el-table-column>
      <el-table-column prop="courseAttr" label="课属性" width="60px"></el-table-column>
      <el-table-column prop="teacherNo" label="讲师编号" width="80px"></el-table-column>
      <el-table-column prop="realname" label="讲师" width="60px"></el-table-column>
      <el-table-column prop="studentNum" label="学生人数" width="80px"></el-table-column>
      <el-table-column prop="weeksNumber" label="周学时" width="60px"></el-table-column>
      <el-table-column prop="weeksSum" label="周数" width="45px"></el-table-column>
       是否固定时间 
      <el-table-column prop="ixFix" label="固定" width="45px"></el-table-column>
       只有固定上课时间才会有固定的时间在时间这个列中 
      <el-table-column prop="classTime" label="时间" width="60px"></el-table-column>

      <el-table-column prop="operation" label="操作" width="150px">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table> -->

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
      // 数据库中课程任务
      classTaskData: [],
      semesterData: [],

      page: 1,
      pageSize: 10,
      total: 0,
      // 学期选择绑定的值
      value: "",
      // 当前选择的学期
      semester: "2019-2020-1",

      fileList: []
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

    // 点击开始提交学期到系统后台排课
    arrangeCourse() {
      this.$axios.post("http://localhost:8080/arrange/" + this.semester)
      .then(res => {
        console.log(res)
        
        this.$message({message: '排课成功', type: 'success'})
        
      })
      .catch(error => {
        this.$message.error('排课失败');
      })
    },

    // 下载模板
    downloadTemplate() {
      window.location.href = 'http://localhost:8080/download'
    },

    // 上传成功
    uploadSuccess(response, file, fileList) {
      // location.reload()
      handleRemove(file, fileList)
    },

    handleRemove(file, fileList) {

    },

    handleError(error, file, fileList) {
      alert("文件上传失败" + error)
    },

    // 提交上传文件事件
    submitUpload() {
      this.$refs.upload.submit()
    },

    handlePreview(file) {
      
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
     * 获得所有开课任务，分页http://localhost:8080/classtask/{page}/{semester}
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