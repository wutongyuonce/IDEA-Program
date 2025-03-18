<template>
  <!-- 后台管理系统主界面 -->
  <div class="wrapper">
    <el-container>
      <!-- 头部 -->
      <el-header class="main-header">
        <div class="header-title">智能排课系统</div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <i class="el-icon-user"></i>
              {{ name }}
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="center">个人中心</el-dropdown-item>
              <el-dropdown-item command="updatePassword">修改密码</el-dropdown-item>
              <el-dropdown-item command="exit">退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="220px">
          <el-menu
            :default-active="default_active"
            @select="handleSelect"
            unique-opened
            background-color="#304156"
            text-color="#fff"
            active-text-color="#409EFF"
            class="main-menu">
            
            <el-menu-item index="0" class="main-menu-item">
              <router-link to="/systemdata" class="menu-link">
                <i class="el-icon-setting"></i>
                <span>系统数据</span>
              </router-link>
            </el-menu-item>

            <el-submenu index="1" class="main-menu-item">
              <template slot="title">
                <i class="el-icon-s-data"></i>
                <span>排课管理</span>
              </template>
              <el-menu-item index="1-1" v-if="!isTeacher" class="sub-menu-item">
                <router-link to="/classtasklist" class="menu-link">课程计划</router-link>
              </el-menu-item>
              <el-menu-item index="1-2" class="sub-menu-item">
                <router-link to="/coursetable" class="menu-link">查看课表</router-link>
              </el-menu-item>
            </el-submenu>

            <el-submenu index="3" v-if="!isTeacher" class="main-menu-item">
              <template slot="title">
                <i class="el-icon-user"></i>
                <span>讲师管理</span>
              </template>
              <el-menu-item index="3-1" class="sub-menu-item">
                <router-link to="/teacherlist" class="menu-link">所有讲师</router-link>
              </el-menu-item>
            </el-submenu>

            <el-submenu index="4" class="main-menu-item">
              <template slot="title">
                <i class="el-icon-box"></i>
                <span>班级管理</span>
              </template>
              <el-menu-item index="4-1" class="sub-menu-item">
                <router-link to="/classmanager" class="menu-link">所有班级</router-link>
              </el-menu-item>
            </el-submenu>

            <el-submenu index="5" class="main-menu-item">
              <template slot="title">
                <i class="el-icon-user"></i>
                <span>学生管理</span>
              </template>
              <el-menu-item index="5-1" class="sub-menu-item">
                <router-link to="/studentlist" class="menu-link">所有学生</router-link>
              </el-menu-item>
            </el-submenu>

            <el-submenu index="6" class="main-menu-item">
              <template slot="title">
                <i class="el-icon-notebook-1"></i>
                <span>教学资料</span>
              </template>
              <el-menu-item index="6-1" class="sub-menu-item">
                <router-link to="/courseinfolist" class="menu-link">教材列表</router-link>
              </el-menu-item>
            </el-submenu>

            <el-submenu index="7" v-if="!isTeacher" class="main-menu-item">
              <template slot="title">
                <i class="el-icon-office-building"></i>
                <span>教学设施</span>
              </template>
              <el-menu-item index="7-1" class="sub-menu-item">
                <router-link to="/teachbuildinglist" class="menu-link">教学楼管理</router-link>
              </el-menu-item>
              <el-menu-item index="7-2" class="sub-menu-item">
                <router-link to="/classroomlist" class="menu-link">教室列表</router-link>
              </el-menu-item>
              <el-menu-item index="7-3" class="sub-menu-item">
                <router-link to="/setteacharea" class="menu-link">教学区域安排</router-link>
              </el-menu-item>
            </el-submenu>

            <el-submenu index="8" class="main-menu-item">
              <template slot="title">
                <i class="el-icon-help"></i>
                <span>帮助中心</span>
              </template>
              <el-menu-item index="8-1" class="sub-menu-item">
                <router-link to="/help" class="menu-link">使用说明</router-link>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view></router-view>
        </el-main>
      </el-container>
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
      name: '用户名',
    };
  },
  computed: {
    isTeacher:()=>{
      return window.localStorage.getItem('teacher') != null;
    }
  },
  mounted() {
    setInterval(() => {
      this.getTime();
    }, 1000);
    
    let admin = window.localStorage.getItem('admin')
    if(admin != null){
      this.name = (JSON.parse(admin)).realname
    } else {
      let teacher = window.localStorage.getItem('teacher')
      if (teacher != null) {
        this.name = (JSON.parse(teacher)).realname
      }
    }
  },

  methods: {
    // 下拉菜单功能，退出、个人中心
    handleCommand(command) {
      // alert(command)
      if (command == 'exit') {
        localStorage.removeItem('token')
        localStorage.removeItem('admin')
        localStorage.removeItem('teacher')
        // 判断，返回指定页面
        this.$router.push('/')
      } else if (command == 'center') {
        // 跳转到个人中心
      } else if (command == 'updatePassword') {
        // 修改密码页面
        this.$router.push('/updatepass')
      }
      
    },

    // 获取系统时间
    getTime() {
      this.time = new Date().toLocaleString();
    },

    // 展开一个菜单
    handleSelect(val) {
      this.default_active = val;
    }
  }
};
</script>

<style lang="less" scoped>
.wrapper {
  min-height: 100vh;
  display: flex;
  
  .el-container {
    width: 100%;
    min-height: 100vh;
  }

  .main-header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    height: 60px;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    
    .header-title {
      font-size: 20px;
      font-weight: bold;
      color: #303133;
    }
    
    .header-right {
      .user-info {
        font-size: 15px;
        color: #606266;
        cursor: pointer;
        
        i {
          margin-right: 5px;
        }
      }
    }
  }
  
  .el-aside {
    position: fixed;
    top: 60px;
    left: 0;
    bottom: 0;
    background-color: #304156;
    overflow-y: auto;
    z-index: 999;
    
    .main-menu {
      border-right: none;
      min-height: 100%;
    }
  }

  .el-main {
    margin-left: 220px;
    margin-top: 60px;
    padding: 20px;
    background-color: #f0f2f5;
    min-height: calc(100vh - 60px);
  }
}

// 主菜单项样式
.main-menu-item {
  font-size: 16px !important;
  font-weight: 500;
  
  .el-submenu__title {
    font-size: 16px !important;
    font-weight: 500;
    height: 56px;
    line-height: 56px;
    
    i {
      color: #fff;
      font-size: 18px;
      margin-right: 12px;
    }
  }
}

// 子菜单项样式
.sub-menu-item {
  font-size: 14px !important;
  background-color: #1f2d3d !important;
  
  &:hover {
    background-color: #1f2d3d !important;
  }
  
  .menu-link {
    color: #fff !important;
    
    &:hover {
      color: #409EFF !important;
    }
  }
}

// 所有菜单链接的通用样式
.menu-link {
  text-decoration: none !important;
  color: #fff !important;
  display: block;
  width: 100%;
  height: 100%;
  
  &:hover {
    color: #409EFF !important;
  }
}

// 覆盖 element-ui 的默认样式
::v-deep .el-menu-item {
  height: 50px;
  line-height: 50px;
  
  &.is-active {
    background-color: #263445 !important;
  }
}

::v-deep .el-submenu .el-menu-item {
  min-width: 220px;
  padding-left: 48px !important;
}

// 添加滚动条样式
.el-aside {
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #41546d;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-track {
    background: #304156;
  }
}

// 确保内容区域正确显示
.el-container {
  flex-direction: column;
}
</style>