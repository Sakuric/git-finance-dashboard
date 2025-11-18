// 导入API服务
import { login as loginApi, register as registerApi } from './src/api/user.js'

// 登录和注册功能
document.addEventListener('DOMContentLoaded', function() {
    
    // 获取DOM元素
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    
    // 调试信息
    console.log('DOM加载完成');
    console.log('loginForm:', loginForm);
    console.log('registerForm:', registerForm);
    
    // 添加全局测试函数
    window.testLogin = function() {
        console.log('测试登录函数被调用');
        document.getElementById('login-username').value = 'admin';
        document.getElementById('login-password').value = 'password123';
        
        // 直接调用表单提交处理函数
        const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
        loginForm.dispatchEvent(submitEvent);
    };
    
    // 忘记密码功能
    const forgotPasswordLink = document.querySelector('.forgot-password');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', function(e) {
            e.preventDefault();
            handleForgotPassword();
        });
    }
    
    // 密码可见性切换
    const togglePasswordButtons = document.querySelectorAll('.toggle-password');
    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function() {
            const input = this.previousElementSibling;
            const icon = this.querySelector('i');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
    
    // 登录表单验证和提交
    if (loginForm) {
        console.log('登录表单找到，添加事件监听器');
        loginForm.addEventListener('submit', function(e) {
            console.log('登录表单提交事件被触发');
            e.preventDefault();
            e.stopPropagation();
            
            if (validateLoginForm()) {
                console.log('表单验证通过');
                // 显示加载状态
                showButtonLoader('loginForm');
                
                // 获取表单数据
                const username = document.getElementById('login-username').value;
                const password = document.getElementById('login-password').value;
                
                console.log('准备调用登录API，用户名:', username);
                
                // 调用登录API
                loginApi({ username, password })
                    .then(response => {
                        console.log('登录成功:', response);
                        
                        // 保存登录状态和token
                        localStorage.setItem('isLoggedIn', 'true');
                        localStorage.setItem('username', username);
                        localStorage.setItem('loginTime', new Date().toISOString());
                        
                        if (response.data) {
                            localStorage.setItem('token', response.data);
                        }
                        
                        showNotification('登录成功！正在跳转...', 'success');
                        
                        // 延迟跳转到主页
                        setTimeout(() => {
                            window.location.href = 'index.html';
                        }, 1500);
                    })
                    .catch(error => {
                        console.error('登录失败:', error);
                        hideButtonLoader('loginForm');
                        
                        // 根据错误类型显示不同的错误信息
                        let errorMessage = '登录失败，请重试';
                        if (error.response) {
                            if (error.response.status === 401) {
                                errorMessage = '邮箱或密码错误';
                            } else if (error.response.status === 403) {
                                errorMessage = '账户已被禁用';
                            } else if (error.response.status === 404) {
                                errorMessage = '用户不存在';
                            }
                        } else if (error.message) {
                            errorMessage = error.message;
                        }
                        
                        showNotification(errorMessage, 'error');
                    });
            } else {
                console.log('表单验证失败');
            }
        });
        
        // 添加按钮点击事件监听器作为备用
        const loginButton = loginForm.querySelector('button[type="submit"]');
        if (loginButton) {
            console.log('登录按钮找到，添加点击事件监听器');
            loginButton.addEventListener('click', function(e) {
                console.log('登录按钮点击事件被触发');
                // 手动触发表单提交事件
                const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
                loginForm.dispatchEvent(submitEvent);
            });
        } else {
            console.error('未找到登录按钮');
        }
    } else {
        console.error('未找到登录表单');
    }
    
    // 注册表单验证和提交
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (validateRegisterForm()) {
                // 显示加载状态
                showButtonLoader('registerForm');
                
                // 获取表单数据
                const username = document.getElementById('register-username').value;
                const email = document.getElementById('register-email').value;
                const password = document.getElementById('register-password').value;
                
                // 调用注册API
                registerApi({ username, email, password })
                    .then(response => {
                        console.log('注册成功:', response);
                        
                        showNotification('注册成功！请登录', 'success');
                        
                        // 清空表单
                        registerForm.reset();
                        hideButtonLoader('registerForm');
                        
                        // 切换到登录表单
                        setTimeout(() => {
                            if (registerForm) registerForm.classList.remove('active');
                            if (loginForm) loginForm.classList.add('active');
                        }, 1500);
                    })
                    .catch(error => {
                        console.error('注册失败:', error);
                        hideButtonLoader('registerForm');
                        
                        // 根据错误类型显示不同的错误信息
                        let errorMessage = '注册失败，请重试';
                        if (error.response) {
                            if (error.response.status === 409) {
                                errorMessage = '邮箱已被注册';
                            } else if (error.response.status === 400) {
                                errorMessage = '输入信息有误';
                            }
                        } else if (error.message) {
                            errorMessage = error.message;
                        }
                        
                        showNotification(errorMessage, 'error');
                    });
            }
        });
    }
    
    // 登录表单验证
    function validateLoginForm() {
        let isValid = true;
        clearAllErrors();
        
        const username = document.getElementById('login-username');
        const password = document.getElementById('login-password');
        
        // 验证用户名
        if (!username.value.trim()) {
            showError('login-username-error', '请输入用户名');
            isValid = false;
        } else if (username.value.length < 3) {
            showError('login-username-error', '用户名至少需要3个字符');
            isValid = false;
        }
        
        // 验证密码
        if (!password.value) {
            showError('login-password-error', '请输入密码');
            isValid = false;
        } else if (password.value.length < 6) {
            showError('login-password-error', '密码至少需要6个字符');
            isValid = false;
        }
        
        return isValid;
    }
    
    // 注册表单验证
    function validateRegisterForm() {
        let isValid = true;
        clearAllErrors();
        
        const username = document.getElementById('register-username');
        const email = document.getElementById('register-email');
        const password = document.getElementById('register-password');
        const confirmPassword = document.getElementById('register-confirm-password');
        const agreeTerms = document.getElementById('agree-terms');
        
        // 验证用户名
        if (!username.value.trim()) {
            showError('register-username-error', '请输入用户名');
            isValid = false;
        } else if (username.value.length < 3) {
            showError('register-username-error', '用户名至少需要3个字符');
            isValid = false;
        }
        
        // 验证邮箱
        if (!email.value.trim()) {
            showError('register-email-error', '请输入邮箱地址');
            isValid = false;
        } else if (!isValidEmail(email.value)) {
            showError('register-email-error', '请输入有效的邮箱地址');
            isValid = false;
        }
        
        // 验证密码
        if (!password.value) {
            showError('register-password-error', '请输入密码');
            isValid = false;
        } else if (password.value.length < 6) {
            showError('register-password-error', '密码至少需要6个字符');
            isValid = false;
        }
        
        // 验证确认密码
        if (!confirmPassword.value) {
            showError('register-confirm-password-error', '请确认密码');
            isValid = false;
        } else if (password.value !== confirmPassword.value) {
            showError('register-confirm-password-error', '两次输入的密码不一致');
            isValid = false;
        }
        
        // 验证服务条款
        if (!agreeTerms.checked) {
            showNotification('请同意服务条款和隐私政策', 'error');
            isValid = false;
        }
        
        return isValid;
    }
    
    // 邮箱验证函数
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    // 显示错误信息
    function showError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
        }
    }
    
    // 清除所有错误信息
    function clearAllErrors() {
        const errorElements = document.querySelectorAll('.error-message');
        errorElements.forEach(element => {
            element.textContent = '';
        });
    }
    
    // 显示按钮加载状态
    function showButtonLoader(formId) {
        const form = document.getElementById(formId);
        if (form) {
            const button = form.querySelector('button[type="submit"]');
            const originalText = button.querySelector('.btn-text').textContent;
            button.querySelector('.btn-text').style.display = 'none';
            button.querySelector('.btn-loader').style.display = 'block';
            button.disabled = true;
            button.dataset.originalText = originalText;
        }
    }
    
    // 隐藏按钮加载状态
    function hideButtonLoader(formId) {
        const form = document.getElementById(formId);
        if (form) {
            const button = form.querySelector('button[type="submit"]');
            button.querySelector('.btn-text').style.display = 'block';
            button.querySelector('.btn-loader').style.display = 'none';
            button.disabled = false;
        }
    }
    
    // 显示通知
    function showNotification(message, type = 'success') {
        // 移除现有通知
        const existingNotification = document.querySelector('.notification');
        if (existingNotification) {
            existingNotification.remove();
        }
        
        // 创建新通知
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        
        const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
        
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas ${icon}"></i>
                <span>${message}</span>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // 显示通知
        setTimeout(() => {
            notification.classList.add('show');
        }, 100);
        
        // 自动隐藏通知
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 3000);
    }
    
    // 检查登录状态
    function checkLoginStatus() {
        const isLoggedIn = localStorage.getItem('isLoggedIn');
        if (isLoggedIn === 'true') {
            // 如果已登录，重定向到主页
            window.location.href = 'index.html';
        }
    }
    
    // 初始化时检查登录状态
    checkLoginStatus();
});

// 退出登录函数（在index.html中使用）
function logout() {
    // 清除本地存储
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('loginTime');
    localStorage.removeItem('token');
    
    // 跳转到登录页面
    window.location.href = 'login.html';
}

// 忘记密码功能
function handleForgotPassword() {
    // 创建忘记密码对话框
    const modal = document.createElement('div');
    modal.className = 'forgot-password-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>重置密码</h3>
                <button class="modal-close" id="closeModal">&times;</button>
            </div>
            <div class="modal-body">
                <p>请输入您的邮箱地址，我们将向您发送密码重置链接。</p>
                <div class="form-group">
                    <label for="reset-email">邮箱地址</label>
                    <div class="input-group">
                        <i class="fas fa-envelope"></i>
                        <input type="email" id="reset-email" placeholder="请输入您的邮箱" required>
                    </div>
                    <span class="error-message" id="reset-email-error"></span>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-secondary" id="cancelReset">取消</button>
                <button type="button" class="btn-primary" id="sendReset">发送重置链接</button>
            </div>
        </div>
        <div class="modal-backdrop"></div>
    `;
    
    document.body.appendChild(modal);
    
    // 绑定事件
    const closeModal = document.getElementById('closeModal');
    const cancelReset = document.getElementById('cancelReset');
    const sendReset = document.getElementById('sendReset');
    
    function closeForgotPasswordModal() {
        modal.remove();
    }
    
    closeModal.addEventListener('click', closeForgotPasswordModal);
    cancelReset.addEventListener('click', closeForgotPasswordModal);
    
    // 点击背景关闭模态框
    modal.querySelector('.modal-backdrop').addEventListener('click', closeForgotPasswordModal);
    
    // 发送重置链接
    sendReset.addEventListener('click', function() {
        const emailInput = document.getElementById('reset-email');
        const email = emailInput.value.trim();
        const errorElement = document.getElementById('reset-email-error');
        
        // 清除之前的错误
        errorElement.textContent = '';
        
        // 验证邮箱
        if (!email) {
            errorElement.textContent = '请输入邮箱地址';
            return;
        }
        
        if (!isValidEmail(email)) {
            errorElement.textContent = '请输入有效的邮箱地址';
            return;
        }
        
        // 显示加载状态
        sendReset.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 发送中...';
        sendReset.disabled = true;
        
        // 这里可以调用API发送重置链接
        setTimeout(() => {
            closeForgotPasswordModal();
            showNotification('密码重置链接已发送到您的邮箱，请查收', 'success');
        }, 1500);
    });
}

// 邮箱验证函数（全局使用）
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}