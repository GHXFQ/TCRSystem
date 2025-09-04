<template>
  <div class="feedback-container">
    <h1>课程反馈管理</h1>
    
    <!-- 过滤器卡片 -->
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>反馈过滤</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="选择课程">
            <el-select v-model="queryParams.courseId" placeholder="请选择课程" clearable @change="handleCourseChange" style="width: 100%">
              <el-option
                v-for="course in courseList"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <div class="filter-info" v-if="queryParams.courseId">
        <span>当前过滤：</span>
        <el-tag class="filter-tag" v-if="queryParams.courseId">
          课程：{{ courseList.find(c => c.id === queryParams.courseId)?.name }}
        </el-tag>
        <el-button type="primary" size="small" @click="fetchFeedbackList">刷新数据</el-button>
        <el-button size="small" @click="resetQuery">重置</el-button>
      </div>
    </el-card>
    
    <!-- 创建反馈卡片 -->
    <el-card class="add-feedback-card" v-if="queryParams.courseId">
      <template #header>
        <div class="card-header">
          <span>创建新话题</span>
        </div>
      </template>
      
      <el-form ref="feedbackFormRef" :model="feedbackForm" :rules="feedbackRules">
        <el-form-item prop="content">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            rows="3"
            placeholder="输入新话题内容..."
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitFeedback">发布话题</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 反馈列表卡片 -->
    <el-card class="feedback-list-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>反馈列表</span>
        </div>
      </template>
      
      <div class="feedback-list" v-if="feedbackList.length > 0">
        <div class="feedback-item" v-for="item in feedbackList" :key="item.id">
          <div class="feedback-header">
            <div class="user-info">
              <div class="avatar-container">
                <div class="user-name-above">用户</div>
                <el-avatar :size="40" :src="getFullAvatarUrl(item.userAvatar)">{{ getUserDisplayName(item).charAt(0) }}</el-avatar>
              </div>
              <div class="user-name">{{ getUserDisplayName(item) }}</div>
            </div>
            <div class="feedback-actions">
              <span class="feedback-time">{{ formatDateTime(item.createTime) }}</span>
              <el-button type="danger" size="small" v-if="isAdmin" @click="deleteFeedback(item.id)">删除</el-button>
            </div>
          </div>
          
          <div class="feedback-content">{{ item.content }}</div>
          
          <!-- 回复列表 -->
          <div class="reply-list" v-if="item.children && item.children.length > 0">
            <div class="reply-item" v-for="reply in item.children" :key="reply.id">
              <div class="reply-header">
                <div class="user-info">
                  <div class="avatar-container">
                    <div class="user-name-above">{{ reply.userId === currentUserId ? '我' : '用户' }}</div>
                    <el-avatar :size="30" :src="getFullAvatarUrl(reply.userAvatar)">{{ getUserDisplayName(reply).charAt(0) }}</el-avatar>
                  </div>
                  <div class="user-name">{{ getUserDisplayName(reply) }}</div>
                </div>
                <div class="reply-actions">
                  <span class="reply-time">{{ formatDateTime(reply.createTime) }}</span>
                  <el-button type="danger" size="small" v-if="canDelete(reply)" @click="deleteFeedback(reply.id)">删除</el-button>
                </div>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
            </div>
          </div>
          
          <!-- 回复表单 -->
          <div class="reply-form">
            <el-input
              v-model="replyContents[item.id]"
              type="textarea"
              rows="2"
              placeholder="输入回复内容..."
              maxlength="500"
              show-word-limit
              style="width: 90%;"
            ></el-input>
            <el-button type="primary" @click="submitReply(item.id)">回复</el-button>
          </div>
        </div>
      </div>
      
      <div class="empty-block" v-else>
        <el-empty description="暂无反馈数据"></el-empty>
      </div>
      
      <!-- 分页器 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="queryParams.pageSize"
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 处理头像路径，确保使用正确的服务器地址
const getFullAvatarUrl = (path) => {
  if (!path) return '';
  // 如果已经是完整URL，则直接返回
  if (path.startsWith('http')) {
    return path;
  }
  // 否则拼接后端地址
  return `http://localhost:8080${path}`;
};

// 当前用户ID
const currentUserId = ref(0)

// 是否为管理员
const isAdmin = ref(false)

// 课程列表
const courseList = ref([])

// 反馈列表
const feedbackList = ref([])

// 加载状态
const loading = ref(false)

// 总数据条数
const total = ref(0)

// 回复内容映射（key: 父反馈ID, value: 回复内容）
const replyContents = reactive({})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  courseId: undefined
})

// 获取当前用户信息
const getCurrentUser = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    currentUserId.value = userInfo.id
    isAdmin.value = userInfo.role === 2
    console.log('当前用户ID:', currentUserId.value, '角色:', userInfo.role)
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取用户显示名称
const getUserDisplayName = (item) => {
  // 调试
  if (item.id && !item.userName) {
    console.warn(`ID为${item.id}的${item.parentId ? '回复' : '反馈'}没有用户名`, item)
  }
  
  return item.userName || '匿名用户'
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
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

// 判断是否可以删除反馈或回复
const canDelete = (item) => {
  return item.userId === currentUserId.value || isAdmin.value
}

// 删除反馈或回复
const deleteFeedback = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条内容吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`/api/feedback/${id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      fetchFeedbackList() // 刷新列表
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 获取课程列表
const fetchCourseList = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const teacherId = userInfo.id
    const response = await axios.get(`/api/course/teacher/${teacherId}`)
    courseList.value = response.data.data || []
    
    // 如果有课程，默认选择第一个
    if (courseList.value.length > 0 && !queryParams.courseId) {
      queryParams.courseId = courseList.value[0].id
      await fetchFeedbackList()
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 获取反馈列表
const fetchFeedbackList = async () => {
  if (!queryParams.courseId) {
    feedbackList.value = []
    total.value = 0
    return
  }
  
  loading.value = true
  try {
    const response = await axios.get('/api/feedback/list', {
      params: queryParams
    })
    
    console.log('反馈列表原始数据:', response.data.data);
    
    feedbackList.value = response.data.data.records || []
    console.log('反馈列表处理后数据:', feedbackList.value);
    
    total.value = response.data.data.total || 0
  } catch (error) {
    console.error('获取反馈列表失败:', error)
    ElMessage.error('获取反馈列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.courseId = undefined
  feedbackList.value = []
  total.value = 0
}

// 处理课程变化
const handleCourseChange = () => {
  fetchFeedbackList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchFeedbackList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchFeedbackList()
}

// 提交回复
const submitReply = async (parentId) => {
  const content = replyContents[parentId]
  if (!content || content.trim() === '') {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    const response = await axios.post('/api/feedback/reply', {
      content,
      courseId: queryParams.courseId,
      parentId
    })
    
    if (response.data.code === 200) {
      ElMessage.success('回复提交成功')
      replyContents[parentId] = ''
      fetchFeedbackList()
    } else {
      ElMessage.error(response.data.message || '回复提交失败')
    }
  } catch (error) {
    console.error('提交回复失败:', error)
    ElMessage.error('提交回复失败')
  }
}

// 反馈表单
const feedbackFormRef = ref(null)
const feedbackForm = reactive({
  content: ''
})

// 表单验证规则
const feedbackRules = {
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 2, max: 500, message: '长度在2到500个字符之间', trigger: 'blur' }
  ]
}

// 提交反馈
const submitFeedback = () => {
  if (!queryParams.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }
  
  feedbackFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用API提交反馈
        const response = await axios.post('/api/feedback/add', {
          content: feedbackForm.content,
          courseId: queryParams.courseId
        })
        
        if (response.data.code === 200) {
          ElMessage.success('反馈发布成功')
          feedbackForm.content = '' // 清空输入框
          fetchFeedbackList() // 刷新列表
        } else {
          ElMessage.error(response.data.message || '反馈发布失败')
        }
      } catch (error) {
        console.error('提交反馈失败:', error)
        ElMessage.error('提交反馈失败')
      }
    }
  })
}

// 页面加载时获取数据
onMounted(() => {
  getCurrentUser()
  fetchCourseList()
})
</script>

<style lang="scss" scoped>
.feedback-container {
  h1 {
    margin-bottom: 20px;
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    .filter-info {
      margin-top: 10px;
      font-size: 14px;
      color: #606266;
      
      .filter-tag {
        margin-left: 8px;
        margin-right: 8px;
      }
    }
  }
  
  .add-feedback-card {
    margin-bottom: 20px;
  }
  
  .feedback-list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .feedback-list {
      .feedback-item {
        padding: 15px;
        border-bottom: 1px solid #ebeef5;
        
        &:last-child {
          border-bottom: none;
        }
        
        .feedback-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
          
          .user-info {
            display: flex;
            align-items: center;
            
            .avatar-container {
              position: relative;
              display: flex;
              flex-direction: column;
              align-items: center;
              
              .user-name-above {
                font-size: 12px;
                color: #606266;
                margin-bottom: 5px;
                white-space: nowrap;
              }
            }
            
            .user-name {
              margin-left: 10px;
              font-weight: bold;
            }
          }
          
          .feedback-actions {
            display: flex;
            align-items: center;
            
            .feedback-time {
              color: #909399;
              font-size: 12px;
              margin-right: 10px;
            }
          }
        }
        
        .feedback-content {
          margin-bottom: 15px;
          line-height: 1.5;
        }
        
        .reply-list {
          background-color: #f5f7fa;
          border-radius: 4px;
          padding: 10px;
          margin-bottom: 15px;
          
          .reply-item {
            padding: 10px;
            border-bottom: 1px solid #ebeef5;
            
            &:last-child {
              border-bottom: none;
            }
            
            .reply-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 5px;
              
              .user-info {
                display: flex;
                align-items: center;
                
                .avatar-container {
                  position: relative;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  
                  .user-name-above {
                    font-size: 12px;
                    color: #606266;
                    margin-bottom: 5px;
                    white-space: nowrap;
                  }
                }
                
                .user-name {
                  margin-left: 10px;
                  font-weight: bold;
                  font-size: 14px;
                }
              }
              
              .reply-actions {
                display: flex;
                align-items: center;
                
                .reply-time {
                  color: #909399;
                  font-size: 12px;
                  margin-right: 10px;
                }
              }
            }
            
            .reply-content {
              margin-left: 40px;
              line-height: 1.5;
            }
          }
        }
        
        .reply-form {
          display: flex;
          align-items: flex-start;
          
          .el-button {
            margin-left: 10px;
          }
        }
      }
    }
    
    .empty-block {
      padding: 30px 0;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 