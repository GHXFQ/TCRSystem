<template>
  <div class="assignment-submissions-container">
    <div class="page-header">
      <div class="title-section">
        <el-button @click="goBack" icon="ArrowLeft" circle plain></el-button>
        <h1>{{ title || '作业提交列表' }}</h1>
      </div>
      <div class="info-section" v-if="assignmentInfo">
        <el-tag :type="getStatusType(assignmentInfo.status)">{{ getStatusText(assignmentInfo.status) }}</el-tag>
        <span>所属课程: {{ assignmentInfo.courseName }}</span>
        <span>截止时间: {{ assignmentInfo.deadline }}</span>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-card class="statistics-card" v-if="statistics">
      <div class="statistics-row">
        <div class="stat-box">
          <div class="stat-value">{{ statistics.totalStudents || 0 }}</div>
          <div class="stat-label">学生总数</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.submittedCount || 0 }}</div>
          <div class="stat-label">已提交</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.submissionRate || '0%' }}</div>
          <div class="stat-label">提交率</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.gradedCount || 0 }}</div>
          <div class="stat-label">已批改</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.gradingRate || '0%' }}</div>
          <div class="stat-label">批改率</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ statistics.avgScore || '暂无' }}</div>
          <div class="stat-label">平均分</div>
        </div>
      </div>
    </el-card>
    
    <!-- 过滤器 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="选择状态" 
            clearable 
            style="width: 120px;"
          >
            <el-option label="已提交" :value="0"></el-option>
            <el-option label="已批改" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchSubmissions">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 提交列表 -->
    <el-card v-loading="loading" class="submissions-card">
      <div class="empty-tip" v-if="!submissionList.length && !loading">
        <el-empty description="暂无符合条件的作业提交"></el-empty>
      </div>
      
      <el-table 
        v-else 
        :data="submissionList" 
        style="width: 100%"
        border
      >
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="100" />
        <el-table-column prop="courseName" label="所属课程" width="150" show-overflow-tooltip />
        <el-table-column prop="content" label="提交内容" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div v-if="scope.row.content" class="submission-content">{{ scope.row.content }}</div>
            <div v-else class="submission-content empty">无文本内容</div>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="提交文件" width="180" show-overflow-tooltip>
          <template #default="scope">
            <el-link 
              v-if="scope.row.filePath" 
              :href="`/api/assignment/submission/download/${scope.row.id}`" 
              target="_blank"
              type="primary"
            >
              {{ scope.row.fileName || '下载文件' }}
            </el-link>
            <span v-else>无文件</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getSubmissionStatusType(scope.row.status)">
              {{ getSubmissionStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isOverdue" label="是否逾期" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isOverdue ? 'danger' : 'success'" size="small">
              {{ scope.row.isOverdue ? '已逾期' : '按时提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80" align="center">
          <template #default="scope">
            {{ scope.row.score !== null && scope.row.score !== undefined ? scope.row.score : '未批改' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              plain
              @click="openGradeDialog(scope.row)"
            >批改</el-button>
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
    
    <!-- 批改对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      title="批改作业"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentSubmission" class="grading-form">
        <div class="submission-info">
          <h3>提交信息</h3>
          
          <div class="info-row">
            <div class="info-label">学生</div>
            <div class="info-value">{{ currentSubmission.studentName }} ({{ currentSubmission.studentId }})</div>
          </div>
          
          <div class="info-row">
            <div class="info-label">提交内容</div>
            <div class="info-value content">
              <div v-if="currentSubmission.content" class="content-text">{{ currentSubmission.content }}</div>
              <div v-else class="content-text empty">无文本内容</div>
            </div>
          </div>
          
          <div class="info-row" v-if="currentSubmission.filePath">
            <div class="info-label">提交文件</div>
            <div class="info-value">
              <el-link 
                :href="`/api/assignment/submission/download/${currentSubmission.id}`" 
                target="_blank"
                type="primary"
              >
                {{ currentSubmission.fileName || '下载文件' }}
              </el-link>
            </div>
          </div>
          
          <div class="info-row">
            <div class="info-label">提交时间</div>
            <div class="info-value">
              {{ currentSubmission.createTime }}
              <el-tag 
                :type="currentSubmission.isOverdue ? 'danger' : 'success'" 
                size="small" 
                style="margin-left: 10px;"
              >
                {{ currentSubmission.isOverdue ? '已逾期' : '按时提交' }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="grading-section">
          <h3>评分</h3>
          
          <el-form :model="gradeForm" :rules="rules" ref="gradeFormRef" label-width="80px">
            <el-form-item label="得分" prop="score">
              <el-input-number 
                v-model="gradeForm.score" 
                :min="0" 
                :max="assignmentInfo?.maxScore || 100" 
                :step="1"
                style="width: 120px;"
              />
              <span class="score-hint">满分: {{ assignmentInfo?.maxScore || 100 }}</span>
            </el-form-item>
            
            <el-form-item label="评语" prop="comment">
              <el-input 
                v-model="gradeForm.comment" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入评语" 
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGrade" :loading="submitting">提交评分</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {  } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const submissionList = ref([])
const total = ref(0)
const assignmentInfo = ref(null)
const statistics = ref(null)
const gradeDialogVisible = ref(false)
const currentSubmission = ref(null)

// 查询参数
const queryParams = reactive({
  current: 1,
  pageSize: 10,
  assignmentId: null,
  status: null
})

// 评分表单
const gradeFormRef = ref(null)
const gradeForm = reactive({
  id: null,
  score: 0,
  comment: ''
})

// 表单校验规则
const rules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' }
  ]
}

// 计算属性
const title = computed(() => {
  return route.query.title ? `作业: ${route.query.title}` : '作业提交列表'
})

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
    case 0: return 'warning' // 已提交
    case 1: return 'success' // 已批改
    default: return 'info'
  }
}

// 获取提交状态文本
const getSubmissionStatusText = (status) => {
  switch (status) {
    case 0: return '已提交'
    case 1: return '已批改'
    default: return '未知'
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 获取作业信息
const fetchAssignmentInfo = async () => {
  try {
    const assignmentId = route.query.assignmentId
    if (!assignmentId) {
      ElMessage.warning('未指定作业ID')
      return
    }
    
    const response = await axios.get(`/api/assignment/${assignmentId}`)
    if (response.data.code === 200) {
      assignmentInfo.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取作业信息失败')
    }
  } catch (error) {
    console.error('获取作业信息失败:', error)
    ElMessage.error('获取作业信息失败')
  }
}

// 获取提交统计
const fetchSubmissionStatistics = async () => {
  try {
    const assignmentId = route.query.assignmentId
    if (!assignmentId) return
    
    const response = await axios.get(`/api/assignment/submission/statistics/${assignmentId}`)
    if (response.data.code === 200) {
      statistics.value = response.data.data
    }
  } catch (error) {
    console.error('获取提交统计失败:', error)
    ElMessage.error('获取提交统计失败')
  }
}

// 获取提交列表
const fetchSubmissions = async () => {
  loading.value = true
  try {
    const assignmentId = route.query.assignmentId
    if (!assignmentId) {
      ElMessage.warning('未指定作业ID')
      loading.value = false
      return
    }
    
    queryParams.assignmentId = assignmentId
    
    const params = {
      current: queryParams.current,
      size: queryParams.pageSize,
      assignmentId: queryParams.assignmentId,
      status: queryParams.status
    }
    
    const response = await axios.get('/api/assignment/submission/list', { params })
    
    if (response.data.code === 200) {
      submissionList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      ElMessage.error(response.data.message || '获取提交列表失败')
    }
  } catch (error) {
    console.error('获取提交列表失败:', error)
    ElMessage.error('获取提交列表失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.status = null
  queryParams.current = 1
  fetchSubmissions()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  queryParams.current = current
  fetchSubmissions()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.current = 1
  fetchSubmissions()
}

// 打开批改对话框
const openGradeDialog = (submission) => {
  currentSubmission.value = submission
  
  // 初始化表单
  gradeForm.id = submission.id
  gradeForm.score = submission.score !== null && submission.score !== undefined ? submission.score : 0
  gradeForm.comment = submission.comment || ''
  
  // 打开对话框
  gradeDialogVisible.value = true
  
  // 重置表单验证
  setTimeout(() => {
    gradeFormRef.value?.clearValidate()
  }, 0)
}

// 提交评分
const submitGrade = async () => {
  if (!gradeFormRef.value) return
  
  await gradeFormRef.value.validate(async (valid) => {
    if (!valid) {
      return false
    }
    
    submitting.value = true
    try {
      const params = {
        id: gradeForm.id,
        score: gradeForm.score,
        comment: gradeForm.comment
      }
      
      const response = await axios.post('/api/assignment/grade', null, { params })
      
      if (response.data.code === 200) {
        ElMessage.success('评分提交成功')
        gradeDialogVisible.value = false
        
        // 更新本地数据
        const index = submissionList.value.findIndex(item => item.id === gradeForm.id)
        if (index !== -1) {
          submissionList.value[index].score = gradeForm.score
          submissionList.value[index].comment = gradeForm.comment
          submissionList.value[index].status = 2 // 已批改
        }
        
        // 重新获取统计数据
        fetchSubmissionStatistics()
      } else {
        ElMessage.error(response.data.message || '评分提交失败')
      }
    } catch (error) {
      console.error('评分提交失败:', error)
      ElMessage.error('评分提交失败：' + (error.response?.data?.message || error.message))
    } finally {
      submitting.value = false
    }
  })
}

// 页面加载时获取数据
onMounted(async () => {
  await fetchAssignmentInfo()
  await fetchSubmissions()
  await fetchSubmissionStatistics()
})
</script>

<style lang="scss" scoped>
.assignment-submissions-container {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    
    .title-section {
      display: flex;
      align-items: center;
      gap: 10px;
      
      h1 {
        margin: 0;
        font-size: 22px;
        font-weight: 500;
        color: #303133;
      }
    }
    
    .info-section {
      display: flex;
      align-items: center;
      gap: 15px;
      color: #606266;
    }
  }
  
  .statistics-card {
    margin-bottom: 20px;
    
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
  
  .submissions-card {
    .empty-tip {
      padding: 30px 0;
    }
    
    .submission-content {
      max-height: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      
      &.empty {
        color: #909399;
        font-style: italic;
      }
    }
    
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
  
  .grading-form {
    .submission-info {
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
          
          &.content {
            .content-text {
              padding: 10px;
              background-color: #f9f9f9;
              border-radius: 4px;
              max-height: 100px;
              overflow-y: auto;
              
              &.empty {
                color: #909399;
                font-style: italic;
              }
            }
          }
        }
      }
    }
    
    .grading-section {
      h3 {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 15px;
        color: #303133;
        border-bottom: 1px solid #EBEEF5;
        padding-bottom: 10px;
      }
      
      .score-hint {
        margin-left: 10px;
        color: #909399;
        font-size: 14px;
      }
    }
  }
}
</style> 