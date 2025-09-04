<template>
  <div class="material-statistics">
    <h1 class="page-title">教学材料统计与分析</h1>
    
    <!-- 过滤条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="材料类型">
          <el-select v-model="queryParams.materialTypeId" placeholder="请选择材料类型" clearable style="width: 200px;" v-bind="safeElementConfig">
            <el-option v-for="item in materialTypes" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学年">
          <el-select v-model="queryParams.academicYear" placeholder="请选择学年" clearable style="width: 200px;" v-bind="safeElementConfig">
            <el-option v-for="year in academicYears" :key="year" :label="year" :value="year"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable style="width: 200px;" v-bind="safeElementConfig">
            <el-option :label="'第一学期'" :value="1"></el-option>
            <el-option :label="'第二学期'" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchStatistics">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 统计概览 -->
    <el-row :gutter="20" class="statistics-overview">
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-title">上传材料总数</div>
            <div class="overview-number">{{ overview.totalMaterials || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-title">已完成教师数</div>
            <div class="overview-number">{{ overview.completedTeachers || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-title">未完成教师数</div>
            <div class="overview-number">{{ overview.uncompletedTeachers || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 统计图表 -->
    <el-row :gutter="20" class="statistics-charts">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>按材料类型统计</span>
            </div>
          </template>
          <div class="chart-container" ref="typeChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>按学院统计</span>
            </div>
          </template>
          <div class="chart-container" ref="collegeChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 未完成教师列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>未完成教师列表</span>
          <el-button size="small" type="warning" @click="handleRemindAll">一键提醒</el-button>
        </div>
      </template>
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="uncompletedTeachers.length === 0" class="empty-data">
        <el-empty description="暂无未完成的教师" />
      </div>
      <el-table v-else :data="uncompletedTeachers" style="width: 100%">
        <el-table-column prop="teacherId" label="教师ID" width="80"></el-table-column>
        <el-table-column prop="teacherName" label="教师姓名"></el-table-column>
        <el-table-column prop="courseName" label="课程名称"></el-table-column>
        <el-table-column prop="collegeName" label="所属学院"></el-table-column>
        <el-table-column prop="materialTypeName" label="材料类型"></el-table-column>
        <el-table-column prop="academicYear" label="学年"></el-table-column>
        <el-table-column prop="semester" label="学期">
          <template #default="scope">
            {{ scope.row.semester === 1 ? '第一学期' : '第二学期' }}
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="warning" @click="handleRemind(scope.row.teacherId, scope.row.taskId)">提醒</el-button>
              <el-button size="small" type="primary" @click="handleViewTask(scope.row.taskId)">查看任务</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';
import * as echarts from 'echarts';
import { safeElementConfig } from '@/utils/resizeObserverFix'

const router = useRouter();
const loading = ref(false);
const uncompletedTeachers = ref([]);
const total = ref(0);

// 图表引用
const typeChartRef = ref(null);
const collegeChartRef = ref(null);
let typeChart = null;
let collegeChart = null;

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  materialTypeId: '',
  academicYear: '',
  semester: ''
});

// 统计概览数据
const overview = reactive({
  totalMaterials: 0,
  completedTeachers: 0,
  uncompletedTeachers: 0
});

// 选项列表
const materialTypes = ref([]);
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

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true;
  try {
    // 获取概览数据
    const overviewResponse = await axios.get('/api/admin/material-statistics/overview', {
      params: {
        materialTypeId: queryParams.materialTypeId,
        academicYear: queryParams.academicYear,
        semester: queryParams.semester
      }
    });
    
    if (overviewResponse.data.code === 200 && overviewResponse.data.data) {
      Object.assign(overview, overviewResponse.data.data);
    }
    
    // 获取未完成教师列表
    const teachersResponse = await axios.get('/api/admin/material-statistics/uncompleted-teachers', {
      params: queryParams
    });
    
    if (teachersResponse.data.code === 200) {
      uncompletedTeachers.value = teachersResponse.data.data.records || [];
      total.value = teachersResponse.data.data.total || 0;
    }
    
    // 获取按材料类型统计数据
    const typeResponse = await axios.get('/api/admin/material-statistics/by-type');
    if (typeResponse.data.code === 200 && typeResponse.data.data) {
      await nextTick();
      initTypeChart(typeResponse.data.data);
    }
    
    // 获取按学院统计数据
    const collegeResponse = await axios.get('/api/admin/material-statistics/by-college');
    if (collegeResponse.data.code === 200 && collegeResponse.data.data) {
      await nextTick();
      initCollegeChart(collegeResponse.data.data);
    }
  } catch (error) {
    console.error('获取统计数据出错:', error);
    ElMessage.error('获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryParams, {
    current: 1,
    size: 10,
    materialTypeId: '',
    academicYear: '',
    semester: ''
  });
  fetchStatistics();
};

// 初始化按材料类型统计图表
const initTypeChart = (data) => {
  if (!typeChartRef.value) return;
  
  if (typeChart) {
    typeChart.dispose();
  }
  
  typeChart = echarts.init(typeChartRef.value);
  
  const names = data.map(item => item.name);
  const completed = data.map(item => item.completed);
  const uncompleted = data.map(item => item.uncompleted);
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['已完成', '未完成']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: names
    },
    series: [
      {
        name: '已完成',
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        emphasis: {
          focus: 'series'
        },
        data: completed
      },
      {
        name: '未完成',
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        emphasis: {
          focus: 'series'
        },
        data: uncompleted
      }
    ]
  };
  
  typeChart.setOption(option);
  
  window.addEventListener('resize', () => {
    typeChart && typeChart.resize();
  });
};

// 初始化按学院统计图表
const initCollegeChart = (data) => {
  if (!collegeChartRef.value) return;
  
  if (collegeChart) {
    collegeChart.dispose();
  }
  
  collegeChart = echarts.init(collegeChartRef.value);
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '未完成比例',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data.map(item => ({
          value: item.count,
          name: item.name
        }))
      }
    ]
  };
  
  collegeChart.setOption(option);
  
  window.addEventListener('resize', () => {
    collegeChart && collegeChart.resize();
  });
};

// 提醒单个教师
const handleRemind = async (teacherId, taskId) => {
  try {
    const response = await axios.post('/api/admin/material-task/remind', {
      teacherId,
      taskId
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

// 一键提醒所有未完成的教师
const handleRemindAll = async () => {
  if (uncompletedTeachers.value.length === 0) {
    ElMessage.warning('没有需要提醒的教师');
    return;
  }
  
  ElMessageBox.confirm(`确定要提醒所有 ${uncompletedTeachers.value.length} 位未完成的教师吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const taskTeacherPairs = uncompletedTeachers.value.map(teacher => ({
        taskId: teacher.taskId,
        teacherId: teacher.teacherId
      }));
      
      const response = await axios.post('/api/admin/material-task/remind-batch', {
        reminders: taskTeacherPairs
      });
      
      if (response.data.code === 200) {
        ElMessage.success('批量发送提醒成功');
      } else {
        ElMessage.error(response.data.message || '批量发送提醒失败');
      }
    } catch (error) {
      console.error('批量发送提醒出错:', error);
      ElMessage.error('批量发送提醒失败，请稍后重试');
    }
  }).catch(() => {});
};

// 查看任务详情
const handleViewTask = (taskId) => {
  router.push({
    path: '/admin/material-tasks',
    query: { taskId }
  });
};

// 分页器相关方法
const handleSizeChange = (size) => {
  queryParams.size = size;
  fetchStatistics();
};

const handleCurrentChange = (current) => {
  queryParams.current = current;
  fetchStatistics();
};

onMounted(() => {
  generateAcademicYears();
  fetchMaterialTypes();
  fetchStatistics();
  
  // 注册窗口大小变化事件
  window.addEventListener('resize', () => {
    typeChart && typeChart.resize();
    collegeChart && collegeChart.resize();
  });
});

// 组件卸载时清理图表
onUnmounted(() => {
  if (typeChart) {
    typeChart.dispose();
    typeChart = null;
  }
  
  if (collegeChart) {
    collegeChart.dispose();
    collegeChart = null;
  }
  
  window.removeEventListener('resize', () => {
    typeChart && typeChart.resize();
    collegeChart && collegeChart.resize();
  });
});
</script>

<style scoped>
.material-statistics {
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

.statistics-overview {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.overview-content {
  text-align: center;
}

.overview-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.overview-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}

.statistics-charts {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 350px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.loading-container, .empty-data {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 