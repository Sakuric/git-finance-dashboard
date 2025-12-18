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

export function updateUserInfo(data) {
  return request({
    url: '/users/update',
    method: 'put',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/users/change-password',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/users/logout',
    method: 'post'
  })
}