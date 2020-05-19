<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-select
        v-model="value1"
        placeholder="年级"
        @change="queryClassByGrade"
        @clear="clearListener"
        clearable
      >
        <el-option v-for="item in grade" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-button slot="trigger" type="primary" @click="addClassInfo()">
        添加
        <i class="el-icon-folder-add el-icon--right"></i>
      </el-button>
      <!-- <el-select
        v-model="value2"
        placeholder="班级"
        @change="queryStudentByClass"
        @clear="classListener"
        clearable
      >
        <el-option
          v-for="item in classNo"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>-->
    </div>
    <div>
      <el-table :data="classInfoData" size="mini">
        <el-table-column label="序号" type="selection"></el-table-column>
        <el-table-column prop="gradeName" label="年级"></el-table-column>
        <el-table-column prop="classNo" label="班级编号"></el-table-column>
        <el-table-column prop="className" label="班级名称"></el-table-column>
        <el-table-column prop="realname" label="班主任"></el-table-column>
        <el-table-column prop="num" label="学生人数"></el-table-column>

        <el-table-column prop="operation" label="操作" width="150px">
          <template slot-scope="scope">
            <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
            <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
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
  </div>
</template>

<script>
export default {
  name: "ClassManager",
  data() {
    return {
      classInfoData: [],
      editFormData: [],
      visibleAdd: false,
      visibleEdit: false,
      page: 1,
      pageSize: 10,
      total: 0,
      value1: '',
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
      ]
    };
  },
  mounted() {
    this.allClassInfo();
  },
  methods: {

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v
      this.allClassInfo()
    },

    // 有选择年级的时候调用
    queryClassByGrade() {
      this.$axios
        .get("http://localhost:8080/queryclassinfo/" + this.page + "?gradeNo=" + this.value1)
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data
            this.classInfoData = ret.records
            this.total = ret.total
          }})
        .catch(error => {})
    },

    // 查询所有班级，分页
    allClassInfo() {
      this.$axios
        .get("http://localhost:8080/queryclassinfo/" + this.page)
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data
            this.classInfoData = ret.records
            this.total = ret.total
          }
        })
        .catch(error => {})
    },

    // 清除年级后重新查询所有班级
    clearListener() {
      this.value1 = ''
      this.allClassInfo()
    },

    // 根据id删除
    deleteById(index, row) {},

    editById(index, row) {},

    // 提交添加班级
    commitAdd() {},

    // 添加班级
    addClassInfo() {},

  }
};
</script>

<style lang="less" scoped>
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