import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(res.message || '请求失败')
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      // token过期或未登录
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request 