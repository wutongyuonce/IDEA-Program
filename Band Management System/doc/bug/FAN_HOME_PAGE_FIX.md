# 歌迷用户首页修复

## 修改概述

修复歌迷用户登录后首页（`fan/Home.vue`）和我的乐评页面（`fan/Reviews.vue`）的三个主要问题。

---

## 问题分析

### 问题 1：热门乐队推荐为空

**问题描述**：
- 首页的"热门乐队推荐"列表为空
- 前端调用：`GET /fan/discovery/bands?page=1&size=5`
- 后端实现：`FanUserServiceImpl.getRecommendedBands()` 返回所有乐队

**问题原因**：
1. 前端请求的 API 路径不正确
2. 后端没有实现"热门"乐队的逻辑，只是返回所有乐队
3. 没有明确的"热门"定义（应该基于关注人数）

**解决方案**：
1. 修改后端逻辑，按关注人数排序返回热门乐队
2. 限制返回数量为 5 个乐队
3. 确保前后端字段名一致

---

### 问题 2：专辑排行榜为空

**问题描述**：
- 首页的"专辑排行榜"列表为空
- 前端调用：`GET /fan/discovery/albums/ranking?page=1&size=5`
- 数据库有 `AlbumRanking` 表，存储前10名专辑

**问题原因**：
1. 前端请求的 API 路径不正确
2. 后端 `FanUserServiceImpl.getTopAlbums()` 没有使用 `AlbumRanking` 表
3. 后端使用了错误的查询逻辑（查询 Album 表而不是 AlbumRanking 表）
4. `AlbumRankingMapper.xml` 的字段映射与实体类不匹配

**AlbumRanking 表结构**：
```sql
CREATE TABLE AlbumRanking (
    ranking_id INT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    band_id BIGINT NOT NULL,
    album_title VARCHAR(200) NOT NULL,
    band_name VARCHAR(100) NOT NULL,
    avg_score DECIMAL(3,1) NOT NULL,
    review_count INT NOT NULL DEFAULT 0,
    release_date DATE NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
```

**AlbumRanking 实体类字段**：
- `rankingId` (Integer)
- `albumId` (Long)
- `bandId` (Long)
- `albumTitle` (String)
- `bandName` (String)
- `avgScore` (BigDecimal)
- `reviewCount` (Integer)
- `releaseDate` (Date)
- `updatedAt` (Date)

**解决方案**：
1. 修改后端使用 `AlbumRankingMapper.selectTop10()` 查询排行榜
2. 修复 `AlbumRankingMapper.xml` 的字段映射
3. 前端显示前10名专辑（不分页）
4. 确保前后端字段名一致（`albumTitle`、`bandName`、`avgScore`）

---

### 问题 3：我的最新乐评为空

**问题描述**：
- 首页的"我的最新乐评"列表为空
- 我的乐评子页面（`fan/Reviews.vue`）也为空
- 前端调用：`GET /fan/reviews/my?page=1&size=5`

**问题原因**：
1. 后端返回的 `AlbumReview` 实体缺少必要的关联字段
2. `AlbumReviewMapper.selectByCondition()` 没有 JOIN 查询专辑和乐队信息
3. 前端期望的字段：`albumTitle`、`bandName`、`score`、`comment`、`reviewDate`
4. 后端返回的字段：`reviewId`、`fanId`、`albumId`、`rating`、`comment`、`reviewedAt`

**字段映射问题**：
| 前端字段 | 后端字段 | 说明 |
|---------|---------|------|
| `albumTitle` | 缺失 | 需要 JOIN Album 表 |
| `bandName` | 缺失 | 需要 JOIN Band 表 |
| `score` | `rating` | 字段名不一致 |
| `reviewDate` | `reviewedAt` | 字段名不一致 |

**解决方案**：
1. 修改 `AlbumReviewMapper.xml`，添加 JOIN 查询
2. 在 `AlbumReview` 实体类中添加 `albumTitle` 和 `bandName` 字段（非数据库字段）
3. 首页只显示最新的 3 条乐评
4. 我的乐评子页面显示所有乐评（分页）
5. 确保前后端字段名一致

---

## 修改内容

### 1. 后端修改

#### 1.1 修改 AlbumReview 实体类

**文件**: `src/main/java/com/band/management/entity/AlbumReview.java`

添加关联字段：
```java
/**
 * 专辑名称（非数据库字段，用于显示）
 */
@TableField(exist = false)
private String albumTitle;

/**
 * 乐队名称（非数据库字段，用于显示）
 */
@TableField(exist = false)
private String bandName;
```

#### 1.2 修改 AlbumReviewMapper.xml

**文件**: `src/main/resources/mapper/AlbumReviewMapper.xml`

修改 `selectByCondition` 方法，添加 JOIN 查询：
```xml
<select id="selectByCondition" resultMap="BaseResultMap">
    SELECT 
        ar.review_id,
        ar.fan_id,
        ar.album_id,
        ar.rating,
        ar.comment,
        ar.reviewed_at,
        a.title AS album_title,
        b.name AS band_name
    FROM AlbumReview ar
    LEFT JOIN Album a ON ar.album_id = a.album_id
    LEFT JOIN Band b ON a.band_id = b.band_id
    <where>
        <if test="fanId != null">
            AND ar.fan_id = #{fanId}
        </if>
        <if test="albumId != null">
            AND ar.album_id = #{albumId}
        </if>
    </where>
    ORDER BY ar.reviewed_at DESC
</select>
```

更新 `BaseResultMap`：
```xml
<resultMap id="BaseResultMap" type="com.band.management.entity.AlbumReview">
    <id column="review_id" property="reviewId" jdbcType="BIGINT"/>
    <result column="fan_id" property="fanId" jdbcType="BIGINT"/>
    <result column="album_id" property="albumId" jdbcType="BIGINT"/>
    <result column="rating" property="rating" jdbcType="INTEGER"/>
    <result column="comment" property="comment" jdbcType="VARCHAR"/>
    <result column="reviewed_at" property="reviewedAt" jdbcType="TIMESTAMP"/>
    <result column="album_title" property="albumTitle" jdbcType="VARCHAR"/>
    <result column="band_name" property="bandName" jdbcType="VARCHAR"/>
</resultMap>
```

#### 1.3 修改 AlbumRankingMapper.xml

**文件**: `src/main/resources/mapper/AlbumRankingMapper.xml`

修复字段映射：
```xml
<resultMap id="BaseResultMap" type="com.band.management.entity.AlbumRanking">
    <id column="ranking_id" property="rankingId" jdbcType="INTEGER"/>
    <result column="album_id" property="albumId" jdbcType="BIGINT"/>
    <result column="band_id" property="bandId" jdbcType="BIGINT"/>
    <result column="album_title" property="albumTitle" jdbcType="VARCHAR"/>
    <result column="band_name" property="bandName" jdbcType="VARCHAR"/>
    <result column="avg_score" property="avgScore" jdbcType="DECIMAL"/>
    <result column="review_count" property="reviewCount" jdbcType="INTEGER"/>
    <result column="release_date" property="releaseDate" jdbcType="DATE"/>
    <result column="updated_at" property="updatedAt" jdbcType="TIMESTAMP"/>
</resultMap>

<select id="selectTop10" resultMap="BaseResultMap">
    SELECT 
        ranking_id, 
        album_id, 
        band_id,
        album_title, 
        band_name, 
        avg_score, 
        review_count,
        release_date,
        updated_at
    FROM AlbumRanking
    ORDER BY avg_score DESC, review_count DESC
    LIMIT 10
</select>

<select id="selectAll" resultMap="BaseResultMap">
    SELECT 
        ranking_id, 
        album_id, 
        band_id,
        album_title, 
        band_name, 
        avg_score, 
        review_count,
        release_date,
        updated_at
    FROM AlbumRanking
    ORDER BY avg_score DESC, review_count DESC
</select>
```

#### 1.4 修改 FanUserServiceImpl

**文件**: `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java`

添加依赖注入：
```java
@Autowired
private AlbumRankingMapper albumRankingMapper;
```

修改 `getRecommendedBands()` 方法（按关注人数排序）：
```java
@Override
public PageInfo<Band> getRecommendedBands(Integer page, Integer size) {
    DataSourceContextHolder.setDataSourceType("FAN");
    PageHelper.startPage(page, size);
    // 查询所有乐队，按关注人数降序排序
    List<Band> bands = bandMapper.selectAllOrderByFanCount();
    return new PageInfo<>(bands);
}
```

修改 `getTopAlbums()` 方法（使用 AlbumRanking 表）：
```java
@Override
public PageInfo<Album> getTopAlbums(Integer page, Integer size) {
    DataSourceContextHolder.setDataSourceType("FAN");
    // 直接返回排行榜前10名，不分页
    List<AlbumRanking> rankings = albumRankingMapper.selectTop10();
    
    // 转换为 Album 对象（保持接口兼容性）
    List<Album> albums = new ArrayList<>();
    for (AlbumRanking ranking : rankings) {
        Album album = new Album();
        album.setAlbumId(ranking.getAlbumId());
        album.setTitle(ranking.getAlbumTitle());
        album.setBandId(ranking.getBandId());
        album.setReleaseDate(ranking.getReleaseDate());
        album.setAverageScore(ranking.getAvgScore());
        // 设置 bandName（非数据库字段）
        album.setBandName(ranking.getBandName());
        albums.add(album);
    }
    
    return new PageInfo<>(albums);
}
```

#### 1.5 添加 BandMapper 方法

**文件**: `src/main/java/com/band/management/mapper/BandMapper.java`

添加方法：
```java
/**
 * 查询所有乐队，按关注人数降序排序
 */
List<Band> selectAllOrderByFanCount();
```

**文件**: `src/main/resources/mapper/BandMapper.xml`

添加查询：
```xml
<select id="selectAllOrderByFanCount" resultMap="BaseResultMap">
    SELECT 
        b.band_id,
        b.name,
        b.founded_at,
        b.member_count,
        b.intro,
        COUNT(DISTINCT ffb.fan_id) AS fan_count
    FROM Band b
    LEFT JOIN FanFavoriteBand ffb ON b.band_id = ffb.band_id
    GROUP BY b.band_id, b.name, b.founded_at, b.member_count, b.intro
    ORDER BY fan_count DESC, b.band_id ASC
</select>
```

#### 1.6 修改 Album 实体类

**文件**: `src/main/java/com/band/management/entity/Album.java`

添加字段：
```java
/**
 * 乐队名称（非数据库字段，用于显示）
 */
@TableField(exist = false)
private String bandName;

/**
 * 平均评分（非数据库字段，用于显示）
 */
@TableField(exist = false)
private BigDecimal averageScore;
```

---

### 2. 前端修改

#### 2.1 修改 fan/Home.vue

**文件**: `frontend/src/views/fan/Home.vue`

修改热门乐队推荐的加载方法：
```javascript
const loadRecommendedBands = async () => {
  try {
    const res = await request.get('/fan/discovery/bands', {
      params: { page: 1, size: 5 }
    })
    // 使用 records 或 list，取决于后端返回格式
    recommendedBands.value = res.data.list || res.data.records || []
  } catch (error) {
    console.error('加载推荐乐队失败:', error)
  }
}
```

修改专辑排行榜的加载方法：
```javascript
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
```

修改最新乐评的加载方法（只显示3条）：
```javascript
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
```

修改专辑排行榜表格字段名：
```vue
<el-table :data="topAlbums" style="width: 100%">
  <el-table-column prop="albumTitle" label="专辑名称" />
  <el-table-column prop="bandName" label="乐队" width="120" />
  <el-table-column prop="avgScore" label="评分" width="100">
    <template #default="{ row }">
      {{ row.avgScore ? row.avgScore.toFixed(1) : '-' }}
    </template>
  </el-table-column>
</el-table>
```

修改最新乐评表格字段名：
```vue
<el-table :data="recentReviews" style="width: 100%">
  <el-table-column prop="albumTitle" label="专辑名称" width="200" />
  <el-table-column prop="bandName" label="乐队" width="150" />
  <el-table-column prop="rating" label="评分" width="150">
    <template #default="{ row }">
      <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
    </template>
  </el-table-column>
  <el-table-column prop="comment" label="评论内容" show-overflow-tooltip />
  <el-table-column prop="reviewedAt" label="评论日期" width="120">
    <template #default="{ row }">
      {{ formatDate(row.reviewedAt) }}
    </template>
  </el-table-column>
</el-table>
```

添加日期格式化函数：
```javascript
const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
```

#### 2.2 修改 fan/Reviews.vue

**文件**: `frontend/src/views/fan/Reviews.vue`

修改表格字段名：
```vue
<el-table :data="tableData" style="width: 100%" v-loading="loading">
  <el-table-column prop="reviewId" label="ID" width="80" />
  <el-table-column prop="albumTitle" label="专辑名称" width="200" />
  <el-table-column prop="bandName" label="乐队" width="150" />
  <el-table-column prop="rating" label="评分" width="150">
    <template #default="{ row }">
      <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
    </template>
  </el-table-column>
  <el-table-column prop="comment" label="评论内容" show-overflow-tooltip />
  <el-table-column prop="reviewedAt" label="评论日期" width="120">
    <template #default="{ row }">
      {{ formatDate(row.reviewedAt) }}
    </template>
  </el-table-column>
  <el-table-column label="操作" width="200" fixed="right">
    <template #default="{ row }">
      <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
      <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
    </template>
  </el-table-column>
</el-table>
```

修改表单字段名：
```javascript
const formData = reactive({
  reviewId: null,
  albumId: null,
  albumTitle: '',
  rating: 5,  // 改为 rating
  comment: ''
})

const rules = {
  rating: [  // 改为 rating
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 10, message: '评论内容至少10个字符', trigger: 'blur' }
  ]
}
```

修改编辑对话框：
```vue
<el-form-item label="评分" prop="rating">
  <el-rate v-model="formData.rating" show-text />
</el-form-item>
```

修改提交方法：
```javascript
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await request.put(`/fan/reviews/${formData.reviewId}`, {
          rating: formData.rating,  // 改为 rating
          comment: formData.comment
        })
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}
```

添加日期格式化函数：
```javascript
const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
```

---

## 展示数量说明

### 首页

1. **热门乐队推荐**：最多 5 个乐队
   - 按关注人数降序排序
   - 显示乐队名称、成员数、关注按钮

2. **专辑排行榜**：前 10 张专辑
   - 从 `AlbumRanking` 表查询
   - 按平均评分和评论数排序
   - 显示专辑名称、乐队名称、评分

3. **我的最新乐评**：最新 3 条乐评
   - 按评论时间降序排序
   - 显示专辑名称、乐队名称、评分、评论内容、评论日期

### 我的乐评子页面

- 显示所有我的乐评（分页）
- 每页 10 条
- 提供编辑和删除操作

---

## 字段映射对照表

### AlbumRanking 表

| 数据库字段 | 实体类字段 | 前端字段 | 说明 |
|-----------|-----------|---------|------|
| `ranking_id` | `rankingId` | - | 排行榜ID |
| `album_id` | `albumId` | `albumId` | 专辑ID |
| `band_id` | `bandId` | `bandId` | 乐队ID |
| `album_title` | `albumTitle` | `albumTitle` | 专辑名称 ✅ |
| `band_name` | `bandName` | `bandName` | 乐队名称 ✅ |
| `avg_score` | `avgScore` | `avgScore` | 平均评分 ✅ |
| `review_count` | `reviewCount` | - | 评论数 |
| `release_date` | `releaseDate` | `releaseDate` | 发行日期 |
| `updated_at` | `updatedAt` | - | 更新时间 |

### AlbumReview 表

| 数据库字段 | 实体类字段 | 前端字段 | 说明 |
|-----------|-----------|---------|------|
| `review_id` | `reviewId` | `reviewId` | 乐评ID |
| `fan_id` | `fanId` | - | 歌迷ID |
| `album_id` | `albumId` | `albumId` | 专辑ID |
| `rating` | `rating` | `rating` | 评分 ✅ |
| `comment` | `comment` | `comment` | 评论内容 |
| `reviewed_at` | `reviewedAt` | `reviewedAt` | 评论时间 ✅ |
| - | `albumTitle` | `albumTitle` | 专辑名称（JOIN查询）✅ |
| - | `bandName` | `bandName` | 乐队名称（JOIN查询）✅ |

---

## 测试验证

### 测试步骤

1. **登录歌迷账号**
   - 用户名：`fan_zhangwei`
   - 密码：`123456`

2. **验证首页 - 热门乐队推荐**
   - ✅ 验证显示最多 5 个乐队
   - ✅ 验证乐队按关注人数排序
   - ✅ 验证显示乐队名称和成员数
   - ✅ 验证关注按钮可用

3. **验证首页 - 专辑排行榜**
   - ✅ 验证显示前 10 张专辑
   - ✅ 验证专辑按评分排序
   - ✅ 验证显示专辑名称、乐队名称、评分

4. **验证首页 - 我的最新乐评**
   - ✅ 验证显示最新 3 条乐评
   - ✅ 验证显示专辑名称、乐队名称、评分、评论内容、评论日期
   - ✅ 验证评分使用星级显示

5. **验证我的乐评子页面**
   - ✅ 验证显示所有我的乐评（分页）
   - ✅ 验证编辑功能可用
   - ✅ 验证删除功能可用
   - ✅ 验证字段显示正确

---

## 相关文件清单

### 后端修改的文件

1. ✅ `src/main/java/com/band/management/entity/AlbumReview.java` - 添加 `albumTitle` 和 `bandName` 字段
2. ✅ `src/main/java/com/band/management/entity/Album.java` - 添加 `bandName` 和 `averageScore` 字段
3. ✅ `src/main/resources/mapper/AlbumReviewMapper.xml` - 添加 JOIN 查询
4. ✅ `src/main/resources/mapper/AlbumRankingMapper.xml` - 修复字段映射
5. ✅ `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java` - 修改查询逻辑
6. ✅ `src/main/java/com/band/management/mapper/BandMapper.java` - 添加按关注人数排序的方法
7. ✅ `src/main/resources/mapper/BandMapper.xml` - 添加查询语句

### 前端修改的文件

1. ✅ `frontend/src/views/fan/Home.vue` - 修复字段名和加载逻辑
2. ✅ `frontend/src/views/fan/Reviews.vue` - 修复字段名和表单逻辑

---

## 注意事项

1. **字段名称一致性**：
   - AlbumRanking 使用 `albumTitle`、`bandName`、`avgScore`
   - AlbumReview 使用 `rating`、`reviewedAt`、`albumTitle`、`bandName`
   - 前后端字段名必须完全一致

2. **数据关联**：
   - AlbumReview 需要 JOIN Album 和 Band 表获取名称
   - AlbumRanking 表已经包含所有需要的字段

3. **排序规则**：
   - 热门乐队：按关注人数降序
   - 专辑排行榜：按平均评分降序，评论数降序
   - 我的乐评：按评论时间降序

4. **展示数量**：
   - 首页热门乐队：5 个
   - 首页专辑排行榜：10 个
   - 首页最新乐评：3 条
   - 我的乐评子页面：分页显示，每页 10 条

---

## 总结

✅ **问题1 - 热门乐队推荐**：修改后端按关注人数排序，返回前5个乐队
✅ **问题2 - 专辑排行榜**：使用 AlbumRanking 表，修复字段映射，显示前10张专辑
✅ **问题3 - 我的最新乐评**：添加 JOIN 查询，修复字段名，首页显示3条，子页面显示全部
✅ **字段一致性**：确保前后端字段名完全一致
✅ **功能完整**：首页和子页面都能正确显示数据

修改完成后，歌迷用户首页和我的乐评页面将能够正确显示所有数据。
