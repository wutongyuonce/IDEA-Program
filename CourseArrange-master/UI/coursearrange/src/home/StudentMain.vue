<template>
  <!-- 学生端主界面 -->
  <!-- 学生点击修改密码，跳转到updatepass.vue与其他用户共用一个组件 -->
  <!-- 学生登录之后默认进入查看课表页面 -->
  <!-- 点击学生右上角的个人中心跳转到一个页面展示个人信息，上方放一个按钮加入班级，一个修改信息 -->
  <div class="wrapper">
    <el-container>
      <el-header>
        <!-- 头 -->
        <el-header style="text-align: right; font-size: 12px">
          <!-- 系统标题 -->
          <el-dropdown @command="handleCommand">
            <i class="el-icon-setting" style="margin-right: 15px"></i>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="center">个人中心</el-dropdown-item>
              <el-dropdown-item command="exit">退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>您好，
          <span>{{name}}</span>
        </el-header>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <!-- 侧边 -->
          <!-- 默认展开的索引default-active -->
          <el-menu :default-active="default_active" @select="handleSelect" unique-opened class="main-menu">
            <el-menu-item index="/index" class="menu-item">
              <template slot="title">
                <i class="el-icon-s-home"></i>
                <span slot="title">首页</span>
              </template>
            </el-menu-item>

            <el-submenu index="1" class="menu-item">
              <template slot="title">
                <i class="el-icon-s-data"></i>
                <span>排课管理</span>
              </template>
              <el-menu-item index="/classtasklist" class="sub-menu-item">
                <span>课程计划</span>
              </el-menu-item>
              <el-menu-item index="/coursetable" class="sub-menu-item">
                <span>查看课表</span>
              </el-menu-item>
            </el-submenu>

            <el-menu-item index="/courseList" class="menu-item">
              <template slot="title">
                <i class="el-icon-s-marketing"></i>
                <span slot="title">课程表</span>
              </template>
            </el-menu-item>

            <el-menu-item index="/emptyclassroom" class="menu-item">
              <template slot="title">
                <i class="el-icon-school"></i>
                <span slot="title">空教室查询</span>
              </template>
            </el-menu-item>

            <el-menu-item index="/center" class="menu-item">
              <template slot="title">
                <i class="el-icon-user"></i>
                <span slot="title">个人中心</span>
              </template>
            </el-menu-item>

            <el-menu-item index="/password" class="menu-item">
              <template slot="title">
                <i class="el-icon-unlock"></i>
                <span slot="title">修改密码</span>
              </template>
            </el-menu-item>
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
      default_active: "0",
      name: "用户名"
    };
  },
  computed: {},
  mounted() {
    setInterval(() => {
      this.getTime();
    }, 1000);

    let student = window.localStorage.getItem("student")
    if (student != null) {
      this.name = JSON.parse(student).realname
    }
  },

  methods: {
    handleCommand(command) {
      if (command == "exit") {
        localStorage.removeItem("token");
        localStorage.removeItem("student");
        this.$router.push("/");
      } else if (command == "center") {
        this.$router.push("/center");
      }
    },

    // 获取系统时间
    getTime() {
      this.time = new Date().toLocaleString();
    },

    // 展开一个菜单
    handleSelect(val) {
      this.default_active = val;
      if(val=='/index'){
        // 跳转到系统主页
        this.$router.push('/');
        return;
      }
      this.$router.push(val);
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
  background-color: #f0f2f5;
  color: #333;
  text-align: left;
  line-height: normal;
  border-right: 1px solid #dcdfe6;
}

.main-menu {
  border-right: none;
  background-color: transparent;

  .menu-item {
    height: 56px;
    line-height: 56px;
    padding: 0 20px;
    font-size: 14px;
    color: #303133;
    transition: all 0.3s;

    &:hover {
      background-color: #ecf5ff;
    }

    i {
      margin-right: 10px;
      font-size: 18px;
      vertical-align: middle;
    }
  }

  .sub-menu-item {
    height: 50px;
    line-height: 50px;
    padding-left: 48px;
    font-size: 14px;
    color: #606266;

    &:hover {
      color: #409EFF;
    }
  }
}

.el-main {
  background-color: #fff;
  color: #333;
  text-align: center;
  padding: 20px;
}

body > .el-container {
  margin-bottom: 40px;
}
</style>