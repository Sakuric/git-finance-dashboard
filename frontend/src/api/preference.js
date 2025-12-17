import request from './request'

export function savePreference(data) {
  return request({
    url: '/investment-preference',
    method: 'post',
    data
  })
}

export function getPreference(userId) {
  return request({
    url: `/investment-preference/user/${userId}`,
    method: 'get'
  })
}

export function deletePreference(userId) {
  return request({
    url: `/investment-preference/user/${userId}`,
    method: 'delete'
  })
}

export function hasPreference() {
  return Promise.reject(new Error('Check endpoint not implemented'))
}