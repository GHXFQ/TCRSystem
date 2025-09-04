<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="course-management">
    <h1>课程管理</h1>
    
    <el-card class="filter-card">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="课程名称">
            <el-input v-model="queryParams.name" placeholder="请输入课程名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="教师">
            <el-select 
              v-model="queryParams.teacherId" 
              placeholder="请选择教师" 
              clearable
              v-bind="safeElementConfig"
            >
              <el-option
                v-for="teacher in teacherList"
                :key="teacher.id"
                :label="teacher.realName"
                :value="teacher.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select 
              v-model="queryParams.status" 
              placeholder="请选择状态" 
              clearable
              v-bind="safeElementConfig"
            >
              <el-option label="未开始" :value="0"></el-option>
              <el-option label="进行中" :value="1"></el-option>
              <el-option label="已结束" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchCourses">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="operation-container">
        <el-button type="primary" @click="handleAdd">添加课程</el-button>
      </div>
    </el-card>
    
    <el-card class="table-card">
      <el-table :data="courseList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="课程名称" width="200"></el-table-column>
        <el-table-column prop="teacherName" label="任课教师" width="150"></el-table-column>
        <el-table-column prop="classTime" label="上课时间" width="150"></el-table-column>
        <el-table-column prop="classLocation" label="上课地点" width="150"></el-table-column>
        <el-table-column label="结课方式" width="120">
          <template #default="scope">
            {{ scope.row.endForm === 0 ? '考试' : '大作业' }}
          </template>
        </el-table-column>
        <el-table-column label="结课时间" width="180">
          <template #default="scope">
            {{ scope.row.endTime ? formatDate(scope.row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endLocation" label="考试地点" width="150"></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 课程编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="650px"
      v-bind="safeElementConfig"
    >
      <el-form :model="courseForm" :rules="courseRules" ref="courseFormRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称"></el-input>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input type="textarea" v-model="courseForm.description" placeholder="请输入课程描述" rows="3"></el-input>
        </el-form-item>
        <el-form-item label="任课教师" prop="teacherId">
          <el-select 
            v-model="courseForm.teacherId" 
            placeholder="请选择任课教师" 
            style="width: 100%"
            v-bind="safeElementConfig"
          >
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.id"
              :label="teacher.realName"
              :value="teacher.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上课时间" prop="classTime">
          <el-input v-model="courseForm.classTime" placeholder="例如：周一 8:00-9:40"></el-input>
        </el-form-item>
        <el-form-item label="上课地点" prop="classLocation">
          <el-input v-model="courseForm.classLocation" placeholder="例如：教学楼A-101"></el-input>
        </el-form-item>
        <el-form-item label="结课形式" prop="endForm">
          <el-radio-group v-model="courseForm.endForm">
            <el-radio :label="0">考试</el-radio>
            <el-radio :label="1">大作业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="结课时间" prop="endTime">
          <el-date-picker
            v-model="courseForm.endTime"
            type="datetime"
            placeholder="选择结课时间"
            style="width: 100%"
            v-bind="safeElementConfig"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="考试地点" prop="endLocation" v-if="courseForm.endForm === 0">
          <el-input v-model="courseForm.endLocation" placeholder="例如：教学楼C-203"></el-input>
        </el-form-item>
        <el-form-item label="课程状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio :label="0">未开始</el-radio>
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCourseForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 课程详情对话框 -->
    <el-dialog 
      title="课程详情" 
      v-model="detailDialogVisible" 
      width="800px"
      v-bind="safeElementConfig"
    >
      <template v-if="courseDetail">
        <div class="detail-header">
          <h2>{{ courseDetail.course.name }}</h2>
          <el-tag :type="getStatusType(courseDetail.course.status)">{{ getStatusText(courseDetail.course.status) }}</el-tag>
        </div>
        
        <div class="detail-info">
          <div class="info-item">
            <label>课程ID：</label>
            <span>{{ courseDetail.course.id }}</span>
          </div>
          <div class="info-item">
            <label>课程描述：</label>
            <span>{{ courseDetail.course.description || '无' }}</span>
          </div>
          <div class="info-item">
            <label>任课教师：</label>
            <span>{{ courseDetail.teacher ? courseDetail.teacher.realName : '未知' }}</span>
          </div>
          <div class="info-item">
            <label>上课时间：</label>
            <span>{{ courseDetail.course.classTime || '未设置' }}</span>
          </div>
          <div class="info-item">
            <label>上课地点：</label>
            <span>{{ courseDetail.course.classLocation || '未设置' }}</span>
          </div>
          <div class="info-item">
            <label>结课形式：</label>
            <span>{{ courseDetail.course.endForm === 0 ? '考试' : '大作业' }}</span>
          </div>
          <div class="info-item">
            <label>结课时间：</label>
            <span>{{ courseDetail.course.endTime ? formatDate(courseDetail.course.endTime) : '未设置' }}</span>
          </div>
          <div class="info-item" v-if="courseDetail.course.endForm === 0">
            <label>考试地点：</label>
            <span>{{ courseDetail.course.endLocation || '未设置' }}</span>
          </div>
        </div>
        
        <el-divider content-position="left">选课学生</el-divider>
        
        <div class="student-list">
          <el-table :data="courseDetail.students" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="username" label="学号" width="150"></el-table-column>
            <el-table-column prop="realName" label="姓名" width="150"></el-table-column>
            <el-table-column prop="email" label="邮箱"></el-table-column>
            <el-table-column prop="phone" label="手机号"></el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="danger" size="small" @click="handleRemoveStudent(scope.row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { safeElementConfig } from '@/utils/resizeObserverFix'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  teacherId: null,
  status: null
})

// 课程列表
const courseList = ref([])
// 教师列表
const teacherList = ref([])
// 总数据条数
const total = ref(0)
// 加载状态
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogType = ref('add') // add or edit
const courseFormRef = ref(null)
const courseForm = reactive({
  id: undefined,
  name: '',
  description: '',
  teacherId: null,
  classTime: '',
  classLocation: '',
  endTime: null,
  endForm: 0,
  endLocation: '',
  status: 1
})

// 课程表单验证规则
const courseRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择任课教师', trigger: 'change' }
  ],
  classTime: [
    { required: true, message: '请输入上课时间', trigger: 'blur' }
  ],
  classLocation: [
    { required: true, message: '请输入上课地点', trigger: 'blur' }
  ],
  endTime: [
    { required: true, message: '请选择结课时间', trigger: 'change' }
  ],
  endLocation: [
    { required: true, message: '请输入考试地点', trigger: 'blur' }
  ]
}

// 课程详情对话框
const detailDialogVisible = ref(false)
const courseDetail = ref(null)

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/course-management/list', {
      params: queryParams
    })
    if (response.data.code === 200) {
      courseList.value = response.data.data.records
      total.value = response.data.data.total
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const response = await axios.get('/api/admin/course-management/available-teachers')
    if (response.data.code === 200) {
      teacherList.value = response.data.data
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.name = ''
  queryParams.teacherId = null
  queryParams.status = null
  fetchCourses()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchCourses()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchCourses()
}

// 处理添加课程
const handleAdd = () => {
  dialogType.value = 'add'
  dialogTitle.value = '添加课程'
  courseForm.id = undefined
  courseForm.name = ''
  courseForm.description = ''
  courseForm.teacherId = null
  courseForm.classTime = ''
  courseForm.classLocation = ''
  courseForm.endTime = null
  courseForm.endForm = 0
  courseForm.endLocation = ''
  courseForm.status = 1
  
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    dialogVisible.value = true
    // 等待DOM更新后再重置表单
    setTimeout(() => {
      courseFormRef.value && courseFormRef.value.resetFields()
    }, 0)
  })
}

// 处理编辑课程
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑课程'
  courseForm.id = row.id
  courseForm.name = row.name
  courseForm.description = row.description
  courseForm.teacherId = row.teacherId
  courseForm.classTime = row.classTime
  courseForm.classLocation = row.classLocation
  courseForm.endTime = row.endTime ? new Date(row.endTime) : null
  courseForm.endForm = row.endForm
  courseForm.endLocation = row.endLocation
  courseForm.status = row.status
  
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    dialogVisible.value = true
  })
}

// 处理查看详情
const handleDetail = async (row) => {
  try {
    loading.value = true
    const response = await axios.get(`/api/admin/course-management/${row.id}/detail`)
    if (response.data.code === 200) {
      // 先清空旧数据，避免显示上一次的内容
      courseDetail.value = null
      
      // 使用nextTick确保DOM已更新
      await nextTick()
      
      // 设置新数据
      courseDetail.value = response.data.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取课程详情失败:', error)
    ElMessage.error('获取课程详情失败')
  } finally {
    loading.value = false
  }
}

// 处理删除课程
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除课程"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`/api/admin/course-management/${row.id}`)
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        fetchCourses()
      } else {
        ElMessage.error(response.data.message || '删除失败')
      }
    } catch (error) {
      console.error('删除课程失败:', error)
      ElMessage.error('删除课程失败')
    }
  }).catch(() => {})
}

// 提交课程表单
const submitCourseForm = () => {
  courseFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (dialogType.value === 'add') {
          response = await axios.post('/api/admin/course-management', courseForm)
        } else {
          response = await axios.put('/api/admin/course-management', courseForm)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
          dialogVisible.value = false
          fetchCourses()
        } else {
          ElMessage.error(response.data.message || (dialogType.value === 'add' ? '添加失败' : '修改失败'))
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加课程失败:' : '修改课程失败:', error)
        ElMessage.error(dialogType.value === 'add' ? '添加课程失败' : '修改课程失败')
      }
    }
  })
}

// 处理移除学生
const handleRemoveStudent = (student) => {
  if (!courseDetail.value || !courseDetail.value.course) return
  
  ElMessageBox.confirm(`确定要将学生"${student.realName}"从课程中移除吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`/api/admin/course-management/${courseDetail.value.course.id}/student/${student.id}`)
      if (response.data.code === 200) {
        ElMessage.success('移除成功')
        // 重新加载课程详情
        handleDetail(courseDetail.value.course)
      } else {
        ElMessage.error(response.data.message || '移除失败')
      }
    } catch (error) {
      console.error('移除学生失败:', error)
      ElMessage.error('移除学生失败')
    }
  }).catch(() => {})
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}

onMounted(() => {
  fetchCourses()
  fetchTeachers()
})
</script>

<style lang="scss" scoped>
.course-management {
  h1 {
    margin-bottom: 20px;
  }
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .operation-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .table-card {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .detail-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      margin-right: 10px;
    }
  }
  
  .detail-info {
    margin-bottom: 20px;
    
    .info-item {
      margin-bottom: 10px;
      
      label {
        font-weight: bold;
        margin-right: 5px;
      }
    }
  }
  
  .student-list {
    margin-top: 20px;
  }
}
</style> 