import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 60000, // 增加到60秒，因为回测需要实时获取数据
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
    return response.data
  },
  async error => {
    console.error('请求错误:', error)

    // 处理HTTP错误状态码
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('isLoggedIn')
          localStorage.removeItem('userEmail')
          localStorage.removeItem('loginTime')
          localStorage.removeItem('userId')
          localStorage.removeItem('username')
          window.location.href = '/login'
          break
        case 403:
          console.error('没有权限访问')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error('未知错误')
      }
    }
    
    return Promise.reject(error)
  }
)

export default request