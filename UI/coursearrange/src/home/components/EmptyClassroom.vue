<template>
  <div>
    <el-alert
      title="空教室查询只基于标准课表占用结果；标准课表不可用时不会再回退旧 tb_course_plan。"
      type="info"
      :closable="false"
      show-icon
      class="query-alert"
    />
    <el-select
      v-model="teachbuildNo"
      placeholder="请选择教学楼"
      @change="selectTeachbuild"
      clearable
      class="select-t"
    >
      <el-option
        v-for="item in teachbuildOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>

    <el-table :data="classroomData" size="mini" :stripe="true" :highlight-current-row="true">
      <el-table-column label="序号" type="selection" />
      <el-table-column prop="classroomNo" label="教室编号" />
      <el-table-column prop="classroomName" label="教室名" />
      <el-table-column prop="teachbuildNo" label="所属教学楼" />
      <el-table-column prop="capacity" label="容量" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="operation" label="操作">
        <template slot-scope="scope">
          <el-button type="success" size="mini" @click="order(scope.$index, scope.row)">预约</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { fetchEmptyClassroomList, fetchLegacyTeachbuildList } from "@/api/modules/base";
import { getErrorMessage } from "@/utils/http";

export default {
  name: "EmptyClassroom",
  data() {
    return {
      teachbuildOptions: [],
      teachbuildNo: "",
      classroomData: [],
    };
  },

  mounted() {
    this.queryTeachbuild();
  },

  methods: {
    order() {
      setTimeout(() => {
        this.$message({ message: "预约成功", type: "success" });
      }, 1000);
    },

    selectTeachbuild() {
      if (!this.teachbuildNo) {
        this.classroomData = [];
        return;
      }
      this.queryByNo(this.teachbuildNo);
    },

    async queryByNo(teachbuildNo) {
      try {
        const response = await fetchEmptyClassroomList(teachbuildNo);
        this.classroomData = response.data || [];
      } catch (error) {
        this.classroomData = [];
        this.$message.error(getErrorMessage(error, "空教室查询失败"));
      }
    },

    async queryTeachbuild() {
      try {
        const response = await fetchLegacyTeachbuildList();
        const ret = response.data || [];
        this.teachbuildOptions = ret.map((item) => ({
          value: item.teachBuildNo,
          label: item.teachBuildName,
        }));
      } catch (error) {
        this.$message.error(getErrorMessage(error, "教学楼加载失败"));
      }
    },
  },
};
</script>

<style lang="less" scoped>
.select-t {
  text-align: left;
  float: left;
  margin-bottom: 5px;
}

.query-alert {
  margin-bottom: 12px;
}
</style>
