import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/styles.css'
import { useAuthStore } from './stores/auth'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建Vue应用实例
const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用Pinia状态管理
const pinia = createPinia()
app.use(pinia)

// 使用Element Plus
app.use(ElementPlus)

// 使用Vue Router
app.use(router)

// 初始化认证状态
const authStore = useAuthStore(pinia)
authStore.checkLoginStatus()

// 挂载应用
app.mount('#app')