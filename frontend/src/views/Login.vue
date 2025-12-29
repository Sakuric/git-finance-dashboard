<template>
  <div class="login-page">
    <!-- 动态背景层 -->
    <div class="particles-bg">
      <div class="particle" v-for="n in 30" :key="n" :style="getParticleStyle(n)"></div>
    </div>
    
    <div class="glow-orb glow-1"></div>
    <div class="glow-orb glow-2"></div>
    
    <!-- 登录容器 -->
    <transition name="el-zoom-in-center">
      <el-card class="login-card-new" shadow="always">
        <div class="login-header">
          <el-icon class="logo-icon" :size="48"><TrendCharts /></el-icon>
          <h1>InvestIQ AI</h1>
          <p>智能金融投资决策系统</p>
        </div>

        <!-- 登录表单 -->
        <div v-if="currentForm === 'login'" class="form-wrapper">
          <!-- 测试账号信息已按要求删除 -->
          
          <el-form :model="loginForm" label-position="top" @submit.prevent="handleLogin">
            <el-form-item label="用户名">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名/邮箱" 
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="密码">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                :prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>

            <div class="flex-between mb-4">
              <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
              <el-button link type="primary" @click="showForgotModal = true">忘记密码？</el-button>
            </div>

            <!-- 登录按钮组：已对齐 -->
            <div class="button-group">
              <el-button type="primary" size="large" :loading="loading" @click="handleLogin">
                立即登录
              </el-button>
              
              <el-button size="large" plain @click="quickLogin">
                <el-icon class="mr-2"><Flashlight /></el-icon> 极速登录
              </el-button>
            </div>
          </el-form>
          
          <div class="switch-link">
            还没有账户？<el-link type="primary" @click="currentForm = 'register'">立即注册</el-link>
          </div>
        </div>

        <!-- 注册表单 -->
        <div v-else class="form-wrapper">
          <el-form :model="registerForm" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="3-20个字符" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item label="电子邮箱">
              <el-input v-model="registerForm.email" placeholder="接收通知邮件" :prefix-icon="Message" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" placeholder="至少6位字符" :prefix-icon="Lock" show-password size="large" />
            </el-form-item>
            
            <el-button type="primary" class="w-full mt-4" size="large" :loading="loading" @click="handleRegister">
              完成注册
            </el-button>
          </el-form>
          
          <div class="switch-link">
            已有账户？<el-link type="primary" @click="currentForm = 'login'">返回登录</el-link>
          </div>
        </div>
      </el-card>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, Lock, Message, TrendCharts, MagicStick as Flashlight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as userApi from '@/api/user'

const router = useRouter()
const authStore = useAuthStore()

const currentForm = ref('login')
const loading = ref(false)
const showForgotModal = ref(false)
const loginForm = reactive({ username: '', password: '', rememberMe: false })
const registerForm = reactive({ username: '', email: '', password: '' })

const getParticleStyle = (n) => ({
  left: `${Math.random() * 100}%`,
  animationDelay: `${Math.random() * 5}s`,
  animationDuration: `${5 + Math.random() * 5}s`
})

const quickLogin = () => {
  // 保持功能逻辑，但移除界面上的测试账号展示
  loginForm.username = 'testuser'
  loginForm.password = '123456'
  handleLogin()
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请填写完整的登录信息')
  }
  
  loading.value = true
  try {
    const res = await userApi.login(loginForm)
    if (res.code === 200) {
      authStore.login(loginForm.username, res.data)
      ElMessage.success('欢迎回来！正在进入系统...')
      setTimeout(() => router.push('/dashboard'), 800)
    } else {
      ElMessage.error(res.message || '账号或密码错误')
    }
  } catch (e) {
    ElMessage.error('服务连接失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.email || !registerForm.password) {
    return ElMessage.warning('请填写完整的注册信息')
  }
  loading.value = true
  try {
    const res = await userApi.register(registerForm)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      currentForm.value = 'login'
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #040608;
  position: relative;
  overflow: hidden;
}

.login-card-new {
  width: 440px;
  background: rgba(13, 17, 23, 0.8) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 20px !important;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-icon {
  color: var(--primary-accent);
  margin-bottom: 1rem;
  filter: drop-shadow(0 0 12px var(--glow-color));
}

.login-header h1 {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  letter-spacing: 2px;
}

.login-header p {
  color: var(--text-tertiary);
  font-size: 0.9rem;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 1.5rem;
}

.button-group .el-button {
  flex: 1;
  margin: 0 !important; /* 覆盖Element Plus默认边距 */
}

.mb-6 { margin-bottom: 1.5rem; }
.mb-4 { margin-bottom: 1rem; }
.mt-4 { margin-top: 1rem; }
.w-full { width: 100%; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }

.switch-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 2rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.switch-link .el-link {
  vertical-align: middle;
}

/* 动效背景 */
.particles-bg {
  position: absolute;
  inset: 0;
}

.particle {
  position: absolute;
  bottom: -10px;
  width: 2px;
  height: 2px;
  background: var(--primary-accent);
  border-radius: 50%;
  opacity: 0;
  animation: rise linear infinite;
}

@keyframes rise {
  0% { transform: translateY(0) scale(1); opacity: 0; }
  50% { opacity: 0.6; }
  100% { transform: translateY(-100vh) scale(1.5); opacity: 0; }
}

.glow-orb {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.15;
  z-index: 1;
}

.glow-1 { top: -10%; right: -5%; background: var(--primary-accent); }
.glow-2 { bottom: -10%; left: -5%; background: var(--secondary-accent); }
</style>