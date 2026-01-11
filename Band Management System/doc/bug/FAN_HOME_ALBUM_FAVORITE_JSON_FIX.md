# 专辑收藏状态 JSON 序列化修复

## 问题描述

在歌迷中心首页的专辑排行榜中，即使歌迷已经收藏了某张专辑，前端仍然显示"收藏"按钮（而不是"已收藏"），点击后会报错"已经收藏过该专辑"。

## 问题原因

**JSON 序列化问题**：

Java 中 Boolean 类型的字段如果以 `is` 开头（如 `isFavorited`），在使用 Lombok 的 `@Data` 注解时，会自动生成以下方法：
- `getIsFavorited()` 
- `setIsFavorited(Boolean isFavorited)`

但是 Jackson JSON 序列化库在序列化时，会将 `isFavorited` 字段名自动转换为 `favorited`（去掉 `is` 前缀），导致前端接收到的 JSON 数据中字段名不匹配。

**后端设置的字段**：`isFavorited = true`

**前端接收到的 JSON**：
```json
{
  "albumId": 1,
  "title": "世界",
  "bandName": "逃跑计划",
  "avgScore": 9.5,
  "favorited": true  // 注意：字段名变成了 favorited
}
```

**前端期望的字段名**：`isFavorited`

**结果**：前端的 `v-if="!row.isFavorited"` 判断失败，因为 `row.isFavorited` 是 `undefined`，所以始终显示"收藏"按钮。

## 解决方案

在 Album 实体类的 `isFavorited` 字段上添加 `@JsonProperty` 注解，明确指定 JSON 序列化时的字段名。

### 修改文件

**文件**：`src/main/java/com/band/management/entity/Album.java`

```java
/**
 * 是否已收藏（非数据库字段，用于前端显示）
 */
@com.fasterxml.jackson.annotation.JsonProperty("isFavorited")
private Boolean isFavorited;
```

### 修改说明

`@JsonProperty("isFavorited")` 注解告诉 Jackson：
- 序列化时：使用 `isFavorited` 作为 JSON 字段名
- 反序列化时：从 `isFavorited` 字段读取值

这样可以确保前后端字段名一致。

## 修复后的效果

### 后端返回的 JSON

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "albumId": 1,
        "title": "世界",
        "bandName": "逃跑计划",
        "avgScore": 9.5,
        "isFavorited": true  // 字段名正确
      },
      {
        "albumId": 2,
        "title": "夜空中最亮的星",
        "bandName": "逃跑计划",
        "avgScore": 9.3,
        "isFavorited": false
      }
    ]
  }
}
```

### 前端显示

- **已收藏的专辑**：显示灰色"已收藏"按钮（禁用）
- **未收藏的专辑**：显示蓝色"收藏"按钮（可点击）

## 技术细节

### Lombok 与 Jackson 的交互

1. **Lombok `@Data` 注解**：
   - 自动生成 getter/setter 方法
   - 对于 Boolean 类型的 `isFavorited` 字段，生成 `getIsFavorited()` 和 `setIsFavorited()`

2. **Jackson 默认行为**：
   - 查找 `getXxx()` 方法来确定 JSON 字段名
   - 对于 `getIsFavorited()`，会去掉 `get` 和 `is` 前缀，得到 `favorited`
   - 因此默认序列化为 `"favorited": true`

3. **使用 `@JsonProperty` 注解**：
   - 明确指定 JSON 字段名为 `isFavorited`
   - 覆盖 Jackson 的默认行为
   - 确保前后端字段名一致

### 其他解决方案（未采用）

#### 方案1：修改字段名
```java
private Boolean favorited;  // 去掉 is 前缀
```
**缺点**：不符合 Java 命名规范，Boolean 类型通常以 `is` 开头

#### 方案2：前端适配
```javascript
<el-button v-if="!row.favorited" ...>  // 使用 favorited 而不是 isFavorited
```
**缺点**：前端字段名不直观，且需要修改多处代码

#### 方案3：使用 `@JsonProperty` 注解（已采用）
```java
@JsonProperty("isFavorited")
private Boolean isFavorited;
```
**优点**：
- 保持 Java 命名规范
- 前端字段名直观
- 只需修改一处代码

## 测试验证

### 测试步骤

1. **重启应用**
2. **登录歌迷账号**：`fan_zhangwei` / `123456`
3. **进入首页**：查看专辑排行榜
4. **验证初始状态**：
   - 已收藏的专辑显示"已收藏"按钮（灰色、禁用）
   - 未收藏的专辑显示"收藏"按钮（蓝色、可点击）
5. **点击收藏**：
   - 点击未收藏专辑的"收藏"按钮
   - 确认按钮变为"已收藏"
6. **刷新页面**：
   - 确认收藏状态保持
   - 已收藏的专辑仍显示"已收藏"

### 预期结果

- ✅ 已收藏专辑初次加载时就显示"已收藏"
- ✅ 未收藏专辑显示"收藏"按钮
- ✅ 点击收藏后按钮立即变为"已收藏"
- ✅ 不会出现"已经收藏过该专辑"的错误

## 相关知识

### Jackson 注解

- `@JsonProperty("fieldName")`：指定 JSON 字段名
- `@JsonIgnore`：忽略该字段，不序列化
- `@JsonFormat(pattern = "yyyy-MM-dd")`：指定日期格式
- `@JsonInclude(JsonInclude.Include.NON_NULL)`：只序列化非 null 字段

### Boolean 字段命名规范

**Java 命名规范**：
- Boolean 类型字段通常以 `is` 开头：`isActive`, `isDeleted`, `isFavorited`
- 生成的 getter 方法：`isActive()`, `isDeleted()`, `isFavorited()`
- 生成的 setter 方法：`setActive()`, `setDeleted()`, `setFavorited()`

**注意**：Lombok 对 `is` 开头的 Boolean 字段会生成 `getIsXxx()` 方法，而不是 `isXxx()` 方法，这可能导致 Jackson 序列化问题。

## 修改文件列表

- `src/main/java/com/band/management/entity/Album.java` - 添加 `@JsonProperty` 注解

## 相关文档

- `FAN_HOME_ALBUM_FAVORITE_FEATURE.md` - 专辑收藏功能实现
- `FAN_HOME_ALBUMRANKING_PERMISSION_FIX.md` - 专辑排行榜权限修复

---

**修复日期**：2025-12-23
**修复人员**：Kiro AI Assistant
