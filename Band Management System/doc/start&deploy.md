# 应用启动与部属

1、解压项目文件

2、初始化数据库

在项目文件夹下进入终端，按顺序执行以下命令：

```shell
# 进入MySQL命令行
mysql -u root -p --default-character-set=utf8mb4

# 执行以下SQL脚本（必须按顺序，否则会有问题）
source sql/database_implementation.sql;
source sql/user_table_creation.sql;
source sql/database_integrity.sql;
source sql/database_security.sql;
```

**说明**:

- `database_implementation.sql` - 创建数据库、表结构和初始数据
- `user_table_creation.sql` - 创建用户表和测试账号
- `database_integrity.sql` - 创建触发器和完整性约束
- `database_security.sql` - 创建数据库用户和权限配置

3、配置数据库连接

编辑 `src/main/resources/application.yml`，确认数据库连接信息：

```yaml
# 数据源配置
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 管理员数据源
      admin:
        url: jdbc:mysql://localhost:3306/band_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
        username: admin_user
        password: Admin@123456
        driver-class-name: com.mysql.cj.jdbc.Driver
      # 乐队用户数据源
      band:
        url: jdbc:mysql://localhost:3306/band_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
        username: band_user
        password: Band@123456
        driver-class-name: com.mysql.cj.jdbc.Driver
      # 歌迷用户数据源
      fan:
        url: jdbc:mysql://localhost:3306/band_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
        username: fan_user
        password: Fan@123456
        driver-class-name: com.mysql.cj.jdbc.Driver
```

4、启动后端服务

```shell
# 在项目根目录执行
mvn clean compile

# 启动Spring Boot应用
mvn spring-boot:run
```

<img src="img/image-20251223235552419.png" alt="image-20251223235552419" style="zoom:50%;" />

5、安装前端依赖

```shell
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install
```

6、启动前端服务

```bash
# 在frontend目录执行
npm run dev
```

<img src="img/image-20251223235754359.png" alt="image-20251223235754359" style="zoom:50%;" />

前端服务将在 **http://localhost:5173** 启动

7、打开浏览器访问 **http://localhost:5173**



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