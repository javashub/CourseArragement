<template>
  <div class="wrapper">
    <!-- 系统数据 -->
    <h1>王总牛逼</h1>

  </div>
</template>

<script>
export default {
  name: 'SystemData',
  data() {
    return {
      
    }
  },
  mounted() {
    this.systemData()
  },
  methods: {
    // 获取系统数据
    systemData() {
      this.$axios.get("http://localhost:8080/systemdata")
      .then()
      .catch()
    }
  }
}
</script>

<style lang="less" scoped>
  .wrapper {
    height: 400px;
    width: 600px;
    // background-color: aqua;
  }
</style>