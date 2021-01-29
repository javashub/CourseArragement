<template>
  <div>
    <div class="c-panel">
      <span>类别</span>
      <el-cascader
        clearable
        size="small"
        v-model="cascader"
        :options="options"
        :props="props"
        @change="handleChange"
      ></el-cascader>

      <el-button size="small" @click="handleUploadCourse" plain style="margin-left:10px;">上传课程</el-button>
      <el-button size="small" @click="handleUploadVideo" plain>上传视频</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%">
      <el-table-column type="index"></el-table-column>
      <el-table-column prop="onlineNo" label="课程号"></el-table-column>
      <el-table-column prop="onlineName" label="课程名"></el-table-column>
      <el-table-column prop="cover" label="封面" width="80">
        <template slot-scope="scope">
          <el-image fit="contain" style="width: 24px; height: 30px" :src="scope.row.cover"></el-image>
        </template>
      </el-table-column>

      <el-table-column prop="onlineCategoryName" label="类别"></el-table-column>
      <el-table-column prop="fromUserName" label="用户"></el-table-column>
      <el-table-column label="详情" width="100">
        <template slot-scope="scope">
          <el-button icon="el-icon-menu" @click="handleDetail(scope.row)" size="mini"></el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      layout="prev, pager, next"
      :total="total"
      hide-on-single-page
      @current-change="currentChange"
      :page-size="limit"
      :current-page.sync="page"
    ></el-pagination>

    <el-dialog title="视频列表" :visible.sync="visibleForm" width="80%" size="small">
      <el-table :data="videoData" style="width: 100%">
        <el-table-column type="index"></el-table-column>
        <el-table-column prop="videoName" label="视频名称"></el-table-column>
        <el-table-column prop="cover" label="封面" width="80">
          <template slot-scope="scope">
            <el-image fit="contain" style="width: 24px; height: 30px" :src="scope.row.cover"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="videoUrl" label="视频地址">
          <template slot-scope="scope">
              <el-link :href="scope.row.videoUrl" target="_blank">视频链接</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="fromUserName" label="发布者"></el-table-column>
        <el-table-column prop="createTime" label="发布时间"></el-table-column>
        <el-table-column label="详情" width="100">
          <template slot-scope="scope">
            <el-button
              icon="el-icon-delete"
              @click="handleVideoDelete(scope.row)"
              type="danger"
              size="mini"
            ></el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      title="上传课程"
      :visible.sync="uploadCourseVisibleForm"
      width="700px"
      style="text-align:left;"
    >
      <el-form :model="uploadCourseForm" label-position="right" label-width="100px">
        <el-form-item label="网课名" prop="onlineName">
          <el-input v-model="uploadCourseForm.onlineName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="课程简介" prop="description">
          <el-input v-model="uploadCourseForm.description" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item label="类别" prop="onlineCategoryId">
          <el-cascader
            clearable
            size="small"
            v-model="uploadCourseForm.cid"
            :options="options"
            :props="props"
            ref="cc"
          ></el-cascader>
        </el-form-item>
        <el-form-item label="上传封面">
          <el-upload
            class="upload-demo"
            action="http://localhost:8080/onlinevideo/upload"
            :on-success="handleCourseCoverSuccess"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item style="text-align:left;">
          <el-button type="primary" @click="saveCourse()" size="small">保存</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      title="上传视频"
      :visible.sync="uploadVideoVisibleForm"
      width="600px"
      style="text-align:left;"
    >
      <el-form :model="uploadVideoForm" label-position="left" label-width="80px">
        <el-form-item label="上传封面">
          <el-upload
            class="upload-demo"
            action="http://localhost:8080/onlinevideo/upload"
            :on-success="handleVideoCoverSuccess"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="上传视频">
          <el-upload
            class="upload-demo"
            action="http://localhost:8080/onlinevideo/upload"
            :on-success="handleVideoUploadSuccess"
          >
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="课程">
          <el-select v-model="uploadVideoForm.courseId" @focus="remoteMethod">
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="视频编号" prop="videoNo">
          <el-input v-model="uploadVideoForm.videoNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item style="text-align:left;">
          <el-button type="primary" @click="saveVideo()" size="small">提 交</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "OnlineCourse",
  data() {
    return {
      props: {
        value: "id",
        label: "categoryName"
      },
      options: [],
      tableData: [],
      page: 1,
      limit: 10,
      total: 0,
      visibleForm: false,
      cascader: [],
      videoData: [],
      uploadVideoForm: {},
      uploadCourseForm: {},
      uploadCourseVisibleForm: false,
      uploadVideoVisibleForm: false,
      detailRowId: {},
      courseOptions: [],
      loading: true,
      uu: {}
    };
  },
  mounted() {
    this.init();
    this.getUserType();
  },
  methods: {
    init() {
      this.options = [];
      this.$axios.get("http://localhost:8080/category/one").then(r => {
        let c = r.data.data;
        c.map(v => {
          this.$axios
            .get("http://localhost:8080/category/two/" + v.id)
            .then(rr => {
              this.options.push({
                id: v.id,
                categoryName: v.categoryName,
                children: rr.data.data,
                remark: v.remark,
                parentId: v.parentId
              });
            });
        });
      });
    },
    handleChange() {
      if (this.cascader.length <= 1) {
        this.tableData = [];
        this.total = 0;
        return;
      }
      let cid = this.cascader[1];
      this.$axios
        .get(
          "http://localhost:8080/onlinecourse/get-list/" +
            cid +
            "/" +
            this.page +
            "/" +
            this.limit
        )
        .then(r => {
          this.tableData = r.data.data.records;
          this.total = r.data.data.total;
        });
    },
    handleDetail(row) {
      this.detailRow = row;
      this.visibleForm = true;
      let courseId = row.id;
      console.log(row.id)
      this.$axios
        .get("http://localhost:8080/onlinevideo/get/" + courseId)
        .then(r => {
          this.videoData = r.data.data;
        });
    },
    currentChange() {
      this.handleChange();
    },
    handleUploadCourse() {
      this.uploadCourseForm = {};
      this.uploadCourseVisibleForm = true;
    },
    handleUploadVideo() {
      this.uploadVideoForm = {};
      this.uploadVideoVisibleForm = true;
    },
    handleVideoDelete(row) {
      this.$axios
        .delete("http://localhost:8080/onlinevideo/delete/" + row.id)
        .then(r => {
          if (r.data.code == 0) {
            this.$message({ message: "删除成功", type: "success" });
            this.handleDetail(this.detailRow);
          }
        });
    },
    saveCourse() {
      let data = this.uploadCourseForm;
      data.fromUserType = this.uu.fromUserType;
      data.fromUserId = this.uu.fromUserId;
      data.fromUserName = this.uu.fromUserName;

      let node = this.$refs["cc"].getCheckedNodes()[0]; //获得当前节点，
      data.onlineCategoryId = data.cid[1];
      data.onlineCategoryName = node.label;
      this.$axios
        .post("http://localhost:8080/onlinecourse/add", data)
        .then(r => {
          if (r.data.code == 0) {
            this.$message({ message: "上传成功", type: "success" });
            this.handleChange();
          }
        });
      this.uploadCourseVisibleForm = false;
    },
    saveVideo() {
      let data = this.uploadVideoForm;
      data.UserType = this.uu.fromUserType;
      data.id = this.uu.fromUserId;
      data.realname = this.uu.fromUserName;

      this.$axios
        .post("http://localhost:8080/onlinevideo/add", data)
        .then(r => {
          if (r.data.code == 0) {
            this.$message({ message: "上传成功", type: "success" });
            this.init();
          }
        });
      this.uploadVideoVisibleForm = false;
    },
    handleCourseCoverSuccess(res, file) {
      this.uploadCourseForm.cover = res.data.url;
    },
    handleVideoCoverSuccess(res, file) {
      this.uploadVideoForm.cover = res.data.url;
    },
    handleVideoUploadSuccess(res, file) {
      this.uploadVideoForm.videoUrl = res.data.url;
      this.uploadVideoForm.videoName = res.data.name;
    },
    remoteMethod() {
      let params = {
        limit: 100
      };
      this.$axios
        .get("http://localhost:8080/onlinecourse/list/" + 1, params)
        .then(r => {
          let records = r.data.data.records;
          this.courseOptions = [];
          records.map(v => {
            this.courseOptions.push({
              label: v.onlineName,
              value: v.id
            });
          });
          this.loading = false;
        });
    },
    // 获得上传的用户类型
    getUserType() {
      // 判断是管理员上传还是讲师上传
      let user = window.localStorage.getItem("admin");
      if (user != null) {
        // 管理员设置为1
        this.uu.fromUserType = 1;
        // 获得上传用户的id
        this.uu.fromUserId = JSON.parse(user).id;
        // 获得上传的用户名
        this.uu.fromUserName = JSON.parse(user).realname;
      } else {
        let user = window.localStorage.getItem("teacher");
        if (user != null) {
          // 讲师设置为2
          this.uu.fromUserType = 2;
          // 获得上传用户的id
          this.uu.fromUserId = JSON.parse(user).id;
          // 获得上传的用户名
          this.uu.fromUserName = JSON.parse(user).realname;
        }
      }
    }
  }
};
</script>

<style lang="less" scoped>
.c-panel {
  text-align: left;
  padding: 10px;
  span {
    font-size: 14px;
    color: #666;
    margin-right: 10px;
  }
}
</style>