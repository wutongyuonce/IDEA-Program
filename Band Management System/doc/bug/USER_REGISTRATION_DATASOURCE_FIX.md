# 用户注册数据源切换问题修复

## 问题描述

在用户注册功能中，乐队用户和歌迷用户的注册操作有时成功，有时失败，表现为间歇性故障。

### 错误信息

```
INSERT command denied to user 'band_user'@'localhost' for table 'fan'
```

或

```
INSERT command denied to user 'fan_user'@'localhost' for table 'band'
```

### 问题表现

- 注册功能不稳定，同样的操作有时成功有时失败
- 失败时提示权限不足，使用了错误的数据库用户（band_user 或 fan_user）
- 成功时使用的是正确的 admin_user

## 问题原因

### 根本原因

Spring 的 `@Transactional` 注解会在方法执行**之前**就开启事务并获取数据库连接。这意味着：

1. 当 `@Transactional` 方法被调用时，Spring AOP 拦截器首先执行
2. 拦截器开启事务，此时会根据当前 ThreadLocal 中的数据源类型获取对应的数据库连接
3. 然后才执行方法体内的代码

### 问题代码

**原来的实现（错误）：**

```java
// AuthServiceImpl.java
@Override
@Transactional(rollbackFor = Exception.class)
public RegisterResponse registerBand(String name, ...) {
    // 此时事务已经开启，数据库连接已经获取
    // 这里设置数据源已经太晚了！
    DataSourceContextHolder.setDataSourceType("ADMIN");
    
    // 后续的数据库操作仍然使用之前获取的连接
    bandMapper.insert(band);
    userMapper.insert(user);
}
```

### 时序问题

```
错误的执行顺序：
1. 调用 registerBand() 方法
2. @Transactional 拦截器执行
3. 根据当前 ThreadLocal 获取数据源（可能是 band/fan/null）
4. 获取数据库连接（使用错误的用户）
5. 执行方法体
6. 设置数据源为 ADMIN（已经太晚了！）
7. 执行 INSERT 操作（使用步骤4获取的错误连接）
8. 权限错误！
```

### 为什么有时成功有时失败？

因为 ThreadLocal 中的数据源类型取决于之前的请求：

- 如果上一个请求是管理员操作，ThreadLocal 中是 "admin"，注册就成功
- 如果上一个请求是乐队/歌迷操作，ThreadLocal 中是 "band"/"fan"，注册就失败
- 如果是新线程，ThreadLocal 为空，使用默认数据源，可能成功也可能失败

## 解决方案

### 修复策略

将数据源切换从 Service 层移到 Controller 层，在调用 Service 方法**之前**设置数据源。

### 正确的执行顺序

```
正确的执行顺序：
1. Controller 接收请求
2. 设置数据源为 ADMIN（在 ThreadLocal 中）
3. 调用 registerBand() 方法
4. @Transactional 拦截器执行
5. 根据 ThreadLocal 获取数据源（此时是 ADMIN）
6. 获取数据库连接（使用 admin_user）
7. 执行方法体
8. 执行 INSERT 操作（使用正确的 admin_user 连接）
9. 成功！
```

## 代码修改

### 1. AuthController.java - 乐队注册

**修改前：**
```java
@PostMapping("/register/band")
public Result<RegisterResponse> registerBand(@RequestBody Map<String, Object> params) {
    String name = (String) params.get("name");
    String foundedAt = (String) params.get("foundedAt");
    String intro = (String) params.get("intro");
    String password = (String) params.get("password");
    
    log.info("乐队用户注册请求: name={}", name);
    RegisterResponse response = authService.registerBand(name, foundedAt, intro, password);
    return Result.success("注册成功", response);
}
```

**修改后：**
```java
@PostMapping("/register/band")
public Result<RegisterResponse> registerBand(@RequestBody Map<String, Object> params) {
    String name = (String) params.get("name");
    String foundedAt = (String) params.get("foundedAt");
    String intro = (String) params.get("intro");
    String password = (String) params.get("password");
    
    log.info("乐队用户注册请求: name={}", name);
    // 在调用服务方法之前设置数据源为ADMIN，确保整个事务使用admin数据源
    DataSourceContextHolder.setDataSourceType("ADMIN");
    RegisterResponse response = authService.registerBand(name, foundedAt, intro, password);
    return Result.success("注册成功", response);
}
```

### 2. AuthController.java - 歌迷注册

**修改前：**
```java
@PostMapping("/register/fan")
public Result<RegisterResponse> registerFan(@RequestBody Map<String, Object> params) {
    String name = (String) params.get("name");
    String gender = (String) params.get("gender");
    Integer age = (Integer) params.get("age");
    String occupation = (String) params.get("occupation");
    String education = (String) params.get("education");
    String password = (String) params.get("password");
    
    log.info("歌迷用户注册请求: name={}", name);
    RegisterResponse response = authService.registerFan(name, gender, age, occupation, education, password);
    return Result.success("注册成功", response);
}
```

**修改后：**
```java
@PostMapping("/register/fan")
public Result<RegisterResponse> registerFan(@RequestBody Map<String, Object> params) {
    String name = (String) params.get("name");
    String gender = (String) params.get("gender");
    Integer age = (Integer) params.get("age");
    String occupation = (String) params.get("occupation");
    String education = (String) params.get("education");
    String password = (String) params.get("password");
    
    log.info("歌迷用户注册请求: name={}", name);
    // 在调用服务方法之前设置数据源为ADMIN，确保整个事务使用admin数据源
    DataSourceContextHolder.setDataSourceType("ADMIN");
    RegisterResponse response = authService.registerFan(name, gender, age, occupation, education, password);
    return Result.success("注册成功", response);
}
```

### 3. AuthServiceImpl.java - 移除冗余代码

**registerBand() 方法修改：**
```java
// 修改前
String username = generateBandUsername(name);
// 使用 ADMIN 数据源进行注册
DataSourceContextHolder.setDataSourceType("ADMIN");
// 检查用户名是否已存在

// 修改后
String username = generateBandUsername(name);
// 数据源已在Controller层设置，这里不再重复设置
// 检查用户名是否已存在
```

**registerFan() 方法修改：**
```java
// 修改前
String username = generateFanUsername(name);
// 使用 ADMIN 数据源进行注册
DataSourceContextHolder.setDataSourceType("ADMIN");
// 检查用户名是否已存在

// 修改后
String username = generateFanUsername(name);
// 数据源已在Controller层设置，这里不再重复设置
// 检查用户名是否已存在
```

## 修改文件清单

1. `src/main/java/com/band/management/controller/AuthController.java`
   - 在 `registerBand()` 方法中添加数据源设置
   - 在 `registerFan()` 方法中添加数据源设置

2. `src/main/java/com/band/management/service/impl/AuthServiceImpl.java`
   - 从 `registerBand()` 方法中移除数据源设置代码
   - 从 `registerFan()` 方法中移除数据源设置代码

## 技术要点

### Spring 事务管理机制

1. **AOP 代理**：`@Transactional` 通过 AOP 实现，会在目标方法执行前后添加事务管理逻辑
2. **连接获取时机**：事务开启时就会获取数据库连接，而不是在执行 SQL 时
3. **ThreadLocal 作用域**：数据源类型存储在 ThreadLocal 中，在同一个线程内有效

### 多数据源切换原理

```java
// 数据源路由器会在获取连接时调用此方法
@Override
protected Object determineCurrentLookupKey() {
    // 从 ThreadLocal 中获取当前数据源类型
    return DataSourceContextHolder.getDataSourceType();
}
```

### 最佳实践

1. **数据源切换时机**：必须在事务开启之前设置数据源
2. **切换位置**：
   - ✅ 在 Controller 层调用 Service 之前
   - ✅ 在 AOP 切面中（如果有自定义切面）
   - ❌ 在 `@Transactional` 方法内部（太晚了）
3. **清理时机**：事务结束后应清理 ThreadLocal，避免内存泄漏

## 验证方法

### 测试步骤

1. **乐队注册测试**：
   ```bash
   curl -X POST http://localhost:8080/api/auth/register/band \
     -H "Content-Type: application/json" \
     -d '{
       "name": "测试乐队",
       "foundedAt": "2024-01-01",
       "intro": "这是一个测试乐队",
       "password": "123456"
     }'
   ```

2. **歌迷注册测试**：
   ```bash
   curl -X POST http://localhost:8080/api/auth/register/fan \
     -H "Content-Type: application/json" \
     -d '{
       "name": "测试歌迷",
       "gender": "男",
       "age": 25,
       "occupation": "学生",
       "education": "本科",
       "password": "123456"
     }'
   ```

3. **重复测试**：多次执行上述请求，确保每次都能成功

### 预期结果

- ✅ 每次注册都应该成功
- ✅ 不再出现权限错误
- ✅ 日志显示使用 admin_user 执行 INSERT 操作

### 日志验证

修复后的日志应该显示：
```
切换数据源到: admin
当前数据源: admin
乐队用户注册成功: bandId=1, userId=1, username=band_ceshiyuedui
```

## 影响范围

### 受影响的功能

- ✅ 乐队用户注册（`/api/auth/register/band`）
- ✅ 歌迷用户注册（`/api/auth/register/fan`）

### 不受影响的功能

- 用户登录
- 用户登出
- 管理员注册（原本就在 Service 层设置数据源，但也存在同样问题）

### 建议后续优化

考虑对 `register()` 方法也进行类似修改，确保一致性：

```java
@PostMapping("/register")
public Result<Long> register(@RequestBody @Valid User user) {
    log.info("用户注册请求: username={}, role={}", user.getUsername(), user.getRole());
    // 建议添加：确保使用 ADMIN 数据源
    DataSourceContextHolder.setDataSourceType("ADMIN");
    Long userId = authService.register(user);
    return Result.success("注册成功", userId);
}
```

## 总结

这个问题是一个典型的 **Spring 事务管理与动态数据源切换的时序问题**。关键在于理解：

1. `@Transactional` 注解会在方法执行前就获取数据库连接
2. 数据源切换必须在事务开启之前完成
3. ThreadLocal 的值在获取连接时就被读取，之后修改无效

通过将数据源切换逻辑从 Service 层移到 Controller 层，确保了在事务开启前就设置好正确的数据源，从而彻底解决了注册功能的间歇性失败问题。

## 修复日期

2026-01-15

## 修复人员

Band Management Team
