<template>
  <div>
    <!-- 添加讲师组件 -->

    
  </div>
</template>

<script>
export default {
  name: 'AddTeacher',
  data() {
    return {
      teacherData: [],
    }
  },
  mounted(){
    
  },

  methods: {
    // 获取所有讲师
    addTeacher() {
      this.$axios
        .post("http://localhost:8080/teacher/queryteacher")
        .then(res => {
          
        })
        .catch(error => {
          console.log("添加讲师失败");
        });
    }
  }
}
</script>

<style lang="less" scoped>

</style>