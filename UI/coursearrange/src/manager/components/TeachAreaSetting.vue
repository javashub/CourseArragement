<template>
  <div>
    <div class="button">
      <el-button type="primary" @click="addTeachArea()">新增安排</el-button>
    </div>
    <div class="wrapper">
      <el-table :data="locationData" size="mini" :stripe="true" :highlight-current-row="true">
        <el-table-column label="序号" type="selection"></el-table-column>
        <el-table-column prop="gradeNo" label="年级编号"></el-table-column>
        <el-table-column prop="gradeName" label="年级"></el-table-column>
        <el-table-column prop="teachbuildNo" label="教学楼编号"></el-table-column>
        <el-table-column prop="teachBuildName" label="教学楼名称"></el-table-column>

        <el-table-column prop="operation" label="操作" width="150px">
          <template slot-scope="scope">
            <!-- <el-popconfirm
              title="确定删除吗？"
              @onConfirm="deleteById"
            > -->
            <el-button type="danger" size="mini" slot="reference" @click="deleteById(scope.$index, scope.row)">删除</el-button>
            <!-- </el-popconfirm> -->
            <!-- <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button> -->
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

    <!-- 弹出表单添加 -->
    <el-dialog title="设置教学区域" :visible.sync="visible">
      <el-form label-position="left" label-width="80px" :model="addForm">
        <el-select v-model="addForm.value1" placeholder="请选择年级" @change="selectGrade" clearable>
          <el-option
            v-for="item in grade"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
        <el-select
          v-model="addForm.value2"
          placeholder="请选择教学楼"
          @change="selectTeachbuild"
          clearable
        >
          <el-option
            v-for="item in teachbuild"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
      </el-form>

      <div slot="footer">
        <el-button @click="visible = false">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "TeachAreaSetting",
  data() {
    return {
      page: 1,
      total: 0,
      pageSize: 10,
      locationData: [],
      teachBuildNo: "",
      gradeNo: "",
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
      teachbuild: [
        {
          value: "",
          label: ""
        }
      ],
      visible: false,
      addForm: {
        value1: "", // 年级
        value2: "" // 教学楼
      }
    };
  },
  mounted() {
    this.allLocation();
  },
  methods: {
    // 提交添加区域请求
    commit() {
      if (this.addForm.value1 == "" || this.addForm.value2 == "") {
        alert("请选择再提交");
      } else {
        this.$axios
          .post("http://localhost:8080/setteacharea", {
            teachBuildNo: this.addForm.value2,
            gradeNo: this.addForm.value1
          })
          .then(res => {
            if (res.data.code == 0) {
              this.$message({ message: "添加成功", type: "success" });
              this.addForm.value1 = "";
              this.addForm.value2 = "";
              this.allLocation();
              this.visible = false;
            } else {
              this.$message.error(res.data.message);
              this.visible = false;
            }
          })
          .catch(error => {});
      }
    },

    selectGrade() {
      this.gradeNo = this.addForm.value1;
    },

    selectTeachbuild() {
      this.teachbuildNo = this.addForm.value2;
    },
    // 添加新的教学区域
    addTeachArea() {
      this.queryTeachbuild();
      this.visible = true;
    },
    // 所有区域安排
    allLocation() {
      this.$axios
        .get("http://localhost:8080/locations/" + this.page)
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data;
            this.total = ret.total;
            this.locationData = ret.records;
          }
        })
        .catch(error => {});
    },

    // 获取教学楼信息
    queryTeachbuild() {
      this.$axios
        .get("http://localhost:8080/teachbuildinfo/list")
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data;
            this.teachbuild.splice(0, this.teachbuild.length);
            ret.map(v => {
              this.teachbuild.push({
                value: v.teachBuildNo,
                label: v.teachBuildName
              });
            });
          } else {
            alert(res.data.message);
          }
        })
        .catch(error => {});
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v;
      this.allLocation();
    },

    deleteById(index, row) {
      this.$axios
        .delete("http://localhost:8080/location/delete/" + row.id)
        .then(res => {
          if (res.data.code == 0) {
            this.allLocation();
          } else {
            alert(res.data.message);
          }
        })
        .catch(error => {});
    },
    editById(index, row) {}
  }
};
</script>

<style lang="less" scoped>
.button {
  margin-bottom: 5px;
  padding: 0;
  text-align: left;
  margin-bottom: 5px;
}

.footer-button {
  margin-top: 10px;
}
</style>