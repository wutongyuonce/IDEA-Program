<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>管理员后台</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/admin/bands">
          <el-icon><User /></el-icon>
          <span>乐队管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/members">
          <el-icon><UserFilled /></el-icon>
          <span>成员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/albums">
          <el-icon><Tickets /></el-icon>
          <span>专辑管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/songs">
          <el-icon><Headset /></el-icon>
          <span>歌曲管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/concerts">
          <el-icon><Microphone /></el-icon>
          <span>演唱会管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/fans">
          <el-icon><Avatar /></el-icon>
          <span>歌迷管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reviews">
          <el-icon><ChatDotRound /></el-icon>
          <span>乐评管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="breadcrumb">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <span class="username">{{ username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout } from '../api/auth'

const router = useRouter()
const route = useRoute()

const username = ref(localStorage.getItem('username') || '管理员')

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '数据统计',
    '/admin/bands': '乐队管理',
    '/admin/members': '成员管理',
    '/admin/albums': '专辑管理',
    '/admin/songs': '歌曲管理',
    '/admin/concerts': '演唱会管理',
    '/admin/fans': '歌迷管理',
    '/admin/reviews': '乐评管理'
  }
  return titles[route.path] || '管理员后台'
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await logout()
    localStorage.clear()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出失败:', error)
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
}

.logo h2 {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left .breadcrumb {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  color: #666;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
