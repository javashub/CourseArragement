<template>
  <!-- 后台管理系统主界面 -->
  <div class="wrapper">
    <el-container>
      <el-header>
        <!-- 头 -->
        <el-header style="text-align: right; font-size: 12px">
          <!-- 系统标题 -->
          <el-dropdown>
            <i class="el-icon-setting" style="margin-right: 15px"></i>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>个人中心</el-dropdown-item>
              <el-dropdown-item>退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>您好，
          <span>梁同学</span>
        </el-header>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <!-- 侧边 -->
          <!-- 默认展开的索引default-active -->
          <el-menu :default-active="default_active" @select="handleSelect" unique-opened>
            <el-menu-item index="0">
              <template slot="title">
                <router-link to="SystemData" class="links">
                  <i class="el-icon-setting"></i>系统数据
                </router-link>
              </template>
            </el-menu-item>

            <el-submenu index="1">
              <template slot="title">
                <i class="el-icon-setting"></i>排课管理
              </template>

              <el-menu-item index="1-1">
                <router-link to="classTaskList" class="links">课程计划</router-link>
              </el-menu-item>
              <!-- <el-menu-item index="1-1">添加计划</el-menu-item> -->
              <el-menu-item index="1-2">排课页面</el-menu-item>
              <el-menu-item index="1-3">
                <router-link to="courseTable" class="links">查看课表</router-link>
              </el-menu-item>
            </el-submenu>

            <el-submenu index="2">
              <template slot="title">
                <i class="el-icon-message"></i>讲师管理
              </template>
              <el-menu-item-group>
                <el-menu-item index="2-1">
                  <router-link to="teacherlist" class="links">所有讲师</router-link>
                </el-menu-item>
                <el-menu-item index="2-2">添加讲师</el-menu-item>
              </el-menu-item-group>
            </el-submenu>

            <el-submenu index="3">
              <template slot="title">
                <i class="el-icon-menu"></i>学生管理
              </template>
              <el-menu-item-group>
                <el-menu-item index="3-1">
                  <router-link to="studentlist" class="links">所有学生</router-link>
                </el-menu-item>
                <el-menu-item index="3-2">添加学生</el-menu-item>
              </el-menu-item-group>
            </el-submenu>

            <el-submenu index="4">
              <template slot="title">
                <i class="el-icon-setting"></i>课程管理
              </template>

              <el-menu-item index="4-1">
                <router-link class="links" to="courseInfoList">教材列表</router-link>
              </el-menu-item>
              <el-menu-item index="4-2">添加教材</el-menu-item>
            </el-submenu>

            <el-submenu index="5">
              <template slot="title">
                <i class="el-icon-setting"></i>教学设施
              </template>

              <el-menu-item index="5-1">
                <router-link class="links" to="teachbuildinglist">教学楼管理</router-link>
              </el-menu-item>

              <el-menu-item index="5-2">
                <router-link to="classroomlist" class="links">教室列表</router-link>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-aside>

        <el-main>
          <!-- Main区域，数据显示 -->
          <router-view></router-view>
        </el-main>
      </el-container>
      <!-- 显示系统时间 -->
      <el-footer>{{time}}</el-footer>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "ManagerMain",
  data() {
    return {
      time: "",
      default_active: "0"
    };
  },

  mounted() {
    setInterval(() => {
      this.getTime();
    }, 1000);
  },

  methods: {
    // 获取系统时间
    getTime() {
      this.time = new Date().toLocaleString();
    },

    // 展开一个菜单
    handleSelect(val) {
      this.default_active = val;
    }
  }
};
</script>

<style lang="less" scoped>
.wrapper {
  height: 100%;
  width: 100%;
  .a {
    text-decoration: none !important;
    color: #303133 !important;
  }
  .links {
    text-decoration: none !important;
    color: #303133 !important;
  }
}

.el-container {
  height: 100%;
  padding: 0;
  margin: 0;
  width: 100%;
}

.el-header,
.el-footer {
  background-color: #b3c0d1;
  color: #333;
  text-align: center;
  line-height: 60px;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  text-align: center;
  line-height: 200px;
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  text-align: center;
  padding: 10px;
}

body > .el-container {
  margin-bottom: 40px;
}

.el-container:nth-child(5) .el-aside,
.el-container:nth-child(6) .el-aside {
  line-height: 260px;
}

.el-container:nth-child(7) .el-aside {
  line-height: 320px;
}
</style>