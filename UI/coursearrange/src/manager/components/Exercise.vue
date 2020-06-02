<template>
  <div>
    <div class="top-select">
      <el-select
        v-model="value"
        placeholder="类别"
        @change="queryExerciseByCategory"
        @clear="allExercise"
        clearable
      >
        <el-option
          v-for="item in category"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>
      <el-button class="add-button" type="primary" @click="visible2 = true">新增题目</el-button>
      <el-button class="add-button" type="primary" @click="visible1 = true">新增类型</el-button>
      <el-button class="add-button" type="primary" @click="putTrain">随机出题</el-button>
    </div>
    <div class="table">
      <el-table
        :data="exerciseData"
        size="mini"
        :stripe="true"
        :show-header="true"
        :highlight-current-row="true"
      >
        <el-table-column label="序号" type="selection"></el-table-column>
        <el-table-column prop="exerciseTitle" label="题目"></el-table-column>
        <el-table-column prop="multiselect" label="是否多选"></el-table-column>
        <el-table-column prop="answer" label="答案"></el-table-column>
        <el-table-column prop="optionA" label="答案A"></el-table-column>
        <el-table-column prop="optionB" label="答案B"></el-table-column>
        <el-table-column prop="optionC" label="答案C"></el-table-column>
        <el-table-column prop="optionD" label="答案D"></el-table-column>
        <el-table-column prop="optionE" label="答案E"></el-table-column>
        <el-table-column prop="optionF" label="答案F"></el-table-column>
        <el-table-column prop="fraction" label="分值"></el-table-column>

        <el-table-column prop="operation" label="操作" width="150px">
          <template slot-scope="scope">
            <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
            <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
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

      <!-- 新增类型 -->
      <el-dialog title="添加类别" :visible.sync="visible1">
        <el-form
          label-position="left"
          label-width="80px"
        >
          <el-form-item label="类别名称">
            <el-input v-model="categoryName" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="visible1 = false">取 消</el-button>
          <el-button type="primary" @click="commitAdd()">提 交</el-button>
        </div>
      </el-dialog>


      <!-- 新增题目 -->
      <el-dialog title="添加题目" :visible.sync="visible2">
        <el-form
          :model="exerciseAdd"
          label-position="left"
          label-width="80px"
          :rules="addExerciseRules"
        >
        <el-select v-model="exerciseAdd.categoryId" placeholder="选择类别" class="c-select">
          <el-option v-for="item in category" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
          <el-form-item label="班级">
            <el-input v-model="exerciseAdd.classNo" placeholder="可选" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="题目">
            <el-input v-model="exerciseAdd.exerciseTitle" placeholder="题目名称" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="是否多选">
            <el-input v-model="exerciseAdd.multiselect" placeholder="必填，0单选，1多选" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="答案">
            <el-input v-model="exerciseAdd.answer" placeholder="单选时输入一个，多选多个" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="选项A">
            <el-input v-model="exerciseAdd.optionA" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="选项B">
            <el-input v-model="exerciseAdd.optionB" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="选项C">
            <el-input v-model="exerciseAdd.optionC" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="选项D">
            <el-input v-model="exerciseAdd.optionD" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="选项E">
            <el-input v-model="exerciseAdd.optionE" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="选项F">
            <el-input v-model="exerciseAdd.optionF" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="分值">
            <el-input v-model="exerciseAdd.fraction" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="visible2 = false">取 消</el-button>
          <el-button type="primary" @click="commit()">提 交</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
export default {
  name: "Exercise",
  data() {
    return {
      exerciseData: [],
      page: 1,
      total: 0,
      pageSize: 10,
      exerciseAdd: {
        categoryId: '',
        classNo: '00000000',
        exerciseTitle: '',
        multiselect: 0,
        answer: '',
        optionA: '',
        optionB: '',
        optionC: '',
        optionD: '',
        optionE: '',
        optionF: '',
        fraction: ''
      },
      type: '',
      total: 0,
      page: 1,
      value: "",
      visible1: false,
      visible2: false,
      category: [
        {
          value: "",
          label: ""
        }
      ],
      addExerciseRules: {},
      categoryName: ''
    };
  },
  mounted() {
    this.queryCategory();
    this.allExercise();
  },
  methods: {

    // 随机出题
    putTrain() {

    },

    handleCurrentChange(v) {
      if (this.type == 1) {
        this.page = v
        this.allExercise()
      } else {
        this.page = v
        this.queryExerciseByCategory()
      }
    },

    handleSizeChange() {

    },

    // 提交添加题目
    commit() {
      this.$axios.post("http://localhost:8080/addexercise", this.exerciseAdd)
      .then(res => {
        if (res.data.code == 0) {
          this.visible2 = false;
          this.$message({message: "添加成功", type:"success"})
          // 添加成功后
          this.exerciseAdd = {}
          this.exerciseAdd.classNo = '00000000'
          this.allExercise()
        } else {
          alert(res.data.message)
        }
      })
      .catch(error => {

      })
    },
    // 提交添加类别
    commitAdd() {
      this.$axios
        .post("http://localhost:8080/exercise/add" + "?categoryName=" + this.categoryName)
        .then(res => {
          if (res.data.code == 0) {
            this.visible1 = false
            this.queryCategory()
            this.$message({message: "添加成功", type: "success"})
            this.categoryName = ''
          } else {
            alert(res.data.message)
          }
        })
        .catch(error => {});
    },

    editById(index, row) {},
    deleteById(index, row) {},
    // 查询类别
    queryCategory() {
      this.$axios
        .get("http://localhost:8080/exercise/categories")
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data;
            this.category.splice(0, this.category.length)
            ret.map(v => {
              this.category.push({
                value: v.id,
                label: v.categoryName
              });
            });
          }
        })
        .catch(error => {});
    },
    // 查询所有题目
    allExercise() {
      this.$axios
        .get("http://localhost:8080/exercise/" + this.page)
        .then(res => {
          if (res.data.code == 0) {
            this.type = 1
            let ret = res.data.data
            this.exerciseData = ret.records
            this.total = ret.total
          }
        })
        .catch(error => {});
    },
    // 分类查询
    queryExerciseByCategory() {
      this.$axios
        .get("http://localhost:8080/exercise/" + this.value + "/" + this.page)
        .then(res => {
          if (res.data.code == 0) {
            this.type = 2
            let ret = res.data.data
            this.exerciseData = ret.records
            this.total = ret.total
          }
        })
        .catch(error => {});
    }
  }
};
</script>

<style lang="less" scoped>
.top-select {
  margin-bottom: 5px;
  padding: 0;
  text-align: left;
  margin-bottom: 5px;
}

</style>