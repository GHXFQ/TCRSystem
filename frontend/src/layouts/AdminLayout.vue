<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :router="true"
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="logo-container">
          <h1 class="logo-text">管理员端</h1>
        </div>
        <el-menu-item index="/admin/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/courses">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/material-review">
          <el-icon><Document /></el-icon>
          <span>材料审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/educational-admin">
          <el-icon><School /></el-icon>
          <span>教务管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ username }}
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
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { HomeFilled, User, Reading, Document, ArrowDown, School } from '@element-plus/icons-vue'
import { useStore } from 'vuex'

const router = useRouter()
const route = useRoute()
const store = useStore()

const username = computed(() => store.state.user?.username || '管理员')
const activeMenu = computed(() => route.path)

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
    
    .logo-container {
      height: 60px;
      display: flex;
      justify-content: center;
      align-items: center;
      
      .logo-text {
        color: #fff;
        font-size: 18px;
        margin: 0;
      }
    }
    
    .el-menu {
      border-right: none;
    }
  }

  .el-header {
    background-color: #fff;
    border-bottom: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 20px;

    .header-right {
      .el-dropdown-link {
        cursor: pointer;
        color: #409eff;
      }
    }
  }

  .el-main {
    background-color: #f0f2f5;
    padding: 20px;
  }
}
</style> 