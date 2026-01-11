<template>
  <div class="band-albums">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>专辑管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            发布专辑
          </el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="专辑名称" width="200" />
        <el-table-column prop="releaseDate" label="发行日期" width="120" />
        <el-table-column prop="avgScore" label="平均评分" width="120">
          <template #default="{ row }">
            {{ row.avgScore ? Number(row.avgScore).toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="copywriting" label="简介" show-overflow-tooltip />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="handleViewSongs(row)">查看歌曲</el-button>
            <el-button type="success" size="small" @click="handleViewReviews(row)">查看评论</el-button>
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="专辑名称" prop="title">
          <el-input v-model="formData.title" placeholder="请输入专辑名称" />
        </el-form-item>
        <el-form-item label="发行日期" prop="releaseDate">
          <el-date-picker
            v-model="formData.releaseDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="专辑简介" prop="copywriting">
          <el-input
            v-model="formData.copywriting"
            type="textarea"
            :rows="4"
            placeholder="请输入专辑简介"
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
    
    <!-- 查看歌曲对话框 -->
    <el-dialog
      v-model="songsDialogVisible"
      :title="`专辑《${currentAlbum.title}》的歌曲列表`"
      width="600px"
    >
      <el-table :data="songsList" style="width: 100%">
        <el-table-column prop="title" label="歌曲名称" />
      </el-table>
      
      <template #footer>
        <el-button @click="songsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 查看评论对话框 -->
    <el-dialog
      v-model="reviewsDialogVisible"
      :title="`专辑《${currentAlbum.title}》的评论列表`"
      width="800px"
    >
      <el-table :data="reviewsList" style="width: 100%" v-loading="reviewsLoading">
        <el-table-column prop="fanName" label="歌迷" width="120" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            {{ row.rating ? Number(row.rating).toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评论内容" show-overflow-tooltip />
        <el-table-column prop="reviewedAt" label="评论时间" width="180" />
      </el-table>
      
      <div v-if="reviewsList.length === 0" style="text-align: center; padding: 20px; color: #999;">
        暂无评论
      </div>
      
      <template #footer>
        <el-button @click="reviewsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const songsDialogVisible = ref(false)
const reviewsDialogVisible = ref(false)
const reviewsLoading = ref(false)
const dialogTitle = ref('发布专辑')
const formRef = ref(null)

const tableData = ref([])
const songsList = ref([])
const reviewsList = ref([])
const currentAlbum = ref({})

const formData = reactive({
  albumId: null,
  title: '',
  releaseDate: '',
  copywriting: ''
})

const rules = {
  title: [
    { required: true, message: '请输入专辑名称', trigger: 'blur' }
  ],
  releaseDate: [
    { required: true, message: '请选择发行日期', trigger: 'change' }
  ]
}

const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/band/albums')
    tableData.value = res.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '发布专辑'
  Object.assign(formData, {
    albumId: null,
    title: '',
    releaseDate: '',
    copywriting: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑专辑'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除专辑"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/band/albums/${row.albumId}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleViewSongs = async (row) => {
  currentAlbum.value = row
  try {
    const res = await request.get(`/band/albums/${row.albumId}/songs`)
    songsList.value = res.data || []
    songsDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载歌曲列表失败')
  }
}

const handleViewReviews = async (row) => {
  currentAlbum.value = row
  reviewsLoading.value = true
  reviewsDialogVisible.value = true
  try {
    // 调用后端API获取该专辑的评论列表
    const res = await request.get(`/band/albums/${row.albumId}/reviews`)
    reviewsList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载评论列表失败')
    reviewsList.value = []
  } finally {
    reviewsLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.albumId) {
          await request.put(`/band/albums/${formData.albumId}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/band/albums', formData)
          ElMessage.success('发布成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.band-albums {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
