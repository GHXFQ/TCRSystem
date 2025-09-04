import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { applyResizeObserverErrorFix } from './utils/resizeObserverFix'
import { registerDirectives } from './utils/directives'
import './assets/css/resize-observer-fix.css'

// 配置axios的baseURL，确保请求发送到正确的后端地址
axios.defaults.baseURL = 'http://localhost:8080'
console.log('配置 axios baseURL:', axios.defaults.baseURL)

// 配置axios
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      // 始终使用 Bearer 前缀
      config.headers.Authorization = `Bearer ${token}`
      
      console.log('===== 请求拦截器 =====')
      console.log('请求 URL:', config.url)
      console.log('请求方法:', config.method)
      console.log('完整请求 URL:', axios.defaults.baseURL + config.url)
      console.log('请求参数:', JSON.stringify(config.params || {}))
      console.log('请求头:', JSON.stringify(config.headers))
      console.log('===== 请求拦截器结束 =====')
    } else {
      console.log('未找到 token，请求将不包含认证信息')
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
axios.interceptors.response.use(
  response => {
    console.log('===== 响应拦截器 =====')
    console.log('响应成功:', response.config.url, response.status)
    console.log('响应状态:', response.status, response.statusText)
    console.log('响应数据:', JSON.stringify(response.data).substring(0, 200) + '...')
    console.log('===== 响应拦截器结束 =====')
    return response
  },
  error => {
    console.error('===== 响应错误 =====')
    console.error('请求URL:', error.config?.url)
    console.error('错误消息:', error.message)
    
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误数据:', JSON.stringify(error.response.data))
      
      if (error.response.status === 403) {
        console.error('403 错误，可能是 token 无效或已过期')
        ElMessage.error('登录已过期，请重新登录')
        // 清除 token 并跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (error.response.status === 500) {
        console.error('500 错误，服务器内部错误')
        ElMessage.error('服务器内部错误，请稍后再试')
      }
    } else if (error.request) {
      console.error('请求已发送但未收到响应', error.request)
      ElMessage.error('网络错误，服务器未响应')
    } else {
      console.error('请求设置错误', error.message)
      ElMessage.error('请求发送失败: ' + error.message)
    }
    console.error('===== 响应错误结束 =====')
    return Promise.reject(error)
  }
)

// 立即应用ResizeObserver错误修复
applyResizeObserverErrorFix()

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册全局指令
registerDirectives(app)

app.use(ElementPlus)
app.use(store)
app.use(router)

app.mount('#app') 