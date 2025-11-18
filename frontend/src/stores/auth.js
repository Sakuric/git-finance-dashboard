import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const isLoggedIn = ref(!!localStorage.getItem('isLoggedIn'))
  const userEmail = ref(localStorage.getItem('userEmail') || '')
  const loginTime = ref(localStorage.getItem('loginTime') || '')

  // 计算属性
  const isAuthenticated = computed(() => isLoggedIn.value)

  // 方法
  const login = (email) => {
    isLoggedIn.value = true
    userEmail.value = email
    loginTime.value = new Date().toISOString()
    
    // 保存到localStorage
    localStorage.setItem('isLoggedIn', 'true')
    localStorage.setItem('userEmail', email)
    localStorage.setItem('loginTime', loginTime.value)
  }

  const logout = () => {
    isLoggedIn.value = false
    userEmail.value = ''
    loginTime.value = ''
    
    // 清除localStorage
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('loginTime')
  }

  const checkLoginStatus = () => {
    const loggedIn = localStorage.getItem('isLoggedIn') === 'true'
    if (loggedIn) {
      isLoggedIn.value = true
      userEmail.value = localStorage.getItem('userEmail') || ''
      loginTime.value = localStorage.getItem('loginTime') || ''
    }
    return loggedIn
  }

  return {
    // 状态
    isLoggedIn,
    userEmail,
    loginTime,
    // 计算属性
    isAuthenticated,
    // 方法
    login,
    logout,
    checkLoginStatus
  }
})