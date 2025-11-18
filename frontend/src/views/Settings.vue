<template>
  <div class="page-content active">
    <header class="header">
      <div class="header-left">
        <h2>设置</h2>
        <p>账户和系统设置</p>
      </div>
    </header>
  
    <div class="content-grid">
      <div class="card">
        <div class="card-header">
          <h3>账户信息</h3>
        </div>
        <div class="settings-section">
          <div class="setting-item">
            <label>邮箱地址</label>
            <input type="email" id="userEmailDisplay" readonly :value="userEmail">
          </div>
          <div class="setting-item">
            <label>登录时间</label>
            <span>{{ formattedLoginTime }}</span>
          </div>
        </div>
      </div>
    
      <div class="card">
        <div class="card-header">
          <h3>系统设置</h3>
        </div>
        <div class="settings-section">
          <div class="setting-item">
            <label>主题模式</label>
            <select v-model="settings.theme" @change="saveSetting('theme', settings.theme)">
              <option value="dark">深色模式</option>
              <option value="light">浅色模式</option>
              <option value="auto">跟随系统</option>
            </select>
          </div>
          <div class="setting-item">
            <label>语言</label>
            <select v-model="settings.language" @change="saveSetting('language', settings.language)">
              <option value="zh-CN">简体中文</option>
              <option value="en">English</option>
            </select>
          </div>
          <div class="setting-item">
            <label>自动刷新数据</label>
            <label class="switch">
              <input type="checkbox" v-model="settings.autoRefresh" @change="saveSetting('autoRefresh', settings.autoRefresh)">
              <span class="slider"></span>
            </label>
          </div>
        </div>
      </div>
    
      <div class="card">
        <div class="card-header">
          <h3>关于</h3>
        </div>
        <div class="settings-section">
          <div class="setting-item">
            <label>版本</label>
            <span>v1.0.0</span>
          </div>
          <div class="setting-item">
            <label>最后更新</label>
            <span>2023-10-27</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Settings',
  setup() {
    const authStore = useAuthStore()

    const userEmail = computed(() => authStore.userEmail)
    const loginTime = computed(() => authStore.loginTime)
    
    const formattedLoginTime = computed(() => {
      if (!loginTime.value) return '--'
      const date = new Date(loginTime.value)
      return date.toLocaleString('zh-CN')
    })

    const settings = ref({
      theme: 'dark',
      language: 'zh-CN',
      autoRefresh: true
    })

    // 保存设置到localStorage
    const saveSetting = (key, value) => {
      localStorage.setItem(`setting_${key}`, value)
      console.log(`设置已保存: ${key} = ${value}`)
      // 这里可以添加显示保存成功通知的逻辑
    }

    // 加载设置
    const loadSettings = () => {
      const theme = localStorage.getItem('setting_theme')
      const language = localStorage.getItem('setting_language')
      const autoRefresh = localStorage.getItem('setting_autoRefresh')
      
      if (theme) settings.value.theme = theme
      if (language) settings.value.language = language
      if (autoRefresh !== null) settings.value.autoRefresh = autoRefresh === 'true'
    }

    onMounted(() => {
      loadSettings()
    })

    return {
      userEmail,
      formattedLoginTime,
      settings,
      saveSetting
    }
  }
}
</script>