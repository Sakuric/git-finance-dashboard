<template>
  <div class="page-content active">
    <!-- 头部 -->
    <el-header class="header card-glass mb-6" height="auto">
      <div class="header-left">
        <h2>系统配置</h2>
        <p>管理您的个人账户、安全首选项及全局界面设置</p>
      </div>
    </el-header>

    <el-tabs type="border-card" class="settings-tabs">
      <!-- 个人资料 -->
      <el-tab-pane>
        <template #label>
          <span class="tab-label"><el-icon><User /></el-icon> 账户中心</span>
        </template>
        
        <div class="account-profile-section">
          <div class="avatar-box">
            <el-avatar :size="100" src="https://i.pravatar.cc/100?u=admin" border />
            <el-button size="small" :icon="Camera" class="mt-4">更换头像</el-button>
          </div>
          
          <div class="info-box">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="登录账号">admin</el-descriptions-item>
              <el-descriptions-item label="绑定邮箱">{{ userEmail || '未绑定' }}</el-descriptions-item>
              <el-descriptions-item label="本次登录时间">{{ formattedLoginTime }}</el-descriptions-item>
              <el-descriptions-item label="账户权限">
                <el-tag size="small" effect="dark">超级管理员</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <div class="actions mt-6">
              <el-button type="primary" plain @click="showChangePass = true">修改登录密码</el-button>
              <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 界面与偏好 -->
      <el-tab-pane>
        <template #label>
          <span class="tab-label"><el-icon><Monitor /></el-icon> 界面首选项</span>
        </template>
        
        <el-form label-position="top" class="p-4 max-w-2xl">
          <el-form-item label="视觉主题模式">
            <el-radio-group v-model="settings.theme" @change="val => saveSetting('theme', val)">
              <el-radio-button label="dark">极致深色</el-radio-button>
              <el-radio-button label="light">明亮办公</el-radio-button>
              <el-radio-button label="auto">跟随系统</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="数据刷新频率 (行情同步频率)">
            <div class="flex-align-center">
              <el-input-number v-model="refreshFreq" :min="5" :max="300" @change="val => saveSetting('refresh', val)" />
              <span class="ml-4 text-secondary">秒/次 (实时行情自动更新间隔)</span>
            </div>
          </el-form-item>

          <el-form-item label="默认显示语言">
            <el-select v-model="settings.language" @change="val => saveSetting('language', val)">
              <el-option label="简体中文 (zh-CN)" value="zh-CN" />
              <el-option label="English (en-US)" value="en" />
            </el-select>
          </el-form-item>

          <el-form-item label="行情交互特性">
            <el-checkbox v-model="settings.autoRefresh" @change="val => saveSetting('autoRefresh', val)">
              启用 K 线动态跳动效果
            </el-checkbox>
            <el-checkbox label="显示盘口详细数据" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 系统关于 -->
      <el-tab-pane>
        <template #label>
          <span class="tab-label"><el-icon><InfoFilled /></el-icon> 关于 InvestIQ AI</span>
        </template>
        
        <div class="about-section text-center p-12">
          <el-icon :size="60" color="var(--primary-accent)"><TrendCharts /></el-icon>
          <h2 class="mt-4">InvestIQ AI 智能投研系统</h2>
          <p class="text-secondary mb-8">Version 1.2.0-PRO (Build 20251219)</p>
          
          <el-descriptions class="mt-12" :column="1" border style="max-width: 400px; margin: 0 auto;">
            <el-descriptions-item label="核心内核">Spring Boot 3.x / Vue 3.x</el-descriptions-item>
            <el-descriptions-item label="AI 引擎">DeepSeek-V3 / GPT-4o-Turbo</el-descriptions-item>
            <el-descriptions-item label="开发团队">InvestIQ AI Pro Team</el-descriptions-item>
          </el-descriptions>
          
          <p class="mt-12 text-tertiary">© 2025 InvestIQ AI. All rights reserved.</p>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showChangePass" title="重置登录密码" width="400px">
      <el-form label-position="top">
        <el-form-item label="原密码">
          <el-input type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePass = false">取消</el-button>
        <el-button type="primary" @click="handlePassUpdate">提交更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Monitor, InfoFilled, Camera, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const showChangePass = ref(false)
const refreshFreq = ref(15)

const userEmail = computed(() => authStore.userEmail)
const loginTime = computed(() => authStore.loginTime)

const formattedLoginTime = computed(() => {
  if (!loginTime.value) return '--'
  return new Date(loginTime.value).toLocaleString()
})

const settings = ref({
  theme: 'dark',
  language: 'zh-CN',
  autoRefresh: true
})

const saveSetting = (key, value) => {
  localStorage.setItem(`setting_${key}`, value)
  ElMessage.success('配置已生效')
}

const handlePassUpdate = () => {
  ElMessage.success('密码已成功修改')
  showChangePass.value = false
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要安全退出当前账号吗？', '登出确认').then(() => {
    authStore.logout()
    router.push('/login')
  })
}

onMounted(() => {
  const theme = localStorage.getItem('setting_theme')
  if (theme) settings.value.theme = theme
})
</script>

<style scoped>
.settings-tabs { background: transparent !important; border: 1px solid var(--border-color) !important; border-radius: 12px; overflow: hidden; }
.settings-tabs :deep(.el-tabs__header) { background: rgba(255,255,255,0.02) !important; border-bottom-color: var(--border-color) !important; }
.settings-tabs :deep(.el-tabs__item.is-active) { background: var(--bg-color) !important; border-right-color: var(--border-color) !important; border-left-color: var(--border-color) !important; color: var(--primary-accent) !important; }

.account-profile-section { display: flex; gap: 4rem; padding: 3rem; }
.avatar-box { display: flex; flex-direction: column; align-items: center; }
.info-box { flex: 1; }

.tab-label { display: flex; align-items: center; gap: 8px; }
.max-w-2xl { max-width: 600px; }
.mt-12 { margin-top: 3rem; }
</style>

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