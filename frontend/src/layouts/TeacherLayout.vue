<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <el-container class="layout-container">
    <el-aside width="220px">
      <el-menu
        :router="true"
        :default-active="$route.path"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="logo-container">
          <h1 class="logo-text">教学资源管理系统</h1>
          <div class="sub-title">教师端</div>
        </div>
        <el-menu-item index="/teacher/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/teacher/course-management">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/material-upload">
          <el-icon><Upload /></el-icon>
          <span>材料上传</span>
        </el-menu-item>
        <el-menu-item index="/teacher/material-review">
          <el-icon><Document /></el-icon>
          <span>材料审核</span>
        </el-menu-item>
        <el-menu-item index="/teacher/resource-sharing">
          <el-icon><Connection /></el-icon>
          <span>课程资源协同</span>
        </el-menu-item>
        <el-menu-item index="/teacher/assignment-management">
          <el-icon><Calendar /></el-icon>
          <span>课程任务管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/course-feedback">
          <el-icon><ChatDotRound /></el-icon>
          <span>课程反馈</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/teacher/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar-image" alt="用户头像" />
              <el-avatar v-else :size="32" class="user-avatar">
                {{ userInfo?.realName?.substring(0, 1) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.realName || '用户' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
      <el-footer>
        <div class="footer-content">
          © 2023 教学课程资源管理系统 - 教师端
        </div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { HomeFilled, Document, Upload, ArrowDown, Reading, Connection, Calendar, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const avatarUrl = ref('')

// 处理头像路径，确保使用正确的服务器地址
const getFullAvatarUrl = (path) => {
  if (!path) return '';
  // 如果已经是完整URL，则直接返回
  if (path.startsWith('http')) {
    return path;
  }
  // 否则拼接后端地址
  return `http://localhost:8080${path}`;
};

// 初始化用户头像
if (userInfo.value.avatar) {
  avatarUrl.value = getFullAvatarUrl(userInfo.value.avatar);
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    // 跳转到个人信息页面
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      // 发送退出登录请求
      await axios.post('/api/user/logout', null, {
        headers: { Authorization: localStorage.getItem('token') }
      })
      
      // 清除本地存储的用户信息和token
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (error) {
      console.error('退出失败:', error)
      
      // 即使请求失败，也清除本地存储并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    background-color: #304156;
    box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
    
    .logo-container {
      height: 80px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 10px 0;
      
      .logo-text {
        color: #fff;
        font-size: 16px;
        margin: 0;
        font-weight: 600;
      }
      
      .sub-title {
        color: #97a8be;
        font-size: 12px;
      }
    }
    
    .el-menu-vertical {
      border-right: none;
    }
    
    .el-menu-item {
      height: 56px;
      line-height: 56px;
      
      &.is-active {
        background-color: #263445 !important;
      }
      
      .el-icon {
        margin-right: 10px;
      }
    }
  }

  .el-header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 50px !important;
    
    .breadcrumb {
      display: flex;
      align-items: center;
    }

    .header-right {
      display: flex;
      align-items: center;
      
      .el-dropdown-link {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        .user-avatar {
          margin-right: 8px;
        }
        
        .avatar-image {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          margin-right: 8px;
          object-fit: cover;
        }
        
        .username {
          margin-right: 4px;
        }
      }
    }
  }

  .el-main {
    background-color: #f0f2f5;
    padding: 20px;
    overflow-y: auto;
  }
  
  .el-footer {
    background-color: #fff;
    box-shadow: 0 -1px 4px rgba(0, 21, 41, 0.08);
    text-align: center;
    padding: 15px 0;
    
    .footer-content {
      color: #606266;
      font-size: 14px;
    }
  }
}
</style> 