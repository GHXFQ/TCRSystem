<template>
  <div class="resource-sharing-container">
    <h1>课程资源协同</h1>
    
    <!-- 查询过滤卡片 -->
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>查询与下载</span>
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
        
        <el-form-item label="学年">
          <el-select 
            v-model="queryParams.academicYear" 
            placeholder="选择学年" 
            clearable 
            style="width: 150px;"
          >
            <el-option
              v-for="year in academicYears"
              :key="year"
              :label="year"
              :value="year"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="学期">
          <el-select 
            v-model="queryParams.semester" 
            placeholder="选择学期" 
            clearable 
            style="width: 150px;"
          >
            <el-option label="第一学期" :value="1"></el-option>
            <el-option label="第二学期" :value="2"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="材料类型">
          <el-select 
            v-model="queryParams.materialTypeId" 
            placeholder="选择材料类型" 
            clearable 
            style="width: 150px;"
          >
            <el-option
              v-for="type in materialTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="fetchMaterials">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 同步教学大纲和授课计划按钮 -->
    <el-card class="sync-card" v-if="queryParams.courseId">
      <div class="sync-actions">
        <el-button 
          type="primary" 
          icon="Download" 
          @click="syncMaterials('教学大纲')"
          :disabled="!hasCourseMaterials"
        >
          同步本课程教学大纲
        </el-button>
        <el-button 
          type="success" 
          icon="Download" 
          @click="syncMaterials('授课计划')"
          :disabled="!hasCourseMaterials"
        >
          同步本课程授课计划
        </el-button>
      </div>
      <div class="sync-tip" v-if="queryParams.courseId && !hasCourseMaterials">
        <el-alert
          title="当前课程暂无其他教师上传的教学大纲或授课计划"
          type="info"
          show-icon
          :closable="false"
        />
      </div>
    </el-card>
    
    <!-- 材料列表 -->
    <el-card v-loading="loading" class="materials-card">
      <template #header>
        <div class="card-header">
          <span>
            {{ queryParams.courseId ? courseMap[queryParams.courseId]?.name || '所有课程' : '所有课程' }} 教学资源
          </span>
          <el-tag v-if="teacherMaterialsOnly" type="success">仅显示同课程教师的材料</el-tag>
        </div>
      </template>
      
      <div class="materials-tip" v-if="!materialList.length && !loading">
        <el-empty description="暂无符合条件的教学资源"></el-empty>
      </div>
      
      <el-table 
        v-else 
        :data="materialList" 
        style="width: 100%"
        border
      >
        <el-table-column prop="name" label="材料名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="course_name" label="所属课程" width="150" show-overflow-tooltip />
        <el-table-column prop="material_type_name" label="材料类型" width="120">
          <template #default="scope">
            {{ scope.row.material_type_name || '未知类型' }}
          </template>
        </el-table-column>
        <el-table-column prop="academic_year" label="学年" width="100">
          <template #default="scope">
            {{ scope.row.academic_year || '未知学年' }}
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="80">
          <template #default="scope">
            {{ scope.row.semester === 1 ? '第一学期' : scope.row.semester === 2 ? '第二学期' : '未知学期' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="upload_user_name" label="上传教师" width="120" />
        <el-table-column prop="create_time" label="上传时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              icon="Download" 
              circle 
              plain
              @click="downloadMaterial(scope.row)"
              title="下载"
            />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 状态变量
const loading = ref(false)
const materialList = ref([])
const courseList = ref([])
const materialTypes = ref([])
const academicYears = ref([])
const total = ref(0)
const courseMap = ref({})
const currentTeacherId = ref(null)
const currentTeacherCourses = ref([])

// 查询参数
const queryParams = reactive({
  current: 1,
  pageSize: 10,
  courseId: null,
  academicYear: null,
  semester: null,
  materialTypeId: null
})

// 计算当前课程是否有材料
const hasCourseMaterials = computed(() => {
  if (!queryParams.courseId) return false
  return materialList.value.some(material => 
    material.course_id === queryParams.courseId && 
    material.upload_user_id !== currentTeacherId.value
  )
})

// 是否只显示同课程教师的材料
const teacherMaterialsOnly = computed(() => !!queryParams.courseId)

// 获取当前教师可访问的课程
const fetchTeacherCourses = async () => {
  try {
    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    currentTeacherId.value = userInfo.id
    
    if (!currentTeacherId.value) {
      ElMessage.warning('无法获取用户信息，请重新登录')
      return
    }
    
    const response = await axios.get(`/api/course/teacher/${currentTeacherId.value}`)
    courseList.value = response.data.data || []
    
    // 创建课程映射，方便查询
    courseList.value.forEach(course => {
      courseMap.value[course.id] = course
      
      // 记录当前教师的课程ID，用于安全管理
      currentTeacherCourses.value.push(course.id)
    })
  } catch (error) {
    console.error('获取教师课程失败:', error)
    ElMessage.error('获取教师课程失败')
  }
}

// 获取材料类型列表
const fetchMaterialTypes = async () => {
  try {
    const response = await axios.get('/api/material-type/list')
    materialTypes.value = response.data.data || []
  } catch (error) {
    console.error('获取材料类型失败:', error)
    ElMessage.error('获取材料类型失败')
  }
}

// 生成学年列表
const generateAcademicYears = () => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = 0; i < 5; i++) {
    const year = currentYear - i
    years.push(`${year}-${year + 1}`)
  }
  academicYears.value = years
}

// 查询教学资源
const fetchMaterials = async () => {
  loading.value = true;
  try {
    // 调用后端API获取教学资源
    const params = { ...queryParams }
    
    // 安全管理：只能查看同一课程的教学资源
    if (!params.courseId && currentTeacherCourses.value.length > 0) {
      // 转换为字符串，便于后端解析
      params.courseIdsStr = currentTeacherCourses.value.join(',');
    }
    
    // 确保semester是数字类型
    if (params.semester !== null && params.semester !== undefined) {
      params.semester = Number(params.semester);
    }
    
    // 确保materialTypeId是数字类型
    if (params.materialTypeId !== null && params.materialTypeId !== undefined) {
      params.materialTypeId = Number(params.materialTypeId);
    }
    
    console.log('查询参数：', params)
    
    const response = await axios.get('/api/material/shared', { params })
    if (response.data.code === 200) {
      // 处理并格式化返回的数据
      const records = response.data.data.records || [];
      materialList.value = records.map(record => {
        // 确保所有必要字段都存在
        return {
          ...record,
          // 如果缺少某些字段，使用默认值
          material_type_name: record.material_type_name || '未知类型',
          course_name: record.course_name || '未知课程',
          academic_year: record.academic_year || '未知学年',
          // 确保学期为数字类型
          semester: record.semester ? Number(record.semester) : null
        };
      });
      
      total.value = response.data.data.total || 0;
      
      // 打印查询结果，检查字段
      console.log('获取到的材料列表:', materialList.value);
      if (materialList.value.length > 0) {
        console.log('第一条材料数据样例:', materialList.value[0]);
        console.log('材料类型名称:', materialList.value[0].material_type_name);
        console.log('学年:', materialList.value[0].academic_year);
        console.log('学期:', materialList.value[0].semester, '类型:', typeof materialList.value[0].semester);
      }
      
      // 检查是否有课程材料用于同步
      if (queryParams.courseId) {
        console.log('检查材料：', materialList.value)
      }
    } else {
      ElMessage.error(response.data.message || '获取教学资源失败')
    }
  } catch (error) {
    console.error('获取教学资源失败:', error)
    ElMessage.error('获取教学资源失败：' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.courseId = null
  queryParams.academicYear = null
  queryParams.semester = null
  queryParams.materialTypeId = null
  queryParams.current = 1
  fetchMaterials()
}

// 处理课程变化
const handleCourseChange = () => {
  queryParams.current = 1
  fetchMaterials()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  queryParams.current = current
  fetchMaterials()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.current = 1
  fetchMaterials()
}

// 下载教学资源
const downloadMaterial = (material) => {
  if (!material.id) {
    ElMessage.warning('材料ID为空')
    return
  }
  
  try {
    // 创建下载链接 - 使用完整的API路径
    const downloadUrl = `/api/material/download/${material.id}`
    
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
    
    // 记录下载次数
    axios.post(`/api/material/download/${material.id}`)
      .catch(error => console.error('更新下载次数失败:', error))
    
    ElMessage.success('开始下载文件')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// 同步教学大纲或授课计划
const syncMaterials = async (materialTypeName) => {
  if (!queryParams.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }
  
  try {
    loading.value = true
    
    // 查找对应的材料类型ID
    const materialTypeId = materialTypes.value.find(type => type.name === materialTypeName)?.id
    if (!materialTypeId) {
      ElMessage.warning(`未找到${materialTypeName}材料类型`)
      return
    }
    
    // 查询同一课程其他教师上传的教学大纲或授课计划
    const params = {
      courseId: queryParams.courseId,
      materialTypeId,
      excludeUserId: currentTeacherId.value
    }
    
    const response = await axios.get('/api/material/course-shared', { params })
    const materials = response.data.data || []
    
    if (!materials.length) {
      ElMessage.warning(`当前课程没有其他教师上传的${materialTypeName}`)
      return
    }
    
    // 显示确认对话框
    await ElMessageBox.confirm(
      `确定要同步本课程的${materialTypeName}吗？共${materials.length}份资料将被下载。`,
      '确认同步',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // 批量下载确认
    ElMessage.success(`正在同步${materials.length}份${materialTypeName}资料，请在弹出的窗口中查看`)
    
    // 延迟一点时间，避免浏览器阻止批量下载
    for (let i = 0; i < materials.length; i++) {
      setTimeout(() => {
        // 使用下载API
        const downloadUrl = `/api/material/download/${materials[i].id}`
        window.open(downloadUrl, '_blank')
        
        // 记录下载
        axios.post(`/api/material/download/${materials[i].id}`)
          .catch(error => console.error('更新下载次数失败:', error))
      }, i * 1000) // 每次下载间隔1秒
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`同步${materialTypeName}失败:`, error)
      ElMessage.error(`同步${materialTypeName}失败`)
    }
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchTeacherCourses()
  fetchMaterialTypes()
  generateAcademicYears()
  fetchMaterials()
})
</script>

<style lang="scss" scoped>
.resource-sharing-container {
  padding: 20px;
  
  h1 {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
    color: #303133;
  }
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .sync-card {
    margin-bottom: 20px;
    
    .sync-actions {
      display: flex;
      gap: 15px;
      margin-bottom: 15px;
    }
    
    .sync-tip {
      margin-top: 10px;
    }
  }
  
  .materials-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .materials-tip {
      padding: 30px 0;
    }
    
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
}
</style> 