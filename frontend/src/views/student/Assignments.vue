<template>
  <div class="assignments-container">
    <h1>课程作业</h1>
    
    <!-- 作业统计卡片 -->
    <el-card class="statistics-card" v-if="courseId">
      <template #header>
        <div class="card-header">
          <span>课程完成情况</span>
          <el-tag type="success" v-if="statistics.completionRate">完成率: {{ statistics.completionRate }}</el-tag>
        </div>
      </template>
      
      <div class="statistics-row">
        <div class="stat-box">
          <div class="stat-value">{{ statistics.totalAssignments || 0 }}</div>
          <div class="stat-label">总作业数</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.submittedCount || 0 }}</div>
          <div class="stat-label">已提交数</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.gradedCount || 0 }}</div>
          <div class="stat-label">已批改数</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.averageScore || '暂无' }}</div>
          <div class="stat-label">平均分</div>
        </div>
      </div>
    </el-card>
    
    <!-- 查询过滤卡片 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="课程">
          <el-select 
            v-model="queryParams.courseId" 
            placeholder="选择课程" 
            clearable 
            filterable
            style="width: 220px;"
            @change="handleCourseChange"
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
        <el-empty description="暂无符合条件的作业"></el-empty>
      </div>
      
      <el-table 
        v-else 
        :data="assignmentList" 
        style="width: 100%"
        border
        @row-click="handleRowClick"
      >
        <el-table-column type="expand">
          <template #default="props">
            <div class="assignment-detail">
              <div class="detail-row">
                <div class="detail-label">作业描述：</div>
                <div class="detail-value description">{{ props.row.description || '无' }}</div>
              </div>
              
              <div class="detail-row" v-if="props.row.submissionInfo">
                <div class="detail-label">提交状态：</div>
                <div class="detail-value">
                  <el-tag :type="getSubmissionStatusType(props.row.submissionInfo.status)">
                    {{ getSubmissionStatusText(props.row.submissionInfo.status) }}
                  </el-tag>
                  
                  <span v-if="props.row.submissionInfo.status > 0" class="submit-time">
                    提交时间: {{ props.row.submissionInfo.submitTime }}
                  </span>
                  
                  <el-tag 
                    v-if="props.row.submissionInfo.status > 0" 
                    :type="props.row.submissionInfo.isOverdue ? 'danger' : 'success'" 
                    size="small"
                    style="margin-left: 10px;"
                  >
                    {{ props.row.submissionInfo.isOverdue ? '已逾期' : '按时提交' }}
                  </el-tag>
                </div>
              </div>
              
              <div class="detail-row" v-if="props.row.submissionInfo && props.row.submissionInfo.status === 2">
                <div class="detail-label">得分：</div>
                <div class="detail-value score">
                  {{ props.row.submissionInfo.score }} / {{ props.row.maxScore || 100 }}
                </div>
              </div>
              
              <div class="detail-row" v-if="props.row.submissionInfo && props.row.submissionInfo.comment">
                <div class="detail-label">教师评语：</div>
                <div class="detail-value comment">{{ props.row.submissionInfo.comment }}</div>
              </div>
              
              <div class="detail-row" v-if="props.row.submissionInfo && props.row.submissionInfo.content">
                <div class="detail-label">提交内容：</div>
                <div class="detail-value content">{{ props.row.submissionInfo.content }}</div>
              </div>
              
              <div class="detail-row" v-if="props.row.submissionInfo && props.row.submissionInfo.filePath">
                <div class="detail-label">提交文件：</div>
                <div class="detail-value">
                  <el-link 
                    :href="`/api/assignment/submission/download/${props.row.submissionInfo.id}`" 
                    target="_blank"
                    type="primary"
                  >
                    {{ props.row.submissionInfo.fileName || '下载文件' }}
                  </el-link>
                </div>
              </div>
              
              <div class="detail-actions">
                <el-button 
                  v-if="canSubmit(props.row)" 
                  type="primary" 
                  @click.stop="openSubmitDialog(props.row)"
                >
                  {{ props.row.submissionInfo && props.row.submissionInfo.status > 0 ? '重新提交' : '提交作业' }}
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="title" label="作业标题" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="assignment-title">
              {{ scope.row.title }}
              <el-tag 
                v-if="isUrgent(scope.row)" 
                type="danger" 
                size="small" 
                effect="dark"
                style="margin-left: 8px;"
              >紧急</el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="courseName" label="所属课程" width="180" show-overflow-tooltip />
        
        <el-table-column prop="deadline" label="截止时间" width="180" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="提交状态" width="120">
          <template #default="scope">
            <el-tag 
              v-if="scope.row.submissionInfo" 
              :type="getSubmissionStatusType(scope.row.submissionInfo.status)"
            >
              {{ getSubmissionStatusText(scope.row.submissionInfo.status) }}
            </el-tag>
            <el-tag v-else type="info">未提交</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="得分" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.submissionInfo && scope.row.submissionInfo.status === 2">
              {{ scope.row.submissionInfo.score }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="发布时间" width="180" />
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="canSubmit(scope.row)" 
              type="primary" 
              size="small" 
              plain
              @click.stop="openSubmitDialog(scope.row)"
            >
              {{ scope.row.submissionInfo && scope.row.submissionInfo.status > 0 ? '重新提交' : '提交' }}
            </el-button>
            <el-button 
              v-else 
              type="info" 
              size="small" 
              plain
              disabled
            >{{ getActionText(scope.row) }}</el-button>
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
    
    <!-- 提交作业对话框 -->
    <el-dialog
      v-model="submitDialogVisible"
      title="提交作业"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentAssignment" class="submission-form">
        <div class="assignment-info">
          <h3>作业信息</h3>
          
          <div class="info-row">
            <div class="info-label">标题</div>
            <div class="info-value">{{ currentAssignment.title }}</div>
          </div>
          
          <div class="info-row">
            <div class="info-label">截止时间</div>
            <div class="info-value">
              {{ currentAssignment.deadline }}
              <el-tag 
                v-if="isUrgent(currentAssignment)" 
                type="danger" 
                size="small" 
                effect="dark"
                style="margin-left: 8px;"
              >即将截止</el-tag>
            </div>
          </div>
          
          <div class="info-row" v-if="currentAssignment.description">
            <div class="info-label">描述</div>
            <div class="info-value description">{{ currentAssignment.description }}</div>
          </div>
        </div>
        
        <div class="submission-section">
          <h3>提交内容</h3>
          
          <el-form :model="submissionForm" ref="submissionFormRef" label-width="80px">
            <el-form-item label="文本内容">
              <el-input 
                v-model="submissionForm.content" 
                type="textarea" 
                :rows="6" 
                placeholder="请输入作业内容" 
              />
            </el-form-item>
            
            <el-form-item label="上传文件">
              <el-upload
                class="upload-area"
                :action="`/api/assignment/submission/upload`"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                :limit="1"
                :file-list="fileList"
                :on-remove="handleFileRemove"
              >
                <el-button type="primary">选择文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持任意格式文件，单个文件不超过10MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>
          </el-form>
          
          <div class="submission-notice">
            <el-alert
              v-if="isDeadlineClose(currentAssignment)"
              title="警告：此作业即将截止，请尽快提交"
              type="warning"
              :closable="false"
              show-icon
            />
            <el-alert
              v-if="isDeadlinePassed(currentAssignment)"
              title="注意：此作业已过截止时间，将被标记为逾期提交"
              type="error"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAssignment" :loading="submitting">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const assignmentList = ref([])
const courseList = ref([])
const total = ref(0)
const submitDialogVisible = ref(false)
const currentAssignment = ref(null)
const fileList = ref([])
const uploadHeaders = ref({})
const statistics = ref({})

// 查询参数
const queryParams = reactive({
  current: 1,
  pageSize: 10,
  courseId: null,
  status: null
})

// 提交表单
const submissionFormRef = ref(null)
const submissionForm = reactive({
  assignmentId: null,
  content: '',
  filePath: '',
  fileName: ''
})

// 计算属性
const courseId = computed(() => queryParams.courseId)

// 获取作业状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'danger'   // 已关闭
    case 1: return 'primary'  // 进行中
    case 2: return 'success'  // 已结束
    default: return 'info'
  }
}

// 获取作业状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '已关闭'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}

// 获取提交状态类型
const getSubmissionStatusType = (status) => {
  switch (status) {
    case 0: return 'info'    // 未提交
    case 1: return 'warning' // 已提交
    case 2: return 'success' // 已批改
    default: return 'info'
  }
}

// 获取提交状态文本
const getSubmissionStatusText = (status) => {
  switch (status) {
    case 0: return '未提交'
    case 1: return '已提交'
    case 2: return '已批改'
    default: return '未知'
  }
}

// 获取操作按钮文本
const getActionText = (assignment) => {
  if (assignment.status === 0) {
    return '已关闭'
  } else if (assignment.status === 2) {
    return '已结束'
  } else if (assignment.submissionInfo && assignment.submissionInfo.status === 2) {
    return '已批改'
  } else {
    return '提交'
  }
}

// 判断是否紧急（距离截止时间不足24小时）
const isUrgent = (assignment) => {
  if (!assignment || !assignment.deadline) return false
  
  const now = new Date()
  const deadline = new Date(assignment.deadline)
  const diffHours = (deadline - now) / (1000 * 60 * 60)
  
  return assignment.status === 1 && diffHours > 0 && diffHours < 24
}

// 判断是否接近截止（距离截止时间不足24小时）
const isDeadlineClose = (assignment) => {
  return isUrgent(assignment)
}

// 判断是否已过截止时间
const isDeadlinePassed = (assignment) => {
  if (!assignment || !assignment.deadline) return false
  
  const now = new Date()
  const deadline = new Date(assignment.deadline)
  
  return now > deadline
}

// 判断是否可以提交作业
const canSubmit = (assignment) => {
  // 只有进行中的作业可以提交
  return assignment.status === 1
}

// 获取课程列表
const fetchCourseList = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const studentId = userInfo.id
    
    if (!studentId) {
      ElMessage.warning('无法获取用户信息，请重新登录')
      return
    }
    
    const response = await axios.get(`/api/course/student/${studentId}`)
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
    
    const response = await axios.get('/api/assignment/student/list', { params })
    
    if (response.data.code === 200) {
      assignmentList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
      
      // 检查截止日期并标记紧急作业
      assignmentList.value.forEach(assignment => {
        assignment.isUrgent = isUrgent(assignment)
      })
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
  if (!queryParams.courseId) {
    statistics.value = {}
    return
  }
  
  try {
    const response = await axios.get(`/api/assignment/student/completion-rate?courseId=${queryParams.courseId}`)
    
    if (response.data.code === 200) {
      statistics.value = response.data.data || {}
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.courseId = null
  queryParams.status = null
  queryParams.current = 1
  fetchAssignments()
  statistics.value = {}
}

// 处理课程变化
const handleCourseChange = () => {
  queryParams.current = 1
  fetchAssignments()
  fetchStatistics()
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

// 处理点击行
const handleRowClick = (row) => {
  console.log('点击作业:', row)
}

// 打开提交对话框
const openSubmitDialog = (assignment) => {
  currentAssignment.value = assignment
  
  // 初始化表单
  submissionForm.assignmentId = assignment.id
  submissionForm.content = assignment.submissionInfo?.content || ''
  submissionForm.filePath = ''
  submissionForm.fileName = ''
  
  // 初始化文件列表
  fileList.value = []
  if (assignment.submissionInfo?.filePath && assignment.submissionInfo?.fileName) {
    fileList.value = [{
      name: assignment.submissionInfo.fileName,
      url: `/api/assignment/submission/download/${assignment.submissionInfo.id}`
    }]
  }
  
  // 设置上传请求头
  const token = localStorage.getItem('token')
  if (token) {
    uploadHeaders.value = {
      'Authorization': `Bearer ${token}`
    }
  }
  
  // 打开对话框
  submitDialogVisible.value = true
}

// 文件上传前检查
const beforeUpload = (file) => {
  // 文件大小限制（10MB）
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}

// 处理上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('文件上传成功')
    submissionForm.filePath = response.data.filePath
    submissionForm.fileName = response.data.fileName
  } else {
    ElMessage.error(response.message || '文件上传失败')
  }
}

// 处理上传失败
const handleUploadError = (error) => {
  console.error('文件上传失败:', error)
  ElMessage.error('文件上传失败')
}

// 处理文件移除
const handleFileRemove = () => {
  submissionForm.filePath = ''
  submissionForm.fileName = ''
}

// 提交作业
const submitAssignment = async () => {
  // 验证文本内容或文件至少提交一项
  if (!submissionForm.content && !submissionForm.filePath) {
    ElMessage.warning('请至少提供文本内容或上传文件')
    return
  }
  
  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('assignmentId', submissionForm.assignmentId)
    formData.append('content', submissionForm.content || '')
    
    if (submissionForm.filePath) {
      // 如果有文件，使用文件上传接口上传的filePath
      formData.append('filePath', submissionForm.filePath)
      formData.append('fileName', submissionForm.fileName)
    }
    
    // 使用AssignmentController已有的API路径
    const response = await axios.post('/api/assignment/submit', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('作业提交成功')
      submitDialogVisible.value = false
      
      // 重新获取作业列表
      fetchAssignments()
      fetchStatistics()
    } else {
      ElMessage.error(response.data.message || '作业提交失败')
    }
  } catch (error) {
    console.error('作业提交失败:', error)
    ElMessage.error('作业提交失败：' + (error.response?.data?.message || error.message))
  } finally {
    submitting.value = false
  }
}

// 页面加载时获取数据
onMounted(async () => {
  await fetchCourseList()
  await fetchAssignments()
})
</script>

<style lang="scss" scoped>
.assignments-container {
  padding: 20px;
  
  h1 {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
    color: #303133;
  }
  
  .statistics-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .statistics-row {
      display: flex;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 10px;
      
      .stat-box {
        padding: 10px 15px;
        text-align: center;
        flex: 1;
        min-width: 100px;
        
        .stat-value {
          font-size: 22px;
          font-weight: bold;
          color: #409EFF;
          margin-bottom: 5px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .assignments-card {
    .assignments-tip {
      padding: 30px 0;
    }
    
    .assignment-title {
      display: flex;
      align-items: center;
    }
    
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
  
  .assignment-detail {
    padding: 20px;
    
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
        
        &.description,
        &.content,
        &.comment {
          padding: 10px;
          background-color: #f9f9f9;
          border-radius: 4px;
          min-height: 40px;
          white-space: pre-line;
        }
        
        &.score {
          font-weight: bold;
          color: #E6A23C;
        }
        
        .submit-time {
          margin-left: 10px;
          color: #909399;
          font-size: 14px;
        }
      }
    }
    
    .detail-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
  
  .submission-form {
    .assignment-info {
      margin-bottom: 20px;
      
      h3 {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 15px;
        color: #303133;
        border-bottom: 1px solid #EBEEF5;
        padding-bottom: 10px;
      }
      
      .info-row {
        display: flex;
        margin-bottom: 15px;
        
        .info-label {
          width: 80px;
          color: #606266;
          font-weight: 500;
        }
        
        .info-value {
          flex: 1;
          
          &.description {
            padding: 10px;
            background-color: #f9f9f9;
            border-radius: 4px;
            max-height: 100px;
            overflow-y: auto;
            white-space: pre-line;
          }
        }
      }
    }
    
    .submission-section {
      h3 {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 15px;
        color: #303133;
        border-bottom: 1px solid #EBEEF5;
        padding-bottom: 10px;
      }
      
      .upload-area {
        width: 100%;
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        padding: 20px;
        text-align: center;
        
        &:hover {
          border-color: #409EFF;
        }
      }
      
      .submission-notice {
        margin-top: 20px;
      }
    }
  }
}
</style> 