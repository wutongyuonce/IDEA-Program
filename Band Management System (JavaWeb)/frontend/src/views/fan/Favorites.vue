<template>
  <div class="fan-favorites">
    <el-card>
      <template #header>
        <span>我的喜好</span>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 关注的乐队 -->
        <el-tab-pane label="关注的乐队" name="bands">
          <el-table :data="favoriteBands" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="乐队名称" width="200" />
            <el-table-column prop="foundedAt" label="成立时间" width="120">
              <template #default="{ row }">
                {{ formatDate(row.foundedAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="memberCount" label="成员数" width="100" />
            <el-table-column prop="intro" label="简介" show-overflow-tooltip />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleUnfollowBand(row)">
                  取消关注
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadFavoriteBands"
            @current-change="loadFavoriteBands"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-tab-pane>
        
        <!-- 收藏的专辑 -->
        <el-tab-pane label="收藏的专辑" name="albums">
          <el-table :data="favoriteAlbums" style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="专辑名称" width="200" />
            <el-table-column prop="bandName" label="乐队" width="150" />
            <el-table-column prop="releaseDate" label="发行日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.releaseDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="avgScore" label="评分" width="120">
              <template #default="{ row }">
                {{ row.avgScore ? row.avgScore.toFixed(1) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="copywriting" label="文案" show-overflow-tooltip />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleUnfavoriteAlbum(row)">
                  取消收藏
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadFavoriteAlbums"
            @current-change="loadFavoriteAlbums"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-tab-pane>
        
        <!-- 收藏的歌曲 -->
        <el-tab-pane label="收藏的歌曲" name="songs">
          <el-table :data="favoriteSongs" style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="歌曲名称" width="200" />
            <el-table-column prop="albumTitle" label="所属专辑" width="180" />
            <el-table-column prop="bandName" label="乐队" width="150" />
            <el-table-column prop="lyricist" label="词作者" width="120" />
            <el-table-column prop="composer" label="曲作者" width="120" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleUnfavoriteSong(row)">
                  取消收藏
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadFavoriteSongs"
            @current-change="loadFavoriteSongs"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-tab-pane>
        
        <!-- 参加的演唱会 -->
        <el-tab-pane label="参加的演唱会" name="concerts">
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="showAddConcertDialog">添加演唱会记录</el-button>
          </div>
          
          <el-table :data="attendedConcerts" style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="演唱会名称" width="200" />
            <el-table-column prop="bandName" label="乐队" width="150" />
            <el-table-column prop="location" label="场地" width="150" />
            <el-table-column prop="eventTime" label="演出日期" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.eventTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleRemoveAttendance(row)">
                  删除记录
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadAttendedConcerts"
            @current-change="loadAttendedConcerts"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 添加演唱会记录对话框 -->
    <el-dialog v-model="addConcertDialogVisible" title="添加演唱会记录" width="500px">
      <el-form :model="concertForm" label-width="100px">
        <el-form-item label="选择演唱会">
          <el-select v-model="concertForm.concertId" placeholder="请选择演唱会" filterable style="width: 100%">
            <el-option
              v-for="concert in allConcerts"
              :key="concert.concertId"
              :label="formatConcertLabel(concert)"
              :value="concert.concertId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addConcertDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddConcert">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const activeTab = ref('bands')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const favoriteBands = ref([])
const favoriteAlbums = ref([])
const favoriteSongs = ref([])
const attendedConcerts = ref([])
const allConcerts = ref([])

const addConcertDialogVisible = ref(false)
const concertForm = reactive({
  concertId: null
})

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const d = new Date(datetime)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const formatConcertLabel = (concert) => {
  const title = concert.title || '未命名演唱会'
  const bandName = concert.bandName || '未知乐队'
  const time = formatDateTime(concert.eventTime)
  return `${title} - ${bandName} (${time})`
}

const loadFavoriteBands = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/favorites/bands', {
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    favoriteBands.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载关注乐队失败:', error)
    ElMessage.error('加载关注乐队失败')
  } finally {
    loading.value = false
  }
}

const loadFavoriteAlbums = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/favorites/albums', {
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    favoriteAlbums.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载收藏专辑失败:', error)
    ElMessage.error('加载收藏专辑失败')
  } finally {
    loading.value = false
  }
}

const loadFavoriteSongs = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/favorites/songs', {
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    favoriteSongs.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载收藏歌曲失败:', error)
    ElMessage.error('加载收藏歌曲失败')
  } finally {
    loading.value = false
  }
}

const loadAttendedConcerts = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/favorites/concerts', {
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    attendedConcerts.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载参加演唱会失败:', error)
    ElMessage.error('加载参加演唱会失败')
  } finally {
    loading.value = false
  }
}

const loadAllConcerts = async () => {
  try {
    const res = await request.get('/fan/concerts/all', {
      params: {
        page: 1,
        size: 1000
      }
    })
    allConcerts.value = res.data.list || res.data.records || []
  } catch (error) {
    console.error('加载演唱会列表失败:', error)
    ElMessage.error('加载演唱会列表失败')
  }
}

const handleTabChange = (tabName) => {
  pagination.page = 1
  
  switch (tabName) {
    case 'bands':
      loadFavoriteBands()
      break
    case 'albums':
      loadFavoriteAlbums()
      break
    case 'songs':
      loadFavoriteSongs()
      break
    case 'concerts':
      loadAttendedConcerts()
      break
  }
}

const handleUnfollowBand = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要取消关注"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/fan/favorites/bands/${row.bandId}`)
    ElMessage.success('取消关注成功')
    loadFavoriteBands()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleUnfavoriteAlbum = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要取消收藏专辑"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/fan/favorites/albums/${row.albumId}`)
    ElMessage.success('取消收藏成功')
    loadFavoriteAlbums()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleUnfavoriteSong = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要取消收藏歌曲"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/fan/favorites/songs/${row.songId}`)
    ElMessage.success('取消收藏成功')
    loadFavoriteSongs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const showAddConcertDialog = async () => {
  await loadAllConcerts()
  concertForm.concertId = null
  addConcertDialogVisible.value = true
}

const handleAddConcert = async () => {
  if (!concertForm.concertId) {
    ElMessage.warning('请选择演唱会')
    return
  }
  
  try {
    await request.post('/fan/favorites/concerts', { concertId: concertForm.concertId })
    ElMessage.success('添加成功')
    addConcertDialogVisible.value = false
    loadAttendedConcerts()
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  }
}

const handleRemoveAttendance = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${row.title}"的参与记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/fan/favorites/concerts/${row.concertId}`)
    ElMessage.success('删除成功')
    loadAttendedConcerts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadFavoriteBands()
})
</script>

<style scoped>
.fan-favorites {
  height: 100%;
}
</style>
