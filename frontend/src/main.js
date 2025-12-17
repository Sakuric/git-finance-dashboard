import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/styles.css'
import { useAuthStore } from './stores/auth'

// 创建Vue应用实例
const app = createApp(App)

// 使用Pinia状态管理
const pinia = createPinia()
app.use(pinia)

// 使用Vue Router
app.use(router)

// 初始化认证状态
const authStore = useAuthStore(pinia)
authStore.checkLoginStatus()

// 挂载应用
app.mount('#app')