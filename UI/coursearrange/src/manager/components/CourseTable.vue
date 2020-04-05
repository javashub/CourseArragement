<template>
  <div class="class-table">
    <div class="table-wrapper">
      <div class="tabel-container">
        <!-- <el-select v-model="value" placeholder="年级">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
            >
          </el-option>
        </el-select>
        <el-select v-model="value" placeholder="班级">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select> -->
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th
                v-for="(weekNum, weekIndex) in classTableData.courses.length"
                :key="weekIndex"
              >{{'周' + digital2Chinese(weekIndex + 1, 'week')}}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(lesson, lessonIndex) in classTableData.lessons" :key="lessonIndex">
              <td>
                <p>{{'第' + digital2Chinese(lessonIndex+1) + "节"}}</p>
                <p class="period">{{ lesson }}</p>
              </td>

              <td
                v-for="(course, courseIndex) in classTableData.courses"
                :key="courseIndex"
              >{{classTableData.courses[courseIndex][lessonIndex] || '-'}}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      classTableData: {
        lessons: [
          "07.20-8.55",
          "9.10-10.45",
          "11.00-12.35",
          "14.20-15.55",
          "16.10-17.45",
          // "14:00-15:00",
          // "15:00-16:00",
          // "16:00-17:00"
        ],
        // 每一行对应周几的一列
        // 第1节：7.20-8.55
        // 第2节：9.10-10.45 
        // 第3节：11.00-12.35

        // 下午：
        // 第4节：14.20-15.55
        // 第5节：16.10-17.45
        // courses: [
        //   ["", "", "", "", "", "", "", ""],
        //   ["生物", "物理", "化学", "政治", "历史"],
        //   ["语文", "数学", "英语", "历史", "", "化学", "物理", "生物"],
        //   ["生物", "", "化学", "政治", "历史", "英语", "数学", "语文"],
        //   ["语文", "数学", "英语", "历史", "政治", "", "物理", "生物"],
        //   ["生物", "物理", "化学", "", "历史", "英语", "数学", "语文"],
        //   ["语文", "数学", "英语", "", "", "", "", ""]
        // ],
        courses: [
          ["生物", "物理", "化学", "政治", "历史"],
          ["语文", "数学", "英语", "历史", "", "化学", "物理", "生物"],
          ["生物", "", "化学", "政治", "历史", "英语", "数学", "语文"],
          ["语文", "数学", "英语", "历史", "政治", "", "物理", "生物"],
          ["生物", "物理", "化学", "", "历史", "英语", "数学", "语文"],
        ]
      }
    };
  },
  created() {
    // /* mock随机数据*/
    // Mock.mock({
    //     lessons: ['08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00', '13:00-14:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'],
    //     'courses|7': [['生物', '物理', '化学', '政治', '历史', '英语', '', '语文']]
    // });
  },
  methods: {
    /**
     * 数字转中文
     * @param {Number} num 需要转换的数字
     * @param {String} identifier 标识符
     * @returns {String} 转换后的中文
     */
    digital2Chinese(num, identifier) {
      const character = [
        "零",
        "一",
        "二",
        "三",
        "四",
        "五",
        // "六",
        // "七",
        // "八",
      ];
      return identifier === "week" && (num === 0 || num === 7)
        ? "日"
        : character[num];
    }
  }
};
</script>

<style lang="less" scoped>
.class-table {
  .table-wrapper {
    width: 100%;
    height: 100%;
    overflow: auto;
  }
  .tabel-container {
    margin: 7px;

    table {
      table-layout: fixed;
      width: 100%;

      thead {
        background-color: #67a1ff;
        th {
          color: #fff;
          line-height: 17px;
          font-weight: normal;
        }
      }
      tbody {
        background-color: #eaf2ff;
        td {
          color: #677998;
          line-height: 12px;
        }
      }
      th,
      td {
        width: 60px;
        padding: 12px 2px;
        font-size: 12px;
        text-align: center;
      }

      tr td:first-child {
        color: #333;
        .period {
          font-size: 8px;
        }
      }
    }
  }
}
</style>