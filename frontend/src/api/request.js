import axios from 'axios'
import { mockApi } from './mock'

// 模拟存储的AI平台数据
const mockAiPlatforms = []

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
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
    const res = response.data
    // 根据后端返回的数据结构进行调整
    if (res.code && res.code !== 200) {
      // 处理业务错误
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  async error => {
    console.error('请求错误:', error)
    console.log('错误详情:', {
      code: error.code,
      message: error.message,
      config: error.config
    })
    
    // 如果是网络错误或超时，尝试使用模拟数据
    if (error.code === 'ECONNREFUSED' || error.code === 'ECONNABORTED' || error.message.includes('Network Error')) {
      console.log('后端服务不可用，使用模拟数据')
      
      // 根据请求的URL和方法调用相应的模拟API
      const { url, method } = error.config
      console.log('尝试调用模拟API:', { url, method })
      
      try {
        // 自选股相关API
        if (url.includes('/api/favorites/list') && method === 'get') {
          console.log('调用获取自选股模拟数据')
          return await mockApi.getFavorites()
        } else if (url.includes('/api/favorites/add') && method === 'post') {
          console.log('调用添加自选股模拟数据')
          return await mockApi.addFavorite(JSON.parse(error.config.data))
        } else if (url.includes('/api/favorites/remove') && method === 'delete') {
          console.log('调用删除自选股模拟数据')
          const stockCode = url.split('/').pop()
          return await mockApi.removeFavorite(stockCode)
        } else if (url.includes('/api/favorites/sort') && method === 'put') {
          console.log('调用更新排序模拟数据')
          return { code: 200, message: '排序更新成功' }
        } else if (url.includes('/api/favorites/remark') && method === 'put') {
          console.log('调用更新备注模拟数据')
          return { code: 200, message: '备注更新成功' }
        } else if (url.includes('/api/favorites/check') && method === 'get') {
          console.log('调用检查自选股模拟数据')
          return { code: 200, data: { isFavorite: false } }
        } else if (url.includes('/api/favorites/count') && method === 'get') {
          console.log('调用获取自选股数量模拟数据')
          return { code: 200, data: { count: 5 } }
        
        // 投资建议相关API
        } else if (url.includes('/api/advice/list') && method === 'get') {
          console.log('调用获取投资建议模拟数据')
          return await mockApi.getAdviceList()
        } else if (url.includes('/api/advice/') && method === 'get') {
          console.log('调用获取投资建议详情模拟数据')
          const id = url.split('/').pop()
          const advice = mockApi.mockAdvices.find(a => a.id === parseInt(id))
          return { code: 200, data: advice }
        
        // 回测相关API
        } else if (url.includes('/api/backtest/run') && method === 'post') {
          console.log('调用运行回测模拟数据，参数:', error.config.data)
          const result = await mockApi.runBacktest(JSON.parse(error.config.data))
          console.log('回测模拟数据结果:', result)
          return result
        } else if (url.includes('/api/backtest/history') && method === 'get') {
          console.log('调用获取回测历史模拟数据')
          return await mockApi.getBacktestHistory()
        } else if (url.includes('/api/backtest/') && method === 'get') {
          console.log('调用获取回测详情模拟数据')
          const id = url.split('/').pop()
          return await mockApi.getBacktestDetail(id)
        } else if (url.includes('/api/backtest/') && method === 'delete') {
          console.log('调用删除回测记录模拟数据')
          return { code: 200, message: '删除成功' }
        } else if (url.includes('/api/backtest/stats') && method === 'get') {
          console.log('调用获取回测统计模拟数据')
          return { code: 200, data: { totalBacktests: 10, successRate: 75 } }
        
        // 用户相关API
        } else if (url.includes('/api/users/login') && method === 'post') {
          console.log('调用用户登录模拟数据')
          return { code: 200, data: 'mock-jwt-token', message: '登录成功' }
        } else if (url.includes('/api/users/register') && method === 'post') {
          console.log('调用用户注册模拟数据')
          return { code: 200, message: '注册成功' }
        } else if (url.includes('/api/users/info') && method === 'get') {
          console.log('调用获取用户信息模拟数据')
          return { code: 200, data: { email: 'test@example.com', username: 'testuser' } }
        } else if (url.includes('/api/users/update') && method === 'put') {
          console.log('调用更新用户信息模拟数据')
          return { code: 200, message: '更新成功' }
        } else if (url.includes('/api/users/change-password') && method === 'post') {
          console.log('调用修改密码模拟数据')
          return { code: 200, message: '密码修改成功' }
        } else if (url.includes('/api/users/logout') && method === 'post') {
          console.log('调用用户登出模拟数据')
          return { code: 200, message: '登出成功' }
        
        // 投资偏好相关API
        } else if (url.includes('/api/preferences/save') && method === 'post') {
          console.log('调用保存投资偏好模拟数据')
          return { code: 200, message: '偏好保存成功' }
        } else if (url.includes('/api/preferences/get') && method === 'get') {
          console.log('调用获取投资偏好模拟数据')
          return { code: 200, data: { riskLevel: 'medium', investmentAmount: 100000 } }
        } else if (url.includes('/api/preferences/delete') && method === 'delete') {
          console.log('调用删除投资偏好模拟数据')
          return { code: 200, message: '偏好删除成功' }
        } else if (url.includes('/api/preferences/check') && method === 'get') {
          console.log('调用检查投资偏好模拟数据')
          return { code: 200, data: { hasPreference: true } }
        
        // 股票相关API
        } else if (url.includes('/stock/list') && method === 'get') {
          console.log('调用获取股票列表模拟数据')
          return { code: 200, data: mockApi.mockFavorites }
        } else if (url.includes('/stock/') && url.includes('/realtime') && method === 'get') {
          console.log('调用获取股票实时数据模拟数据')
          const stockCode = url.split('/')[2]
          return { code: 200, data: { price: 100.5, change: 2.3 } }
        } else if (url.includes('/stock/') && url.includes('/kline') && method === 'get') {
          console.log('调用获取股票K线数据模拟数据')
          return { code: 200, data: [] }
        } else if (url.includes('/stock/watchlist') && method === 'get') {
          console.log('调用获取自选股列表模拟数据')
          return { code: 200, data: mockApi.mockFavorites }
        } else if (url.includes('/stock/watchlist/add') && method === 'post') {
          console.log('调用添加自选股模拟数据')
          return { code: 200, message: '添加成功' }
        } else if (url.includes('/stock/watchlist/remove') && method === 'post') {
          console.log('调用移除自选股模拟数据')
          return { code: 200, message: '移除成功' }
        } else if (url.includes('/stock/indices') && method === 'get') {
          console.log('调用获取市场指数模拟数据')
          return { code: 200, data: [] }
        } else if (url.includes('/stock/search') && method === 'get') {
          console.log('调用搜索股票模拟数据')
          return { code: 200, data: mockApi.mockFavorites.slice(0, 3) }
        
        // AI平台相关API
        } else if (url.includes('/api/ai-platforms/list') && method === 'get') {
          console.log('调用获取AI平台列表模拟数据')
          return { code: 200, data: mockAiPlatforms }
        } else if (url.includes('/api/ai-platforms/save') && method === 'post') {
          console.log('调用保存AI平台模拟数据')
          const data = JSON.parse(error.config.data)
          const newPlatform = { id: Date.now(), ...data }
          mockAiPlatforms.push(newPlatform)
          return { code: 200, data: newPlatform, message: '保存成功' }
        } else if (url.includes('/api/ai-platforms/update') && method === 'put') {
          console.log('调用更新AI平台模拟数据')
          const data = JSON.parse(error.config.data)
          const idx = mockAiPlatforms.findIndex(p => p.id === data.id)
          if (idx !== -1) mockAiPlatforms[idx] = { ...mockAiPlatforms[idx], ...data }
          return { code: 200, message: '更新成功' }
        } else if (url.includes('/api/ai-platforms/delete') && method === 'delete') {
          console.log('调用删除AI平台模拟数据')
          const id = parseInt(url.split('/').pop())
          const idx = mockAiPlatforms.findIndex(p => p.id === id)
          if (idx !== -1) mockAiPlatforms.splice(idx, 1)
          return { code: 200, message: '删除成功' }
        
        } else {
          console.log('未找到匹配的模拟API:', { url, method })
        }
      } catch (mockError) {
        console.error('模拟数据调用失败:', mockError)
      }
    }
    
    // 处理HTTP错误状态码
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('isLoggedIn')
          localStorage.removeItem('userEmail')
          localStorage.removeItem('loginTime')
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