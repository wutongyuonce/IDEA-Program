<template>
  <div class="concerts-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>演唱会管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加演唱会
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="演唱会名称">
          <el-input v-model="searchForm.title" placeholder="请输入演唱会名称" clearable />
        </el-form-item>
        <el-form-item label="所属乐队">
          <el-select v-model="searchForm.bandId" placeholder="请选择乐队" clearable filterable>
            <el-option
              v-for="band in bandList"
              :key="band.bandId"
              :label="band.name"
              :value="band.bandId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="演唱会名称" width="250" show-overflow-tooltip />
        <el-table-column prop="bandName" label="所属乐队" width="150" />
        <el-table-column prop="location" label="场地" width="200" show-overflow-tooltip />
        <el-table-column prop="eventTime" label="演出时间" width="180" />
        <el-table-column prop="attendanceCount" label="参与人数" width="120" align="center" />
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
        <el-form-item label="所属乐队" prop="bandId">
          <el-select v-model="formData.bandId" placeholder="请选择乐队" style="width: 100%">
            <el-option
              v-for="band in bandList"
              :key="band.bandId"
              :label="band.name"
              :value="band.bandId"
            />
          </el-select>
        </el-form-item>
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
const dialogTitle = ref('添加演唱会')
const formRef = ref(null)

const bandList = ref([])

const searchForm = reactive({
  title: '',
  bandId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  concertId: null,
  bandId: null,
  title: '',
  location: '',
  eventTime: ''
})

const rules = {
  bandId: [
    { required: true, message: '请选择所属乐队', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入演唱会名称', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入场地', trigger: 'blur' }
  ],
  eventTime: [
    { required: true, message: '请选择演出时间', trigger: 'change' }
  ]
}

const loadBandList = async () => {
  try {
    const res = await request.get('/admin/bands/list')
    bandList.value = res.data || []
  } catch (error) {
    console.error('加载乐队列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/concerts', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        title: searchForm.title,
        bandId: searchForm.bandId
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
  searchForm.title = ''
  searchForm.bandId = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加演唱会'
  Object.assign(formData, {
    concertId: null,
    bandId: null,
    title: '',
    location: '',
    eventTime: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑演唱会'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除演唱会"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/admin/concerts/${row.concertId}`)
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
        if (formData.concertId) {
          await request.put(`/admin/concerts/${formData.concertId}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/admin/concerts', formData)
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
  loadBandList()
  loadData()
})
</script>

<style scoped>
.concerts-page {
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
