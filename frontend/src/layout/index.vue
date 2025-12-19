<template>
  <el-container class="app-layout">
    <!-- 左侧导航栏 -->
    <el-aside width="260px" class="sidebar-new">
      <div class="logo-section">
        <el-icon class="logo-icon" :size="32"><TrendCharts /></el-icon>
        <span class="logo-text">InvestIQ AI</span>
      </div>
      
      <el-scrollbar>
        <el-menu
          :default-active="$route.path"
          class="sidebar-menu"
          router
        >
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <div class="sidebar-footer-new">
        <el-menu class="footer-menu" router>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
          <el-menu-item @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>安全退出</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-main class="main-container">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { 
  Menu as IconMenu, 
  TrendCharts, 
  Monitor, 
  Star, 
  Service, 
  Cpu, 
  Odometer,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const menuItems = [
  {
    name: 'Dashboard',
    path: '/dashboard',
    title: '工作仪表盘',
    icon: Monitor
  },
  {
    name: 'Market',
    path: '/market',
    title: '个股行情',
    icon: TrendCharts
  },
  {
    name: 'Watchlist',
    path: '/watchlist',
    title: '我的自选',
    icon: Star
  },
  {
    name: 'Advisor',
    path: '/advisor',
    title: '智能投顾',
    icon: Service
  },
  {
    name: 'AiModels',
    path: '/ai-models',
    title: '模型管理',
    icon: Cpu
  },
  {
    name: 'Backtest',
    path: '/backtest',
    title: '策略回测',
    icon: Odometer
  }
]

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录并清除会话吗？', '安全退出', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning',
    background: '#1D1E1F'
  }).then(() => {
    authStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  background-color: var(--bg-color);
}

.sidebar-new {
  background-color: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: var(--transition-base);
}

.logo-section {
  padding: 2rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  color: var(--primary-accent);
  filter: drop-shadow(0 0 8px var(--glow-color));
}

.logo-text {
  font-size: 1.4rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--text-primary), var(--primary-accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
}

.sidebar-footer-new {
  padding: 1rem 0;
  border-top: 1px solid var(--border-color);
}

.main-container {
  background-color: transparent;
  padding: 0;
  position: relative;
}

/* 过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-15px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(15px);
}

:deep(.el-menu-item) {
  height: 54px;
  line-height: 54px;
  margin: 4px 12px;
  border-radius: 8px;
  color: var(--text-secondary);
}

:deep(.el-menu-item:hover) {
  color: var(--primary-accent);
  background-color: rgba(0, 166, 255, 0.08) !important;
}

:deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(90deg, var(--primary-accent), var(--secondary-accent)) !important;
  box-shadow: 0 4px 12px rgba(0, 166, 255, 0.3);
}
</style>