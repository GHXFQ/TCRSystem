<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <h2>教学课程资源管理系统</h2>
      </div>
      <el-form ref="loginForm" :model="loginData" :rules="rules" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginData.username" placeholder="用户名/学号/工号">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginData.password" type="password" placeholder="密码" show-password @keyup.enter="handleLogin">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">登录</el-button>
        </el-form-item>
        <div class="login-options">
          <router-link to="/register">没有账号？立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loginForm = ref(null)
const loading = ref(false)

const loginData = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginForm.value) return
  
  try {
    await loginForm.value.validate()
    loading.value = true
    
    try {
      const response = await axios.post('/api/user/login', loginData)
      
      if (response.data.code === 200) {
        const { data } = response.data
        
        // 保存token和用户信息到localStorage
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(data))
        
        console.log('登录成功，保存的 token:', data.token)
        console.log('登录成功，保存的用户信息:', data)
        
        // 测试 localStorage 是否正确保存了 token
        setTimeout(() => {
          console.log('从 localStorage 获取的 token:', localStorage.getItem('token'))
        }, 100)
        
        ElMessage.success('登录成功')
        
        // 根据角色跳转到不同的首页
        if (data.role === 0) {
          router.push('/student/home')
        } else if (data.role === 1) {
          router.push('/teacher/home')
        } else if (data.role === 2) {
          router.push('/admin/home')
        }
      } else {
        ElMessage.error(response.data.message || '登录失败，请重试')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error(error.response?.data?.message || '登录失败，请重试')
    } finally {
      loading.value = false
    }
  } catch (error) {
    console.error('表单验证失败', error)
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-box {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
}

.login-title h2 {
  font-weight: 500;
  color: #409eff;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
}

.login-options {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  font-size: 14px;
}

.login-options a {
  color: #409eff;
  text-decoration: none;
}
</style> 