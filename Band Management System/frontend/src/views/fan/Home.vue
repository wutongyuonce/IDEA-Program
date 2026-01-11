<template>
  <div class="fan-home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon :size="30"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.favoriteBandCount }}</div>
              <div class="stat-label">关注乐队</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon :size="30"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.favoriteAlbumCount }}</div>
              <div class="stat-label">收藏专辑</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon :size="30"><Headset /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.favoriteSongCount }}</div>
              <div class="stat-label">收藏歌曲</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <el-icon :size="30"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.reviewCount }}</div>
              <div class="stat-label">我的乐评</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>热门乐队推荐</span>
          </template>
          <el-table :data="recommendedBands" style="width: 100%">
            <el-table-column prop="name" label="乐队名称" />
            <el-table-column prop="memberCount" label="成员数" width="100" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleFollowBand(row)">
                  关注
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>专辑排行榜</span>
          </template>
          <el-table :data="topAlbums" style="width: 100%">
            <el-table-column prop="title" label="专辑名称" />
            <el-table-column prop="bandName" label="乐队" width="120" />
            <el-table-column prop="avgScore" label="评分" width="100">
              <template #default="{ row }">
                {{ row.avgScore ? row.avgScore.toFixed(1) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  v-if="!row.isFavorited" 
                  type="primary" 
                  size="small" 
                  @click="handleFavoriteAlbum(row)"
                >
                  收藏
                </el-button>
                <el-button 
                  v-else 
                  type="info" 
                  size="small" 
                  disabled
                >
                  已收藏
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>我的最新乐评</span>
          </template>
          <el-table :data="recentReviews" style="width: 100%">
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
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const statistics = ref({
  favoriteBandCount: 0,
  favoriteAlbumCount: 0,
  favoriteSongCount: 0,
  reviewCount: 0
})

const recommendedBands = ref([])
const topAlbums = ref([])
const recentReviews = ref([])

const loadStatistics = async () => {
  try {
    const res = await request.get('/fan/statistics')
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecommendedBands = async () => {
  try {
    const res = await request.get('/fan/discovery/bands', {
      params: { page: 1, size: 5 }
    })
    // 使用 list 或 records，取决于后端返回格式
    recommendedBands.value = res.data.list || res.data.records || []
  } catch (error) {
    console.error('加载推荐乐队失败:', error)
  }
}

const loadTopAlbums = async () => {
  try {
    const res = await request.get('/fan/discovery/albums/ranking', {
      params: { page: 1, size: 10 }
    })
    // 直接使用返回的列表，显示前10名
    topAlbums.value = res.data.list || res.data.records || []
  } catch (error) {
    console.error('加载排行榜失败:', error)
  }
}

const loadRecentReviews = async () => {
  try {
    const res = await request.get('/fan/reviews/my', {
      params: { page: 1, size: 3 }
    })
    recentReviews.value = res.data.list || res.data.records || []
  } catch (error) {
    console.error('加载乐评失败:', error)
  }
}

const handleFollowBand = async (band) => {
  try {
    await request.post('/fan/favorites/bands', { bandId: band.bandId })
    ElMessage.success('关注成功')
    loadRecommendedBands()
    loadStatistics()
  } catch (error) {
    ElMessage.error(error.message || '关注失败')
  }
}

const handleFavoriteAlbum = async (album) => {
  try {
    await request.post('/fan/favorites/albums', { albumId: album.albumId })
    ElMessage.success('收藏成功')
    // 更新专辑的收藏状态
    album.isFavorited = true
    // 刷新统计数据
    loadStatistics()
  } catch (error) {
    ElMessage.error(error.message || '收藏失败')
  }
}

const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  loadStatistics()
  loadRecommendedBands()
  loadTopAlbums()
  loadRecentReviews()
})
</script>

<style scoped>
.fan-home {
  padding: 0;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}
</style>
