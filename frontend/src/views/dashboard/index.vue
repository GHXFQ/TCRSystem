<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>我的课程</span>
            </div>
          </template>
          <div class="card-content">
            暂无课程数据
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>最新资料</span>
            </div>
          </template>
          <div class="card-content">
            暂无资料数据
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>最新反馈</span>
            </div>
          </template>
          <div class="card-content">
            暂无反馈数据
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Dashboard">
// 后续添加数据获取和处理逻辑
</script>

<style lang="scss" scoped>
.dashboard-container {
  .dashboard-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-content {
      min-height: 100px;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #909399;
    }
  }
}
</style> 