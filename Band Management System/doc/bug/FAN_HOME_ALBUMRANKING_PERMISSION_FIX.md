# 歌迷中心首页专辑排行榜权限修复

## 问题描述

歌迷用户登录后访问首页 `fan/home` 时报错：
- 错误信息：系统错误，请联系管理员
- 专辑排行榜为空，没有正确显示

## 问题原因

通过查看后端日志发现错误：
```
java.sql.SQLSyntaxErrorException: SELECT command denied to user 'fan_user'@'localhost' for table 'albumranking'
```

**根本原因**：`fan_user` 数据库用户没有 `AlbumRanking` 表的 SELECT 权限。

## 解决方案

### 1. 修改数据库安全配置文件

**文件**：`sql/database_security.sql`

在歌迷用户权限设置部分添加 `AlbumRanking` 表的 SELECT 权限：

```sql
-- 歌迷用户可以查看：乐队信息、成员信息、专辑信息、歌曲信息、演唱会信息、专辑排行榜
GRANT SELECT ON band_management.Band TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Member TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Album TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Song TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Concert TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.Performance TO 'fan_user'@'localhost';
GRANT SELECT ON band_management.AlbumRanking TO 'fan_user'@'localhost';  -- 新增

-- 歌迷用户可以查询 User 表（用于登录验证）
GRANT SELECT ON band_management.User TO 'fan_user'@'localhost';
```

### 2. 执行权限授予

执行以下 SQL 命令授予权限：

```sql
USE band_management;
GRANT SELECT ON band_management.AlbumRanking TO 'fan_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 验证权限

执行以下命令验证权限已正确设置：

```sql
SHOW GRANTS FOR 'fan_user'@'localhost';
```

确认输出中包含：
```
GRANT SELECT ON `band_management`.`albumranking` TO `fan_user`@`localhost`
```

## 相关代码

### 后端查询逻辑

**文件**：`src/main/java/com/band/management/service/impl/FanUserServiceImpl.java`

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
        album.setAvgScore(ranking.getAvgScore());
        album.setBandName(ranking.getBandName());
        albums.add(album);
    }
    
    return new PageInfo<>(albums);
}
```

### Mapper 查询

**文件**：`src/main/resources/mapper/AlbumRankingMapper.xml`

```xml
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
```

### 前端展示

**文件**：`frontend/src/views/fan/Home.vue`

```vue
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
  </el-table>
</el-card>
```

## 测试验证

1. 重启应用（如果需要）
2. 使用歌迷账号登录：`fan_zhangwei` / `123456`
3. 访问歌迷中心首页
4. 确认专辑排行榜正确显示前10张专辑及其评分

## 修改文件列表

- `sql/database_security.sql` - 添加 AlbumRanking 表的 SELECT 权限

## 注意事项

1. **权限最小化原则**：歌迷用户只被授予 `AlbumRanking` 表的 SELECT（只读）权限，不能修改排行榜数据
2. **数据源切换**：确保在查询时使用 `FAN` 数据源
3. **排行榜数据**：`AlbumRanking` 表应该由系统定期更新（通过触发器或定时任务）
4. **显示数量**：首页固定显示前10张专辑

## 相关文档

- `DATABASE_SECURITY_UPDATE.md` - 数据库安全配置更新记录
- `FAN_HOME_PAGE_FINAL_FIX.md` - 歌迷中心首页最终修复
- `USER_TABLE_SELECT_PERMISSION_FIX.md` - User 表权限修复

---

**修复日期**：2025-12-23
**修复人员**：Kiro AI Assistant
