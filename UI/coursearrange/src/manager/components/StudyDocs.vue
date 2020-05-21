<template>
  <div>
    <h1>学习文档</h1>
    <div class="top-button">
      上传文档
    </div>
    <div class="table">
      <el-table :data="docData" size="mini">
        <el-table-column label="序号" type="selection"></el-table-column>
        <el-table-column prop="gradeNo" label="年级编号" ></el-table-column>
        <el-table-column prop="gradeName" label="年级" ></el-table-column>
        <el-table-column prop="teachbuildNo" label="教学楼编号" ></el-table-column>
        <el-table-column prop="teachBuildName" label="教学楼名称" ></el-table-column>

        <el-table-column prop="operation" label="操作" width="150px">
          <template slot-scope="scope">
            <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
            <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudyDocs',
  data() {
    return {
      docData: [],

    }
  },
  mounted() {

  },
  methods: {

  }
}
</script>

<style lang="less" scoped>

</style>