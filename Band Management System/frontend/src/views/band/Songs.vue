<template>
  <div class="band-songs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>歌曲管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加歌曲
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="歌曲名称">
          <el-input v-model="searchForm.title" placeholder="请输入歌曲名称" clearable />
        </el-form-item>
        <el-form-item label="所属专辑">
          <el-select v-model="searchForm.albumId" placeholder="请选择专辑" clearable filterable>
            <el-option
              v-for="album in albumList"
              :key="album.albumId"
              :label="album.title"
              :value="album.albumId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="歌曲名称" width="200" />
        <el-table-column prop="albumTitle" label="所属专辑" width="180" />
        <el-table-column prop="lyricist" label="作词" width="150" />
        <el-table-column prop="composer" label="作曲" width="150" />
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
        <el-form-item label="歌曲名称" prop="title">
          <el-input v-model="formData.title" placeholder="请输入歌曲名称" />
        </el-form-item>
        <el-form-item label="所属专辑" prop="albumId">
          <el-select v-model="formData.albumId" placeholder="请选择专辑" style="width: 100%" filterable>
            <el-option
              v-for="album in albumList"
              :key="album.albumId"
              :label="album.title"
              :value="album.albumId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作词" prop="lyricist">
          <el-input v-model="formData.lyricist" placeholder="请输入作词人" />
        </el-form-item>
        <el-form-item label="作曲" prop="composer">
          <el-input v-model="formData.composer" placeholder="请输入作曲人" />
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
const dialogTitle = ref('添加歌曲')
const formRef = ref(null)

const albumList = ref([])

const searchForm = reactive({
  title: '',
  albumId: null
})

const tableData = ref([])
const allSongsData = ref([]) // 存储所有歌曲数据

const formData = reactive({
  songId: null,
  title: '',
  albumId: null,
  lyricist: '',
  composer: ''
})

const rules = {
  title: [
    { required: true, message: '请输入歌曲名称', trigger: 'blur' }
  ],
  albumId: [
    { required: true, message: '请选择所属专辑', trigger: 'change' }
  ],
  lyricist: [
    { required: true, message: '请输入作词人', trigger: 'blur' }
  ],
  composer: [
    { required: true, message: '请输入作曲人', trigger: 'blur' }
  ]
}

const loadAlbumList = async () => {
  try {
    const res = await request.get('/band/albums')
    albumList.value = res.data || []
  } catch (error) {
    console.error('加载专辑列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/band/songs')
    // 后端返回的是List<Song>，不是分页对象
    allSongsData.value = res.data || []
    // 应用过滤
    filterData()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const filterData = () => {
  let filtered = [...allSongsData.value]
  
  // 按歌曲名称过滤
  if (searchForm.title) {
    filtered = filtered.filter(song => 
      song.title && song.title.toLowerCase().includes(searchForm.title.toLowerCase())
    )
  }
  
  // 按所属专辑过滤
  if (searchForm.albumId) {
    filtered = filtered.filter(song => song.albumId === searchForm.albumId)
  }
  
  tableData.value = filtered
}

const handleSearch = () => {
  filterData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.albumId = null
  filterData()
}

const handleAdd = () => {
  dialogTitle.value = '添加歌曲'
  Object.assign(formData, {
    songId: null,
    title: '',
    albumId: null,
    lyricist: '',
    composer: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑歌曲'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除歌曲"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/band/songs/${row.songId}`)
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
        if (formData.songId) {
          await request.put(`/band/songs/${formData.songId}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/band/songs', formData)
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
  loadAlbumList()
  loadData()
})
</script>

<style scoped>
.band-songs {
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
