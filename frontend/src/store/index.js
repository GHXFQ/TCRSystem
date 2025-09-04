import { createStore } from 'vuex'
import axios from 'axios'
import router from '@/router'
import user from './modules/user'

// 创建axios实例
const api = axios.create({
  baseURL: process.env.VUE_APP_API_URL || '/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
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
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 401 清除token信息并跳转到登录页面
      store.commit('clearAuth')
      router.push('/login')
    }
    return Promise.reject(error.response ? error.response.data : error)
  }
)

const store = createStore({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  },
  getters: {
    isLoggedIn: state => !!state.token,
    userRole: state => state.userInfo.role || -1,
    userInfo: state => state.userInfo,
    token: state => state.token
  },
  mutations: {
    setToken(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    setUser(state, user) {
      state.userInfo = user
      localStorage.setItem('userInfo', JSON.stringify(user))
    },
    clearAuth(state) {
      state.token = ''
      state.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  },
  actions: {
    // 用户注册
    async register(_, user) {
      const response = await api.post('/user/register', user)
      return response.data
    },
    
    // 用户登录
    async login({ commit }, credentials) {
      const response = await api.post('/user/login', credentials)
      const { token, ...userInfo } = response.data
      
      // 保存token和用户信息
      commit('setToken', token)
      commit('setUser', userInfo)
      
      return userInfo
    },
    
    // 用户登出
    logout({ commit }) {
      // 清除认证信息
      commit('clearAuth')
      return Promise.resolve()
    },
    
    // 获取用户信息
    async getUserInfo({ commit }) {
      const response = await api.get('/user/info')
      commit('setUser', response.data)
      return response.data
    }
  },
  modules: {
    user
  }
})

export default store
export { api } 