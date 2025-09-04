<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="courses-container">
    <h1>课程选择</h1>
    
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="我的课程" name="myCourses">
        <el-card class="course-list-card">
          <template #header>
            <div class="card-header">
              <span>已选课程</span>
            </div>
          </template>
          
          <div v-loading="loading">
            <div v-if="myCourses.length === 0" class="empty-block">
              <el-empty description="暂无已选课程"></el-empty>
            </div>
            
            <el-row :gutter="20" v-else>
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in myCourses" :key="course.id">
                <el-card class="course-card" shadow="hover">
                  <template #header>
                    <div class="course-header">
                      <span class="course-name">{{ course.name }}</span>
                      <el-tag :type="getStatusTag(course.status)">{{ getStatusLabel(course.status) }}</el-tag>
                    </div>
                  </template>
                  <div class="course-content">
                    <p class="course-description">{{ course.description || '暂无描述' }}</p>
                    <p class="course-teacher">教师: {{ course.teacherName || '未知' }}</p>
                    <p class="course-time">
                      <el-icon><Calendar /></el-icon> 上课时间: {{ course.classTime || '未设置' }}
                    </p>
                    <p class="course-location">
                      <el-icon><Location /></el-icon> 上课地点: {{ course.classLocation || '未设置' }}
                    </p>
                    <p class="course-end-time">
                      <el-icon><Timer /></el-icon> 结课时间: {{ formatEndTime(course.endTime) }}
                    </p>
                    <p class="course-end-form">
                      <el-icon><Document /></el-icon> 结课形式: {{ getEndFormLabel(course.endForm) }}
                    </p>
                    <p class="course-end-location" v-if="course.endForm === 0">
                      <el-icon><Location /></el-icon> 考试地点: {{ course.endLocation || '未设置' }}
                    </p>
                  </div>
                  <div class="course-footer">
                    <el-button type="primary" @click="viewMaterials(course.id)">查看资料</el-button>
                    <el-button type="info" @click="viewFeedback(course.id)">课程反馈</el-button>
                    <el-button type="danger" @click="dropCourse(course.id)">退选课程</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="可选课程" name="availableCourses">
        <el-card class="course-list-card">
          <template #header>
            <div class="card-header">
              <span>可选课程</span>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索课程名称"
                style="width: 200px"
                clearable
                @clear="fetchAvailableCourses"
                @keyup.enter="searchCourses"
              >
                <template #append>
                  <el-button @click="searchCourses">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
          </template>
          
          <div v-loading="loading">
            <div v-if="availableCourses.length === 0" class="empty-block">
              <el-empty description="暂无可选课程"></el-empty>
            </div>
            
            <el-row :gutter="20" v-else>
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in availableCourses" :key="course.id">
                <el-card class="course-card" shadow="hover">
                  <template #header>
                    <div class="course-header">
                      <span class="course-name">{{ course.name }}</span>
                      <el-tag :type="getStatusTag(course.status)">{{ getStatusLabel(course.status) }}</el-tag>
                    </div>
                  </template>
                  <div class="course-content">
                    <p class="course-description">{{ course.description || '暂无描述' }}</p>
                    <p class="course-teacher">教师: {{ course.teacherName || '未知' }}</p>
                    <p class="course-time">
                      <el-icon><Calendar /></el-icon> 上课时间: {{ course.classTime || '未设置' }}
                    </p>
                    <p class="course-location">
                      <el-icon><Location /></el-icon> 上课地点: {{ course.classLocation || '未设置' }}
                    </p>
                    <p class="course-end-time">
                      <el-icon><Timer /></el-icon> 结课时间: {{ formatEndTime(course.endTime) }}
                    </p>
                    <p class="course-end-form">
                      <el-icon><Document /></el-icon> 结课形式: {{ getEndFormLabel(course.endForm) }}
                    </p>
                    <p class="course-end-location" v-if="course.endForm === 0">
                      <el-icon><Location /></el-icon> 考试地点: {{ course.endLocation || '未设置' }}
                    </p>
                  </div>
                  <div class="course-footer">
                    <el-button type="primary" @click="selectCourse(course.id)">选择课程</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <div class="pagination-container">
              <el-pagination
                :current-page="currentPage"
                :page-size="pageSize"
                :page-sizes="[8, 16, 24, 32]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Calendar, Location, Timer, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('myCourses')
const myCourses = ref([])
const availableCourses = ref([])
const total = ref(0)
const searchKeyword = ref('')

// 分页参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 8,
  name: '',
  status: 1 // 只查询状态为正常的课程
})

// 计算属性用于v-model绑定
const currentPage = computed({
  get: () => queryParams.pageNum,
  set: (val) => { queryParams.pageNum = val }
})

const pageSize = computed({
  get: () => queryParams.pageSize,
  set: (val) => { queryParams.pageSize = val }
})

// 格式化结课时间
const formatEndTime = (endTime) => {
  if (!endTime) return '未设置';
  return dayjs(endTime).format('YYYY-MM-DD HH:mm');
}

// 获取结课形式标签
const getEndFormLabel = (endForm) => {
  switch (endForm) {
    case 0: return '考试';
    case 1: return '大作业';
    default: return '未设置';
  }
}

// 获取我的课程列表
const fetchMyCourses = async () => {
  loading.value = true
  try {
    console.log('开始获取我的课程列表...');
    const token = localStorage.getItem('token');
    console.log('当前token:', token);
    
    const response = await axios.get('/api/student/courses', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    console.log('获取我的课程列表成功:', response.data);
    if (response.data.code === 200) {
      myCourses.value = response.data.data || [];
      console.log('我的课程数量:', myCourses.value.length);
    } else {
      console.error('获取我的课程列表失败:', response.data.message);
      ElMessage.error(response.data.message || '获取我的课程列表失败');
    }
  } catch (error) {
    console.error('获取我的课程列表失败:', error);
    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误数据:', error.response.data);
    }
    ElMessage.error('获取我的课程列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
}

// 获取可选课程列表
const fetchAvailableCourses = async () => {
  loading.value = true;
  try {
    console.log('开始获取可选课程列表...');
    const token = localStorage.getItem('token');
    console.log('当前token:', token);
    
    const response = await axios.get('/api/course/list', {
      params: queryParams,
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    console.log('获取可选课程列表成功:', response.data);
    if (response.data.code === 200) {
      availableCourses.value = response.data.data.records || [];
      total.value = response.data.data.total || 0;
      
      // 过滤掉已选课程
      if (myCourses.value.length > 0) {
        const myCourseIds = myCourses.value.map(course => course.id);
        availableCourses.value = availableCourses.value.filter(course => !myCourseIds.includes(course.id));
      }
      
      console.log('可选课程数量:', availableCourses.value.length);
    } else {
      console.error('获取可选课程列表失败:', response.data.message);
      ElMessage.error(response.data.message || '获取可选课程列表失败');
    }
  } catch (error) {
    console.error('获取可选课程列表失败:', error);
    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误数据:', error.response.data);
    }
    ElMessage.error('获取可选课程列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
}

// 搜索课程
const searchCourses = () => {
  queryParams.name = searchKeyword.value
  queryParams.pageNum = 1
  fetchAvailableCourses()
}

// 选择课程
const selectCourse = async (courseId) => {
  try {
    const response = await axios.post(`/api/student/course/select/${courseId}`)
    if (response.data.code === 200) {
      ElMessage.success('选课成功')
      fetchMyCourses()
      fetchAvailableCourses()
    } else {
      ElMessage.error(response.data.message || '选课失败')
    }
  } catch (error) {
    console.error('选课失败:', error)
    ElMessage.error('选课失败')
  }
}

// 退选课程
const dropCourse = async (courseId) => {
  ElMessageBox.confirm('确定要退选该课程吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.post(`/api/student/course/drop/${courseId}`)
      if (response.data.code === 200) {
        ElMessage.success('退选成功')
        fetchMyCourses()
        fetchAvailableCourses()
      } else {
        ElMessage.error(response.data.message || '退选失败')
      }
    } catch (error) {
      console.error('退选失败:', error)
      ElMessage.error('退选失败')
    }
  }).catch(() => {})
}

// 查看课程资料
const viewMaterials = (courseId) => {
  router.push({
    name: 'StudentMaterials',
    query: { courseId }
  })
}

// 查看课程反馈
const viewFeedback = (courseId) => {
  router.push({
    name: 'StudentFeedback',
    query: { courseId }
  })
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchAvailableCourses()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchAvailableCourses()
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

// 页面加载时获取数据
onMounted(() => {
  fetchMyCourses()
  fetchAvailableCourses()
})
</script>

<style scoped>
.courses-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-list-card {
  margin-bottom: 20px;
}

.course-card {
  margin-bottom: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-name {
  font-weight: bold;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-content {
  flex: 1;
  margin-bottom: 15px;
}

.course-description {
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-teacher, .course-time, .course-location, .course-end-time, .course-end-form, .course-end-location {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.course-teacher .el-icon, .course-time .el-icon, .course-location .el-icon, 
.course-end-time .el-icon, .course-end-form .el-icon, .course-end-location .el-icon {
  margin-right: 5px;
}

.course-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.empty-block {
  padding: 30px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 