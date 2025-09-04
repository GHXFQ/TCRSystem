<template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人信息</h2>
          <el-button type="primary" size="small" @click="goToHome">返回首页</el-button>
        </div>
      </template>
      
      <div class="profile-content">
        <div class="avatar-section">
          <div class="avatar-display">
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar" alt="用户头像" />
            <el-avatar v-else :size="100" class="user-avatar">{{ userInfo.realName?.substring(0, 1) || userInfo.username?.substring(0, 1) || 'U' }}</el-avatar>
          </div>
          
          <el-upload
            class="avatar-uploader"
            action="/api/user/upload-avatar"
            :headers="{ Authorization: token }"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            v-bind="safeElementConfig"
          >
            <el-button size="small" type="primary" class="upload-button">上传新头像</el-button>
          </el-upload>
          <div class="upload-hint">支持JPG、PNG格式，大小不超过2MB</div>
        </div>
        
        <div class="info-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item v-if="userInfo.role === 0" label="学号">{{ userInfo.studentId || userInfo.username }}</el-descriptions-item>
            <el-descriptions-item v-else-if="userInfo.role === 1" label="工号">{{ userInfo.teacherId || userInfo.username }}</el-descriptions-item>
            <el-descriptions-item v-else label="用户名">{{ userInfo.username }}</el-descriptions-item>
            
            <el-descriptions-item label="姓名">{{ userInfo.realName || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              {{ userInfo.role === 0 ? '学生' : userInfo.role === 1 ? '教师' : '管理员' }}
            </el-descriptions-item>
            <el-descriptions-item v-if="userInfo.role === 0" label="班级">{{ userInfo.className || '未设置' }}</el-descriptions-item>
            <el-descriptions-item v-if="userInfo.role === 0" label="专业">{{ userInfo.majorName || '未设置' }}</el-descriptions-item>
            <el-descriptions-item v-if="userInfo.role === 0" label="学院">{{ userInfo.collegeName || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '未设置' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <div class="password-section">
        <h3>修改密码</h3>
        <el-form 
          :model="passwordForm" 
          :rules="passwordRules" 
          ref="passwordFormRef" 
          label-width="120px"
          status-icon
        >
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              show-password
              placeholder="请输入当前密码"
              v-bind="safeElementConfig" 
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password" 
              show-password
              placeholder="请输入新密码" 
              v-bind="safeElementConfig"
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              show-password
              placeholder="请再次输入新密码" 
              v-bind="safeElementConfig"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitPasswordChange" :loading="loading">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { safeElementConfig } from '@/utils/resizeObserverFix';

const router = useRouter();
const token = localStorage.getItem('token');
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'));
const avatarUrl = ref('');
const loading = ref(false);
const passwordFormRef = ref(null);

// 返回对应用户角色的首页
const goToHome = () => {
  const role = userInfo.value.role;
  if (role === 0) {
    router.push('/student/home');
  } else if (role === 1) {
    router.push('/teacher/home');
  } else if (role === 2) {
    router.push('/admin/home');
  } else {
    router.push('/login');
  }
};

// 处理头像路径，确保使用正确的服务器地址
const getFullAvatarUrl = (path) => {
  if (!path) return '';
  // 如果已经是完整URL，则直接返回
  if (path.startsWith('http')) {
    return path;
  }
  // 否则拼接后端地址
  // 假设后端服务运行在 http://localhost:8080
  return `http://localhost:8080${path}`;
};

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码表单校验规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
};

// 头像上传前的校验
const beforeAvatarUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJpgOrPng) {
    ElMessage.error('头像只能是JPG或PNG格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过2MB!');
    return false;
  }
  return true;
};

// 头像上传成功的回调
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    const avatarPath = response.data;
    avatarUrl.value = getFullAvatarUrl(avatarPath);
    // 更新本地存储的用户信息
    const updatedUserInfo = { ...userInfo.value, avatar: avatarPath };  // 注意这里使用avatar而不是avatarUrl
    localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo));
    userInfo.value = updatedUserInfo;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

// 提交密码修改
const submitPasswordChange = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        // 确保token正确发送
        const response = await axios.post('/api/user/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        }, {
          headers: { 'Authorization': token }
        });
        
        if (response.data.code === 200) {
          ElMessage.success('密码修改成功，请重新登录');
          // 清除登录信息，跳转到登录页
          setTimeout(() => {
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            router.push('/login');
          }, 1500);
        } else {
          ElMessage.error(response.data.message || '密码修改失败');
        }
      } catch (error) {
        console.error('密码修改出错:', error);
        ElMessage.error('密码修改失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    }
  });
};

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const response = await axios.get('/api/user/profile', {
      headers: { 'Authorization': token }
    });
    
    if (response.data.code === 200) {
      userInfo.value = response.data.data;
      // 更新本地存储的用户信息
      localStorage.setItem('userInfo', JSON.stringify(response.data.data));
      // 如果有头像，设置头像URL
      if (response.data.data.avatar) {
        avatarUrl.value = getFullAvatarUrl(response.data.data.avatar);
      }
    }
  } catch (error) {
    console.error('获取用户信息出错:', error);
  }
};

onMounted(() => {
  fetchUserProfile();
});
</script>

<style scoped>
.user-profile-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  display: flex;
  margin-bottom: 30px;
}

.avatar-section {
  margin-right: 30px;
  text-align: center;
  flex-shrink: 0;
}

.info-section {
  flex-grow: 1;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.user-avatar {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.upload-hint {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

.password-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.password-section h3 {
  margin-bottom: 20px;
  color: #303133;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
  }
  
  .avatar-section {
    margin-right: 0;
    margin-bottom: 20px;
  }
}
</style> 