<template>
  <div class="admin-home">
    <div class="dashboard-header">
      <div class="welcome-section">
        <h1>欢迎回来，{{ userInfo?.realName || '管理员' }}</h1>
        <p class="subtitle">今天是 {{ currentDate }} · {{ weekday }}</p>
      </div>
      <div class="actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon> 刷新数据
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ statistics.userCount || 0 }}</div>
              <div class="stat-trend" :class="{'up': statistics.userTrend > 0}">
                <el-icon v-if="statistics.userTrend > 0"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
                <span>{{ Math.abs(statistics.userTrend || 0) }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon teacher-icon">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">教师数量</div>
              <div class="stat-value">{{ statistics.teacherCount || 0 }}</div>
              <div class="stat-trend" :class="{'up': statistics.teacherTrend > 0}">
                <el-icon v-if="statistics.teacherTrend > 0"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
                <span>{{ Math.abs(statistics.teacherTrend || 0) }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon course-icon">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">课程数量</div>
              <div class="stat-value">{{ statistics.courseCount || 0 }}</div>
              <div class="stat-trend" :class="{'up': statistics.courseTrend > 0}">
                <el-icon v-if="statistics.courseTrend > 0"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
                <span>{{ Math.abs(statistics.courseTrend || 0) }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon file-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">资料数量</div>
              <div class="stat-value">{{ statistics.materialCount || 0 }}</div>
              <div class="stat-trend" :class="{'up': statistics.materialTrend > 0}">
                <el-icon v-if="statistics.materialTrend > 0"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
                <span>{{ Math.abs(statistics.materialTrend || 0) }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表和快速访问部分 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统数据趋势</span>
              <div class="chart-controls">
                <el-radio-group v-model="chartTimeRange" size="small">
                  <el-radio-button label="week">周</el-radio-button>
                  <el-radio-button label="month">月</el-radio-button>
                  <el-radio-button label="year">年</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="chartContainer" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card class="quick-actions-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快速访问</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="navigateTo('/admin/teacher-management')">
              <el-icon><User /></el-icon> 教师管理
            </el-button>
            <el-button type="success" @click="navigateTo('/admin/student-management')">
              <el-icon><Avatar /></el-icon> 学生管理
            </el-button>
            <el-button type="warning" @click="navigateTo('/admin/courses')">
              <el-icon><Reading /></el-icon> 课程管理
            </el-button>
            <el-button type="danger" @click="navigateTo('/admin/material-review')">
              <el-icon><Document /></el-icon> 材料审核
            </el-button>
            <el-button type="info" @click="navigateTo('/admin/educational-admin')">
              <el-icon><School /></el-icon> 教务管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { 
  User, 
  Reading, 
  Document, 
  CaretTop, 
  CaretBottom, 
  Refresh, 
  Avatar,
  School
} from '@element-plus/icons-vue'
import axios from 'axios'

const store = useStore()
const router = useRouter()
const userInfo = computed(() => store.state.user.userInfo)

// 日期格式化
const currentDate = ref(new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }))
const weekday = ref(new Date().toLocaleDateString('zh-CN', { weekday: 'long' }))

// 统计数据
const statistics = reactive({
  userCount: 0,
  userTrend: 5,
  teacherCount: 0,
  teacherTrend: 3,
  courseCount: 0, 
  courseTrend: 10,
  materialCount: 0,
  materialTrend: -2
})

// 图表相关
const chartContainer = ref(null)
const chartTimeRange = ref('month')
let chart = null


// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 获取用户统计数据
    try {
      const userResponse = await axios.get('/api/admin/user/statistics')
      if (userResponse.data.code === 200) {
        statistics.userCount = userResponse.data.data.totalUsers || 0
        statistics.teacherCount = userResponse.data.data.teacherCount || 0
      }
    } catch (error) {
      console.error('获取用户统计数据失败:', error)
      // 保持现有值不变，或设置一个备用值
    }
    
    // 获取课程数量
    try {
      const courseResponse = await axios.get('/api/admin/course/count')
      if (courseResponse.data.code === 200) {
        statistics.courseCount = courseResponse.data.data || 0
      }
    } catch (error) {
      console.error('获取课程数量失败:', error)
      // 保持现有值不变
    }
    
    // 获取材料数量
    try {
      const materialResponse = await axios.get('/api/admin/material/count')
      if (materialResponse.data.code === 200) {
        statistics.materialCount = materialResponse.data.data || 0
      }
    } catch (error) {
      console.error('获取材料数量失败:', error)
      // 保持现有值不变
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化图表
const initChart = () => {
  if (chartContainer.value) {
    chart = echarts.init(chartContainer.value)
    updateChart()
  }
}

// 更新图表数据
const updateChart = () => {
  // 模拟不同时间范围的数据
  let xAxisData = []
  let userData = []
  let courseData = []
  
  if (chartTimeRange.value === 'week') {
    xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    userData = [120, 132, 101, 134, 90, 30, 20]
    courseData = [220, 182, 191, 234, 290, 330, 310]
  } else if (chartTimeRange.value === 'month') {
    xAxisData = Array.from({length: 30}, (_, i) => (i + 1) + '日')
    userData = Array.from({length: 30}, () => Math.floor(Math.random() * 200) + 100)
    courseData = Array.from({length: 30}, () => Math.floor(Math.random() * 300) + 200)
  } else {
    xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    userData = [820, 932, 901, 934, 1290, 1330, 1320, 999, 1090, 1230, 1310, 1510]
    courseData = [620, 732, 701, 734, 1090, 1130, 1120, 899, 990, 1130, 1110, 1310]
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['用户数', '课程数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: xAxisData,
        axisTick: {
          alignWithLabel: true
        }
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '用户数',
        type: 'line',
        smooth: true,
        data: userData,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(64, 158, 255, 0.7)'
              },
              {
                offset: 1,
                color: 'rgba(64, 158, 255, 0.1)'
              }
            ]
          }
        }
      },
      {
        name: '课程数',
        type: 'line',
        smooth: true,
        data: courseData,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(103, 194, 58, 0.7)'
              },
              {
                offset: 1,
                color: 'rgba(103, 194, 58, 0.1)'
              }
            ]
          }
        }
      }
    ]
  }
  
  chart.setOption(option)
}

// 刷新图表大小
const resizeChart = () => {
  if (chart) {
    chart.resize()
  }
}

// 刷新数据
const refreshData = async () => {
  await fetchStatistics()
  if (chart) {
    updateChart()
  }
}



// 页面导航
const navigateTo = (path) => {
  router.push(path)
}

// 窗口大小改变时重新计算图表大小
window.addEventListener('resize', resizeChart)

// 监听图表时间范围变化
watch(chartTimeRange, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    updateChart()
  }
})

onMounted(async () => {
  await fetchStatistics()
  nextTick(() => {
    initChart()
  })
})
</script>

<style lang="scss" scoped>
.admin-home {
  padding: 20px;
  
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .welcome-section {
      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }
      
      .subtitle {
        margin: 8px 0 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
  
  .stat-row {
    margin-bottom: 20px;
  }
  
  .stat-card {
    margin-bottom: 20px;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
    }
    
    .stat-item {
      display: flex;
      align-items: center;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-right: 15px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        
        .el-icon {
          font-size: 28px;
          color: white;
        }
        
        &.user-icon {
          background: linear-gradient(135deg, #409EFF, #53a8ff);
        }
        
        &.teacher-icon {
          background: linear-gradient(135deg, #9254de, #b37feb);
        }
        
        &.course-icon {
          background: linear-gradient(135deg, #67C23A, #85ce61);
        }
        
        &.file-icon {
          background: linear-gradient(135deg, #E6A23C, #f5cb6c);
        }
      }
      
      .stat-info {
        flex: 1;
        
        .stat-label {
          color: #909399;
          font-size: 14px;
          margin-bottom: 5px;
        }
        
        .stat-value {
          font-size: 26px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 5px;
        }
        
        .stat-trend {
          display: flex;
          align-items: center;
          color: #F56C6C;
          font-size: 12px;
          
          &.up {
            color: #67C23A;
          }
          
          .el-icon {
            margin-right: 3px;
          }
        }
      }
    }
  }
  
  .chart-row {
    margin-bottom: 20px;
  }
  
  .chart-card, .quick-actions-card, .activity-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
      
      .chart-controls {
        .el-radio-group {
          margin-left: 10px;
        }
      }
    }
  }
  
  .chart-container {
    height: 350px;
    width: 100%;
  }
  
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
    
    .el-button {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80px;
      white-space: normal;
      
      .el-icon {
        margin-right: 5px;
        font-size: 18px;
      }
    }
  }
  
  .activity-card {
    margin-bottom: 0;
    
    .el-timeline {
      padding-left: 0;
      margin-top: 20px;
    }
  }
}

// 媒体查询 - 移动端响应式调整
@media (max-width: 768px) {
  .admin-home {
    .dashboard-header {
      flex-direction: column;
      align-items: flex-start;
      
      .actions {
        margin-top: 15px;
        width: 100%;
        
        .el-button {
          width: 100%;
        }
      }
    }
    
    .quick-actions {
      grid-template-columns: 1fr;
    }
  }
}
</style> 