<template>
  <div class="material-review">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>教学材料审核</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我提交的审核" name="submit">
          <div class="filter-container">
            <el-form :inline="true" :model="submitQueryParams" class="demo-form-inline">
              <el-form-item label="学年">
                <el-select v-model="submitQueryParams.academicYear" placeholder="请选择学年" clearable style="width: 250px;" v-bind="safeElementConfig">
                  <el-option v-for="year in academicYears" :key="year" :label="year" :value="year"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="学期">
                <el-select v-model="submitQueryParams.semester" placeholder="请选择学期" clearable style="width: 250px;" v-bind="safeElementConfig">
                  <el-option label="第一学期" :value="1"></el-option>
                  <el-option label="第二学期" :value="2"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="submitQueryParams.status" placeholder="请选择状态" clearable style="width: 250px;" v-bind="safeElementConfig">
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
                <el-button type="primary" @click="fetchSubmitReviews">查询</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table :data="submitReviews" style="width: 100%" v-loading="submitLoading" @resize="onTableResize" v-if="submitTableVisible">
            <el-table-column prop="material_name" label="材料名称" width="180"></el-table-column>
            <el-table-column prop="course_name" label="所属课程" width="180"></el-table-column>
            <el-table-column prop="material_type_name" label="材料类型"></el-table-column>
            <el-table-column prop="academic_year" label="学年"></el-table-column>
            <el-table-column prop="semester" label="学期">
              <template #default="scope">
                {{ scope.row.semester === 1 ? '第一学期' : '第二学期' }}
              </template>
            </el-table-column>
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
              :total="submitTotal"
              :page-size="submitQueryParams.size"
              :current-page="submitQueryParams.current"
              @current-change="handleSubmitCurrentChange"
            ></el-pagination>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="待我审核" name="pending">
          <div class="filter-container">
            <el-form :inline="true" :model="pendingQueryParams" class="demo-form-inline">
              <el-form-item label="学年">
                <el-select v-model="pendingQueryParams.academicYear" placeholder="请选择学年" clearable style="width: 250px;" v-bind="safeElementConfig">
                  <el-option v-for="year in academicYears" :key="year" :label="year" :value="year"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="学期">
                <el-select v-model="pendingQueryParams.semester" placeholder="请选择学期" clearable style="width: 250px;" v-bind="safeElementConfig">
                  <el-option label="第一学期" :value="1"></el-option>
                  <el-option label="第二学期" :value="2"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="fetchPendingReviews">查询</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table :data="pendingReviews" style="width: 100%" v-loading="pendingLoading" @resize="onTableResize" v-if="pendingTableVisible">
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
                <el-button type="text" size="small" @click="reviewMaterial(scope.row.id)">审核</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :total="pendingTotal"
              :page-size="pendingQueryParams.size"
              :current-page="pendingQueryParams.current"
              @current-change="handlePendingCurrentChange"
            ></el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 审核详情对话框 -->
    <el-dialog 
      title="审核详情" 
      v-model="detailDialogVisible" 
      width="60%"
      @close="handleDialogClose"
      v-bind="safeElementConfig">
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
          <el-table 
            :data="reviewRecords" 
            style="width: 100%"
            @resize="onTableResize"
            v-if="reviewRecordTableVisible">
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
    
    <!-- 审核对话框 -->
    <el-dialog 
      title="审核材料" 
      v-model="reviewDialogVisible" 
      width="50%"
      @close="handleDialogClose"
      v-bind="safeElementConfig">
      <div v-if="reviewDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="材料名称">{{ reviewDetail.material_name }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ reviewDetail.course_name }}</el-descriptions-item>
          <el-descriptions-item label="材料类型">{{ reviewDetail.material_type_name }}</el-descriptions-item>
          <el-descriptions-item label="学年/学期">{{ reviewDetail.academic_year }} / {{ reviewDetail.semester === 1 ? '第一学期' : '第二学期' }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ reviewDetail.submit_user_name }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ reviewDetail.create_time }}</el-descriptions-item>
          <el-descriptions-item label="材料描述" :span="2">{{ reviewDetail.material_description }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="file-info">
          <h3>材料文件</h3>
          <p>文件名：{{ reviewDetail.material_name }}</p>
          <p>文件大小：{{ formatFileSize(reviewDetail.file_size) }}</p>
          <p>文件类型：{{ reviewDetail.file_type }}</p>
          <el-button type="primary" size="small" @click="downloadFile(reviewDetail.material_id)">下载文件</el-button>
        </div>
        
        <el-form :model="reviewForm" label-width="100px" class="review-form">
          <el-form-item label="审核结果" required>
            <el-radio-group v-model="reviewForm.result">
              <el-radio :label="1">通过</el-radio>
              <el-radio :label="0">不通过</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见" required>
            <el-input v-model="reviewForm.comment" type="textarea" :rows="4" placeholder="请输入审核意见"></el-input>
          </el-form-item>
        </el-form>
        
        <div class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="submitReviewLoading">提交审核</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, nextTick, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { useStore } from 'vuex';
import { safeElementConfig } from '@/utils/resizeObserverFix'

export default {
  name: 'MaterialReview',
  setup() {
    const store = useStore();
    const activeTab = ref('submit');
    
    // 当前用户信息
    const currentUser = computed(() => store.state.user);
    
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
    
    // 我提交的审核
    const submitQueryParams = reactive({
      current: 1,
      size: 10,
      teacherId: null,
      academicYear: null,
      semester: null,
      status: null
    });
    const submitReviews = ref([]);
    const submitTotal = ref(0);
    const submitLoading = ref(false);
    
    const submitTableVisible = ref(true);
    
    const fetchSubmitReviews = async () => {
      submitLoading.value = true;
      try {
        // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
        let teacherId = currentUser.value?.id;
        if (!teacherId) {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          teacherId = userInfo.id;
        }
        
        if (!teacherId) {
          ElMessage.warning('无法获取用户信息，请重新登录');
          submitLoading.value = false;
          return;
        }
        
        submitQueryParams.teacherId = teacherId;
        console.log('提交的审核查询参数:', submitQueryParams);
        
        const response = await axios.get('/api/material-review/teacher-submit', {
          params: submitQueryParams
        });
        console.log('提交的审核响应:', response.data);
        
        if (response.data && response.data.data) {
          submitReviews.value = response.data.data.records || [];
          submitTotal.value = response.data.data.total || 0;
        } else {
          submitReviews.value = [];
          submitTotal.value = 0;
        }
      } catch (error) {
        console.error('获取提交的审核列表失败', error);
        ElMessage.error('获取提交的审核列表失败');
        submitReviews.value = [];
        submitTotal.value = 0;
      } finally {
        submitLoading.value = false;
      }
    };
    
    const handleSubmitCurrentChange = (current) => {
      submitQueryParams.current = current;
      fetchSubmitReviews();
    };
    
    // 待我审核
    const pendingQueryParams = reactive({
      current: 1,
      size: 10,
      reviewerId: null,
      academicYear: null,
      semester: null
    });
    const pendingReviews = ref([]);
    const pendingTotal = ref(0);
    const pendingLoading = ref(false);
    
    const pendingTableVisible = ref(true);
    
    const fetchPendingReviews = async () => {
      pendingLoading.value = true;
      try {
        // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
        let reviewerId = currentUser.value?.id;
        if (!reviewerId) {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          reviewerId = userInfo.id;
        }
        
        if (!reviewerId) {
          ElMessage.warning('无法获取用户信息，请重新登录');
          pendingLoading.value = false;
          return;
        }
        
        pendingQueryParams.reviewerId = reviewerId;
        console.log('待审核查询参数:', pendingQueryParams);
        
        // 检查当前用户是否为专业负责人
        let isMajorLeader = false;
        try {
          // 获取用户职称信息
          const titlesResponse = await axios.get('/api/teacher/titles', {
            params: { teacherId: reviewerId }
          });
          
          if (titlesResponse.data && titlesResponse.data.code === 200 && titlesResponse.data.data) {
            const titles = titlesResponse.data.data;
            // 检查是否有专业负责人职称(titleType=2)
            isMajorLeader = titles.some(title => title.titleType === 2);
            console.log('当前用户是否为专业负责人:', isMajorLeader);
          }
        } catch (error) {
          console.warn('获取用户职称信息失败:', error);
        }
        
        const response = await axios.get('/api/material-review/teacher-pending', {
          params: pendingQueryParams
        });
        console.log('待审核响应:', response.data);
        
        if (response.data && response.data.data) {
          // 获取原始数据
          const rawRecords = response.data.data.records || [];
          
          // 对于专业负责人，可能存在重复记录的问题，进行数据去重
          // 使用Map来存储唯一的记录，以id为键
          const uniqueRecordsMap = new Map();
          
          for (const record of rawRecords) {
            // 只有当Map中不存在该id的记录时才添加
            if (!uniqueRecordsMap.has(record.id)) {
              uniqueRecordsMap.set(record.id, record);
            }
          }
          
          // 将Map转换回数组
          pendingReviews.value = Array.from(uniqueRecordsMap.values());
          pendingTotal.value = response.data.data.total || 0;
          
          // 如果去重后数量减少，更新total
          if (pendingReviews.value.length < rawRecords.length) {
            console.log(`检测到并移除了 ${rawRecords.length - pendingReviews.value.length} 条重复记录`);
            
            // 专业负责人问题的诊断日志
            if (isMajorLeader) {
              console.log('专业负责人模式: 记录重复问题诊断');
              
              // 创建一个ID出现次数的映射
              const idCounts = {};
              rawRecords.forEach(record => {
                idCounts[record.id] = (idCounts[record.id] || 0) + 1;
              });
              
              // 找出出现多次的ID
              const duplicateIds = Object.entries(idCounts)
                .filter(([, count]) => count > 1)
                .map(([id, count]) => ({ id, count }));
                
              if (duplicateIds.length > 0) {
                console.log('重复ID列表:', duplicateIds);
                
                // 对于每个重复的ID，输出它们的详细信息以找出差异
                duplicateIds.forEach(({ id }) => {
                  const duplicates = rawRecords.filter(record => record.id.toString() === id);
                  console.log(`ID ${id} 的重复记录:`, duplicates);
                });
              }
            }
            
            pendingTotal.value = pendingReviews.value.length;
          }
        } else {
          pendingReviews.value = [];
          pendingTotal.value = 0;
        }
      } catch (error) {
        console.error('获取待审核列表失败', error);
        ElMessage.error('获取待审核列表失败');
        pendingReviews.value = [];
        pendingTotal.value = 0;
      } finally {
        pendingLoading.value = false;
      }
    };
    
    const handlePendingCurrentChange = (current) => {
      pendingQueryParams.current = current;
      fetchPendingReviews();
    };
    
    // 审核详情
    const detailDialogVisible = ref(false);
    const reviewDetail = ref(null);
    const reviewRecords = ref([]);
    const reviewRecordTableVisible = ref(true);
    
    const viewReviewDetail = async (reviewId) => {
      try {
        const detailResponse = await axios.get(`/api/material-review/detail/${reviewId}`);
        console.log('审核详情响应:', detailResponse.data);
        if (detailResponse.data && detailResponse.data.data) {
          reviewDetail.value = detailResponse.data.data;
          console.log('材料名称:', reviewDetail.value.material_name);
          console.log('材料ID:', reviewDetail.value.material_id);
          
          const recordsResponse = await axios.get(`/api/material-review/records/${reviewId}`);
          console.log('审核记录响应:', recordsResponse.data);
          if (recordsResponse.data && recordsResponse.data.data) {
            reviewRecords.value = recordsResponse.data.data || [];
          } else {
            reviewRecords.value = [];
          }
          
          detailDialogVisible.value = true;
          // 延迟渲染表格以防止ResizeObserver错误
          reviewRecordTableVisible.value = false;
          await nextTick();
          reviewRecordTableVisible.value = true;
        } else {
          ElMessage.error('获取审核详情失败');
        }
      } catch (error) {
        console.error('获取审核详情失败', error);
        ElMessage.error('获取审核详情失败');
        reviewRecords.value = [];
      }
    };
    
    // 审核材料
    const reviewDialogVisible = ref(false);
    const reviewForm = reactive({
      reviewId: null,
      reviewerId: null,
      reviewerTitle: null,
      result: 1,
      comment: ''
    });
    const submitReviewLoading = ref(false);
    
    const reviewMaterial = async (reviewId) => {
      try {
        const response = await axios.get(`/api/material-review/detail/${reviewId}`);
        console.log('审核材料详情响应:', response.data);
        if (response.data && response.data.data) {
          reviewDetail.value = response.data.data;
          
          // 首先尝试从Vuex获取用户ID，如果不存在则从localStorage获取
          let reviewerId = currentUser.value?.id;
          if (!reviewerId) {
            const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
            reviewerId = userInfo.id;
          }
          
          if (!reviewerId) {
            ElMessage.warning('无法获取用户信息，请重新登录');
            return;
          }
          
          reviewForm.reviewId = reviewId;
          reviewForm.reviewerId = reviewerId;
          
          // 根据当前审核状态设置审核人职称
          switch (reviewDetail.value.status) {
            case 1: // 课程负责人审核中
              reviewForm.reviewerTitle = 1;
              break;
            case 2: // 专业负责人审核中
              reviewForm.reviewerTitle = 2;
              break;
            case 3: // 副院长审核中
              reviewForm.reviewerTitle = 3;
              break;
            case 4: // 教务人员审核中
              reviewForm.reviewerTitle = 4;
              break;
            default:
              reviewForm.reviewerTitle = 0;
          }
          
          reviewForm.result = 1;
          reviewForm.comment = '';
          
          reviewDialogVisible.value = true;
        } else {
          ElMessage.error('获取审核详情失败');
        }
      } catch (error) {
        console.error('获取审核详情失败', error);
        ElMessage.error('获取审核详情失败');
      }
    };
    
    const submitReview = async () => {
      if (!reviewForm.comment) {
        ElMessage.warning('请输入审核意见');
        return;
      }
      
      submitReviewLoading.value = true;
      try {
        const response = await axios.post('/api/material-review/review', reviewForm);
        if (response.data.code === 200) {
          ElMessage.success('审核提交成功');
          reviewDialogVisible.value = false;
          fetchPendingReviews();
        } else {
          ElMessage.error(response.data.message || '审核提交失败');
        }
      } catch (error) {
        console.error('审核提交失败', error);
        ElMessage.error('审核提交失败');
      } finally {
        submitReviewLoading.value = false;
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
    
    const formatFileSize = (size) => {
      if (!size) return '0 B';
      const units = ['B', 'KB', 'MB', 'GB', 'TB'];
      let index = 0;
      let fileSize = size;
      while (fileSize >= 1024 && index < units.length - 1) {
        fileSize /= 1024;
        index++;
      }
      return fileSize.toFixed(2) + ' ' + units[index];
    };
    
    const downloadFile = async (materialId) => {
      if (!materialId) {
        ElMessage.warning('材料ID为空');
        return;
      }
      
      try {
        console.log('下载材料ID:', materialId);
        // 使用axios下载文件
        const response = await axios.get(`/api/material/download/${materialId}`, {
          responseType: 'blob'
        });
        
        // 创建Blob对象
        const blob = new Blob([response.data]);
        
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        
        // 从响应头中获取文件名
        const contentDisposition = response.headers['content-disposition'];
        console.log('Content-Disposition头部:', contentDisposition);
        let filename = '未知文件';
        
        // 首先尝试从审核详情中获取材料名称
        const hasReviewDetail = typeof reviewDetail !== 'undefined' && reviewDetail.value && reviewDetail.value.material_name;
        const reviewDetailName = hasReviewDetail ? reviewDetail.value.material_name : null;
        
        if (contentDisposition) {
          if (reviewDetailName && reviewDetailName.trim() !== '') {
            console.log('使用审核详情中的材料名称:', reviewDetailName);
            filename = reviewDetailName;
          } else {
            // RFC 8187/5987格式: filename*=UTF-8''encoded-value
            const filenameStarMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
            if (filenameStarMatch && filenameStarMatch[1]) {
              // 解码UTF-8编码的文件名
              filename = decodeURIComponent(filenameStarMatch[1].replace(/\+/g, ' '));
              console.log('从RFC 5987格式解析的文件名:', filename);
            } else {
              // 尝试RFC 6266建议的传统格式: filename="value"
              const filenameMatch = contentDisposition.match(/filename="([^"]*?)"/i);
              if (filenameMatch && filenameMatch[1]) {
                filename = filenameMatch[1];
                console.log('从带引号格式解析的文件名:', filename);
              } else {
                // 尝试无引号格式: filename=value
                const noQuotesMatch = contentDisposition.match(/filename=([^;,\s]+)/i);
                if (noQuotesMatch && noQuotesMatch[1]) {
                  filename = noQuotesMatch[1].trim();
                  console.log('从无引号格式解析的文件名:', filename);
                }
              }
            }
          }
        } else {
          // 如果没有Content-Disposition头部，但有审核详情
          if (hasReviewDetail) {
            filename = reviewDetailName;
            console.log('使用审核详情中的材料名称:', filename);
          }
        }
        
        // 确保从MIME类型猜测正确的文件扩展名
        const contentType = response.headers['content-type'];
        console.log('Content-Type:', contentType);
        if (contentType) {
          // 如果文件名没有扩展名或者仍然是默认的"未知文件"
          if (!filename.includes('.') || filename === '未知文件') {
            const extension = getExtensionFromMimeType(contentType);
            if (extension) {
              filename += extension;
              console.log('添加扩展名后的文件名:', filename);
            }
          }
        }
        
        // 如果文件名仍然是"未知文件"且有审核详情，使用材料名称
        if ((filename === '未知文件' || filename === '未知文件.txt') && hasReviewDetail) {
          // 确保有文件扩展名
          if (contentType && !reviewDetailName.includes('.')) {
            const extension = getExtensionFromMimeType(contentType);
            filename = reviewDetailName + (extension || '');
          } else {
            filename = reviewDetailName;
          }
          console.log('使用审核详情材料名作为最终文件名:', filename);
        }
        
        // 如果仍然没有合适的文件名，尝试从URL中提取
        if (filename === '未知文件' || filename === '未知文件.txt') {
          // 尝试从URL末尾获取文件名
          const urlParts = materialId.toString().split('/');
          const lastPart = urlParts[urlParts.length - 1];
          if (lastPart) {
            if (contentType) {
              const extension = getExtensionFromMimeType(contentType);
              filename = `教学材料_${lastPart}${extension || ''}`;
            } else {
              filename = `教学材料_${lastPart}`;
            }
            console.log('从URL生成的文件名:', filename);
          }
        }
        
        // 设置下载文件名
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        
        // 清理URL对象
        setTimeout(() => {
          window.URL.revokeObjectURL(url);
          document.body.removeChild(link);
        }, 100);
        
        // 记录下载次数
        axios.post(`/api/material/download/${materialId}`)
          .catch(error => {
            console.error('更新下载次数失败:', error);
          });
        
        ElMessage.success('文件下载成功');
      } catch (error) {
        console.error('下载文件失败:', error);
        ElMessage.error('下载文件失败');
      }
    };
    
    // 根据MIME类型获取文件扩展名
    const getExtensionFromMimeType = (mimeType) => {
      const mimeExtMap = {
        'application/pdf': '.pdf',
        'application/msword': '.doc',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx',
        'application/vnd.ms-excel': '.xls',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '.xlsx',
        'application/vnd.ms-powerpoint': '.ppt',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation': '.pptx',
        'text/plain': '.txt',
        'text/html': '.html',
        'text/xml': '.xml',
        'image/jpeg': '.jpg',
        'image/png': '.png',
        'image/gif': '.gif',
        'application/zip': '.zip',
        'application/x-rar-compressed': '.rar',
        'video/mp4': '.mp4',
        'audio/mpeg': '.mp3'
      };
      
      return mimeExtMap[mimeType] || '';
    };
    
    // 添加全局错误处理，忽略ResizeObserver错误
    const ignoreResizeObserverErrors = () => {
      const originalConsoleError = console.error;
      console.error = (...args) => {
        if (args[0] && args[0].toString().includes('ResizeObserver')) {
          return;
        }
        originalConsoleError.apply(console, args);
      };
      
      return () => {
        console.error = originalConsoleError;
      };
    };
    
    const resetResizeObserver = ignoreResizeObserverErrors();
    
    onBeforeUnmount(() => {
      resetResizeObserver();
    });
    
    const handleTabClick = (tab) => {
      if (tab.props.name === 'submit') {
        // 防止快速重复渲染导致的ResizeObserver错误
        pendingTableVisible.value = false;
        nextTick(() => {
          submitTableVisible.value = true;
          fetchSubmitReviews();
        });
      } else if (tab.props.name === 'pending') {
        submitTableVisible.value = false;
        nextTick(() => {
          pendingTableVisible.value = true;
          fetchPendingReviews();
        });
      }
    };
    
    // 添加表格resize回调
    const onTableResize = () => {
      // 通过返回Promise来延迟ResizeObserver的回调
      return new Promise(resolve => setTimeout(resolve, 0));
    };
    
    // 添加防抖函数
    const debounce = (fn, delay) => {
      let timer = null;
      return function(...args) {
        if (timer) clearTimeout(timer);
        timer = setTimeout(() => {
          fn.apply(this, args);
        }, delay);
      };
    };
    
    onMounted(() => {
      generateAcademicYears();
      fetchSubmitReviews();
      
      // 使用防抖来减少ResizeObserver警告
      const resizeHandler = debounce(() => {
        if (activeTab.value === 'submit') {
          if (submitTableVisible.value) {
            submitTableVisible.value = false;
            nextTick(() => {
              submitTableVisible.value = true;
            });
          }
        } else if (activeTab.value === 'pending') {
          if (pendingTableVisible.value) {
            pendingTableVisible.value = false;
            nextTick(() => {
              pendingTableVisible.value = true;
            });
          }
        }
      }, 300);
      
      window.addEventListener('resize', resizeHandler);
      
      onBeforeUnmount(() => {
        window.removeEventListener('resize', resizeHandler);
      });
    });
    
    // 添加对话框关闭处理，清理相关状态
    const handleDialogClose = () => {
      reviewRecordTableVisible.value = false;
      nextTick(() => {
        reviewRecordTableVisible.value = true;
      });
    };
    
    return {
      activeTab,
      academicYears,
      submitQueryParams,
      submitReviews,
      submitTotal,
      submitLoading,
      fetchSubmitReviews,
      handleSubmitCurrentChange,
      pendingQueryParams,
      pendingReviews,
      pendingTotal,
      pendingLoading,
      fetchPendingReviews,
      handlePendingCurrentChange,
      detailDialogVisible,
      reviewDetail,
      reviewRecords,
      viewReviewDetail,
      reviewDialogVisible,
      reviewForm,
      submitReviewLoading,
      reviewMaterial,
      submitReview,
      getStatusText,
      getStatusType,
      getReviewerTitleText,
      formatFileSize,
      downloadFile,
      handleTabClick,
      getExtensionFromMimeType,
      submitTableVisible,
      pendingTableVisible,
      onTableResize,
      reviewRecordTableVisible,
      handleDialogClose,
      safeElementConfig
    };
  }
};
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

.review-form {
  margin-top: 20px;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}

/* 确保下拉框中的选项文本不会被截断 */
:deep(.el-select-dropdown__item) {
  white-space: normal;
  height: auto;
  line-height: 1.5;
  padding: 8px 20px;
}
</style> 