<template>
  <section class="resource-shell">
    <div class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">Base Resource Studio</div>
        <h1 class="hero-title">基础数据</h1>
        <p class="hero-description">
          先把教师、学生、课程、教室这些基础资源做成统一后台入口。当前阶段优先保证联调闭环，后续再把接口逐步升级为新规范。
        </p>
      </div>
      <div class="hero-stats">
        <div class="stat-chip">
          <span class="stat-label">当前页签</span>
          <strong>{{ activeTabLabel }}</strong>
        </div>
        <div class="stat-chip">
          <span class="stat-label">联调状态</span>
          <strong>可新增 / 可编辑 / 可删除</strong>
        </div>
      </div>
    </div>

    <el-card shadow="never" class="resource-card">
      <el-tabs v-model="activeTab" class="resource-tabs">
        <el-tab-pane label="教师管理" name="teacher">
          <div class="toolbar-row">
            <el-input
              v-model="teacherState.keyword"
              clearable
              placeholder="搜索教师姓名，例如 张老师"
              @keyup.enter="loadTeachers(true)"
              @clear="loadTeachers(true)"
            />
            <el-select
              v-model="teacherState.statusFilter"
              clearable
              placeholder="筛选状态"
              class="toolbar-select"
              @change="applyTeacherFilter"
              @clear="applyTeacherFilter"
            >
              <el-option label="正常" :value="0" />
              <el-option label="封禁" :value="1" />
            </el-select>
            <div class="toolbar-actions">
              <el-upload class="excel-upload" :show-file-list="false" :auto-upload="false" :before-upload="handleTeacherImport" accept=".xls,.xlsx">
                <el-button class="ghost-action">导入教师</el-button>
              </el-upload>
              <el-button class="ghost-action" @click="handleTeacherTemplateDownload">模板</el-button>
              <el-button class="ghost-action" @click="handleTeacherExport">导出教师</el-button>
              <el-button class="ghost-action" @click="resetTeacherSearch">重置</el-button>
              <el-button class="primary-action" type="primary" @click="openTeacherDialog()">新增教师</el-button>
            </div>
          </div>

          <el-table :data="teacherState.displayRecords" stripe v-loading="teacherState.loading">
            <el-table-column prop="teacherNo" label="教师编号" min-width="130" />
            <el-table-column prop="realname" label="姓名" min-width="110" />
            <el-table-column prop="teach" label="教授科目" min-width="140" />
            <el-table-column prop="jobtitle" label="职称" min-width="120" />
            <el-table-column prop="telephone" label="联系电话" min-width="140" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'warning'" effect="plain">
                  {{ row.status === 0 ? '正常' : '封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTeacherDialog(row)">编辑</el-button>
                <el-button link type="warning" @click="toggleTeacher(row)">
                  {{ row.status === 0 ? '封禁' : '解封' }}
                </el-button>
                <el-button link type="danger" @click="removeTeacher(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :current-page="teacherState.pageNum"
              :page-size="teacherState.pageSize"
              :total="teacherState.total"
              @current-change="handleTeacherPageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="学生管理" name="student">
          <div class="toolbar-row">
            <el-input
              v-model="studentState.keyword"
              clearable
              placeholder="搜索学生姓名，例如 李同学"
              @keyup.enter="loadStudents(true)"
              @clear="loadStudents(true)"
            />
            <el-select
              v-model="studentState.statusFilter"
              clearable
              placeholder="筛选状态"
              class="toolbar-select"
              @change="applyStudentFilter"
              @clear="applyStudentFilter"
            >
              <el-option label="正常" :value="0" />
              <el-option label="封禁" :value="1" />
            </el-select>
            <div class="toolbar-actions">
              <el-upload class="excel-upload" :show-file-list="false" :auto-upload="false" :before-upload="handleStudentImport" accept=".xls,.xlsx">
                <el-button class="ghost-action">导入学生</el-button>
              </el-upload>
              <el-button class="ghost-action" @click="handleStudentTemplateDownload">模板</el-button>
              <el-button class="ghost-action" @click="handleStudentExport">导出学生</el-button>
              <el-button class="ghost-action" @click="resetStudentSearch">重置</el-button>
              <el-button class="primary-action" type="primary" @click="openStudentDialog()">新增学生</el-button>
            </div>
          </div>

          <el-table :data="studentState.displayRecords" stripe v-loading="studentState.loading">
            <el-table-column prop="studentNo" label="学号" min-width="130" />
            <el-table-column prop="realname" label="姓名" min-width="110" />
            <el-table-column prop="grade" label="年级" min-width="120" />
            <el-table-column prop="classNo" label="班级" min-width="120" />
            <el-table-column prop="telephone" label="联系电话" min-width="140" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'warning'" effect="plain">
                  {{ row.status === 0 ? '正常' : '封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openStudentDialog(row)">编辑</el-button>
                <el-button link type="warning" @click="toggleStudent(row)">
                  {{ row.status === 0 ? '封禁' : '解封' }}
                </el-button>
                <el-button link type="danger" @click="removeStudent(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :current-page="studentState.pageNum"
              :page-size="studentState.pageSize"
              :total="studentState.total"
              @current-change="handleStudentPageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="课程管理" name="course">
          <div class="toolbar-row">
            <el-input
              v-model="courseState.keyword"
              clearable
              placeholder="搜索课程名称，例如 高等数学"
              @keyup.enter="loadCourses(true)"
              @clear="loadCourses(true)"
            />
            <el-select
              v-model="courseState.statusFilter"
              clearable
              placeholder="筛选状态"
              class="toolbar-select"
              @change="applyCourseFilter"
              @clear="applyCourseFilter"
            >
              <el-option label="正常" :value="0" />
              <el-option label="停用" :value="1" />
            </el-select>
            <div class="toolbar-actions">
              <el-upload class="excel-upload" :show-file-list="false" :auto-upload="false" :before-upload="handleCourseImport" accept=".xls,.xlsx">
                <el-button class="ghost-action">导入课程</el-button>
              </el-upload>
              <el-button class="ghost-action" @click="handleCourseTemplateDownload">模板</el-button>
              <el-button class="ghost-action" @click="handleCourseExport">导出课程</el-button>
              <el-button class="ghost-action" @click="resetCourseSearch">重置</el-button>
              <el-button class="primary-action" type="primary" @click="openCourseDialog()">新增课程</el-button>
            </div>
          </div>

          <el-table :data="courseState.displayRecords" stripe v-loading="courseState.loading">
            <el-table-column prop="courseNo" label="课程编号" min-width="130" />
            <el-table-column prop="courseName" label="课程名称" min-width="160" />
            <el-table-column prop="courseAttr" label="课程属性" min-width="140" />
            <el-table-column prop="publisher" label="出版社" min-width="160" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'info'" effect="plain">
                  {{ row.status === 0 ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openCourseDialog(row)">编辑</el-button>
                <el-button link type="warning" @click="toggleCourse(row)">
                  {{ row.status === 0 ? '停用' : '启用' }}
                </el-button>
                <el-button link type="danger" @click="removeCourse(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :current-page="courseState.pageNum"
              :page-size="courseState.pageSize"
              :total="courseState.total"
              @current-change="handleCoursePageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="教室管理" name="classroom">
          <div class="toolbar-row">
            <el-input
              v-model="classroomState.keyword"
              clearable
              placeholder="搜索教室编号或名称，例如 A101、第一多媒体教室"
              @clear="applyClassroomFilter"
              @input="applyClassroomFilter"
            />
            <el-select
              v-model="classroomState.teachbuildFilter"
              clearable
              filterable
              placeholder="筛选教学楼"
              class="toolbar-select"
              @change="applyClassroomFilter"
              @clear="applyClassroomFilter"
            >
              <el-option
                v-for="item in teachbuildOptions"
                :key="item.id"
                :label="`${item.teachBuildNo} ${item.teachBuildName}`"
                :value="item.teachBuildNo"
              />
            </el-select>
            <div class="toolbar-actions">
              <el-upload class="excel-upload" :show-file-list="false" :auto-upload="false" :before-upload="handleClassroomImport" accept=".xls,.xlsx">
                <el-button class="ghost-action">导入教室</el-button>
              </el-upload>
              <el-button class="ghost-action" @click="handleClassroomTemplateDownload">教室模板</el-button>
              <el-button class="ghost-action" @click="handleClassroomExport">导出教室</el-button>
              <el-upload class="excel-upload" :show-file-list="false" :auto-upload="false" :before-upload="handleTeachbuildImport" accept=".xls,.xlsx">
                <el-button class="ghost-action">导入教学楼</el-button>
              </el-upload>
              <el-button class="ghost-action" @click="handleTeachbuildTemplateDownload">教学楼模板</el-button>
              <el-button class="ghost-action" @click="handleTeachbuildExport">导出教学楼</el-button>
              <el-button class="ghost-action" @click="resetClassroomSearch">重置</el-button>
              <el-button class="primary-action" type="primary" @click="openClassroomDialog()">新增教室</el-button>
            </div>
          </div>

          <el-table :data="classroomState.displayRecords" stripe v-loading="classroomState.loading">
            <el-table-column prop="classroomNo" label="教室编号" min-width="130" />
            <el-table-column prop="classroomName" label="教室名称" min-width="150" />
            <el-table-column prop="teachbuildNo" label="教学楼编号" min-width="120" />
            <el-table-column prop="capacity" label="容量" width="90" />
            <el-table-column prop="attr" label="教室属性" min-width="120" />
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openClassroomDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="removeClassroom(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :current-page="classroomState.pageNum"
              :page-size="classroomState.pageSize"
              :total="classroomState.total"
              @current-change="handleClassroomPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="teacherDialogVisible" :title="teacherForm.id ? '编辑教师' : '新增教师'" width="680px">
      <el-form :model="teacherForm" label-position="top">
        <div class="form-grid">
          <el-form-item label="教师编号">
            <div class="inline-field">
              <el-input v-model="teacherForm.teacherNo" placeholder="例如 T2026001" />
              <el-button @click="generateTeacherNo">生成编号</el-button>
            </div>
          </el-form-item>
          <el-form-item label="登录账号">
            <el-input v-model="teacherForm.username" placeholder="例如 zhangsan" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="真实姓名">
            <el-input v-model="teacherForm.realname" placeholder="例如 张老师" />
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="teacherForm.jobtitle" placeholder="例如 讲师、副教授" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="教授科目">
            <el-input v-model="teacherForm.teach" placeholder="例如 高等数学、英语" />
          </el-form-item>
          <el-form-item label="年龄">
            <el-input-number v-model="teacherForm.age" :min="18" :max="90" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="联系电话">
            <el-input v-model="teacherForm.telephone" placeholder="例如 13800000000" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="teacherForm.email" placeholder="例如 teacher@school.edu.cn" />
          </el-form-item>
        </div>
        <el-form-item label="联系地址">
          <el-input v-model="teacherForm.address" placeholder="例如 主校区教师公寓 3 栋 302" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teacherDialogVisible = false">取消</el-button>
        <el-button class="primary-action" type="primary" :loading="teacherSubmitting" @click="submitTeacher">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="studentDialogVisible" :title="studentForm.id ? '编辑学生' : '新增学生'" width="700px">
      <el-form :model="studentForm" label-position="top">
        <div class="form-grid">
          <el-form-item label="学号">
            <div class="inline-field">
              <el-input v-model="studentForm.studentNo" placeholder="例如 2026020001" />
              <el-button @click="generateStudentNo">生成学号</el-button>
            </div>
          </el-form-item>
          <el-form-item label="登录账号">
            <el-input v-model="studentForm.username" placeholder="例如 lisi" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="真实姓名">
            <el-input v-model="studentForm.realname" placeholder="例如 李同学" />
          </el-form-item>
          <el-form-item label="年级">
            <el-input v-model="studentForm.grade" placeholder="例如 02、2024级、初一" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="班级">
            <el-input v-model="studentForm.classNo" placeholder="例如 2024-1 班" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="studentForm.status">
              <el-radio :value="0">正常</el-radio>
              <el-radio :value="1">封禁</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <div v-if="!studentForm.id" class="form-grid">
          <el-form-item label="初始密码">
            <el-input v-model="studentForm.password" show-password placeholder="例如 123456" />
          </el-form-item>
          <div />
        </div>
        <div class="form-grid">
          <el-form-item label="联系电话">
            <el-input v-model="studentForm.telephone" placeholder="例如 13800000000" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="studentForm.email" placeholder="例如 student@school.edu.cn" />
          </el-form-item>
        </div>
        <el-form-item label="联系地址">
          <el-input v-model="studentForm.address" placeholder="例如 主校区宿舍 5 栋 402" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="studentDialogVisible = false">取消</el-button>
        <el-button class="primary-action" type="primary" :loading="studentSubmitting" @click="submitStudent">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="courseDialogVisible" :title="courseForm.id ? '编辑课程' : '新增课程'" width="680px">
      <el-form :model="courseForm" label-position="top">
        <div class="form-grid">
          <el-form-item label="课程编号">
            <div class="inline-field">
              <el-input v-model="courseForm.courseNo" placeholder="例如 10001" />
              <el-button @click="generateCourseNo">生成编号</el-button>
            </div>
          </el-form-item>
          <el-form-item label="课程名称">
            <el-input v-model="courseForm.courseName" placeholder="例如 高等数学" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="课程属性">
            <el-input v-model="courseForm.courseAttr" placeholder="例如 必修、选修、实验课" />
          </el-form-item>
          <el-form-item label="出版社">
            <el-input v-model="courseForm.publisher" placeholder="例如 高等教育出版社" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="状态">
            <el-radio-group v-model="courseForm.status">
              <el-radio :value="0">正常</el-radio>
              <el-radio :value="1">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="优先级">
            <el-input-number v-model="courseForm.piority" :min="0" :max="99" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="courseForm.remark" type="textarea" :rows="3" placeholder="例如 大一上学期核心课程" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button class="primary-action" type="primary" :loading="courseSubmitting" @click="submitCourse">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="classroomDialogVisible" :title="classroomForm.id ? '编辑教室' : '新增教室'" width="680px">
      <el-form :model="classroomForm" label-position="top">
        <div class="form-grid">
          <el-form-item label="教室编号">
            <el-input v-model="classroomForm.classroomNo" placeholder="例如 A101、08-302" />
          </el-form-item>
          <el-form-item label="教室名称">
            <el-input v-model="classroomForm.classroomName" placeholder="例如 第一多媒体教室" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="教学楼编号">
            <el-select v-model="classroomForm.teachbuildNo" clearable filterable placeholder="例如 08 实验楼">
              <el-option
                v-for="item in teachbuildOptions"
                :key="item.id"
                :label="`${item.teachBuildNo} ${item.teachBuildName}`"
                :value="item.teachBuildNo"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="容量">
            <el-input-number v-model="classroomForm.capacity" :min="0" :max="500" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="教室属性">
            <el-input v-model="classroomForm.attr" placeholder="例如 普通教室、实验室、机房" />
          </el-form-item>
          <div />
        </div>
        <el-form-item label="备注">
          <el-input v-model="classroomForm.remark" type="textarea" :rows="3" placeholder="例如 支持投影和录播设备" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classroomDialogVisible = false">取消</el-button>
        <el-button class="primary-action" type="primary" :loading="classroomSubmitting" @click="submitClassroom">
          保存
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createClassroom,
  createCourse,
  createStudent,
  createTeacher,
  deleteClassroom,
  deleteCourse,
  deleteStudent,
  deleteTeacher,
  downloadClassroomTemplate,
  downloadCourseTemplate,
  downloadStudentTemplate,
  downloadTeachbuildTemplate,
  downloadTeacherTemplate,
  exportClassroomExcel,
  exportCourseExcel,
  exportStudentExcel,
  exportTeachbuildExcel,
  exportTeacherExcel,
  fetchClassroomDetail,
  fetchClassroomPage,
  fetchCoursePage,
  fetchNextCourseNo,
  fetchNextStudentNo,
  fetchNextTeacherNo,
  fetchStudentDetail,
  fetchStudentPage,
  fetchTeachbuildList,
  fetchTeacherDetail,
  fetchTeacherPage,
  importClassroomExcel,
  importCourseExcel,
  importStudentExcel,
  importTeachbuildExcel,
  importTeacherExcel,
  searchCoursePage,
  searchStudentPage,
  searchTeacherPage,
  toggleTeacherStatus,
  updateClassroom,
  updateCourse,
  updateStudent,
  updateTeacher
} from '@/api/modules/base';

const activeTab = ref('teacher');
const teacherDialogVisible = ref(false);
const studentDialogVisible = ref(false);
const courseDialogVisible = ref(false);
const classroomDialogVisible = ref(false);

const teacherSubmitting = ref(false);
const studentSubmitting = ref(false);
const courseSubmitting = ref(false);
const classroomSubmitting = ref(false);

const teachbuildOptions = ref([]);

const teacherState = reactive(createPageState());
const studentState = reactive(createPageState());
const courseState = reactive(createPageState());
const classroomState = reactive(createPageState());

const teacherForm = ref(createTeacherForm());
const studentForm = ref(createStudentForm());
const courseForm = ref(createCourseForm());
const classroomForm = ref(createClassroomForm());

function createPageState() {
  return {
    loading: false,
    records: [],
    displayRecords: [],
    total: 0,
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    statusFilter: '',
    teachbuildFilter: ''
  };
}

function createTeacherForm() {
  return {
    id: null,
    teacherNo: '',
    username: '',
    realname: '',
    jobtitle: '',
    teach: '',
    telephone: '',
    email: '',
    address: '',
    age: 30
  };
}

function createStudentForm() {
  return {
    id: null,
    studentNo: '',
    username: '',
    password: '123456',
    realname: '',
    grade: '',
    classNo: '',
    telephone: '',
    email: '',
    address: '',
    status: 0
  };
}

function createCourseForm() {
  return {
    id: null,
    courseNo: '',
    courseName: '',
    courseAttr: '',
    publisher: '',
    status: 0,
    piority: 0,
    remark: ''
  };
}

function createClassroomForm() {
  return {
    id: null,
    classroomNo: '',
    classroomName: '',
    teachbuildNo: '',
    capacity: 40,
    attr: '',
    remark: ''
  };
}

function getImportResult(payload) {
  return payload?.data || payload?.response?.data?.data || null;
}

function buildImportSummary(resourceName, payload, fallbackMessage) {
  const result = getImportResult(payload);
  if (!result) {
    return fallbackMessage;
  }
  const totalCount = result.totalCount ?? 0;
  const successCount = result.successCount ?? 0;
  const failedCount = result.failedCount ?? 0;
  return `${resourceName}导入完成，合计 ${totalCount} 条，成功 ${successCount} 条，失败 ${failedCount} 条`;
}

async function showImportErrors(title, payload) {
  const result = getImportResult(payload);
  const errors = Array.isArray(result?.errors) ? result.errors : [];
  if (!errors.length) {
    return;
  }
  const previewErrors = errors.slice(0, 12).join('<br/>');
  const appendix = errors.length > 12 ? `<br/><br/>仅展示前 12 条，共 ${errors.length} 条。` : '';
  await ElMessageBox.alert(`${previewErrors}${appendix}`, title, {
    type: 'error',
    dangerouslyUseHTMLString: true,
    confirmButtonText: '我知道了'
  });
}

const activeTabLabel = computed(() => {
  const map = {
    teacher: '教师管理',
    student: '学生管理',
    course: '课程管理',
    classroom: '教室管理'
  };
  return map[activeTab.value] || '基础数据';
});

function applyPageData(state, pageData) {
  state.records = pageData?.records || [];
  state.total = pageData?.total || 0;
  state.displayRecords = [...state.records];
}

function applyTeacherFilter() {
  teacherState.displayRecords = teacherState.records.filter((item) => {
    if (teacherState.statusFilter === '' || teacherState.statusFilter === null || teacherState.statusFilter === undefined) {
      return true;
    }
    return item.status === teacherState.statusFilter;
  });
}

function applyStudentFilter() {
  studentState.displayRecords = studentState.records.filter((item) => {
    if (studentState.statusFilter === '' || studentState.statusFilter === null || studentState.statusFilter === undefined) {
      return true;
    }
    return item.status === studentState.statusFilter;
  });
}

function applyCourseFilter() {
  courseState.displayRecords = courseState.records.filter((item) => {
    if (courseState.statusFilter === '' || courseState.statusFilter === null || courseState.statusFilter === undefined) {
      return true;
    }
    return item.status === courseState.statusFilter;
  });
}

function applyClassroomFilter() {
  const keyword = classroomState.keyword.trim().toLowerCase();
  classroomState.displayRecords = classroomState.records.filter((item) => {
    const matchKeyword = !keyword
      || [item.classroomNo, item.classroomName, item.remark].filter(Boolean).join(' ').toLowerCase().includes(keyword);
    const matchTeachbuild = !classroomState.teachbuildFilter || item.teachbuildNo === classroomState.teachbuildFilter;
    return matchKeyword && matchTeachbuild;
  });
}

async function loadTeachers(resetPage = false) {
  if (resetPage) {
    teacherState.pageNum = 1;
  }
  teacherState.loading = true;
  try {
    const response = teacherState.keyword
      ? await searchTeacherPage(teacherState.keyword, teacherState.pageNum, teacherState.pageSize)
      : await fetchTeacherPage(teacherState.pageNum, teacherState.pageSize);
    applyPageData(teacherState, response.data);
    applyTeacherFilter();
  } finally {
    teacherState.loading = false;
  }
}

async function loadStudents(resetPage = false) {
  if (resetPage) {
    studentState.pageNum = 1;
  }
  studentState.loading = true;
  try {
    const response = studentState.keyword
      ? await searchStudentPage(studentState.keyword, studentState.pageNum, studentState.pageSize)
      : await fetchStudentPage(studentState.pageNum, studentState.pageSize);
    applyPageData(studentState, response.data);
    applyStudentFilter();
  } finally {
    studentState.loading = false;
  }
}

async function loadCourses(resetPage = false) {
  if (resetPage) {
    courseState.pageNum = 1;
  }
  courseState.loading = true;
  try {
    const response = courseState.keyword
      ? await searchCoursePage(courseState.keyword, courseState.pageNum, courseState.pageSize)
      : await fetchCoursePage(courseState.pageNum, courseState.pageSize);
    applyPageData(courseState, response.data);
    applyCourseFilter();
  } finally {
    courseState.loading = false;
  }
}

async function loadClassrooms() {
  classroomState.loading = true;
  try {
    const response = await fetchClassroomPage(classroomState.pageNum, classroomState.pageSize);
    applyPageData(classroomState, response.data);
    applyClassroomFilter();
  } finally {
    classroomState.loading = false;
  }
}

function resetTeacherSearch() {
  teacherState.keyword = '';
  teacherState.statusFilter = '';
  loadTeachers(true);
}

function resetStudentSearch() {
  studentState.keyword = '';
  studentState.statusFilter = '';
  loadStudents(true);
}

function resetCourseSearch() {
  courseState.keyword = '';
  courseState.statusFilter = '';
  loadCourses(true);
}

function resetClassroomSearch() {
  classroomState.keyword = '';
  classroomState.teachbuildFilter = '';
  loadClassrooms();
}

function handleTeacherPageChange(page) {
  teacherState.pageNum = page;
  loadTeachers();
}

function handleStudentPageChange(page) {
  studentState.pageNum = page;
  loadStudents();
}

function handleCoursePageChange(page) {
  courseState.pageNum = page;
  loadCourses();
}

function handleClassroomPageChange(page) {
  classroomState.pageNum = page;
  loadClassrooms();
}

async function openTeacherDialog(row) {
  if (!row) {
    teacherForm.value = createTeacherForm();
    teacherDialogVisible.value = true;
    return;
  }
  const response = await fetchTeacherDetail(row.id);
  teacherForm.value = {
    ...createTeacherForm(),
    ...(response.data || {})
  };
  teacherDialogVisible.value = true;
}

async function submitTeacher() {
  teacherSubmitting.value = true;
  try {
    if (teacherForm.value.id) {
      await updateTeacher(teacherForm.value);
      ElMessage.success('教师信息更新成功');
    } else {
      await createTeacher(teacherForm.value);
      ElMessage.success('教师创建成功，默认密码为 123456');
    }
    teacherDialogVisible.value = false;
    await loadTeachers();
  } finally {
    teacherSubmitting.value = false;
  }
}

async function generateTeacherNo() {
  const response = await fetchNextTeacherNo();
  const nextNo = response.data ? String(Number(response.data) + 1) : '';
  teacherForm.value.teacherNo = nextNo;
}

async function toggleTeacher(row) {
  await toggleTeacherStatus(row.id);
  ElMessage.success(row.status === 0 ? '教师账号已封禁' : '教师账号已解封');
  await loadTeachers();
}

async function removeTeacher(row) {
  await ElMessageBox.confirm(`确认删除教师“${row.realname}”吗？`, '删除确认', { type: 'warning' });
  await deleteTeacher(row.id);
  ElMessage.success('教师删除成功');
  await loadTeachers();
}

async function handleTeacherExport() {
  try {
    await exportTeacherExcel({
      keyword: teacherState.keyword,
      status: teacherState.statusFilter
    });
    ElMessage.success('教师数据导出中');
  } catch (error) {
    ElMessage.error('教师数据导出失败');
  }
}

async function handleTeacherTemplateDownload() {
  try {
    await downloadTeacherTemplate();
  } catch (error) {
    ElMessage.error('教师模板下载失败');
  }
}

async function handleTeacherImport(file) {
  try {
    const response = await importTeacherExcel(file);
    ElMessage.success(buildImportSummary('教师', response, '教师数据导入成功'));
    await loadTeachers(true);
  } catch (error) {
    await showImportErrors('教师导入失败明细', error);
    return false;
  }
  return false;
}

async function openStudentDialog(row) {
  if (!row) {
    studentForm.value = createStudentForm();
    studentDialogVisible.value = true;
    return;
  }
  const response = await fetchStudentDetail(row.id);
  studentForm.value = {
    ...createStudentForm(),
    password: '',
    ...(response.data || {})
  };
  studentDialogVisible.value = true;
}

async function generateStudentNo() {
  if (!studentForm.value.grade) {
    ElMessage.warning('请先填写年级，再生成学号');
    return;
  }
  const response = await fetchNextStudentNo(studentForm.value.grade);
  studentForm.value.studentNo = response.data || '';
}

async function submitStudent() {
  studentSubmitting.value = true;
  try {
    if (studentForm.value.id) {
      await updateStudent(studentForm.value.id, studentForm.value);
      ElMessage.success('学生信息更新成功');
    } else {
      await createStudent(studentForm.value);
      ElMessage.success('学生创建成功');
    }
    studentDialogVisible.value = false;
    await loadStudents();
  } finally {
    studentSubmitting.value = false;
  }
}

async function removeStudent(row) {
  await ElMessageBox.confirm(`确认删除学生“${row.realname}”吗？`, '删除确认', { type: 'warning' });
  await deleteStudent(row.id);
  ElMessage.success('学生删除成功');
  await loadStudents();
}

async function handleStudentExport() {
  try {
    await exportStudentExcel({
      keyword: studentState.keyword,
      status: studentState.statusFilter
    });
    ElMessage.success('学生数据导出中');
  } catch (error) {
    ElMessage.error('学生数据导出失败');
  }
}

async function handleStudentTemplateDownload() {
  try {
    await downloadStudentTemplate();
  } catch (error) {
    ElMessage.error('学生模板下载失败');
  }
}

async function handleStudentImport(file) {
  try {
    const response = await importStudentExcel(file);
    ElMessage.success(buildImportSummary('学生', response, '学生数据导入成功'));
    await loadStudents(true);
  } catch (error) {
    await showImportErrors('学生导入失败明细', error);
    return false;
  }
  return false;
}

async function toggleStudent(row) {
  await updateStudent(row.id, {
    ...row,
    status: row.status === 0 ? 1 : 0
  });
  ElMessage.success(row.status === 0 ? '学生账号已封禁' : '学生账号已解封');
  await loadStudents();
}

function openCourseDialog(row) {
  courseForm.value = row ? { ...createCourseForm(), ...row } : createCourseForm();
  courseDialogVisible.value = true;
}

async function generateCourseNo() {
  const response = await fetchNextCourseNo();
  courseForm.value.courseNo = response.data || '';
}

async function submitCourse() {
  courseSubmitting.value = true;
  try {
    if (courseForm.value.id) {
      await updateCourse(courseForm.value.id, courseForm.value);
      ElMessage.success('课程更新成功');
    } else {
      await createCourse(courseForm.value);
      ElMessage.success('课程创建成功');
    }
    courseDialogVisible.value = false;
    await loadCourses();
  } finally {
    courseSubmitting.value = false;
  }
}

async function removeCourse(row) {
  await ElMessageBox.confirm(`确认删除课程“${row.courseName}”吗？`, '删除确认', { type: 'warning' });
  await deleteCourse(row.id);
  ElMessage.success('课程删除成功');
  await loadCourses();
}

async function handleCourseExport() {
  try {
    await exportCourseExcel({
      keyword: courseState.keyword,
      status: courseState.statusFilter
    });
    ElMessage.success('课程数据导出中');
  } catch (error) {
    ElMessage.error('课程数据导出失败');
  }
}

async function handleCourseTemplateDownload() {
  try {
    await downloadCourseTemplate();
  } catch (error) {
    ElMessage.error('课程模板下载失败');
  }
}

async function handleCourseImport(file) {
  try {
    const response = await importCourseExcel(file);
    ElMessage.success(buildImportSummary('课程', response, '课程数据导入成功'));
    await loadCourses(true);
  } catch (error) {
    await showImportErrors('课程导入失败明细', error);
    return false;
  }
  return false;
}

async function toggleCourse(row) {
  await updateCourse(row.id, {
    ...row,
    status: row.status === 0 ? 1 : 0
  });
  ElMessage.success(row.status === 0 ? '课程已停用' : '课程已启用');
  await loadCourses();
}

async function openClassroomDialog(row) {
  if (!row) {
    classroomForm.value = createClassroomForm();
    classroomDialogVisible.value = true;
    return;
  }
  const response = await fetchClassroomDetail(row.id);
  classroomForm.value = {
    ...createClassroomForm(),
    ...(response.data || {})
  };
  classroomDialogVisible.value = true;
}

async function submitClassroom() {
  classroomSubmitting.value = true;
  try {
    if (classroomForm.value.id) {
      await updateClassroom(classroomForm.value);
      ElMessage.success('教室更新成功');
    } else {
      await createClassroom(classroomForm.value);
      ElMessage.success('教室创建成功');
    }
    classroomDialogVisible.value = false;
    await loadClassrooms();
  } finally {
    classroomSubmitting.value = false;
  }
}

async function removeClassroom(row) {
  await ElMessageBox.confirm(`确认删除教室“${row.classroomName}”吗？`, '删除确认', { type: 'warning' });
  await deleteClassroom(row.id);
  ElMessage.success('教室删除成功');
  await loadClassrooms();
}

async function handleClassroomTemplateDownload() {
  try {
    await downloadClassroomTemplate();
  } catch (error) {
    ElMessage.error('教室模板下载失败');
  }
}

async function handleClassroomExport() {
  try {
    await exportClassroomExcel({
      keyword: classroomState.keyword,
      teachbuildNo: classroomState.teachbuildFilter
    });
    ElMessage.success('教室数据导出中');
  } catch (error) {
    ElMessage.error('教室数据导出失败');
  }
}

async function handleClassroomImport(file) {
  try {
    const response = await importClassroomExcel(file);
    ElMessage.success(buildImportSummary('教室', response, '教室数据导入成功'));
    await loadClassrooms();
  } catch (error) {
    await showImportErrors('教室导入失败明细', error);
    return false;
  }
  return false;
}

async function handleTeachbuildTemplateDownload() {
  try {
    await downloadTeachbuildTemplate();
  } catch (error) {
    ElMessage.error('教学楼模板下载失败');
  }
}

async function handleTeachbuildExport() {
  try {
    await exportTeachbuildExcel({
      keyword: classroomState.teachbuildFilter || classroomState.keyword
    });
    ElMessage.success('教学楼数据导出中');
  } catch (error) {
    ElMessage.error('教学楼数据导出失败');
  }
}

async function handleTeachbuildImport(file) {
  try {
    const response = await importTeachbuildExcel(file);
    ElMessage.success(buildImportSummary('教学楼', response, '教学楼数据导入成功'));
    await Promise.all([loadTeachbuildOptions(), loadClassrooms()]);
  } catch (error) {
    await showImportErrors('教学楼导入失败明细', error);
    return false;
  }
  return false;
}

async function loadTeachbuildOptions() {
  const response = await fetchTeachbuildList();
  teachbuildOptions.value = response.data || [];
}

onMounted(async () => {
  await Promise.all([loadTeachers(), loadStudents(), loadCourses(), loadClassrooms(), loadTeachbuildOptions()]);
});
</script>

<style scoped>
.resource-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-panel {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  border: 1px solid #dbe7f6;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgb(255 255 255 / 88%), transparent 36%),
    linear-gradient(135deg, #fffdf8 0%, #f6fbff 48%, #eef6ff 100%);
  box-shadow: 0 18px 42px rgb(17 34 68 / 8%);
}

.eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  color: #7b6c45;
  text-transform: uppercase;
}

.hero-title {
  margin: 10px 0 8px;
  font-size: 34px;
  color: #17263d;
}

.hero-description {
  max-width: 760px;
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #53667f;
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.stat-chip {
  min-width: 180px;
  padding: 14px 16px;
  border: 1px solid #e5edf6;
  border-radius: 20px;
  background: rgb(255 255 255 / 75%);
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #8090a7;
}

.resource-card {
  border: 1px solid #e5edf6;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 4%);
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.toolbar-row > .el-input {
  max-width: 320px;
}

.toolbar-select {
  width: 170px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.excel-upload {
  display: inline-flex;
}

.toolbar-placeholder {
  font-size: 13px;
  color: #6b7b90;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.inline-field {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

:deep(.resource-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.resource-tabs .el-tabs__item) {
  height: 42px;
  padding: 0 18px;
  font-weight: 700;
  color: #62748b;
}

:deep(.resource-tabs .el-tabs__item.is-active) {
  color: #165dff;
}

:deep(.resource-tabs .el-tabs__active-bar) {
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, #165dff 0%, #0ea5a4 100%);
}

:deep(.primary-action.el-button) {
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #165dff 0%, #0ea5a4 100%);
  box-shadow: 0 12px 24px rgb(22 93 255 / 22%);
}

:deep(.ghost-action.el-button) {
  border-radius: 999px;
  border-color: #d7e1ef;
  background: rgb(255 255 255 / 82%);
}

@media (max-width: 960px) {
  .hero-panel,
  .toolbar-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-row > .el-input {
    max-width: none;
    width: 100%;
  }

  .toolbar-select {
    width: 100%;
  }
}
</style>
