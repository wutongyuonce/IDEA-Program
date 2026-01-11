# 乐队管理系统

## 项目概述

**项目名称**: 乐队管理系统 (Band Management System)  

**技术栈**: Spring Boot 2.7.18 + MyBatis + MySQL + Vue.js  

---

## 目录

[TOC]

***

## 1. 项目架构

### 1.1 项目结构

```
band-management/
├── src/main/java/com/band/management/
│   ├── config/              # 配置类
│   │   ├── DataSourceConfig.java          # 多数据源配置
│   │   ├── DynamicDataSource.java         # 动态数据源
│   │   ├── DataSourceContextHolder.java   # 数据源上下文
│   │   └── SecurityConfig.java            # 安全配置
│   ├── controller/          # 控制器层
│   │   ├── AuthController.java            # 认证控制器
│   │   ├── AdminSongController.java       # 管理员歌曲控制器
│   │   └── ...
│   ├── service/             # 服务层
│   │   ├── AuthService.java               # 认证服务接口
│   │   ├── impl/
│   │   │   └── AuthServiceImpl.java       # 认证服务实现
│   │   └── ...
│   ├── mapper/              # 数据访问层
│   │   ├── UserMapper.java                # 用户Mapper
│   │   ├── SongMapper.java                # 歌曲Mapper
│   │   └── ...
│   ├── entity/              # 实体类
│   ├── dto/                 # 数据传输对象
│   ├── vo/                  # 视图对象
│   ├── common/              # 公共类
│   ├── exception/           # 异常处理
│   └── util/                # 工具类
├── src/main/resources/
│   ├── mapper/              # MyBatis XML映射文件
│   │   ├── SongMapper.xml
│   │   ├── AlbumMapper.xml
│   │   └── ...
│   └── application.yml      # 应用配置
└── frontend/                # Vue.js前端项目
    └── src/
        ├── views/           # 页面组件
        ├── api/             # API请求
        └── router/          # 路由配置
```


---

## 2. 核心配置

### 2.1 应用配置 (application.yml)

```yaml
# 多数据源配置 - 核心特性
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 管理员数据源 - 拥有所有权限
      admin:
        url: jdbc:mysql://localhost:3306/band_management
        username: admin_user
        password: Admin@123456
        driver-class-name: com.mysql.cj.jdbc.Driver
      
      # 乐队用户数据源 - 只能管理自己的数据
      band:
        url: jdbc:mysql://localhost:3306/band_management
        username: band_user
        password: Band@123456
        driver-class-name: com.mysql.cj.jdbc.Driver
      
      # 歌迷用户数据源 - 只能管理个人数据
      fan:
        url: jdbc:mysql://localhost:3306/band_management
        username: fan_user
        password: Fan@123456
        driver-class-name: com.mysql.cj.jdbc.Driver
      
      # 连接池配置
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000

# MyBatis配置
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.band.management.entity
  configuration:
    map-underscore-to-camel-case: true  # 驼峰命名转换
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# PageHelper分页配置
pagehelper:
  helper-dialect: mysql
  reasonable: true
  support-methods-arguments: true
```

### 2.2 Maven依赖 (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.3.1</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- Druid 数据库连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.2.20</version>
    </dependency>

    <!-- PageHelper 分页插件 -->
    <dependency>
        <groupId>com.github.pagehelper</groupId>
        <artifactId>pagehelper-spring-boot-starter</artifactId>
        <version>1.4.7</version>
    </dependency>

    <!-- Pinyin4j 中文转拼音 -->
    <dependency>
        <groupId>com.belerweb</groupId>
        <artifactId>pinyin4j</artifactId>
        <version>2.5.1</version>
    </dependency>
</dependencies>
```


---

## 3. 多数据源实现

### 3.1 数据源上下文持有者

```java
package com.band.management.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据源上下文持有者
 * 使用ThreadLocal存储当前线程使用的数据源类型
 * 
 * 核心功能：
 * 1. 使用ThreadLocal实现线程隔离
 * 2. 根据用户角色动态切换数据源
 * 3. 确保数据库权限隔离
 * 
 * @author Band Management Team
 */
@Slf4j
public class DataSourceContextHolder {

    /**
     * 数据源类型枚举
     */
    public enum DataSourceType {
        ADMIN("admin"),   // 管理员数据源 - 所有权限
        BAND("band"),     // 乐队用户数据源 - 管理乐队数据
        FAN("fan");       // 歌迷用户数据源 - 管理个人数据

        private final String value;

        DataSourceType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // ThreadLocal确保每个线程有独立的数据源设置
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 设置数据源类型
     * 在Service层调用，根据业务需求切换数据源
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        if (dataSourceType == null) {
            log.warn("数据源类型为空，使用默认数据源");
            return;
        }
        log.debug("切换数据源到: {}", dataSourceType.getValue());
        contextHolder.set(dataSourceType.getValue());
    }

    /**
     * 设置数据源类型（通过字符串）
     * 支持从配置或请求参数中动态设置
     */
    public static void setDataSourceType(String dataSourceType) {
        if (dataSourceType == null || dataSourceType.isEmpty()) {
            log.warn("数据源类型为空，使用默认数据源");
            return;
        }
        String type = dataSourceType.toLowerCase();
        log.debug("切换数据源到: {}", type);
        contextHolder.set(type);
    }

    /**
     * 获取数据源类型
     * 由DynamicDataSource调用，确定当前使用哪个数据源
     */
    public static String getDataSourceType() {
        String dataSource = contextHolder.get();
        log.debug("当前数据源: {}", dataSource);
        return dataSource;
    }

    /**
     * 清除数据源类型
     * 请求结束后清理ThreadLocal，防止内存泄漏
     */
    public static void clearDataSourceType() {
        log.debug("清除数据源设置");
        contextHolder.remove();
    }
}
```

### 3.2 动态数据源

```java
package com.band.management.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源
 * 继承AbstractRoutingDataSource实现数据源动态切换
 * 
 * 工作原理：
 * 1. Spring在执行SQL前调用determineCurrentLookupKey()
 * 2. 根据返回的key从targetDataSources中获取对应的数据源
 * 3. 使用该数据源执行SQL
 * 
 * @author Band Management Team
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 确定当前数据源的key
     * 从ThreadLocal中获取当前线程设置的数据源类型
     * 
     * @return 数据源key (admin/band/fan)
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}
```


### 3.3 数据源配置类

```java
package com.band.management.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 * 
 * 核心功能：
 * 1. 创建三个独立的数据源（admin、band、fan）
 * 2. 配置动态数据源，支持运行时切换
 * 3. 实现基于角色的数据库权限隔离
 * 
 * @author Band Management Team
 */
@Configuration
public class DataSourceConfig {

    /**
     * 管理员数据源
     * 拥有所有表的增删改查权限
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.admin")
    public DataSource adminDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 乐队用户数据源
     * 只能管理本乐队的数据，查看歌迷数据
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.band")
    public DataSource bandDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 歌迷用户数据源
     * 只能管理个人数据，查看乐队数据
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.fan")
    public DataSource fanDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 动态数据源（主数据源）
     * 
     * 工作流程：
     * 1. 将三个数据源注册到targetDataSources
     * 2. 设置默认数据源为admin
     * 3. 运行时根据ThreadLocal中的值动态切换
     * 
     * @Primary 注解确保这是主数据源
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        // 配置目标数据源映射
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(
            DataSourceContextHolder.DataSourceType.ADMIN.getValue(), 
            adminDataSource()
        );
        targetDataSources.put(
            DataSourceContextHolder.DataSourceType.BAND.getValue(), 
            bandDataSource()
        );
        targetDataSources.put(
            DataSourceContextHolder.DataSourceType.FAN.getValue(), 
            fanDataSource()
        );
        
        dynamicDataSource.setTargetDataSources(targetDataSources);
        
        // 设置默认数据源（当ThreadLocal中没有值时使用）
        dynamicDataSource.setDefaultTargetDataSource(adminDataSource());
        
        return dynamicDataSource;
    }
}
```

### 3.4 使用示例

```java
// 在Service层方法中切换数据源
@Service
public class AdminSongServiceImpl implements AdminSongService {
    
    @Override
    public PageResult<Song> page(int pageNum, int pageSize, Song condition) {
        // 设置使用管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");
        
        // 执行查询 - 自动使用admin数据源
        PageHelper.startPage(pageNum, pageSize);
        List<Song> list = songMapper.selectByCondition(condition);
        
        return PageResult.of(list);
    }
}
```


---

## 4. 认证与授权

### 4.1 认证控制器

```java
package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.dto.LoginRequest;
import com.band.management.service.AuthService;
import com.band.management.vo.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 * 
 * 核心功能：
 * 1. 用户登录/登出
 * 2. 乐队用户注册（自动生成用户名）
 * 3. 歌迷用户注册（自动生成用户名）
 * 4. 获取当前登录用户信息
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     * 
     * 请求示例：
     * POST /api/auth/login
     * {
     *   "username": "band_taopaojihua",
     *   "password": "band123",
     *   "role": "BAND"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "登录成功",
     *   "data": {
     *     "userId": 1,
     *     "username": "band_taopaojihua",
     *     "role": "BAND",
     *     "relatedId": 1
     *   }
     * }
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("用户登录请求: username={}, role={}", 
                 loginRequest.getUsername(), loginRequest.getRole());
        
        // 调用认证服务进行登录验证
        LoginResponse response = authService.login(loginRequest);
        
        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     * 清除Session中的用户信息
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出请求");
        authService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息
     * 从Session中获取，用于前端判断登录状态
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        // 清除密码字段，避免泄露
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 乐队用户注册
     * 
     * 请求示例：
     * POST /api/auth/register/band
     * {
     *   "name": "新乐队",
     *   "foundedAt": "2024-01-01",
     *   "intro": "乐队简介",
     *   "password": "password123"
     * }
     * 
     * 自动生成用户名规则：band_乐队名称拼音
     * 例如："逃跑计划" -> "band_taopaojihua"
     */
    @PostMapping("/register/band")
    public Result<RegisterResponse> registerBand(
            @RequestBody Map<String, Object> params) {
        
        String name = (String) params.get("name");
        String foundedAt = (String) params.get("foundedAt");
        String intro = (String) params.get("intro");
        String password = (String) params.get("password");
        
        log.info("乐队用户注册请求: name={}", name);
        
        // 调用服务层创建乐队和用户
        RegisterResponse response = authService.registerBand(
            name, foundedAt, intro, password
        );
        
        return Result.success("注册成功", response);
    }

    /**
     * 歌迷用户注册
     * 
     * 自动生成用户名规则：fan_姓名拼音
     * 例如："张三" -> "fan_zhangsan"
     */
    @PostMapping("/register/fan")
    public Result<RegisterResponse> registerFan(
            @RequestBody Map<String, Object> params) {
        
        String name = (String) params.get("name");
        String gender = (String) params.get("gender");
        Integer age = (Integer) params.get("age");
        String occupation = (String) params.get("occupation");
        String education = (String) params.get("education");
        String password = (String) params.get("password");
        
        log.info("歌迷用户注册请求: name={}", name);
        
        // 调用服务层创建歌迷和用户
        RegisterResponse response = authService.registerFan(
            name, gender, age, occupation, education, password
        );
        
        return Result.success("注册成功", response);
    }
}
```


### 4.2 认证服务实现（核心逻辑）

```java
package com.band.management.service.impl;

import com.band.management.config.DataSourceContextHolder;
import com.band.management.dto.LoginRequest;
import com.band.management.entity.User;
import com.band.management.exception.BusinessException;
import com.band.management.exception.ErrorCode;
import com.band.management.mapper.UserMapper;
import com.band.management.service.AuthService;
import com.band.management.util.EncryptUtil;
import com.band.management.vo.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

/**
 * 认证服务实现类
 * 
 * 核心功能：
 * 1. 用户登录验证（支持密码加密和明文）
 * 2. Session管理
 * 3. 数据源动态切换
 * 4. 用户注册（事务管理）
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BandMapper bandMapper;

    @Autowired
    private FanMapper fanMapper;

    // Session中存储用户信息的key
    private static final String SESSION_USER_KEY = "current_user";

    /**
     * 用户登录
     * 
     * 核心流程：
     * 1. 参数校验
     * 2. 根据角色切换数据源
     * 3. 查询用户信息
     * 4. 验证密码（支持加密和明文）
     * 5. 保存用户信息到Session
     * 
     * @param loginRequest 登录请求
     * @return 登录响应（包含用户基本信息）
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录: username={}, role={}", 
                 loginRequest.getUsername(), loginRequest.getRole());

        // 1. 参数校验
        if (StringUtil.isEmpty(loginRequest.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                      "用户名不能为空");
        }
        if (StringUtil.isEmpty(loginRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                      "密码不能为空");
        }

        // 2. 验证角色是否合法
        String role = loginRequest.getRole().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("BAND") && !role.equals("FAN")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                      "角色类型不正确");
        }

        // 3. 根据用户角色设置数据源
        // 注意：band_user 和 fan_user 现在也有 User 表的 SELECT 权限
        DataSourceContextHolder.setDataSourceType(role);

        // 4. 查询用户
        User user = userMapper.selectByUsernameAndRole(
            loginRequest.getUsername(), role
        );
        if (user == null) {
            log.error("用户不存在: username={}, role={}", 
                     loginRequest.getUsername(), role);
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 5. 验证密码
        // 先尝试加密验证（兼容旧的加密密码）
        boolean passwordMatches = EncryptUtil.matchesPassword(
            loginRequest.getPassword(), user.getPassword()
        );
        
        // 如果加密验证失败，尝试明文比较（兼容新的明文密码）
        if (!passwordMatches) {
            passwordMatches = loginRequest.getPassword()
                                         .equals(user.getPassword());
        }
        
        if (!passwordMatches) {
            log.error("密码不匹配: username={}", loginRequest.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 6. 检查账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 7. 保存用户信息到Session
        HttpSession session = getSession();
        session.setAttribute(SESSION_USER_KEY, user);
        session.setAttribute("user_role", role);

        log.info("用户登录成功: userId={}, role={}", user.getUserId(), role);

        // 8. 构建响应
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(role)
                .relatedId(user.getRelatedId())
                .build();
    }

    /**
     * 用户登出
     * 清除Session中的用户信息和数据源设置
     */
    @Override
    public void logout() {
        log.info("用户登出");
        HttpSession session = getSession();
        session.removeAttribute(SESSION_USER_KEY);
        session.removeAttribute("user_role");
        DataSourceContextHolder.clearDataSourceType();
    }

    /**
     * 获取当前登录用户
     * 从Session中获取，如果未登录则抛出异常
     */
    @Override
    public User getCurrentUser() {
        HttpSession session = getSession();
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }
}
```


### 4.3 乐队用户注册（核心业务逻辑）

```java
/**
 * 乐队用户注册
 * 
 * 核心流程：
 * 1. 参数校验
 * 2. 自动生成用户名（中文转拼音）
 * 3. 切换到ADMIN数据源（只有admin有INSERT权限）
 * 4. 创建Band记录
 * 5. 创建User记录
 * 6. 事务提交
 * 
 * @param name 乐队名称
 * @param foundedAt 成立日期
 * @param intro 乐队简介
 * @param password 密码
 * @return 注册响应（包含生成的用户名）
 */
@Override
@Transactional(rollbackFor = Exception.class)
public RegisterResponse registerBand(String name, String foundedAt, 
                                     String intro, String password) {
    log.info("乐队用户注册: name={}", name);

    // 1. 参数校验
    if (StringUtil.isEmpty(name)) {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                   "乐队名称不能为空");
    }
    if (StringUtil.isEmpty(password)) {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                   "密码不能为空");
    }
    if (password.length() < 6) {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                   "密码长度至少6位");
    }

    // 2. 自动生成用户名: band_名称拼音
    // 例如："逃跑计划" -> "band_taopaojihua"
    String username = generateBandUsername(name);

    // 3. 使用 ADMIN 数据源进行注册
    // 原因：只有admin_user有INSERT权限
    DataSourceContextHolder.setDataSourceType("ADMIN");

    // 4. 检查用户名是否已存在
    User existingUser = userMapper.selectByUsername(username);
    if (existingUser != null) {
        throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), 
                                   "用户名已存在，请使用不同的乐队名称");
    }

    // 5. 创建乐队记录
    Band band = new Band();
    band.setName(name);
    band.setFoundedAt(java.sql.Date.valueOf(foundedAt));
    band.setIntro(intro);
    band.setMemberCount(0);  // 初始成员数为0
    
    int bandResult = bandMapper.insert(band);
    if (bandResult <= 0) {
        throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), 
                                   "乐队创建失败");
    }

    // 6. 创建用户记录
    User user = new User();
    user.setUsername(username);
    user.setPassword(password);  // 明文存储
    user.setRole("BAND");
    user.setRelatedId(band.getBandId());  // 关联到乐队ID
    user.setStatus(1);  // 设置状态为启用
    
    int userResult = userMapper.insert(user);
    if (userResult <= 0) {
        throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), 
                                   "用户创建失败");
    }

    log.info("乐队用户注册成功: bandId={}, userId={}, username={}", 
             band.getBandId(), user.getUserId(), username);
    
    // 7. 返回注册响应
    return RegisterResponse.builder()
            .userId(user.getUserId())
            .username(username)  // 返回生成的用户名供用户登录使用
            .relatedId(band.getBandId())
            .role("BAND")
            .build();
}

/**
 * 生成乐队用户名: band_名称拼音
 * 
 * 转换规则：
 * - 中文字符转换为拼音
 * - 英文和数字保持不变
 * - 其他字符移除
 * 
 * 示例：
 * "逃跑计划" -> "taopaojihua" -> "band_taopaojihua"
 * "五月天" -> "wuyuetian" -> "band_wuyuetian"
 */
private String generateBandUsername(String name) {
    String pinyin = convertToPinyin(name);
    return "band_" + pinyin;
}

/**
 * 中文转拼音（使用 pinyin4j 库）
 */
private String convertToPinyin(String text) {
    if (StringUtil.isEmpty(text)) {
        return "";
    }
    return PinyinUtil.toPinyin(text);
}
```


---

## 5. 业务层实现

### 5.1 歌曲管理Service（分页查询示例）

```java
package com.band.management.service.impl;

import com.band.management.common.PageResult;
import com.band.management.config.DataSourceContextHolder;
import com.band.management.entity.Song;
import com.band.management.mapper.SongMapper;
import com.band.management.service.AdminSongService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员歌曲服务实现类
 * 
 * 核心功能：
 * 1. 分页查询（使用PageHelper）
 * 2. 多条件查询（歌曲名称、乐队名称、专辑）
 * 3. 数据源切换
 * 
 * @author Band Management Team
 */
@Slf4j
@Service
public class AdminSongServiceImpl implements AdminSongService {

    @Autowired
    private SongMapper songMapper;

    /**
     * 分页查询歌曲列表
     * 
     * 支持的查询条件：
     * - title: 歌曲名称（模糊查询）
     * - bandName: 乐队名称（模糊查询）
     * - albumId: 所属专辑（精确查询）
     * 
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param condition 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<Song> page(int pageNum, int pageSize, Song condition) {
        // 1. 设置使用管理员数据源
        DataSourceContextHolder.setDataSourceType("admin");

        // 2. 使用PageHelper进行分页
        // 原理：在执行查询前，PageHelper会拦截SQL，
        // 自动添加LIMIT和OFFSET子句
        PageHelper.startPage(pageNum, pageSize);
        
        // 3. 执行查询
        // 如果condition为null，查询所有；否则按条件查询
        List<Song> list = condition == null ? 
                         songMapper.selectAll() : 
                         songMapper.selectByCondition(condition);
        
        // 4. PageHelper自动封装分页结果
        // 包含：total（总记录数）、list（当前页数据）、pages（总页数）
        return PageResult.of(list);
    }

    /**
     * 创建歌曲
     * 
     * 核心逻辑：
     * 1. 参数校验
     * 2. 验证专辑是否存在
     * 3. 插入歌曲记录
     * 4. 事务管理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Song song) {
        log.info("管理员创建歌曲: {}", song.getTitle());

        // 设置数据源
        DataSourceContextHolder.setDataSourceType("admin");

        // 参数校验
        if (StringUtil.isEmpty(song.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                      "歌曲标题不能为空");
        }
        if (song.getAlbumId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                                      "专辑ID不能为空");
        }

        // 验证专辑是否存在
        Album album = albumMapper.selectById(song.getAlbumId());
        if (album == null) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }

        // 插入数据
        int result = songMapper.insert(song);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), 
                                      "创建歌曲失败");
        }

        log.info("管理员创建歌曲成功，ID: {}", song.getSongId());
        return song.getSongId();
    }
}
```

### 5.2 统一响应结果封装

```java
package com.band.management.common;

import lombok.Data;

/**
 * 统一响应结果
 * 
 * 所有API接口都返回此格式，便于前端统一处理
 * 
 * @author Band Management Team
 */
@Data
public class Result<T> {
    
    /**
     * 响应码
     * 200: 成功
     * 400: 参数错误
     * 401: 未登录
     * 403: 无权限
     * 500: 服务器错误
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

### 5.3 分页结果封装

```java
package com.band.management.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 * 
 * 封装PageHelper的分页信息，提供统一的分页响应格式
 * 
 * @author Band Management Team
 */
@Data
public class PageResult<T> {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页数据
     */
    private List<T> list;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 从PageHelper的PageInfo创建分页结果
     * 
     * @param list 查询结果列表（已被PageHelper处理）
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        
        PageResult<T> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        
        return result;
    }
}
```


---

## 6. 数据访问层

### 6.1 MyBatis Mapper接口

```java
package com.band.management.mapper;

import com.band.management.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 歌曲Mapper接口
 * 
 * 使用MyBatis进行数据访问
 * XML配置文件位于：resources/mapper/SongMapper.xml
 * 
 * @author Band Management Team
 */
@Mapper
public interface SongMapper {

    /**
     * 插入歌曲
     * 使用useGeneratedKeys自动获取主键
     */
    int insert(Song song);

    /**
     * 根据ID删除歌曲
     */
    int deleteById(@Param("songId") Long songId);

    /**
     * 更新歌曲信息
     * 使用动态SQL，只更新非空字段
     */
    int update(Song song);

    /**
     * 根据ID查询歌曲
     */
    Song selectById(@Param("songId") Long songId);

    /**
     * 查询所有歌曲
     */
    List<Song> selectAll();

    /**
     * 根据专辑ID查询歌曲列表
     */
    List<Song> selectByAlbumId(@Param("albumId") Long albumId);

    /**
     * 条件查询歌曲列表
     * 支持多条件组合查询
     */
    List<Song> selectByCondition(Song song);
}
```

### 6.2 MyBatis XML映射文件（核心SQL）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.band.management.mapper.SongMapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.band.management.entity.Song">
        <id column="song_id" property="songId" jdbcType="BIGINT"/>
        <result column="album_id" property="albumId" jdbcType="BIGINT"/>
        <result column="title" property="title" jdbcType="VARCHAR"/>
        <result column="lyricist" property="lyricist" jdbcType="VARCHAR"/>
        <result column="composer" property="composer" jdbcType="VARCHAR"/>
        <result column="created_at" property="createdAt" jdbcType="TIMESTAMP"/>
        <result column="updated_at" property="updatedAt" jdbcType="TIMESTAMP"/>
        <!-- 关联查询的字段 -->
        <result column="album_title" property="albumTitle" jdbcType="VARCHAR"/>
        <result column="band_name" property="bandName" jdbcType="VARCHAR"/>
    </resultMap>

    <!-- 基础列 -->
    <sql id="Base_Column_List">
        song_id, album_id, title, lyricist, composer, created_at, updated_at
    </sql>

    <!-- 插入 -->
    <insert id="insert" parameterType="com.band.management.entity.Song" 
            useGeneratedKeys="true" keyProperty="songId">
        INSERT INTO Song (album_id, title, lyricist, composer)
        VALUES (#{albumId}, #{title}, #{lyricist}, #{composer})
    </insert>

    <!-- 根据ID删除 -->
    <delete id="deleteById" parameterType="long">
        DELETE FROM Song WHERE song_id = #{songId}
    </delete>

    <!-- 动态更新 -->
    <update id="update" parameterType="com.band.management.entity.Song">
        UPDATE Song
        <set>
            <if test="albumId != null">album_id = #{albumId},</if>
            <if test="title != null and title != ''">title = #{title},</if>
            <if test="lyricist != null">lyricist = #{lyricist},</if>
            <if test="composer != null">composer = #{composer},</if>
        </set>
        WHERE song_id = #{songId}
    </update>

    <!-- 条件查询（核心SQL） -->
    <!-- 
        功能：多条件组合查询
        支持的条件：
        1. albumId: 按专辑筛选
        2. title: 按歌曲名称模糊查询
        3. bandName: 按乐队名称模糊查询（新增）
        
        关联查询：
        - LEFT JOIN Album: 获取专辑名称
        - LEFT JOIN Band: 获取乐队名称
    -->
    <select id="selectByCondition" 
            parameterType="com.band.management.entity.Song" 
            resultMap="BaseResultMap">
        SELECT 
            s.song_id, s.album_id, s.title, s.lyricist, s.composer, 
            s.created_at, s.updated_at,
            a.title AS album_title,
            b.name AS band_name
        FROM Song s
        LEFT JOIN Album a ON s.album_id = a.album_id
        LEFT JOIN Band b ON a.band_id = b.band_id
        <where>
            <!-- 按专辑筛选 -->
            <if test="albumId != null">
                AND s.album_id = #{albumId}
            </if>
            <!-- 按歌曲名称模糊查询 -->
            <if test="title != null and title != ''">
                AND s.title LIKE CONCAT('%', #{title}, '%')
            </if>
            <!-- 按乐队名称模糊查询（新增功能） -->
            <if test="bandName != null and bandName != ''">
                AND b.name LIKE CONCAT('%', #{bandName}, '%')
            </if>
        </where>
        ORDER BY s.song_id DESC
    </select>

    <!-- 查询所有歌曲 -->
    <select id="selectAll" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM Song
        ORDER BY song_id DESC
    </select>

    <!-- 根据专辑ID查询 -->
    <select id="selectByAlbumId" parameterType="long" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM Song
        WHERE album_id = #{albumId}
        ORDER BY song_id ASC
    </select>

</mapper>
```

## 7. 前端核心代码

### 7.1 API请求封装

```javascript
// frontend/src/api/request.js

import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 创建axios实例
 * 
 * 配置说明：
 * - baseURL: 后端API基础路径
 * - timeout: 请求超时时间
 * - withCredentials: 携带Cookie（Session认证必需）
 */
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  withCredentials: true  // 重要：携带Cookie进行Session认证
})

/**
 * 请求拦截器
 * 在发送请求前执行
 */
request.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 在收到响应后执行
 * 
 * 统一处理：
 * 1. 业务错误（code != 200）
 * 2. HTTP错误（401、403、500等）
 * 3. 网络错误
 */
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 业务成功
    if (res.code === 200) {
      return res
    }
    
    // 业务失败
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  error => {
    console.error('响应错误:', error)
    
    // HTTP错误处理
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未登录或登录已过期')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default request
```

### 7.2 认证API

```javascript
// frontend/src/api/auth.js

import request from './request'

/**
 * 用户登录
 * 
 * @param {Object} data 登录信息
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @param {string} data.role 角色（ADMIN/BAND/FAN）
 * @returns {Promise} 登录响应
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/auth/current',
    method: 'get'
  })
}

/**
 * 乐队用户注册
 */
export function registerBand(data) {
  return request({
    url: '/auth/register/band',
    method: 'post',
    data
  })
}

/**
 * 歌迷用户注册
 */
export function registerFan(data) {
  return request({
    url: '/auth/register/fan',
    method: 'post',
    data
  })
}
```


### 7.3 管理员歌曲管理页面（核心组件）

```vue
<!-- frontend/src/views/admin/Songs.vue -->

<template>
  <div class="songs-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>歌曲管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加歌曲
          </el-button>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <!-- 歌曲名称搜索 -->
        <el-form-item label="歌曲名称">
          <el-input 
            v-model="searchForm.title" 
            placeholder="请输入歌曲名称" 
            clearable 
          />
        </el-form-item>
        
        <!-- 乐队名称搜索（新增功能） -->
        <el-form-item label="乐队名称">
          <el-input 
            v-model="searchForm.bandName" 
            placeholder="请输入乐队名称" 
            clearable 
          />
        </el-form-item>
        
        <!-- 所属专辑筛选 -->
        <el-form-item label="所属专辑">
          <el-select 
            v-model="searchForm.albumId" 
            placeholder="请选择专辑" 
            clearable 
            filterable
          >
            <el-option
              v-for="album in albumList"
              :key="album.albumId"
              :label="`${album.title} (${album.bandName})`"
              :value="album.albumId"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 数据表格 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="歌曲名称" width="200" />
        <el-table-column prop="albumTitle" label="所属专辑" width="180" />
        <el-table-column prop="bandName" label="所属乐队" width="150" />
        <el-table-column prop="lyricist" label="作词" width="120" />
        <el-table-column prop="composer" label="作曲" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="所属专辑" prop="albumId">
          <el-select 
            v-model="formData.albumId" 
            placeholder="请选择专辑" 
            style="width: 100%" 
            filterable
          >
            <el-option
              v-for="album in albumList"
              :key="album.albumId"
              :label="`${album.title} (${album.bandName})`"
              :value="album.albumId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="歌曲名称" prop="title">
          <el-input v-model="formData.title" placeholder="请输入歌曲名称" />
        </el-form-item>
        <el-form-item label="作词" prop="lyricist">
          <el-input v-model="formData.lyricist" placeholder="请输入作词者" />
        </el-form-item>
        <el-form-item label="作曲" prop="composer">
          <el-input v-model="formData.composer" placeholder="请输入作曲者" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加歌曲')
const formRef = ref(null)
const albumList = ref([])

// 搜索表单
const searchForm = reactive({
  title: '',        // 歌曲名称
  bandName: '',     // 乐队名称（新增）
  albumId: null     // 所属专辑
})

// 分页配置
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

// 表单数据
const formData = reactive({
  songId: null,
  albumId: null,
  title: '',
  lyricist: '',
  composer: ''
})

// 表单验证规则
const rules = {
  albumId: [
    { required: true, message: '请选择所属专辑', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入歌曲名称', trigger: 'blur' }
  ]
}

/**
 * 加载专辑列表
 * 用于下拉选择框
 */
const loadAlbumList = async () => {
  try {
    const res = await request.get('/admin/albums/list')
    albumList.value = res.data || []
  } catch (error) {
    console.error('加载专辑列表失败:', error)
  }
}

/**
 * 加载歌曲数据
 * 支持分页和多条件查询
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/songs', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        title: searchForm.title,
        bandName: searchForm.bandName,  // 新增参数
        albumId: searchForm.albumId
      }
    })
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 * 重置到第一页并查询
 */
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  searchForm.title = ''
  searchForm.bandName = ''  // 重置新增字段
  searchForm.albumId = null
  handleSearch()
}

/**
 * 添加歌曲
 */
const handleAdd = () => {
  dialogTitle.value = '添加歌曲'
  Object.assign(formData, {
    songId: null,
    albumId: null,
    title: '',
    lyricist: '',
    composer: ''
  })
  dialogVisible.value = true
}

/**
 * 编辑歌曲
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑歌曲'
  Object.assign(formData, row)
  dialogVisible.value = true
}

/**
 * 删除歌曲
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除歌曲"${row.title}"吗？`, 
      '提示', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.delete(`/admin/songs/${row.songId}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.songId) {
          // 更新
          await request.put(`/admin/songs/${formData.songId}`, formData)
          ElMessage.success('更新成功')
        } else {
          // 添加
          await request.post('/admin/songs', formData)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

/**
 * 对话框关闭时重置表单
 */
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 组件挂载时加载数据
onMounted(() => {
  loadAlbumList()
  loadData()
})
</script>

<style scoped>
.songs-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
```


---

## 8. 核心工具类

### 8.1 中文转拼音工具

```java
package com.band.management.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具类
 * 使用pinyin4j库将中文转换为拼音
 * 
 * 应用场景：
 * 1. 自动生成用户名（乐队注册、歌迷注册）
 * 2. 搜索优化
 * 
 * @author Band Management Team
 */
public class PinyinUtil {

    /**
     * 中文转拼音
     * 
     * 转换规则：
     * - 中文字符转换为小写拼音（无声调）
     * - 英文和数字保持不变
     * - 其他字符（空格、标点等）移除
     * 
     * 示例：
     * "逃跑计划" -> "taopaojihua"
     * "五月天" -> "wuyuetian"
     * "Beyond乐队" -> "beyondledui"
     * "张三123" -> "zhangsan123"
     * 
     * @param text 输入文本
     * @return 拼音字符串
     */
    public static String toPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 配置拼音输出格式
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);  // 小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  // 无声调

        StringBuilder result = new StringBuilder();

        // 遍历每个字符
        for (char c : text.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                // 中文字符
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        result.append(pinyinArray[0]);  // 取第一个拼音（多音字）
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    result.append(c);
                }
            } else if (Character.isLetterOrDigit(c)) {
                // 英文字母或数字，保持不变
                result.append(Character.toLowerCase(c));
            }
            // 其他字符（空格、标点等）忽略
        }

        return result.toString();
    }

    /**
     * 获取首字母
     * 
     * 示例：
     * "逃跑计划" -> "tpjh"
     * "五月天" -> "wyt"
     */
    public static String getFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        result.append(pinyinArray[0].charAt(0));  // 取首字母
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    result.append(c);
                }
            } else if (Character.isLetter(c)) {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }
}
```

### 8.2 密码加密工具

```java
package com.band.management.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密
 * 
 * BCrypt特点：
 * 1. 单向加密，无法解密
 * 2. 每次加密结果不同（加盐）
 * 3. 验证时使用matches方法
 * 
 * @author Band Management Team
 */
public class EncryptUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * 
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * 
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

### 8.3 字符串工具类

```java
package com.band.management.util;

/**
 * 字符串工具类
 * 
 * @author Band Management Team
 */
public class StringUtil {

    /**
     * 判断字符串是否为空
     * 
     * @param str 字符串
     * @return true-为空，false-不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 去除字符串两端空格
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 字符串默认值
     * 如果字符串为空，返回默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
}
```

---

## 9. 异常处理

### 9.1 业务异常类

```java
package com.band.management.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于业务逻辑中的异常情况
 * 
 * @author Band Management Team
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
```

### 9.2 错误码枚举

```java
package com.band.management.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * @author Band Management Team
 */
@Getter
public enum ErrorCode {

    // 通用错误
    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统错误"),

    // 业务错误
    LOGIN_FAILED(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    DATA_NOT_FOUND(2001, "数据不存在"),
    DATA_ALREADY_EXISTS(2002, "数据已存在"),
    OPERATION_FAILED(3001, "操作失败"),
    
    // 具体业务错误
    BAND_NOT_FOUND(4001, "乐队不存在"),
    ALBUM_NOT_FOUND(4002, "专辑不存在"),
    SONG_NOT_FOUND(4003, "歌曲不存在"),
    FAN_NOT_FOUND(4004, "歌迷不存在");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

### 9.3 全局异常处理器

```java
package com.band.management.exception;

import com.band.management.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准格式
 * 
 * @author Band Management Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常: {}", e.getMessage());
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误，请联系管理员");
    }
}
```


---

## 10. 核心特性总结

### 10.1 多数据源动态切换

**实现原理**：
1. 配置三个独立的数据源（admin、band、fan）
2. 使用`AbstractRoutingDataSource`实现动态路由
3. 通过`ThreadLocal`存储当前线程的数据源类型
4. 在Service层方法中根据业务需求切换数据源

**优势**：
- 数据库层面的权限隔离
- 不同角色使用不同的数据库用户
- 提高系统安全性

**使用示例**：
```java
// 在Service方法中切换数据源
DataSourceContextHolder.setDataSourceType("admin");
// 后续的数据库操作自动使用admin数据源
```

### 10.2 Session认证机制

**实现原理**：
1. 用户登录成功后，将用户信息存储到HttpSession
2. 后续请求通过Cookie携带JSESSIONID
3. 服务端从Session中获取用户信息
4. 前端配置`withCredentials: true`自动携带Cookie

**优势**：
- 简单易用，无需额外的token管理
- Spring Boot自动管理Session生命周期
- 支持分布式Session（可配置Redis）

**关键配置**：
```yaml
# application.yml
server:
  servlet:
    session:
      timeout: 30m  # Session超时时间
```

```javascript
// 前端axios配置
const request = axios.create({
  withCredentials: true  // 携带Cookie
})
```

### 10.3 自动生成用户名

**实现原理**：
1. 使用pinyin4j库将中文转换为拼音
2. 根据角色添加前缀（band_/fan_）
3. 注册时自动生成，返回给用户

**转换规则**：
- 中文 → 拼音（小写，无声调）
- 英文/数字 → 保持不变
- 其他字符 → 移除

**示例**：
```
"逃跑计划" → "band_taopaojihua"
"张三" → "fan_zhangsan"
"五月天" → "band_wuyuetian"
```

### 10.4 分页查询

**实现原理**：
1. 使用PageHelper插件
2. 在查询前调用`PageHelper.startPage()`
3. PageHelper自动拦截SQL，添加LIMIT子句
4. 自动封装分页结果

**使用示例**：
```java
// Service层
PageHelper.startPage(pageNum, pageSize);
List<Song> list = songMapper.selectByCondition(condition);
return PageResult.of(list);
```

### 10.5 多条件动态查询

**实现原理**：
1. 使用MyBatis动态SQL（`<if>`标签）
2. 根据条件是否为空动态拼接WHERE子句
3. 支持多条件组合查询

**示例**：
```xml
<where>
    <if test="title != null and title != ''">
        AND title LIKE CONCAT('%', #{title}, '%')
    </if>
    <if test="bandName != null and bandName != ''">
        AND b.name LIKE CONCAT('%', #{bandName}, '%')
    </if>
</where>
```

## 11. 开发规范

### 11.1 代码规范

1. **命名规范**
   - 类名：大驼峰（PascalCase）
   - 方法名/变量名：小驼峰（camelCase）
   - 常量：全大写下划线分隔（UPPER_SNAKE_CASE）
   - 数据库表名/字段名：小写下划线分隔（snake_case）

2. **注释规范**
   - 类注释：说明类的功能和作用
   - 方法注释：说明方法的功能、参数、返回值
   - 关键代码：添加行内注释说明逻辑

3. **异常处理**
   - 使用自定义业务异常
   - 统一异常处理器捕获
   - 返回标准错误格式

### 11.2 数据库规范

1. **表设计**
   - 主键使用BIGINT AUTO_INCREMENT
   - 外键使用BIGINT
   - 时间字段使用TIMESTAMP，默认CURRENT_TIMESTAMP

2. **索引设计**
   - 主键自动创建索引
   - 外键字段创建索引
   - 常用查询字段创建索引

3. **权限设计**
   - admin_user：所有权限
   - band_user：管理乐队数据，查看歌迷数据
   - fan_user：管理个人数据，查看乐队数据

### 11.3 API规范

1. **RESTful风格**
   - GET：查询
   - POST：创建
   - PUT：更新
   - DELETE：删除

2. **URL设计**
   - 基础路径：`/api`
   - 资源路径：`/api/admin/songs`
   - 参数传递：查询参数或路径参数

3. **响应格式**
   ```json
   {
     "code": 200,
     "message": "操作成功",
     "data": {}
   }
   ```

---

## 12. 项目亮点

1. **多数据源动态切换**：实现数据库层面的权限隔离
2. **自动生成用户名**：中文转拼音，提升用户体验
3. **统一响应格式**：便于前端统一处理
4. **全局异常处理**：统一异常处理，返回标准错误格式
5. **分页查询优化**：使用PageHelper简化分页逻辑
6. **关联查询优化**：避免N+1查询问题
7. **Session认证**：简单易用的认证机制
8. **代码规范**：遵循阿里巴巴Java开发手册

***

## 13. 功能截图展示

### 管理员

1、登录

![image-20251224224504506](img/image-20251224224504506.png)

2、数据统计

![img](img/clip_image002.png)

3、乐队管理

![img](img/clip_image004.png)

> 所有类似的表
>
> * 都可以调整一页显示多少条记录；
> * 都提供查询功能，支持模糊查询，比如只输入一个字。
>
> 查询名称中带有a字母的乐队：
>
> ![img](img/clip_image010.png)

（1）编辑乐队信息（乐队名称、成立时间年月日、队长从现役队员中选择、简介）

![img](img/clip_image016.png)

（2）删除乐队（需要保证所有成员已经删除否则无法删除）

![img](img/clip_image030.png)

![img](img/clip_image034.png)

（3）添加乐队

![img](img/clip_image038.png)

4、成员管理

![img](img/clip_image042.png)

（1）编辑成员（修改所属乐队且只能在已有乐队中选择、姓名、性别、出生日期、角色、加入日期、离队日期可选）

![img](img/clip_image052.png)

（2）删除成员

![img](img/clip_image066.png)

（3）添加成员（设定所属乐队却只能在已有乐队中选择、姓名、出生日期、角色、加入日期、离队日期可选）

![img](img/clip_image070.png)

5、专辑管理

![img](img/clip_image074.png)

（1）编辑专辑（修改所属乐队，专辑名称，发行日期，简介）

![img](img/clip_image082.png)

（2）删除专辑

![img](img/clip_image090.png)

（3）为已有乐队添加专辑

![img](img/clip_image096.png)

6、歌曲管理

![img](img/clip_image100.png)

（1）编辑歌曲（修改所属专辑、名称、作词、作曲）

![img](img/clip_image110.png)

（2）删除歌曲

![img](img/clip_image116.png) 

（3）为已有专辑添加歌曲

![img](img/clip_image120.png)

7、演唱会管理

![img](img/clip_image124.png)

（1）编辑演唱会（修改所属乐队，演唱会名称，场地，演出时间）

![img](img/clip_image136.png)

（2）删除演唱会

![img](img/clip_image140.png)

（3）添加演唱会

![img](img/clip_image144.png)

8、歌迷管理

![img](img/clip_image148.png)

（1）编辑歌迷（修改姓名、性别、年龄、职业和学历）

![img](img/clip_image158.png) 

（2）删除歌迷

![img](img/clip_image162.png)

（3）添加歌迷

![img](img/clip_image166.png)

9、乐评管理

![img](img/clip_image170.png)

（1）查看操作可以查看完整信息

![img](img/clip_image176.png)

（2）删除乐评

![img](img/clip_image178.png)

### 乐队用户

1、登录与注册

![img](img/clip_image002-1766587461999-168.png)

![img](img/clip_image004-1766587461999-166.png)

2、乐队管理首页

![img](img/clip_image006.png)

（1）编辑乐队信息

![img](img/clip_image008.png)

乐队名称、成立时间在注册时候设定，无法修改

成员数量、专辑数量、歌曲数量自动统计更新

3、成员管理

![img](img/clip_image010-1766587461999-165.png)

（1）编辑成员（修改姓名、出生日期、角色、加入日期、离队日期）

![img](img/clip_image012.png)

（2）删除成员

![img](img/clip_image014.png)

删除后主页成员数量减少

![img](img/clip_image016-1766587461999-167.png)

（3）添加成员

![img](img/clip_image018.png) 

4、专辑管理

![img](img/clip_image020.png)

（1）查看专辑歌曲

![img](img/clip_image022.png)

（2）查看评论

![img](img/clip_image024.png)

（3）编辑专辑（修改名称、发行日期、简介）

![img](img/clip_image026.png)

（4）删除专辑

![img](img/clip_image028.png)

（5）添加专辑

![img](img/clip_image030-1766587462001-169.png)

5、歌曲管理

![img](img/clip_image032.png)

（1）编辑歌曲（修改歌名、所属专辑、作词）

![img](img/clip_image034-1766587462002-170.png)

（2）删除歌曲

![img](img/clip_image036.png)

（3）添加歌曲

![img](img/clip_image038-1766587462002-171.png)

6、管理演唱会信息

![img](img/clip_image040.png)

（1）编辑演唱会（修改名称、地点、时间）

![img](img/clip_image042-1766587462002-172.png)

（2）删除演唱会

![img](img/clip_image044.png)

（3）添加演唱会

![img](img/clip_image046.png)

7、查看歌迷信息

![img](img/clip_image048.png)

![img](img/clip_image050.png)

### 歌迷用户

1、登录与注册

![img](img/clip_image052-1766587462002-173.png)

![img](img/clip_image056.png)

2、歌迷中心首页

* 有关注收藏数据统计

* 有热门乐队推荐（关注人数前5）与专辑排行榜（评分前10），都会动态更新，可以对其中的乐队和专辑进行关注收藏

![img](img/clip_image092.png)

3、个人信息

* 可以修改个人信息（姓名、性别、年龄、职业、学历）

  ![img](img/clip_image058.png)

* 可以修改密码，删除歌迷用户账户

  ![img](img/clip_image060.png)

4、我的喜好

* 可以取消关注已关注乐队

  ![img](img/clip_image064.png)

* 可以取消收藏已收藏的专辑和歌曲

  ![img](img/clip_image066-1766587462002-175.png)

  ![img](img/clip_image068.png)

* 可以添加或删除演唱会记录

  ![img](img/clip_image086.png)

  ![img](img/clip_image070-1766587462002-174.png)

5、发现界面（发现乐队、专辑、歌曲）

可以进入发现界面查看搜索其他乐队、专辑、歌曲并对感兴趣的进行关注和收藏，操作完成后该条会从列表除去

![img](img/clip_image072.png)

![img](img/clip_image078.png)

![img](img/clip_image082-1766587462002-177.png)

6、我的乐评

![img](img/clip_image094.png)

（1）编辑乐评

可以修改评分和评论，评论字数必须大于10

![img](img/clip_image096-1766587462002-179.png)

（2）添加乐评

![9dd06a4aa41df44f32a221784f3f887b](img/9dd06a4aa41df44f32a221784f3f887b.png)
