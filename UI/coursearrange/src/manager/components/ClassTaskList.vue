<template>
  <div>
    <!-- 下拉选择学期 -->
    <el-select
      class="semester-select"
      @change="handleSelectChange"
      clearable
      v-model="value"
      placeholder="选择学期"
    >
      <el-option v-for="(item,index) in semesterData" :key="index" :value="item"></el-option>
    </el-select>

    <!-- 添加课程计划 -->
    <el-button class="addButton" type="primary" @click="btnAddClassTask()">添加课程计划</el-button>
    <!-- 提示信息 -->

    <!-- <el-popover
    class="tips"
    placement="top-start"
    width="200"
    trigger="hover"
    content="这是一段内容,这是一段内容,这是一段内容,这是一段内容。">
    <el-button slot="reference" icon="el-icon-question" type="info" circle></el-button>
    </el-popover>-->

    <!-- 开课任务，等待排课的课程 -->
    <el-table class="ckasstask-table" :data="classTaskData" size="mini">
      <el-table-column label="序号" type="selection"></el-table-column>
      <el-table-column prop="semester" label="学期"></el-table-column>
      <el-table-column prop="gradeNo" label="年级"></el-table-column>
      <el-table-column prop="classNo" label="班级"></el-table-column>
      <el-table-column prop="courseNo" label="学科"></el-table-column>
      <el-table-column prop="courseAttr" label="课程属性"></el-table-column>
      <el-table-column prop="teacherNo" label="讲师"></el-table-column>
      <el-table-column prop="studentNum" label="学生人数"></el-table-column>
      <el-table-column prop="weeksNumber" label="周学时"></el-table-column>
      <el-table-column prop="weeksSum" label="周数"></el-table-column>
      <!-- 是否固定时间 -->
      <el-table-column prop="ixFix" label="固定"></el-table-column>
      <!-- 只有固定上课时间才会有固定的时间在时间这个列中 -->
      <el-table-column prop="classTime" label="时间"></el-table-column>

      <el-table-column prop="operation" label="操作" width="150px">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加开课任务表单 -->
    <!-- 年级，班级，课程，讲师，课程属性，学生人数，周数，周学时，是否固定时间，时间 -->
    <el-dialog title="添加课程计划" :visible.sync="visibleForm" show-close="true">
      <el-form :model="classAddForm">
        <el-form-item label="学期" :label-width="formLabelWidth">
          <el-select placeholder="请选择学期">
            <el-option label="2019-2020-1" value="2019-2020-1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年级" :label-width="formLabelWidth">
          <el-select placeholder="请选择年级" clearable>
            <el-option ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
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
      // 数据库中课程任务
      classTaskData: [],
      semesterData: [],
      classAddForm: [], // 添加课程计划的窗口
      page: 1,
      pageSize: 10,
      total: 0,
      // 学期选择绑定的值
      value: "",
      // 当前选择的学期
      semester: "",
      visibleForm: false,
      formLabelWidth: '80px',
      // 添加课程计划表单的年级下拉值
      options: [{
        value: '01',
        label: '高一'
      }, {
        value: '02',
        label: ''
      }, {
        value: '',
        label: ''
      }]
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
    // 添加课程计划按钮响应事件
    btnAddClassTask() {
      this.visibleForm = true;
    },

    // 得到对应选中的年级
    handleSelectChange(val) {
      // 这里的V就是选择的学期了
      this.semester = val;
      alert(v);
    },

    deleteById(index, row) {
      this.deleteClassTaskById(row.id);
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

    /**
     * 获得学期列表后默认填充第一个到学期选择下拉列表中
     * 这里只放一个学期先吧
     */
    getSemester() {
      this.$axios
        .get("http://localhost:8080/semester")
        .then(res => {
          console.log(res);
          let ret = res.data.data;
          this.semesterData = ret;
          console.log(ret);
        })
        .catch(error => {
          console.log("查询教室失败");
        });
    },

    /**
     * 获得所有开课任务，分页http://localhost:8080/classtask/{page}/{semester}
     */
    allClassTask() {
      this.$axios
        .get("http://localhost:8080/classtask/" + 1 + "/" + "2019-2020-1")
        .then(res => {
          let ret = res.data.data;
          this.classTaskData = ret.records;
          this.total = ret.total;
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
          this.$message({ message: "删除成功", type: "success" });
        })
        .catch(error => {
          this.$message.error("删除失败");
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
  background-color: aqua;
}

.addButton {
  float: left;
  margin-left: 30px;
}
</style>