<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="materials-container">
    <h1>课程资料</h1>
    
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="课程">
          <el-select 
            v-model="queryParams.courseId" 
            placeholder="选择课程" 
            clearable
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
        <el-form-item label="资料类型">
          <el-select 
            v-model="queryParams.type" 
            placeholder="选择类型" 
            clearable
            style="width: 150px;"
          >
            <el-option :label="'课程资料'" :value="0" />
            <el-option :label="'学生资料'" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchMaterialList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 显示当前筛选条件 -->
      <div class="filter-info" v-if="selectedCourseName || selectedTypeName">
        <span>当前筛选: </span>
        <el-tag v-if="selectedCourseName" type="info" class="filter-tag">
          课程: {{ selectedCourseName }}
        </el-tag>
        <el-tag v-if="selectedTypeName" type="info" class="filter-tag">
          类型: {{ selectedTypeName }}
        </el-tag>
      </div>
    </el-card>
    
    <el-card class="material-list-card">
      <template #header>
        <div class="card-header">
          <span>资料列表</span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="materialList"
        style="width: 100%"
        border
      >
        <el-table-column prop="name" label="资料名称" min-width="180" />
        <el-table-column prop="description" label="资料描述" min-width="200" />
        <el-table-column label="所属课程" min-width="120">
          <template #default="scope">
            {{ getCourseName(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="文件类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getFileTypeTag(scope.row.fileType)">
              {{ getFileTypeLabel(scope.row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" min-width="100">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" min-width="100" />
        <el-table-column label="上传者" min-width="100">
          <template #default="scope">
            {{ getUploaderName(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleDownload(scope.row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="val => currentPage = val"
          @update:page-size="val => pageSize = val"
        />
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
const materialList = ref([])
const courseList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  courseId: undefined,
  type: undefined
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

// 计算属性：当前选中的课程名称
const selectedCourseName = computed(() => {
  if (!queryParams.courseId) return ''
  const course = courseList.value.find(c => c.id === queryParams.courseId)
  return course ? course.name : ''
})

// 计算属性：当前选中的资料类型名称
const selectedTypeName = computed(() => {
  if (queryParams.type === undefined) return ''
  return queryParams.type === 0 ? '课程资料' : '学生资料'
})

// 获取课程列表
const fetchCourseList = async () => {
  try {
    const response = await axios.get('/api/student/courses')
    courseList.value = response.data.data || []
    
    // 如果有课程，默认选择第一个
    if (courseList.value.length > 0 && !queryParams.courseId) {
      queryParams.courseId = courseList.value[0].id
      await fetchMaterialList()
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 获取资料列表
const fetchMaterialList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/material/list', {
      params: queryParams
    })
    materialList.value = response.data.data.records || []
    
    // 检查返回的数据结构，以便调试
    if (materialList.value.length > 0) {
      const firstItem = materialList.value[0]
      console.log('资料列表第一条数据:', firstItem)
      console.log('资料列表第一条数据的所有字段:', Object.keys(firstItem))
      
      // 检查所有可能的上传者名称字段
      const possibleFields = ['uploadUserName', 'upload_user_name', 'uploaderName', 'uploader_name', 'userName', 'user_name']
      possibleFields.forEach(field => {
        console.log(`字段 ${field} 的值:`, firstItem[field])
      })
    }
    
    // 处理课程名称和上传者名称
    materialList.value = materialList.value.map(material => {
      // 添加课程名称处理
      if (material.courseId && !material.courseName && !material.course_name) {
        const course = courseList.value.find(c => c.id === material.courseId)
        if (course) {
          material.courseName = course.name
        }
      }
      
      // 检查并记录上传者字段情况（调试用）
      if (!material.uploadUserName || material.uploadUserName.trim() === '') {
        console.log('资料缺少上传者信息:', material.id, material.name, '所有字段:', Object.keys(material))
      }
      
      return material
    })
    
    total.value = response.data.data.total || 0
  } catch (error) {
    console.error('获取资料列表失败:', error)
    ElMessage.error('获取资料列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.courseId = undefined
  queryParams.type = undefined
  fetchMaterialList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchMaterialList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchMaterialList()
}

// 处理下载
const handleDownload = (row) => {
  ElMessageBox.confirm('确认下载该资料?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 这里应该调用API下载资料
    window.open(`/api/material/download/${row.id}`, '_blank')
  }).catch(() => {})
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024
    index++
  }
  return size.toFixed(2) + ' ' + units[index]
}

// 获取文件类型标签样式
const getFileTypeTag = (fileType) => {
  if (!fileType) return ''
  if (fileType.includes('image')) return 'success'
  if (fileType.includes('pdf')) return 'danger'
  if (fileType.includes('word') || fileType.includes('document')) return 'primary'
  if (fileType.includes('excel') || fileType.includes('sheet')) return ''
  if (fileType.includes('zip') || fileType.includes('rar')) return 'warning'
  return 'info'
}

// 获取文件类型显示标签
const getFileTypeLabel = (fileType) => {
  if (!fileType) return '未知'
  if (fileType.includes('image')) return '图片'
  if (fileType.includes('pdf')) return 'PDF'
  if (fileType.includes('word') || fileType.includes('document')) return 'Word'
  if (fileType.includes('excel') || fileType.includes('sheet')) return 'Excel'
  if (fileType.includes('zip') || fileType.includes('rar')) return '压缩包'
  return fileType.split('/')[1] || fileType
}

// 获取课程名称（处理不同的属性名和空值情况）
const getCourseName = (row) => {
  // 按优先级检查不同可能的属性名
  if (row.courseName) return row.courseName
  if (row.course_name) return row.course_name
  
  // 如果有courseId但没有名称，尝试从courseList查找
  if (row.courseId || row.course_id) {
    const courseId = row.courseId || row.course_id
    const course = courseList.value.find(c => c.id === courseId)
    return course ? course.name : '未知课程'
  }
  
  return '未知课程'
}

// 获取上传者名称（处理不同的属性名和空值情况）
const getUploaderName = (row) => {
  // 按优先级检查不同可能的属性名
  if (row.uploadUserName && row.uploadUserName.trim() !== '') return row.uploadUserName
  if (row.upload_user_name && row.upload_user_name.trim() !== '') return row.upload_user_name
  if (row.uploaderName && row.uploaderName.trim() !== '') return row.uploaderName
  if (row.uploader_name && row.uploader_name.trim() !== '') return row.uploader_name
  if (row.userName && row.userName.trim() !== '') return row.userName
  if (row.user_name && row.user_name.trim() !== '') return row.user_name
  
  // 如果找不到任何名称，返回未知用户
  console.log('找不到上传者名称字段，资料ID:', row.id, '可用字段:', Object.keys(row))
  return '未知用户'
}

// 页面加载时获取数据
onMounted(async () => {
  // 先获取课程列表，再获取材料列表
  await fetchCourseList()
  // fetchMaterialList会在fetchCourseList中自动调用
})
</script>

<style lang="scss" scoped>
.materials-container {
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
  
  .material-list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 