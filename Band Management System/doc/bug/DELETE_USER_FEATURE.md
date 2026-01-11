# 删除用户功能实现文档

## 功能概述

为乐队管理系统添加了删除用户功能，包括：
1. **删除乐队用户** - 在乐队管理首页
2. **删除歌迷用户** - 在歌迷个人信息页面

## 实现内容

### 1. 删除乐队功能

#### 后端实现

**文件：** `src/main/java/com/band/management/controller/BandUserController.java`
- 添加 `@DeleteMapping("/delete")` 接口

**文件：** `src/main/java/com/band/management/service/BandUserService.java`
- 添加 `void deleteBand(Long bandId)` 方法声明

**文件：** `src/main/java/com/band/management/service/impl/BandUserServiceImpl.java`
- 实现删除乐队逻辑
- 检查乐队是否存在
- 检查是否有成员（有成员则无法删除）
- 删除 Band 表记录（级联删除相关数据）
- 删除 User 表对应记录

#### 前端实现

**文件：** `frontend/src/views/band/Home.vue`
- 在修改密码卡片后添加"危险操作"卡片
- 显示删除警告信息
- 实现两次确认对话框
- 删除成功后跳转到登录页

#### 删除逻辑

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void deleteBand(Long bandId) {
    // 1. 检查乐队是否存在
    Band band = bandMapper.selectById(bandId);
    if (band == null) {
        throw new BusinessException("乐队不存在");
    }

    // 2. 检查是否有成员
    if (band.getMemberCount() != null && band.getMemberCount() > 0) {
        throw new BusinessException("需要先删除所有成员数据");
    }

    // 3. 删除乐队（级联删除）
    bandMapper.deleteById(bandId);

    // 4. 删除用户记录
    userMapper.deleteByRoleAndRelatedId("BAND", bandId);
}
```

#### 级联删除的数据

根据数据库外键约束 `ON DELETE CASCADE`，删除乐队会自动删除：
- ✅ Album（专辑）
- ✅ Song（歌曲，通过专辑级联）
- ✅ Concert（演唱会）
- ✅ FanFavoriteBand（歌迷关注关系）
- ✅ FanFavoriteAlbum（歌迷收藏的专辑）
- ✅ FanFavoriteSong（歌迷收藏的歌曲）
- ✅ AlbumReview（专辑评论）
- ✅ ConcertAttendance（演唱会参与记录）
- ✅ User（用户记录）

#### 删除限制

- ❌ 如果乐队有成员（member_count > 0），无法删除
- 提示："需要先删除所有成员数据"

---

### 2. 删除歌迷功能

#### 后端实现

**文件：** `src/main/java/com/band/management/controller/FanUserController.java`
- 添加 `@DeleteMapping("/delete")` 接口

**文件：** `src/main/java/com/band/management/service/FanUserService.java`
- 添加 `void deleteFan(Long fanId)` 方法声明

**文件：** `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java`
- 实现删除歌迷逻辑
- 检查歌迷是否存在
- 删除 Fan 表记录（级联删除相关数据）
- 删除 User 表对应记录

#### 前端实现

**文件：** `frontend/src/views/fan/Profile.vue`
- 在修改密码卡片后添加"危险操作"卡片
- 显示删除警告信息
- 实现两次确认对话框
- 删除成功后跳转到登录页

#### 删除逻辑

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void deleteFan(Long fanId) {
    // 1. 检查歌迷是否存在
    Fan fan = fanMapper.selectById(fanId);
    if (fan == null) {
        throw new BusinessException("歌迷不存在");
    }

    // 2. 删除歌迷（级联删除）
    fanMapper.deleteById(fanId);

    // 3. 删除用户记录
    userMapper.deleteByRoleAndRelatedId("FAN", fanId);
}
```

#### 级联删除的数据

根据数据库外键约束 `ON DELETE CASCADE`，删除歌迷会自动删除：
- ✅ FanFavoriteBand（关注的乐队关系）
- ✅ FanFavoriteAlbum（收藏的专辑关系）
- ✅ FanFavoriteSong（收藏的歌曲关系）
- ✅ ConcertAttendance（参加的演唱会记录）
- ✅ AlbumReview（发表的乐评）
- ✅ User（用户记录）

#### 删除限制

- ✅ 无限制条件，可以直接删除

---

## 用户体验设计

### 警告提示

两个删除功能都包含清晰的警告提示：

**乐队删除警告：**
```
删除乐队是不可逆操作，将会连带删除以下数据：
• 所有专辑数据
• 所有歌曲数据
• 所有演唱会数据
• 所有歌迷关注数据

注意：如果乐队有成员，需要先删除所有成员才能删除乐队
```

**歌迷删除警告：**
```
删除账号是不可逆操作，将会连带删除以下数据：
• 关注的乐队数据
• 收藏的专辑数据
• 收藏的歌曲数据
• 参加的演唱会记录
• 发表的所有乐评

此操作无法恢复，请谨慎操作！
```

### 二次确认流程

1. **第一次确认**
   - 类型：警告（warning）
   - 按钮：继续 / 取消
   - 说明删除的后果

2. **第二次确认**
   - 类型：错误（error）
   - 按钮：确定删除 / 取消
   - 最终确认

3. **执行删除**
   - 显示加载状态
   - 成功后提示
   - 2秒后自动跳转到登录页

### 前端代码示例

```javascript
const handleDeleteBand = async () => {
  try {
    // 第一次确认
    await ElMessageBox.confirm(
      '删除乐队会连带删除专辑、歌曲、演唱会以及歌迷关注的数据，此操作不可恢复，是否继续？',
      '警告',
      {
        confirmButtonText: '继续',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 第二次确认
    await ElMessageBox.confirm(
      '请再次确认：您确定要删除乐队吗？这将永久删除所有相关数据！',
      '最终确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 执行删除
    await request.delete('/band/delete')
    ElMessage.success('乐队删除成功')
    
    // 跳转到登录页
    setTimeout(() => {
      localStorage.removeItem('token')
      router.push('/login')
    }, 2000)
    
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('已取消删除')
    } else {
      ElMessage.error(error.message || '删除失败')
    }
  }
}
```

## API 接口

### 删除乐队

**请求：**
```
DELETE /api/band/delete
```

**响应：**
```json
{
  "code": 200,
  "message": "乐队删除成功",
  "data": null
}
```

**错误响应：**
```json
{
  "code": 400,
  "message": "需要先删除所有成员数据",
  "data": null
}
```

### 删除歌迷

**请求：**
```
DELETE /api/fan/delete
```

**响应：**
```json
{
  "code": 200,
  "message": "账号删除成功",
  "data": null
}
```

## 数据库外键约束

所有级联删除都基于数据库外键约束：

```sql
-- 示例：FanFavoriteBand 表
CONSTRAINT fk_favband_fan FOREIGN KEY (fan_id) REFERENCES Fan(fan_id) 
    ON UPDATE RESTRICT ON DELETE CASCADE

-- 示例：Album 表
CONSTRAINT fk_album_band FOREIGN KEY (band_id) REFERENCES Band(band_id) 
    ON UPDATE RESTRICT ON DELETE CASCADE
```

## 安全性考虑

1. **权限验证**
   - 只能删除当前登录用户自己的账号
   - 通过 `getCurrentBandId()` 和 `getCurrentFanId()` 获取当前用户ID

2. **事务管理**
   - 使用 `@Transactional` 确保数据一致性
   - 删除失败时自动回滚

3. **数据源切换**
   - 删除操作使用 `admin` 数据源
   - 确保有足够的权限执行删除

4. **二次确认**
   - 防止误操作
   - 明确告知删除后果

## 测试建议

### 乐队删除测试

1. **有成员的乐队**
   - 尝试删除
   - 验证提示"需要先删除所有成员数据"

2. **无成员的乐队**
   - 删除成功
   - 验证 Band 表和 User 表记录被删除
   - 验证相关专辑、歌曲、演唱会数据被删除

3. **取消删除**
   - 第一次确认点击取消
   - 第二次确认点击取消
   - 验证数据未被删除

### 歌迷删除测试

1. **有数据的歌迷**
   - 删除成功
   - 验证 Fan 表和 User 表记录被删除
   - 验证关注、收藏、参与、评论数据被删除

2. **取消删除**
   - 确认对话框点击取消
   - 验证数据未被删除

## 修改文件清单

### 后端文件
1. `src/main/java/com/band/management/controller/BandUserController.java`
2. `src/main/java/com/band/management/service/BandUserService.java`
3. `src/main/java/com/band/management/service/impl/BandUserServiceImpl.java`
4. `src/main/java/com/band/management/controller/FanUserController.java`
5. `src/main/java/com/band/management/service/FanUserService.java`
6. `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java`

### 前端文件
1. `frontend/src/views/band/Home.vue`
2. `frontend/src/views/fan/Profile.vue`

## 实现日期

2024-12-23


---

## 修复记录

### 2024-12-23 - 数据源切换顺序修复

**问题：** 删除歌迷/乐队时报错"系统错误，请联系管理员"

**原因：** 在删除方法中，一开始就切换到 `admin` 数据源，但在查询 Fan/Band 表检查是否存在时，`admin_user` 没有对这些表的 SELECT 权限，导致查询失败。

**解决方案：**
1. 先使用对应的数据源（FAN/band）进行查询和验证
2. 验证通过后，再切换到 `admin` 数据源执行删除操作

**修改文件：**
- `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java` - deleteFan方法
- `src/main/java/com/band/management/service/impl/BandUserServiceImpl.java` - deleteBand方法

**修改后的逻辑：**
```java
// 歌迷删除
public void deleteFan(Long fanId) {
    // 1. 先使用FAN数据源检查歌迷是否存在
    DataSourceContextHolder.setDataSourceType("FAN");
    Fan fan = fanMapper.selectById(fanId);
    if (fan == null) {
        throw new BusinessException("歌迷不存在");
    }

    // 2. 切换到admin数据源进行删除操作
    DataSourceContextHolder.setDataSourceType("admin");
    fanMapper.deleteById(fanId);
    userMapper.deleteByRoleAndRelatedId("FAN", fanId);
}

// 乐队删除
public void deleteBand(Long bandId) {
    // 1. 先使用band数据源检查乐队是否存在和成员数量
    DataSourceContextHolder.setDataSourceType("band");
    Band band = bandMapper.selectById(bandId);
    if (band == null) {
        throw new BusinessException("乐队不存在");
    }
    if (band.getMemberCount() > 0) {
        throw new BusinessException("需要先删除所有成员数据");
    }

    // 2. 切换到admin数据源进行删除操作
    DataSourceContextHolder.setDataSourceType("admin");
    bandMapper.deleteById(bandId);
    userMapper.deleteByRoleAndRelatedId("BAND", bandId);
}
```

**测试验证：**
- ✅ 歌迷删除功能正常工作
- ✅ 乐队删除功能正常工作
- ✅ 数据源权限问题已解决
