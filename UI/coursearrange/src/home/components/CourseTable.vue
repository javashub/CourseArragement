<template>
  <div class="class-table">
    <div class="top-select"></div>
    <div class="table-wrapper">
      <div class="tabel-container">
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
      semester: [
        {
          value: "2019-2020-1",
          label: "2019-2020-1"
        }
      ],
      grade: [
        {
          value: "01",
          label: "高一"
        },
        {
          value: "02",
          label: "高二"
        },
        {
          value: "03",
          label: "高三"
        }
      ],
      classNo: [
        {
          value: "",
          label: ""
        }
      ],
      value1: "",
      value2: "",
      value3: "",
      classTableData: {
        lessons: [
          "07.20-8.55",
          "9.10-10.45",
          "11.00-12.35",
          "14.20-15.55",
          "16.10-17.45"
        ],
        courses: [[], [], [], [], []]
      }
    };
  },
  created() {},
  mounted() {
    let student = window.localStorage.getItem("student");
    if (student != null) {
      this.value3 = JSON.parse(student).classNo;
      if (this.value3 === "" || this.value3 === null) {
        this.$message.error("您还没加入班级呢，暂时无法查询课表哦");
      } else {
        this.queryCoursePlan()
      }
    } else {
      this.$message({
        message: "获取本地用户信息失败，请重新登录",
        type: "error"
      });
    }
  },
  methods: {
    // 查询课程表
    queryCoursePlan() {
      this.classTableData.courses.map((item, index) => {
        this.classTableData.courses[index].splice(
          0,
          this.classTableData.courses[index].length
        );
      });
      this.$axios
        .get("http://localhost:8080/courseplan/" + this.value3)
        .then(res => {
          console.log(res);
          let courseData = res.data.data;
          let level = 0;
          let times = 0;
          for (let index = 0; index < courseData.length; index++) {
            times = times + 1;
            const item = courseData[index];
            if (parseInt(item.classTime) != times) {
              this.classTableData.courses[level].push("");
              index = index - 1;
            } else {
              this.classTableData.courses[level].push(
                item.teacher.realname +
                  "-" +
                  item.courseInfo.courseName +
                  "(" +
                  item.classroomNo +
                  ")"
              );
            }
            if (times % 5 == 0) {
              level = level + 1;
            }
          }
        });
    },

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
        "五"
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
  .top-select {
    text-align: left;
    margin-left: 7px;
  }
  .table-wrapper {
    width: 100%;
    height: 100%;
    overflow: auto;
  }
  .tabel-container {
    margin: 7px;
    font-size: 30px;
    table {
      table-layout: fixed;
      width: 100%;
      word-wrap: break-word;
      word-break: break-all;
      border-collapse: collapse;
      thead {
        background-color: #67a1ff;
        th {
          color: #fff;
          line-height: 17px;
          font-weight: normal;
          font-size: 15px;
        }
      }
      tbody {
        background-color: #eaf2ff;
        td {
          font-size: 13px;
          color: #677998;
          line-height: 13px;
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
          font-size: 12px;
        }
      }
    }
  }
}
</style>