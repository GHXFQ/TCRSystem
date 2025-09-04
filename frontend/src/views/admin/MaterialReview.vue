<template>
  <div class="material-review">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>教学材料审核</span>
        </div>
      </template>
      
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="学年">
            <el-select v-model="queryParams.academicYear" placeholder="请选择学年" clearable style="width: 250px;" v-bind="safeElementConfig">
              <el-option v-for="year in academicYears" :key="year" :label="year" :value="year"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="学期">
            <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable style="width: 250px;" v-bind="safeElementConfig">
              <el-option label="第一学期" :value="1"></el-option>
              <el-option label="第二学期" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 250px;" v-bind="safeElementConfig">
              <el-option label="待审核" :value="0"></el-option>
              <el-option label="课程负责人审核中" :value="1"></el-option>
              <el-option label="专业负责人审核中" :value="2"></el-option>
              <el-option label="副院长审核中" :value="3"></el-option>
              <el-option label="教务人员审核中" :value="4"></el-option>
              <el-option label="审核通过" :value="5"></el-option>
              <el-option label="审核不通过" :value="6"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchReviews">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="reviews" style="width: 100%" v-loading="loading">
        <el-table-column prop="material_name" label="材料名称" width="180"></el-table-column>
        <el-table-column prop="course_name" label="所属课程" width="180"></el-table-column>
        <el-table-column prop="material_type_name" label="材料类型"></el-table-column>
        <el-table-column prop="academic_year" label="学年"></el-table-column>
        <el-table-column prop="semester" label="学期">
          <template #default="scope">
            {{ scope.row.semester === 1 ? '第一学期' : '第二学期' }}
          </template>
        </el-table-column>
        <el-table-column prop="submit_user_name" label="提交人"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="提交时间"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="text" size="small" @click="viewReviewDetail(scope.row.id)">查看详情</el-button>
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
    
    <!-- 审核详情对话框 -->
    <el-dialog title="审核详情" v-model="detailDialogVisible" width="60%" v-bind="safeElementConfig">
      <div v-if="reviewDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="材料名称">{{ reviewDetail.material_name }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ reviewDetail.course_name }}</el-descriptions-item>
          <el-descriptions-item label="材料类型">{{ reviewDetail.material_type_name }}</el-descriptions-item>
          <el-descriptions-item label="学年/学期">{{ reviewDetail.academic_year }} / {{ reviewDetail.semester === 1 ? '第一学期' : '第二学期' }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ reviewDetail.submit_user_name }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ reviewDetail.create_time }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(reviewDetail.status)">{{ getStatusText(reviewDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前审核人">{{ reviewDetail.current_reviewer_name }}</el-descriptions-item>
          <el-descriptions-item label="材料描述" :span="2">{{ reviewDetail.material_description }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="file-info">
          <h3>材料文件</h3>
          <p>文件名：{{ reviewDetail.material_name }}</p>
          <p>文件大小：{{ formatFileSize(reviewDetail.file_size) }}</p>
          <p>文件类型：{{ reviewDetail.file_type }}</p>
          <el-button type="primary" size="small" @click="downloadFile(reviewDetail.material_id)">下载文件</el-button>
        </div>
        
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

        <!-- 审核操作区域 -->
        <div class="review-actions" v-if="reviewDetail.status !== 5 && reviewDetail.status !== 6">
          <el-divider></el-divider>
          <h3>审核操作</h3>
          <el-form :model="reviewForm" label-width="100px">
            <el-form-item label="审核意见">
              <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请输入审核意见"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="handleReview(1)">通过</el-button>
              <el-button type="danger" @click="handleReview(0)">不通过</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { safeElementConfig } from '@/utils/resizeObserverFix'

export default {
  name: 'AdminMaterialReview',
  setup() {
    // 检查用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (!userInfo.id) {
      ElMessage.error('未获取到用户信息，请重新登录');
      // 可以在这里添加重定向到登录页的逻辑
      // router.push('/login');
      return;
    }

    // 学年列表
    const academicYears = ref([])
    const generateAcademicYears = () => {
      const currentYear = new Date().getFullYear()
      const years = []
      for (let i = 0; i < 5; i++) {
        const year = currentYear - i
        years.push(`${year}-${year + 1}`)
      }
      academicYears.value = years
    }
    
    // 查询参数
    const queryParams = reactive({
      current: 1,
      size: 10,
      academicYear: null,
      semester: null,
      status: null
    })
    
    const reviews = ref([])
    const total = ref(0)
    const loading = ref(false)
    
    const fetchReviews = async () => {
      loading.value = true
      try {
        const response = await axios.get('/api/material-review/admin/list', {
          params: queryParams
        })
        
        if (response.data && response.data.data) {
          reviews.value = response.data.data.records || []
          total.value = response.data.data.total || 0
        } else {
          reviews.value = []
          total.value = 0
        }
      } catch (error) {
        console.error('获取审核列表失败', error)
        ElMessage.error('获取审核列表失败')
        reviews.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }
    
    const handleCurrentChange = (current) => {
      queryParams.current = current
      fetchReviews()
    }
    
    // 审核详情
    const detailDialogVisible = ref(false)
    const reviewDetail = ref(null)
    const reviewRecords = ref([])
    
    const viewReviewDetail = async (reviewId) => {
      try {
        const detailResponse = await axios.get(`/api/material-review/detail/${reviewId}`)
        if (detailResponse.data && detailResponse.data.data) {
          reviewDetail.value = detailResponse.data.data
          
          const recordsResponse = await axios.get(`/api/material-review/records/${reviewId}`)
          if (recordsResponse.data && recordsResponse.data.data) {
            reviewRecords.value = recordsResponse.data.data || []
          } else {
            reviewRecords.value = []
          }
          
          detailDialogVisible.value = true
        } else {
          ElMessage.error('获取审核详情失败')
        }
      } catch (error) {
        console.error('获取审核详情失败', error)
        ElMessage.error('获取审核详情失败')
        reviewRecords.value = []
      }
    }
    
    // 工具函数
    const getStatusText = (status) => {
      switch (status) {
        case 0: return '待审核'
        case 1: return '课程负责人审核中'
        case 2: return '专业负责人审核中'
        case 3: return '副院长审核中'
        case 4: return '教务人员审核中'
        case 5: return '审核通过'
        case 6: return '审核不通过'
        default: return '未知状态'
      }
    }
    
    const getStatusType = (status) => {
      switch (status) {
        case 0: return 'info'
        case 1: case 2: case 3: case 4: return 'warning'
        case 5: return 'success'
        case 6: return 'danger'
        default: return 'info'
      }
    }
    
    const getReviewerTitleText = (title) => {
      switch (title) {
        case 0: return '普通教师'
        case 1: return '课程负责人'
        case 2: return '专业负责人'
        case 3: return '副院长'
        case 4: return '教务人员'
        default: return '未知职称'
      }
    }
    
    const formatFileSize = (size) => {
      if (!size) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let index = 0
      let fileSize = size
      while (fileSize >= 1024 && index < units.length - 1) {
        fileSize /= 1024
        index++
      }
      return fileSize.toFixed(2) + ' ' + units[index]
    }
    
    const downloadFile = async (materialId) => {
      if (!materialId) {
        ElMessage.warning('材料ID为空')
        return
      }
      
      try {
        // 添加日志，跟踪下载过程
        console.log(`开始下载材料，ID: ${materialId}`)
        
        const response = await axios.get(`/api/material/download/${materialId}`, {
          responseType: 'blob'
        })
        
        // 记录响应头信息，用于调试
        console.log('下载响应头:', response.headers)
        
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        
        // 获取Content-Disposition头部
        const contentDisposition = response.headers['content-disposition']
        console.log('Content-Disposition头:', contentDisposition)
        
        // 从reviewDetail中获取默认文件名（后备方案）
        let defaultFileName = reviewDetail.value?.material_name || '下载文件'
        const fileType = reviewDetail.value?.file_type || ''
        if (fileType && !defaultFileName.toLowerCase().endsWith('.' + fileType.toLowerCase())) {
          defaultFileName += '.' + fileType
        }
        
        // 尝试从Content-Disposition头中提取文件名
        let filename = defaultFileName
        
        if (contentDisposition) {
          // 尝试提取filename*参数（RFC 5987格式）
          const filenameStarMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
          if (filenameStarMatch && filenameStarMatch[1]) {
            try {
              // 对URL编码的文件名进行解码
              filename = decodeURIComponent(filenameStarMatch[1])
              console.log('从filename*参数解析的文件名:', filename)
            } catch (error) {
              console.error('解码filename*参数出错:', error)
            }
          } else {
            // 尝试提取普通filename参数
            const filenameMatch = contentDisposition.match(/filename=(?:"([^"]+)"|([^;]+))/i)
            if (filenameMatch) {
              // 获取引号中或非引号的值
              const extractedName = filenameMatch[1] || filenameMatch[2]
              if (extractedName) {
                try {
                  // 可能需要URL解码
                  filename = decodeURIComponent(extractedName)
                  console.log('从filename参数解析的文件名:', filename)
                } catch (error) {
                  // 如果解码失败，使用原始值
                  filename = extractedName
                  console.log('使用未解码的filename参数:', filename)
                }
              }
            }
          }
        } else {
          console.warn('响应头中缺少Content-Disposition')
        }
        
        // 确保文件名中包含扩展名
        const contentType = response.headers['content-type']
        if (contentType && !filename.includes('.')) {
          // 从contentType获取扩展名
          const extension = getExtensionFromMimeType(contentType)
          if (extension) {
            filename += extension
            console.log(`根据Content-Type添加扩展名: ${extension}, 最终文件名: ${filename}`)
          }
        }
        
        console.log('最终下载文件名:', filename)
        
        // 设置下载文件名
        link.download = filename
        document.body.appendChild(link)
        link.click()
        
        // 清理
        setTimeout(() => {
          window.URL.revokeObjectURL(url)
          document.body.removeChild(link)
        }, 100)
        
        // 更新下载次数
        axios.post(`/api/material/download/${materialId}`)
          .catch(error => console.error('更新下载次数失败:', error))
        
        ElMessage.success('文件下载成功')
      } catch (error) {
        console.error('下载文件失败:', error)
        ElMessage.error('下载文件失败')
      }
    }
    
    // 审核表单数据
    const reviewForm = ref({
      comment: ''
    });

    // 处理审核操作
    const handleReview = async (result) => {
      if (!reviewForm.value.comment) {
        ElMessage.warning('请输入审核意见');
        return;
      }

      // 从localStorage获取用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      if (!userInfo.id) {
        ElMessage.error('未获取到用户信息，请重新登录');
        return;
      }

      try {
        // 获取用户角色和职称
        const role = parseInt(userInfo.role || '0');
        let reviewerTitle = 0; // 默认为普通教师

        // 根据角色设置职称
        if (role === 2) { // 管理员
          reviewerTitle = 4; // 教务人员
        } else {
          // 获取当前审核状态
          const status = parseInt(reviewDetail.value.status);
          switch (status) {
            case 1: // 课程负责人审核中
              reviewerTitle = 1;
              break;
            case 2: // 专业负责人审核中
              reviewerTitle = 2;
              break;
            case 3: // 副院长审核中
              reviewerTitle = 3;
              break;
            case 4: // 教务人员审核中
              reviewerTitle = 4;
              break;
          }
        }

        console.log('发送审核请求，参数：', {
          reviewId: parseInt(reviewDetail.value.id),
          reviewerId: parseInt(userInfo.id),
          reviewerTitle: parseInt(reviewerTitle),
          result: parseInt(result),
          comment: reviewForm.value.comment
        });

        const response = await axios.post('/api/material-review/review', {
          reviewId: parseInt(reviewDetail.value.id),
          reviewerId: parseInt(userInfo.id),
          reviewerTitle: parseInt(reviewerTitle),
          result: parseInt(result),
          comment: reviewForm.value.comment
        });

        if (response.data && response.data.code === 200) {
          ElMessage.success('审核操作成功');
          detailDialogVisible.value = false;
          fetchReviews(); // 刷新列表
        } else {
          ElMessage.error(response.data.message || '审核操作失败');
        }
      } catch (error) {
        console.error('审核操作失败', error);
        ElMessage.error('审核操作失败');
      }
    };
    
    // 根据MIME类型获取文件扩展名
    const getExtensionFromMimeType = (mimeType) => {
      const mimeToExt = {
        'application/pdf': '.pdf',
        'application/msword': '.doc',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx',
        'application/vnd.ms-excel': '.xls',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '.xlsx',
        'application/vnd.ms-powerpoint': '.ppt',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation': '.pptx',
        'text/plain': '.txt',
        'image/jpeg': '.jpg',
        'image/png': '.png',
        'image/gif': '.gif',
        'application/zip': '.zip',
        'application/x-rar-compressed': '.rar',
        'application/x-7z-compressed': '.7z'
      };
      
      return mimeToExt[mimeType] || '';
    }
    
    onMounted(() => {
      generateAcademicYears()
      fetchReviews()
    })
    
    return {
      academicYears,
      queryParams,
      reviews,
      total,
      loading,
      fetchReviews,
      handleCurrentChange,
      detailDialogVisible,
      reviewDetail,
      reviewRecords,
      viewReviewDetail,
      getStatusText,
      getStatusType,
      getReviewerTitleText,
      formatFileSize,
      downloadFile,
      reviewForm,
      handleReview,
      getExtensionFromMimeType,
      safeElementConfig
    }
  }
}
</script>

<style scoped>
.material-review {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.file-info {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.review-records {
  margin-top: 20px;
}

.review-actions {
  margin-top: 20px;
}

:deep(.el-select-dropdown__item) {
  white-space: normal;
  height: auto;
  line-height: 1.5;
  padding: 8px 20px;
}
</style> 