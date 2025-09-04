<template>
  <div class="material-upload">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>教学材料上传</span>
        </div>
      </template>
      
      <el-form :model="uploadForm" :rules="rules" ref="uploadFormRef" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="uploadForm.courseId" placeholder="请选择课程" @change="handleCourseChange" style="width: 350px;">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="材料类型" prop="materialTypeId">
          <el-select v-model="uploadForm.materialTypeId" placeholder="请选择材料类型" style="width: 350px;">
            <el-option
              v-for="type in materialTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="学年" prop="academicYear">
          <el-select v-model="uploadForm.academicYear" placeholder="请选择学年" style="width: 350px;">
            <el-option
              v-for="year in academicYears"
              :key="year"
              :label="year"
              :value="year"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="学期" prop="semester">
          <el-radio-group v-model="uploadForm.semester">
            <el-radio :label="1">第一学期</el-radio>
            <el-radio :label="2">第二学期</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="任务标题">
          <el-select v-model="uploadForm.taskId" placeholder="请选择任务标题（非必选）" clearable style="width: 350px;">
            <el-option
              v-for="task in teacherTasks"
              :key="task.id"
              :label="task.title"
              :value="task.id"
            ></el-option>
          </el-select>
          <div class="el-form-item__description">
            <small>关联到教学材料任务可以自动记录任务完成情况</small>
          </div>
        </el-form-item>
        
        <el-form-item label="材料名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入材料名称"></el-input>
        </el-form-item>
        
        <el-form-item label="材料描述" prop="description">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入材料描述"></el-input>
        </el-form-item>
        
        <el-form-item label="上传文件" prop="file">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :limit="1"
            :file-list="fileList"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 doc/docx/pdf 文件，且不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitUpload" :loading="uploading">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>已上传材料</span>
          <div class="filter-container">
            <el-form :inline="true" :model="queryParams" class="demo-form-inline">
              <el-form-item label="课程">
                <el-select v-model="queryParams.courseId" placeholder="请选择课程" clearable style="width: 250px;">
                  <el-option
                    v-for="course in courses"
                    :key="course.id"
                    :label="course.name"
                    :value="course.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="材料类型">
                <el-select v-model="queryParams.materialTypeId" placeholder="请选择材料类型" clearable style="width: 250px;">
                  <el-option
                    v-for="type in materialTypes"
                    :key="type.id"
                    :label="type.name"
                    :value="type.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="学年">
                <el-select v-model="queryParams.academicYear" placeholder="请选择学年" clearable style="width: 250px;">
                  <el-option
                    v-for="year in academicYears"
                    :key="year"
                    :label="year"
                    :value="year"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="学期">
                <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable style="width: 250px;">
                  <el-option label="第一学期" :value="1"></el-option>
                  <el-option label="第二学期" :value="2"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="fetchMaterials">查询</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>
      
      <el-table :data="materials" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="材料名称" width="180"></el-table-column>
        <el-table-column prop="course_name" label="所属课程" width="150"></el-table-column>
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
        <el-table-column prop="semester" label="学期" width="100">
          <template #default="scope">
            {{ scope.row.semester === 1 ? '第一学期' : scope.row.semester === 2 ? '第二学期' : '未知学期' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="create_time" label="上传时间" width="180"></el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button type="text" size="small" @click="downloadMaterial(scope.row)">下载</el-button>
            <el-button type="text" size="small" @click="submitToReview(scope.row)" v-if="!scope.row.review_id">提交审核</el-button>
            <el-button type="text" size="small" @click="viewReviewStatus(scope.row)" v-if="scope.row.review_id">查看审核状态</el-button>
            <el-button type="text" size="small" @click="deleteMaterial(scope.row)" class="danger-button">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="queryParams.size"
          :current-page="queryParams.current"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 审核状态对话框 -->
    <el-dialog title="审核状态" v-model="reviewDialogVisible" width="60%">
      <div v-if="reviewDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="材料名称">{{ reviewDetail.material_name }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ reviewDetail.course_name || '未知课程' }}</el-descriptions-item>
          <el-descriptions-item label="材料类型">{{ reviewDetail.material_type_name || '未知类型' }}</el-descriptions-item>
          <el-descriptions-item label="学年/学期">{{ reviewDetail.academic_year || '未知学年' }} / {{ reviewDetail.semester === 1 ? '第一学期' : '第二学期' }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(reviewDetail.status)">{{ getStatusText(reviewDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前审核人">{{ reviewDetail.current_reviewer_name || '暂无审核人' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="review-records">
          <h3>审核记录</h3>
          <el-table :data="reviewRecords" style="width: 100%">
            <el-table-column prop="reviewer_name" label="审核人" width="120"></el-table-column>
            <el-table-column prop="reviewer_title" label="审核人职称" width="120">
              <template #default="scope">
                {{ getReviewerTitleText(scope.row.reviewer_title) }}
              </template>
            </el-table-column>
            <el-table-column prop="result" label="审核结果" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.result === 1 ? 'success' : 'danger'">
                  {{ scope.row.result === 1 ? '通过' : '不通过' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="审核意见"></el-table-column>
            <el-table-column prop="create_time" label="审核时间" width="180"></el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';
import { useStore } from 'vuex';
import { useRoute } from 'vue-router';

export default {
  name: 'MaterialUpload',
  setup() {
    const store = useStore();
    const uploadFormRef = ref(null);
    const route = useRoute();
    
    // 当前用户信息
    const currentUser = computed(() => store.state.user);
    
    // 上传表单
    const uploadForm = reactive({
      courseId: '',
      materialTypeId: '',
      academicYear: '',
      semester: 1,
      taskId: null,
      name: '',
      description: '',
      file: null
    });
    
    // 表单验证规则
    const rules = {
      courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
      materialTypeId: [{ required: true, message: '请选择材料类型', trigger: 'change' }],
      academicYear: [{ required: true, message: '请选择学年', trigger: 'change' }],
      semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
      name: [{ required: true, message: '请输入材料名称', trigger: 'blur' }],
      description: [{ required: true, message: '请输入材料描述', trigger: 'blur' }]
    };
    
    // 上传相关
    const uploadUrl = '/api/material/upload';
    const uploadHeaders = {
      Authorization: `Bearer ${localStorage.getItem('token')}`
    };
    const fileList = ref([]);
    const uploading = ref(false);
    
    // 课程列表
    const courses = ref([]);
    const fetchCourses = async () => {
      try {
        // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
        let teacherId = currentUser.value?.id;
        if (!teacherId) {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          teacherId = userInfo.id;
        }
        
        if (!teacherId) {
          ElMessage.warning('无法获取用户信息，请重新登录');
          return;
        }
        
        console.log('当前教师ID:', teacherId);
        const response = await axios.get(`/api/course/teacher/${teacherId}`);
        courses.value = response.data.data;
      } catch (error) {
        console.error('获取课程列表失败', error);
        ElMessage.error('获取课程列表失败');
      }
    };
    
    // 材料类型列表
    const materialTypes = ref([]);
    const fetchMaterialTypes = async () => {
      try {
        const response = await axios.get('/api/material-type/list');
        materialTypes.value = response.data.data;
      } catch (error) {
        console.error('获取材料类型列表失败', error);
        ElMessage.error('获取材料类型列表失败');
      }
    };
    
    // 学年列表
    const academicYears = ref([]);
    const generateAcademicYears = () => {
      const currentYear = new Date().getFullYear();
      const years = [];
      for (let i = 0; i < 5; i++) {
        const year = currentYear - i;
        years.push(`${year}-${year + 1}`);
      }
      academicYears.value = years;
    };
    
    // 教师任务列表
    const teacherTasks = ref([]);
    const fetchTeacherTasks = async () => {
      try {
        // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
        let teacherId = currentUser.value?.id;
        if (!teacherId) {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          teacherId = userInfo.id;
        }
        
        if (!teacherId) {
          console.warn('无法获取教师ID，未加载用户信息');
          return;
        }

        console.log('获取教师任务列表，教师ID:', teacherId);
        const response = await axios.get('/api/material-task/teacher/tasks', {
          params: { teacherId }
        });
        
        if (response.data.code === 200) {
          teacherTasks.value = response.data.data || [];
          console.log('获取到教师任务列表:', teacherTasks.value.length);
        } else {
          console.error('获取教师任务列表失败:', response.data.message);
          teacherTasks.value = [];
        }
      } catch (error) {
        console.error('获取教师任务列表失败:', error);
        teacherTasks.value = [];
      }
    };
    
    // 上传文件相关方法
    const handlePreview = (/* file */) => {
      // console.log(file);
    };
    
    const handleRemove = (/* file, fileList */) => {
      uploadForm.file = null;
    };
    
    const beforeUpload = (file) => {
      // 简化文件类型判断和处理
      let isValidType = false;
      let simpleFileType = '';
      
      if (file.type === 'application/msword' || file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') {
        isValidType = true;
        simpleFileType = 'doc';
      } else if (file.type === 'application/pdf') {
        isValidType = true;
        simpleFileType = 'pdf';
      }
      
      const isLt10M = file.size / 1024 / 1024 < 10;
      
      if (!isValidType) {
        ElMessage.error('上传文件只能是 doc/docx/pdf 格式!');
        return false;
      }
      
      if (!isLt10M) {
        ElMessage.error('上传文件大小不能超过 10MB!');
        return false;
      }
      
      uploadForm.file = file;
      uploadForm.fileType = simpleFileType;
      return true;
    };
    
    const handleUploadSuccess = (response, /* file, fileList */) => {
      if (response.code === 200) {
        uploadForm.filePath = response.data.filePath;
        uploadForm.fileSize = response.data.fileSize;
        // 使用简化后的文件类型
        uploadForm.fileType = uploadForm.fileType || 'unknown';
        ElMessage.success('文件上传成功');
      } else {
        ElMessage.error(response.message || '文件上传失败');
      }
    };
    
    const handleUploadError = (error, /* file, fileList */) => {
      console.error('文件上传失败', error);
      ElMessage.error('文件上传失败');
    };
    
    // 提交上传
    const submitUpload = async () => {
      if (!uploadForm.file) {
        ElMessage.warning('请先上传文件');
        return;
      }
      
      uploadFormRef.value.validate(async (valid) => {
        if (valid) {
          uploading.value = true;
          try {
            // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
            let teacherId = currentUser.value?.id;
            if (!teacherId) {
              const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
              teacherId = userInfo.id;
            }
            
            if (!teacherId) {
              ElMessage.warning('无法获取用户信息，请重新登录');
              uploading.value = false;
              return;
            }
            
            // 添加日志，检查taskId
            console.log('提交材料时的表单数据:', JSON.stringify(uploadForm));
            console.log('任务ID:', uploadForm.taskId, '类型:', typeof uploadForm.taskId);
            
            const formData = new FormData();
            formData.append('file', uploadForm.file);
            formData.append('courseId', uploadForm.courseId);
            formData.append('materialTypeId', uploadForm.materialTypeId);
            formData.append('academicYear', uploadForm.academicYear);
            formData.append('semester', uploadForm.semester);
            formData.append('name', uploadForm.name);
            formData.append('description', uploadForm.description);
            formData.append('type', 0); // 0-课程材料
            formData.append('teacherId', teacherId); // 添加教师ID
            
            // 如果选择了任务，添加任务ID
            if (uploadForm.taskId) {
              console.log('添加任务ID到表单:', uploadForm.taskId);
              formData.append('taskId', uploadForm.taskId);
            } else {
              console.log('未选择任务，不添加taskId');
            }
            
            // 上传材料
            const response = await axios.post('/api/material/upload', formData, {
              headers: {
                'Content-Type': 'multipart/form-data'
              }
            });
            
            if (response.data.code === 200) {
              ElMessage.success('材料上传成功，已提交审核');
              resetForm();
              fetchMaterials();
            } else {
              ElMessage.error(response.data.message || '材料上传失败');
            }
          } catch (error) {
            console.error('材料上传失败', error);
            ElMessage.error('材料上传失败: ' + (error.response?.data?.message || error.message || '未知错误'));
          } finally {
            uploading.value = false;
          }
        }
      });
    };
    
    // 重置表单
    const resetForm = () => {
      uploadFormRef.value.resetFields();
      fileList.value = [];
      uploadForm.file = null;
    };
    
    // 查询已上传材料
    const queryParams = reactive({
      current: 1,
      size: 10,
      teacherId: null,
      courseId: null,
      materialTypeId: null,
      academicYear: null,
      semester: null,
      type: 0 // 0-课程材料
    });
    const materials = ref([]);
    const total = ref(0);
    const loading = ref(false);
    
    const fetchMaterials = async () => {
      loading.value = true;
      try {
        // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
        let teacherId = currentUser.value?.id;
        if (!teacherId) {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          teacherId = userInfo.id;
        }
        
        if (!teacherId) {
          ElMessage.warning('无法获取用户信息，请重新登录');
          loading.value = false;
          return;
        }
        
        queryParams.teacherId = teacherId;
        const response = await axios.get('/api/material/teacher', {
          params: queryParams
        });
        if (response.data && response.data.data) {
          // 处理并格式化返回的数据
          const records = response.data.data.records || [];
          materials.value = records.map(record => {
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
          console.log('获取到的材料列表:', materials.value);
          if (materials.value.length > 0) {
            console.log('第一条材料数据样例:', materials.value[0]);
            console.log('材料类型名称:', materials.value[0].material_type_name);
            console.log('学年:', materials.value[0].academic_year);
            console.log('学期:', materials.value[0].semester, '类型:', typeof materials.value[0].semester);
          }
        } else {
          materials.value = [];
          total.value = 0;
        }
      } catch (error) {
        console.error('获取材料列表失败', error);
        ElMessage.error('获取材料列表失败');
        materials.value = [];
        total.value = 0;
      } finally {
        loading.value = false;
      }
    };
    
    const handleCurrentChange = (current) => {
      queryParams.current = current;
      fetchMaterials();
    };
    
    const handleCourseChange = (/* courseId */) => {
      // 可以在这里根据课程ID获取相关信息
    };
    
    // 下载材料
    const downloadMaterial = (material) => {
      if (!material.id) {
        ElMessage.warning('材料ID为空');
        return;
      }
      
      try {
        // 创建下载链接 - 使用API接口路径
        const downloadUrl = `/api/material/download/${material.id}`;
        
        // 创建一个隐藏的a标签，用于下载
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.target = '_blank';
        link.setAttribute('download', ''); // 强制使用下载属性
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // 记录下载次数
        axios.post(`/api/material/download/${material.id}`)
          .catch(error => console.error('更新下载次数失败:', error));
        
        ElMessage.success('开始下载文件');
      } catch (error) {
        console.error('下载文件失败:', error);
        ElMessage.error('下载文件失败');
      }
    };
    
    // 删除材料
    const deleteMaterial = (material) => {
      ElMessageBox.confirm('确定要删除该材料吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await axios.delete(`/api/material/${material.id}`);
          if (response.data.code === 200) {
            ElMessage.success('删除成功');
            fetchMaterials();
          } else {
            ElMessage.error(response.data.message || '删除失败');
          }
        } catch (error) {
          console.error('删除失败', error);
          ElMessage.error('删除失败');
        }
      }).catch(() => {});
    };
    
    // 提交审核
    const submitToReview = async (material) => {
      try {
        // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
        let teacherId = currentUser.value?.id;
        if (!teacherId) {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          teacherId = userInfo.id;
        }
        
        if (!teacherId) {
          ElMessage.warning('无法获取用户信息，请重新登录');
          return;
        }
        
        const response = await axios.post('/api/material-review/submit', {
          materialId: material.id,
          academicYear: material.academic_year,
          semester: material.semester,
          materialTypeId: material.material_type_id,
          teacherId: teacherId,
          courseId: material.course_id
        });
        
        if (response.data.code === 200) {
          ElMessage.success('提交审核成功');
          fetchMaterials();
        } else {
          ElMessage.error(response.data.message || '提交审核失败');
        }
      } catch (error) {
        console.error('提交审核失败', error);
        ElMessage.error('提交审核失败');
      }
    };
    
    // 查看审核状态
    const reviewDialogVisible = ref(false);
    const reviewDetail = ref(null);
    const reviewRecords = ref([]);
    
    const viewReviewStatus = async (material) => {
      try {
        const detailResponse = await axios.get(`/api/material-review/detail/${material.review_id}`);
        reviewDetail.value = detailResponse.data.data;
        
        // 打印审核详情，检查字段
        console.log('审核详情:', reviewDetail.value);
        
        const recordsResponse = await axios.get(`/api/material-review/records/${material.review_id}`);
        reviewRecords.value = recordsResponse.data.data;
        
        // 确保审核详情包含所需字段
        if (reviewDetail.value) {
          if (!reviewDetail.value.material_type_name) {
            console.warn('审核详情中缺少材料类型名称');
          }
          if (!reviewDetail.value.academic_year) {
            console.warn('审核详情中缺少学年信息');
          }
        }
        
        reviewDialogVisible.value = true;
      } catch (error) {
        console.error('获取审核详情失败', error);
        ElMessage.error('获取审核详情失败');
      }
    };
    
    // 工具函数
    const getStatusText = (status) => {
      switch (status) {
        case 0: return '待审核';
        case 1: return '课程负责人审核中';
        case 2: return '专业负责人审核中';
        case 3: return '副院长审核中';
        case 4: return '教务人员审核中';
        case 5: return '审核通过';
        case 6: return '审核不通过';
        default: return '未知状态';
      }
    };
    
    const getStatusType = (status) => {
      switch (status) {
        case 0: return 'info';
        case 1: case 2: case 3: case 4: return 'warning';
        case 5: return 'success';
        case 6: return 'danger';
        default: return 'info';
      }
    };
    
    const getReviewerTitleText = (title) => {
      switch (title) {
        case 0: return '普通教师';
        case 1: return '课程负责人';
        case 2: return '专业负责人';
        case 3: return '副院长';
        case 4: return '教务人员';
        default: return '未知职称';
      }
    };
    
    onMounted(() => {
      // 初始化获取数据
      fetchCourses();
      fetchMaterialTypes();
      generateAcademicYears();
      fetchTeacherTasks(); // 获取教师任务列表
      
      // 获取URL中的查询参数
      const { taskId, materialTypeId, courseId, academicYear, semester } = route.query;
      
      if (taskId) {
        uploadForm.taskId = Number(taskId);
      }
      if (materialTypeId) {
        uploadForm.materialTypeId = Number(materialTypeId);
      }
      if (courseId) {
        uploadForm.courseId = Number(courseId);
      }
      if (academicYear) {
        uploadForm.academicYear = academicYear;
      }
      if (semester) {
        uploadForm.semester = Number(semester);
      }
      
      fetchMaterials();
    });
    
    return {
      uploadFormRef,
      uploadForm,
      rules,
      uploadUrl,
      uploadHeaders,
      fileList,
      uploading,
      courses,
      materialTypes,
      academicYears,
      handlePreview,
      handleRemove,
      beforeUpload,
      handleUploadSuccess,
      handleUploadError,
      submitUpload,
      resetForm,
      queryParams,
      materials,
      total,
      loading,
      fetchMaterials,
      handleCurrentChange,
      handleCourseChange,
      downloadMaterial,
      deleteMaterial,
      submitToReview,
      reviewDialogVisible,
      reviewDetail,
      reviewRecords,
      viewReviewStatus,
      getStatusText,
      getStatusType,
      getReviewerTitleText,
      teacherTasks
    };
  }
};
</script>

<style scoped>
.material-upload {
  padding: 20px;
}

.filter-container {
  margin-top: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.review-records {
  margin-top: 20px;
}

.danger-button {
  color: #F56C6C;
}

.el-form-item__description {
  padding-top: 5px;
  color: #909399;
  font-size: 12px;
  line-height: 1.2;
}
</style> 