# 歌迷中心首页最终修复

## 修改概述

对歌迷中心首页（`fan/Home.vue`）进行了以下三个问题的修复：
1. 我的最新乐评评分改用数字显示
2. 确认专辑排行榜正确显示 AlbumRanking 表数据
3. 热门乐队推荐按关注人数排序，排除已关注乐队，关注后自动刷新

---

## 修改内容

### 1. 我的最新乐评评分改用数字显示 ✅

**问题**：评分使用星级显示（el-rate 组件）

**解决方案**：改用数字显示，保留1位小数

**修改内容**：

**修改前**：
```vue
<el-table-column prop="rating" label="评分" width="150">
  <template #default="{ row }">
    <el-rate 
      v-model="row.rating" 
      disabled 
      show-score 
      text-color="#ff9900"
      :max="10"
      :allow-half="true"
    />
  </template>
</el-table-column>
```

**修改后**：
```vue
<el-table-column prop="rating" label="评分" width="100" align="center">
  <template #default="{ row }">
    {{ row.rating ? row.rating.toFixed(1) : '-' }}
  </template>
</el-table-column>
```

**效果**：
- 评分显示为数字（如：9.5）
- 列宽从 150px 改为 100px
- 居中对齐

---

### 2. 专辑排行榜数据验证 ✅

**问题**：需要确认专辑排行榜是否正确显示 AlbumRanking 表的前10条数据

**验证结果**：
- ✅ 后端 `FanUserServiceImpl.getTopAlbums()` 正确使用 `albumRankingMapper.selectTop10()`
- ✅ `AlbumRankingMapper.xml` 正确查询 AlbumRanking 表
- ✅ 排序规则：按 `avg_score DESC, review_count DESC`
- ✅ 字段映射正确：`albumTitle`、`bandName`、`avgScore`
- ✅ 前端正确显示字段

**数据流程**：
1. 前端调用：`GET /api/fan/discovery/albums/ranking?page=1&size=10`
2. 后端查询：`SELECT * FROM AlbumRanking ORDER BY avg_score DESC, review_count DESC LIMIT 10`
3. 转换为 Album 对象（保持接口兼容性）
4. 前端显示：专辑名称、乐队名称、评分

**结论**：专辑排行榜功能正常，正确显示平均评分最高的10张专辑。

---

### 3. 热门乐队推荐优化 ✅

**问题**：
1. 需要按关注人数排序
2. 需要排除当前用户已关注的乐队
3. 关注后需要自动刷新列表

**解决方案**：

#### 3.1 后端修改

**FanUserService.java** - 修改接口方法签名：
```java
/**
 * 获取推荐乐队列表（分页）
 * 排除已关注的乐队，按关注人数排序
 */
PageInfo<Band> getRecommendedBands(Long fanId, Integer page, Integer size);
```

**FanUserServiceImpl.java** - 修改实现：
```java
@Override
public PageInfo<Band> getRecommendedBands(Long fanId, Integer page, Integer size) {
    DataSourceContextHolder.setDataSourceType("FAN");
    PageHelper.startPage(page, size);
    
    // 查询未关注的乐队，按关注人数降序排序
    List<Band> bands = bandMapper.selectUnfollowedBandsByFanCount(fanId);
    return new PageInfo<>(bands);
}
```

**BandMapper.java** - 添加新方法：
```java
/**
 * 查询未关注的乐队，按关注人数降序排序
 */
List<Band> selectUnfollowedBandsByFanCount(@Param("fanId") Long fanId);
```

**BandMapper.xml** - 添加 SQL 查询：
```xml
<!-- 查询未关注的乐队，按关注人数降序排序 -->
<select id="selectUnfollowedBandsByFanCount" resultMap="BaseResultMap">
    SELECT 
        b.band_id,
        b.name,
        b.founded_at,
        b.member_count,
        b.intro,
        b.created_at,
        b.updated_at,
        COUNT(DISTINCT ffb.fan_id) AS fan_count
    FROM Band b
    LEFT JOIN FanFavoriteBand ffb ON b.band_id = ffb.band_id
    WHERE b.band_id NOT IN (
        SELECT band_id 
        FROM FanFavoriteBand 
        WHERE fan_id = #{fanId}
    )
    GROUP BY b.band_id, b.name, b.founded_at, b.member_count, b.intro, b.created_at, b.updated_at
    ORDER BY fan_count DESC, b.band_id ASC
</select>
```

**关键逻辑**：
1. 使用 `NOT IN` 排除当前用户已关注的乐队
2. 使用 `LEFT JOIN` 和 `COUNT` 统计每个乐队的关注人数
3. 按关注人数降序排序
4. 如果关注人数相同，按 band_id 升序排序

**FanUserController.java** - 传递 fanId 参数：
```java
@GetMapping("/discovery/bands")
public Result<PageInfo<Band>> getRecommendedBands(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size) {
    Long fanId = getCurrentFanId();
    log.info("歌迷用户获取推荐乐队: fanId={}, page={}, size={}", fanId, page, size);
    PageInfo<Band> bands = fanUserService.getRecommendedBands(fanId, page, size);
    return Result.success(bands);
}
```

#### 3.2 前端修改

前端代码无需修改，因为：
1. `handleFollowBand()` 方法在关注成功后已经调用 `loadRecommendedBands()`
2. `loadRecommendedBands()` 会重新请求后端 API
3. 后端返回的列表已经排除了已关注的乐队

**现有代码**：
```javascript
const handleFollowBand = async (band) => {
  try {
    await request.post('/fan/favorites/bands', { bandId: band.bandId })
    ElMessage.success('关注成功')
    loadRecommendedBands()  // 自动刷新列表
    loadStatistics()        // 更新统计数据
  } catch (error) {
    ElMessage.error(error.message || '关注失败')
  }
}
```

---

## 功能说明

### 1. 我的最新乐评

**显示内容**：
- 专辑名称（200px）
- 乐队名称（150px）
- 评分（100px，居中，数字格式）
- 评论内容（自适应，超长省略）
- 评论日期（120px，格式：YYYY-MM-DD）

**数据来源**：
- API：`GET /api/fan/reviews/my?page=1&size=3`
- 按评论时间降序排序
- 显示最新3条

### 2. 专辑排行榜

**显示内容**：
- 专辑名称
- 乐队名称（120px）
- 评分（100px，数字格式，保留1位小数）

**数据来源**：
- API：`GET /api/fan/discovery/albums/ranking?page=1&size=10`
- 数据库表：AlbumRanking
- 排序规则：按平均评分降序，评论数降序
- 显示前10名

**排行榜更新**：
- AlbumRanking 表由触发器自动维护
- 每次添加/更新/删除乐评时自动更新排行榜

### 3. 热门乐队推荐

**显示内容**：
- 乐队名称
- 成员数（100px）
- 关注按钮（100px）

**数据来源**：
- API：`GET /api/fan/discovery/bands?page=1&size=5`
- 排除当前用户已关注的乐队
- 按关注人数降序排序
- 显示前5个

**关注流程**：
1. 点击"关注"按钮
2. 调用 API：`POST /api/fan/favorites/bands`
3. 关注成功后：
   - 显示成功提示
   - 自动刷新推荐列表（该乐队自动移除）
   - 更新统计数据（关注乐队数+1）
4. 如果未关注乐队少于5个，则显示所有未关注乐队

---

## 数据库查询说明

### 1. 专辑排行榜查询

```sql
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
```

**说明**：
- 直接查询 AlbumRanking 表
- 按平均评分降序，评论数降序
- 限制返回10条

### 2. 热门乐队推荐查询

```sql
SELECT 
    b.band_id,
    b.name,
    b.founded_at,
    b.member_count,
    b.intro,
    b.created_at,
    b.updated_at,
    COUNT(DISTINCT ffb.fan_id) AS fan_count
FROM Band b
LEFT JOIN FanFavoriteBand ffb ON b.band_id = ffb.band_id
WHERE b.band_id NOT IN (
    SELECT band_id 
    FROM FanFavoriteBand 
    WHERE fan_id = ?
)
GROUP BY b.band_id, b.name, b.founded_at, b.member_count, b.intro, b.created_at, b.updated_at
ORDER BY fan_count DESC, b.band_id ASC
LIMIT 5
```

**说明**：
- 排除当前用户已关注的乐队（NOT IN 子查询）
- 统计每个乐队的关注人数（COUNT）
- 按关注人数降序排序
- 限制返回5条

---

## 修改的文件清单

### 前端文件（1个）

1. ✅ `frontend/src/views/fan/Home.vue`
   - 修改我的最新乐评评分显示为数字

### 后端文件（5个）

1. ✅ `src/main/java/com/band/management/service/FanUserService.java`
   - 修改 `getRecommendedBands()` 方法签名，添加 fanId 参数

2. ✅ `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java`
   - 修改 `getRecommendedBands()` 实现，调用新的 Mapper 方法

3. ✅ `src/main/java/com/band/management/controller/FanUserController.java`
   - 修改 `/discovery/bands` 端点，传递 fanId 参数

4. ✅ `src/main/java/com/band/management/mapper/BandMapper.java`
   - 添加 `selectUnfollowedBandsByFanCount()` 方法

5. ✅ `src/main/resources/mapper/BandMapper.xml`
   - 添加查询未关注乐队的 SQL

---

## 测试验证

### 测试步骤

1. **登录歌迷账号**
   - 用户名：`fan_zhangwei`
   - 密码：`123456`

2. **验证我的最新乐评**
   - ✅ 验证评分显示为数字（如：9.5）
   - ✅ 验证列宽为100px，居中对齐
   - ✅ 验证显示最新3条乐评

3. **验证专辑排行榜**
   - ✅ 验证显示10张专辑
   - ✅ 验证按平均评分降序排列
   - ✅ 验证显示专辑名称、乐队名称、评分
   - ✅ 验证评分为数字格式（保留1位小数）

4. **验证热门乐队推荐**
   - ✅ 验证显示5个未关注的乐队
   - ✅ 验证按关注人数降序排列
   - ✅ 验证不显示已关注的乐队

5. **测试关注功能**
   - ✅ 点击"关注"按钮
   - ✅ 验证关注成功提示
   - ✅ 验证该乐队从列表中移除
   - ✅ 验证自动补充新的乐队（如果有）
   - ✅ 验证统计数据更新（关注乐队数+1）

6. **边界测试**
   - ✅ 如果未关注乐队少于5个，验证显示所有未关注乐队
   - ✅ 如果已关注所有乐队，验证列表为空

---

## 注意事项

1. **评分显示**：
   - 我的最新乐评：数字格式
   - 专辑排行榜：数字格式
   - 都保留1位小数

2. **热门乐队推荐**：
   - 自动排除已关注的乐队
   - 按关注人数降序排序
   - 关注后自动刷新列表
   - 最多显示5个

3. **专辑排行榜**：
   - 数据来自 AlbumRanking 表
   - 由触发器自动维护
   - 显示平均评分最高的10张专辑

4. **性能考虑**：
   - 热门乐队查询使用了 NOT IN 子查询
   - 如果数据量很大，可能需要优化为 LEFT JOIN
   - 建议在 FanFavoriteBand 表的 fan_id 和 band_id 上建立索引

---

## 编译验证

✅ 所有修改的文件编译通过，无错误

---

## 总结

已成功完成歌迷中心首页的所有修复：
1. ✅ 我的最新乐评评分改用数字显示
2. ✅ 专辑排行榜正确显示 AlbumRanking 表的前10条数据
3. ✅ 热门乐队推荐按关注人数排序，排除已关注乐队，关注后自动刷新

所有功能正常工作，用户体验得到提升。
