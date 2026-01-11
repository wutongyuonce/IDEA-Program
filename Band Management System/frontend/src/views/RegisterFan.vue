<template>
  <div class="register-container">
    <div class="register-box">
      <h1 class="title">歌迷用户注册</h1>
      <el-alert
        title="登录提示"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        注册成功后，登录用户名格式为：<strong>fan_姓名拼音</strong><br/>
        例如：姓名为"张宇"，登录用户名为 <strong>fan_zhangyu</strong>
      </el-alert>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form" label-width="120px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入姓名（中文或英文）" />
          <div class="form-tip">此名称将自动转换为拼音，生成登录用户名</div>
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="registerForm.gender">
            <el-radio label="M">男</el-radio>
            <el-radio label="F">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="registerForm.age" :min="1" :max="120" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="职业" prop="occupation">
          <el-input v-model="registerForm.occupation" placeholder="请输入职业" />
        </el-form-item>
        
        <el-form-item label="学历" prop="education">
          <el-select v-model="registerForm.education" placeholder="请选择学历" style="width: 100%">
            <el-option label="高中及以下" value="高中及以下" />
            <el-option label="专科" value="专科" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
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
  gender: 'M',
  age: 20,
  occupation: '',
  education: '',
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
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  occupation: [
    { required: true, message: '请输入职业', trigger: 'blur' }
  ],
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
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
        const response = await request.post('/auth/register/fan', {
          name: registerForm.name,
          gender: registerForm.gender,
          age: registerForm.age,
          occupation: registerForm.occupation,
          education: registerForm.education,
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
