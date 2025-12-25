<template>
  <div class="fans-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>歌迷管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加歌迷
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="歌迷姓名">
          <el-input v-model="searchForm.name" placeholder="请输入歌迷姓名" clearable />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="searchForm.gender" placeholder="请选择性别" clearable filterable>
            <el-option label="男" value="M" />
            <el-option label="女" value="F" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '其他' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="occupation" label="职业" width="150" />
        <el-table-column prop="education" label="学历" width="120" />
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
            <el-option label="大专" value="大专" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加歌迷')
const formRef = ref(null)

const searchForm = reactive({
  name: '',
  gender: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  fanId: null,
  name: '',
  gender: 'M',
  age: 20,
  occupation: '',
  education: ''
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

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/fans', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        name: searchForm.name,
        gender: searchForm.gender
      }
    })
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.gender = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加歌迷'
  Object.assign(formData, {
    fanId: null,
    name: '',
    gender: 'M',
    age: 20,
    occupation: '',
    education: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑歌迷'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除歌迷"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/admin/fans/${row.fanId}`)
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
        if (formData.fanId) {
          await request.put(`/admin/fans/${formData.fanId}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/admin/fans', formData)
          ElMessage.success('添加成功')
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
.fans-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
