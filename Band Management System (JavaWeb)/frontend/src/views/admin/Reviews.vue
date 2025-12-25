<template>
  <div class="reviews-page">
    <el-card>
      <template #header>
        <span>乐评管理</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="专辑名称">
          <el-input v-model="searchForm.albumTitle" placeholder="请输入专辑名称" clearable />
        </el-form-item>
        <el-form-item label="歌迷姓名">
          <el-input v-model="searchForm.fanName" placeholder="请输入歌迷姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="albumTitle" label="专辑名称" width="180" />
        <el-table-column prop="bandName" label="乐队" width="150" />
        <el-table-column prop="fanName" label="歌迷" width="120" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            {{ row.rating }}
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评论内容" show-overflow-tooltip />
        <el-table-column prop="reviewedAt" label="评论日期" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
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
    
    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="乐评详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="专辑名称">{{ currentReview.albumTitle }}</el-descriptions-item>
        <el-descriptions-item label="所属乐队">{{ currentReview.bandName }}</el-descriptions-item>
        <el-descriptions-item label="歌迷姓名">{{ currentReview.fanName }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          {{ currentReview.rating }}
        </el-descriptions-item>
        <el-descriptions-item label="评论日期">{{ currentReview.reviewedAt }}</el-descriptions-item>
        <el-descriptions-item label="评论内容">
          <div style="white-space: pre-wrap;">{{ currentReview.comment }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const dialogVisible = ref(false)

const searchForm = reactive({
  albumTitle: '',
  fanName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const currentReview = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/reviews', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        albumTitle: searchForm.albumTitle,
        fanName: searchForm.fanName
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
  searchForm.albumTitle = ''
  searchForm.fanName = ''
  handleSearch()
}

const handleView = (row) => {
  currentReview.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除该乐评吗？\n专辑：${row.albumTitle}\n歌迷：${row.fanName}`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.delete(`/admin/reviews/${row.reviewId}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.reviews-page {
  height: 100%;
}

.search-form {
  margin-bottom: 20px;
}
</style>
