<template>
  <div class="auth-body">
    <div class="auth-container">
      <!-- 左侧背景装饰 -->
      <div class="auth-background">
        <div class="floating-chart">
          <div class="chart-line"></div>
          <div class="chart-line"></div>
          <div class="chart-line"></div>
          <div class="chart-line"></div>
        </div>
        <div class="floating-icons">
          <i class="fas fa-chart-line"></i>
          <i class="fas fa-coins"></i>
          <i class="fas fa-trending-up"></i>
          <i class="fas fa-dollar-sign"></i>
          <i class="fas fa-chart-pie"></i>
        </div>
      </div>

      <!-- 右侧登录/注册表单 -->
      <div class="auth-form-container">
        <!-- Logo和标题 -->
        <div class="auth-header">
          <div class="logo">
            <i class="fas fa-chart-pie"></i>
            <h1>量融</h1>
          </div>
          <p class="auth-subtitle">智能金融投资平台</p>
        </div>

        <!-- 登录表单 -->
        <div id="login-form" class="auth-form active" v-show="currentForm === 'login'">
          <h2>登录账户</h2>
          <div class="test-account-info">
            <p>测试账号：admin@example.com</p>
            <p>测试密码：password123</p>
          </div>
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label for="login-email">邮箱地址</label>
              <div class="input-group">
                <i class="fas fa-envelope"></i>
                <input 
                  type="email" 
                  id="login-email" 
                  v-model="loginForm.email" 
                  placeholder="请输入您的邮箱" 
                  required
                >
              </div>
              <span class="error-message" v-if="errors.loginEmail">{{ errors.loginEmail }}</span>
            </div>
          
            <div class="form-group">
              <label for="login-password">密码</label>
              <div class="input-group">
                <i class="fas fa-lock"></i>
                <input 
                  :type="showLoginPassword ? 'text' : 'password'" 
                  id="login-password" 
                  v-model="loginForm.password" 
                  placeholder="请输入您的密码" 
                  required
                >
                <button 
                  type="button" 
                  class="toggle-password" 
                  @click="togglePasswordVisibility('login')"
                  title="显示/隐藏密码"
                >
                  <i :class="showLoginPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                </button>
              </div>
              <span class="error-message" v-if="errors.loginPassword">{{ errors.loginPassword }}</span>
            </div>
          
            <div class="form-options">
              <label class="checkbox-container">
                <input type="checkbox" v-model="loginForm.rememberMe">
                <span class="checkmark"></span>
                记住我
              </label>
              <a href="#" @click.prevent="handleForgotPassword" class="forgot-password">忘记密码？</a>
            </div>
          
            <button type="submit" class="auth-btn" :disabled="loginLoading">
              <span class="btn-text" v-if="!loginLoading">登录</span>
              <div class="btn-loader" v-if="loginLoading">
                <i class="fas fa-spinner fa-spin"></i>
              </div>
            </button>
          </form>
        
          <div class="auth-footer">
            <p>还没有账户？<a href="#" @click.prevent="showRegisterForm" class="register-link">立即注册</a></p>
          </div>
        </div>

        <!-- 注册表单 -->
        <div id="register-form" class="auth-form" v-show="currentForm === 'register'">
          <h2>创建账户</h2>
          <form @submit.prevent="handleRegister">
            <div class="form-group">
              <label for="register-username">用户名</label>
              <div class="input-group">
                <i class="fas fa-user"></i>
                <input 
                  type="text" 
                  id="register-username" 
                  v-model="registerForm.username" 
                  placeholder="请输入用户名" 
                  required
                >
              </div>
              <span class="error-message" v-if="errors.registerUsername">{{ errors.registerUsername }}</span>
            </div>
          
            <div class="form-group">
              <label for="register-email">邮箱地址</label>
              <div class="input-group">
                <i class="fas fa-envelope"></i>
                <input 
                  type="email" 
                  id="register-email" 
                  v-model="registerForm.email" 
                  placeholder="请输入您的邮箱" 
                  required
                >
              </div>
              <span class="error-message" v-if="errors.registerEmail">{{ errors.registerEmail }}</span>
            </div>
          
            <div class="form-group">
              <label for="register-password">密码</label>
              <div class="input-group">
                <i class="fas fa-lock"></i>
                <input 
                  :type="showRegisterPassword ? 'text' : 'password'" 
                  id="register-password" 
                  v-model="registerForm.password" 
                  placeholder="请输入密码（至少8位）" 
                  required
                >
                <button 
                  type="button" 
                  class="toggle-password" 
                  @click="togglePasswordVisibility('register')"
                  title="显示/隐藏密码"
                >
                  <i :class="showRegisterPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                </button>
              </div>
              <span class="error-message" v-if="errors.registerPassword">{{ errors.registerPassword }}</span>
            </div>
          
            <div class="form-group">
              <label for="register-confirm-password">确认密码</label>
              <div class="input-group">
                <i class="fas fa-lock"></i>
                <input 
                  :type="showConfirmPassword ? 'text' : 'password'" 
                  id="register-confirm-password" 
                  v-model="registerForm.confirmPassword" 
                  placeholder="请再次输入密码" 
                  required
                >
                <button 
                  type="button" 
                  class="toggle-password" 
                  @click="togglePasswordVisibility('confirm')"
                  title="显示/隐藏密码"
                >
                  <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                </button>
              </div>
              <span class="error-message" v-if="errors.registerConfirmPassword">{{ errors.registerConfirmPassword }}</span>
            </div>
          
            <div class="form-group">
              <label class="checkbox-container">
                <input type="checkbox" v-model="registerForm.agreeTerms" required>
                <span class="checkmark"></span>
                我已阅读并同意<a href="#" class="terms-link">服务条款</a>和<a href="#" class="privacy-link">隐私政策</a>
              </label>
            </div>
          
            <button type="submit" class="auth-btn" :disabled="registerLoading">
              <span class="btn-text" v-if="!registerLoading">注册</span>
              <div class="btn-loader" v-if="registerLoading">
                <i class="fas fa-spinner fa-spin"></i>
              </div>
            </button>
          </form>
        
          <div class="auth-footer">
            <p>已有账户？<a href="#" @click.prevent="showLoginForm" class="login-link">立即登录</a></p>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示消息 -->
    <div id="notification" class="notification" :class="{ show: notification.show, [notification.type]: true }">
      <div class="notification-content">
        <i :class="notification.type === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"></i>
        <span>{{ notification.message }}</span>
      </div>
    </div>

    <!-- 忘记密码模态框 -->
    <div v-if="showForgotPasswordModal" class="forgot-password-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>重置密码</h3>
          <button class="modal-close" @click="closeForgotPasswordModal">&times;</button>
        </div>
        <div class="modal-body">
          <p>请输入您的邮箱地址，我们将向您发送密码重置链接。</p>
          <div class="form-group">
            <label for="reset-email">邮箱地址</label>
            <div class="input-group">
              <i class="fas fa-envelope"></i>
              <input 
                type="email" 
                id="reset-email" 
                v-model="resetEmail" 
                placeholder="请输入您的邮箱" 
                required
              >
            </div>
            <span class="error-message" v-if="errors.resetEmail">{{ errors.resetEmail }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn-secondary" @click="closeForgotPasswordModal">取消</button>
          <button type="button" class="btn-primary" @click="sendResetEmail" :disabled="resetLoading">
            <span v-if="!resetLoading">发送重置链接</span>
            <i v-if="resetLoading" class="fas fa-spinner fa-spin"></i>
          </button>
        </div>
      </div>
      <div class="modal-backdrop" @click="closeForgotPasswordModal"></div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    // 表单状态
    const currentForm = ref('login')
    const showLoginPassword = ref(false)
    const showRegisterPassword = ref(false)
    const showConfirmPassword = ref(false)
    const loginLoading = ref(false)
    const registerLoading = ref(false)
    const resetLoading = ref(false)
    const showForgotPasswordModal = ref(false)
    const resetEmail = ref('')

    // 表单数据
    const loginForm = reactive({
      email: '',
      password: '',
      rememberMe: false
    })

    const registerForm = reactive({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      agreeTerms: false
    })

    // 错误信息
    const errors = reactive({
      loginEmail: '',
      loginPassword: '',
      registerUsername: '',
      registerEmail: '',
      registerPassword: '',
      registerConfirmPassword: '',
      resetEmail: ''
    })

    // 通知消息
    const notification = reactive({
      show: false,
      message: '',
      type: 'success'
    })

    // 显示通知
    const showNotification = (message, type = 'success') => {
      notification.message = message
      notification.type = type
      notification.show = true
      
      setTimeout(() => {
        notification.show = false
      }, 3000)
    }

    // 切换表单
    const showRegisterForm = () => {
      currentForm.value = 'register'
      clearAllErrors()
    }

    const showLoginForm = () => {
      currentForm.value = 'login'
      clearAllErrors()
    }

    // 切换密码可见性
    const togglePasswordVisibility = (type) => {
      switch (type) {
        case 'login':
          showLoginPassword.value = !showLoginPassword.value
          break
        case 'register':
          showRegisterPassword.value = !showRegisterPassword.value
          break
        case 'confirm':
          showConfirmPassword.value = !showConfirmPassword.value
          break
      }
    }

    // 清除所有错误
    const clearAllErrors = () => {
      Object.keys(errors).forEach(key => {
        errors[key] = ''
      })
    }

    // 邮箱验证
    const isValidEmail = (email) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email)
    }

    // 验证登录表单
    const validateLoginForm = () => {
      clearAllErrors()
      let isValid = true

      if (!loginForm.email.trim()) {
        errors.loginEmail = '请输入邮箱地址'
        isValid = false
      } else if (!isValidEmail(loginForm.email)) {
        errors.loginEmail = '请输入有效的邮箱地址'
        isValid = false
      }

      if (!loginForm.password) {
        errors.loginPassword = '请输入密码'
        isValid = false
      } else if (loginForm.password.length < 6) {
        errors.loginPassword = '密码至少需要6个字符'
        isValid = false
      }

      return isValid
    }

    // 验证注册表单
    const validateRegisterForm = () => {
      clearAllErrors()
      let isValid = true

      if (!registerForm.username.trim()) {
        errors.registerUsername = '请输入用户名'
        isValid = false
      } else if (registerForm.username.length < 3) {
        errors.registerUsername = '用户名至少需要3个字符'
        isValid = false
      }

      if (!registerForm.email.trim()) {
        errors.registerEmail = '请输入邮箱地址'
        isValid = false
      } else if (!isValidEmail(registerForm.email)) {
        errors.registerEmail = '请输入有效的邮箱地址'
        isValid = false
      }

      if (!registerForm.password) {
        errors.registerPassword = '请输入密码'
        isValid = false
      } else if (registerForm.password.length < 6) {
        errors.registerPassword = '密码至少需要6个字符'
        isValid = false
      }

      if (!registerForm.confirmPassword) {
        errors.registerConfirmPassword = '请确认密码'
        isValid = false
      } else if (registerForm.password !== registerForm.confirmPassword) {
        errors.registerConfirmPassword = '两次输入的密码不一致'
        isValid = false
      }

      if (!registerForm.agreeTerms) {
        showNotification('请同意服务条款和隐私政策', 'error')
        isValid = false
      }

      return isValid
    }

    // 处理登录
    const handleLogin = async () => {
      if (!validateLoginForm()) return

      loginLoading.value = true

      // 模拟登录请求
      setTimeout(() => {
        if (loginForm.email === 'admin@example.com' && loginForm.password === 'password123') {
          authStore.login(loginForm.email)
          showNotification('登录成功！正在跳转...', 'success')
          
          setTimeout(() => {
            router.push('/dashboard')
          }, 1500)
        } else {
          showNotification('邮箱或密码错误', 'error')
        }
        loginLoading.value = false
      }, 1500)
    }

    // 处理注册
    const handleRegister = async () => {
      if (!validateRegisterForm()) return

      registerLoading.value = true

      // 模拟注册请求
      setTimeout(() => {
        showNotification('注册成功！请登录', 'success')
        
        // 清空表单
        Object.keys(registerForm).forEach(key => {
          if (typeof registerForm[key] === 'boolean') {
            registerForm[key] = false
          } else {
            registerForm[key] = ''
          }
        })
        
        registerLoading.value = false
        
        // 切换到登录表单
        setTimeout(() => {
          currentForm.value = 'login'
        }, 1500)
      }, 1500)
    }

    // 忘记密码
    const handleForgotPassword = () => {
      showForgotPasswordModal.value = true
    }

    const closeForgotPasswordModal = () => {
      showForgotPasswordModal.value = false
      resetEmail.value = ''
      errors.resetEmail = ''
    }

    const sendResetEmail = () => {
      errors.resetEmail = ''
      
      if (!resetEmail.value.trim()) {
        errors.resetEmail = '请输入邮箱地址'
        return
      }
      
      if (!isValidEmail(resetEmail.value)) {
        errors.resetEmail = '请输入有效的邮箱地址'
        return
      }
      
      resetLoading.value = true
      
      setTimeout(() => {
        closeForgotPasswordModal()
        showNotification('密码重置链接已发送到您的邮箱，请查收', 'success')
        resetLoading.value = false
      }, 1500)
    }

    return {
      // 状态
      currentForm,
      showLoginPassword,
      showRegisterPassword,
      showConfirmPassword,
      loginLoading,
      registerLoading,
      resetLoading,
      showForgotPasswordModal,
      resetEmail,
      
      // 表单数据
      loginForm,
      registerForm,
      
      // 错误和通知
      errors,
      notification,
      
      // 方法
      showRegisterForm,
      showLoginForm,
      togglePasswordVisibility,
      handleLogin,
      handleRegister,
      handleForgotPassword,
      closeForgotPasswordModal,
      sendResetEmail
    }
  }
}
</script>