<template>
  <div>
    <div style="padding:10px 0 10px 0;text-align:left;">
      <el-button type="primary" @click="visibleForm = true" size="small">添加类别</el-button>
    </div>
    <el-table
    size="small"
      :data="tableData"
      style="width: 100%;margin-bottom: 20px;"
      row-key="id"
      border
      default-expand-all
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
    >
      <el-table-column type="index"></el-table-column>
      <!-- <el-table-column prop="id" label="id" sortable width="180"></el-table-column> -->
      <el-table-column prop="categoryName" label="名称" sortable></el-table-column>
      <!-- <el-table-column prop="remark" label="备注"></el-table-column> -->
      <el-table-column label="级别">
        <template scope="scope">{{scope.row.parentId == 0 ? '一级类别' : '二级类别'}}</template>
      </el-table-column>
      <el-table-column prop="categoryNo" label="编号" width="100"></el-table-column>
      <el-table-column prop="id" label="#" sortable width="100"></el-table-column>

      <el-table-column label="操作" width="150px">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="添加类别" :visible.sync="visibleForm" width="500px">
      <el-form :model="editFormData" label-position="left" label-width="80px">
        <el-form-item label="No" prop="categoryNo">
          <el-input v-model="editFormData.categoryNo" autocomplete="off" placeholder="类别编号"></el-input>
        </el-form-item>
        <el-form-item label="名称" prop="categoryName">
          <el-input v-model="editFormData.categoryName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="父级ID" prop="parentId">
          <el-input v-model="editFormData.parentId" autocomplete="off" placeholder="0为一级类别"></el-input>
        </el-form-item>
        <el-form-item style="text-align:left;">
          <el-button type="primary" @click="save()" size="small">提 交</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "OnlineCategory",
  data() {
    return {
      tableData: [],
      visibleForm: false,
      editFormData: {
        categoryNo: null,
        categoryName: null,
        parentId: 0
      }
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.tableData = []
      this.$axios.get("http://localhost:8080/category/one").then(r => {
        console.log(r)
        let c = r.data.data;
        c.map(v => {
          this.$axios
            .get("http://localhost:8080/category/two/" + v.id)
            .then(rr => {
              this.tableData.push({
                id: v.id,
                categoryName: v.categoryName,
                children: rr.data.data,
                categoryNo: v.categoryNo,
                parentId: v.parentId
              });
            });
        });
      });
    },


    save() {
      this.$axios({
        method: "post",
        url: "http://localhost:8080/category/add",
        params: {
          categoryNo: this.editFormData.categoryNo,
          categoryName: this.editFormData.categoryName,
          parentId: this.editFormData.parentId
        }
      }).then(r => {
        if (r.data.code == 0) {
          this.$message({ message: "添加成功", type: "success" });
          this.init();
        }
      });
      this.editFormData = {};
      this.visibleForm = false;
    },
    deleteById(v) {
      this.$axios
        .delete("http://localhost:8080/category/delete/" + v.id)
        .then(r => {
          if (r.data.code == 0) {
            this.$message({ message: "删除成功", type: "success" });
            this.init();
          }
        });
    }
  }
};
</script>

<style lang="less" scoped>
</style>