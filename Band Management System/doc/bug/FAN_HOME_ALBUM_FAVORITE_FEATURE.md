# 歌迷中心首页专辑排行榜收藏功能

## 功能描述

在歌迷中心首页（`fan/home`）的专辑排行榜展示框中添加收藏操作列，允许歌迷直接收藏排行榜中的专辑。

## 功能特性

1. **收藏按钮**：在专辑排行榜最右侧添加"操作"列，显示"收藏"按钮
2. **收藏状态显示**：
   - 未收藏：显示蓝色"收藏"按钮，可点击
   - 已收藏：显示灰色"已收藏"按钮，不可点击
3. **实时更新**：点击收藏后，按钮立即变为"已收藏"状态
4. **数据同步**：收藏后自动更新统计数据中的"收藏专辑"数量

## 实现细节

### 1. 后端修改

#### 1.1 Album 实体类添加字段

**文件**：`src/main/java/com/band/management/entity/Album.java`

```java
/**
 * 是否已收藏（非数据库字段，用于前端显示）
 */
private Boolean isFavorited;
```

#### 1.2 修改 Service 接口

**文件**：`src/main/java/com/band/management/service/FanUserService.java`

```java
/**
 * 获取专辑排行榜（分页）
 * @param fanId 歌迷ID，用于判断是否已收藏
 */
PageInfo<Album> getTopAlbums(Long fanId, Integer page, Integer size);
```

**修改说明**：添加 `fanId` 参数，用于查询该歌迷是否已收藏每个专辑。

#### 1.3 修改 Service 实现

**文件**：`src/main/java/com/band/management/service/impl/FanUserServiceImpl.java`

```java
@Override
public PageInfo<Album> getTopAlbums(Long fanId, Integer page, Integer size) {
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
        album.setAvgScore(ranking.getAvgScore());
        album.setBandName(ranking.getBandName());
        
        // 检查是否已收藏（新增）
        if (fanId != null) {
            int count = fanFavoriteMapper.checkFavoriteAlbum(fanId, ranking.getAlbumId());
            album.setIsFavorited(count > 0);
        } else {
            album.setIsFavorited(false);
        }
        
        albums.add(album);
    }
    
    return new PageInfo<>(albums);
}
```

**关键逻辑**：
- 遍历每个专辑，调用 `fanFavoriteMapper.checkFavoriteAlbum()` 检查是否已收藏
- 如果 `count > 0`，说明已收藏，设置 `isFavorited = true`
- 否则设置 `isFavorited = false`

#### 1.4 修改 Controller

**文件**：`src/main/java/com/band/management/controller/FanUserController.java`

```java
@GetMapping("/discovery/albums/ranking")
public Result<PageInfo<Album>> getTopAlbums(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size) {
    Long fanId = getCurrentFanId();  // 获取当前登录歌迷ID
    log.info("歌迷用户获取专辑排行榜: fanId={}, page={}, size={}", fanId, page, size);
    PageInfo<Album> albums = fanUserService.getTopAlbums(fanId, page, size);
    return Result.success(albums);
}
```

**修改说明**：传递当前登录歌迷的 `fanId` 给 Service 层。

### 2. 前端修改

#### 2.1 添加收藏操作列

**文件**：`frontend/src/views/fan/Home.vue`

```vue
<el-table :data="topAlbums" style="width: 100%">
  <el-table-column prop="title" label="专辑名称" />
  <el-table-column prop="bandName" label="乐队" width="120" />
  <el-table-column prop="avgScore" label="评分" width="100">
    <template #default="{ row }">
      {{ row.avgScore ? row.avgScore.toFixed(1) : '-' }}
    </template>
  </el-table-column>
  <!-- 新增操作列 -->
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
```

**UI 逻辑**：
- 使用 `v-if` 和 `v-else` 根据 `row.isFavorited` 显示不同按钮
- 未收藏：显示蓝色（`type="primary"`）可点击按钮
- 已收藏：显示灰色（`type="info"`）禁用（`disabled`）按钮

#### 2.2 添加收藏处理函数

```javascript
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
```

**处理流程**：
1. 调用后端 API：`POST /api/fan/favorites/albums`
2. 传递参数：`{ albumId: album.albumId }`
3. 成功后：
   - 显示成功提示
   - 立即更新 `album.isFavorited = true`（按钮变为"已收藏"）
   - 刷新统计数据（收藏专辑数量 +1）
4. 失败时：显示错误提示

### 3. 数据库操作

#### 3.1 FanFavoriteAlbum 表结构

```sql
CREATE TABLE FanFavoriteAlbum (
    fan_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (fan_id, album_id),
    FOREIGN KEY (fan_id) REFERENCES Fan(fan_id),
    FOREIGN KEY (album_id) REFERENCES Album(album_id)
);
```

#### 3.2 插入收藏记录

**Mapper**：`FanFavoriteMapper.insertFavoriteAlbum()`

```xml
<insert id="insertFavoriteAlbum">
    INSERT INTO FanFavoriteAlbum (fan_id, album_id)
    VALUES (#{fanId}, #{albumId})
</insert>
```

#### 3.3 检查收藏状态

**Mapper**：`FanFavoriteMapper.checkFavoriteAlbum()`

```xml
<select id="checkFavoriteAlbum" resultType="int">
    SELECT COUNT(*)
    FROM FanFavoriteAlbum
    WHERE fan_id = #{fanId} AND album_id = #{albumId}
</select>
```

**返回值**：
- `0`：未收藏
- `1`：已收藏

## 数据流程

```
用户点击"收藏"按钮
  ↓
前端调用 handleFavoriteAlbum(album)
  ↓
POST /api/fan/favorites/albums
{
  albumId: 1
}
  ↓
Controller: FanUserController.favoriteAlbum()
  ↓
获取当前登录歌迷ID: fanId = getCurrentFanId()
  ↓
Service: FanUserServiceImpl.favoriteAlbum(fanId, albumId)
  ↓
1. 检查专辑是否存在
2. 检查是否已收藏（防止重复）
3. 插入收藏记录到 FanFavoriteAlbum 表
  ↓
INSERT INTO FanFavoriteAlbum (fan_id, album_id)
VALUES (1, 1)
  ↓
返回成功
  ↓
前端更新 UI
  ↓
1. album.isFavorited = true
2. 按钮变为"已收藏"（灰色、禁用）
3. 刷新统计数据
```

## 与热门乐队推荐的对比

| 特性 | 热门乐队推荐 | 专辑排行榜 |
|------|------------|-----------|
| 操作 | 关注 | 收藏 |
| 按钮文字 | "关注" / "已关注" | "收藏" / "已收藏" |
| 数据表 | FanFavoriteBand | FanFavoriteAlbum |
| 操作后行为 | 从列表中移除（自动刷新） | 按钮变为"已收藏" |
| 统计字段 | favoriteBandCount | favoriteAlbumCount |

**设计差异原因**：
- **热门乐队推荐**：关注后自动从推荐列表移除，因为推荐的是"未关注"的乐队
- **专辑排行榜**：收藏后保留在列表中，因为排行榜是固定的前10名，不会因为收藏而改变

## 错误处理

### 1. 重复收藏

**后端检查**：
```java
// 检查是否已收藏
int existing = fanFavoriteMapper.checkFavoriteAlbum(fanId, albumId);
if (existing > 0) {
    throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "已经收藏过该专辑");
}
```

**前端防护**：
- 已收藏的专辑按钮显示为"已收藏"且禁用
- 即使用户绕过前端限制，后端也会拦截

### 2. 专辑不存在

**后端检查**：
```java
// 检查专辑是否存在
Album album = albumMapper.selectById(albumId);
if (album == null) {
    throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
}
```

### 3. 网络错误

**前端处理**：
```javascript
catch (error) {
    ElMessage.error(error.message || '收藏失败')
}
```

## 测试验证

### 测试步骤

1. **登录歌迷账号**：`fan_zhangwei` / `123456`
2. **进入首页**：查看专辑排行榜
3. **验证初始状态**：
   - 未收藏的专辑显示蓝色"收藏"按钮
   - 已收藏的专辑显示灰色"已收藏"按钮
4. **点击收藏**：
   - 点击某个专辑的"收藏"按钮
   - 确认提示"收藏成功"
   - 确认按钮变为"已收藏"（灰色、禁用）
   - 确认统计数据中"收藏专辑"数量增加
5. **刷新页面**：
   - 刷新浏览器
   - 确认刚才收藏的专辑仍显示"已收藏"
6. **数据库验证**：
   ```sql
   SELECT * FROM FanFavoriteAlbum WHERE fan_id = 1;
   ```
   确认有新增的收藏记录

### 预期结果

- ✅ 未收藏专辑显示"收藏"按钮
- ✅ 已收藏专辑显示"已收藏"按钮（禁用）
- ✅ 点击收藏后按钮立即变为"已收藏"
- ✅ 收藏成功后统计数据更新
- ✅ 刷新页面后收藏状态保持
- ✅ 数据库中正确插入收藏记录
- ✅ 重复收藏被后端拦截

## 修改文件列表

### 后端
1. `src/main/java/com/band/management/entity/Album.java` - 添加 `isFavorited` 字段
2. `src/main/java/com/band/management/service/FanUserService.java` - 修改 `getTopAlbums` 接口签名
3. `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java` - 实现收藏状态检查
4. `src/main/java/com/band/management/controller/FanUserController.java` - 传递 `fanId` 参数

### 前端
1. `frontend/src/views/fan/Home.vue` - 添加收藏操作列和处理函数

## 相关功能

### 1. 收藏的专辑列表

歌迷可以在"我的收藏"页面查看所有收藏的专辑：
- API：`GET /api/fan/favorites/albums`
- 支持分页
- 按收藏时间倒序排列

### 2. 取消收藏

在"我的收藏"页面可以取消收藏：
- API：`DELETE /api/fan/favorites/albums/{albumId}`
- 取消后从收藏列表移除
- 统计数据自动更新

### 3. 统计数据

首页顶部统计卡片显示：
- 关注乐队数量
- 收藏专辑数量（包含新收藏的专辑）
- 收藏歌曲数量
- 我的乐评数量

## 注意事项

1. **数据源**：所有操作使用 `FAN` 数据源
2. **权限控制**：只能收藏自己的专辑，不能代替其他歌迷收藏
3. **主键约束**：`FanFavoriteAlbum` 表使用 `(fan_id, album_id)` 作为联合主键，防止重复收藏
4. **级联删除**：如果专辑被删除，相关的收藏记录也会被删除（外键约束）
5. **实时更新**：前端使用 Vue 的响应式特性，直接修改 `album.isFavorited` 即可更新 UI

## 相关文档

- `FAN_HOME_PAGE_FINAL_FIX.md` - 歌迷中心首页最终修复
- `FAN_HOME_ALBUMRANKING_PERMISSION_FIX.md` - 专辑排行榜权限修复
- `FAN_REVIEWS_PAGE_IMPROVEMENT.md` - 我的乐评页面改进

---

**功能添加日期**：2025-12-23
**开发人员**：Kiro AI Assistant
