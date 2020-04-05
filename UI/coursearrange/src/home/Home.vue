<template>
  <div>
    
    <h1>欢迎来到遗传算法排课系统</h1>
    
    <h3><router-link to="admin/login" style="text-decoration:none;">管理员入口</router-link>：© 2020 排课</h3>
    <h3><router-link to="teacher/login" style="text-decoration:none;">讲师入口</router-link>：© 2020 排课</h3>
    <h3><router-link to="student/login" style="text-decoration:none;">学生入口</router-link>：© 2020 排课</h3>
    <h3 class="footer"><router-link to="/" style="text-decoration:none;">联系作者</router-link>：© 2020 排课</h3>
    
  </div>
</template>

<script>
export default {
  name: 'Home',
  
}
</script>

<style lang="less" scoped>
  .footer {
    margin-bottom: 0%;
  }
</style>