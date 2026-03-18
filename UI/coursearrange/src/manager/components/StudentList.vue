<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索学生" v-model="keyword" @clear="inputListener" clearable>
        <el-button slot="append" type="primary" icon="el-icon-search" @click="searchStudent">搜索</el-button>
      </el-input>
      <el-select v-model="value1" placeholder="年级" @change="queryClass" @clear="gradeListener" clearable>
        <el-option v-for="item in grade" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-select v-model="value2" placeholder="班级" @change="queryStudentByClass" @clear="classListener" clearable>
        <el-option
          v-for="item in classNo"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>
    </div>
    <!-- 数据显示 -->
    <el-table :data="studentData" size="mini" :stripe="true" :highlight-current-row="true">
      <el-table-column label="序号" type="selection"></el-table-column>
      <!-- <el-table-column prop="id" label="ID"></el-table-column> -->
      <el-table-column prop="studentNo" label="学号" fixed width="100"></el-table-column>
      <el-table-column prop="realname" label="姓名" fixed width="100"></el-table-column>
      <el-table-column prop="username" label="昵称" fixed width="100"></el-table-column>
      <el-table-column prop="grade" label="年级" fixed width="100"></el-table-column>
      <el-table-column prop="classNo" label="班级" fixed width="100"></el-table-column>
      <el-table-column prop="age" label="年龄" fixed width="80"></el-table-column>
      <el-table-column prop="telephone" label="电话" fixed width="100"></el-table-column>
      <el-table-column prop="email" label="邮件" fixed width="150"></el-table-column>
      <el-table-column prop="address" label="地址"></el-table-column>

      <el-table-column prop="operation" label="操作">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹出表单编辑学生 -->
    <el-dialog title="编辑学生" :visible.sync="visibleForm">
      <el-form
        :model="editFormData"
        label-position="left"
        label-width="80px"
        :rules="editFormRules"
      >
        <el-form-item label="学号">
          <el-input v-model="editFormData.studentNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="username">
          <el-input v-model="editFormData.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realname">
          <el-input v-model="editFormData.realname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="editFormData.grade" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="班级" prop="classNo">
          <el-input v-model="editFormData.classNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="手机" prop="telephone">
          <el-input v-model="editFormData.telephone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮件" prop="email">
          <el-input v-model="editFormData.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editFormData.address" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="editFormData.age" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleForm = false">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 上一页，当前页，下一页 -->
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
  deleteStudent,
  fetchStudentPage,
  fetchStudentsByClassPage,
  searchStudentPage,
  updateStudent
} from "@/api/modules/base";
import { fetchClassOptions } from "@/api/modules/course";

export default {
  name: "StudentList",
  data() {
    return {
      studentData: [],
      editFormData: [],
      keyword: "",
      page: 1,
      pageSize: 10,
      total: 0,
      value1: "", // 年级
      value2: "", // 班级
      grade: [
        {
          value: "01",
          label: "高一"
        },
        {
          value: "02",
          label: "高二"
        },
        {
          value: "03",
          label: "高三"
        }
      ],
      classNo: [
        {
          value: "",
          label: ""
        }
      ],
      visibleForm: false,
      editFormRules: {
        username: [{ required: true, message: "请输入昵称", trigger: "blur" }],
        realname: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        grade: [{ required: true, message: "请输入年级", trigger: "blur" }],
        classNo: [{ required: true, message: "请输入班级", trigger: "blur" }],
        telephone: [{ required: true, message: "请输入联系电话", trigger: "blur" }],
        email: [{ required: true, message: "请输入邮件", trigger: "blur" }],
        address: [{ required: true, message: "请输入地址", trigger: "blur" }],
        age: [{ required: true, message: "请输入年龄", trigger: "blur" }]
      }
    };
  },

  mounted() {
    this.allStudent();
  },

  methods: {

    // 清空年级回到查询所有学生
    gradeListener() {
      this.allStudent()
      this.value2 = ''
    },

    // 清空班级回到查询所有班级
    classListener() {
      this.value2 = ''
      this.allStudent()
    },

    // 查询班级信息
    async queryClass() {
      try {
        const response = await fetchClassOptions(this.value1)
        let ret = response.data || []
        this.classNo.splice(0, this.classNo.length)
        this.value2 = ""
        ret.forEach(v => {
          this.classNo.push({
            value: v.classNo,
            label: v.className || v.classNo
          });
        });
      } catch (error) {}
    },

    // 根据班级查询学生信息
    async queryStudentByClass() {
      try {
        const response = await fetchStudentsByClassPage(this.page, this.value2, this.pageSize)
        let ret = response.data || {}
        this.studentData = ret.records || []
        this.total = ret.total || 0
      } catch (error) {}
    },

    /***
     * 编辑提交
     */
    commit() {
      this.modifyStudent(this.editFormData)
    },

    inputListener() {
      this.allStudent()
    },

    /**
     * 查询所有学生
     */
    async allStudent() {
      try {
        const response = await fetchStudentPage(this.page, this.pageSize)
        let ret = response.data || {}
        this.studentData = ret.records || []
        this.total = ret.total || 0
      } catch (error) {
        this.$message.error("查询学生列表失败")
      }
    },

    /**
     * 关键字查询学生
     */
    async searchStudent() {
      try {
        const response = await searchStudentPage(this.keyword, this.page, this.pageSize)
        let ret = response.data || {}
        this.studentData = ret.records || []
        this.total = ret.total || 0
        this.$message({ message: "查询成功", type: "success" })
      } catch (error) {
        this.$message.error("查询失败")
      }
    },

    /**
     * 根据id删除学生
     */
    deleteById(index, row) {
      this.deleteStudentById(row.id)
    },

    async deleteStudentById(id) {
      try {
        await deleteStudent(id)
        this.$message({ message: "删除成功", type: "success" })
        this.allStudent()
      } catch (error) {
        this.$message.error("删除失败")
      }
    },

    /**
     * 编辑学生
     */
    editById(index, row) {
      let modifyId = row.id
      this.editFormData = row
      this.visibleForm = true
    },

    /**
     * 更新学生
     */
    async modifyStudent(modifyData) {
      try {
        await updateStudent(this.editFormData.id, modifyData)
        this.$message({ message: "更新成功", type: "success" })
        this.allStudent()
        this.visibleForm = false
      } catch (error) {
        this.$message.error("更新失败")
      }
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v
      this.allStudent()
    }
  }
};
</script>

<style lang="less" scoped>
.el-input-group {
  width: 40%;
}
.header-menu {
  margin-bottom: 5px;
  padding: 0;
  text-align: left;
  margin-bottom: 5px;
}

.footer-button {
  margin-top: 10px;
}
</style>
