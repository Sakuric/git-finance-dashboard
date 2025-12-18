<template>
  <div class="login-page">
    <!-- 粒子背景 -->
    <div class="particles-bg">
      <div class="particle" v-for="n in 60" :key="n"></div>
    </div>
    
    <!-- 网格线背景 -->
    <div class="grid-bg"></div>
    
    <!-- 光晕效果 -->
    <div class="glow-orb glow-1"></div>
    <div class="glow-orb glow-2"></div>
    
    <!-- 登录卡片 - 居中 -->
    <div class="login-card">
      <!-- Logo -->
      <div class="login-logo">
        <i class="fas fa-chart-pie"></i>
        <h1>量融</h1>
      </div>
      <p class="login-subtitle">智能金融投资平台</p>
      
      <!-- 登录表单 -->
      <div v-show="currentForm === 'login'">
        <div class="test-info">
          <span>测试账号: testuser</span>
          <span>密码: 123456</span>
        </div>
        
        <form @submit.prevent="handleLogin">
          <div class="input-field" :class="{ error: loginErrors.username }">
            <i class="fas fa-user"></i>
            <input
              type="text"
              id="login-username"
              name="username"
              autocomplete="username"
              v-model="loginForm.username"
              placeholder="用户名/邮箱"
              @blur="validateLoginField('username')"
            >
            <span class="error-msg" v-if="loginErrors.username">{{ loginErrors.username }}</span>
          </div>

          <div class="input-field" :class="{ error: loginErrors.password }">
            <i class="fas fa-lock"></i>
            <input
              :type="showPassword ? 'text' : 'password'"
              id="login-password"
              name="password"
              autocomplete="current-password"
              v-model="loginForm.password"
              placeholder="密码"
              @blur="validateLoginField('password')"
            >
            <button type="button" class="eye-btn" @click="showPassword = !showPassword">
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
            <span class="error-msg" v-if="loginErrors.password">{{ loginErrors.password }}</span>
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.rememberMe">
              <span>记住我</span>
            </label>
            <a href="#" @click.prevent="showForgotModal = true">忘记密码？</a>
          </div>
          
          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <i v-else class="fas fa-spinner fa-spin"></i>
          </button>
          
          <button type="button" class="quick-login-btn" @click="quickLogin" :disabled="loading">
            <i class="fas fa-bolt"></i>
            快速登录
          </button>
        </form>
        
        <p class="switch-form">
          还没有账户？<a href="#" @click.prevent="currentForm = 'register'">立即注册</a>
        </p>
      </div>
      
      <!-- 注册表单 -->
      <div v-show="currentForm === 'register'">
        <form @submit.prevent="handleRegister">
          <div class="input-field" :class="{ error: registerErrors.username }">
            <i class="fas fa-user"></i>
            <input type="text" v-model="registerForm.username" placeholder="用户名 (3-20字符)" @blur="validateRegisterField('username')">
            <span class="error-msg" v-if="registerErrors.username">{{ registerErrors.username }}</span>
          </div>
          
          <div class="input-field" :class="{ error: registerErrors.email }">
            <i class="fas fa-envelope"></i>
            <input type="email" v-model="registerForm.email" placeholder="邮箱" @blur="validateRegisterField('email')">
            <span class="error-msg" v-if="registerErrors.email">{{ registerErrors.email }}</span>
          </div>
          
          <div class="input-field" :class="{ error: registerErrors.password }">
            <i class="fas fa-lock"></i>
            <input type="password" v-model="registerForm.password" placeholder="密码 (至少6位)" @blur="validateRegisterField('password')">
            <span class="error-msg" v-if="registerErrors.password">{{ registerErrors.password }}</span>
          </div>
          
          <div class="input-field" :class="{ error: registerErrors.confirmPassword }">
            <i class="fas fa-lock"></i>
            <input type="password" v-model="registerForm.confirmPassword" placeholder="确认密码" @blur="validateRegisterField('confirmPassword')">
            <span class="error-msg" v-if="registerErrors.confirmPassword">{{ registerErrors.confirmPassword }}</span>
          </div>
          
          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="!loading">注 册</span>
            <i v-else class="fas fa-spinner fa-spin"></i>
          </button>
        </form>
        
        <p class="switch-form">
          已有账户？<a href="#" @click.prevent="currentForm = 'login'">立即登录</a>
        </p>
      </div>
    </div>
    
    <!-- 通知 -->
    <div class="toast" :class="{ show: toast.show, success: toast.type === 'success', error: toast.type === 'error' }">
      <i :class="toast.type === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"></i>
      {{ toast.message }}
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import * as userApi from '@/api/user'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    
    const currentForm = ref('login')
    const showPassword = ref(false)
    const loading = ref(false)
    const showForgotModal = ref(false)
    
    const loginForm = reactive({ username: '', password: '', rememberMe: false })
    const registerForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })
    const loginErrors = reactive({ username: '', password: '' })
    const registerErrors = reactive({ username: '', email: '', password: '', confirmPassword: '' })
    const toast = reactive({ show: false, message: '', type: 'success' })
    
    const validateLoginField = (field) => {
      if (field === 'username') {
        loginErrors.username = loginForm.username ? '' : '请输入用户名'
      } else if (field === 'password') {
        loginErrors.password = loginForm.password ? '' : '请输入密码'
      }
    }
    
    const validateRegisterField = (field) => {
      if (field === 'username') {
        if (!registerForm.username) registerErrors.username = '请输入用户名'
        else if (registerForm.username.length < 3 || registerForm.username.length > 20) registerErrors.username = '用户名需3-20个字符'
        else registerErrors.username = ''
      } else if (field === 'email') {
        const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!registerForm.email) registerErrors.email = '请输入邮箱'
        else if (!emailReg.test(registerForm.email)) registerErrors.email = '邮箱格式不正确'
        else registerErrors.email = ''
      } else if (field === 'password') {
        if (!registerForm.password) registerErrors.password = '请输入密码'
        else if (registerForm.password.length < 6) registerErrors.password = '密码至少6位'
        else registerErrors.password = ''
      } else if (field === 'confirmPassword') {
        if (!registerForm.confirmPassword) registerErrors.confirmPassword = '请确认密码'
        else if (registerForm.confirmPassword !== registerForm.password) registerErrors.confirmPassword = '两次密码不一致'
        else registerErrors.confirmPassword = ''
      }
    }
    
    const validateLogin = () => {
      validateLoginField('username')
      validateLoginField('password')
      return !loginErrors.username && !loginErrors.password
    }
    
    const validateRegister = () => {
      validateRegisterField('username')
      validateRegisterField('email')
      validateRegisterField('password')
      validateRegisterField('confirmPassword')
      return !registerErrors.username && !registerErrors.email && !registerErrors.password && !registerErrors.confirmPassword
    }
    
    const showToast = (message, type = 'success') => {
      toast.message = message
      toast.type = type
      toast.show = true
      setTimeout(() => toast.show = false, 3000)
    }
    
    const quickLogin = () => {
      loginForm.username = 'testuser'
      loginForm.password = '123456'
      handleLogin()
    }
    
    const handleLogin = async () => {
      if (!validateLogin()) return
      
      loading.value = true
      try {
        const res = await userApi.login({
          username: loginForm.username,
          password: loginForm.password
        })
        //存储
        if (res.code === 200 && res.data) {
          authStore.login(loginForm.username, res.data)
          showToast('登录成功！')
          setTimeout(() => router.push('/dashboard'), 1000)
        } else {
          showToast(res.message || '登录失败', 'error')
        }
      } catch (e) {
        showToast('网络错误', 'error')
      } finally {
        loading.value = false
      }
    }
    
    const handleRegister = async () => {
      if (!validateRegister()) return
      
      loading.value = true
      try {
        const res = await userApi.register({
          username: registerForm.username,
          email: registerForm.email,
          password: registerForm.password
        })
        
        if (res.code === 200) {
          showToast('注册成功！')
          currentForm.value = 'login'
        } else {
          showToast(res.message || '注册失败', 'error')
        }
      } catch (e) {
        showToast('网络错误', 'error')
      } finally {
        loading.value = false
      }
    }
    
    return {
      currentForm, showPassword, loading, showForgotModal,
      loginForm, registerForm, loginErrors, registerErrors, toast,
      validateLoginField, validateRegisterField,
      quickLogin, handleLogin, handleRegister
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0e17 0%, #1a1f2e 50%, #0d1321 100%);
  position: relative;
  overflow: hidden;
}

/* 网格背景 */
.grid-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(0, 166, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 166, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  z-index: 1;
}

/* 粒子背景 */
.particles-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  overflow: hidden;
}

.particle {
  position: absolute;
  width: 3px;
  height: 3px;
  background: #00a6ff;
  border-radius: 50%;
  opacity: 0;
  animation: float-up 10s infinite;
}

.particle:nth-child(odd) { background: #00d4ff; width: 2px; height: 2px; }
.particle:nth-child(3n) { background: #0088cc; width: 4px; height: 4px; }

.particle:nth-child(1) { left: 5%; animation-delay: 0s; }
.particle:nth-child(2) { left: 10%; animation-delay: 0.5s; }
.particle:nth-child(3) { left: 15%; animation-delay: 1s; }
.particle:nth-child(4) { left: 20%; animation-delay: 1.5s; }
.particle:nth-child(5) { left: 25%; animation-delay: 2s; }
.particle:nth-child(6) { left: 30%; animation-delay: 2.5s; }
.particle:nth-child(7) { left: 35%; animation-delay: 3s; }
.particle:nth-child(8) { left: 40%; animation-delay: 3.5s; }
.particle:nth-child(9) { left: 45%; animation-delay: 4s; }
.particle:nth-child(10) { left: 50%; animation-delay: 4.5s; }
.particle:nth-child(11) { left: 55%; animation-delay: 5s; }
.particle:nth-child(12) { left: 60%; animation-delay: 5.5s; }
.particle:nth-child(13) { left: 65%; animation-delay: 6s; }
.particle:nth-child(14) { left: 70%; animation-delay: 6.5s; }
.particle:nth-child(15) { left: 75%; animation-delay: 7s; }
.particle:nth-child(16) { left: 80%; animation-delay: 7.5s; }
.particle:nth-child(17) { left: 85%; animation-delay: 8s; }
.particle:nth-child(18) { left: 90%; animation-delay: 8.5s; }
.particle:nth-child(19) { left: 95%; animation-delay: 9s; }
.particle:nth-child(20) { left: 8%; animation-delay: 9.5s; }
.particle:nth-child(n+21) { left: calc((var(--n, 1) - 20) * 5%); animation-delay: calc((var(--n, 1) - 20) * 0.3s); }

@keyframes float-up {
  0% { transform: translateY(100vh) scale(0); opacity: 0; }
  10% { opacity: 0.8; }
  90% { opacity: 0.8; }
  100% { transform: translateY(-100vh) scale(1); opacity: 0; }
}

/* 光晕 */
.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 1;
}

.glow-1 {
  width: 400px;
  height: 400px;
  background: rgba(0, 166, 255, 0.15);
  top: -100px;
  right: -100px;
  animation: pulse-glow 4s infinite ease-in-out;
}

.glow-2 {
  width: 300px;
  height: 300px;
  background: rgba(0, 212, 255, 0.1);
  bottom: -50px;
  left: -50px;
  animation: pulse-glow 5s infinite ease-in-out reverse;
}

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 3rem 2.5rem;
  background: rgba(20, 25, 35, 0.85);
  border: 1px solid rgba(0, 166, 255, 0.2);
  border-radius: 20px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.5),
    0 0 100px rgba(0, 166, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00a6ff, #00d4ff, #00a6ff, transparent);
  border-radius: 20px 20px 0 0;
}

/* Logo */
.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.login-logo i {
  font-size: 2.5rem;
  color: #00a6ff;
  filter: drop-shadow(0 0 10px rgba(0, 166, 255, 0.5));
}

.login-logo h1 {
  font-size: 2rem;
  font-weight: 600;
  background: linear-gradient(135deg, #00a6ff, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  text-align: center;
  color: #8b949e;
  margin-bottom: 2rem;
  font-size: 0.95rem;
}

/* 测试信息 */
.test-info {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  padding: 0.75rem;
  background: rgba(0, 166, 255, 0.1);
  border: 1px solid rgba(0, 166, 255, 0.2);
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-size: 0.85rem;
  color: #00a6ff;
}

/* 输入框 */
.input-field {
  position: relative;
  margin-bottom: 1.25rem;
}

.input-field.error input {
  border-color: #ff4d4f;
}

.error-msg {
  position: absolute;
  bottom: -18px;
  left: 0;
  font-size: 12px;
  color: #ff4d4f;
}

.input-field i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6a737d;
  font-size: 1rem;
  transition: color 0.3s;
}

.input-field input {
  width: 100%;
  padding: 1rem 3rem 1rem 3rem;
  background: rgba(30, 37, 48, 0.8);
  border: 1px solid rgba(46, 55, 70, 0.8);
  border-radius: 10px;
  color: #e1e4e8;
  font-size: 1rem;
  transition: all 0.3s;
}

.input-field input:focus {
  outline: none;
  border-color: #00a6ff;
  box-shadow: 0 0 0 3px rgba(0, 166, 255, 0.15), 0 0 20px rgba(0, 166, 255, 0.1);
}

.input-field input:focus + i,
.input-field input:focus ~ i {
  color: #00a6ff;
}

.input-field input::placeholder {
  color: #6a737d;
}

.eye-btn {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #6a737d;
  cursor: pointer;
  padding: 0.25rem;
  transition: color 0.3s;
}

.eye-btn:hover {
  color: #00a6ff;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #8b949e;
  cursor: pointer;
}

.remember-me input {
  accent-color: #00a6ff;
}

.form-options a {
  color: #00a6ff;
  text-decoration: none;
  transition: color 0.3s;
}

.form-options a:hover {
  color: #00d4ff;
}

/* 按钮 */
.login-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #00a6ff, #0088cc);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 166, 255, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.quick-login-btn {
  width: 100%;
  padding: 0.875rem;
  margin-top: 0.75rem;
  background: transparent;
  border: 1px solid rgba(0, 166, 255, 0.5);
  border-radius: 10px;
  color: #00a6ff;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.quick-login-btn:hover {
  background: rgba(0, 166, 255, 0.1);
  border-color: #00a6ff;
}

/* 切换表单 */
.switch-form {
  text-align: center;
  margin-top: 1.5rem;
  color: #8b949e;
  font-size: 0.95rem;
}

.switch-form a {
  color: #00a6ff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.switch-form a:hover {
  color: #00d4ff;
}

/* Toast通知 */
.toast {
  position: fixed;
  top: 30px;
  right: 30px;
  padding: 1rem 1.5rem;
  background: rgba(20, 25, 35, 0.95);
  border: 1px solid rgba(0, 166, 255, 0.3);
  border-radius: 10px;
  color: #e1e4e8;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  transform: translateX(400px);
  transition: transform 0.3s ease;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.toast.show {
  transform: translateX(0);
}

.toast.success {
  border-color: #00c087;
}

.toast.success i {
  color: #00c087;
}

.toast.error {
  border-color: #ff4d4f;
}

.toast.error i {
  color: #ff4d4f;
}

/* 响应式 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 2rem 1.5rem;
  }
  
  .test-info {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>