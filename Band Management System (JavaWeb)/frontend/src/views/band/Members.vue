<template>
  <div class="band-members">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成员管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加成员
          </el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : row.gender === 'O' ? '其他' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="birthDate" label="出生日期" width="120" />
        <el-table-column prop="role" label="角色" width="150" />
        <el-table-column prop="joinDate" label="加入日期" width="120" />
        <el-table-column prop="leaveDate" label="离队日期" width="120">
          <template #default="{ row }">
            {{ row.leaveDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.leaveDate ? 'info' : 'success'">
              {{ row.leaveDate ? '已离队' : '在队' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
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
        <el-form-item label="成员姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入成员姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio label="M">男</el-radio>
            <el-radio label="F">女</el-radio>
            <el-radio label="O">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="formData.birthDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-input v-model="formData.role" placeholder="如：主唱、吉他手、鼓手等" />
        </el-form-item>
        <el-form-item label="加入日期" prop="joinDate">
          <el-date-picker
            v-model="formData.joinDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="离队日期">
          <el-date-picker
            v-model="formData.leaveDate"
            type="date"
            placeholder="选择日期（可选）"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            clearable
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
const dialogTitle = ref('添加成员')
const formRef = ref(null)

const tableData = ref([])

const formData = reactive({
  memberId: null,
  name: '',
  gender: '',
  birthDate: '',
  role: '',
  joinDate: '',
  leaveDate: ''
})

const rules = {
  name: [
    { required: true, message: '请输入成员姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  birthDate: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  role: [
    { required: true, message: '请输入角色', trigger: 'blur' }
  ],
  joinDate: [
    { required: true, message: '请选择加入日期', trigger: 'change' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/band/members')
    tableData.value = res.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加成员'
  Object.assign(formData, {
    memberId: null,
    name: '',
    gender: '',
    birthDate: '',
    role: '',
    joinDate: '',
    leaveDate: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑成员'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除成员"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/band/members/${row.memberId}`)
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
        if (formData.memberId) {
          await request.put(`/band/members/${formData.memberId}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/band/members', formData)
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
.band-members {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
