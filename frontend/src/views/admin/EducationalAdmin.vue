<template>
  <div class="educational-admin">
    <h1 class="page-title">教务管理</h1>
    
    <div class="card-container">
      <!-- 教学材料任务管理卡片 -->
      <el-card class="task-card" @click="navigateToMaterialTasks">
        <div class="card-content">
          <el-icon class="card-icon"><Calendar /></el-icon>
          <div class="card-text">
            <h2>教学材料任务管理</h2>
            <p>发布教师上传课程教学大纲、授课计划等教学材料的任务</p>
          </div>
        </div>
      </el-card>
      
      <!-- 统计分析卡片 -->
      <el-card class="statistics-card" @click="navigateToStatistics">
        <div class="card-content">
          <el-icon class="card-icon"><PieChart /></el-icon>
          <div class="card-text">
            <h2>统计与分析</h2>
            <p>统计与分析未上传对应教学材料的教师情况</p>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 最近发布的任务列表 -->
    <el-card class="recent-tasks-card">
      <template #header>
        <div class="card-header">
          <span>最近发布的任务</span>
        </div>
      </template>
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="recentTasks.length === 0" class="empty-data">
        <el-empty description="暂无任务" />
      </div>
      <el-table v-else :data="recentTasks" style="width: 100%">
        <el-table-column prop="title" label="任务标题" width="250"></el-table-column>
        <el-table-column prop="materialTypeName" label="材料类型"></el-table-column>
        <el-table-column prop="courseName" label="课程"></el-table-column>
        <el-table-column prop="academicYear" label="学年"></el-table-column>
        <el-table-column prop="semester" label="学期">
          <template #default="scope">
            {{ scope.row.semester === 1 ? '第一学期' : '第二学期' }}
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : (scope.row.status === 0 ? 'warning' : 'danger')">
              {{ scope.row.status === 1 ? '进行中' : (scope.row.status === 0 ? '未开始' : '已结束') }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, PieChart } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const recentTasks = ref([]);

// 导航到材料任务页面
const navigateToMaterialTasks = () => {
  router.push('/admin/material-tasks');
};

// 导航到统计分析页面
const navigateToStatistics = () => {
  router.push('/admin/material-statistics');
};

// 获取最近的任务
const fetchRecentTasks = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/admin/material-task/recent');
    if (response.data.code === 200) {
      recentTasks.value = response.data.data || [];
    } else {
      ElMessage.error(response.data.message || '获取最近任务失败');
    }
  } catch (error) {
    console.error('获取最近任务出错:', error);
    ElMessage.error('获取最近任务失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchRecentTasks();
});
</script>

<style scoped>
.educational-admin {
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #303133;
}

.card-container {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.task-card, .statistics-card {
  flex: 1;
  cursor: pointer;
  transition: all 0.3s;
  min-height: 150px;
}

.task-card:hover, .statistics-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.card-icon {
  font-size: 48px;
  margin-right: 20px;
  color: #409EFF;
}

.card-text h2 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.card-text p {
  margin: 0;
  color: #606266;
}

.recent-tasks-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container, .empty-data {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}

.task-card {
  background-color: #ecf5ff;
  border-color: #d9ecff;
}

.statistics-card {
  background-color: #f0f9eb;
  border-color: #e1f3d8;
}
</style> 