<template>
  <div class="student-home">
    <h1>学生首页</h1>
    <el-card class="welcome-card">
      <div class="welcome-message">
        <h2>欢迎回来，{{ userInfo?.realName || '同学' }}</h2>
        <p>您可以在这里查看您的课程、学习资料和提交反馈。</p>
      </div>
    </el-card>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card v-loading="coursesLoading">
          <template #header>
            <div class="card-header">
              <span>我的课程</span>
              <el-button type="text" @click="goToCourses">查看全部</el-button>
            </div>
          </template>
          <div class="empty-block" v-if="recentCourses.length === 0">
            <el-empty description="暂无课程数据"></el-empty>
          </div>
          <div v-else class="table-container">
            <el-table 
              v-if="showCoursesTable"
              :data="recentCourses" 
              style="width: 100%"
              :height="tableHeight"
              :show-header="true"
              :highlight-current-row="false"
            >
              <el-table-column prop="name" label="课程名称" />
              <el-table-column prop="teacherName" label="教师" width="120" />
              <el-table-column prop="classTime" label="上课时间" width="130" />
              <el-table-column prop="classLocation" label="上课地点" width="130" />
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusTag(scope.row.status)">
                    {{ getStatusLabel(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewMaterials(scope.row.id)">
                    查看资料
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="loading-placeholder"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card v-loading="materialsLoading">
          <template #header>
            <div class="card-header">
              <span>最新资料</span>
              <el-button type="text" @click="goToMaterials">查看全部</el-button>
            </div>
          </template>
          <div class="empty-block" v-if="recentMaterials.length === 0">
            <el-empty description="暂无资料数据"></el-empty>
          </div>
          <div v-else class="table-container">
            <el-table 
              v-if="showMaterialsTable"
              :data="recentMaterials" 
              style="width: 100%"
              :height="tableHeight"
              :show-header="true"
              :highlight-current-row="false"
            >
              <el-table-column prop="name" label="资料名称" />
              <el-table-column prop="courseName" label="所属课程" width="120" />
              <el-table-column label="上传时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="downloadMaterial(scope.row)">
                    下载
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="loading-placeholder"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row class="mt-20">
      <el-col :span="24">
        <el-card v-loading="assignmentsLoading">
          <template #header>
            <div class="card-header">
              <span>待完成作业</span>
              <el-button type="text" @click="goToAssignments">查看全部</el-button>
            </div>
          </template>
          <div class="empty-block" v-if="recentAssignments.length === 0">
            <el-empty description="暂无待完成作业"></el-empty>
          </div>
          <div v-else class="table-container">
            <el-table 
              v-if="showAssignmentsTable"
              :data="recentAssignments" 
              style="width: 100%"
              :height="tableHeight"
              :show-header="true"
              :highlight-current-row="false"
            >
              <el-table-column prop="title" label="作业标题" min-width="180" />
              <el-table-column prop="courseName" label="所属课程" width="150" />
              <el-table-column label="截止日期" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.deadline) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusTag(scope.row.status)">
                    {{ getStatusLabel(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="提交状态" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.submitted" type="success">已提交</el-tag>
                  <el-tag v-else type="danger">未提交</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default>
                  <el-button 
                    type="success" 
                    size="small" 
                    @click="goToAssignments"
                  >
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="loading-placeholder"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const store = useStore()
const router = useRouter()
const userInfo = computed(() => store.state.user.userInfo)

// 课程相关
const coursesLoading = ref(false)
const recentCourses = ref([])
const showCoursesTable = ref(false)

// 资料相关
const materialsLoading = ref(false)
const recentMaterials = ref([])
const showMaterialsTable = ref(false)

// 作业相关
const assignmentsLoading = ref(false)
const recentAssignments = ref([])
const showAssignmentsTable = ref(false)

// 表格高度
const tableHeight = 200

// 添加 ResizeObserver 错误处理
const originalError = window.console.error;
const errorHandler = function() {
  const args = Array.from(arguments);
  const firstArg = args[0];
  
  if (firstArg && typeof firstArg === 'string' && firstArg.includes('ResizeObserver')) {
    // 忽略 ResizeObserver 错误
    return;
  }
  return originalError.apply(window.console, args);
};

// 获取最近的课程（最多5个）
const fetchRecentCourses = async () => {
  coursesLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/student/courses', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data.code === 200) {
      // 获取最近的5个课程
      recentCourses.value = (response.data.data || []).slice(0, 5)
      console.log('获取最近课程成功:', recentCourses.value)
      
      // 延迟显示表格
      nextTick(() => {
        showCoursesTable.value = true
      })
      
      // 获取作业列表
      fetchRecentAssignments()
    } else {
      console.error('获取课程失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取课程出错:', error)
  } finally {
    coursesLoading.value = false
  }
}

// 获取最近的资料（最多5个）
const fetchRecentMaterials = async () => {
  materialsLoading.value = true
  try {
    const token = localStorage.getItem('token')
    // 先获取课程列表，用于后续关联课程名称
    const coursesResponse = await axios.get('/api/student/courses', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (coursesResponse.data.code === 200) {
      const courses = coursesResponse.data.data || []
      
      // 直接查询所有课程资料，不限定特定课程
      const materialsResponse = await axios.get('/api/material/list', {
        params: {
          pageNum: 1,
          pageSize: 5,
          // 不指定courseId，查询所有课程
          // 按创建时间降序排序
          orderBy: 'create_time',
          orderType: 'desc'
        },
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (materialsResponse.data.code === 200) {
        // 获取最新的5个资料
        const materials = materialsResponse.data.data.records || []
        
        // 添加课程名称
        recentMaterials.value = materials.map(material => {
          const course = courses.find(c => c.id === material.courseId)
          return {
            ...material,
            courseName: course ? course.name : '未知课程'
          }
        })
        
        // 延迟显示表格
        nextTick(() => {
          showMaterialsTable.value = true
        })
        
        console.log('获取最新资料成功:', recentMaterials.value)
      } else {
        console.error('获取资料失败:', materialsResponse.data.message)
      }
    }
  } catch (error) {
    console.error('获取资料出错:', error)
  } finally {
    materialsLoading.value = false
  }
}

// 获取待完成作业（最多5个）
const fetchRecentAssignments = async () => {
  assignmentsLoading.value = true
  try {
    // 使用正确的学生作业列表API
    const response = await axios.get('/api/assignment/student/list', {
      params: {
        current: 1,
        size: 5,
        status: 1 // 只查询进行中的作业
      }
    })
    
    if (response.data.code === 200) {
      // 获取所有作业
      const allAssignments = response.data.data.records || []
      
      // 筛选未提交的作业
      const pendingAssignments = allAssignments
        .filter(a => !a.submitted)
        .slice(0, 5)
      
      recentAssignments.value = pendingAssignments
      
      // 延迟显示表格
      nextTick(() => {
        showAssignmentsTable.value = true
      })
      
      console.log('获取待完成作业成功:', recentAssignments.value)
    } else {
      console.error('获取作业失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取作业出错:', error)
  } finally {
    assignmentsLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 获取课程状态标签类型
const getStatusTag = (status) => {
  switch (status) {
    case 0: return 'danger'
    case 1: return 'success'
    case 2: return 'info'
    default: return ''
  }
}

// 获取课程状态标签文本
const getStatusLabel = (status) => {
  switch (status) {
    case 0: return '已关闭'
    case 1: return '正常'
    case 2: return '已结束'
    default: return '未知'
  }
}

// 查看课程资料
const viewMaterials = (courseId) => {
  router.push({
    name: 'StudentMaterials',
    query: { courseId }
  })
}


// 下载资料
const downloadMaterial = (material) => {
  window.open(`/api/material/download/${material.id}`, '_blank')
  ElMessage.success('开始下载资料')
}

// 跳转到课程页面
const goToCourses = () => {
  router.push({ name: 'StudentCourses' })
}

// 跳转到资料页面
const goToMaterials = () => {
  // 如果有课程，跳转到第一个课程的资料页面
  if (recentCourses.value.length > 0) {
    router.push({
      name: 'StudentMaterials',
      query: { courseId: recentCourses.value[0].id }
    })
  } else {
    // 否则跳转到课程页面
    router.push({ name: 'StudentCourses' })
    ElMessage.info('请先选择一门课程')
  }
}

// 跳转到作业页面
const goToAssignments = () => {
  router.push({ name: 'StudentAssignments' })
}

// 页面加载时获取数据
onMounted(() => {
  // 设置错误处理器
  window.console.error = errorHandler;
  
  // 获取数据
  fetchRecentCourses();
  fetchRecentMaterials();
});

onBeforeUnmount(() => {
  // 恢复原始错误处理
  window.console.error = originalError;
});
</script>

<style lang="scss" scoped>
.student-home {
  h1 {
    margin-bottom: 20px;
  }
  
  .welcome-card {
    margin-bottom: 20px;
    
    .welcome-message {
      h2 {
        margin-top: 0;
        margin-bottom: 10px;
        color: #409EFF;
      }
      
      p {
        margin: 0;
        color: #606266;
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
  
  .empty-block {
    padding: 30px 0;
  }
  
  .table-container {
    height: 200px;
    overflow: hidden;
    
    .el-table {
      width: 100%;
      height: 100%;
    }
  }
  
  .loading-placeholder {
    height: 200px;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f7fa;
    border-radius: 4px;
    color: #909399;
    font-size: 14px;
    
    &::before {
      content: "加载中...";
    }
  }
}
</style> 