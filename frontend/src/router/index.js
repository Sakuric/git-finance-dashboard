import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 路由组件懒加载
const Login = () => import('@/views/Login.vue')
const Layout = () => import('@/layout/index.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const Market = () => import('@/views/Market.vue')
const Watchlist = () => import('@/views/Watchlist.vue')
const Advisor = () => import('@/views/Advisor.vue')
const AiModels = () => import('@/views/AiModels.vue')
const Backtest = () => import('@/views/Backtest.vue')
const Settings = () => import('@/views/Settings.vue')
const Preference = () => import('@/views/Preference.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表盘' }
      },
      {
        path: 'market',
        name: 'Market',
        component: Market,
        meta: { title: '个股行情' }
      },
      {
        path: 'watchlist',
        name: 'Watchlist',
        component: Watchlist,
        meta: { title: '我的自选' }
      },
      {
        path: 'advisor',
        name: 'Advisor',
        component: Advisor,
        meta: { title: '智能投顾' }
      },
      {
        path: 'ai-models',
        name: 'AiModels',
        component: AiModels,
        meta: { title: 'AI模型管理' }
      },
      {
        path: 'backtest',
        name: 'Backtest',
        component: Backtest,
        meta: { title: '建议回测' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: Settings,
        meta: { title: '设置' }
      },
      {
        path: 'preference',
        name: 'Preference',
        component: Preference,
        meta: { title: '投资偏好' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 确保认证状态已初始化
  authStore.checkLoginStatus()
  
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && authStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router