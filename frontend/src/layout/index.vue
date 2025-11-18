<template>
  <div class="app-container">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
      <div class="logo">
        <i class="fas fa-chart-pie"></i>
        <h1>量融</h1>
      </div>
      <nav class="menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.name"
          :to="item.path" 
          class="menu-item"
          :class="{ active: $route.name === item.name }"
        >
          <i :class="item.icon"></i>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <router-link 
          to="/settings" 
          class="menu-item"
          :class="{ active: $route.name === 'Settings' }"
        >
          <i class="fas fa-cog"></i>
          <span>设置</span>
        </router-link>
        <a href="#" class="menu-item" @click.prevent="handleLogout">
          <i class="fas fa-sign-out-alt"></i>
          <span>退出登录</span>
        </a>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Layout',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    const menuItems = [
      {
        name: 'Dashboard',
        path: '/dashboard',
        title: '仪表盘',
        icon: 'fas fa-tachometer-alt'
      },
      {
        name: 'Market',
        path: '/market',
        title: '个股行情',
        icon: 'fas fa-water'
      },
      {
        name: 'Watchlist',
        path: '/watchlist',
        title: '我的自选',
        icon: 'fas fa-star'
      },
      {
        name: 'Advisor',
        path: '/advisor',
        title: '智能投顾',
        icon: 'fas fa-robot'
      },
      {
        name: 'AiModels',
        path: '/ai-models',
        title: 'AI模型管理',
        icon: 'fas fa-brain'
      }
    ]

    const handleLogout = () => {
      if (confirm('确定要退出登录吗？')) {
        authStore.logout()
        router.push('/login')
      }
    }

    return {
      menuItems,
      handleLogout
    }
  }
}
</script>