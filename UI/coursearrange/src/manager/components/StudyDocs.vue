<template>
  <div>
    <div class="top-button">
      <!-- 按照班级查询 -->
      <!-- <el-select>

      </el-select> -->
      
      <el-button type="primary" @click="addDocs()">新增</el-button>
    </div>

    <!-- 表格 -->
    <div class="table">
      <el-table :data="docData" size="mini" :stripe="true">
        <el-table-column label="序号" type="selection"></el-table-column>
        <el-table-column prop="docName" label="文件名"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="toClassNo" label="接收班级"></el-table-column>
        <el-table-column prop="fromUserName" label="发布者"></el-table-column>
        <el-table-column prop="expire" label="有效期(天)"></el-table-column>
        <el-table-column prop="createTime" label="上传时间"></el-table-column>
        <el-table-column prop="operation" label="操作" width="150px">
        <template slot-scope="scope">
          <!-- <el-button type="text" size="small" @click="previewById(scope.$index, scope.row)">预览</el-button> -->
          <el-button type="primary" size="small" @click="downloadById(scope.$index, scope.row)">下载</el-button>
          <el-button type="danger" size="small" @click="deleteById(scope.$index, scope.row)">删除</el-button>
        </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页插件 -->
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

    <!-- 添加文档 -->
    <el-dialog title="添加文档" :visible.sync="visible">
      <el-upload
      class="add-button"
        ref="upload"
        accept=".doc, .docx, .pdf, .jpeg, .png, .jpg"
        action="http://localhost:8080/uploaddocs"
        :on-success="uploadSuccess"
        :disabled="importBtnDisabled"
        :file-list="fileList"
        :auto-upload="true"
        :limit="1"
      >
        <el-button
          style="margin-left: 10px;"
          slot="trigger"
          size="small"
          type="primary"
        >
          上传
          <i class="el-icon-upload2 el-icon--right"></i>
        </el-button>
      </el-upload>
      <el-form :model="addDocData" label-position="left" label-width="80px">
        <el-form-item label="文件名" prop="fileName">
          <el-input v-model="addDocData.fileName" autocomplete="off" placeholder="输入文件名"></el-input>
        </el-form-item>
        <el-form-item label="班级编号" prop="toClassNo">
          <el-input v-model="addDocData.toClassNo" autocomplete="off" placeholder="推送至的班级"></el-input>
        </el-form-item>
        <el-form-item label="添加描述" prop="description">
          <el-input v-model="addDocData.description" autocomplete="off" placeholder="文档描述"></el-input>
        </el-form-item>
        <el-form-item label="有效期" prop="expire">
          <el-input v-model="addDocData.expire" autocomplete="off" placeholder="天数，只填写数字"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelAdd()">取 消</el-button>
        <el-button type="primary" @click="submitUpload()" :loading="loading">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "StudyDocs",
  data() {
    return {
      docData: [],
      fileList: [],
      addDocData: {
        fromUserId: '',
        fromUserType: '',
        fromUserName: '',
        docUrl: '',
        // 文件的实际名字
        docName: '' ,
        // 上传者自定义的名字
        fileName: ''
      },
      // 需要预览的文件url地址
      url: "",
      // docName: '',
      visible: false,
      importBtnDisabled: false,
      loading: false,
      page: 1,
      total: 10,
      pageSize: 10,
    };
  },

  mounted() {
    this.allDocs()
  },

  methods: {

    addDocs() {
      this.visible = true
      
    },

    

    // 获得上传的用户类型
    getUserType() {
      // 判断是管理员上传还是讲师上传
      let user = window.localStorage.getItem('admin')
      if (user != null) {
        // 管理员设置为1
        this.addDocData.fromUserType = 1
        // 获得上传用户的id
        this.addDocData.fromUserId = (JSON.parse(user)).id
        // 获得上传的用户名
        this.addDocData.fromUserName = (JSON.parse(user)).realname
      } else {
        let user = window.localStorage.getItem('teacher')
        if (user != null) {
          // 讲师设置为2
          this.addDocData.fromUserType = 2
          // 获得上传用户的id
          this.addDocData.fromUserId = (JSON.parse(user)).id
          // 获得上传的用户名
          this.addDocData.fromUserName = (JSON.parse(user)).realname
        }
      }
    },

    // 提交上传
    submitUpload() {
      this.loading = true
      // 获取操作的用户信息
      this.getUserType()
      // this.$refs.upload.submit()
      // 提交input信息
      this.$axios.post("http://localhost:8080/adddocs", this.addDocData)
      .then(res => {
        if (res.data.code == 0) {
          this.addDocData = {}
          this.visible = false
          this.$message({message: "添加文档成功", type: "success"})
          this.loading = false
          this.allDocs()
        } else {
          this.$message.error(res.data.message)
        }
      })
      .catch(error => {
        
      })
    },

    // 所有文档
    allDocs() {
      this.$axios.get("http://localhost:8080/docs/" + this.page)
      .then(res => {
        if (res.data.code == 0) {
          let ret = res.data.data
          this.docData = ret.records
          this.total = ret.total
          
        } else {
          this.$message.error(res.data.message)
        }
      })
      .catch(error => {
        
      })
    },

    // 下载
    downloadById(index, row) {
      console.log(row.docUrl)
      window.location.href = row.docUrl
    },

    deleteById(index, row) {
      alert(row.id)
      this.$axios.delete("http://localhost:8080/deletedoc?id=" + row.id)
      .then(res => {
        if (res.data.code == 0) {
          // 删除成功
          console.log(res)
          this.allDocs()
          this.$message({message: "删除成功", type: "success"})
        } else {
          this.$message.error(res.data.message)
        }
      })
      .catch()
    },

    // 取消
    cancelAdd() {
      this.visible = false
      this.addDocData = {}
    },

    handleSizeChange(v) {
      this.page = v
      this.allDocs()
    },

    handleCurrentChange() {

    },

    uploadSuccess(response, file, fileList) {      
      this.importBtnDisabled = false
      this.addDocData.docName = response.data.name
      this.addDocData.docUrl = response.data.url
      
    }
  }
};
</script>

<style lang="less" scoped>

.top-button {
  text-align: left;
  margin-bottom: 5px;
}

.add-button {
  text-align: left;
  margin-left: 70px;
  margin-bottom: 5px;
}
</style>