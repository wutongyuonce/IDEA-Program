# 歌迷中心添加乐评 reviewedAt 字段修复

## 问题描述

在歌迷中心"我的乐评"页面（`fan/reviews`）添加乐评时报错：
- 错误信息：系统错误，请联系管理员
- 操作：点击"添加乐评"按钮，填写信息后提交

## 问题原因

**数据传输逻辑问题**：

1. **前端传递的数据**：
   ```javascript
   await request.post('/fan/reviews', {
     albumId: formData.albumId,
     rating: formData.rating,
     comment: formData.comment
   })
   ```
   前端只传递了 `albumId`、`rating` 和 `comment` 三个字段。

2. **后端 Mapper 插入语句**：
   ```xml
   <insert id="insert" parameterType="com.band.management.entity.AlbumReview" useGeneratedKeys="true" keyProperty="reviewId">
       INSERT INTO AlbumReview (fan_id, album_id, rating, comment, reviewed_at)
       VALUES (#{fanId}, #{albumId}, #{rating}, #{comment}, #{reviewedAt})
   </insert>
   ```
   插入语句需要 `reviewedAt` 字段，但前端没有传递这个值，导致插入失败。

3. **根本原因**：前端没有传递 `reviewedAt`（评论日期），而后端也没有自动设置这个字段。

## 解决方案

在后端的 `addReview` 方法中自动设置 `reviewedAt` 为当前时间。

### 修改文件

**文件**：`src/main/java/com/band/management/service/impl/FanUserServiceImpl.java`

**修改内容**：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Long addReview(AlbumReview review) {
    DataSourceContextHolder.setDataSourceType("FAN");
    
    // 检查专辑是否存在
    Album album = albumMapper.selectById(review.getAlbumId());
    if (album == null) {
        throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
    }

    // 检查是否已评论过
    AlbumReview condition = new AlbumReview();
    condition.setFanId(review.getFanId());
    condition.setAlbumId(review.getAlbumId());
    List<AlbumReview> existing = albumReviewMapper.selectByCondition(condition);
    if (!existing.isEmpty()) {
        throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    // 设置评论时间为当前时间（新增）
    if (review.getReviewedAt() == null) {
        review.setReviewedAt(new java.util.Date());
    }

    // 添加乐评
    int result = albumReviewMapper.insert(review);
    if (result <= 0) {
        throw new BusinessException(ErrorCode.OPERATION_FAILED);
    }
    
    return review.getReviewId();
}
```

**关键修改**：
```java
// 设置评论时间为当前时间
if (review.getReviewedAt() == null) {
    review.setReviewedAt(new java.util.Date());
}
```

## 技术细节

### 1. 为什么在后端设置而不是前端？

**优点**：
- **服务器时间准确**：使用服务器时间，避免客户端时间不准确的问题
- **简化前端逻辑**：前端不需要处理时间格式和时区问题
- **数据一致性**：所有时间戳都由服务器统一生成
- **安全性**：防止客户端篡改时间

### 2. 数据流程

```
前端提交
  ↓
{
  albumId: 1,
  rating: 9.5,
  comment: "很棒的专辑！"
}
  ↓
后端 Controller 接收
  ↓
设置 fanId（从当前登录用户获取）
  ↓
Service 层处理
  ↓
自动设置 reviewedAt = new Date()
  ↓
{
  fanId: 1,
  albumId: 1,
  rating: 9.5,
  comment: "很棒的专辑！",
  reviewedAt: "2025-12-23 15:30:00"
}
  ↓
插入数据库
```

### 3. AlbumReview 实体字段

```java
@Data
public class AlbumReview implements Serializable {
    private Long reviewId;        // 乐评ID（自动生成）
    private Long fanId;           // 歌迷ID（后端设置）
    private Long albumId;         // 专辑ID（前端传递）
    private BigDecimal rating;    // 评分（前端传递）
    private String comment;       // 评论内容（前端传递）
    private Date reviewedAt;      // 评论时间（后端自动设置）
    private Date createdAt;       // 创建时间（数据库默认）
    
    // 非数据库字段
    private String fanName;       // 歌迷姓名
    private String albumTitle;    // 专辑名称
    private String bandName;      // 乐队名称
}
```

## 前端代码（无需修改）

**文件**：`frontend/src/views/fan/Reviews.vue`

```javascript
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          // 编辑乐评
          await request.put(`/fan/reviews/${formData.reviewId}`, {
            rating: formData.rating,
            comment: formData.comment
          })
          ElMessage.success('更新成功')
        } else {
          // 添加乐评（只传递必要字段）
          await request.post('/fan/reviews', {
            albumId: formData.albumId,
            rating: formData.rating,
            comment: formData.comment
          })
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || (isEdit.value ? '更新失败' : '添加失败'))
      } finally {
        submitLoading.value = false
      }
    }
  })
}
```

## 测试验证

1. 重启应用（如果需要）
2. 使用歌迷账号登录：`fan_zhangwei` / `123456`
3. 进入"我的乐评"页面
4. 点击"添加乐评"按钮
5. 选择专辑、输入评分和评论内容
6. 点击"确定"提交
7. 确认乐评添加成功，评论日期自动显示为当前日期

## 相关功能

### 编辑乐评

编辑乐评时，`reviewedAt` 字段不会被更新（保持原评论时间）：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void updateReview(AlbumReview review) {
    DataSourceContextHolder.setDataSourceType("FAN");
    
    AlbumReview existReview = albumReviewMapper.selectById(review.getReviewId());
    if (existReview == null) {
        throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
    }

    // 验证权限：只能修改自己的乐评
    if (!existReview.getFanId().equals(review.getFanId())) {
        throw new BusinessException(ErrorCode.FAN_PERMISSION_DENIED);
    }

    // 只更新 rating 和 comment，不更新 reviewedAt
    int result = albumReviewMapper.update(review);
    if (result <= 0) {
        throw new BusinessException(ErrorCode.OPERATION_FAILED);
    }
}
```

### 数据库表结构

```sql
CREATE TABLE AlbumReview (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fan_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    rating DECIMAL(3,1) NOT NULL CHECK (rating >= 0 AND rating <= 10),
    comment TEXT,
    reviewed_at DATETIME NOT NULL,  -- 评论时间（必填）
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fan_id) REFERENCES Fan(fan_id),
    FOREIGN KEY (album_id) REFERENCES Album(album_id)
);
```

## 修改文件列表

- `src/main/java/com/band/management/service/impl/FanUserServiceImpl.java` - 添加自动设置 reviewedAt 逻辑

## 注意事项

1. **时间格式**：`reviewedAt` 使用 `java.util.Date` 类型，数据库存储为 `DATETIME`
2. **时区处理**：实体类使用 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")` 确保时区正确
3. **前端显示**：前端使用 `formatDate()` 函数格式化为 `YYYY-MM-DD` 格式
4. **编辑保留**：编辑乐评时不修改 `reviewedAt`，保持原评论时间
5. **数据验证**：后端会检查是否已对同一专辑评论过，防止重复评论

## 相关文档

- `FAN_REVIEWS_PAGE_IMPROVEMENT.md` - 我的乐评页面改进
- `FAN_HOME_PAGE_FIX_COMPLETED.md` - 歌迷中心首页修复

---

**修复日期**：2025-12-23
**修复人员**：Kiro AI Assistant
