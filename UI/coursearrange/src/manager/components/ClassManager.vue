<template>
  <section class="class-manager-shell">
    <div class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">Class Constraint Studio</div>
        <h1 class="hero-title">班级管理</h1>
        <p class="hero-description">
          班级基础信息、班主任和禁排时间现在统一在这一页维护。班级禁排会直接进入标准排课主链，不再依赖临时备注字段。
        </p>
      </div>
      <div class="hero-stats">
        <div class="stat-chip">
          <span class="stat-label">当前筛选</span>
          <strong>{{ activeGradeLabel }}</strong>
        </div>
        <div class="stat-chip">
          <span class="stat-label">当前页班级</span>
          <strong>{{ classInfoData.length }}</strong>
        </div>
      </div>
    </div>

    <el-card shadow="never" class="manager-card">
      <div class="toolbar-row">
        <el-select
          v-model="value1"
          clearable
          filterable
          placeholder="按年级筛选"
          class="toolbar-select"
          @change="queryClassByGrade"
          @clear="clearListener"
        >
          <el-option v-for="item in grade" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <div class="toolbar-actions">
          <el-button class="ghost-action" @click="allClassInfo">刷新</el-button>
          <el-button class="primary-action" type="primary" @click="addClass">新增班级</el-button>
        </div>
      </div>

      <el-table :data="classInfoData" stripe size="default" :highlight-current-row="true">
        <el-table-column prop="gradeName" label="年级" min-width="100" />
        <el-table-column prop="classNo" label="班级编号" min-width="120" />
        <el-table-column prop="className" label="班级名称" min-width="140" />
        <el-table-column prop="realname" label="班主任" min-width="110" />
        <el-table-column prop="num" label="人数" width="90" />
        <el-table-column label="禁排时间" min-width="160">
          <template #default="{ row }">
            <span class="slot-text">{{ row.forbiddenTimeSlotsText || '未配置' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="editById(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteById(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer-button">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="visible"
      :title="isEdit ? '编辑班级' : '新增班级'"
      width="640px"
      class="class-dialog"
      destroy-on-close
    >
      <el-form :model="addClassData" label-position="top" class="dialog-form">
        <div class="dialog-grid">
          <el-form-item label="年级">
            <el-select v-model="addClassData.gradeNo" placeholder="选择年级" clearable>
              <el-option v-for="item in grade" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="班主任">
            <el-select v-model="addClassData.teacherId" placeholder="选择班主任" filterable clearable>
              <el-option v-for="item in teacher" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="dialog-grid">
          <el-form-item label="班级编号">
            <el-input v-model="addClassData.classNo" autocomplete="off" placeholder="例如 2501" />
          </el-form-item>
          <el-form-item label="班级名称">
            <el-input v-model="addClassData.className" autocomplete="off" placeholder="例如 25级1班" />
          </el-form-item>
        </div>
        <div class="dialog-grid">
          <el-form-item label="学生人数">
            <el-input-number v-model="addClassData.num" :min="1" :max="300" controls-position="right" />
          </el-form-item>
          <div class="constraint-panel">
            <div class="constraint-title">班级禁排时间</div>
            <div class="constraint-text">
              使用两位时间编码，多个编码用逗号分隔，例如 `01,06,11`。固定课会前置拦截，自动排课也会避开这些时间片。
            </div>
          </div>
        </div>
        <el-form-item label="禁排时间编码">
          <el-input
            v-model="addClassData.forbiddenTimeSlotsText"
            autocomplete="off"
            placeholder="例如 01,06,11"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button class="primary-action" type="primary" @click="commit">提交</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script>
import {
  createLegacyClassInfo,
  deleteLegacyClassInfo,
  fetchLegacyClassInfoPage,
  fetchTeacherPage,
  updateLegacyClassInfo
} from "@/api/modules/base";

function normalizeForbiddenTimeSlots(rawValue) {
  return [...new Set(
    String(rawValue || "")
      .split(/[\s,，]+/)
      .filter(Boolean)
      .map((item) => item.trim())
      .filter((item) => /^\d{1,2}$/.test(item))
      .map((item) => item.padStart(2, "0"))
  )];
}

function createEmptyClassForm() {
  return {
    id: null,
    gradeNo: "",
    classNo: "",
    className: "",
    num: 40,
    teacherId: "",
    forbiddenTimeSlots: [],
    forbiddenTimeSlotsText: ""
  };
}

export default {
  name: "ClassManager",
  data() {
    return {
      classInfoData: [],
      addClassData: createEmptyClassForm(),
      visible: false,
      isEdit: false,
      page: 1,
      pageSize: 10,
      total: 0,
      value1: "",
      grade: [
        { value: "01", label: "高一" },
        { value: "02", label: "高二" },
        { value: "03", label: "高三" }
      ],
      teacher: []
    };
  },
  computed: {
    activeGradeLabel() {
      const current = this.grade.find((item) => item.value === this.value1);
      return current?.label || "全部年级";
    }
  },
  mounted() {
    this.allClassInfo();
  },
  methods: {
    async commit() {
      const forbiddenTimeSlots = normalizeForbiddenTimeSlots(this.addClassData.forbiddenTimeSlotsText);
      const payload = {
        ...this.addClassData,
        forbiddenTimeSlots,
        teacherId: this.addClassData.teacherId
      };
      try {
        if (this.isEdit && this.addClassData.id) {
          await updateLegacyClassInfo(this.addClassData.id, payload);
          this.$message({ message: "修改班级成功", type: "success" });
        } else {
          await createLegacyClassInfo(payload);
          this.$message({ message: "添加班级成功", type: "success" });
        }
        this.visible = false;
        this.addClassData = createEmptyClassForm();
        this.isEdit = false;
        this.allClassInfo();
      } catch (error) {
        this.$message.error(this.isEdit ? "修改班级失败" : "添加班级失败");
      }
    },

    async addClass() {
      await this.allTeacher();
      this.isEdit = false;
      this.addClassData = createEmptyClassForm();
      this.visible = true;
    },

    async allTeacher() {
      try {
        const response = await fetchTeacherPage(1, 100);
        const records = response.data?.records || [];
        this.teacher = records.map((item) => ({
          value: item.id,
          label: `${item.teacherNo || ""} ${item.realname || ""}`.trim()
        }));
      } catch (error) {
        this.teacher = [];
      }
    },

    handleCurrentChange(v) {
      this.page = v;
      if (this.value1) {
        this.queryClassByGrade();
        return;
      }
      this.allClassInfo();
    },

    async queryClassByGrade() {
      try {
        const response = await fetchLegacyClassInfoPage(this.page, this.pageSize, this.value1);
        const ret = response.data || {};
        this.classInfoData = ret.records || [];
        this.total = ret.total || 0;
      } catch (error) {
        this.classInfoData = [];
        this.total = 0;
      }
    },

    async allClassInfo() {
      try {
        const response = await fetchLegacyClassInfoPage(this.page, this.pageSize, this.value1);
        const ret = response.data || {};
        this.classInfoData = ret.records || [];
        this.total = ret.total || 0;
      } catch (error) {
        this.classInfoData = [];
        this.total = 0;
      }
    },

    clearListener() {
      this.value1 = "";
      this.page = 1;
      this.allClassInfo();
    },

    async deleteById(row) {
      try {
        await this.$confirm(`确认删除班级 ${row.classNo} 吗？`, "删除确认", { type: "warning" });
        await deleteLegacyClassInfo(row.id);
        this.$message({ message: "删除班级成功", type: "success" });
        this.allClassInfo();
      } catch (error) {
        if (error !== "cancel") {
          this.$message.error("删除班级失败");
        }
      }
    },

    async editById(row) {
      await this.allTeacher();
      this.isEdit = true;
      this.addClassData = {
        id: row.id,
        gradeNo: row.gradeNo || row.remark || "",
        classNo: row.classNo || "",
        className: row.className || "",
        num: Number(row.num || 40),
        teacherId: row.teacherId || row.teacher || "",
        forbiddenTimeSlots: row.forbiddenTimeSlots || [],
        forbiddenTimeSlotsText: row.forbiddenTimeSlotsText || ""
      };
      this.visible = true;
    }
  }
};
</script>

<style lang="less" scoped>
.class-manager-shell {
  display: grid;
  gap: 18px;
}

.hero-panel,
.manager-card {
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.1);
  background:
    radial-gradient(circle at top left, rgba(214, 171, 92, 0.16), transparent 36%),
    linear-gradient(135deg, rgba(244, 238, 226, 0.96), rgba(255, 255, 255, 0.98));
  box-shadow: 0 18px 44px rgba(33, 45, 78, 0.08);
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: #9a6b27;
}

.hero-title {
  margin: 8px 0 10px;
  font-size: 32px;
  font-weight: 700;
  color: #162033;
}

.hero-description {
  max-width: 680px;
  margin: 0;
  line-height: 1.7;
  color: #52607a;
}

.hero-stats {
  display: grid;
  gap: 12px;
  min-width: 200px;
}

.stat-chip {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(154, 107, 39, 0.18);
}

.stat-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #7c889f;
}

.manager-card {
  padding: 8px;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-select {
  width: 220px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.primary-action {
  border-radius: 999px;
}

.ghost-action {
  border-radius: 999px;
  border-color: rgba(22, 32, 51, 0.12);
}

.slot-text {
  color: #52607a;
}

.footer-button {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.dialog-form {
  display: grid;
  gap: 10px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.constraint-panel {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(20, 33, 61, 0.92);
  color: rgba(255, 255, 255, 0.88);
}

.constraint-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 700;
}

.constraint-text {
  line-height: 1.6;
  font-size: 12px;
}

@media (max-width: 960px) {
  .hero-panel,
  .toolbar-row,
  .dialog-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-stats,
  .toolbar-actions {
    width: 100%;
  }

  .toolbar-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    display: grid;
  }
}
</style>
