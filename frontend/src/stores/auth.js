import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { jwtDecode } from 'jwt-decode'

export const useAuthStore = defineStore('auth', () => {
  const isLoggedIn = ref(!!localStorage.getItem('isLoggedIn'))
  const userEmail = ref(localStorage.getItem('userEmail') || '')
  const loginTime = ref(localStorage.getItem('loginTime') || '')
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')

  const isAuthenticated = computed(() => isLoggedIn.value)

  const login = (email, jwtToken) => {
    isLoggedIn.value = true
    userEmail.value = email
    loginTime.value = new Date().toISOString()
    token.value = jwtToken

    try {
      const decoded = jwtDecode(jwtToken)
      userId.value = decoded.userId || decoded.sub || ''
      username.value = decoded.username || email
    } catch (e) {
      console.error('Token decode error:', e)
    }

    localStorage.setItem('isLoggedIn', 'true')
    localStorage.setItem('userEmail', email)
    localStorage.setItem('loginTime', loginTime.value)
    localStorage.setItem('token', jwtToken)
    localStorage.setItem('userId', userId.value)
    localStorage.setItem('username', username.value)
  }

  const setToken = (jwtToken) => {
    token.value = jwtToken
    localStorage.setItem('token', jwtToken)

    try {
      const decoded = jwtDecode(jwtToken)
      userId.value = decoded.userId || decoded.sub || ''
      username.value = decoded.username || userEmail.value
      localStorage.setItem('userId', userId.value)
      localStorage.setItem('username', username.value)
    } catch (e) {
      console.error('Token decode error:', e)
    }
  }

  const logout = () => {
    isLoggedIn.value = false
    userEmail.value = ''
    loginTime.value = ''
    token.value = ''
    userId.value = ''
    username.value = ''

    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('loginTime')
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  const checkLoginStatus = () => {
    const loggedIn = localStorage.getItem('isLoggedIn') === 'true'

    if (loggedIn) {
      isLoggedIn.value = true
      userEmail.value = localStorage.getItem('userEmail') || ''
      loginTime.value = localStorage.getItem('loginTime') || ''
      token.value = localStorage.getItem('token') || ''
      userId.value = localStorage.getItem('userId') || ''
      username.value = localStorage.getItem('username') || ''
    }
    return loggedIn
  }

  return {
    isLoggedIn,
    userEmail,
    loginTime,
    token,
    userId,
    username,
    isAuthenticated,
    login,
    setToken,
    logout,
    checkLoginStatus
  }
})