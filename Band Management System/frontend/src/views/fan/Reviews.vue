<template>
  <div class="fan-reviews">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的乐评</span>
          <el-button type="primary" @click="handleAdd">添加乐评</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="albumTitle" label="专辑名称" width="200" />
        <el-table-column prop="bandName" label="乐队" width="150" />
        <el-table-column prop="rating" label="评分" width="100" align="center">
          <template #default="{ row }">
            {{ row.rating ? row.rating.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评论内容" show-overflow-tooltip />
        <el-table-column prop="reviewedAt" label="评论日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.reviewedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 编辑/添加乐评对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑乐评' : '添加乐评'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="专辑名称" prop="albumId" v-if="!isEdit">
          <el-select 
            v-model="formData.albumId" 
            placeholder="请选择专辑"
            filterable
            @change="handleAlbumChange"
            style="width: 100%"
          >
            <el-option
              v-for="album in albumList"
              :key="album.albumId"
              :label="album.title"
              :value="album.albumId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专辑名称" v-else>
          <el-input v-model="formData.albumTitle" disabled />
        </el-form-item>
        <el-form-item label="乐队" v-if="formData.bandName">
          <el-input v-model="formData.bandName" disabled />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-input-number
            v-model="formData.rating"
            :min="0"
            :max="10"
            :step="0.5"
            :precision="1"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999;">（0-10分，步长0.5）</span>
        </el-form-item>
        <el-form-item label="评论内容" prop="comment">
          <el-input
            v-model="formData.comment"
            type="textarea"
            :rows="6"
            placeholder="请输入您的评论..."
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
import request from '../../api/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const albumList = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  reviewId: null,
  albumId: null,
  albumTitle: '',
  bandName: '',
  rating: 5,
  comment: ''
})

const rules = {
  albumId: [
    { required: true, message: '请选择专辑', trigger: 'change' }
  ],
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' },
    { type: 'number', min: 0, max: 10, message: '评分范围为0-10', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 10, message: '评论内容至少10个字符', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/reviews/my', {
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    tableData.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadAlbums = async () => {
  try {
    const res = await request.get('/fan/discovery/albums', {
      params: { page: 1, size: 1000 }
    })
    albumList.value = res.data.list || res.data.records || []
  } catch (error) {
    console.error('加载专辑列表失败:', error)
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    reviewId: null,
    albumId: null,
    albumTitle: '',
    bandName: '',
    rating: 5,
    comment: ''
  })
  loadAlbums()
  dialogVisible.value = true
}

const handleAlbumChange = (albumId) => {
  const album = albumList.value.find(a => a.albumId === albumId)
  if (album) {
    formData.albumTitle = album.title
    formData.bandName = album.bandName
  }
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除对专辑《${row.albumTitle}》的乐评吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.delete(`/fan/reviews/${row.reviewId}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          // 编辑乐评
          await request.put(`/fan/reviews/${formData.reviewId}`, {
            rating: formData.rating,
            comment: formData.comment
          })
          ElMessage.success('更新成功')
        } else {
          // 添加乐评
          await request.post('/fan/reviews', {
            albumId: formData.albumId,
            rating: formData.rating,
            comment: formData.comment
          })
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || (isEdit.value ? '更新失败' : '添加失败'))
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.fan-reviews {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
