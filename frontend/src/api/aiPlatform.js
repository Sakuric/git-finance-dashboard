import request from './request'

export function getAiPlatforms(userId) {
  return request({
    url: `/ai-models/user/${userId}`,
    method: 'get'
  })
}

export function getAiPlatformsList() {
  return request({
    url: '/ai-models',
    method: 'get'
  })
}

export function saveAiPlatform(data) {
  return request({
    url: '/ai-models',
    method: 'post',
    data
  })
}

export function updateAiPlatform(data) {
  return request({
    url: `/ai-models/${data.id}`,
    method: 'put',
    data
  })
}

export function deleteAiPlatform(id) {
  return request({
    url: `/ai-models/${id}`,
    method: 'delete'
  })
}

export function testApiKey(data) {
  return request({
    url: '/ai-models/test',
    method: 'post',
    data
  })
}