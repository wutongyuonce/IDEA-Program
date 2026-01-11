# 歌迷中心个人信息页面完整修复

## 问题描述

歌迷中心的 `fan/profile` 个人信息子页面存在以下问题：
1. 进入页面后报错：`Request failed with status code 404`、`加载个人信息失败`
2. 个人信息展示框和编辑信息页面中包含邮箱和电话字段，但数据库 Fan 表中没有这些字段
3. 修改密码功能无法正常工作，缺少后端 API

## 问题原因

### 1. API 路径不匹配
- **前端调用**：`GET /api/fan/profile`、`PUT /api/fan/profile`
- **后端实际路径**：`GET /api/fan/info`、`PUT /api/fan/info`
- **结果**：404 错误

### 2. 字段不匹配
- **Fan 实体类字段**：`fanId`, `name`, `gender`, `age`, `occupation`, `education`
- **前端期望字段**：包含 `email` 和 `phone`（数据库中不存在）

### 3. 性别格式不一致
- **数据库存储**：`M`（男）、`F`（女）、`O`（其他）
- **前端显示**：直接显示 `M`、`F`，不友好

### 4. 缺少修改密码 API
- 前端调用 `PUT /api/fan/password`，但后端没有实现

## 解决方案

### 1. 前端修改

#### 1.1 修复 API 路径

**文件**：`frontend/src/views/fan/Profile.vue`

```javascript
// 修改前
const res = await request.get('/fan/profile')
await request.put('/fan/profile', formData)

// 修改后
const res = await request.get('/fan/info')
await request.put('/fan/info', formData)
```

#### 1.2 删除邮箱和电话字段

**个人信息展示**：
```vue
<el-descriptions :column="2" border v-loading="loading">
  <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
  <el-descriptions-item label="性别">{{ formatGender(profile.gender) }}</el-descriptions-item>
  <el-descriptions-item label="年龄">{{ profile.age }}</el-descriptions-item>
  <el-descriptions-item label="职业">{{ profile.occupation }}</el-descriptions-item>
  <el-descriptions-item label="学历" :span="2">{{ profile.education }}</el-descriptions-item>
  <!-- 删除了邮箱和电话 -->
</el-descriptions>
```

**编辑表单**：
```vue
<el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
  <el-form-item label="姓名" prop="name">
    <el-input v-model="formData.name" placeholder="请输入姓名" />
  </el-form-item>
  <el-form-item label="性别" prop="gender">
    <el-radio-group v-model="formData.gender">
      <el-radio label="M">男</el-radio>
      <el-radio label="F">女</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="年龄" prop="age">
    <el-input-number v-model="formData.age" :min="1" :max="120" style="width: 100%" />
  </el-form-item>
  <el-form-item label="职业" prop="occupation">
    <el-input v-model="formData.occupation" placeholder="请输入职业" />
  </el-form-item>
  <el-form-item label="学历" prop="education">
    <el-select v-model="formData.education" placeholder="请选择学历" style="width: 100%">
      <el-option label="高中及以下" value="高中及以下" />
      <el-option label="专科" value="专科" />
      <el-option label="本科" value="本科" />
      <el-option label="硕士" value="硕士" />
      <el-option label="博士" value="博士" />
    </el-select>
  </el-form-item>
  <!-- 删除了邮箱和电话 -->
</el-form>
```

#### 1.3 添加性别格式化函数

```javascript
const formatGender = (gender) => {
  const genderMap = {
    'M': '男',
    'F': '女',
    'O': '其他'
  }
  return genderMap[gender] || gender
}
```

#### 1.4 修复性别选择

```vue
<!-- 修改前 -->
<el-radio-group v-model="formData.gender">
  <el-radio label="男">男</el-radio>
  <el-radio label="女">女</el-radio>
</el-radio-group>

<!-- 修改后 -->
<el-radio-group v-model="formData.gender">
  <el-radio label="M">男</el-radio>
  <el-radio label="F">女</el-radio>
</el-radio-group>
```

#### 1.5 修复修改密码 API

```javascript
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await request.put('/fan/password', passwordForm)
        ElMessage.success('密码修改成功，请重新登录')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        passwordFormRef.value.resetFields()
        // 3秒后跳转到登录页
        setTimeout(() => {
          window.location.href = '/login'
        }, 3000)
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}
```

### 2. 后端修改

#### 2.1 添加修改密码接口

**AuthService.java**：
```java
/**
 * 修改密码
 * 
 * @param userId 用户ID
 * @param oldPassword 原密码
 * @param newPassword 新密码
 */
void changePassword(Long userId, String oldPassword, String newPassword);
```

**AuthServiceImpl.java**：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public void changePassword(Long userId, String oldPassword, String newPassword) {
    log.info("修改密码: userId={}", userId);

    // 参数校验
    if (StringUtil.isEmpty(oldPassword)) {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "原密码不能为空");
    }
    if (StringUtil.isEmpty(newPassword)) {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码不能为空");
    }
    if (newPassword.length() < 6) {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码长度至少6位");
    }

    // 修改密码需要使用 ADMIN 数据源，因为只有 admin_user 有 UPDATE 权限
    DataSourceContextHolder.setDataSourceType("ADMIN");

    // 查询用户
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    // 验证原密码
    if (!EncryptUtil.matchesPassword(oldPassword, user.getPassword())) {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "原密码不正确");
    }

    // 加密新密码
    String encryptedPassword = EncryptUtil.encryptPassword(newPassword);

    // 更新密码
    user.setPassword(encryptedPassword);
    int result = userMapper.update(user);
    if (result <= 0) {
        throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "密码修改失败");
    }

    log.info("密码修改成功: userId={}", userId);
}
```

**关键点**：
- 使用 `ADMIN` 数据源，因为只有 `admin_user` 有 User 表的 UPDATE 权限
- 验证原密码是否正确
- 使用 `EncryptUtil.encryptPassword()` 加密新密码
- 更新 User 表中的密码

#### 2.2 添加 Controller API

**FanUserController.java**：
```java
/**
 * 修改密码
 */
@PutMapping("/password")
public Result<Void> changePassword(@RequestBody Map<String, String> params) {
    User currentUser = authService.getCurrentUser();
    String oldPassword = params.get("oldPassword");
    String newPassword = params.get("newPassword");
    log.info("歌迷用户修改密码: userId={}", currentUser.getUserId());
    authService.changePassword(currentUser.getUserId(), oldPassword, newPassword);
    return Result.success("密码修改成功");
}
```

## 数据库权限说明

### User 表权限

| 用户 | SELECT | INSERT | UPDATE | DELETE |
|------|--------|--------|--------|--------|
| admin_user | ✅ | ✅ | ✅ | ✅ |
| band_user | ✅ | ❌ | ❌ | ❌ |
| fan_user | ✅ | ❌ | ❌ | ❌ |

**说明**：
- `band_user` 和 `fan_user` 只有 SELECT 权限（用于登录验证）
- 修改密码时，需要切换到 `ADMIN` 数据源才能执行 UPDATE 操作
- 这是通过 `DataSourceContextHolder.setDataSourceType("ADMIN")` 实现的

### Fan 表字段

```sql
CREATE TABLE Fan (
    fan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    gender CHAR(1) CHECK (gender IN ('M', 'F', 'O')),
    age INT,
    occupation VARCHAR(50),
    education VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**注意**：Fan 表中没有 `email` 和 `phone` 字段。

## 功能流程

### 1. 加载个人信息

```
用户进入个人信息页面
  ↓
前端调用 GET /api/fan/info
  ↓
Controller: FanUserController.getFanInfo()
  ↓
获取当前登录用户的 fanId
  ↓
Service: FanUserServiceImpl.getFanInfo(fanId)
  ↓
使用 FAN 数据源查询 Fan 表
  ↓
返回 Fan 对象
  ↓
前端显示：姓名、性别（格式化）、年龄、职业、学历
```

### 2. 编辑个人信息

```
用户点击"编辑信息"按钮
  ↓
打开编辑对话框，填充当前信息
  ↓
用户修改信息后点击"确定"
  ↓
前端调用 PUT /api/fan/info
{
  name: "张伟",
  gender: "M",
  age: 25,
  occupation: "软件工程师",
  education: "本科"
}
  ↓
Controller: FanUserController.updateFanInfo()
  ↓
设置 fanId（从当前登录用户获取）
  ↓
Service: FanUserServiceImpl.updateFanInfo(fan)
  ↓
使用 FAN 数据源更新 Fan 表
  ↓
返回成功
  ↓
前端刷新个人信息
```

### 3. 修改密码

```
用户填写原密码、新密码、确认密码
  ↓
前端验证：
  - 原密码不为空
  - 新密码长度至少6位
  - 确认密码与新密码一致
  ↓
前端调用 PUT /api/fan/password
{
  oldPassword: "123456",
  newPassword: "newpass123",
  confirmPassword: "newpass123"
}
  ↓
Controller: FanUserController.changePassword()
  ↓
获取当前登录用户的 userId
  ↓
Service: AuthServiceImpl.changePassword(userId, oldPassword, newPassword)
  ↓
切换到 ADMIN 数据源
  ↓
查询 User 表，获取用户信息
  ↓
验证原密码是否正确
  ↓
加密新密码
  ↓
更新 User 表中的密码
  ↓
返回成功
  ↓
前端提示"密码修改成功，请重新登录"
  ↓
3秒后跳转到登录页
```

## 测试验证

### 测试步骤

1. **登录歌迷账号**：`fan_zhangwei` / `123456`
2. **进入个人信息页面**：
   - 确认页面正常加载，不报错
   - 确认显示：姓名、性别（男/女）、年龄、职业、学历
   - 确认没有邮箱和电话字段
3. **编辑个人信息**：
   - 点击"编辑信息"按钮
   - 修改姓名、性别、年龄等信息
   - 点击"确定"
   - 确认信息更新成功
4. **修改密码**：
   - 输入原密码：`123456`
   - 输入新密码：`newpass123`
   - 输入确认密码：`newpass123`
   - 点击"修改密码"
   - 确认提示"密码修改成功，请重新登录"
   - 等待3秒后自动跳转到登录页
   - 使用新密码登录：`fan_zhangwei` / `newpass123`
   - 确认登录成功

### 预期结果

- ✅ 个人信息页面正常加载，不报404错误
- ✅ 个人信息正确显示（无邮箱和电话）
- ✅ 性别显示为"男"或"女"（而不是M/F）
- ✅ 编辑信息功能正常工作
- ✅ 修改密码功能正常工作
- ✅ 修改密码后需要重新登录
- ✅ 新密码可以正常登录

## 修改文件列表

### 前端
1. `frontend/src/views/fan/Profile.vue` - 完整重构个人信息页面

### 后端
1. `src/main/java/com/band/management/service/AuthService.java` - 添加 changePassword 接口
2. `src/main/java/com/band/management/service/impl/AuthServiceImpl.java` - 实现 changePassword 方法
3. `src/main/java/com/band/management/controller/FanUserController.java` - 添加修改密码 API

## 注意事项

1. **数据源切换**：
   - 查询 Fan 信息：使用 `FAN` 数据源
   - 更新 Fan 信息：使用 `FAN` 数据源
   - 修改密码：使用 `ADMIN` 数据源（因为需要更新 User 表）

2. **密码安全**：
   - 密码使用 BCrypt 加密存储
   - 修改密码时验证原密码
   - 新密码长度至少6位
   - 修改密码后建议重新登录

3. **性别字段**：
   - 数据库存储：`M`、`F`、`O`
   - 前端显示：男、女、其他
   - 使用 `formatGender()` 函数转换

4. **字段验证**：
   - 姓名：必填
   - 性别：必选
   - 年龄：必填，1-120
   - 职业：可选
   - 学历：可选

## 相关文档

- `FAN_HOME_PAGE_FIX_COMPLETED.md` - 歌迷中心首页修复
- `FAN_REVIEWS_PAGE_IMPROVEMENT.md` - 我的乐评页面改进
- `DATABASE_SECURITY_UPDATE.md` - 数据库安全配置

---

**修复日期**：2025-12-23
**修复人员**：Kiro AI Assistant
