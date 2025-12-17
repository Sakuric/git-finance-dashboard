import request from './request'

export function login(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/users/info',
    method: 'get'
  })
}

export function updateUserInfo() {
  return Promise.reject(new Error('Backend missing /users/update'))
}

export function changePassword() {
  return Promise.reject(new Error('Backend missing /users/change-password'))
}

export function logout() {
  return request({
    url: '/users/logout',
    method: 'post'
  })
}