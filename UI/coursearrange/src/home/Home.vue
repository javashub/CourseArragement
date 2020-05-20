<template>
  <div class="wrapper" :style="wrapper">
    <div>
      <h1 class="title">欢迎来到遗传算法排课系统</h1>
      <div class="inner">
        <h3><router-link to="admin/login" style="text-decoration:none;">管理员讲师入口</router-link></h3>
        <h3><router-link to="student/login" style="text-decoration:none;">学生入口</router-link></h3>
      </div>
    </div>
    
    <h3 class="footer"><router-link to="/" style="text-decoration:none;">联系作者</router-link></h3>
    <h3>Copy Right：© 2020 Course Arrange System</h3>
    
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      wrapper: {
         backgroundImage: "url(" + require("../images/image1.jpg") + ")",
            backgroundRepeat: "no-repeat",
            
      },
    }
  }
}
</script>

<style lang="less" scoped>
  .footer {
    // margin-bottom: 0%;
    position: relative;
    margin: auto;
  }

  .wrapper {
    background-image: "@images/psb.jpg";
    height: 100%;
    width: 100%;
    position: absolute;
  }

  .title {
    text-align: left;
    margin-left: 30px;
    margin-top: 10px;
  }
</style>