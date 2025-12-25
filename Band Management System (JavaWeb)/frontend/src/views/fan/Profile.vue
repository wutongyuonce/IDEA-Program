<template>
  <div class="fan-profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" @click="handleEdit">编辑信息</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ formatGender(profile.gender) }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ profile.age }}</el-descriptions-item>
        <el-descriptions-item label="职业">{{ profile.occupation }}</el-descriptions-item>
        <el-descriptions-item label="学历" :span="2">{{ profile.education }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑个人信息"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio label="M">男</el-radio>
            <el-radio label="F">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="formData.age" :min="1" :max="120" style="width: 100%" />
        </el-form-item>
        <el-form-item label="职业" prop="occupation">
          <el-input v-model="formData.occupation" placeholder="请输入职业" />
        </el-form-item>
        <el-form-item label="学历" prop="education">
          <el-select v-model="formData.education" placeholder="请选择学历" style="width: 100%">
            <el-option label="高中及以下" value="高中及以下" />
            <el-option label="专科" value="专科" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 修改密码卡片 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>修改密码</span>
      </template>
      
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" style="max-width: 500px;">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 删除账号卡片 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span style="color: #F56C6C;">危险操作</span>
      </template>
      
      <el-alert
        title="警告"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        删除账号是不可逆操作，将会连带删除以下数据：
        <ul style="margin: 10px 0 0 20px;">
          <li>关注的乐队数据</li>
          <li>收藏的专辑数据</li>
          <li>收藏的歌曲数据</li>
          <li>参加的演唱会记录</li>
          <li>发表的所有乐评</li>
        </ul>
        <p style="margin-top: 10px; color: #E6A23C; font-weight: bold;">
          此操作无法恢复，请谨慎操作！
        </p>
      </el-alert>
      
      <el-button type="danger" @click="handleDeleteAccount" :loading="deleteLoading">
        删除账号
      </el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../../api/request'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const passwordLoading = ref(false)
const deleteLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const passwordFormRef = ref(null)

const profile = ref({
  fanId: null,
  name: '',
  gender: '',
  age: 0,
  occupation: '',
  education: ''
})

const formData = reactive({
  name: '',
  gender: 'M',
  age: 20,
  occupation: '',
  education: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const formatGender = (gender) => {
  const genderMap = {
    'M': '男',
    'F': '女',
    'O': '其他'
  }
  return genderMap[gender] || gender
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/info')
    profile.value = res.data || {}
  } catch (error) {
    console.error('加载个人信息失败:', error)
    ElMessage.error('加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  Object.assign(formData, profile.value)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await request.put('/fan/info', formData)
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadProfile()
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await request.put('/fan/password', passwordForm)
        ElMessage.success('密码修改成功，请重新登录')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        passwordFormRef.value.resetFields()
        // 3秒后跳转到登录页
        setTimeout(() => {
          window.location.href = '/login'
        }, 3000)
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handleDeleteAccount = async () => {
  try {
    // 第一次确认
    await ElMessageBox.confirm(
      '删除账号会连带删除关注的乐队、收藏的专辑、收藏的歌曲、参加的演唱会记录以及发表的所有乐评，此操作不可恢复，是否继续？',
      '警告',
      {
        confirmButtonText: '继续',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    
    // 第二次确认
    await ElMessageBox.confirm(
      '请再次确认：您确定要删除账号吗？这将永久删除所有相关数据！',
      '最终确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        distinguishCancelAndClose: true
      }
    )
    
    // 执行删除
    deleteLoading.value = true
    await request.delete('/fan/delete')
    
    ElMessage.success('账号删除成功')
    
    // 2秒后跳转到登录页
    setTimeout(() => {
      // 清除登录状态
      localStorage.removeItem('token')
      router.push('/login')
    }, 2000)
    
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('已取消删除')
    } else {
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    deleteLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.fan-profile {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
