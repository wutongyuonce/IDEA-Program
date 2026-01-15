<template>
  <div class="bands-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>乐队列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加乐队
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="乐队名称">
          <el-input v-model="searchForm.name" placeholder="请输入乐队名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="乐队名称" width="150" />
        <el-table-column prop="foundedAt" label="成立时间" width="120" />
        <el-table-column prop="leaderName" label="队长" width="120">
          <template #default="{ row }">
            {{ row.leaderName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="isDisbanded" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isDisbanded === 'Y'" type="info">已解散</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="disbandedAt" label="解散日期" width="120">
          <template #default="{ row }">
            {{ row.disbandedAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="intro" label="简介" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              v-if="row.isDisbanded !== 'Y'" 
              type="warning" 
              size="small" 
              @click="handleDisband(row)"
            >
              解散
            </el-button>
            <el-button 
              v-else 
              type="info" 
              size="small" 
              disabled
            >
              已解散
            </el-button>
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
        <el-form-item label="乐队名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入乐队名称" />
        </el-form-item>
        <el-form-item label="成立时间" prop="foundedAt">
          <el-date-picker
            v-model="formData.foundedAt"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="队长" prop="leaderMemberId">
          <el-select 
            v-model="formData.leaderMemberId" 
            :placeholder="formData.bandId ? '请选择队长（可选）' : '请先保存乐队后再设置队长'" 
            style="width: 100%"
            clearable
            filterable
            :disabled="!formData.bandId"
          >
            <el-option
              v-for="member in memberList"
              :key="member.memberId"
              :label="member.name"
              :value="member.memberId"
            />
          </el-select>
          <div v-if="!formData.bandId" style="color: #909399; font-size: 12px; margin-top: 4px;">
            新建乐队时无法设置队长，请先保存乐队并添加成员后再编辑设置队长
          </div>
          <div v-else-if="memberList.length === 0" style="color: #909399; font-size: 12px; margin-top: 4px;">
            该乐队暂无在队成员，请先添加成员
          </div>
        </el-form-item>
        <el-form-item label="解散日期" prop="disbandedAt">
          <el-date-picker
            v-model="formData.disbandedAt"
            type="date"
            placeholder="选择解散日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            :disabled="formData.isDisbanded !== 'Y'"
          />
          <div v-if="formData.isDisbanded !== 'Y'" style="color: #909399; font-size: 12px; margin-top: 4px;">
            乐队未解散，无法编辑解散日期
          </div>
          <div v-else style="color: #E6A23C; font-size: 12px; margin-top: 4px;">
            解散日期不能早于成立日期
          </div>
        </el-form-item>
        <el-form-item label="乐队简介" prop="intro">
          <el-input
            v-model="formData.intro"
            type="textarea"
            :rows="4"
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
import request from '../../api/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加乐队')
const formRef = ref(null)

const memberList = ref([])

const searchForm = reactive({
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  bandId: null,
  name: '',
  foundedAt: '',
  leaderMemberId: null,
  isDisbanded: 'N',
  disbandedAt: null,
  intro: ''
})

// 自定义验证器：解散日期不能早于成立日期
const validateDisbandedAt = (rule, value, callback) => {
  if (formData.isDisbanded === 'Y' && value) {
    if (formData.foundedAt && value < formData.foundedAt) {
      callback(new Error('解散日期不能早于成立日期'))
    } else {
      callback()
    }
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
  disbandedAt: [
    { validator: validateDisbandedAt, trigger: 'change' }
  ]
}

const loadMemberList = async (bandId = null) => {
  try {
    if (bandId) {
      // 编辑时：只加载该乐队的在队成员
      const res = await request.get('/admin/members', {
        params: {
          pageNum: 1,
          pageSize: 1000,
          bandId: bandId
        }
      })
      // 只显示在队成员（leaveDate为null）
      memberList.value = (res.data.list || []).filter(m => !m.leaveDate)
    } else {
      // 添加时：不显示成员列表（新乐队还没有成员）
      memberList.value = []
    }
  } catch (error) {
    console.error('加载成员列表失败:', error)
    memberList.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/bands', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        name: searchForm.name
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
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加乐队'
  Object.assign(formData, {
    bandId: null,
    name: '',
    foundedAt: '',
    leaderMemberId: null,
    isDisbanded: 'N',
    disbandedAt: null,
    intro: ''
  })
  // 添加乐队时不加载成员列表（新乐队还没有成员）
  loadMemberList(null)
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑乐队'
  Object.assign(formData, {
    bandId: row.bandId,
    name: row.name,
    foundedAt: row.foundedAt,
    leaderMemberId: row.leaderMemberId,
    isDisbanded: row.isDisbanded || 'N',
    disbandedAt: row.disbandedAt || null,
    intro: row.intro
  })
  // 编辑时只加载该乐队的在队成员
  await loadMemberList(row.bandId)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除乐队"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/admin/bands/${row.bandId}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDisband = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要解散乐队"${row.name}"吗？解散后将自动设置所有未离队成员为已离队状态。`,
      '解散乐队',
      {
        confirmButtonText: '确定解散',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.put(`/admin/bands/${row.bandId}/disband`)
    ElMessage.success('解散成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解散失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.bandId) {
          await request.put(`/admin/bands/${formData.bandId}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/admin/bands', formData)
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
.bands-page {
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
