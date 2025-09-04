<template>
  <div class="course-management">
    <h1>课程管理</h1>
    
    <el-card class="filter-card">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="课程名称">
            <el-select 
              v-model="queryParams.name" 
              placeholder="选择课程" 
              clearable 
              filterable
              style="width: 220px;"
            >
              <el-option
                v-for="course in allCourseOptions"
                :key="course.name"
                :label="course.name"
                :value="course.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width: 150px;">
              <el-option label="未开始" :value="0" />
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchCourses">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card class="course-list-card">
      <el-table :data="courseList" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="课程名称" width="180" />
        <el-table-column prop="description" label="课程描述" show-overflow-tooltip />
        <el-table-column prop="classTime" label="上课时间" width="150" />
        <el-table-column prop="classLocation" label="上课地点" width="150" />
        <el-table-column label="结课时间" width="170">
          <template #default="scope">
            {{ formatEndTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结课形式" width="100">
          <template #default="scope">
            {{ getEndFormLabel(scope.row.endForm) }}
          </template>
        </el-table-column>
        <el-table-column prop="endLocation" label="结课地点" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          :current-page="queryParams.pageNum"
          :page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑课程对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加课程' : '编辑课程'"
      width="600px"
      append-to-body
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入课程描述"
          />
        </el-form-item>
        <el-form-item label="上课时间" prop="classTime">
          <el-input v-model="courseForm.classTime" placeholder="例如：周一 8:00-9:40" />
        </el-form-item>
        <el-form-item label="上课地点" prop="classLocation">
          <el-input v-model="courseForm.classLocation" placeholder="例如：教学楼A-101" />
        </el-form-item>
        <el-form-item label="结课时间" prop="endTime">
          <el-date-picker
            v-model="courseForm.endTime"
            type="datetime"
            placeholder="选择结课时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结课形式" prop="endForm">
          <el-radio-group v-model="courseForm.endForm">
            <el-radio :label="0">考试</el-radio>
            <el-radio :label="1">大作业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="结课地点" prop="endLocation" v-if="courseForm.endForm === 0">
          <el-input v-model="courseForm.endLocation" placeholder="例如：教学楼C-201" />
        </el-form-item>
        <el-form-item label="课程状态" prop="status">
          <el-select v-model="courseForm.status" placeholder="请选择课程状态">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import dayjs from 'dayjs'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: ''
})

// 课程列表
const courseList = ref([])
const allCourseOptions = ref([]) // 用于课程名称筛选的选项列表
const total = ref(0)
const loading = ref(false)

// 对话框相关变量
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const courseFormRef = ref(null)
const courseForm = reactive({
  id: null,
  name: '',
  description: '',
  classTime: '',
  classLocation: '',
  endTime: '',
  endForm: 0,
  endLocation: '',
  status: 1
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }],
  classTime: [{ required: true, message: '请输入上课时间', trigger: 'blur' }],
  classLocation: [{ required: true, message: '请输入上课地点', trigger: 'blur' }],
  endTime: [{ required: true, message: '请选择结课时间', trigger: 'change' }],
  endForm: [{ required: true, message: '请选择结课形式', trigger: 'change' }],
  status: [{ required: true, message: '请选择课程状态', trigger: 'change' }]
}

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    // 获取当前教师ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const teacherId = userInfo.id
    
    if (!teacherId) {
      ElMessage.warning('获取用户信息失败，请重新登录')
      return
    }
    
    // 创建查询参数对象
    const params = { 
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      name: queryParams.name,
      status: queryParams.status !== '' ? Number(queryParams.status) : null
    }
    
    console.log('查询参数:', params)
    
    // 使用新的分页API
    const response = await axios.get(`/api/course/teacher/${teacherId}/page`, {
      params: params
    })
    
    if (response.data.code === 200) {
      // 分页数据结构处理
      const pageData = response.data.data
      courseList.value = pageData.records || []
      total.value = pageData.total || 0
      
      // 如果是第一次加载或全部课程列表为空，获取所有课程名称用于筛选
      if (allCourseOptions.value.length === 0) {
        fetchAllCourseOptions(teacherId)
      }
    } else {
      ElMessage.error(response.data.message || '获取课程列表失败')
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 获取所有课程选项用于筛选
const fetchAllCourseOptions = async (teacherId) => {
  try {
    // 使用原始API获取所有教师课程，不带筛选条件
    const response = await axios.get(`/api/course/teacher/${teacherId}`)
    
    if (response.data.code === 200) {
      const courses = response.data.data || []
      // 提取所有不重复的课程名称
      allCourseOptions.value = Array.from(
        new Set(courses.map(course => course.name))
      ).map(name => ({ name }))
      
      console.log('课程筛选选项:', allCourseOptions.value)
    }
  } catch (error) {
    console.error('获取课程选项失败:', error)
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.name = ''
  queryParams.status = ''
  fetchCourses()
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchCourses()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchCourses()
}

// 编辑课程
const handleEdit = (row) => {
  resetCourseForm()
  dialogType.value = 'edit'
  Object.assign(courseForm, row)
  dialogVisible.value = true
}

// 删除课程
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该课程吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`/api/course/${row.id}`)
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        fetchCourses()
      } else {
        ElMessage.error(response.data.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 重置课程表单
const resetCourseForm = () => {
  courseForm.id = null
  courseForm.name = ''
  courseForm.description = ''
  courseForm.classTime = ''
  courseForm.classLocation = ''
  courseForm.endTime = ''
  courseForm.endForm = 0
  courseForm.endLocation = ''
  courseForm.status = 1
  
  if (courseFormRef.value) {
    courseFormRef.value.resetFields()
  }
}

// 提交表单
const submitForm = () => {
  courseFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const url = dialogType.value === 'add' 
        ? '/api/course' 
        : '/api/course'
      
      const method = dialogType.value === 'add' ? 'post' : 'put'
      
      const response = await axios[method](url, courseForm)
      
      if (response.data.code === 200) {
        ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
        dialogVisible.value = false
        fetchCourses()
      } else {
        ElMessage.error(response.data.message || (dialogType.value === 'add' ? '添加失败' : '修改失败'))
      }
    } catch (error) {
      console.error(dialogType.value === 'add' ? '添加失败:' : '修改失败:', error)
      ElMessage.error(dialogType.value === 'add' ? '添加失败' : '修改失败')
    }
  })
}

// 格式化结课时间
const formatEndTime = (endTime) => {
  if (!endTime) return '未设置'
  return dayjs(endTime).format('YYYY-MM-DD HH:mm')
}

// 获取结课形式标签
const getEndFormLabel = (endForm) => {
  switch (endForm) {
    case 0: return '考试'
    case 1: return '大作业'
    default: return '未设置'
  }
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
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.course-management {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.course-list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 