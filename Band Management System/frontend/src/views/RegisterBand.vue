<template>
  <div class="register-container">
    <div class="register-box">
      <h1 class="title">乐队用户注册</h1>
      <el-alert
        title="登录提示"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        注册成功后，登录用户名格式为：<strong>band_乐队名称拼音</strong><br/>
        例如：乐队名称为"你好乐队"，登录用户名为 <strong>band_nihaoyuedui</strong>
      </el-alert>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form" label-width="120px">
        <el-form-item label="乐队名称" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入乐队名称（中文或英文）" />
          <div class="form-tip">此名称将自动转换为拼音，生成登录用户名</div>
        </el-form-item>
        
        <el-form-item label="成立时间" prop="foundedAt">
          <el-date-picker
            v-model="registerForm.foundedAt"
            type="date"
            placeholder="请选择成立时间"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="乐队简介" prop="intro">
          <el-input
            v-model="registerForm.intro"
            type="textarea"
            :rows="4"
            placeholder="请输入乐队简介"
          />
        </el-form-item>
        
        <el-form-item label="登录密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入登录密码（至少6位）"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button
            size="large"
            style="width: 100%"
            @click="goToLogin"
          >
            返回登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  name: '',
  foundedAt: '',
  intro: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入乐队名称', trigger: 'blur' }
  ],
  foundedAt: [
    { required: true, message: '请选择成立时间', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await request.post('/auth/register/band', {
          name: registerForm.name,
          foundedAt: registerForm.foundedAt,
          intro: registerForm.intro,
          password: registerForm.password
        })
        
        // 使用后端返回的实际用户名
        const username = response.data.username
        ElMessage.success('注册成功！登录用户名为：' + username)
        setTimeout(() => {
          router.push('/login')
        }, 3000)
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 简单的拼音转换函数（仅用于提示）
const convertToPinyin = (name) => {
  // 移除空格和特殊字符，转换为小写
  return name.toLowerCase().replace(/[^a-z0-9\u4e00-\u9fa5]/g, '')
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 600px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
  font-size: 28px;
}

.register-form {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
