<template>
  <div class="page-container">
    <div class="dashboard-header">
      <h2 class="page-title">系统概览</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="8" v-for="(item, index) in statsCards" :key="index">
        <div class="stats-card" :class="item.class">
          <div class="icon-wrapper">
            <i :class="item.icon"></i>
          </div>
          <div class="content">
            <div class="title">{{item.title}}</div>
            <div class="number">{{item.value}}</div>
            <div class="desc">{{item.description}}</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
@import '../../assets/styles/common.less';

.dashboard-header {
  margin-bottom: 30px;
}

.stats-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  transition: all 0.3s;
  margin-bottom: 20px;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  }
  
  .icon-wrapper {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;
    
    i {
      font-size: 40px;
      color: #fff;
    }
  }
  
  .content {
    flex: 1;
    
    .title {
      font-size: 16px;
      color: #606266;
      margin-bottom: 10px;
    }
    
    .number {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 5px;
    }
    
    .desc {
      font-size: 14px;
      color: #909399;
    }
  }
}
</style> 