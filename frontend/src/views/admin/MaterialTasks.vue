<template>
  <div class="material-tasks">
    <h1 class="page-title">教学材料任务管理</h1>
    
    <!-- 过滤条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="任务标题">
          <el-input v-model="queryParams.title" placeholder="请输入任务标题" clearable></el-input>
        </el-form-item>
        <el-form-item label="材料类型">
          <el-select v-model="queryParams.materialTypeId" placeholder="请选择材料类型" clearable style="width: 200px;">
            <el-option v-for="item in materialTypes" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学年">
          <el-select v-model="queryParams.academicYear" placeholder="请选择学年" clearable style="width: 200px;">
            <el-option v-for="year in academicYears" :key="year" :label="year" :value="year"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable style="width: 200px;">
            <el-option :label="'第一学期'" :value="1"></el-option>
            <el-option :label="'第二学期'" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px;">
            <el-option :label="'未开始'" :value="0"></el-option>
            <el-option :label="'进行中'" :value="1"></el-option>
            <el-option :label="'已结束'" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作按钮 -->
    <div class="operation-container">
      <el-button type="primary" @click="handleAdd">添加任务</el-button>
      <el-button type="success" @click="handlePublishBatch" :disabled="selectedTasks.length === 0">批量发布</el-button>
      <el-button type="danger" @click="handleDeleteBatch" :disabled="selectedTasks.length === 0">批量删除</el-button>
    </div>
    
    <!-- 任务表格 -->
    <el-card class="table-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <el-table
        v-else
        :data="taskList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="任务标题" width="200"></el-table-column>
        <el-table-column prop="materialTypeName" label="材料类型"></el-table-column>
        <el-table-column prop="courseName" label="课程" width="150"></el-table-column>
        <el-table-column prop="academicYear" label="学年"></el-table-column>
        <el-table-column prop="semester" label="学期">
          <template #default="scope">
            {{ scope.row.semester === 1 ? '第一学期' : '第二学期' }}
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期" width="150"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : (scope.row.status === 0 ? 'warning' : 'danger')">
              {{ scope.row.status === 1 ? '进行中' : (scope.row.status === 0 ? '未开始' : '已结束') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button-group>
              <el-button v-if="scope.row.status === 0" size="small" type="primary" @click="handlePublish(scope.row.id)">发布</el-button>
              <el-button size="small" type="success" @click="handleViewProgress(scope.row.id)">完成情况</el-button>
              <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑任务对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="taskForm"
        :rules="taskRules"
        ref="taskFormRef"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="请输入任务标题"></el-input>
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="材料类型" prop="materialTypeId">
          <el-select v-model="taskForm.materialTypeId" placeholder="请选择材料类型" style="width: 100%;">
            <el-option v-for="item in materialTypes" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属课程" prop="courseIds">
          <el-select
            v-model="taskForm.courseIds"
            multiple
            collapse-tags
            placeholder="请选择课程，可多选"
            style="width: 100%;"
          >
            <el-option v-for="item in courseList" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-select v-model="taskForm.academicYear" placeholder="请选择学年" style="width: 100%;">
            <el-option v-for="year in academicYears" :key="year" :label="year" :value="year"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-radio-group v-model="taskForm.semester">
            <el-radio :label="1">第一学期</el-radio>
            <el-radio :label="2">第二学期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="选择截止日期"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="taskForm.status">
            <el-radio :label="0">未发布</el-radio>
            <el-radio :label="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 任务完成情况对话框 -->
    <el-dialog
      title="任务完成情况"
      v-model="progressDialogVisible"
      width="700px"
    >
      <div v-if="progressLoading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <template v-else>
        <el-descriptions v-if="currentTask" :column="2" border>
          <el-descriptions-item label="任务标题">{{ currentTask.title }}</el-descriptions-item>
          <el-descriptions-item label="材料类型">{{ currentTask.materialTypeName }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentTask.deadline }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentTask.status === 1 ? 'success' : (currentTask.status === 0 ? 'warning' : 'danger')">
              {{ currentTask.status === 1 ? '进行中' : (currentTask.status === 0 ? '未开始' : '已结束') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="完成情况" :span="2">
            {{ progressData.completedCount || 0 }} / {{ progressData.totalCount || 0 }}
            ({{ progressData.totalCount ? Math.round(progressData.completedCount / progressData.totalCount * 100) : 0 }}%)
          </el-descriptions-item>
        </el-descriptions>
        
        <el-tabs v-model="progressActiveTab" class="progress-tabs">
          <el-tab-pane label="已完成" name="completed">
            <el-table :data="progressData.completedTeachers || []" style="width: 100%" v-if="progressActiveTab === 'completed'">
              <el-table-column prop="teacherId" label="教师ID" width="80"></el-table-column>
              <el-table-column prop="teacherName" label="教师姓名"></el-table-column>
              <el-table-column prop="courseName" label="课程名称"></el-table-column>
              <el-table-column prop="materialName" label="材料名称"></el-table-column>
              <el-table-column prop="submitTime" label="提交时间" width="160"></el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="viewMaterial(scope.row.materialId)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="未完成" name="uncompleted">
            <el-table :data="progressData.uncompletedTeachers || []" style="width: 100%" v-if="progressActiveTab === 'uncompleted'">
              <el-table-column prop="teacherId" label="教师ID" width="80"></el-table-column>
              <el-table-column prop="teacherName" label="教师姓名"></el-table-column>
              <el-table-column prop="courseName" label="课程名称"></el-table-column>
              <el-table-column prop="email" label="邮箱"></el-table-column>
              <el-table-column prop="phone" label="电话"></el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button size="small" type="warning" @click="sendReminder(scope.row.teacherId)">提醒</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

// 任务列表相关数据
const loading = ref(false);
const taskList = ref([]);
const total = ref(0);
const selectedTasks = ref([]);

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  title: '',
  materialTypeId: '',
  academicYear: '',
  semester: '',
  status: ''
});

// 添加/编辑对话框相关数据
const dialogVisible = ref(false);
const dialogTitle = ref('添加任务');
const taskFormRef = ref(null);
const submitLoading = ref(false);
const taskForm = reactive({
  id: null,
  title: '',
  description: '',
  materialTypeId: '',
  courseIds: [],
  academicYear: '',
  semester: 1,
  deadline: '',
  status: 0
});

// 任务表单校验规则
const taskRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  materialTypeId: [{ required: true, message: '请选择材料类型', trigger: 'change' }],
  courseIds: [{ required: true, message: '请选择课程', trigger: 'change' }],
  academicYear: [{ required: true, message: '请选择学年', trigger: 'change' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }]
};

// 进度对话框相关数据
const progressDialogVisible = ref(false);
const progressLoading = ref(false);
const currentTask = ref(null);
const progressData = ref({});
const progressActiveTab = ref('completed');

// 选项列表
const materialTypes = ref([]);
const courseList = ref([]);
const academicYears = ref([]);

// 生成学年列表
const generateAcademicYears = () => {
  const years = [];
  const currentYear = new Date().getFullYear();
  for (let i = 0; i < 5; i++) {
    const year = currentYear - i;
    years.push(`${year}-${year + 1}`);
  }
  academicYears.value = years;
};

// 获取材料类型列表
const fetchMaterialTypes = async () => {
  try {
    const response = await axios.get('/api/material-type/list');
    if (response.data.code === 200 && response.data.data) {
      materialTypes.value = response.data.data;
    }
  } catch (error) {
    console.error('获取材料类型出错:', error);
    ElMessage.error('获取材料类型失败');
  }
};

// 获取课程列表
const fetchCourseList = async () => {
  try {
    const response = await axios.get('/api/admin/course/list');
    if (response.data.code === 200 && response.data.data) {
      courseList.value = response.data.data;
    }
  } catch (error) {
    console.error('获取课程列表出错:', error);
    ElMessage.error('获取课程列表失败');
  }
};

// 获取任务列表
const fetchTaskList = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/admin/material-task/list', {
      params: queryParams
    });
    if (response.data.code === 200) {
      taskList.value = response.data.data.records || [];
      total.value = response.data.data.total || 0;
    } else {
      ElMessage.error(response.data.message || '获取任务列表失败');
    }
  } catch (error) {
    console.error('获取任务列表出错:', error);
    ElMessage.error('获取任务列表失败');
  } finally {
    loading.value = false;
  }
};

// 查询任务
const handleQuery = () => {
  queryParams.current = 1;
  fetchTaskList();
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryParams, {
    current: 1,
    size: 10,
    title: '',
    materialTypeId: '',
    academicYear: '',
    semester: '',
    status: ''
  });
  fetchTaskList();
};

// 表格选择行变化
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection;
};

// 添加任务
const handleAdd = () => {
  dialogTitle.value = '添加任务';
  resetTaskForm();
  dialogVisible.value = true;
};

// 编辑任务
const handleEdit = (row) => {
  dialogTitle.value = '编辑任务';
  resetTaskForm();
  Object.assign(taskForm, { ...row });
  if (typeof taskForm.courseIds === 'string') {
    taskForm.courseIds = taskForm.courseIds.split(',').map(id => parseInt(id));
  }
  dialogVisible.value = true;
};

// 重置任务表单
const resetTaskForm = () => {
  taskForm.id = null;
  taskForm.title = '';
  taskForm.description = '';
  taskForm.materialTypeId = '';
  taskForm.courseIds = [];
  taskForm.academicYear = academicYears.value[0] || '';
  taskForm.semester = 1;
  taskForm.deadline = '';
  taskForm.status = 0;
  if (taskFormRef.value) {
    taskFormRef.value.resetFields();
  }
};

// 提交表单
const submitForm = async () => {
  if (!taskFormRef.value) return;
  
  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitLoading.value = true;
    try {
      const method = taskForm.id ? 'put' : 'post';
      const url = taskForm.id ? `/api/admin/material-task/${taskForm.id}` : '/api/admin/material-task';
      
      const response = await axios[method](url, taskForm);
      if (response.data.code === 200) {
        ElMessage.success(`${taskForm.id ? '更新' : '添加'}任务成功`);
        dialogVisible.value = false;
        fetchTaskList();
      } else {
        ElMessage.error(response.data.message || `${taskForm.id ? '更新' : '添加'}任务失败`);
      }
    } catch (error) {
      console.error(`${taskForm.id ? '更新' : '添加'}任务出错:`, error);
      ElMessage.error(`${taskForm.id ? '更新' : '添加'}任务失败，请稍后重试`);
    } finally {
      submitLoading.value = false;
    }
  });
};

// 发布任务
const handlePublish = async (id) => {
  try {
    const response = await axios.put(`/api/admin/material-task/publish/${id}`);
    if (response.data.code === 200) {
      ElMessage.success('发布任务成功');
      fetchTaskList();
    } else {
      ElMessage.error(response.data.message || '发布任务失败');
    }
  } catch (error) {
    console.error('发布任务出错:', error);
    ElMessage.error('发布任务失败，请稍后重试');
  }
};

// 批量发布任务
const handlePublishBatch = async () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请选择要发布的任务');
    return;
  }
  
  const taskIds = selectedTasks.value.map(task => task.id);
  try {
    const response = await axios.put('/api/admin/material-task/publish-batch', { taskIds });
    if (response.data.code === 200) {
      ElMessage.success('批量发布任务成功');
      fetchTaskList();
    } else {
      ElMessage.error(response.data.message || '批量发布任务失败');
    }
  } catch (error) {
    console.error('批量发布任务出错:', error);
    ElMessage.error('批量发布任务失败，请稍后重试');
  }
};

// 删除任务
const handleDelete = async (id) => {
  ElMessageBox.confirm('确定要删除此任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`/api/admin/material-task/${id}`);
      if (response.data.code === 200) {
        ElMessage.success('删除任务成功');
        fetchTaskList();
      } else {
        ElMessage.error(response.data.message || '删除任务失败');
      }
    } catch (error) {
      console.error('删除任务出错:', error);
      ElMessage.error('删除任务失败，请稍后重试');
    }
  }).catch(() => {});
};

// 批量删除任务
const handleDeleteBatch = async () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请选择要删除的任务');
    return;
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedTasks.value.length} 个任务吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const taskIds = selectedTasks.value.map(task => task.id);
    try {
      const response = await axios.delete('/api/admin/material-task/batch', {
        data: { taskIds }
      });
      if (response.data.code === 200) {
        ElMessage.success('批量删除任务成功');
        fetchTaskList();
      } else {
        ElMessage.error(response.data.message || '批量删除任务失败');
      }
    } catch (error) {
      console.error('批量删除任务出错:', error);
      ElMessage.error('批量删除任务失败，请稍后重试');
    }
  }).catch(() => {});
};

// 查看任务完成情况
const handleViewProgress = async (id) => {
  progressLoading.value = true;
  progressDialogVisible.value = true;
  
  try {
    // 获取任务详情
    const taskResponse = await axios.get(`/api/admin/material-task/${id}`);
    if (taskResponse.data.code === 200 && taskResponse.data.data) {
      currentTask.value = taskResponse.data.data;
    }
    
    // 获取任务完成情况
    const progressResponse = await axios.get(`/api/admin/material-task/progress/${id}`);
    if (progressResponse.data.code === 200 && progressResponse.data.data) {
      progressData.value = progressResponse.data.data;
    }
  } catch (error) {
    console.error('获取任务完成情况出错:', error);
    ElMessage.error('获取任务完成情况失败，请稍后重试');
  } finally {
    progressLoading.value = false;
  }
};

// 查看材料
const viewMaterial = (materialId) => {
  window.open(`/api/material/download/${materialId}`, '_blank');
};

// 发送提醒
const sendReminder = async (teacherId) => {
  try {
    const response = await axios.post('/api/admin/material-task/remind', {
      taskId: currentTask.value.id,
      teacherId
    });
    if (response.data.code === 200) {
      ElMessage.success('已发送提醒通知');
    } else {
      ElMessage.error(response.data.message || '发送提醒失败');
    }
  } catch (error) {
    console.error('发送提醒出错:', error);
    ElMessage.error('发送提醒失败，请稍后重试');
  }
};

// 分页器相关方法
const handleSizeChange = (size) => {
  queryParams.size = size;
  fetchTaskList();
};

const handleCurrentChange = (current) => {
  queryParams.current = current;
  fetchTaskList();
};

onMounted(() => {
  generateAcademicYears();
  fetchMaterialTypes();
  fetchCourseList();
  fetchTaskList();
});
</script>

<style scoped>
.material-tasks {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.operation-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.loading-container {
  padding: 20px 0;
}

.progress-tabs {
  margin-top: 20px;
}
</style> 