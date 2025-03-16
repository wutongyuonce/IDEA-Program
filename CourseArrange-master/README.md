# CourseArrange

GitHub地址: https://github.com/imlyk/CourseArragement


## 软件技术栈
前端技术栈：
    Vue2.x + Element UI，使用 npm 包管理工具

后端技术栈：
    JDK1.8 + SpringBoot + MySQL8.0 + Mybatis-Plus + Maven

## 安装教程

> 版本：
>
> vue：4.2.2
> npm：6.13.4
> node：12.14.0
> jdk：1.8
> MySQL：8.x
> maven:3.6.1

1、**环境配置**

- JDK 1.8

  - 验证安装：`java -version`（需显示 `1.8.x`）

- Node.js 12.14.0 + npm 6.13.4

  - 推荐使用 nvm 管理版本：

    ```bash
    nvm install 12.14.0
    nvm use 12.14.0
    ```

  - 验证安装：`node -v` 和 `npm -v`

- Maven 3.6.1

  - 配置环境变量 `MAVEN_HOME`，验证：`mvn -v`

- MySQL 8.x

  - 安装后用 navicat 启动服务，创建空数据库（名称参考项目配置）

2、**前端配置（Vue项目）**

前端项目在 UI 目录下的文件夹内，在 CourseArrange 目录下运行命令：

1. 安装依赖

   ```cmd
   cd UI/CourseArrange   # 进入前端项目目录
   npm install           # 安装依赖（网络问题可尝试删掉 node_modules 后重试）
   ```

2. 启动开发服务器

   ```cmd
   npm run dev
   ```

   - **成功标志**：控制台输出 `Local: http://localhost:8080`（具体端口以实际为准）

3、**后端配置（Spring Boot项目）**

1. **导入项目**

   - 使用 IntelliJ IDEA 打开后端项目根目录（含 `pom.xml` 的文件夹）

   - 配置 Maven 包，等待初始依赖下载完成

2. **配置数据库**

   * 创建数据库，执行项目 doc 文件夹中的 sql 文件

   * 修改配置文件，找到 src/main/resources/application.properties，修改：properties

     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/your_db_name?useSSL=false&serverTimezone=Asia/Shanghai
     spring.datasource.username=root
     spring.datasource.password=your_password
     ```

3. 如需文件上传功能，配置阿里云OSS key与密钥

   - 修改 utils/AliyunUtil.java 或对应配置文件：

     ```java
     private static String accessKeyId = "你的阿里云AccessKey";
     private static String accessKeySecret = "你的阿里云Secret";
     ```

   - 阿里云密钥获取：https://ram.console.aliyun.com/manage/ak

4、**项目启动和终止**

1. **启动项目**
   - 找到 `Application` 启动类（通常位于 `src/main/java/com/xxx`），右键 `Run`

2. **终止项目**

   关闭后端，在关闭终端前，手动终止进程以确保端口释放：

   * **方法1：直接终止进程（推荐）**
     - **在终端中按下 `Ctrl+C`**
       大多数 `npm run dev` 启动的服务会监听 `SIGINT` 信号，按下 `Ctrl+C` 可优雅退出。

   * **方法2：查找并强制终止残留进程**

     如果已关闭终端导致进程残留，按以下步骤操作：

     **Windows 系统**

     1. **查找占用端口的进程PID**：

        ```cmd
        netstat -ano | findstr :8081
        ```

        - 输出示例：

          ```
          TCP    127.0.0.1:8081    0.0.0.0:0    LISTENING    12345
          ```

        - `12345` 是进程PID。

     2. **终止进程**：

        ```cmd
        taskkill /PID 12345 /F
        ```

     **Linux/macOS 系统**

     1. **查找占用端口的进程PID**：

        ```bash
        lsof -i :8081
        ```

        - 输出示例：

          ```
          COMMAND   PID    USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
          node    12345   user   20u  IPv6 123456      0t0  TCP *:8081 (LISTEN)
          ```

        - `12345` 是进程PID。

     2. **终止进程**：

        ```cmd
        kill -9 12345
        ```

## 实现功能

1.  系统有管理员（教务处主任）、讲师、学生三种用户
2.  前端比较菜，应用启动后进入的引导页面如下，根据需求进入不同登录页面
    ![输入图片说明](https://images.gitee.com/uploads/images/2020/0708/111552_fafcb0e9_5505532.png "屏幕截图.png")
3.  这里主要放管理员的功能截图
    1）管理员登录成功后进入到系统数据页面
        ![输入图片说明](https://images.gitee.com/uploads/images/2020/0708/111732_908e9b16_5505532.png "屏幕截图.png")
    2）课程计划是某一个学期需要安排上的课程，应该一次性全部导入
        ![输入图片说明](https://images.gitee.com/uploads/images/2020/0708/111837_60f807d0_5505532.png "屏幕截图.png")
        可以手动添加课程任务（课程编号，讲师编号等信息一定要与数据库对得上）、也可以使用Excel模板填写后导入Excel文件直接将课程任务导入（点击“导入”选择好文件之后，点上传到服务器即可），没有模板可以点击下载模板下载对应的Excel模板（模板文件也根据UploadController.java中的路径存放在自己本地），随后根据要求填写模板，点击“排课”按钮开始排课，排课完成跳转到课表页面
        ![输入图片说明](https://images.gitee.com/uploads/images/2020/0708/111952_de046c5a_5505532.png "屏幕截图.png")
    3）课程表效果如下所示（选择对应的年级、对应的班级之后即可显示）
    ![输入图片说明](https://images.gitee.com/uploads/images/2020/0708/112342_b83b9f32_5505532.png "屏幕截图.png")

# 如需协助，有偿解答，代码开源了就不要白嫖人力了，Q:1576070851



