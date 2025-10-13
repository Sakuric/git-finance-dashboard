// frontend/src/api/user.js
import request from '@/utils/request'

export function register(data) {
    return request({
        url: '/users/register',
        method: 'post',
        data
    })
}

export function login(data) {
    return request({
        url: '/users/login',
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

export function logout() {
    return request({
        url: '/users/logout',
        method: 'post'
    })
}