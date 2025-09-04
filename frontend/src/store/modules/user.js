import { login, logout, getUserInfo } from '@/api/user'

const state = {
  token: localStorage.getItem('token'),
  userInfo: null
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_USER_INFO: (state, userInfo) => {
    state.userInfo = userInfo
  }
}

const actions = {
  // 用户登录
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      login({ username: username.trim(), password: password })
        .then(response => {
          const { data } = response
          commit('SET_TOKEN', data.token)
          commit('SET_USER_INFO', data)
          localStorage.setItem('token', data.token)
          resolve(data)
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 获取用户信息
  getUserInfo({ commit }) {
    return new Promise((resolve, reject) => {
      getUserInfo()
        .then(response => {
          const { data } = response
          commit('SET_USER_INFO', data)
          resolve(data)
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 退出登录
  logout({ commit }) {
    return new Promise((resolve, reject) => {
      logout()
        .then(() => {
          commit('SET_TOKEN', '')
          commit('SET_USER_INFO', null)
          localStorage.removeItem('token')
          resolve()
        })
        .catch(error => {
          reject(error)
        })
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 