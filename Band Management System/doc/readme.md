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

### 3.1 实现架构

本系统采用**动态数据源切换**机制，实现基于角色的数据库权限隔离：

**核心组件**：
1. **DataSourceContextHolder** - 数据源上下文持有者
   - 使用 ThreadLocal 存储当前线程的数据源类型
   - 提供数据源类型的设置、获取和清除方法
   - 确保线程安全，防止数据源混乱

2. **DynamicDataSource** - 动态数据源
   - 继承 Spring 的 AbstractRoutingDataSource
   - 重写 determineCurrentLookupKey() 方法
   - 根据 ThreadLocal 中的值动态返回数据源 key

3. **DataSourceConfig** - 数据源配置类
   - 创建三个独立的 Druid 数据源（admin、band、fan）
   - 配置动态数据源，注册所有数据源到 targetDataSources
   - 设置默认数据源为 admin

### 3.2 工作流程

```
用户请求 → Service层设置数据源类型 → ThreadLocal存储 
→ MyBatis执行SQL → DynamicDataSource获取数据源key 
→ 使用对应数据源执行 → 请求结束清理ThreadLocal
```

### 3.3 权限隔离

| 数据源 | 用户类型 | 权限范围 |
|--------|---------|---------|
| admin_user | 管理员 | 所有表的增删改查权限 |
| band_user | 乐队用户 | 管理本乐队数据，查看歌迷数据 |
| fan_user | 歌迷用户 | 管理个人数据，查看乐队数据 |

---

## 4. 认证与授权

### 4.1 认证机制

**Session + Cookie 认证方式**：
- 用户登录成功后，服务器创建 Session 并返回 JSESSIONID Cookie
- 后续请求携带 Cookie，服务器从 Session 中获取用户信息
- Session 默认超时时间 30 分钟

**登录流程**：
1. 前端提交用户名、密码、角色
2. 后端根据角色切换数据源
3. 查询用户信息并验证密码（支持加密和明文）
4. 验证通过后将用户信息存入 Session
5. 返回用户基本信息给前端

### 4.2 用户注册

**自动生成用户名**：
- 乐队用户：`band_` + 乐队名称拼音（如：band_taopaojihua）
- 歌迷用户：`fan_` + 姓名拼音（如：fan_zhangsan）
- 使用 pinyin4j 库实现中文转拼音

**注册流程**：
1. 参数校验（名称、密码等）
2. 生成用户名并检查是否已存在
3. 切换到 ADMIN 数据源（只有 admin 有 INSERT 权限）
4. 创建实体记录（Band/Fan）
5. 创建用户记录并关联实体 ID
6. 事务提交，返回生成的用户名

---

## 5. 业务层实现

### 5.1 分页查询

**使用 PageHelper 插件**：
- 在查询前调用 `PageHelper.startPage(pageNum, pageSize)`
- PageHelper 自动拦截 SQL，添加 LIMIT 和 OFFSET 子句
- 查询结果自动封装为 PageInfo 对象
- 通过 PageResult.of() 转换为统一的分页响应格式

### 5.2 数据源切换

**在 Service 层实现**：
```java
// 设置数据源
DataSourceContextHolder.setDataSourceType("admin");

// 执行数据库操作
List<Song> list = songMapper.selectAll();

// 请求结束时自动清理（通过拦截器或过滤器）
```

### 5.3 统一响应封装

**Result<T> 类**：
- 统一的响应格式：code、message、data
- 提供静态方法：success()、error()
- 便于前端统一处理响应

**PageResult<T> 类**：
- 封装分页信息：total、list、pageNum、pageSize、pages
- 提供 of() 方法从 PageInfo 转换

---

## 6. 数据访问层

### 6.1 MyBatis 配置

**Mapper 接口**：
- 使用 @Mapper 注解标识
- 定义数据访问方法（insert、update、delete、select）
- 通过 @Param 注解指定参数名

**XML 映射文件**：
- 定义 SQL 语句和结果映射
- 使用动态 SQL（if、where、set 标签）
- 支持多条件组合查询
- 使用 useGeneratedKeys 自动获取主键

### 6.2 核心 SQL 特性

**动态 SQL**：
- 根据参数动态拼接 WHERE 条件
- 只更新非空字段（动态 UPDATE）
- 支持模糊查询（LIKE CONCAT）

**关联查询**：
- LEFT JOIN 获取关联表数据
- 在 resultMap 中映射关联字段
- 一次查询获取完整信息

---

## 7. 前端实现

### 7.1 API 请求封装

**Axios 配置**：
- baseURL: `http://localhost:8080/api`
- withCredentials: true（携带 Cookie）
- timeout: 10000ms

**请求拦截器**：
- 可添加 token 等认证信息
- 统一处理请求参数

**响应拦截器**：
- 统一处理业务错误（code != 200）
- 统一处理 HTTP 错误（401、403、500）
- 自动显示错误提示
- 401 自动跳转登录页

### 7.2 路由配置

**Vue Router 配置**：
- 根据用户角色动态加载路由
- 路由守卫验证登录状态
- 未登录自动跳转登录页

### 7.3 状态管理

**使用 Pinia**：
- 存储用户信息（userId、username、role）
- 提供登录、登出方法
- 持久化用户状态（localStorage）

---

## 8. 异常处理

### 8.1 全局异常处理器

**GlobalExceptionHandler**：
- 使用 @ControllerAdvice 注解
- 捕获所有 Controller 抛出的异常
- 统一返回 Result 格式的错误响应

**异常类型**：
- BusinessException：业务异常（自定义）
- SQLException：数据库异常
- ConstraintViolationException：约束违反异常
- 其他运行时异常

### 8.2 错误信息提取

**数据库错误处理**：
- 从 SQLException 中提取触发器错误信息
- 解析 MySQL 错误消息
- 转换为友好的中文提示

**应用层验证**：
- 参数校验（非空、格式、范围）
- 业务规则验证（日期逻辑、权限检查）
- 提供详细的错误信息（包含具体日期等）

---

## 9. 数据库触发器

### 9.1 成员人数自动维护

**触发器**：
- trg_member_insert_update_count
- trg_member_delete_update_count
- trg_member_update_band

**功能**：
- 成员插入/删除/更新时自动更新乐队的 member_count
- 只统计在队成员（leave_date IS NULL）

### 9.2 专辑排行榜自动更新

**触发器**：
- trg_review_insert_update_album
- trg_review_update_album
- trg_review_delete_update_album

**功能**：
- 乐评插入/更新/删除时自动更新专辑平均分
- 调用存储过程 sp_update_album_ranking 更新排行榜
- 只保留评分前 10 的专辑

### 9.3 完整性约束触发器

**队长约束**：
- trg_band_check_leader
- 确保队长是本乐队成员

**成员时间重叠约束**：
- trg_member_check_person_overlap_insert
- trg_member_check_person_overlap_update
- 防止同一人同时在多个乐队

**日期完整性约束**：
- trg_member_check_join_date_*
- trg_album_check_release_date_*
- trg_concert_check_event_time_*
- 确保加入日期、发行日期、演出日期不早于乐队成立日期

---

## 10. 核心特性总结

### 10.1 多数据源动态切换

**实现方式**：
- 继承 AbstractRoutingDataSource 实现动态路由
- 使用 ThreadLocal 存储数据源类型
- Service 层根据业务需求切换数据源

**优势**：
- 数据库层面的权限隔离
- 不同角色使用不同的数据库连接
- 提高系统安全性

### 10.2 自动化数据维护

**触发器机制**：
- 成员人数自动统计（只统计在队成员）
- 专辑平均分自动计算
- 排行榜自动更新（前 10 名）

**优势**：
- 减少应用层代码复杂度
- 保证数据一致性
- 提高性能（数据库层面计算）

### 10.3 完善的异常处理

**双重验证**：
- 应用层验证：提供详细的错误信息
- 数据库层验证：通过触发器确保数据完整性

**错误信息提取**：
- 从数据库异常中提取触发器错误
- 转换为友好的中文提示
- 包含具体的日期、名称等信息

### 10.4 乐队解散功能

**核心特性**：
- 支持管理员和乐队用户解散乐队
- 自动将所有未离队成员设置为已离队
- 解散日期修改时智能同步成员离队日期
- Admin Privilege Mode：管理员可为已解散乐队添加成员

**设计原则**：
- 智能日期同步（只更新与旧解散日期一致的成员）
- 应用层约束（离队日期必填且不能晚于解散日期）
- 历史数据修正（管理员特权）

### 10.5 用户注册自动化

**自动生成用户名**：
- 使用 pinyin4j 库实现中文转拼音
- 乐队：band_名称拼音
- 歌迷：fan_姓名拼音

**优势**：
- 用户无需手动输入用户名
- 用户名规范统一
- 避免用户名冲突

### 10.6 Session 认证机制

**实现方式**：
- 登录成功后创建 Session
- 用户信息存储在 Session 中
- 前端携带 Cookie（JSESSIONID）

**优势**：
- 实现简单，无需额外的 token 管理
- 服务器端控制 Session 生命周期
- 支持自动过期和清理

### 10.7 统一响应格式

**Result<T> 封装**：
- 统一的响应结构（code、message、data）
- 便于前端统一处理
- 支持泛型，灵活返回不同类型数据

**PageResult<T> 封装**：
- 统一的分页响应格式
- 包含完整的分页信息
- 与 PageHelper 无缝集成

---

## 11. 技术栈总结

### 11.1 后端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis | 2.3.1 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| Druid | 1.2.20 | 数据库连接池 |
| PageHelper | 1.4.7 | 分页插件 |
| Pinyin4j | 2.5.1 | 中文转拼音 |

### 11.2 前端技术

| 技术 | 用途 |
|------|------|
| Vue 3 | 前端框架 |
| Vue Router | 路由管理 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| Axios | HTTP 请求 |
| Vite | 构建工具 |

### 11.3 数据库特性

| 特性 | 说明 |
|------|------|
| 触发器 | 自动维护数据一致性 |
| 存储过程 | 复杂业务逻辑封装 |
| 外键约束 | 引用完整性 |
| CHECK 约束 | 数据范围验证 |
| UNIQUE 约束 | 唯一性保证 |

---

## 12. 部署说明

### 12.1 环境要求

- JDK 8 或以上
- MySQL 8.0 或以上
- Node.js 16 或以上
- Maven 3.6 或以上

### 12.2 数据库初始化

1. 创建数据库：`CREATE DATABASE band_management;`
2. 执行建表脚本：`sql/database_implementation.sql`
3. 执行完整性脚本：`sql/database_integrity.sql`
4. 执行安全脚本：`sql/database_security.sql`
5. 执行用户表脚本：`sql/user_table_creation.sql`

### 12.3 后端启动

```bash
# 编译打包
mvn clean package

# 运行
java -jar target/band-management-1.0.0.jar

# 或使用 Maven 直接运行
mvn spring-boot:run
```

### 12.4 前端启动

```bash
# 安装依赖
cd frontend
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

生产环境打包
npm run build

部署到Nginx
将 dist 目录复制到 Nginx 的 html 目录
```nginx
server {
    listen 80;
    server_name example.com;
    
    # 前端静态文件
    location / {
        root /var/www/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

***

## 13. 功能截图展示

### 管理员

1、登录



2、数据统计



3、乐队管理



> 所有类似的表
>
> * 都可以调整一页显示多少条记录；
> * 都提供查询功能，支持模糊查询，比如只输入一个字。
>
> 查询名称中带有a字母的乐队：
>
> 

（1）编辑乐队信息（乐队名称、成立时间年月日、队长从现役队员中选择、简介）



（2）删除乐队（需要保证所有成员已经删除否则无法删除）





（3）添加乐队



4、成员管理



（1）编辑成员（修改所属乐队且只能在已有乐队中选择、姓名、性别、出生日期、角色、加入日期、离队日期可选）



（2）删除成员



（3）添加成员（设定所属乐队却只能在已有乐队中选择、姓名、出生日期、角色、加入日期、离队日期可选）



5、专辑管理



（1）编辑专辑（修改所属乐队，专辑名称，发行日期，简介）



（2）删除专辑



（3）为已有乐队添加专辑



6、歌曲管理



（1）编辑歌曲（修改所属专辑、名称、作词、作曲）



（2）删除歌曲

 

（3）为已有专辑添加歌曲



7、演唱会管理



（1）编辑演唱会（修改所属乐队，演唱会名称，场地，演出时间）



（2）删除演唱会



（3）添加演唱会



8、歌迷管理



（1）编辑歌迷（修改姓名、性别、年龄、职业和学历）

 

（2）删除歌迷



（3）添加歌迷



9、乐评管理



（1）查看操作可以查看完整信息



（2）删除乐评



### 乐队用户

1、登录与注册



2、乐队管理首页



（1）编辑乐队信息



乐队名称、成立时间在注册时候设定，无法修改

成员数量、专辑数量、歌曲数量自动统计更新

3、成员管理



（1）编辑成员（修改姓名、出生日期、角色、加入日期、离队日期）



（2）删除成员



删除后主页成员数量减少



（3）添加成员

 

4、专辑管理



（1）查看专辑歌曲



（2）查看评论



（3）编辑专辑（修改名称、发行日期、简介）



（4）删除专辑



（5）添加专辑



5、歌曲管理



（1）编辑歌曲（修改歌名、所属专辑、作词）



（2）删除歌曲



（3）添加歌曲



6、管理演唱会信息



（1）编辑演唱会（修改名称、地点、时间）



（2）删除演唱会



（3）添加演唱会



7、查看歌迷信息



### 歌迷用户

1、登录与注册



2、歌迷中心首页

* 有关注收藏数据统计

* 有热门乐队推荐（关注人数前5）与专辑排行榜（评分前10），都会动态更新，可以对其中的乐队和专辑进行关注收藏



3、个人信息

* 可以修改个人信息（姓名、性别、年龄、职业、学历）

  

* 可以修改密码，删除歌迷用户账户

  

4、我的喜好

* 可以取消关注已关注乐队

  

* 可以取消收藏已收藏的专辑和歌曲

  

  

* 可以添加或删除演唱会记录

  

  

5、发现界面（发现乐队、专辑、歌曲）

可以进入发现界面查看搜索其他乐队、专辑、歌曲并对感兴趣的进行关注和收藏，操作完成后该条会从列表除去



6、我的乐评



（1）编辑乐评

可以修改评分和评论，评论字数必须大于10



（2）添加乐评

