// frontend/src/utils/request.js
import axios from 'axios'
import router from '../router'
import store from '../store'

const service = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = store.state.token
        if (token) {
            // 确保token格式为Bearer xxx
            const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`
            config.headers['Authorization'] = formattedToken
        }
        return config
    },
    error => {
        console.log(error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            // token过期或无效
            if (res.code === 401) {
                store.commit('clearUserInfo')
                router.push('/login')
            }
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            return res
        }
    },
    error => {
        console.log('err' + error)
        return Promise.reject(error)
    }
)

export default service