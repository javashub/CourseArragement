<template>
  <div>
    <div class="top-button">
      <el-upload
            class="add-button"
            ref="upload"
            accept=".doc,.docx,.pdf,.jpeg,.png,.jpg,"
            action="http://localhost:8080/upload"
            :on-remove="handleRemove"
            :on-error="handleError"
            :on-success="uploadSuccess"
            :disabled="importBtnDisabled"
            :file-list="fileList"
            :auto-upload="false"
            :limit="1"
          >
            <el-button style="margin-left: 10px;" slot="trigger" size="small" type="primary">选择文件<i class="el-icon-upload2 el-icon--right"></i></el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" :loading="loading" @click="submitUpload">上传到服务器<i class="el-icon-upload el-icon--right"></i></el-button>
            <!-- 下载模板 <a class="atag" href="http://localhost:8080/download">-->
            <el-button class="add-button" size="small" type="primary" @click="downloadTemplate()">
              下载模板
              <i class="el-icon-download el-icon--right"></i>
            </el-button>
            
          </el-upload>
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

.top-button {
  text-align: left;
  margin-bottom: 5px;
  margin-left: -10px;
}

</style>