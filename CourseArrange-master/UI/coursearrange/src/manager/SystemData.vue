<template>
  <div class="container">
    <div id="chart" class="chart-wrapper">
      <!-- 系统数据 -->
    </div>
  </div>
</template>

<script>
export default {
  name: "SystemData",
  data() {
    return {
      sysData: [],
    };
  },

  mounted() {
    this.systemData();
  },
  computed: {},
  methods: {
    // 获取系统数据
    systemData() {
      this.$axios
        .get("http://localhost:8080/systemdata")
        .then((res) => {
          if (res.data.code == 0) {
            let ret = res.data.data;
            this.sysData = ret;
            this.draw();
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {});
    },

    // 画图
    draw(sys) {
      let chart = this.$echarts.init(document.getElementById("chart"));

      chart.setOption({
        title: { text: "系统数据" },
        tooltip: {
          trigger: "axis",
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: "shadow", // 默认为直线，可选为：'line' | 'shadow'
          },
        },
        color: ["#3398DB"],
        grid: {
          left: "3%",
          right: "4%",
          bottom: "3%",
          containLabel: true,
        },
        xAxis: [
          {
            type: "category",
            data: [
              "讲师",
              "学生",
              "班级",
              "教室",
              "教学楼",
              "教材",
              "排课任务",
              "新增学生",
              "新增讲师",
            ],
            axisTick: {
              alignWithLabel: true,
            },
          },
        ],
        yAxis: [
          {
            type: "value",
          },
        ],
        series: [
          {
            name: "",
            type: "bar",
            barWidth: "60%",
            data: [
              this.sysData.teachers,
              this.sysData.students,
              this.sysData.classes,
              this.sysData.classrooms,
              this.sysData.teachbuilds,
              this.sysData.courses,
              this.sysData.classtasks,
              this.sysData.studentReg,
              this.sysData.teacherReg,
            ],
          },
        ],
      });
    },
  },
};
</script>

<style lang="less" scoped>
.container {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.chart-wrapper {
  width: 100%;
  height: 600px;
  margin: 0 auto;
  position: relative;
}
</style>
