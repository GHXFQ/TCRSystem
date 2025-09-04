<template>
  <div class="teacher-home">
    <div class="welcome-section">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="welcome-card" shadow="hover">
            <div class="welcome-content">
              <div class="welcome-text">
        <h2>欢迎回来，{{ userInfo?.realName || '老师' }}</h2>
                <p class="subtitle">今天是 {{ currentDate }}，祝您工作愉快！</p>
                <p class="description">您可以在这里上传教学材料并提交审核，查看审核进度和结果。</p>
              </div>
              <div class="welcome-image">
                <img src="https://img.icons8.com/color/240/000000/teacher.png" alt="教师图标">
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="quick-stats" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>数据概览</span>
              </div>
            </template>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-icon upload">
                  <el-icon><Upload /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ uploadCount }}</div>
                  <div class="stat-label">已上传材料</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon review">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ reviewCount }}</div>
                  <div class="stat-label">审核中材料</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="quick-actions">
      <el-row :gutter="20">
        <el-col :span="8" v-for="(action, index) in quickActions" :key="index">
          <el-card class="action-card" shadow="hover" @click="navigateTo(action.path)">
            <div class="action-content">
              <div class="action-icon" :class="action.iconClass">
                <el-icon><component :is="action.icon" /></el-icon>
              </div>
              <div class="action-text">
                <h3>{{ action.title }}</h3>
                <p>{{ action.description }}</p>
              </div>
      </div>
    </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="recent-section">
      <el-row :gutter="20">
      <el-col :span="12">
          <el-card shadow="hover">
          <template #header>
            <div class="card-header">
                <span>最近上传的材料</span>
                <el-button type="primary" text @click="navigateTo('/teacher/material-upload')">查看全部</el-button>
            </div>
          </template>
            <div v-if="recentMaterials.length > 0">
              <el-table :data="recentMaterials" style="width: 100%">
                <el-table-column prop="name" label="材料名称" width="180"></el-table-column>
                <el-table-column prop="course_name" label="所属课程"></el-table-column>
                <el-table-column prop="create_time" label="上传时间" width="180"></el-table-column>
              </el-table>
          </div>
            <el-empty v-else description="暂无上传记录"></el-empty>
        </el-card>
      </el-col>
      <el-col :span="12">
          <el-card shadow="hover">
          <template #header>
            <div class="card-header">
                <span>待审核的材料</span>
                <el-button type="primary" text @click="navigateTo('/teacher/material-review')">查看全部</el-button>
            </div>
          </template>
            <div v-if="pendingReviews.length > 0">
              <el-table :data="pendingReviews" style="width: 100%">
                <el-table-column prop="material_name" label="材料名称" width="180"></el-table-column>
                <el-table-column prop="status" label="状态">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="current_reviewer_name" label="当前审核人"></el-table-column>
              </el-table>
          </div>
            <el-empty v-else description="暂无待审核材料"></el-empty>
        </el-card>
      </el-col>
    </el-row>
    </div>
    
    <!-- 教学材料任务通知 -->
    <div class="tasks-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>教学材料任务</span>
            <el-badge :value="uncompletedTasksCount" :hidden="uncompletedTasksCount === 0" type="warning">
              <el-tag type="warning" v-if="uncompletedTasksCount > 0">有 {{ uncompletedTasksCount }} 个任务待完成</el-tag>
            </el-badge>
          </div>
        </template>
        <div v-if="materialTasks.length > 0">
          <el-table :data="materialTasks" style="width: 100%">
            <el-table-column prop="title" label="任务标题" width="250"></el-table-column>
            <el-table-column prop="materialTypeName" label="材料类型" width="120"></el-table-column>
            <el-table-column prop="courseName" label="课程" width="180"></el-table-column>
            <el-table-column prop="deadline" label="截止日期" width="180"></el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag 
                  :type="scope.row.completed ? 'success' : (isTaskExpired(scope.row.deadline) ? 'danger' : 'warning')"
                >
                  {{ scope.row.completed ? '已完成' : (isTaskExpired(scope.row.deadline) ? '已过期' : '未完成') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button 
                  v-if="!scope.row.completed" 
                  type="primary" 
                  size="small" 
                  @click="handleUploadForTask(scope.row)"
                >
                  上传材料
                </el-button>
                <el-button 
                  v-else 
                  type="success" 
                  size="small" 
                  @click="viewUploadedMaterial(scope.row.materialId)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无教学材料任务"></el-empty>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Document, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const store = useStore()
const router = useRouter()
const userInfo = computed(() => store.state.user)

// 当前日期
const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

// 统计数据
const uploadCount = ref(0)
const reviewCount = ref(0)

// 快捷操作
const quickActions = [
  {
    title: '上传材料',
    description: '上传新的教学材料',
    icon: 'Upload',
    iconClass: 'upload-icon',
    path: '/teacher/material-upload'
  },
  {
    title: '材料审核',
    description: '查看材料审核状态',
    icon: 'Document',
    iconClass: 'review-icon',
    path: '/teacher/material-review'
  },
  {
    title: '返回首页',
    description: '回到教师首页',
    icon: 'HomeFilled',
    iconClass: 'home-icon',
    path: '/teacher/home'
  }
]

// 最近上传的材料
const recentMaterials = ref([])
// 待审核的材料
const pendingReviews = ref([])

// 教学材料任务相关
const materialTasks = ref([]);
const uncompletedTasksCount = ref(0);

// 获取用户信息
const fetchUserInfo = async () => {
  console.log('开始获取用户信息');
  try {
    // 从localStorage获取用户信息
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('用户未登录，请先登录');
      ElMessage.warning('用户未登录，请先登录');
      router.push('/login');
      throw new Error('未登录');
    }

    // 如果store中已有用户信息，则不重复获取
    if (userInfo.value && userInfo.value.id) {
      console.log('从store获取到用户信息:', userInfo.value);
      return userInfo.value;
    }

    // 从localStorage尝试获取
    const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (localUserInfo && localUserInfo.id) {
      store.commit('SET_USER', localUserInfo);
      console.log('从localStorage获取并设置用户信息:', localUserInfo);
      return localUserInfo;
    }

    // 如果本地存储也没有，则从服务器获取
    console.log('从服务器获取用户信息');
    const response = await axios.get('/api/user/info');
    if (response.data.code === 200 && response.data.data) {
      store.commit('SET_USER', response.data.data);
      localStorage.setItem('userInfo', JSON.stringify(response.data.data));
      console.log('从服务器获取并设置用户信息成功:', response.data.data);
      return response.data.data;
    } else {
      console.warn('获取用户信息失败:', response.data);
      router.push('/login');
      throw new Error('获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息出错:', error);
    router.push('/login');
    throw error;
  }
};

// 获取最近上传的材料
const fetchRecentMaterials = async () => {
  try {
    // 获取用户ID，首先从Vuex尝试获取
    let teacherId = userInfo.value?.id;
    
    // 如果不存在，尝试从localStorage获取
    if (!teacherId) {
      console.warn('从Vuex未获取到教师ID，尝试从localStorage获取');
      const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      teacherId = localUserInfo.id;
    }
    
    // 如果仍然无法获取ID，则退出
    if (!teacherId) {
      console.error('无法获取教师ID，请重新登录');
      return;
    }
    
    console.log('开始获取最近上传材料，教师ID:', teacherId);
    
    // 请求API
    const response = await axios.get('/api/material/teacher', {
      params: {
        current: 1,
        size: 5,
        teacherId: teacherId,
        type: 0 // 0表示上传的材料
      }
    });
    
    console.log('最近上传材料响应:', response.data);
    
    // 处理响应数据
    if (response.data && response.data.code === 200 && response.data.data) {
      recentMaterials.value = response.data.data.records || [];
      uploadCount.value = response.data.data.total || 0;
      console.log('已设置最近上传材料数据:', recentMaterials.value.length);
      console.log('已设置上传总数:', uploadCount.value);
    } else {
      console.warn('获取最近上传材料返回异常:', response.data);
      recentMaterials.value = [];
      uploadCount.value = 0;
    }
  } catch (error) {
    console.error('获取最近上传材料失败:', error);
    recentMaterials.value = [];
    uploadCount.value = 0;
  }
}

// 获取待审核的材料
const fetchPendingReviews = async () => {
  try {
    // 获取用户ID，首先从Vuex尝试获取
    let teacherId = userInfo.value?.id;
    
    // 如果不存在，尝试从localStorage获取
    if (!teacherId) {
      console.warn('从Vuex未获取到教师ID，尝试从localStorage获取');
      const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      teacherId = localUserInfo.id;
    }
    
    // 如果仍然无法获取ID，则退出
    if (!teacherId) {
      console.error('无法获取教师ID，请重新登录');
      return;
    }
    
    console.log('开始获取待审核材料，教师ID:', teacherId);
    
    // 请求API
    const response = await axios.get('/api/material-review/teacher-submit', {
      params: {
        current: 1,
        size: 5,
        teacherId: teacherId
        // 不指定status，获取所有状态的审核
      }
    });
    
    console.log('待审核材料响应:', response.data);
    
    // 处理响应数据
    if (response.data && response.data.code === 200 && response.data.data) {
      pendingReviews.value = response.data.data.records || [];
      console.log('已设置待审核材料数据:', pendingReviews.value.length);
      
      // 计算审核中的材料数量 - 状态为1,2,3,4的表示审核中
      const inReviewCount = pendingReviews.value.filter(item => 
        item.status === 1 || item.status === 2 || 
        item.status === 3 || item.status === 4
      ).length;
      
      reviewCount.value = inReviewCount;
      console.log('已设置审核中数量:', reviewCount.value);
    } else {
      console.warn('获取待审核材料返回异常:', response.data);
      pendingReviews.value = [];
      reviewCount.value = 0;
    }
  } catch (error) {
    console.error('获取待审核材料失败:', error);
    pendingReviews.value = [];
    reviewCount.value = 0;
  }
}

// 状态相关工具函数
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '课程负责人审核中'
    case 2: return '专业负责人审核中'
    case 3: return '副院长审核中'
    case 4: return '教务人员审核中'
    case 5: return '审核通过'
    case 6: return '审核不通过'
    default: return '未知状态'
  }
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: case 2: case 3: case 4: return 'warning'
    case 5: return 'success'
    case 6: return 'danger'
    default: return 'info'
  }
}

// 页面跳转
const navigateTo = (path) => {
  router.push(path)
}

// 获取教学材料任务
const fetchMaterialTasks = async () => {
  console.log('======= 开始执行fetchMaterialTasks函数 =======', new Date().toISOString());
  try {
    // 获取用户ID
    let teacherId = userInfo.value?.id;
    
    // 如果Vuex中没有，尝试从localStorage获取
    if (!teacherId) {
      console.warn('从Vuex未获取到教师ID，尝试从localStorage获取');
      const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      teacherId = localUserInfo.id;
    }
    
    if (!teacherId) {
      console.error('无法获取教师ID，未加载用户信息');
      throw new Error('无法获取教师ID');
    }

    console.log('开始获取教师任务列表，教师ID:', teacherId);
    console.log('请求URL:', '/api/material-task/teacher/tasks');
    
    // 构造请求配置，确保请求头和参数正确
    const config = {
      url: '/api/material-task/teacher/tasks',
      method: 'get',
      params: { teacherId },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    };
    
    console.log('请求配置:', JSON.stringify(config));
    
    // 发起请求
    console.log('正在发送请求...');
    const response = await axios(config);
    
    console.log('接收到教师任务列表响应:', response.status);
    console.log('响应数据:', JSON.stringify(response.data));

    if (response.data.code === 200) {
      materialTasks.value = response.data.data || [];
      console.log('成功获取任务列表，数量:', materialTasks.value.length);
      
      if (materialTasks.value.length > 0) {
        console.log('任务列表第一项:', JSON.stringify(materialTasks.value[0]));
      } else {
        console.warn('任务列表为空');
      }
      
      // 计算未完成任务数量
      uncompletedTasksCount.value = materialTasks.value.filter(task => !task.completed).length;
      console.log('未完成任务数量:', uncompletedTasksCount.value);
    } else {
      console.error('获取教师任务列表失败:', response.data.message);
      materialTasks.value = [];
      uncompletedTasksCount.value = 0;
    }
  } catch (error) {
    console.error('获取教学材料任务失败:', error);
    console.error('错误详情:', error.response || error.message || error);
    materialTasks.value = [];
    uncompletedTasksCount.value = 0;
    ElMessage.error('获取任务列表失败: ' + (error.message || '未知错误'));
  } finally {
    console.log('======= fetchMaterialTasks函数执行结束 =======');
  }
};

// 判断任务是否过期
const isTaskExpired = (deadline) => {
  if (!deadline) return false;
  const deadlineDate = new Date(deadline);
  const now = new Date();
  return deadlineDate < now;
};

// 处理为任务上传材料
const handleUploadForTask = (task) => {
  // 跳转到材料上传页面并传递任务参数
  router.push({
    path: '/teacher/material-upload',
    query: {
      taskId: task.id,
      materialTypeId: task.materialTypeId,
      courseId: task.courseId,
      academicYear: task.academicYear,
      semester: task.semester
    }
  });
};

// 查看已上传的材料
const viewUploadedMaterial = (materialId) => {
  if (!materialId) return;
  window.open(`/api/material/download/${materialId}`, '_blank');
};

// 在组件挂载时获取数据
onMounted(() => {
  console.log('====== 教师首页组件挂载 ======');
  
  // 先获取用户信息
  fetchUserInfo().then(() => {
    console.log('用户信息获取完成，开始获取任务数据');
    // 依次调用其他数据获取函数
    fetchRecentMaterials();
    fetchPendingReviews();
    
    // 确保在获取用户信息后调用任务获取
    console.log('准备调用fetchMaterialTasks函数');
    fetchMaterialTasks();
  }).catch(err => {
    console.error('获取用户信息失败，无法继续获取任务:', err);
  });
  
  // 取消自动更新，避免干扰调试
  // setInterval(() => {
  //   fetchMaterialTasks();
  // }, 300000); // 5分钟更新一次任务数据
});
</script>

<style lang="scss" scoped>
.teacher-home {
  .welcome-section {
    margin-bottom: 20px;
    
    .welcome-card {
      height: 100%;
      
      .welcome-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .welcome-text {
          flex: 1;
          
          h2 {
            margin-top: 0;
            margin-bottom: 10px;
            color: #303133;
            font-size: 24px;
          }
          
          .subtitle {
            color: #606266;
            font-size: 16px;
            margin-bottom: 15px;
          }
          
          .description {
            color: #909399;
            font-size: 14px;
            line-height: 1.6;
          }
        }
        
        .welcome-image {
          margin-left: 20px;
          
          img {
            width: 120px;
            height: 120px;
          }
        }
      }
    }
    
    .quick-stats {
      height: 100%;
      
      .stats-content {
        display: flex;
        flex-direction: column;
        gap: 20px;
        
        .stat-item {
          display: flex;
          align-items: center;
          
          .stat-icon {
            width: 50px;
            height: 50px;
            border-radius: 10px;
            display: flex;
            justify-content: center;
            align-items: center;
            margin-right: 15px;
            
            .el-icon {
              font-size: 24px;
              color: white;
            }
            
            &.upload {
              background-color: #67c23a;
            }
            
            &.review {
              background-color: #e6a23c;
            }
          }
          
          .stat-info {
            .stat-value {
              font-size: 24px;
              font-weight: bold;
              color: #303133;
              line-height: 1;
              margin-bottom: 5px;
            }
            
            .stat-label {
              font-size: 14px;
              color: #909399;
            }
          }
        }
      }
    }
  }
  
  .quick-actions {
    margin-bottom: 20px;
    
    .action-card {
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
      }
      
      .action-content {
        display: flex;
        align-items: center;
        padding: 10px 0;
        
        .action-icon {
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
          
          &.upload-icon {
            background-color: #409eff;
          }
          
          &.review-icon {
            background-color: #e6a23c;
          }
          
          &.home-icon {
            background-color: #67c23a;
          }
        }
        
        .action-text {
          h3 {
            margin: 0 0 5px 0;
            font-size: 18px;
            color: #303133;
      }
      
      p {
        margin: 0;
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }
  
  .recent-section {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  }
}
</style> 