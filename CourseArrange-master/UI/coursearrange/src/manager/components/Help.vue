<template>
  <div class="help-container">
    <h2 class="help-title">系统使用说明文档</h2>
    <div class="help-content">
      <div class="help-section">
        <h3>1、系统数据</h3>
        <p>系统基础数据的管理与维护，包括学期信息、课程信息等基础数据的配置。</p>
      </div>
      
      <div class="help-section">
        <h3>2、排课管理</h3>
        <p>包含课程计划的制定与课表查看功能。可以通过Excel导入或手动添加课程任务，支持自动排课和课表查询。</p>
      </div>
      
      <div class="help-section">
        <h3>3、讲师管理</h3>
        <p>教师信息的维护与管理，包括教师基本信息的添加、修改、删除等操作。</p>
      </div>
      
      <div class="help-section">
        <h3>4、班级管理</h3>
        <p>班级信息的维护与管理，支持班级信息的增删改查等基本操作。</p>
      </div>
      
      <div class="help-section">
        <h3>5、学生管理</h3>
        <p>学生信息的维护与管理，支持学生信息的批量导入与个别添加。</p>
      </div>
      
      <div class="help-section">
        <h3>6、教学资料</h3>
        <p>教材信息的管理，包括教材的基本信息维护。</p>
      </div>
      
      <div class="help-section">
        <h3>7、教学设施</h3>
        <p>教学楼、教室等教学设施的管理，包括教学区域的规划与安排。</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Help'
}
</script>

<style lang="less" scoped>
.help-container {
  padding: 20px;
  text-align: left;
}

.help-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.help-content {
  max-width: 800px;
  margin: 0 auto;
}

.help-section {
  margin-bottom: 25px;
  
  h3 {
    color: #409EFF;
    margin-bottom: 10px;
  }
  
  p {
    color: #666;
    line-height: 1.6;
    margin-left: 20px;
  }
}
</style>