<template>
  <div class="student-management">
    <h1>学生管理</h1>
    
    <el-card class="filter-card">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="学生姓名">
            <el-input v-model="queryParams.realName" placeholder="请输入学生姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model="queryParams.username" placeholder="请输入学号" clearable></el-input>
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="queryParams.classId" placeholder="请选择班级" clearable v-bind="safeElementConfig">
              <el-option
                v-for="item in classList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchStudents">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="operation-container">
        <el-button type="primary" @click="handleAdd">添加学生</el-button>
        <el-button type="success" @click="handleAddClass">添加班级</el-button>
      </div>
    </el-card>
    
    <el-card class="table-card">
      <el-table :data="studentList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="学号" width="120"></el-table-column>
        <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="className" label="班级"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="手机号"></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleManageCourses(scope.row)">管理选课</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
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
    
    <!-- 添加/编辑学生对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" v-bind="safeElementConfig">
      <el-form :model="studentForm" :rules="studentRules" ref="studentFormRef" label-width="100px">
        <el-form-item label="学号" prop="username">
          <el-input v-model="studentForm.username" placeholder="请输入学号"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="studentForm.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="studentForm.password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="修改密码" v-if="dialogType === 'edit'">
          <el-input v-model="studentForm.password" placeholder="不修改请留空" show-password></el-input>
          <span class="tip-text">不修改密码请留空</span>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="studentForm.classId" placeholder="请选择班级" v-bind="safeElementConfig">
            <el-option
              v-for="item in classList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="studentForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="studentForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStudentForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加班级对话框 -->
    <el-dialog title="添加班级" v-model="classDialogVisible" width="500px" v-bind="safeElementConfig">
      <el-form :model="classForm" :rules="classRules" ref="classFormRef" label-width="100px">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="classForm.name" placeholder="请输入班级名称"></el-input>
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="classForm.majorId" placeholder="请选择所属专业" v-bind="safeElementConfig">
            <el-option
              v-for="item in majorList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份" prop="year">
          <el-date-picker
            v-model="classForm.year"
            type="year"
            placeholder="选择年份"
            format="YYYY"
            value-format="YYYY"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="classForm.description"
            type="textarea"
            placeholder="请输入班级描述"
            rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="classDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitClassForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 管理选课对话框 -->
    <el-dialog 
      :title="`管理选课 - ${currentStudent.realName}`" 
      v-model="courseDialogVisible" 
      width="700px" 
      v-bind="safeElementConfig"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <div class="course-management">
        <el-tabs 
          v-model="courseActiveTab" 
          v-bind="safeTabsConfig"
          @tab-change="handleTabChange"
        >
          <el-tab-pane label="已选课程" name="selected">
            <el-table 
              :data="selectedCourses" 
              style="width: 100%" 
              v-loading="courseLoading"
              row-key="id"
            >
              <el-table-column prop="id" label="ID" width="80"></el-table-column>
              <el-table-column prop="name" label="课程名称"></el-table-column>
              <el-table-column prop="teacherName" label="授课教师"></el-table-column>
              <el-table-column prop="classTime" label="上课时间"></el-table-column>
              <el-table-column prop="classLocation" label="上课地点"></el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button type="danger" size="small" @click="handleDropCourse(scope.row)" :loading="dropLoading">退选</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="可选课程" name="available">
            <el-table 
              :data="availableCourses" 
              style="width: 100%" 
              v-loading="courseLoading"
              row-key="id"
            >
              <el-table-column prop="id" label="ID" width="80"></el-table-column>
              <el-table-column prop="name" label="课程名称"></el-table-column>
              <el-table-column prop="teacherName" label="授课教师"></el-table-column>
              <el-table-column prop="classTime" label="上课时间"></el-table-column>
              <el-table-column prop="classLocation" label="上课地点"></el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button type="success" size="small" @click="handleSelectCourse(scope.row)" :loading="selectLoading">选课</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { safeElementConfig, safeTabsConfig } from '@/utils/resizeObserverFix'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  realName: '',
  username: '',
  classId: undefined
})

// 学生列表
const studentList = ref([])
// 班级列表
const classList = ref([])
// 专业列表
const majorList = ref([])
// 总数据条数
const total = ref(0)
// 加载状态
const loading = ref(false)

// 学生对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogType = ref('add') // add or edit
const studentFormRef = ref(null)
const studentForm = reactive({
  id: undefined,
  username: '',
  realName: '',
  password: '',
  classId: undefined,
  email: '',
  phone: '',
  status: 1,
  role: 0 // 学生角色固定为0
})

// 学生表单验证规则
const studentRules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: dialogType === 'add', message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 班级对话框相关
const classDialogVisible = ref(false)
const classFormRef = ref(null)
const classForm = reactive({
  name: '',
  majorId: undefined,
  year: '',
  description: ''
})

// 班级表单验证规则
const classRules = {
  name: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ],
  majorId: [
    { required: true, message: '请选择所属专业', trigger: 'change' }
  ],
  year: [
    { required: true, message: '请选择入学年份', trigger: 'change' }
  ]
}

// 选课对话框相关
const courseDialogVisible = ref(false)
const courseActiveTab = ref('selected')
const courseLoading = ref(false)
const currentStudent = ref({})
const selectedCourses = ref([])
const availableCourses = ref([])
const selectLoading = ref(false)
const dropLoading = ref(false)

// 获取学生列表
const fetchStudents = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/student/list', {
      params: queryParams
    })
    if (response.data.code === 200) {
      studentList.value = response.data.data.records
      total.value = response.data.data.total
      
      // 确保班级名称正确显示
      studentList.value.forEach(student => {
        if (!student.className && student.classId) {
          const classInfo = classList.value.find(c => c.id === student.classId)
          if (classInfo) {
            student.className = classInfo.name
          }
        }
      })
    }
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 获取班级列表
const fetchClasses = async () => {
  try {
    const response = await axios.get('/api/admin/class/list')
    if (response.data.code === 200) {
      classList.value = response.data.data
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  }
}

// 获取专业列表
const fetchMajors = async () => {
  try {
    const response = await axios.get('/api/admin/major/list')
    if (response.data.code === 200) {
      majorList.value = response.data.data
    }
  } catch (error) {
    console.error('获取专业列表失败:', error)
    ElMessage.error('获取专业列表失败')
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.realName = ''
  queryParams.username = ''
  queryParams.classId = undefined
  fetchStudents()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchStudents()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchStudents()
}

// 处理添加学生
const handleAdd = () => {
  dialogType.value = 'add'
  dialogTitle.value = '添加学生'
  studentForm.id = undefined
  studentForm.username = ''
  studentForm.realName = ''
  studentForm.password = ''
  studentForm.classId = undefined
  studentForm.email = ''
  studentForm.phone = ''
  studentForm.status = 1
  studentForm.role = 0
  dialogVisible.value = true
}

// 处理编辑学生
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑学生'
  studentForm.id = row.id
  studentForm.username = row.username
  studentForm.realName = row.realName
  studentForm.password = '' // 清空密码字段
  studentForm.classId = row.classId
  studentForm.email = row.email
  studentForm.phone = row.phone
  studentForm.status = row.status
  studentForm.role = row.role
  dialogVisible.value = true
}

// 处理删除学生
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该学生吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`/api/admin/student/${row.id}`)
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        fetchStudents()
      } else {
        ElMessage.error(response.data.message || '删除失败')
      }
    } catch (error) {
      console.error('删除学生失败:', error)
      ElMessage.error('删除学生失败')
    }
  }).catch(() => {})
}

// 处理状态变化
const handleStatusChange = async (row) => {
  try {
    const response = await axios.put('/api/admin/student/status', {
      id: row.id,
      status: row.status
    })
    if (response.data.code === 200) {
      ElMessage.success(`${row.status === 1 ? '启用' : '禁用'}成功`)
    } else {
      ElMessage.error(response.data.message || '操作失败')
      // 恢复原状态
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('修改状态失败:', error)
    ElMessage.error('修改状态失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 提交学生表单
const submitStudentForm = () => {
  studentFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (dialogType.value === 'add') {
          response = await axios.post('/api/admin/student', studentForm)
        } else {
          response = await axios.put('/api/admin/student', studentForm)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
          dialogVisible.value = false
          fetchStudents()
        } else {
          ElMessage.error(response.data.message || (dialogType.value === 'add' ? '添加失败' : '修改失败'))
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加学生失败:' : '修改学生失败:', error)
        ElMessage.error(dialogType.value === 'add' ? '添加学生失败' : '修改学生失败')
      }
    }
  })
}

// 处理添加班级
const handleAddClass = () => {
  classForm.name = ''
  classForm.majorId = undefined
  classForm.year = ''
  classForm.description = ''
  classDialogVisible.value = true
}

// 提交班级表单
const submitClassForm = () => {
  classFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await axios.post('/api/admin/class', classForm)
        if (response.data.code === 200) {
          ElMessage.success('添加班级成功')
          classDialogVisible.value = false
          fetchClasses()
        } else {
          ElMessage.error(response.data.message || '添加班级失败')
        }
      } catch (error) {
        console.error('添加班级失败:', error)
        ElMessage.error('添加班级失败')
      }
    }
  })
}

// 处理管理选课
const handleManageCourses = async (row) => {
  currentStudent.value = row
  courseDialogVisible.value = true
  courseActiveTab.value = 'selected'
  await fetchStudentCourses(row.id)
}

// 获取学生课程
const fetchStudentCourses = async (studentId) => {
  courseLoading.value = true
  try {
    // 获取已选课程
    const selectedResponse = await axios.get(`/api/student/course/${studentId}`)
    if (selectedResponse.data.code === 200) {
      selectedCourses.value = selectedResponse.data.data || []
    }
    
    // 获取可选课程
    const availableResponse = await axios.get('/api/admin/course/available', {
      params: { studentId }
    })
    if (availableResponse.data.code === 200) {
      availableCourses.value = availableResponse.data.data || []
    }
    
    // 强制下一帧更新UI
    await nextTick()
    window.dispatchEvent(new Event('resize'))
  } catch (error) {
    console.error('获取学生课程失败:', error)
    ElMessage.error('获取学生课程失败')
  } finally {
    courseLoading.value = false
  }
}

// 处理标签页切换
const handleTabChange = () => {
  // 强制刷新表格
  nextTick(() => {
    window.dispatchEvent(new Event('resize'))
  })
}

// 处理选课
const handleSelectCourse = async (course) => {
  selectLoading.value = true
  try {
    const response = await axios.post('/api/admin/student/course/select', {
      studentId: currentStudent.value.id,
      courseId: course.id
    })
    
    if (response.data.code === 200) {
      ElMessage.success('选课成功')
      await fetchStudentCourses(currentStudent.value.id)
    } else {
      ElMessage.error(response.data.message || '选课失败')
      console.error('选课返回错误:', response.data)
    }
  } catch (error) {
    console.error('选课异常:', error)
    ElMessage.error('选课失败，系统异常')
    
    // 尝试重新加载课程数据
    await fetchStudentCourses(currentStudent.value.id)
  } finally {
    selectLoading.value = false
  }
}

// 处理退课
const handleDropCourse = async (course) => {
  dropLoading.value = true
  try {
    const response = await axios.delete(`/api/admin/course-management/${course.id}/student/${currentStudent.value.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('退课成功')
      await fetchStudentCourses(currentStudent.value.id)
    } else {
      ElMessage.error(response.data.message || '退课失败')
      console.error('退课返回错误:', response.data)
    }
  } catch (error) {
    console.error('退课异常:', error)
    ElMessage.error('退课失败，系统异常')
    
    // 尝试重新加载课程数据
    await fetchStudentCourses(currentStudent.value.id)
  } finally {
    dropLoading.value = false
  }
}

onMounted(() => {
  fetchStudents()
  fetchClasses()
  fetchMajors()
})
</script>

<style lang="scss" scoped>
.student-management {
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
    gap: 10px;
  }
  
  .table-card {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .course-management {
    .el-tabs {
      margin-bottom: 20px;
    }
    
    /* 解决标签页切换时的ResizeObserver错误 */
    :deep(.el-tabs__content) {
      min-height: 350px;
      transform: translateZ(0);
      contain: layout style;
    }
    
    /* 为表格应用特殊的CSS修复 */
    :deep(.el-table) {
      overflow: hidden !important;
      transform: translateZ(0);
      min-height: 300px;
      max-height: 450px;
    }
    
    /* 确保表格内容能够滚动 */
    :deep(.el-table__body-wrapper) {
      overflow-y: auto !important;
      min-height: 250px;
    }
  }
  
  .tip-text {
    color: #909399;
    font-size: 12px;
    margin-top: 5px;
    display: block;
  }
}
</style> 