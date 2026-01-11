<template>
  <div class="band-home">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="band-info-card">
          <template #header>
            <div class="card-header">
              <span>乐队信息</span>
              <el-button type="primary" size="small" @click="handleEditInfo">编辑</el-button>
            </div>
          </template>
          <el-descriptions :column="2" border v-loading="loading">
            <el-descriptions-item label="乐队名称">{{ bandInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="成立时间">{{ bandInfo.foundedAt }}</el-descriptions-item>
            <el-descriptions-item label="成员数量">{{ bandInfo.memberCount }}</el-descriptions-item>
            <el-descriptions-item label="专辑数量">{{ statistics.albumCount }}</el-descriptions-item>
            <el-descriptions-item label="歌曲数量" :span="2">{{ statistics.songCount }}</el-descriptions-item>
            <el-descriptions-item label="乐队简介" :span="2">
              <div style="white-space: pre-wrap;">{{ bandInfo.intro || '暂无简介' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ bandInfo.memberCount }}</div>
              <div class="stat-label">乐队成员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon :size="30"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.albumCount }}</div>
              <div class="stat-label">发行专辑</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon :size="30"><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.fanCount }}</div>
              <div class="stat-label">关注歌迷</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最新专辑</span>
          </template>
          <el-table :data="recentAlbums" style="width: 100%">
            <el-table-column prop="title" label="专辑名称" />
            <el-table-column prop="releaseDate" label="发行日期" width="120" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最新演唱会</span>
          </template>
          <el-table :data="recentConcerts" style="width: 100%">
            <el-table-column prop="title" label="演唱会名称" show-overflow-tooltip />
            <el-table-column prop="eventTime" label="演出日期" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.eventTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="attendanceCount" label="参与人数" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 修改密码卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
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
      </el-col>
    </el-row>
    
    <!-- 删除乐队卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span style="color: #F56C6C;">危险操作</span>
          </template>
          
          <el-alert
            title="警告"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px;"
          >
            删除乐队是不可逆操作，将会连带删除以下数据：
            <ul style="margin: 10px 0 0 20px;">
              <li>所有专辑数据</li>
              <li>所有歌曲数据</li>
              <li>所有演唱会数据</li>
              <li>所有歌迷关注数据</li>
            </ul>
            <p style="margin-top: 10px; color: #E6A23C; font-weight: bold;">
              注意：如果乐队有成员，需要先删除所有成员才能删除乐队
            </p>
          </el-alert>
          
          <el-button type="danger" @click="handleDeleteBand" :loading="deleteLoading">
            删除乐队
          </el-button>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 编辑乐队信息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑乐队信息"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="乐队简介" prop="intro">
          <el-input
            v-model="formData.intro"
            type="textarea"
            :rows="6"
            placeholder="请输入乐队简介"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
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

const bandInfo = ref({
  bandId: null,
  name: '',
  foundedAt: '',
  memberCount: 0,
  intro: ''
})

const statistics = ref({
  albumCount: 0,
  songCount: 0,
  fanCount: 0
})

const recentAlbums = ref([])
const recentConcerts = ref([])

const formData = reactive({
  intro: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  intro: [
    { max: 500, message: '简介不能超过500字', trigger: 'blur' }
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

const loadBandInfo = async () => {
  loading.value = true
  try {
    const res = await request.get('/band/info')
    bandInfo.value = res.data || {}
    formData.intro = bandInfo.value.intro || ''
  } catch (error) {
    console.error('加载乐队信息失败:', error)
    ElMessage.error('加载乐队信息失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await request.get('/band/statistics')
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentData = async () => {
  try {
    const [albumsRes, concertsRes] = await Promise.all([
      request.get('/band/albums/recent'),
      request.get('/band/concerts/recent')
    ])
    recentAlbums.value = albumsRes.data || []
    recentConcerts.value = concertsRes.data || []
  } catch (error) {
    console.error('加载最新数据失败:', error)
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const handleEditInfo = () => {
  formData.intro = bandInfo.value.intro || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await request.put('/band/info', formData)
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadBandInfo()
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
        await request.put('/band/password', passwordForm)
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

const handleDeleteBand = async () => {
  try {
    // 第一次确认
    await ElMessageBox.confirm(
      '删除乐队会连带删除专辑、歌曲、演唱会以及歌迷关注的数据，此操作不可恢复，是否继续？',
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
      '请再次确认：您确定要删除乐队吗？这将永久删除所有相关数据！',
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
    await request.delete('/band/delete')
    
    ElMessage.success('乐队删除成功')
    
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
  loadBandInfo()
  loadStatistics()
  loadRecentData()
})
</script>

<style scoped>
.band-home {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.band-info-card {
  margin-bottom: 0;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}
</style>
