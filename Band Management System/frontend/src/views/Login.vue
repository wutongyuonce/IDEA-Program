<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">乐队管理系统</h1>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="role">
          <el-select
            v-model="loginForm.role"
            placeholder="请选择角色"
            size="large"
            style="width: 100%"
          >
            <el-option label="管理员" value="ADMIN" />
            <el-option label="乐队用户" value="BAND" />
            <el-option label="歌迷用户" value="FAN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-links">
        <el-button type="text" @click="goToRegister('band')">乐队用户注册</el-button>
        <el-divider direction="vertical" />
        <el-button type="text" @click="goToRegister('fan')">歌迷用户注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: 'ADMIN' // 默认角色
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        console.log('开始登录...', loginForm)
        const res = await login(loginForm)
        console.log('登录响应:', res)
        
        // 保存用户信息到localStorage（Session由后端管理，cookie自动携带）
        localStorage.setItem('userRole', res.data.role)
        localStorage.setItem('userId', res.data.userId)
        localStorage.setItem('username', res.data.username)
        if (res.data.relatedId) {
          localStorage.setItem('relatedId', res.data.relatedId)
        }
        
        console.log('用户信息已保存:', {
          role: res.data.role,
          userId: res.data.userId,
          username: res.data.username
        })
        
        ElMessage.success('登录成功')
        
        // 根据角色跳转到不同页面
        let targetPath = '/login'
        if (res.data.role === 'ADMIN') {
          targetPath = '/admin/dashboard'
        } else if (res.data.role === 'BAND') {
          targetPath = '/band/home'
        } else if (res.data.role === 'FAN') {
          targetPath = '/fan/home'
        }
        
        console.log('准备跳转到:', targetPath)
        await router.push(targetPath)
        console.log('跳转完成')
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = (type) => {
  if (type === 'band') {
    router.push('/register/band')
  } else if (type === 'fan') {
    router.push('/register/fan')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 28px;
}

.login-form {
  margin-top: 20px;
}

.register-links {
  margin-top: 20px;
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 5px;
}

.register-links .el-button {
  font-size: 14px;
  padding: 0 10px;
}

.tips {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 5px;
  font-size: 12px;
  color: #666;
  line-height: 1.8;
}

.tips p {
  margin: 0;
}
</style>
