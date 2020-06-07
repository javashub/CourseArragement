<template>
  <div id="chart" class="wrapper">
    <!-- 系统数据 -->
  </div>
</template>

<script>
export default {
  name: "SystemData",
  data() {
    return {

    };
  },

  mounted() {
    this.systemData()
    this.draw()
  },
  computed: {

  },
  methods: {
    // 获取系统数据
    systemData() {
      this.$axios
        .get("http://localhost:8080/systemdata")
        .then(res => {
          if (res.data.code == 0) {
          } else {
            console.log(res.data.message);
          }
        })
        .catch();
    },

    // 画图
    draw() {
      let chart = this.$echarts.init(document.getElementById("chart"));

      chart.setOption({
        title: { text: "系统数据" },
        tooltip: {
          trigger: "axis",
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: "shadow" // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        color: ["#3398DB"],
        grid: {
          left: "3%",
          right: "4%",
          bottom: "3%",
          containLabel: true
        },
        xAxis: [
          {
            type: "category",
            data: ["讲师", "学生", "班级", "教室", "教学楼", "教材", "文档", "网课", "题库", "排课任务", "新增学生"],
            axisTick: {
              alignWithLabel: true
            }
          }
        ],
        yAxis: [
          {
            type: "value"
          }
        ],
        series: [
          {
            name: "",
            type: "bar",
            barWidth: "60%",
            data: [10, 52, 200, 334, 390, 330, 220, 100, 80, 90, 5]
          }
        ]
      });
    }
  }
};
</script>

<style lang="less" scoped>
.wrapper {
  height: 600px;
  width: 900px;
  // background-color: aqua;
}
</style>