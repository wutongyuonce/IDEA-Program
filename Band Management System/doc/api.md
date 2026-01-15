# 乐队管理系统 API 文档

## 文档说明

**项目名称**: 乐队管理系统  
**版本**: v1.0  
**基础URL**: `http://localhost:8080`  
**更新日期**: 2024-12-24

---
## 完整的请求流程示例

### 用户登录流程

> 1. 用户在浏览器访问：
>    http://localhost:5173/login
>    ↓
> 2. 用户点击登录按钮，前端调用：
>    login({ username: 'xxx', password: 'xxx', role: 'BAND' })
>    ↓
> 3. axios 自动拼接 baseURL：
>    http://localhost:8080/api + /auth/login
>    ↓
> 4. 实际发送的HTTP请求：
>    POST http://localhost:8080/api/auth/login
>    ↓
> 5. 后端 Spring Boot 接收请求：
>    @PostMapping("/api/auth/login")  ← Controller中定义的路径
>    ↓
> 6. 返回响应给前端
>    ↓
> 7. 前端跳转到：
>    http://localhost:5173/band/home  ← 前端路由

### 为什么要这样设计？

* 统一管理 - 所有API请求都有 /api 前缀，便于识别
* 便于代理 - 可以通过 Nginx 等反向代理区分前端和后端
* 代码简洁 - 前端调用时不需要每次都写完整URL
* 易于切换 - 只需修改 baseURL 就能切换环境

### 实际部署时的配置

```
# Nginx 配置示例
server {
    listen 80;
    server_name example.com;
    
    # 前端静态文件
    location / {
        root /var/www/frontend;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API（有 /api 前缀）
    location /api/ {
        proxy_pass http://localhost:8080;
    }
}
```

### 如何查看实际的API请求？

方法1：浏览器开发者工具

1. 按 F12 打开开发者工具
2. 切换到 Network 标签
3. 刷新页面或执行操作
4. 查看 XHR 或 Fetch 类型的请求
5. 点击请求可以看到完整的URL，包括 /api 前缀

方法2：查看请求详情

在 Network 标签中点击任意请求，可以看到：

* Request URL: http://localhost:8080/api/auth/login
* Request Method: POST
* Request Headers: 包含 Cookie 等信息
* Request Payload: 请求参数
* Response: 服务器返回的数据

## 目录

1. [认证接口](#1-认证接口)
2. [管理员接口](#2-管理员接口)
3. [乐队用户接口](#3-乐队用户接口)
4. [歌迷用户接口](#4-歌迷用户接口)
5. [公共接口](#5-公共接口)
6. [数据模型](#6-数据模型)
7. [错误码说明](#7-错误码说明)

---

## 通用说明

### 请求头

```http
Content-Type: application/json
Cookie: JSESSIONID=xxx  # 登录后自动设置
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录已过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 认证接口

### 1.1 用户登录

**接口**: `POST /api/auth/login`

**描述**: 用户登录系统，支持管理员、乐队用户、歌迷用户三种角色

**请求参数**:
```json
{
  "username": "band_taopaojihua",
  "password": "band123",
  "role": "BAND"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| role | String | 是 | 角色：ADMIN/BAND/FAN |

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "band_taopaojihua",
    "role": "BAND",
    "relatedId": 1,
    "relatedName": "逃跑计划"
  }
}
```

---

### 1.2 用户登出

**接口**: `POST /api/auth/logout`

**描述**: 用户登出系统

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

### 1.3 获取当前用户信息

**接口**: `GET /api/auth/current`

**描述**: 获取当前登录用户的信息

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "userId": 1,
    "username": "band_taopaojihua",
    "role": "BAND",
    "relatedId": 1,
    "status": 1
  }
}
```

---

### 1.4 乐队用户注册

**接口**: `POST /api/auth/register/band`

**描述**: 注册新的乐队用户

**请求参数**:
```json
{
  "name": "新乐队",
  "foundedAt": "2024-01-01",
  "intro": "乐队简介",
  "password": "password123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 10,
    "username": "band_xinledui",
    "message": "注册成功！您的用户名是：band_xinledui"
  }
}
```

---

### 1.5 歌迷用户注册

**接口**: `POST /api/auth/register/fan`

**描述**: 注册新的歌迷用户

**请求参数**:
```json
{
  "name": "张三",
  "gender": "M",
  "age": 25,
  "occupation": "软件工程师",
  "education": "本科",
  "password": "password123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 20,
    "username": "fan_zhangsan",
    "message": "注册成功！您的用户名是：fan_zhangsan"
  }
}
```

---

## 2. 管理员接口

### 2.1 乐队管理

#### 2.1.1 获取所有乐队（分页）

**接口**: `GET /api/admin/bands`

**描述**: 获取所有乐队列表（支持分页和搜索）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |
| name | String | 否 | 乐队名称（模糊搜索） |

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 5,
    "list": [
      {
        "bandId": 1,
        "name": "逃跑计划",
        "foundedAt": "2007-06-15",
        "intro": "中国内地流行摇滚乐队",
        "leaderMemberId": 1,
        "memberCount": 4
      }
    ],
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  }
}
```

---

#### 2.1.2 获取所有乐队（列表）

**接口**: `GET /api/admin/bands/list`

**描述**: 获取所有乐队列表（不分页）

---

#### 2.1.3 获取乐队详情

**接口**: `GET /api/admin/bands/{id}`

**描述**: 获取指定乐队的基本信息

---

#### 2.1.4 获取乐队详细信息

**接口**: `GET /api/admin/bands/{id}/detail`

**描述**: 获取指定乐队的详细信息（包含成员列表）

---

#### 2.1.5 创建乐队

**接口**: `POST /api/admin/bands`

**请求参数**:
```json
{
  "name": "新乐队",
  "foundedAt": "2024-01-01",
  "intro": "乐队简介"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 11
}
```

---

#### 2.1.6 更新乐队信息

**接口**: `PUT /api/admin/bands/{id}`

**请求参数**:
```json
{
  "name": "逃跑计划",
  "intro": "更新后的简介"
}
```

---

#### 2.1.7 删除乐队

**接口**: `DELETE /api/admin/bands/{id}`

**描述**: 删除指定乐队（会级联删除相关数据）

---

#### 2.1.8 设置乐队队长

**接口**: `PUT /api/admin/bands/{bandId}/leader/{memberId}`

**描述**: 设置指定成员为乐队队长

---

#### 2.1.9 解散乐队

**接口**: `PUT /api/admin/bands/{bandId}/disband`

**描述**: 解散指定乐队，自动将所有未离队成员设置为已离队

**请求参数**:
- 路径参数：`bandId` - 乐队ID

**响应示例**:
```json
{
  "code": 200,
  "message": "解散乐队成功",
  "data": null
}
```

**功能说明**:
- 设置乐队状态为已解散（`isDisbanded = 'Y'`）
- 设置解散日期为当前日期
- 将所有未离队成员（`leaveDate IS NULL`）设置为已离队
- 成员离队日期设置为解散日期
- 已解散的乐队不能重复解散

**错误响应**:
```json
{
  "code": 1001,
  "message": "该乐队已经解散",
  "data": null
}
```

---

### 2.2 成员管理

#### 2.2.1 获取所有成员（分页）

**接口**: `GET /api/admin/members`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |
| bandId | Long | 否 | 按乐队筛选 |

---

#### 2.2.2 获取所有成员（列表）

**接口**: `GET /api/admin/members/list`

**描述**: 获取所有成员列表（不分页）

---

#### 2.2.3 获取成员详情

**接口**: `GET /api/admin/members/{id}`

**描述**: 获取指定成员的详细信息

---

#### 2.2.4 根据乐队ID获取成员

**接口**: `GET /api/admin/members/band/{bandId}`

**描述**: 获取指定乐队的所有成员，支持传入 "all" 查询所有成员

---

#### 2.2.5 创建成员

**接口**: `POST /api/admin/members`

**请求参数**:
```json
{
  "personId": 100,
  "bandId": 1,
  "name": "新成员",
  "gender": "M",
  "birthDate": "1990-01-01",
  "role": "吉他手",
  "joinDate": "2024-01-01",
  "leaveDate": null
}
```

---

#### 2.2.6 更新成员信息

**接口**: `PUT /api/admin/members/{id}`

---

#### 2.2.7 删除成员

**接口**: `DELETE /api/admin/members/{id}`

---

### 2.3 专辑管理

#### 2.3.1 获取所有专辑（分页）

**接口**: `GET /api/admin/albums`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |
| bandId | Long | 否 | 按乐队筛选 |
| title | String | 否 | 专辑名称搜索 |

---

#### 2.3.2 获取所有专辑（列表）

**接口**: `GET /api/admin/albums/list`

**描述**: 获取所有专辑列表（不分页）

---

#### 2.3.3 获取专辑详情

**接口**: `GET /api/admin/albums/{id}`

**描述**: 获取指定专辑的基本信息

---

#### 2.3.4 获取专辑详细信息

**接口**: `GET /api/admin/albums/{id}/detail`

**描述**: 获取指定专辑的详细信息（包含歌曲列表）

---

#### 2.3.5 根据乐队ID获取专辑

**接口**: `GET /api/admin/albums/band/{bandId}`

**描述**: 获取指定乐队的所有专辑，支持传入 "all" 查询所有专辑

---

#### 2.3.6 创建专辑

**接口**: `POST /api/admin/albums`

---

#### 2.3.7 更新专辑信息

**接口**: `PUT /api/admin/albums/{id}`

---

#### 2.3.8 删除专辑

**接口**: `DELETE /api/admin/albums/{id}`

---

### 2.4 歌曲管理

#### 2.4.1 获取所有歌曲（分页）

**接口**: `GET /api/admin/songs`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 2.4.2 获取所有歌曲（列表）

**接口**: `GET /api/admin/songs/list`

**描述**: 获取所有歌曲列表（不分页）

---

#### 2.4.3 获取歌曲详情

**接口**: `GET /api/admin/songs/{id}`

**描述**: 获取指定歌曲的详细信息

---

#### 2.4.4 根据专辑ID获取歌曲

**接口**: `GET /api/admin/songs/album/{albumId}`

**描述**: 获取指定专辑的所有歌曲，支持传入 "all" 查询所有歌曲

---

#### 2.4.5 创建歌曲

**接口**: `POST /api/admin/songs`

---

#### 2.4.6 更新歌曲信息

**接口**: `PUT /api/admin/songs/{id}`

---

#### 2.4.7 删除歌曲

**接口**: `DELETE /api/admin/songs/{id}`

---

### 2.5 演唱会管理

#### 2.5.1 获取所有演唱会（分页）

**接口**: `GET /api/admin/concerts`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 2.5.2 获取所有演唱会（列表）

**接口**: `GET /api/admin/concerts/list`

**描述**: 获取所有演唱会列表（不分页）

---

#### 2.5.3 获取演唱会详情

**接口**: `GET /api/admin/concerts/{id}`

**描述**: 获取指定演唱会的详细信息

---

#### 2.5.4 根据乐队ID获取演唱会

**接口**: `GET /api/admin/concerts/band/{bandId}`

**描述**: 获取指定乐队的所有演唱会，支持传入 "all" 查询所有演唱会

---

#### 2.5.5 创建演唱会

**接口**: `POST /api/admin/concerts`

---

#### 2.5.6 更新演唱会信息

**接口**: `PUT /api/admin/concerts/{id}`

---

#### 2.5.7 删除演唱会

**接口**: `DELETE /api/admin/concerts/{id}`

---

### 2.6 歌迷管理

#### 2.6.1 获取所有歌迷（分页）

**接口**: `GET /api/admin/fans`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 2.6.2 获取所有歌迷（列表）

**接口**: `GET /api/admin/fans/list`

**描述**: 获取所有歌迷列表（不分页）

---

#### 2.6.3 获取歌迷详情

**接口**: `GET /api/admin/fans/{id}`

**描述**: 获取指定歌迷的详细信息

---

#### 2.6.4 创建歌迷

**接口**: `POST /api/admin/fans`

---

#### 2.6.5 更新歌迷信息

**接口**: `PUT /api/admin/fans/{id}`

---

#### 2.6.6 删除歌迷

**接口**: `DELETE /api/admin/fans/{id}`

---

### 2.7 乐评管理

#### 2.7.1 获取所有乐评（分页）

**接口**: `GET /api/admin/reviews`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 2.7.2 获取所有乐评（列表）

**接口**: `GET /api/admin/reviews/list`

**描述**: 获取所有乐评列表（不分页）

---

#### 2.7.3 获取乐评详情

**接口**: `GET /api/admin/reviews/{id}`

**描述**: 获取指定乐评的详细信息

---

#### 2.7.4 根据专辑ID获取乐评

**接口**: `GET /api/admin/reviews/album/{albumId}`

**描述**: 获取指定专辑的所有乐评，支持传入 "all" 查询所有乐评

---

#### 2.7.5 根据歌迷ID获取乐评

**接口**: `GET /api/admin/reviews/fan/{fanId}`

**描述**: 获取指定歌迷的所有乐评，支持传入 "all" 查询所有乐评

---

#### 2.7.6 删除乐评

**接口**: `DELETE /api/admin/reviews/{id}`

---

### 2.8 统计数据

#### 2.8.1 获取系统总体统计数据

**接口**: `GET /api/admin/statistics/overall`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "bandCount": 5,
    "memberCount": 19,
    "albumCount": 12,
    "songCount": 28,
    "concertCount": 8,
    "fanCount": 15,
    "reviewCount": 20
  }
}
```

---

#### 2.8.2 获取专辑排行榜前10名

**接口**: `GET /api/admin/statistics/albums/top10`

**描述**: 获取评分最高的前10张专辑

---

#### 2.8.3 获取完整专辑排行榜

**接口**: `GET /api/admin/statistics/albums/rankings`

**描述**: 获取所有专辑的排行榜数据

---

#### 2.8.4 获取乐队统计数据

**接口**: `GET /api/admin/statistics/bands/{bandId}`

**描述**: 获取指定乐队的统计数据（专辑数、歌曲数、歌迷数等）

---

## 3. 乐队用户接口

### 3.1 乐队信息管理

#### 3.1.1 获取乐队信息

**接口**: `GET /api/band/info`

**描述**: 获取当前登录乐队的详细信息

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "bandId": 1,
    "name": "逃跑计划",
    "foundedAt": "2007-06-15",
    "intro": "中国内地流行摇滚乐队",
    "leaderMemberId": 1,
    "memberCount": 4
  }
}
```

---

#### 3.1.2 更新乐队信息

**接口**: `PUT /api/band/info`

**请求参数**:
```json
{
  "intro": "更新后的乐队简介"
}
```

---

#### 3.1.3 删除乐队

**接口**: `DELETE /api/band/{bandId}`

**描述**: 删除乐队账号（需要先删除所有成员）

---

#### 3.1.4 获取统计数据

**接口**: `GET /api/band/{bandId}/statistics`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "albumCount": 3,
    "songCount": 7,
    "fanCount": 8
  }
}
```

---

#### 3.1.5 修改密码

**接口**: `PUT /api/band/password`

**描述**: 修改当前登录乐队用户的密码

**请求参数**:
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 当前密码 |
| newPassword | String | 是 | 新密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

**错误响应**:
```json
{
  "code": 1001,
  "message": "原密码错误",
  "data": null
}
```

---

#### 3.1.6 解散乐队

**接口**: `PUT /api/band/info/{bandId}/disband`

**描述**: 解散当前登录的乐队，自动将所有未离队成员设置为已离队

**请求参数**:
- 路径参数：`bandId` - 乐队ID

**响应示例**:
```json
{
  "code": 200,
  "message": "解散乐队成功",
  "data": null
}
```

**功能说明**:
- 设置乐队状态为已解散（`isDisbanded = 'Y'`）
- 设置解散日期为当前日期
- 将所有未离队成员（`leaveDate IS NULL`）设置为已离队
- 成员离队日期设置为解散日期
- 已解散的乐队不能重复解散

**错误响应**:
```json
{
  "code": 1001,
  "message": "该乐队已经解散",
  "data": null
}
```

---

### 3.2 成员管理

#### 3.2.1 获取成员列表

**接口**: `GET /api/band/{bandId}/members`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "memberId": 1,
      "personId": 1,
      "bandId": 1,
      "name": "毛川",
      "gender": "M",
      "birthDate": "1982-05-12",
      "role": "主唱/吉他",
      "joinDate": "2007-06-15",
      "leaveDate": null
    }
  ]
}
```

---

#### 3.2.2 添加成员

**接口**: `POST /api/band/{bandId}/members`

**请求参数**:
```json
{
  "personId": 100,
  "name": "新成员",
  "gender": "M",
  "birthDate": "1990-01-01",
  "role": "键盘手",
  "joinDate": "2024-01-01"
}
```

---

#### 3.2.3 更新成员信息

**接口**: `PUT /api/band/{bandId}/members/{memberId}`

---

#### 3.2.4 删除成员

**接口**: `DELETE /api/band/{bandId}/members/{memberId}`

---

### 3.3 专辑管理

#### 3.3.1 获取专辑列表

**接口**: `GET /api/band/{bandId}/albums`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "albumId": 1,
      "bandId": 1,
      "title": "世界",
      "releaseDate": "2011-12-01",
      "copywriting": "首张专辑",
      "avgScore": 9.5
    }
  ]
}
```

---

#### 3.3.2 发布专辑

**接口**: `POST /api/band/{bandId}/albums`

**请求参数**:
```json
{
  "title": "新专辑",
  "releaseDate": "2024-01-01",
  "copywriting": "专辑文案"
}
```

---

#### 3.3.3 更新专辑

**接口**: `PUT /api/band/{bandId}/albums/{albumId}`

---

#### 3.3.4 删除专辑

**接口**: `DELETE /api/band/{bandId}/albums/{albumId}`

---

#### 3.3.5 获取专辑歌曲

**接口**: `GET /api/band/{bandId}/albums/{albumId}/songs`

---

#### 3.3.6 获取专辑乐评

**接口**: `GET /api/band/albums/{id}/reviews`

**描述**: 获取指定专辑的所有乐评

---

#### 3.3.7 获取最新专辑

**接口**: `GET /api/band/albums/recent`

**描述**: 获取本乐队最新发布的专辑列表

---

### 3.4 歌曲管理

#### 3.4.1 获取歌曲列表

**接口**: `GET /api/band/songs`

**描述**: 获取本乐队的所有歌曲列表

---

#### 3.4.2 添加歌曲

**接口**: `POST /api/band/songs`

**请求参数**:
```json
{
  "albumId": 1,
  "title": "新歌曲",
  "lyricist": "词作者",
  "composer": "曲作者"
}
```

---

#### 3.4.3 更新歌曲

**接口**: `PUT /api/band/songs/{id}`

---

#### 3.4.4 删除歌曲

**接口**: `DELETE /api/band/songs/{id}`

---

### 3.5 演唱会管理

#### 3.5.1 获取演唱会列表

**接口**: `GET /api/band/concerts`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "concertId": 1,
      "bandId": 1,
      "title": "逃跑计划2023巡回演唱会-北京站",
      "eventTime": "2023-05-20 19:30:00",
      "location": "北京工人体育场",
      "attendanceCount": 4
    }
  ]
}
```

---

#### 3.5.2 获取最新演唱会

**接口**: `GET /api/band/concerts/recent`

**描述**: 获取本乐队最新的演唱会列表

---

#### 3.5.3 发布演唱会

**接口**: `POST /api/band/concerts`

**请求参数**:
```json
{
  "title": "演唱会名称",
  "eventTime": "2024-06-01 19:30:00",
  "location": "演出地点"
}
```

---

#### 3.5.4 更新演唱会

**接口**: `PUT /api/band/concerts/{id}`

---

#### 3.5.5 删除演唱会

**接口**: `DELETE /api/band/concerts/{id}`

---

### 3.6 歌迷数据查看

#### 3.6.1 获取歌迷列表

**接口**: `GET /api/band/fans`

**描述**: 查看关注本乐队的歌迷列表

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "fanId": 1,
      "name": "张伟",
      "gender": "M",
      "age": 28,
      "occupation": "软件工程师",
      "education": "本科"
    }
  ]
}
```

---

#### 3.6.2 获取歌迷统计数据

**接口**: `GET /api/band/fans/statistics`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "totalFans": 8,
    "maleFans": 5,
    "femaleFans": 3,
    "avgAge": 27
  }
}
```

---

#### 3.6.3 获取歌迷年龄分布

**接口**: `GET /api/band/fans/age-distribution`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "ageRange": "18-25岁",
      "count": 3,
      "percentage": 37.5
    },
    {
      "ageRange": "26-30岁",
      "count": 4,
      "percentage": 50.0
    }
  ]
}
```

---

#### 3.6.4 获取歌迷学历分布

**接口**: `GET /api/band/fans/education-distribution`

**描述**: 获取关注本乐队歌迷的学历分布统计

---

#### 3.6.5 获取喜欢专辑的歌迷统计

**接口**: `GET /api/band/fans/albums`

**描述**: 获取喜欢本乐队专辑的歌迷统计数据

---

#### 3.6.6 获取喜欢歌曲的歌迷统计

**接口**: `GET /api/band/fans/songs`

**描述**: 获取喜欢本乐队歌曲的歌迷统计数据

---

### 3.7 乐队账号管理

#### 3.7.1 删除乐队账号

**接口**: `DELETE /api/band/delete`

**描述**: 删除当前登录的乐队账号（会级联删除相关数据，如果乐队有成员则无法删除）

---

## 4. 歌迷用户接口

### 4.1 个人信息管理

#### 4.1.1 获取个人信息

**接口**: `GET /api/fan/{fanId}/profile`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "fanId": 1,
    "name": "张伟",
    "gender": "M",
    "age": 28,
    "occupation": "软件工程师",
    "education": "本科"
  }
}
```

---

#### 4.1.2 更新个人信息

**接口**: `PUT /api/fan/{fanId}/profile`

**请求参数**:
```json
{
  "occupation": "高级软件工程师",
  "education": "硕士"
}
```

---

#### 4.1.3 删除账号

**接口**: `DELETE /api/fan/{fanId}`

**描述**: 删除歌迷账号（会级联删除相关数据）

---

#### 4.1.4 获取统计数据

**接口**: `GET /api/fan/{fanId}/statistics`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "favoriteBandCount": 3,
    "favoriteAlbumCount": 5,
    "favoriteSongCount": 8,
    "reviewCount": 2
  }
}
```

---

#### 4.1.5 修改密码

**接口**: `PUT /api/fan/password`

**描述**: 修改当前登录歌迷用户的密码

**请求参数**:
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 当前密码 |
| newPassword | String | 是 | 新密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

**错误响应**:
```json
{
  "code": 1001,
  "message": "原密码错误",
  "data": null
}
```

---

#### 4.1.6 删除歌迷账号

**接口**: `DELETE /api/fan/delete`

**描述**: 删除当前登录的歌迷账号（会级联删除相关数据）

---

### 4.2 我的喜好管理

#### 4.2.1 获取喜欢的乐队

**接口**: `GET /api/fan/favorites/bands`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 3,
    "list": [
      {
        "bandId": 1,
        "name": "逃跑计划",
        "foundedAt": "2007-06-15",
        "intro": "中国内地流行摇滚乐队",
        "memberCount": 4
      }
    ]
  }
}
```

---

#### 4.2.2 关注乐队

**接口**: `POST /api/fan/favorites/bands`

**请求参数**:
```json
{
  "bandId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "关注成功",
  "data": null
}
```

---

#### 4.2.3 取消关注乐队

**接口**: `DELETE /api/fan/favorites/bands/{bandId}`

---

#### 4.2.4 获取喜欢的专辑

**接口**: `GET /api/fan/favorites/albums`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

#### 4.2.5 收藏专辑

**接口**: `POST /api/fan/favorites/albums`

**请求参数**:
```json
{
  "albumId": 1
}
```

---

#### 4.2.6 取消收藏专辑

**接口**: `DELETE /api/fan/favorites/albums/{albumId}`

---

#### 4.2.7 获取喜欢的歌曲

**接口**: `GET /api/fan/favorites/songs`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

#### 4.2.8 收藏歌曲

**接口**: `POST /api/fan/favorites/songs`

**请求参数**:
```json
{
  "songId": 1
}
```

---

#### 4.2.9 取消收藏歌曲

**接口**: `DELETE /api/fan/favorites/songs/{songId}`

---

#### 4.2.10 获取参加的演唱会

**接口**: `GET /api/fan/favorites/concerts`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

#### 4.2.11 添加演唱会参与记录

**接口**: `POST /api/fan/favorites/concerts`

**请求参数**:
```json
{
  "concertId": 1
}
```

---

#### 4.2.12 删除演唱会参与记录

**接口**: `DELETE /api/fan/favorites/concerts/{concertId}`

---

### 4.3 发现与浏览

#### 4.3.1 发现乐队

**接口**: `GET /api/fan/discovery/bands`

**描述**: 获取推荐的乐队列表（按关注人数排序）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| name | String | 否 | 乐队名称搜索 |

---

#### 4.3.2 浏览所有专辑

**接口**: `GET /api/fan/discovery/albums`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认1000 |
| title | String | 否 | 专辑名称搜索 |

---

#### 4.3.3 浏览所有歌曲

**接口**: `GET /api/fan/discovery/songs`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| title | String | 否 | 歌曲名称搜索 |

---

#### 4.3.4 浏览所有演唱会

**接口**: `GET /api/fan/concerts/all`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认1000 |

---

#### 4.3.5 查看专辑排行榜

**接口**: `GET /api/fan/discovery/albums/ranking`

**描述**: 获取评分前10的专辑排行榜

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "list": [
      {
        "albumId": 1,
        "title": "世界",
        "bandId": 1,
        "bandName": "逃跑计划",
        "releaseDate": "2011-12-01",
        "avgScore": 9.5,
        "reviewCount": 3,
        "isFavorited": true
      }
    ]
  }
}
```

---

### 4.4 乐评功能

#### 4.4.1 获取我的乐评

**接口**: `GET /api/fan/reviews/my`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 2,
    "list": [
      {
        "reviewId": 1,
        "fanId": 1,
        "albumId": 1,
        "albumTitle": "世界",
        "bandName": "逃跑计划",
        "rating": 9.5,
        "comment": "非常棒的专辑！",
        "reviewedAt": "2023-06-01 10:30:00"
      }
    ]
  }
}
```

---

#### 4.4.2 发表乐评

**接口**: `POST /api/fan/reviews`

**请求参数**:
```json
{
  "albumId": 1,
  "rating": 9.5,
  "comment": "非常棒的专辑！"
}
```

**说明**:
- rating: 评分范围 1.0-10.0，步长 0.5
- 每个专辑只能评论一次

---

#### 4.4.3 修改乐评

**接口**: `PUT /api/fan/reviews/{id}`

**请求参数**:
```json
{
  "rating": 10.0,
  "comment": "更新后的评论"
}
```

---

#### 4.4.4 删除乐评

**接口**: `DELETE /api/fan/reviews/{id}`

---

## 5. 公共接口

### 5.1 乐队信息

#### 5.1.1 获取乐队列表（分页）

**接口**: `GET /api/bands`

**描述**: 获取所有乐队列表（公开访问，支持分页）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |
| name | String | 否 | 乐队名称（模糊搜索） |

---

#### 5.1.2 获取乐队列表（不分页）

**接口**: `GET /api/bands/list`

**描述**: 获取所有乐队列表（不分页）

---

#### 5.1.3 获取乐队详情

**接口**: `GET /api/bands/{id}`

**描述**: 获取指定乐队的基本信息

---

#### 5.1.4 获取乐队详细信息

**接口**: `GET /api/bands/{id}/detail`

**描述**: 获取指定乐队的详细信息（包含成员列表）

---

#### 5.1.5 根据名称查询乐队

**接口**: `GET /api/bands/name/{name}`

**描述**: 根据乐队名称查询乐队信息

---

### 5.2 专辑信息

#### 5.2.1 获取专辑列表（分页）

**接口**: `GET /api/albums`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |
| title | String | 否 | 专辑名称（模糊搜索） |

---

#### 5.2.2 获取专辑列表（不分页）

**接口**: `GET /api/albums/list`

**描述**: 获取所有专辑列表（不分页）

---

#### 5.2.3 获取专辑详情

**接口**: `GET /api/albums/{id}`

**描述**: 获取指定专辑的基本信息

---

#### 5.2.4 获取专辑详细信息

**接口**: `GET /api/albums/{id}/detail`

**描述**: 获取指定专辑的详细信息（包含歌曲列表）

---

#### 5.2.5 根据乐队ID获取专辑

**接口**: `GET /api/albums/band/{bandId}`

**描述**: 获取指定乐队的所有专辑

---

### 5.3 歌曲信息

#### 5.3.1 获取歌曲列表（分页）

**接口**: `GET /api/songs`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |
| title | String | 否 | 歌曲名称（模糊搜索） |

---

#### 5.3.2 获取歌曲列表（不分页）

**接口**: `GET /api/songs/list`

**描述**: 获取所有歌曲列表（不分页）

---

#### 5.3.3 获取歌曲详情

**接口**: `GET /api/songs/{id}`

**描述**: 获取指定歌曲的详细信息

---

#### 5.3.4 根据专辑ID获取歌曲

**接口**: `GET /api/songs/album/{albumId}`

**描述**: 获取指定专辑的所有歌曲

---

### 5.4 演唱会信息

#### 5.4.1 获取演唱会列表（分页）

**接口**: `GET /api/concerts`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 5.4.2 获取演唱会列表（不分页）

**接口**: `GET /api/concerts/list`

**描述**: 获取所有演唱会列表（不分页）

---

#### 5.4.3 获取演唱会详情

**接口**: `GET /api/concerts/{id}`

**描述**: 获取指定演唱会的详细信息

---

#### 5.4.4 根据乐队ID获取演唱会

**接口**: `GET /api/concerts/band/{bandId}`

**描述**: 获取指定乐队的所有演唱会

---

### 5.5 成员信息

#### 5.5.1 获取成员列表（分页）

**接口**: `GET /api/members`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 5.5.2 获取成员列表（不分页）

**接口**: `GET /api/members/list`

**描述**: 获取所有成员列表（不分页）

---

#### 5.5.3 获取成员详情

**接口**: `GET /api/members/{id}`

**描述**: 获取指定成员的详细信息

---

#### 5.5.4 根据乐队ID获取成员

**接口**: `GET /api/members/band/{bandId}`

**描述**: 获取指定乐队的所有成员

---

### 5.6 歌迷信息

#### 5.6.1 获取歌迷列表（分页）

**接口**: `GET /api/fans`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 5.6.2 获取歌迷列表（不分页）

**接口**: `GET /api/fans/list`

**描述**: 获取所有歌迷列表（不分页）

---

#### 5.6.3 获取歌迷详情

**接口**: `GET /api/fans/{id}`

**描述**: 获取指定歌迷的详细信息

---

### 5.7 乐评信息

#### 5.7.1 获取乐评列表（分页）

**接口**: `GET /api/reviews`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

#### 5.7.2 获取乐评详情

**接口**: `GET /api/reviews/{id}`

**描述**: 获取指定乐评的详细信息

---

#### 5.7.3 根据专辑ID获取乐评

**接口**: `GET /api/reviews/album/{albumId}`

**描述**: 获取指定专辑的所有乐评

---

#### 5.7.4 根据歌迷ID获取乐评

**接口**: `GET /api/reviews/fan/{fanId}`

**描述**: 获取指定歌迷的所有乐评

---

## 6. 数据模型

### 6.1 User（用户）

```json
{
  "userId": 1,
  "username": "band_taopaojihua",
  "password": "加密后的密码",
  "role": "BAND",
  "relatedId": 1,
  "status": 1
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| username | String | 用户名 |
| password | String | 密码（BCrypt加密） |
| role | String | 角色：ADMIN/BAND/FAN |
| relatedId | Long | 关联ID（乐队ID或歌迷ID） |
| status | Integer | 状态：1-正常，0-禁用 |

---

### 6.2 Band（乐队）

```json
{
  "bandId": 1,
  "name": "逃跑计划",
  "foundedAt": "2007-06-15",
  "intro": "中国内地流行摇滚乐队",
  "leaderMemberId": 1,
  "memberCount": 4,
  "isDisbanded": "N",
  "disbandedAt": null
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| bandId | Long | 乐队ID |
| name | String | 乐队名称 |
| foundedAt | Date | 成立日期 |
| intro | String | 乐队简介 |
| leaderMemberId | Long | 队长成员ID |
| memberCount | Integer | 成员人数（自动维护，只统计在队成员） |
| isDisbanded | String | 是否已解散：'Y'-已解散，'N'-未解散 |
| disbandedAt | Date | 解散日期（未解散时为NULL） |

**解散功能说明**:
- 解散乐队时，自动将所有未离队成员设置为已离队
- 成员离队日期自动设置为乐队解散日期
- 已解散的乐队不能重复解散
- 管理员可以编辑已解散乐队的解散日期，系统会同步更新相关成员的离队日期

---

### 6.3 Member（成员）

```json
{
  "memberId": 1,
  "personId": 1,
  "bandId": 1,
  "name": "毛川",
  "gender": "M",
  "birthDate": "1982-05-12",
  "role": "主唱/吉他",
  "joinDate": "2007-06-15",
  "leaveDate": null
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| memberId | Long | 成员ID |
| personId | Long | 人员ID（标识同一个人） |
| bandId | Long | 所属乐队ID |
| name | String | 姓名 |
| gender | String | 性别：M-男，F-女，O-其他 |
| birthDate | Date | 出生日期 |
| role | String | 乐队分工 |
| joinDate | Date | 加入日期 |
| leaveDate | Date | 离队日期（NULL表示在队） |

**已解散乐队成员约束**:
- 为已解散的乐队添加成员时，离队日期必填
- 离队日期不能晚于乐队解散日期
- 此约束仅在应用层实施（Admin Privilege Mode）
- 管理员可以为已解散乐队添加成员，用于历史数据修正

---

### 6.4 Album（专辑）

```json
{
  "albumId": 1,
  "bandId": 1,
  "title": "世界",
  "releaseDate": "2011-12-01",
  "copywriting": "首张专辑",
  "avgScore": 9.5
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| albumId | Long | 专辑ID |
| bandId | Long | 所属乐队ID |
| title | String | 专辑名称 |
| releaseDate | Date | 发行日期 |
| copywriting | String | 专辑文案 |
| avgScore | Decimal | 平均评分（自动计算） |

---

### 6.5 Song（歌曲）

```json
{
  "songId": 1,
  "albumId": 1,
  "title": "夜空中最亮的星",
  "lyricist": "逃跑计划",
  "composer": "逃跑计划"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| songId | Long | 歌曲ID |
| albumId | Long | 所属专辑ID |
| title | String | 歌曲名称 |
| lyricist | String | 词作者 |
| composer | String | 曲作者 |

---

### 6.6 Concert（演唱会）

```json
{
  "concertId": 1,
  "bandId": 1,
  "title": "逃跑计划2023巡回演唱会-北京站",
  "eventTime": "2023-05-20 19:30:00",
  "location": "北京工人体育场"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| concertId | Long | 演唱会ID |
| bandId | Long | 主办乐队ID |
| title | String | 演唱会名称 |
| eventTime | DateTime | 演出时间 |
| location | String | 演出地点 |

---

### 6.7 Fan（歌迷）

```json
{
  "fanId": 1,
  "name": "张伟",
  "gender": "M",
  "age": 28,
  "occupation": "软件工程师",
  "education": "本科"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| fanId | Long | 歌迷ID |
| name | String | 姓名 |
| gender | String | 性别：M-男，F-女，O-其他 |
| age | Integer | 年龄 |
| occupation | String | 职业 |
| education | String | 学历 |

---

### 6.8 AlbumReview（乐评）

```json
{
  "reviewId": 1,
  "fanId": 1,
  "albumId": 1,
  "rating": 9.5,
  "comment": "非常棒的专辑！",
  "reviewedAt": "2023-06-01 10:30:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| reviewId | Long | 乐评ID |
| fanId | Long | 歌迷ID |
| albumId | Long | 专辑ID |
| rating | Decimal | 评分（1.0-10.0，步长0.5） |
| comment | String | 评论内容 |
| reviewedAt | DateTime | 评论时间 |

---

### 6.9 AlbumRanking（专辑排行榜）

```json
{
  "rankingId": 1,
  "albumId": 1,
  "bandId": 1,
  "albumTitle": "世界",
  "bandName": "逃跑计划",
  "avgScore": 9.5,
  "reviewCount": 3,
  "releaseDate": "2011-12-01"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| rankingId | Integer | 排行ID |
| albumId | Long | 专辑ID |
| bandId | Long | 乐队ID |
| albumTitle | String | 专辑名称 |
| bandName | String | 乐队名称 |
| avgScore | Decimal | 平均评分 |
| reviewCount | Integer | 评论数量 |
| releaseDate | Date | 发行日期 |

**说明**: 此表由触发器自动维护，只保留评分前10的专辑

---

## 7. 错误码说明

### 7.1 通用错误码

| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 200 | 成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未登录或登录已过期 | 401 |
| 403 | 无权限访问 | 403 |
| 404 | 资源不存在 | 404 |
| 500 | 服务器内部错误 | 500 |

---

### 7.2 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名或密码错误 |
| 1002 | 用户已存在 |
| 1003 | 用户不存在 |
| 1004 | 用户已被禁用 |
| 2001 | 乐队不存在 |
| 2002 | 乐队名称已存在 |
| 2003 | 无权操作该乐队 |
| 2004 | 乐队有成员，无法删除 |
| 3001 | 成员不存在 |
| 3002 | 成员时间重叠 |
| 4001 | 专辑不存在 |
| 4002 | 专辑名称已存在 |
| 5001 | 歌曲不存在 |
| 6001 | 演唱会不存在 |
| 7001 | 歌迷不存在 |
| 8001 | 乐评不存在 |
| 8002 | 已评论过该专辑 |
| 8003 | 评分范围错误（1.0-10.0） |
| 9001 | 数据已存在 |
| 9002 | 数据不存在 |
| 9003 | 操作失败 |

---

### 7.3 错误响应示例

```json
{
  "code": 1001,
  "message": "用户名或密码错误",
  "data": null
}
```

```json
{
  "code": 8002,
  "message": "已评论过该专辑",
  "data": null
}
```

```json
{
  "code": 403,
  "message": "无权操作该乐队",
  "data": null
}
```

---

## 8. 接口调用示例

### 8.1 完整的用户登录流程

```javascript
// 1. 用户登录
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'band_taopaojihua',
    password: 'band123',
    role: 'BAND'
  }),
  credentials: 'include' // 重要：携带Cookie
});

const loginData = await loginResponse.json();
console.log(loginData);
// {
//   "code": 200,
//   "message": "登录成功",
//   "data": {
//     "userId": 1,
//     "username": "band_taopaojihua",
//     "role": "BAND",
//     "relatedId": 1,
//     "relatedName": "逃跑计划"
//   }
// }

// 2. 获取乐队信息（自动携带Cookie）
const bandResponse = await fetch('http://localhost:8080/api/band/info', {
  credentials: 'include'
});

const bandData = await bandResponse.json();
console.log(bandData);
```

---

### 8.2 歌迷发表乐评

```javascript
// 发表乐评
const reviewResponse = await fetch('http://localhost:8080/api/fan/1/reviews', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    albumId: 1,
    rating: 9.5,
    comment: '非常棒的专辑！'
  }),
  credentials: 'include'
});

const reviewData = await reviewResponse.json();
console.log(reviewData);
// {
//   "code": 200,
//   "message": "发表成功",
//   "data": 21
// }
```

---

### 8.3 乐队查看歌迷统计

```javascript
// 获取歌迷年龄分布
const statsResponse = await fetch(
  'http://localhost:8080/api/band/1/fans/age-distribution',
  {
    credentials: 'include'
  }
);

const statsData = await statsResponse.json();
console.log(statsData);
// {
//   "code": 200,
//   "message": "成功",
//   "data": [
//     {
//       "ageRange": "18-25岁",
//       "count": 3,
//       "percentage": 37.5
//     },
//     {
//       "ageRange": "26-30岁",
//       "count": 4,
//       "percentage": 50.0
//     }
//   ]
// }
```

---

## 9. 注意事项

### 9.1 认证机制

- 系统使用 **Session + Cookie** 进行认证
- 登录成功后，服务器会设置 `JSESSIONID` Cookie
- 后续请求需要携带此 Cookie（设置 `credentials: 'include'`）
- Session 默认超时时间为 30 分钟

---

### 9.2 权限控制

**数据库层面**:
- admin_user: 拥有所有权限
- band_user: 可以管理乐队数据，只能查看歌迷数据
- fan_user: 可以管理歌迷数据，只能查看乐队数据

**应用层面**:
- 乐队用户只能操作自己的乐队数据（通过 bandId 过滤）
- 歌迷用户只能操作自己的数据（通过 fanId 过滤）
- 乐评只能由发表者本人修改或删除

---

### 9.3 数据自动维护

以下字段由数据库触发器自动维护，**不需要手动更新**:

- `Band.memberCount` - 成员人数（只统计在队成员）
- `Album.avgScore` - 专辑平均分
- `AlbumRanking` 表 - 专辑排行榜（前10名）

---

### 9.4 数据约束

- **评分范围**: 1.0 - 10.0，步长 0.5
- **唯一约束**: 同一歌迷对同一专辑只能评论一次
- **日期逻辑**: 离队日期必须 >= 加入日期
- **成员时间重叠**: 同一人不能同时在多个乐队

---

### 9.5 分页参数

所有列表接口都支持分页，默认参数：
- `page`: 页码，默认 1
- `size`: 每页数量，默认 10

---

### 9.6 跨域配置

开发环境已配置 CORS，允许前端跨域访问：
- 允许的源: `http://localhost:5173`（Vue开发服务器）
- 允许携带凭证: `credentials: true`

---

## 10. 更新日志

### v1.0 (2024-12-24)

**新增功能**:
- ✅ 用户认证（登录、登出、注册）
- ✅ 管理员功能（完整的数据管理）
- ✅ 乐队用户功能（乐队信息、成员、专辑、歌曲、演唱会管理）
- ✅ 歌迷用户功能（个人信息、喜好、乐评管理）
- ✅ 歌迷数据统计（年龄、性别、职业、学历分布）
- ✅ 专辑排行榜（自动更新）
- ✅ 中文转拼音（自动生成用户名）
- ✅ 账号删除功能（带二次确认）

**技术特性**:
- ✅ 多数据源动态切换
- ✅ 数据库触发器自动维护
- ✅ BCrypt 密码加密
- ✅ Session 认证机制
- ✅ 统一异常处理
- ✅ PageHelper 分页

---

## 11. 联系方式

**项目地址**: [GitHub仓库地址]  
**技术支持**: [support@example.com]  
**文档维护**: Band Management Team

---

**文档版本**: v1.0  
**最后更新**: 2024-12-24  
**适用系统版本**: v1.0
