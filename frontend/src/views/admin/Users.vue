<template>
  <div class="users-container">
    <h1>用户管理</h1>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="user-card" @click="goToTeacherManagement">
          <div class="user-card-content">
            <el-icon class="user-icon"><User /></el-icon>
            <div class="user-info">
              <h2>教师管理</h2>
              <p>管理教师账号、职称、负责课程等信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="user-card" @click="goToStudentManagement">
          <div class="user-card-content">
            <el-icon class="user-icon"><User /></el-icon>
            <div class="user-info">
              <h2>学生管理</h2>
              <p>管理学生账号、班级、选课等信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户统计</span>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-icon total-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
                  <div class="stat-label">总用户数</div>
                </div>
              </div>
            </el-col>
            
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-icon teacher-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ statistics.teacherCount || 0 }}</div>
                  <div class="stat-label">教师数量</div>
                </div>
              </div>
            </el-col>
            
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-icon student-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ statistics.studentCount || 0 }}</div>
                  <div class="stat-label">学生数量</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 用户统计数据
const statistics = ref({
  totalUsers: 0,
  teacherCount: 0,
  studentCount: 0
})

// 跳转到教师管理页面
const goToTeacherManagement = () => {
  router.push('/admin/teacher-management')
}

// 跳转到学生管理页面
const goToStudentManagement = () => {
  router.push('/admin/student-management')
}

// 获取用户统计数据
const fetchUserStatistics = async () => {
  try {
    const response = await axios.get('/api/admin/user/statistics')
    if (response.data.code === 200) {
      statistics.value = response.data.data
    }
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
    ElMessage.error('获取用户统计数据失败')
  }
}

onMounted(() => {
  fetchUserStatistics()
})
</script>

<style lang="scss" scoped>
.users-container {
  h1 {
    margin-bottom: 20px;
  }
  
  .user-card {
    height: 150px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    }
    
    .user-card-content {
      height: 100%;
      display: flex;
      align-items: center;
      padding: 20px;
      
      .user-icon {
        font-size: 50px;
        color: #409EFF;
        margin-right: 20px;
      }
      
      .user-info {
        h2 {
          margin-top: 0;
          margin-bottom: 10px;
          color: #303133;
        }
        
        p {
          margin: 0;
          color: #606266;
        }
      }
    }
  }
  
  .mt-20 {
    margin-top: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .stat-item {
    display: flex;
    align-items: center;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-right: 15px;
      
      .el-icon {
        font-size: 30px;
        color: white;
      }
      
      &.total-icon {
        background-color: #409EFF;
      }
      
      &.teacher-icon {
        background-color: #67C23A;
      }
      
      &.student-icon {
        background-color: #E6A23C;
      }
    }
    
    .stat-info {
      .stat-value {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 5px;
      }
      
      .stat-label {
        color: #909399;
      }
    }
  }
}
</style> 