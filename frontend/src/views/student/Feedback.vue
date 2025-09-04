<template>
  <div class="feedback-container">
    <h1>课程反馈</h1>
    
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="课程">
          <el-select 
            v-model="queryParams.courseId" 
            placeholder="选择课程" 
            clearable 
            @change="handleCourseChange"
            style="width: 220px;"
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
        <el-form-item>
          <el-button type="primary" @click="fetchFeedbackList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 显示当前筛选条件 -->
      <div class="filter-info" v-if="selectedCourseName">
        <span>当前筛选: </span>
        <el-tag v-if="selectedCourseName" type="info" class="filter-tag">
          课程: {{ selectedCourseName }}
        </el-tag>
      </div>
    </el-card>
    
    <el-card v-if="queryParams.courseId" class="add-feedback-card">
      <template #header>
        <div class="card-header">
          <span>发表反馈</span>
        </div>
      </template>
      <el-form ref="feedbackFormRef" :model="feedbackForm" :rules="feedbackRules">
        <el-form-item prop="content">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入您的反馈或问题..."
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card v-if="queryParams.courseId" class="feedback-list-card">
      <template #header>
        <div class="card-header">
          <span>反馈列表</span>
        </div>
      </template>
      
      <div v-loading="loading">
        <div v-if="feedbackList.length === 0" class="empty-block">
          <el-empty description="暂无反馈数据"></el-empty>
        </div>
        
        <div v-else class="feedback-list">
          <div v-for="item in feedbackList" :key="item.id" class="feedback-item">
            <div class="feedback-header">
              <div class="user-info">
                <div class="avatar-container">
                  <div class="user-name-above">{{ getUserDisplayName(item) }}</div>
                  <el-avatar :size="40" :src="item.userAvatar || defaultAvatar"></el-avatar>
                </div>
              </div>
              <div class="feedback-actions">
                <span class="feedback-time">{{ formatDateTime(item.createTime) }}</span>
                <el-button 
                  v-if="canDelete(item)" 
                  type="danger" 
                  size="small" 
                  icon="Delete" 
                  circle 
                  plain
                  @click="deleteFeedback(item.id)"
                ></el-button>
              </div>
            </div>
            <div class="feedback-content">{{ item.content }}</div>
            
            <!-- 回复列表 -->
            <div v-if="item.children && item.children.length > 0" class="reply-list">
              <div v-for="reply in item.children" :key="reply.id" class="reply-item">
                <div class="reply-header">
                  <div class="user-info">
                    <div class="avatar-container">
                      <div class="user-name-above">{{ getUserDisplayName(reply) }}</div>
                      <el-avatar :size="30" :src="reply.userAvatar || defaultAvatar"></el-avatar>
                    </div>
                  </div>
                  <div class="reply-actions">
                    <span class="reply-time">{{ formatDateTime(reply.createTime) }}</span>
                    <el-button 
                      v-if="canDelete(reply)" 
                      type="danger" 
                      size="small" 
                      icon="Delete" 
                      circle 
                      plain
                      @click="deleteFeedback(reply.id)"
                    ></el-button>
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
                :rows="2"
                placeholder="回复该反馈..."
              />
              <el-button type="primary" size="small" @click="submitReply(item.id)">回复</el-button>
            </div>
          </div>
        </div>
        
        <div class="pagination-container">
          <el-pagination
            :current-page="queryParams.pageNum"
            :page-size="queryParams.pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 状态变量
const loading = ref(false)
const feedbackList = ref([])
const courseList = ref([])
const total = ref(0)
const feedbackFormRef = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 当前用户ID
const currentUserId = ref(null)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  courseId: undefined
})

// 反馈表单
const feedbackForm = reactive({
  content: '',
  courseId: computed(() => queryParams.courseId)
})

// 回复内容
const replyContents = reactive({})

// 表单校验规则
const feedbackRules = {
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
  ]
}

// 计算属性：当前选中的课程名称
const selectedCourseName = computed(() => {
  if (!queryParams.courseId) return ''
  const course = courseList.value.find(c => c.id === queryParams.courseId)
  return course ? course.name : ''
})

// 获取当前用户信息
const getCurrentUser = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    currentUserId.value = userInfo.id
    console.log('当前用户ID:', currentUserId.value)
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
  return item.userId === currentUserId.value
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
    const response = await axios.get('/api/student/courses')
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
    // 打印API返回的原始数据
    console.log('反馈列表原始数据:', response.data.data);
    
    feedbackList.value = response.data.data.records || []
    // 打印处理后的数据
    console.log('反馈列表处理后数据:', feedbackList.value);
    
    // 检查所有反馈的数据
    if (feedbackList.value.length > 0) {
      feedbackList.value.forEach((feedback, index) => {
        console.log(`第${index+1}条反馈 ID:${feedback.id}, 用户ID:${feedback.userId}, 用户名:${feedback.userName}`);
        
        // 检查回复数据
        if (feedback.children && feedback.children.length > 0) {
          feedback.children.forEach((reply, replyIndex) => {
            console.log(`  - 回复${replyIndex+1} ID:${reply.id}, 用户ID:${reply.userId}, 用户名:${reply.userName}`);
          });
        }
      });
    }
    
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

// 提交反馈
const submitFeedback = () => {
  if (!queryParams.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }
  
  feedbackFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 这里应该调用API提交反馈
        const response = await axios.post('/api/feedback/add', {
          content: feedbackForm.content,
          courseId: queryParams.courseId
        })
        
        if (response.data.code === 200) {
          ElMessage.success('反馈提交成功')
          feedbackForm.content = ''
          fetchFeedbackList()
        } else {
          ElMessage.error(response.data.message || '反馈提交失败')
        }
      } catch (error) {
        console.error('提交反馈失败:', error)
        ElMessage.error('提交反馈失败')
      }
    }
  })
}

// 提交回复
const submitReply = async (parentId) => {
  const content = replyContents[parentId]
  if (!content || content.trim() === '') {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    // 这里应该调用API提交回复
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