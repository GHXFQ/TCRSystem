<template>
  <div class="assignment-management-container">
    <h1>课程任务管理</h1>
    
    <!-- 统计信息卡片组 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card total-card">
            <div class="stat-title">作业任务总数</div>
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
            <div class="stat-icon"><el-icon><Document /></el-icon></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card ongoing-card">
            <div class="stat-title">进行中作业</div>
            <div class="stat-value">{{ statistics.ongoingCount || 0 }}</div>
            <div class="stat-progress">{{ statistics.ongoingRate || '0%' }}</div>
            <div class="stat-icon"><el-icon><Timer /></el-icon></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card completed-card">
            <div class="stat-title">已结束作业</div>
            <div class="stat-value">{{ statistics.completedCount || 0 }}</div>
            <div class="stat-progress">{{ statistics.completedRate || '0%' }}</div>
            <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card closed-card">
            <div class="stat-title">已关闭作业</div>
            <div class="stat-value">{{ statistics.closedCount || 0 }}</div>
            <div class="stat-progress">{{ statistics.closedRate || '0%' }}</div>
            <div class="stat-icon"><el-icon><CloseBold /></el-icon></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 课程作业统计图表 -->
    <el-card class="chart-card" v-if="courseAssignmentStats.length > 0">
      <template #header>
        <div class="card-header">
          <span>课程作业统计</span>
        </div>
      </template>
      <div class="chart-container" ref="courseChartRef"></div>
    </el-card>
    
    <!-- 作业提交情况统计图表 -->
    <el-card class="chart-card" v-if="submissionRateStats.length > 0">
      <template #header>
        <div class="card-header">
          <span>作业提交率统计</span>
        </div>
      </template>
      <div class="chart-container" ref="submissionChartRef"></div>
    </el-card>
    
    <!-- 查询过滤卡片 -->
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>作业管理</span>
          <el-button type="primary" @click="openPublishDialog">发布新作业</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="课程">
          <el-select 
            v-model="queryParams.courseId" 
            placeholder="选择课程" 
            clearable 
            filterable
            style="width: 220px;"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="选择状态" 
            clearable 
            style="width: 120px;"
          >
            <el-option label="进行中" :value="1"></el-option>
            <el-option label="已结束" :value="2"></el-option>
            <el-option label="已关闭" :value="0"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="fetchAssignments">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 作业列表 -->
    <el-card v-loading="loading" class="assignments-card">
      <div class="assignments-tip" v-if="!assignmentList.length && !loading">
        <el-empty description="暂无符合条件的作业，点击发布新作业开始"></el-empty>
      </div>
      
      <el-table 
        v-else 
        :data="assignmentList" 
        style="width: 100%"
        border
      >
        <el-table-column prop="title" label="作业标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="150" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submissionCount" label="提交数" width="100" align="center">
          <template #default="scope">
            <el-badge :value="scope.row.submittedCount || 0" :max="99" type="primary">
              <el-button size="small" text @click="viewSubmissions(scope.row)">查看</el-button>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="submissionRate" label="提交率" width="100" align="center">
          <template #default="scope">
            {{ scope.row.submissionRate || '0%' }}
          </template>
        </el-table-column>
        <el-table-column prop="avgScore" label="平均分数" width="100" align="center">
          <template #default="scope">
            {{ scope.row.avgScore || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              plain
              @click="viewDetail(scope.row)"
            >详情</el-button>
            
            <el-dropdown trigger="click" @command="(command) => handleCommand(command, scope.row)">
              <el-button size="small" type="success" plain>
                状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="scope.row.status === 1" command="setOngoing">设为进行中</el-dropdown-item>
                  <el-dropdown-item :disabled="scope.row.status === 2" command="setCompleted">设为已结束</el-dropdown-item>
                  <el-dropdown-item :disabled="scope.row.status === 0" command="setClosed">设为已关闭</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          :current-page="queryParams.current"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 发布作业对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布新作业"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="assignmentForm" :rules="rules" ref="assignmentFormRef" label-width="100px">
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="assignmentForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        
        <el-form-item label="所属课程" prop="courseId">
          <el-select 
            v-model="assignmentForm.courseId" 
            placeholder="选择课程" 
            style="width: 100%;"
            filterable
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="作业描述" prop="description">
          <el-input 
            v-model="assignmentForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入作业描述" 
          />
        </el-form-item>
        
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker v-model="assignmentForm.deadline" type="datetime" placeholder="选择日期时间" />
        </el-form-item>
        
        <el-form-item label="满分分数" prop="maxScore">
          <el-input-number v-model="assignmentForm.maxScore" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAssignment" :loading="submitting">发布</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 作业详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="作业详情"
      width="700px"
    >
      <div v-if="currentAssignment" class="assignment-detail">
        <h2>{{ currentAssignment.title }}</h2>
        
        <div class="detail-row">
          <div class="detail-label">所属课程：</div>
          <div class="detail-value">{{ currentAssignment.courseName }}</div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">截止日期：</div>
          <div class="detail-value">{{ formatDatetime(currentAssignment.deadline) }}</div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">状态：</div>
          <div class="detail-value">
            <el-tag :type="getStatusType(currentAssignment.status)">
              {{ getStatusText(currentAssignment.status) }}
            </el-tag>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">满分分数：</div>
          <div class="detail-value">{{ currentAssignment.maxScore || 100 }}</div>
        </div>
        
        <div class="detail-description">
          <div class="detail-label">作业描述：</div>
          <div class="detail-content">{{ currentAssignment.description || '无' }}</div>
        </div>
        
        <!-- 提交统计信息 -->
        <div class="submission-stats" v-if="submissionStats">
          <h3>提交统计</h3>
          
          <div class="stats-row">
            <div class="stat-box">
              <div class="stat-label">学生总数</div>
              <div class="stat-number">{{ submissionStats.totalStudents || 0 }}</div>
            </div>
            
            <div class="stat-box">
              <div class="stat-label">已提交数</div>
              <div class="stat-number">{{ submissionStats.submittedCount || 0 }}</div>
            </div>
            
            <div class="stat-box">
              <div class="stat-label">提交率</div>
              <div class="stat-number">{{ submissionStats.submissionRate || '0%' }}</div>
            </div>
            
            <div class="stat-box">
              <div class="stat-label">批改数</div>
              <div class="stat-number">{{ submissionStats.gradedCount || 0 }}</div>
            </div>
            
            <div class="stat-box">
              <div class="stat-label">批改率</div>
              <div class="stat-number">{{ submissionStats.gradingRate || '0%' }}</div>
            </div>
            
            <div class="stat-box">
              <div class="stat-label">平均分</div>
              <div class="stat-number">{{ submissionStats.avgScore || '暂无' }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="viewSubmissions(currentAssignment)">查看提交</el-button>
        </span>
      </template>
    </el-dialog>
    
  </div>
</template>

<script setup>
import { ref, reactive,  onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Timer, CircleCheck, CloseBold, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const assignmentList = ref([])
const courseList = ref([])
const total = ref(0)
const publishDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentAssignment = ref(null)
const submissionStats = ref(null)
const statistics = ref({})
const courseAssignmentStats = ref([])
const submissionRateStats = ref([])
const courseChartRef = ref(null)
const submissionChartRef = ref(null)
let courseChart = null
let submissionChart = null

// 查询参数
const queryParams = reactive({
  current: 1,
  pageSize: 10,
  courseId: null,
  status: null
})

// 作业表单
const assignmentFormRef = ref(null)
const assignmentForm = reactive({
  id: null,
  title: '',
  description: '',
  courseId: '',
  deadline: '',
  maxScore: 100,
  status: 1 // 默认为进行中
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在2到50个字符之间', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ],
  deadline: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'danger'   // 已关闭
    case 1: return 'primary'  // 进行中
    case 2: return 'success'  // 已结束
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '已关闭'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}

// 格式化日期时间
const formatDatetime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString('zh-CN', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化错误:', error)
    return dateTimeStr
  }
}

// 获取课程列表
const fetchCourseList = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const teacherId = userInfo.id
    
    if (!teacherId) {
      ElMessage.warning('无法获取用户信息，请重新登录')
      return
    }
    
    const response = await axios.get(`/api/course/teacher/${teacherId}`)
    courseList.value = response.data.data || []
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 获取作业列表
const fetchAssignments = async () => {
  loading.value = true
  try {
    const params = {
      current: queryParams.current,
      size: queryParams.pageSize,
      courseId: queryParams.courseId,
      status: queryParams.status
    }
    
    const response = await axios.get('/api/assignment/teacher/list', { params })
    
    if (response.data.code === 200) {
      assignmentList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      ElMessage.error(response.data.message || '获取作业列表失败')
    }
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await axios.get('/api/assignment/statistics')
    
    if (response.data.code === 200) {
      statistics.value = response.data.data || {}
      courseAssignmentStats.value = response.data.data.courseAssignmentStats || []
      
      // 初始化图表
      nextTick(() => {
        initCourseChart()
      })
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取提交率统计
const fetchSubmissionRateInfo = async () => {
  try {
    const response = await axios.get('/api/assignment/submission-rate')
    
    if (response.data.code === 200) {
      submissionRateStats.value = response.data.data || []
      
      // 初始化图表
      nextTick(() => {
        initSubmissionChart()
      })
    }
  } catch (error) {
    console.error('获取提交率统计失败:', error)
    ElMessage.error('获取提交率统计失败')
  }
}

// 初始化课程作业统计图表
const initCourseChart = () => {
  if (!courseChartRef.value || courseAssignmentStats.value.length === 0) return
  
  // 销毁已有的图表实例
  if (courseChart) {
    courseChart.dispose()
  }
  
  courseChart = echarts.init(courseChartRef.value)
  
  const courseNames = courseAssignmentStats.value.map(item => item.courseName)
  const assignmentCounts = courseAssignmentStats.value.map(item => item.assignmentCount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '作业数量',
      nameLocation: 'end'
    },
    yAxis: {
      type: 'category',
      data: courseNames,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    series: [
      {
        name: '作业数量',
        type: 'bar',
        data: assignmentCounts,
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  courseChart.setOption(option)
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    courseChart && courseChart.resize()
  })
}

// 初始化提交率统计图表
const initSubmissionChart = () => {
  if (!submissionChartRef.value || submissionRateStats.value.length === 0) return
  
  // 销毁已有的图表实例
  if (submissionChart) {
    submissionChart.dispose()
  }
  
  submissionChart = echarts.init(submissionChartRef.value)
  
  const titles = submissionRateStats.value.map(item => item.title)
  const submissionRates = submissionRateStats.value.map(item => parseFloat(item.submissionRate))
  const gradingRates = submissionRateStats.value.map(item => parseFloat(item.gradingRate))
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['提交率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: titles,
      axisLabel: {
        interval: 0,
        rotate: 30,
        formatter: function(value) {
          return value.length > 10 ? value.substring(0, 10) + '...' : value
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '百分比',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '提交率',
        type: 'bar',
        stack: 'total',
        itemStyle: {
          color: '#67C23A'
        },
        data: submissionRates
      },
      {
        name: '批改率',
        type: 'bar',
        stack: 'total',
        itemStyle: {
          color: '#409EFF'
        },
        data: gradingRates
      }
    ]
  }
  
  submissionChart.setOption(option)
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    submissionChart && submissionChart.resize()
  })
}

// 重置查询
const resetQuery = () => {
  queryParams.courseId = null
  queryParams.status = null
  queryParams.current = 1
  fetchAssignments()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  queryParams.current = current
  fetchAssignments()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.current = 1
  fetchAssignments()
}

// 打开发布对话框
const openPublishDialog = () => {
  // 重置表单
  assignmentForm.id = null
  assignmentForm.title = ''
  assignmentForm.description = ''
  assignmentForm.courseId = null
  assignmentForm.deadline = ''
  assignmentForm.maxScore = 100
  assignmentForm.status = 1
  
  // 打开对话框
  publishDialogVisible.value = true
  
  // 重置表单验证
  nextTick(() => {
    assignmentFormRef.value?.resetFields()
  })
}

// 提交作业表单
const submitAssignment = async () => {
  // 表单验证
  if (!assignmentFormRef.value) return
  
  await assignmentFormRef.value.validate(async (valid) => {
    if (!valid) {
      return false
    }
    
    submitting.value = true
    try {
      const response = await axios.post('/api/assignment/publish', assignmentForm)
      
      if (response.data.code === 200) {
        ElMessage.success('作业发布成功')
        publishDialogVisible.value = false
        
        // 重新加载作业列表和统计数据
        fetchAssignments()
        fetchStatistics()
        fetchSubmissionRateInfo()
      } else {
        ElMessage.error(response.data.message || '作业发布失败')
      }
    } catch (error) {
      console.error('作业发布失败:', error)
      ElMessage.error('作业发布失败：' + (error.response?.data?.message || error.message))
    } finally {
      submitting.value = false
    }
  })
}

// 查看作业详情
const viewDetail = async (assignment) => {
  currentAssignment.value = assignment
  detailDialogVisible.value = true
  
  // 获取提交统计
  try {
    const response = await axios.get(`/api/assignment/submission/statistics/${assignment.id}`)
    
    if (response.data.code === 200) {
      submissionStats.value = response.data.data || {}
    }
  } catch (error) {
    console.error('获取提交统计失败:', error)
    ElMessage.error('获取提交统计失败')
  }
}

// 查看提交记录
const viewSubmissions = (assignment) => {
  detailDialogVisible.value = false
  
  // 跳转到提交列表页面
  router.push({
    name: 'AssignmentSubmissions',
    query: {
      assignmentId: assignment.id,
      title: assignment.title
    }
  })
}

// 处理状态更改命令
const handleCommand = async (command, assignment) => {
  let status = null
  let statusText = ''
  
  switch (command) {
    case 'setOngoing':
      status = 1
      statusText = '进行中'
      break
    case 'setCompleted':
      status = 2
      statusText = '已结束'
      break
    case 'setClosed':
      status = 0
      statusText = '已关闭'
      break
    default:
      return
  }
  
  try {
    // 确认操作
    await ElMessageBox.confirm(
      `确定要将作业"${assignment.title}"设置为${statusText}吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 发送请求
    const response = await axios.put(`/api/assignment/status/${assignment.id}?status=${status}`)
    
    if (response.data.code === 200) {
      ElMessage.success(`已将作业设置为${statusText}`)
      
      // 更新本地数据
      const index = assignmentList.value.findIndex(item => item.id === assignment.id)
      if (index !== -1) {
        assignmentList.value[index].status = status
      }
      
      // 如果当前对话框打开，也更新详情中的状态
      if (currentAssignment.value && currentAssignment.value.id === assignment.id) {
        currentAssignment.value.status = status
      }
      
      // 重新获取统计数据
      fetchStatistics()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

// 页面加载时获取数据
onMounted(async () => {
  await fetchCourseList()
  await fetchAssignments()
  await fetchStatistics()
  await fetchSubmissionRateInfo()
})
</script>

<style lang="scss" scoped>
.assignment-management-container {
  padding: 20px;
  
  h1 {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
    color: #303133;
  }
  
  .statistics-cards {
    margin-bottom: 20px;
    
    .stat-card {
      height: 120px;
      position: relative;
      overflow: hidden;
      
      .stat-title {
        font-size: 16px;
        color: #606266;
        margin-bottom: 15px;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: bold;
      }
      
      .stat-progress {
        margin-top: 5px;
        font-size: 14px;
        color: #909399;
      }
      
      .stat-icon {
        position: absolute;
        right: 20px;
        bottom: 20px;
        font-size: 48px;
        opacity: 0.2;
      }
      
      &.total-card .stat-value {
        color: #409EFF;
      }
      
      &.ongoing-card .stat-value {
        color: #E6A23C;
      }
      
      &.completed-card .stat-value {
        color: #67C23A;
      }
      
      &.closed-card .stat-value {
        color: #F56C6C;
      }
    }
  }
  
  .chart-card {
    margin-bottom: 20px;
    
    .chart-container {
      height: 300px;
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .assignments-card {
    .assignments-tip {
      padding: 30px 0;
    }
    
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
  
  .assignment-detail {
    h2 {
      margin-bottom: 20px;
      font-size: 20px;
      font-weight: 500;
      color: #303133;
    }
    
    .detail-row {
      display: flex;
      margin-bottom: 15px;
      
      .detail-label {
        width: 100px;
        color: #606266;
        font-weight: 500;
      }
      
      .detail-value {
        flex: 1;
      }
    }
    
    .detail-description {
      margin-bottom: 15px;
      
      .detail-label {
        color: #606266;
        font-weight: 500;
        margin-bottom: 10px;
      }
      
      .detail-content {
        padding: 10px;
        background-color: #f9f9f9;
        border-radius: 4px;
        min-height: 80px;
        white-space: pre-line;
      }
    }
    
    .submission-stats {
      margin-top: 30px;
      border-top: 1px solid #EBEEF5;
      padding-top: 20px;
      
      h3 {
        margin-bottom: 15px;
        font-size: 16px;
        font-weight: 500;
        color: #303133;
      }
      
      .stats-row {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        
        .stat-box {
          background-color: #f9f9f9;
          border-radius: 4px;
          padding: 15px;
          width: calc(33.33% - 10px);
          text-align: center;
          
          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 10px;
          }
          
          .stat-number {
            font-size: 20px;
            font-weight: bold;
            color: #409EFF;
          }
        }
      }
    }
  }
}
</style> 