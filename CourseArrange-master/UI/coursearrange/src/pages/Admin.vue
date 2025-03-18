<template>
  <div class="login-wrapper">
    
    <div class="login-box">
      <!-- 头像 -->
      <div class="login-avatar">
        <img src="@/assets/logo.png" alt />
      </div>

      <!-- 登录表单 -->
      <el-form class="login-form" ref="loginFormRef" :model="adminLoginForm" :rules="adminLoginFormRules">
        <h3 class="login-title">用户登录</h3>
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input v-model="adminLoginForm.username" placeholder="请输入账号" prefix-icon="iconfont iconicon"></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input v-model="adminLoginForm.password" placeholder="请输入密码" prefix-icon="iconfont iconmima" type="password" show-password></el-input>
        </el-form-item>
        <!-- 登录类型 -->
        <el-form-item class="login-type">
          <template>
            <el-radio v-model="radio" label="1" @change="getType()">管理员</el-radio>
            <el-radio v-model="radio" label="2" @change="getType()">讲师</el-radio>
          </template>
        </el-form-item>
        
        <!-- 按钮 -->
        <el-form-item class="button">
          <el-button type="primary" @click="login" class="login-btn">登录</el-button>
          <el-button type="info" @click="resetLoginForm" class="reset-btn">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Admin",
  data() {
    return {
      // 类型选择，默认选择管理员登录
      radio: '1',
      // 登录表单的对象
      adminLoginForm: {
        username: '',
        password: ''
      },
      adminLoginFormRules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 12, message: '长度在 5 到 12 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 15, message: '长度在 6 到 15 个字符', trigger: 'blur' }
        ]
      },
    }
  },
  methods: {
    // 重置方法
    resetLoginForm() {
      this.$refs.loginFormRef.resetFields();
    },
    getType() {
      // 调用这个方法直接获取到了类型
    },
    
    login() {
      this.$refs.loginFormRef.validate(valid => {
        // 表单预验证
        if (!valid) return;
        if (this.radio == 1) {
          // 管理员登录
          this.$axios.post('http://localhost:8080/admin/login', {
            username: this.adminLoginForm.username,
            password: this.adminLoginForm.password
          })
          .then(res => {
            if (res.data.code == 0) {
              let ret = res.data.data
              // 保存信息，跳转到主页
              window.localStorage.setItem('token', ret.token)
              window.localStorage.setItem('admin', JSON.stringify(ret.admin))
              this.$router.push('/systemdata')
              this.$message({message: "登录成功", type: "success"})
            } else {
              alert(res.data.message)
              this.adminLoginForm.password = ''
            }
          }).catch((error) => {
            this.$message.error("登录失败")
          });
        } else if(this.radio == 2) {
          // 讲师登录
          this.$axios.post('http://localhost:8080/teacher/login', {
            username: this.adminLoginForm.username,
            password: this.adminLoginForm.password
          })
          .then(res => {
            if (res.data.code == 0) {
              let ret = res.data.data
              window.localStorage.setItem('token', ret.token)
              window.localStorage.setItem('teacher', JSON.stringify(ret.teacher))
              // 跳转
              this.$router.push('/systemdata')
              this.$message({message: "登录成功", type: "success"})
            } else {
              alert(res.data.message)
              this.adminLoginForm.password = ''
            }
          }).catch((error) => {
            this.$message.error("登录失败")
          });
        }
      })
    }
  }
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

.login-type {
  text-align: center;
  margin: 15px 0;

  .el-radio {
    margin-right: 20px;
    color: #606266;
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

  .reset-btn {
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