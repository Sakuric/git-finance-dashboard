import request from './request'

// 用户登录
export function login(data) {
  return request({
    url: '/api/users/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/api/users/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/api/users/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/api/users/update',
    method: 'put',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/api/users/change-password',
    method: 'post',
    data
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/api/users/logout',
    method: 'post'
  })
}