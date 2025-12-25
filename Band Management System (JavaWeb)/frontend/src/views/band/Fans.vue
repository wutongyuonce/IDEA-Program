<template>
  <div class="band-fans">
    <!-- 第一部分：喜欢本乐队的歌迷 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>喜欢本乐队的歌迷</span>
          <el-tag type="primary" size="large">总人数: {{ bandFansCount }}</el-tag>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409EFF;">
                <el-icon :size="30"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalFans }}</div>
                <div class="stat-label">关注歌迷总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67C23A;">
                <el-icon :size="30"><Male /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.maleFans }}</div>
                <div class="stat-label">男性歌迷</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #F56C6C;">
                <el-icon :size="30"><Female /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.femaleFans }}</div>
                <div class="stat-label">女性歌迷</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #E6A23C;">
                <el-icon :size="30"><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.avgAge }}</div>
                <div class="stat-label">平均年龄</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>歌迷年龄分布</span>
            </template>
            <el-table :data="ageDistribution" style="width: 100%">
              <el-table-column prop="ageRange" label="年龄段" />
              <el-table-column prop="count" label="人数" />
              <el-table-column prop="percentage" label="占比" width="100">
                <template #default="{ row }">
                  {{ row.percentage }}%
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>歌迷学历分布</span>
            </template>
            <el-table :data="educationDistribution" style="width: 100%">
              <el-table-column prop="education" label="学历" />
              <el-table-column prop="count" label="人数" />
              <el-table-column prop="percentage" label="占比" width="100">
                <template #default="{ row }">
                  {{ row.percentage }}%
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 第二部分：喜欢乐队专辑的歌迷 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>喜欢乐队专辑的歌迷</span>
          <el-tag type="success" size="large">总人数: {{ albumFansData.totalFans }}</el-tag>
        </div>
      </template>
      
      <el-table :data="albumFansData.albums" style="width: 100%" v-loading="loadingAlbums">
        <el-table-column prop="albumTitle" label="专辑名称" width="300" show-overflow-tooltip />
        <el-table-column prop="fanCount" label="喜欢人数" width="150" align="center" />
      </el-table>
    </el-card>

    <!-- 第三部分：喜欢乐队歌曲的歌迷 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>喜欢乐队歌曲的歌迷</span>
          <el-tag type="warning" size="large">总人数: {{ songFansData.totalFans }}</el-tag>
        </div>
      </template>
      
      <el-table :data="songFansData.songs" style="width: 100%" v-loading="loadingSongs">
        <el-table-column prop="songTitle" label="歌曲名称" width="250" show-overflow-tooltip />
        <el-table-column prop="albumTitle" label="所属专辑" width="200" show-overflow-tooltip />
        <el-table-column prop="fanCount" label="喜欢人数" width="150" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const loadingAlbums = ref(false)
const loadingSongs = ref(false)

const statistics = ref({
  totalFans: 0,
  maleFans: 0,
  femaleFans: 0,
  avgAge: 0
})

const ageDistribution = ref([])
const educationDistribution = ref([])

const albumFansData = ref({
  totalFans: 0,
  albums: []
})

const songFansData = ref({
  totalFans: 0,
  songs: []
})

// 计算喜欢本乐队的歌迷总数
const bandFansCount = computed(() => statistics.value.totalFans)

const loadStatistics = async () => {
  try {
    const res = await request.get('/band/fans/statistics')
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadDistributions = async () => {
  try {
    const [ageRes, eduRes] = await Promise.all([
      request.get('/band/fans/age-distribution'),
      request.get('/band/fans/education-distribution')
    ])
    ageDistribution.value = ageRes.data || []
    educationDistribution.value = eduRes.data || []
  } catch (error) {
    console.error('加载分布数据失败:', error)
  }
}

const loadAlbumFans = async () => {
  loadingAlbums.value = true
  try {
    const res = await request.get('/band/fans/albums')
    albumFansData.value = res.data || { totalFans: 0, albums: [] }
  } catch (error) {
    console.error('加载专辑歌迷数据失败:', error)
    ElMessage.error('加载专辑歌迷数据失败')
  } finally {
    loadingAlbums.value = false
  }
}

const loadSongFans = async () => {
  loadingSongs.value = true
  try {
    const res = await request.get('/band/fans/songs')
    songFansData.value = res.data || { totalFans: 0, songs: [] }
  } catch (error) {
    console.error('加载歌曲歌迷数据失败:', error)
    ElMessage.error('加载歌曲歌迷数据失败')
  } finally {
    loadingSongs.value = false
  }
}

onMounted(() => {
  loadStatistics()
  loadDistributions()
  loadAlbumFans()
  loadSongFans()
})
</script>

<style scoped>
.band-fans {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
