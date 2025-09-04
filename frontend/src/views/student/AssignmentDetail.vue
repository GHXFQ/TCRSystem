<template>
  <div class="assignment-detail">
    <div class="page-header">
      <el-page-header @back="goBack" :title="assignment.courseName || '课程作业'" :content="assignment.title || '作业详情'" />
    </div>
    
    <el-card v-loading="loading" class="assignment-card">
      <template #header>
        <div class="card-header">
          <span>作业详情</span>
          <div>
            <el-tag :type="getStatusTag(assignment.status)">{{ getStatusLabel(assignment.status) }}</el-tag>
            <el-tag v-if="submission.id" type="success" class="ml-10">已提交</el-tag>
            <el-tag v-else type="danger" class="ml-10">未提交</el-tag>
          </div>
        </div>
      </template>
      
      <div class="assignment-info">
        <h2>{{ assignment.title }}</h2>
        <div class="meta-info">
          <span>课程：{{ assignment.courseName }}</span>
          <span>教师：{{ assignment.teacherName }}</span>
          <span>截止日期：{{ formatDate(assignment.deadline) }}</span>
          <span v-if="submission.score !== null && submission.score !== undefined">得分：{{ submission.score }}</span>
        </div>
        
        <div class="description">
          <h3>作业描述</h3>
          <div class="description-content">{{ assignment.description }}</div>
        </div>
        
        <div class="submission-section" v-if="submission.id">
          <h3>我的提交</h3>
          <div class="submission-info">
            <p><strong>提交时间：</strong>{{ formatDate(submission.createTime) }}</p>
            <p v-if="submission.filePath"><strong>提交文件：</strong>{{ submission.fileName }} 
              <el-button type="primary" size="small" @click="downloadFile(submission.id)">下载</el-button>
            </p>
            <div class="submission-content">
              <p><strong>提交内容：</strong></p>
              <div class="content-box">{{ submission.content }}</div>
            </div>
            <div v-if="submission.status === 1" class="feedback">
              <p><strong>教师评语：</strong></p>
              <div class="content-box">{{ submission.comment || '无评语' }}</div>
            </div>
          </div>
        </div>
        
        <div class="submit-form" v-else>
          <h3>提交作业</h3>
          <el-form :model="submitForm" label-width="100px">
            <el-form-item label="提交内容">
              <el-input 
                v-model="submitForm.content" 
                type="textarea" 
                :rows="6" 
                placeholder="请输入作业内容"
              ></el-input>
            </el-form-item>
            <el-form-item label="上传文件">
              <el-upload
                class="upload-demo"
                :action="`/api/assignment/submission/upload`"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                :file-list="fileList"
                :limit="1"
              >
                <el-button type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持任意类型文件，单个文件不超过50MB
                    <div v-if="submitForm.filePath" class="uploaded-file">
                      已上传: {{ submitForm.fileName }}
                    </div>
                  </div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitAssignment" :loading="submitting">提交作业</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const assignmentId = ref(route.params.id)

// 数据对象
const assignment = ref({})
const submission = ref({})
const loading = ref(false)
const submitting = ref(false)

// 提交表单
const submitForm = reactive({
  content: '',
  filePath: '',
  fileSize: 0,
  fileType: '',
  fileName: ''
})

// 文件上传相关
const fileList = ref([])
const uploadHeaders = reactive({
  Authorization: `Bearer ${localStorage.getItem('token')}`
})

// 初始化
onMounted(() => {
  fetchAssignmentDetail()
})

// 获取作业详情
const fetchAssignmentDetail = async () => {
  loading.value = true
  try {
    // 获取作业详情
    const assignmentResponse = await axios.get(`/api/assignment/${assignmentId.value}`)
    if (assignmentResponse.data.code === 200) {
      assignment.value = assignmentResponse.data.data || {}
      
      // 获取提交详情
      const submissionResponse = await axios.get(`/api/assignment/submission/assignment/${assignmentId.value}`)
      if (submissionResponse.data.code === 200) {
        submission.value = submissionResponse.data.data || {}
      }
    } else {
      ElMessage.error(assignmentResponse.data.message || '获取作业详情失败')
    }
  } catch (error) {
    console.error('获取作业详情失败:', error)
    ElMessage.error('获取作业详情失败')
  } finally {
    loading.value = false
  }
}

// 提交作业
const submitAssignment = async () => {
  if (!submitForm.content && !submitForm.filePath) {
    ElMessage.warning('请输入作业内容或上传文件')
    return
  }
  
  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('assignmentId', assignmentId.value)
    formData.append('content', submitForm.content)
    
    if (fileList.value.length > 0) {
      // 如果已经通过el-upload上传了文件，我们只需传递文件路径
      formData.append('filePath', submitForm.filePath)
    }
    
    const response = await axios.post('/api/assignment/submission', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('提交成功')
      // 重新获取作业详情
      fetchAssignmentDetail()
    } else {
      ElMessage.error(response.data.message || '提交失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败：' + (error.response?.data?.message || error.message))
  } finally {
    submitting.value = false
  }
}

// 文件上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    submitForm.filePath = response.data.filePath;
    submitForm.fileName = response.data.fileName;
    ElMessage.success('文件上传成功');
  } else {
    ElMessage.error(response.message || '文件上传失败');
  }
}

// 文件上传失败
const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

// 上传前检查
const beforeUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
  return true
}

// 下载文件
const downloadFile = (submissionId) => {
  if (!submissionId) {
    ElMessage.warning('提交ID为空')
    return
  }
  
  try {
    // 创建下载链接
    const downloadUrl = `/api/assignment/submission/download/${submissionId}`
    
    // 创建一个隐藏的a标签，用于下载
    const link = document.createElement('a')
    link.href = downloadUrl
    link.setAttribute('download', '') // 强制使用下载属性
    document.body.appendChild(link)
    link.click()
    
    // 清理DOM
    setTimeout(() => {
      document.body.removeChild(link)
    }, 100)
    
    ElMessage.success('开始下载文件')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// 返回上一页
const goBack = () => {
  router.push({
    name: 'StudentAssignments',
    query: { courseId: assignment.value.courseId }
  })
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 获取作业状态标签类型
const getStatusTag = (status) => {
  switch (status) {
    case 0: return 'danger'
    case 1: return 'success'
    case 2: return 'info'
    default: return ''
  }
}

// 获取作业状态标签文本
const getStatusLabel = (status) => {
  switch (status) {
    case 0: return '已关闭'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}
</script>

<style lang="scss" scoped>
.assignment-detail {
  .page-header {
    margin-bottom: 20px;
  }
  
  .assignment-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .ml-10 {
    margin-left: 10px;
  }
  
  .assignment-info {
    h2 {
      margin-top: 0;
      margin-bottom: 15px;
      color: #303133;
    }
    
    .meta-info {
      display: flex;
      flex-wrap: wrap;
      margin-bottom: 20px;
      color: #606266;
      
      span {
        margin-right: 20px;
        margin-bottom: 10px;
      }
    }
    
    .description {
      margin-bottom: 30px;
      
      h3 {
        margin-top: 0;
        margin-bottom: 10px;
        font-size: 16px;
        color: #303133;
      }
      
      .description-content {
        padding: 15px;
        background-color: #f5f7fa;
        border-radius: 4px;
        white-space: pre-wrap;
      }
    }
    
    .submission-section {
      margin-bottom: 30px;
      
      h3 {
        margin-top: 0;
        margin-bottom: 10px;
        font-size: 16px;
        color: #303133;
      }
      
      .submission-info {
        p {
          margin: 10px 0;
        }
        
        .content-box {
          padding: 15px;
          background-color: #f5f7fa;
          border-radius: 4px;
          margin-top: 5px;
          white-space: pre-wrap;
        }
        
        .feedback {
          margin-top: 20px;
        }
      }
    }
    
    .submit-form {
      h3 {
        margin-top: 0;
        margin-bottom: 20px;
        font-size: 16px;
        color: #303133;
      }
      
      .uploaded-file {
        margin-top: 8px;
        padding: 5px 10px;
        background-color: #f0f9eb;
        color: #67c23a;
        border-radius: 4px;
        display: inline-block;
      }
    }
  }
}
</style> 