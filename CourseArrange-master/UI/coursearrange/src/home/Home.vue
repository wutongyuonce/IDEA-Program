<template>
  <div class="wrapper" :style="wrapper">
    <div class="content-container">
      <h1 class="main-title">欢迎来到遗传算法排课系统</h1>
      <div class="entrance-links">
        <h2><router-link to="admin/login" class="entrance-link">管理员 / 讲师入口</router-link></h2>
        <h2><router-link to="student/login" class="entrance-link">学生入口</router-link></h2>
      </div>
    </div>

    <h3 class="copyright">Copy Right：© 2025 Course Arrange System</h3>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      wrapper: {
         backgroundImage: "url(" + require("../images/image1.jpg") + ")",
            backgroundRepeat: "no-repeat",

      },
    }
  }
}
</script>

<style lang="less" scoped>
  .footer {
    // margin-bottom: 0%;
    position: relative;
    margin: auto;
  }

  .wrapper {
    background-image: "@images/psb.jpg";
    height: 100%;
    width: 100%;
    position: absolute;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .title {
    text-align: left;
    margin-left: 30px;
    margin-top: 10px;
  }

  .content-container {
    text-align: center;
    margin-top: 10vh;
  }

  .main-title {
    font-size: 2.5rem;
    margin-bottom: 3rem;
    color: #333;
  }

  .entrance-links {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .entrance-link {
    font-size: 1.8rem;
    text-decoration: none;
    color: #2c3e50;
    transition: color 0.3s;
  }

  .entrance-link:hover {
    color: #42b983;
  }

  .copyright {
    color: white;
    text-align: center;
    padding-bottom: 20px;
    margin-top: auto;
  }
</style>
