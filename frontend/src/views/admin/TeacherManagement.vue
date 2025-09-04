<template>
  <div class="teacher-management">
    <h1>教师管理</h1>
    
    <el-card class="filter-card">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="教师姓名">
            <el-input v-model="queryParams.realName" placeholder="请输入教师姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="工号">
            <el-input v-model="queryParams.username" placeholder="请输入工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="职称">
            <el-select v-model="queryParams.titleType" placeholder="请选择职称" clearable>
              <el-option label="普通教师" :value="0"></el-option>
              <el-option label="课程负责人" :value="1"></el-option>
              <el-option label="专业负责人" :value="2"></el-option>
              <el-option label="副院长" :value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchTeachers">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="operation-container">
        <el-button type="primary" @click="handleAdd">添加教师</el-button>
      </div>
    </el-card>
    
    <el-card class="table-card">
      <el-table :data="teacherList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="工号" width="120"></el-table-column>
        <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="手机号"></el-table-column>
        <el-table-column label="职称">
          <template #default="scope">
            <el-tag v-if="hasTitle(scope.row.id, 0)" type="info">普通教师</el-tag>
            <el-tag v-if="hasTitle(scope.row.id, 1)" type="success">课程负责人</el-tag>
            <el-tag v-if="hasTitle(scope.row.id, 2)" type="warning">专业负责人</el-tag>
            <el-tag v-if="hasTitle(scope.row.id, 3)" type="danger">副院长</el-tag>
          </template>
        </el-table-column>
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
            <el-button type="success" size="small" @click="handleSetTitle(scope.row)">设置职称</el-button>
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
    
    <!-- 添加/编辑教师对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="teacherForm" :rules="teacherRules" ref="teacherFormRef" label-width="100px">
        <el-form-item label="工号" prop="username">
          <el-input v-model="teacherForm.username" placeholder="请输入工号"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="teacherForm.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="teacherForm.password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="修改密码" v-if="dialogType === 'edit'">
          <el-input v-model="teacherForm.password" placeholder="不修改请留空" show-password></el-input>
          <span class="tip-text">不修改密码请留空</span>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="teacherForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="teacherForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="teacherForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTeacherForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 设置职称对话框 -->
    <el-dialog title="设置教师职称" v-model="titleDialogVisible" width="600px" @closed="handleTitleDialogClosed">
      <el-form :model="titleForm" ref="titleFormRef" label-width="120px">
        <el-form-item label="教师">
          <span>{{ currentTeacher.realName }}</span>
        </el-form-item>
        
        <el-divider content-position="left">职称设置</el-divider>
        
        <el-form-item label="普通教师">
          <el-switch v-model="titleForm.isNormalTeacher"></el-switch>
        </el-form-item>
        
        <template v-if="titleForm.isCourseLeader || !titleForm.hasCourseLeader">
          <el-form-item label="课程负责人">
            <el-switch v-model="titleForm.isCourseLeader" @change="handleCourseLeaderChange"></el-switch>
            <div v-if="titleForm.isCourseLeader" class="mt-10">
              <el-select 
                v-model="titleForm.courseId" 
                placeholder="请选择负责的课程" 
                style="width: 100%" 
                filterable
                :clearable="false"
                @change="handleCourseChange"
              >
                <el-option
                  v-for="course in courseList"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                ></el-option>
              </el-select>
            </div>
          </el-form-item>
        </template>
        
        <template v-if="titleForm.isMajorLeader || !titleForm.hasMajorLeader">
          <el-form-item label="专业负责人">
            <el-switch v-model="titleForm.isMajorLeader" @change="handleMajorLeaderChange"></el-switch>
            <div v-if="titleForm.isMajorLeader" class="mt-10">
              <el-select 
                v-model="titleForm.majorId" 
                placeholder="请选择负责的专业" 
                style="width: 100%"
                filterable
                :clearable="false"
                @change="handleMajorChange"
              >
                <el-option
                  v-for="major in majorList"
                  :key="major.id"
                  :label="major.name"
                  :value="major.id"
                ></el-option>
              </el-select>
            </div>
          </el-form-item>
        </template>
        
        <template v-if="titleForm.isViceDean || !titleForm.hasViceDean">
          <el-form-item label="副院长">
            <el-switch v-model="titleForm.isViceDean" @change="handleViceDeanChange"></el-switch>
            <div v-if="titleForm.isViceDean" class="mt-10">
              <el-select 
                v-model="titleForm.collegeId" 
                placeholder="请选择所属学院" 
                style="width: 100%"
                filterable
                :clearable="false"
                @change="handleCollegeChange"
              >
                <el-option
                  v-for="college in collegeList"
                  :key="college.id"
                  :label="college.name"
                  :value="college.id"
                ></el-option>
              </el-select>
            </div>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="titleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTitleForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  realName: '',
  username: '',
  titleType: undefined
})

// 教师列表
const teacherList = ref([])
// 教师职称列表
const teacherTitles = ref([])
// 总数据条数
const total = ref(0)
// 加载状态
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogType = ref('add') // add or edit
const teacherFormRef = ref(null)
const teacherForm = reactive({
  id: undefined,
  username: '',
  realName: '',
  password: '',
  email: '',
  phone: '',
  status: 1,
  role: 1 // 教师角色固定为1
})

// 教师表单验证规则
const teacherRules = {
  username: [
    { required: true, message: '请输入工号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: dialogType === 'add', message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 职称对话框相关
const titleDialogVisible = ref(false)
const titleFormRef = ref(null)
const currentTeacher = ref({})
const titleForm = reactive({
  teacherId: undefined,
  isNormalTeacher: false,
  isCourseLeader: false,
  isMajorLeader: false,
  isViceDean: false,
  courseId: undefined,
  majorId: undefined,
  collegeId: undefined,
  hasCourseLeader: false,
  hasMajorLeader: false,
  hasViceDean: false
})

// 课程、专业、学院列表
const courseList = ref([])
const majorList = ref([])
const collegeList = ref([])

// 获取教师列表
const fetchTeachers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/teacher/list', {
      params: queryParams
    })
    if (response.data.code === 200) {
      teacherList.value = response.data.data.records
      total.value = response.data.data.total
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

// 获取教师职称列表
const fetchTeacherTitles = async () => {
  try {
    const response = await axios.get('/api/admin/teacher/titles')
    if (response.data.code === 200) {
      teacherTitles.value = response.data.data
    }
  } catch (error) {
    console.error('获取教师职称列表失败:', error)
    ElMessage.error('获取教师职称列表失败')
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    const response = await axios.get('/api/course/list', {
      params: { pageNum: 1, pageSize: 100 }
    })
    if (response.data.code === 200) {
      courseList.value = response.data.data.records
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
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

// 获取学院列表
const fetchColleges = async () => {
  try {
    const response = await axios.get('/api/admin/college/list')
    if (response.data.code === 200) {
      collegeList.value = response.data.data
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.realName = ''
  queryParams.username = ''
  queryParams.titleType = undefined
  fetchTeachers()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchTeachers()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchTeachers()
}

// 处理添加教师
const handleAdd = () => {
  dialogType.value = 'add'
  dialogTitle.value = '添加教师'
  teacherForm.id = undefined
  teacherForm.username = ''
  teacherForm.realName = ''
  teacherForm.password = ''
  teacherForm.email = ''
  teacherForm.phone = ''
  teacherForm.status = 1
  teacherForm.role = 1
  dialogVisible.value = true
}

// 处理编辑教师
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑教师'
  teacherForm.id = row.id
  teacherForm.username = row.username
  teacherForm.realName = row.realName
  teacherForm.password = '' // 清空密码字段
  teacherForm.email = row.email
  teacherForm.phone = row.phone
  teacherForm.status = row.status
  teacherForm.role = row.role
  dialogVisible.value = true
}

// 处理删除教师
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该教师吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`/api/admin/teacher/${row.id}`)
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        fetchTeachers()
      } else {
        ElMessage.error(response.data.message || '删除失败')
      }
    } catch (error) {
      console.error('删除教师失败:', error)
      ElMessage.error('删除教师失败')
    }
  }).catch(() => {})
}

// 处理状态变化
const handleStatusChange = async (row) => {
  try {
    const response = await axios.put('/api/admin/teacher/status', {
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

// 提交教师表单
const submitTeacherForm = () => {
  teacherFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (dialogType.value === 'add') {
          response = await axios.post('/api/admin/teacher', teacherForm)
        } else {
          response = await axios.put('/api/admin/teacher', teacherForm)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
          dialogVisible.value = false
          fetchTeachers()
        } else {
          ElMessage.error(response.data.message || (dialogType.value === 'add' ? '添加失败' : '修改失败'))
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加教师失败:' : '修改教师失败:', error)
        ElMessage.error(dialogType.value === 'add' ? '添加教师失败' : '修改教师失败')
      }
    }
  })
}

// 判断教师是否有指定职称
const hasTitle = (teacherId, titleType) => {
  return teacherTitles.value.some(title => 
    title.teacherId === teacherId && title.titleType === titleType
  )
}

// 处理设置职称
const handleSetTitle = async (row) => {
  try {
    currentTeacher.value = row
    titleForm.teacherId = row.id
    
    // 初始化职称表单
    titleForm.isNormalTeacher = hasTitle(row.id, 0)
    titleForm.isCourseLeader = hasTitle(row.id, 1)
    titleForm.isMajorLeader = hasTitle(row.id, 2)
    titleForm.isViceDean = hasTitle(row.id, 3)
    
    // 获取当前职称对应的课程、专业、学院ID
    const courseLeaderTitle = teacherTitles.value.find(title => 
      title.teacherId === row.id && title.titleType === 1
    )
    titleForm.courseId = courseLeaderTitle ? courseLeaderTitle.courseId : undefined
    
    const majorLeaderTitle = teacherTitles.value.find(title => 
      title.teacherId === row.id && title.titleType === 2
    )
    titleForm.majorId = majorLeaderTitle ? majorLeaderTitle.majorId : undefined
    
    const viceDeanTitle = teacherTitles.value.find(title => 
      title.teacherId === row.id && title.titleType === 3
    )
    titleForm.collegeId = viceDeanTitle ? viceDeanTitle.collegeId : undefined
    
    // 如果是课程负责人但没有选择课程，则默认选择第一个课程
    if (titleForm.isCourseLeader && !titleForm.courseId && courseList.value.length > 0) {
      titleForm.courseId = courseList.value[0].id
    }
    
    // 如果是专业负责人但没有选择专业，则默认选择第一个专业
    if (titleForm.isMajorLeader && !titleForm.majorId && majorList.value.length > 0) {
      titleForm.majorId = majorList.value[0].id
    }
    
    // 如果是副院长但没有选择学院，则默认选择第一个学院
    if (titleForm.isViceDean && !titleForm.collegeId && collegeList.value.length > 0) {
      titleForm.collegeId = collegeList.value[0].id
    }
    
    // 检查是否已有其他教师担任这些职称
    if (titleForm.courseId) {
      titleForm.hasCourseLeader = teacherTitles.value.some(title => 
        title.titleType === 1 && title.teacherId !== row.id && title.courseId === titleForm.courseId
      )
    } else {
      titleForm.hasCourseLeader = false
    }
    
    if (titleForm.majorId) {
      titleForm.hasMajorLeader = teacherTitles.value.some(title => 
        title.titleType === 2 && title.teacherId !== row.id && title.majorId === titleForm.majorId
      )
    } else {
      titleForm.hasMajorLeader = false
    }
    
    if (titleForm.collegeId) {
      titleForm.hasViceDean = teacherTitles.value.some(title => 
        title.titleType === 3 && title.teacherId !== row.id && title.collegeId === titleForm.collegeId
      )
    } else {
      titleForm.hasViceDean = false
    }
    
    titleDialogVisible.value = true
  } catch (error) {
    console.error('设置教师职称时出错:', error)
    ElMessage.error('设置教师职称时出错，请刷新页面后重试')
  }
}

// 提交职称表单
const submitTitleForm = async () => {
  try {
    // 验证必填项
    if (titleForm.isCourseLeader && !titleForm.courseId) {
      ElMessage.warning('请选择负责的课程')
      return
    }
    
    if (titleForm.isMajorLeader && !titleForm.majorId) {
      ElMessage.warning('请选择负责的专业')
      return
    }
    
    if (titleForm.isViceDean && !titleForm.collegeId) {
      ElMessage.warning('请选择所属学院')
      return
    }
    
    const response = await axios.post('/api/admin/teacher/titles', {
      teacherId: titleForm.teacherId,
      isNormalTeacher: titleForm.isNormalTeacher,
      isCourseLeader: titleForm.isCourseLeader,
      isMajorLeader: titleForm.isMajorLeader,
      isViceDean: titleForm.isViceDean,
      courseId: titleForm.courseId,
      majorId: titleForm.majorId,
      collegeId: titleForm.collegeId
    })
    
    if (response.data.code === 200) {
      ElMessage.success('设置职称成功')
      titleDialogVisible.value = false
      fetchTeacherTitles()
      fetchTeachers()
    } else {
      ElMessage.error(response.data.message || '设置职称失败')
    }
  } catch (error) {
    console.error('设置职称失败:', error)
    ElMessage.error('设置职称失败，请刷新页面后重试')
  }
}

// 处理职称对话框关闭
const handleTitleDialogClosed = () => {
  try {
    // 重置职称表单
    titleForm.teacherId = undefined
    titleForm.isNormalTeacher = false
    titleForm.isCourseLeader = false
    titleForm.isMajorLeader = false
    titleForm.isViceDean = false
    titleForm.courseId = undefined
    titleForm.majorId = undefined
    titleForm.collegeId = undefined
    titleForm.hasCourseLeader = false
    titleForm.hasMajorLeader = false
    titleForm.hasViceDean = false
    
    // 清空当前选中教师
    currentTeacher.value = {}
  } catch (error) {
    console.error('关闭对话框重置表单时出错:', error)
  }
}

// 处理课程负责人变化
const handleCourseLeaderChange = (value) => {
  if (value) {
    // 如果开启课程负责人，默认选择第一个课程
    if (!titleForm.courseId && courseList.value.length > 0) {
      titleForm.courseId = courseList.value[0].id
      // 检查是否已有其他教师担任这一职称
      titleForm.hasCourseLeader = teacherTitles.value.some(title => 
        title.titleType === 1 && title.teacherId !== titleForm.teacherId && title.courseId === titleForm.courseId
      )
    }
  } else {
    // 如果关闭，清空选择
    titleForm.courseId = undefined
    titleForm.hasCourseLeader = false
  }
}

// 处理课程变化
const handleCourseChange = (courseId) => {
  // 检查是否已有其他教师担任该课程负责人
  titleForm.hasCourseLeader = teacherTitles.value.some(title => 
    title.titleType === 1 && title.teacherId !== titleForm.teacherId && title.courseId === courseId
  )
}

// 处理专业负责人变化
const handleMajorLeaderChange = (value) => {
  if (value) {
    // 如果开启专业负责人，默认选择第一个专业
    if (!titleForm.majorId && majorList.value.length > 0) {
      titleForm.majorId = majorList.value[0].id
      // 检查是否已有其他教师担任这一职称
      titleForm.hasMajorLeader = teacherTitles.value.some(title => 
        title.titleType === 2 && title.teacherId !== titleForm.teacherId && title.majorId === titleForm.majorId
      )
    }
  } else {
    // 如果关闭，清空选择
    titleForm.majorId = undefined
    titleForm.hasMajorLeader = false
  }
}

// 处理专业变化
const handleMajorChange = (majorId) => {
  // 检查是否已有其他教师担任该专业负责人
  titleForm.hasMajorLeader = teacherTitles.value.some(title => 
    title.titleType === 2 && title.teacherId !== titleForm.teacherId && title.majorId === majorId
  )
}

// 处理副院长变化
const handleViceDeanChange = (value) => {
  if (value) {
    // 如果开启副院长，默认选择第一个学院
    if (!titleForm.collegeId && collegeList.value.length > 0) {
      titleForm.collegeId = collegeList.value[0].id
      // 检查是否已有其他教师担任这一职称
      titleForm.hasViceDean = teacherTitles.value.some(title => 
        title.titleType === 3 && title.teacherId !== titleForm.teacherId && title.collegeId === titleForm.collegeId
      )
    }
  } else {
    // 如果关闭，清空选择
    titleForm.collegeId = undefined
    titleForm.hasViceDean = false
  }
}

// 处理学院变化
const handleCollegeChange = (collegeId) => {
  // 检查是否已有其他教师担任该学院副院长
  titleForm.hasViceDean = teacherTitles.value.some(title => 
    title.titleType === 3 && title.teacherId !== titleForm.teacherId && title.collegeId === collegeId
  )
}

onMounted(() => {
  fetchTeachers()
  fetchTeacherTitles()
  fetchCourses()
  fetchMajors()
  fetchColleges()
})
</script>

<style lang="scss" scoped>
.teacher-management {
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
  
  .mt-10 {
    margin-top: 10px;
  }
}

.tip-text {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
  display: block;
}
</style> 