<template>
  <div>
    <h1>网课类别管理</h1>
    <p>一级分类一级二级分类，可以通过下拉选择一级分类来显示二级分类在下方列表中</p>
    <p>或者通过计算属性计算parent_id字段来判断，是0则为一级分类，否则为2级分类</p>
    <p>类别名称，所属级别分别显示在两列即可</p>
  </div>
</template>

<script>
export default {
  name: 'OnlineCategory',
  data() {
    return {

    }
  },
  mounted() {

  },
  methods: {

  }
}
</script>

<style lang="less" scoped>

</style>