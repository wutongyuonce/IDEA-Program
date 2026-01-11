<template>
  <div class="band-concerts">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>演唱会管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            发布演唱会
          </el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="演唱会名称" width="250" show-overflow-tooltip />
        <el-table-column prop="location" label="场地" width="200" show-overflow-tooltip />
        <el-table-column prop="eventTime" label="演出日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.eventTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="attendanceCount" label="参与人数" width="120" align="center" />
        <el-table-column label="操作" width="180" fixed="right">
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="演唱会名称" prop="title">
          <el-input v-model="formData.title" placeholder="请输入演唱会名称" />
        </el-form-item>
        <el-form-item label="场地" prop="location">
          <el-input v-model="formData.location" placeholder="请输入场地" />
        </el-form-item>
        <el-form-item label="演出时间" prop="eventTime">
          <el-date-picker
            v-model="formData.eventTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
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
const dialogTitle = ref('发布演唱会')
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  concertId: null,
  title: '',
  location: '',
  eventTime: ''
})

const rules = {
  title: [
    { required: true, message: '请输入演唱会名称', trigger: 'blur' },
    { max: 200, message: '演唱会名称不能超过200个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入场地', trigger: 'blur' },
    { max: 200, message: '场地不能超过200个字符', trigger: 'blur' }
  ],
  eventTime: [
    { required: true, message: '请选择演出时间', trigger: 'change' }
  ]
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/band/concerts')
    tableData.value = res.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '发布演唱会'
  Object.assign(formData, {
    concertId: null,
    title: '',
    location: '',
    eventTime: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑演唱会'
  Object.assign(formData, {
    concertId: row.concertId,
    title: row.title,
    location: row.location,
    eventTime: row.eventTime
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除演唱会"${row.title}"吗？删除后相关的参与记录也会一并删除。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/band/concerts/${row.concertId}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.concertId) {
          await request.put(`/band/concerts/${formData.concertId}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/band/concerts', formData)
          ElMessage.success('发布成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('操作失败:', error)
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
.band-concerts {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
