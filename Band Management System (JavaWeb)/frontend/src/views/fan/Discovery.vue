<template>
  <div class="fan-discovery">
    <el-card>
      <template #header>
        <span>发现与浏览</span>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 发现乐队 -->
        <el-tab-pane label="发现乐队" name="bands">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="乐队名称">
              <el-input v-model="searchForm.name" placeholder="请输入乐队名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="bandList" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="乐队名称" width="200" />
            <el-table-column prop="foundedAt" label="成立时间" width="120">
              <template #default="{ row }">
                {{ formatDate(row.foundedAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="memberCount" label="成员数" width="100" />
            <el-table-column prop="intro" label="简介" show-overflow-tooltip />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleFollowBand(row)">
                  关注
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
            @size-change="loadBands"
            @current-change="loadBands"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-tab-pane>
        
        <!-- 发现专辑 -->
        <el-tab-pane label="发现专辑" name="albums">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="专辑名称">
              <el-input v-model="searchForm.title" placeholder="请输入专辑名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="albumList" style="width: 100%" v-loading="loading">
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
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleFavoriteAlbum(row)">
                  收藏
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
            @size-change="loadAlbums"
            @current-change="loadAlbums"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-tab-pane>
        
        <!-- 发现歌曲 -->
        <el-tab-pane label="发现歌曲" name="songs">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="歌曲名称">
              <el-input v-model="searchForm.songTitle" placeholder="请输入歌曲名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="songList" style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="歌曲名称" width="200" />
            <el-table-column prop="albumTitle" label="专辑" width="180" />
            <el-table-column prop="bandName" label="乐队" width="150" />
            <el-table-column prop="lyricist" label="词作者" width="120" />
            <el-table-column prop="composer" label="曲作者" width="120" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleFavoriteSong(row)">
                  收藏
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
            @size-change="loadSongs"
            @current-change="loadSongs"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const activeTab = ref('bands')

const searchForm = reactive({
  name: '',
  title: '',
  songTitle: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const bandList = ref([])
const albumList = ref([])
const songList = ref([])

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const loadBands = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/discovery/bands', {
      params: {
        page: pagination.page,
        size: pagination.size,
        name: searchForm.name
      }
    })
    bandList.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载乐队失败:', error)
    ElMessage.error('加载乐队失败')
  } finally {
    loading.value = false
  }
}

const loadAlbums = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/discovery/albums', {
      params: {
        page: pagination.page,
        size: pagination.size,
        title: searchForm.title
      }
    })
    albumList.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载专辑失败:', error)
    ElMessage.error('加载专辑失败')
  } finally {
    loading.value = false
  }
}

const loadSongs = async () => {
  loading.value = true
  try {
    const res = await request.get('/fan/discovery/songs', {
      params: {
        page: pagination.page,
        size: pagination.size,
        title: searchForm.songTitle
      }
    })
    songList.value = res.data.list || res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载歌曲失败:', error)
    ElMessage.error('加载歌曲失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tabName) => {
  pagination.page = 1
  searchForm.name = ''
  searchForm.title = ''
  searchForm.songTitle = ''
  
  switch (tabName) {
    case 'bands':
      loadBands()
      break
    case 'albums':
      loadAlbums()
      break
    case 'songs':
      loadSongs()
      break
  }
}

const handleSearch = () => {
  pagination.page = 1
  if (activeTab.value === 'bands') {
    loadBands()
  } else if (activeTab.value === 'albums') {
    loadAlbums()
  } else if (activeTab.value === 'songs') {
    loadSongs()
  }
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.title = ''
  searchForm.songTitle = ''
  handleSearch()
}

const handleFollowBand = async (band) => {
  try {
    await request.post('/fan/favorites/bands', { bandId: band.bandId })
    ElMessage.success('关注成功')
    // 从列表中移除该乐队
    bandList.value = bandList.value.filter(b => b.bandId !== band.bandId)
    pagination.total = Math.max(0, pagination.total - 1)
  } catch (error) {
    ElMessage.error(error.message || '关注失败')
  }
}

const handleFavoriteAlbum = async (album) => {
  try {
    await request.post('/fan/favorites/albums', { albumId: album.albumId })
    ElMessage.success('收藏成功')
    // 从列表中移除该专辑
    albumList.value = albumList.value.filter(a => a.albumId !== album.albumId)
    pagination.total = Math.max(0, pagination.total - 1)
  } catch (error) {
    ElMessage.error(error.message || '收藏失败')
  }
}

const handleFavoriteSong = async (song) => {
  try {
    await request.post('/fan/favorites/songs', { songId: song.songId })
    ElMessage.success('收藏成功')
    // 从列表中移除该歌曲
    songList.value = songList.value.filter(s => s.songId !== song.songId)
    pagination.total = Math.max(0, pagination.total - 1)
  } catch (error) {
    ElMessage.error(error.message || '收藏失败')
  }
}

onMounted(() => {
  loadBands()
})
</script>

<style scoped>
.fan-discovery {
  height: 100%;
}

.search-form {
  margin-bottom: 20px;
}
</style>
