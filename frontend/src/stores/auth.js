import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const isLoggedIn = ref(!!localStorage.getItem('isLoggedIn'))
  const userEmail = ref(localStorage.getItem('userEmail') || '')
  const loginTime = ref(localStorage.getItem('loginTime') || '')
  const token = ref(localStorage.getItem('token') || '')

  // 计算属性
  const isAuthenticated = computed(() => isLoggedIn.value)

  // 方法
  // 登录
  const login = (email) => {
    console.log('执行登录:', email)
    isLoggedIn.value = true
    userEmail.value = email
    loginTime.value = new Date().toISOString()
    
    // 保存到localStorage
    localStorage.setItem('isLoggedIn', 'true')
    localStorage.setItem('userEmail', email)
    localStorage.setItem('loginTime', loginTime.value)
    console.log('登录状态已保存')
  }
  // 设置token
  const setToken = (jwtToken) => {
    token.value = jwtToken
    localStorage.setItem('token', jwtToken)
  }

  // 登出
  const logout = () => {
    isLoggedIn.value = false
    userEmail.value = ''
    loginTime.value = ''
    token.value = ''
    
    // 清除localStorage
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('loginTime')
    localStorage.removeItem('token')
  }
  // 检查登录状态
  const checkLoginStatus = () => {
    const loggedIn = localStorage.getItem('isLoggedIn') === 'true'
    
    if (loggedIn) {
      isLoggedIn.value = true
      userEmail.value = localStorage.getItem('userEmail') || ''
      loginTime.value = localStorage.getItem('loginTime') || ''
      token.value = localStorage.getItem('token') || ''
    }
    return loggedIn
  }

  return {
    // 状态
    isLoggedIn,
    userEmail,
    loginTime,
    token,
    // 计算属性
    isAuthenticated,
    // 方法
    login,
    setToken,
    logout,
    checkLoginStatus
  }
})