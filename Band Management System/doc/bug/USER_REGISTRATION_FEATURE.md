# 用户注册功能实现

## 需求描述

1. 删除登录页面下面的测试账号提示
2. 增加乐队用户注册和歌迷用户注册功能
3. 注册时填写数据库中band表和fan表所需的信息
4. 提交后自动在相应表中增加数据，同时在user表中增加用户数据
5. 账号username使用注册时的英文名称（乐队名称全英文拼音或歌迷名字全英文拼音）
6. 密码按照注册信息填写
7. 在注册页面提醒用户登录时使用的账号为英文名称

## 实现方案

### 1. 前端修改

#### 1.1 修改登录页面

**文件**：`frontend/src/views/Login.vue`

**修改内容**：
1. **删除测试账号提示**：
   ```vue
   <!-- 删除了这部分 -->
   <div class="tips">
     <p>测试账号：</p>
     <p>管理员: admin / 123456</p>
     <p>乐队: band_escape / 123456（逃跑计划）</p>
     <p>歌迷: fan_zhangwei / 123456（张伟）</p>
   </div>
   ```

2. **添加注册链接**：
   ```vue
   <div class="register-links">
     <el-button type="text" @click="goToRegister('band')">乐队用户注册</el-button>
     <el-divider direction="vertical" />
     <el-button type="text" @click="goToRegister('fan')">歌迷用户注册</el-button>
   </div>
   ```

3. **添加跳转方法**：
   ```javascript
   const goToRegister = (type) => {
     if (type === 'band') {
       router.push('/register/band')
     } else if (type === 'fan') {
       router.push('/register/fan')
     }
   }
   ```

#### 1.2 创建乐队注册页面

**文件**：`frontend/src/views/RegisterBand.vue`

**功能特点**：
1. **登录提示**：
   - 使用 `el-alert` 组件提示用户
   - 明确说明使用"乐队名称的英文拼音"作为用户名登录

2. **表单字段**：
   - 乐队名称（中文）- 必填
   - 英文名称（用于登录）- 必填，只能包含英文字母、数字和下划线
   - 成立时间 - 必填，日期选择器
   - 乐队简介 - 可选，多行文本
   - 登录密码 - 必填，至少6位
   - 确认密码 - 必填，必须与密码一致

3. **表单验证**：
   - 英文名称格式验证：`/^[a-zA-Z0-9_]+$/`
   - 密码长度验证：至少6位
   - 确认密码一致性验证

4. **用户体验**：
   - 提交按钮显示加载状态
   - 注册成功后提示"注册成功！请使用英文名称登录"
   - 2秒后自动跳转到登录页
   - 提供"返回登录"按钮

#### 1.3 创建歌迷注册页面

**文件**：`frontend/src/views/RegisterFan.vue`

**功能特点**：
1. **登录提示**：
   - 使用 `el-alert` 组件提示用户
   - 明确说明使用"姓名的英文拼音"作为用户名登录

2. **表单字段**：
   - 姓名（中文）- 必填
   - 英文名称（用于登录）- 必填，只能包含英文字母、数字和下划线
   - 性别 - 必填，单选（男/女）
   - 年龄 - 必填，数字输入框（1-120）
   - 职业 - 必填
   - 学历 - 必填，下拉选择（高中及以下/专科/本科/硕士/博士）
   - 登录密码 - 必填，至少6位
   - 确认密码 - 必填，必须与密码一致

3. **表单验证**：
   - 与乐队注册相同的验证规则

4. **用户体验**：
   - 与乐队注册相同的用户体验

#### 1.4 添加路由配置

**文件**：`frontend/src/router/index.js`

**添加内容**：
```javascript
{
  path: '/register/band',
  name: 'RegisterBand',
  component: () => import('../views/RegisterBand.vue')
},
{
  path: '/register/fan',
  name: 'RegisterFan',
  component: () => import('../views/RegisterFan.vue')
}
```

**修改路由守卫**：
```javascript
// 如果访问登录页或注册页，直接放行
if (to.path === '/login' || to.path.startsWith('/register')) {
  next()
  return
}
```

### 2. 后端修改

#### 2.1 在AuthService接口中添加注册方法

**文件**：`src/main/java/com/band/management/service/AuthService.java`

**添加方法**：
```java
/**
 * 乐队用户注册
 */
Long registerBand(String name, String username, String foundedAt, String intro, String password);

/**
 * 歌迷用户注册
 */
Long registerFan(String name, String username, String gender, Integer age, String occupation, String education, String password);
```

#### 2.2 在AuthServiceImpl中实现注册方法

**文件**：`src/main/java/com/band/management/service/impl/AuthServiceImpl.java`

**添加依赖注入**：
```java
@Autowired
private com.band.management.mapper.BandMapper bandMapper;

@Autowired
private com.band.management.mapper.FanMapper fanMapper;
```

**实现乐队注册**：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public Long registerBand(String name, String username, String foundedAt, String intro, String password) {
    // 1. 参数校验
    // 2. 使用 ADMIN 数据源
    // 3. 检查用户名是否已存在
    // 4. 创建乐队记录
    // 5. 创建用户记录（role=BAND, relatedId=bandId）
    // 6. 返回乐队ID
}
```

**实现歌迷注册**：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public Long registerFan(String name, String username, String gender, Integer age, String occupation, String education, String password) {
    // 1. 参数校验
    // 2. 使用 ADMIN 数据源
    // 3. 检查用户名是否已存在
    // 4. 创建歌迷记录
    // 5. 创建用户记录（role=FAN, relatedId=fanId）
    // 6. 返回歌迷ID
}
```

**关键点**：
- 使用 `@Transactional` 确保事务一致性
- 使用 `ADMIN` 数据源（只有admin_user有INSERT权限）
- 密码以明文形式存储（按用户要求）
- 先创建Band/Fan记录，再创建User记录
- User记录的relatedId指向Band/Fan的ID

#### 2.3 在AuthController中添加注册API

**文件**：`src/main/java/com/band/management/controller/AuthController.java`

**添加API**：
```java
/**
 * 乐队用户注册
 */
@PostMapping("/register/band")
public Result<Long> registerBand(@RequestBody Map<String, Object> params) {
    String name = (String) params.get("name");
    String username = (String) params.get("username");
    String foundedAt = (String) params.get("foundedAt");
    String intro = (String) params.get("intro");
    String password = (String) params.get("password");
    
    Long bandId = authService.registerBand(name, username, foundedAt, intro, password);
    return Result.success("注册成功", bandId);
}

/**
 * 歌迷用户注册
 */
@PostMapping("/register/fan")
public Result<Long> registerFan(@RequestBody Map<String, Object> params) {
    String name = (String) params.get("name");
    String username = (String) params.get("username");
    String gender = (String) params.get("gender");
    Integer age = (Integer) params.get("age");
    String occupation = (String) params.get("occupation");
    String education = (String) params.get("education");
    String password = (String) params.get("password");
    
    Long fanId = authService.registerFan(name, username, gender, age, occupation, education, password);
    return Result.success("注册成功", fanId);
}
```

## API接口

### 1. 乐队用户注册
- **URL**：`POST /api/auth/register/band`
- **请求体**：
  ```json
  {
    "name": "逃跑计划",
    "username": "taopaojihua",
    "foundedAt": "2005-01-01",
    "intro": "中国摇滚乐队",
    "password": "123456"
  }
  ```
- **返回**：
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": 1
  }
  ```

### 2. 歌迷用户注册
- **URL**：`POST /api/auth/register/fan`
- **请求体**：
  ```json
  {
    "name": "张伟",
    "username": "zhangwei",
    "gender": "M",
    "age": 25,
    "occupation": "学生",
    "education": "本科",
    "password": "123456"
  }
  ```
- **返回**：
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": 1
  }
  ```

## 数据流程

### 乐队用户注册流程

```
用户访问登录页
  ↓
点击"乐队用户注册"
  ↓
跳转到 /register/band
  ↓
填写注册信息
  - 乐队名称（中文）
  - 英文名称（用于登录）
  - 成立时间
  - 乐队简介
  - 登录密码
  - 确认密码
  ↓
点击"注册"按钮
  ↓
前端验证表单
  ↓
POST /api/auth/register/band
  ↓
后端处理：
  1. 参数校验
  2. 检查用户名是否已存在
  3. 创建Band记录
  4. 创建User记录（role=BAND, relatedId=bandId）
  ↓
返回成功
  ↓
前端提示"注册成功！请使用英文名称登录"
  ↓
2秒后自动跳转到登录页
  ↓
用户使用英文名称和密码登录
```

### 歌迷用户注册流程

```
用户访问登录页
  ↓
点击"歌迷用户注册"
  ↓
跳转到 /register/fan
  ↓
填写注册信息
  - 姓名（中文）
  - 英文名称（用于登录）
  - 性别
  - 年龄
  - 职业
  - 学历
  - 登录密码
  - 确认密码
  ↓
点击"注册"按钮
  ↓
前端验证表单
  ↓
POST /api/auth/register/fan
  ↓
后端处理：
  1. 参数校验
  2. 检查用户名是否已存在
  3. 创建Fan记录
  4. 创建User记录（role=FAN, relatedId=fanId）
  ↓
返回成功
  ↓
前端提示"注册成功！请使用英文名称登录"
  ↓
2秒后自动跳转到登录页
  ↓
用户使用英文名称和密码登录
```

## 数据库变更

### Band表
```sql
INSERT INTO Band (name, founded_at, intro, member_count)
VALUES ('逃跑计划', '2005-01-01', '中国摇滚乐队', 0);
```

### Fan表
```sql
INSERT INTO Fan (name, gender, age, occupation, education)
VALUES ('张伟', 'M', 25, '学生', '本科');
```

### User表
```sql
-- 乐队用户
INSERT INTO User (username, password, role, related_id)
VALUES ('taopaojihua', '123456', 'BAND', 1);

-- 歌迷用户
INSERT INTO User (username, password, role, related_id)
VALUES ('zhangwei', '123456', 'FAN', 1);
```

## 测试验证

### 测试步骤

#### 测试乐队用户注册

1. **访问登录页**：`http://localhost:5173/login`
2. **点击"乐队用户注册"链接**
3. **填写注册信息**：
   - 乐队名称：逃跑计划
   - 英文名称：taopaojihua
   - 成立时间：2005-01-01
   - 乐队简介：中国摇滚乐队
   - 登录密码：123456
   - 确认密码：123456
4. **点击"注册"按钮**
5. **确认提示**："注册成功！请使用英文名称登录"
6. **等待自动跳转**到登录页
7. **使用新账号登录**：
   - 用户名：taopaojihua
   - 密码：123456
   - 角色：乐队用户
8. **确认登录成功**并跳转到乐队管理首页

#### 测试歌迷用户注册

1. **访问登录页**：`http://localhost:5173/login`
2. **点击"歌迷用户注册"链接**
3. **填写注册信息**：
   - 姓名：张伟
   - 英文名称：zhangwei
   - 性别：男
   - 年龄：25
   - 职业：学生
   - 学历：本科
   - 登录密码：123456
   - 确认密码：123456
4. **点击"注册"按钮**
5. **确认提示**："注册成功！请使用英文名称登录"
6. **等待自动跳转**到登录页
7. **使用新账号登录**：
   - 用户名：zhangwei
   - 密码：123456
   - 角色：歌迷用户
8. **确认登录成功**并跳转到歌迷中心首页

#### 测试表单验证

1. **英文名称格式验证**：
   - 输入中文字符
   - 确认显示"只能包含英文字母、数字和下划线"错误提示

2. **密码长度验证**：
   - 输入少于6位的密码
   - 确认显示"密码长度至少6位"错误提示

3. **确认密码一致性验证**：
   - 两次输入不同的密码
   - 确认显示"两次输入的密码不一致"错误提示

4. **用户名重复验证**：
   - 使用已存在的用户名注册
   - 确认显示"用户名已存在"错误提示

### 预期结果

- ✅ 登录页面不再显示测试账号提示
- ✅ 登录页面显示注册链接
- ✅ 乐队注册页面正常显示和工作
- ✅ 歌迷注册页面正常显示和工作
- ✅ 表单验证正常工作
- ✅ 注册成功后自动跳转到登录页
- ✅ 新注册的账号可以正常登录
- ✅ 数据正确保存到数据库

## 修改文件列表

### 前端
1. `frontend/src/views/Login.vue` - 删除测试账号提示，添加注册链接
2. `frontend/src/views/RegisterBand.vue` - 新建乐队注册页面
3. `frontend/src/views/RegisterFan.vue` - 新建歌迷注册页面
4. `frontend/src/router/index.js` - 添加注册页面路由

### 后端
1. `src/main/java/com/band/management/service/AuthService.java` - 添加注册方法接口
2. `src/main/java/com/band/management/service/impl/AuthServiceImpl.java` - 实现注册方法
3. `src/main/java/com/band/management/controller/AuthController.java` - 添加注册API

## 技术要点

1. **事务管理**：使用 `@Transactional` 确保Band/Fan和User记录同时创建成功
2. **数据源切换**：使用 `ADMIN` 数据源进行注册（只有admin_user有INSERT权限）
3. **用户名唯一性**：注册前检查用户名是否已存在
4. **密码存储**：密码以明文形式存储（按用户要求）
5. **关联关系**：User表的relatedId字段指向Band/Fan表的主键
6. **表单验证**：前端使用Element Plus的表单验证，后端也进行参数校验
7. **用户体验**：注册成功后自动跳转，明确提示使用英文名称登录

## 注意事项

1. **用户名格式**：只能包含英文字母、数字和下划线
2. **密码长度**：至少6位
3. **数据源权限**：注册使用ADMIN数据源，确保有INSERT权限
4. **事务回滚**：如果User创建失败，Band/Fan记录也会回滚
5. **登录提示**：注册页面明确提示用户使用英文名称登录
6. **自动跳转**：注册成功2秒后自动跳转到登录页

---

**实现日期**：2025-12-23
**实现人员**：Kiro AI Assistant
