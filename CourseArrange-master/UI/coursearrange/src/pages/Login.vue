<template>
  <div class="login-wrapper">
    <div class="login-box">
      <!-- 头像 -->
      <div class="login-avatar">
        <img src="@/assets/logo.png" alt />
      </div>

      <!-- 登录表单 -->
      <el-form
        class="login-form"
        ref="loginFormRef"
        :model="studentLoginForm"
        :rules="studentLoginFormRules"
      >
        <h3 class="login-title">学生登录</h3>
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="studentLoginForm.username"
            placeholder="请输入学号"
            prefix-icon="iconfont iconicon"
          ></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="studentLoginForm.password"
            placeholder="请输入密码"
            @keyup.enter.native="login"
            prefix-icon="iconfont iconmima"
            type="password"
          ></el-input>
        </el-form-item>
        <!-- 按钮 -->
        <el-form-item class="button">
          <el-button type="primary" @click="login" class="login-btn">登录</el-button>
          <el-button type="info" @click="registerNo" class="register-btn">注册账号</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      // 登录表单的对象
      studentLoginForm: {
        username: "",
        password: "",
      },
      studentLoginFormRules: {
        username: [
          { required: true, message: "请输入账号", trigger: "blur" },
          {
            min: 3,
            max: 12,
            message: "长度在 5 到 12 个字符",
            trigger: "blur",
          },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          {
            min: 3,
            max: 15,
            message: "长度在 6 到 15 个字符",
            trigger: "blur",
          },
        ],
      },
    };
  },
  methods: {
    registerNo() {
      // 跳转到注册页面
      window.location.href = "http://localhost:8081/#/student/register";
    },

    login() {
      // 表单预验证
      this.$refs.loginFormRef.validate((valid) => {
        if (!valid) return;
        this.$axios
          .post("http://localhost:8080/student/login", {
            username: this.studentLoginForm.username,
            password: this.studentLoginForm.password,
          })
          .then((res) => {
            if (res.data.code == 0) {
              // 成功响应,得到token
              let ret = res.data.data;
              window.localStorage.setItem("token", ret.token);
              window.localStorage.setItem(
                "student",
                JSON.stringify(ret.student)
              );
              this.$router.push("/student");
              this.$message({ message: "登录成功", type: "success" });
            } else {
              alert(res.data.message);
            }
          })
          .catch((error) => {
            // 失败
            this.$message.error("登录失败");
          });
      });
    },
  },
};
</script>

<style lang="less" scoped>
.login-wrapper {
  background: linear-gradient(to right, #2c3e50, #3498db);
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 420px;
  min-height: 440px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  position: relative;
  box-shadow: 0 15px 25px rgba(0, 0, 0, 0.15);
  padding: 20px;
  box-sizing: border-box;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 30px rgba(0, 0, 0, 0.2);
  }
}

.login-form {
  position: relative;
  width: 100%;
  padding: 0 20px;
  box-sizing: border-box;
  margin-top: 80px;
}

.login-title {
  text-align: center;
  font-size: 24px;
  color: #2c3e50;
  margin-bottom: 30px;
  font-weight: 500;
}

.login-avatar {
  height: 100px;
  width: 100px;
  border-radius: 50%;
  padding: 5px;
  background: white;
  position: absolute;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1;
  transition: all 0.3s ease;

  &:hover {
    transform: translate(-50%, -52%);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  }

  img {
    height: 100%;
    width: 100%;
    border-radius: 50%;
    object-fit: cover;
  }
}

.button {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;

  .el-button {
    width: 45%;
    border-radius: 20px;
    padding: 12px 20px;
    font-size: 14px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    }
  }

  .login-btn {
    background: linear-gradient(to right, #4facfe, #00f2fe);
    border: none;

    &:hover {
      background: linear-gradient(to right, #00f2fe, #4facfe);
    }
  }

  .register-btn {
    background: #f5f7fa;
    color: #909399;
    border: none;

    &:hover {
      background: #e4e7ed;
      color: #606266;
    }
  }
}

:deep(.el-input__inner) {
  border-radius: 20px;
  padding-left: 15px;
  height: 40px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;

  &:focus {
    border-color: #4facfe;
    box-shadow: 0 0 8px rgba(79, 172, 254, 0.2);
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
